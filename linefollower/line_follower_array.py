import smbus
import time
from sx1509_registers import REG_RESET, REG_INTERRUPT_MASK_A, REG_DIR_A, REG_DIR_B,\
    REG_DATA_A, REG_DATA_B, REG_MISC, REG_CLOCK, REG_DEBOUNCE_CONFIG, REG_DEBOUNCE_ENABLE_B

class Follower:
    
    def __init__(self, i2c_address=0x3E):
        self._bus = smbus.SMBus(1)
        self._i2c_address = i2c_address
        
        self.reset()
        
        test = self._read_word(REG_INTERRUPT_MASK_A)
        if test != 0xFF00:
            raise OSError('Could not talk to SX1509')
        
        self._write_byte(REG_DIR_A,  0xFF)
        self._write_byte(REG_DIR_B,  0xFC)
        self._write_byte(REG_DATA_B, 0x01)
        
        self._bar_strobe = False
        self._invert_bits = False
    
    def reset(self):
        self._write_byte(REG_RESET,0x12)
        self._write_byte(REG_RESET,0x34)
    
    def debounce_config(self,config_value):
        
        # First make sure clock is configured
        temp = self._read_byte(REG_MISC)
        if (temp & 0x70) == 0:
            temp |= (1<<4) # Just default to no divider if not set
            self._write_byte(REG_MISC, temp)            
        temp = self._read_byte(REG_CLOCK)
        if (temp & 0x60) == 0:
            temp |= (1<<6) # Default to internal osc
            self._write_byte(REG_CLOCK,temp)
            
        config_value &= 7 # 3-bit value
        self._write_byte(REG_DEBOUNCE_CONFIG, config_value)
    
    def debounce_enabled(self,pin,is_enabled):
        # TODO how about a disable?
        debounce_enable = self._read_word(REG_DEBOUNCE_ENABLE_B)
        if is_enabled:
            debounce_enable |= (1 << pin)
        else:
            debounce_enable &= ~(1<<pin)        
        
        self._write_word(REG_DEBOUNCE_ENABLE_B, debounce_enable)
    
    def config_clock(self,osc_source,osc_function,osc_freq,osc_divider):
        
        '''
        RegClock constructed as follows:
        6:5 - Oscillator frequency souce
            00: off, 01: external input, 10: internal 2MHz, 1: reserved
        4 - OSCIO pin function
            0: input, 1 ouptut
        3:0 - Frequency of oscout pin
            0: LOW, 0xF: high, else fOSCOUT = FoSC/(2^(RegClock[3:0]-1))
        '''
        
        osc_source = (osc_source & 3) << 5      # 2-bit value, bits 6:5
        osc_function = (osc_function & 1) << 4  # 1-bit value bit 4
        osc_freq = (osc_freq & 15)              # 4-bit value, bits 3:0
        regClock = osc_source | osc_function | osc_freq
        self._write_byte(REG_CLOCK, regClock)

        # Config RegMisc[6:4] with oscDivider
        # 0: off, else ClkX = fOSC / (2^(RegMisc[6:4] -1))
        osc_divider = (osc_divider & 7) << 4    # 3-bit value, bits 6:4
        regMisc = self._read_byte(REG_MISC)
        regMisc &= ~(7 << 4)
        regMisc |= osc_divider
        self._write_byte(REG_MISC, regMisc)
    
    def set_bar_strobe(self,is_strobe):
        self._bar_strobe = is_strobe    
    
    def set_invert_bits(self,is_invert):
        self._invert_bits = is_invert        
    
    # ########################################## BAR FUNCTIONS
    
    def get_raw(self):
        if self._bar_strobe:  
            self._write_byte(REG_DATA_B, 0x02) # Turn on IR
            # delay(2)  //Additional delay required after IR is turned on to allow LEDs to achieve full brightness
            self._write_byte(REG_DATA_B, 0x00) #Turn on feedback
        else:
            self._write_byte(REG_DATA_B, 0x00) #make sure both IR and indicators are on
  
        #Operate the I2C machine
        ret = self._read_byte(REG_DATA_A )  #Peel the data off port A
          
        if self.invertBits: # Invert the bits if needed
            ret ^= 0xFF        
          
        if self._bar_strobe:
            self._write_byte(REG_DATA_B, 0x03) #Turn off IR and feedback when done
        # //delay(8)
        
        return ret
    
    def get_position(self):
        # TODO
        pass
    
    def get_density(self):
        # TODO
        pass
    
    def _read_byte(self,addr):
        return self._bus.read_byte_data(self._i2c_address,addr) 
    
    def _write_byte(self,addr,value):
        self._bus.write_byte_data(self._i2c_address,addr,value)        
        
follower = Follower()

while True:
    print(follower.read_raw())
    time.sleep(0.5)
