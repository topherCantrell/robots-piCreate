import smbus
import time
from sx1509_registers import REG_RESET, REG_INTERRUPT_MASK_A
from sx1509_registers import REG_DIR_A, REG_DIR_B, REG_DATA_A, REG_DATA_B 
from sx1509_registers import REG_MISC, REG_CLOCK, REG_DEBOUNCE_CONFIG, REG_DEBOUNCE_ENABLE_B
    
# TODO Interrupts

'''

From the schematics:
https://cdn.sparkfun.com/datasheets/Sensors/Infrared/SparkFun%20Line%20Follower%20Array_v10.pdf

Interrupt pin brought off board

Data Port A:
I/O[0] <- SEN0
I/O[1] <- SEN1
I/O[2] <- SEN2
I/O[3] <- SEN3
I/O[4] <- SEN4
I/O[5] <- SEN5
I/O[6] <- SEN6
I/O[7] <- SEN7

Data Port B:
I/O[8] -> -IREN (enable all IR LEDs:0=ON, 1=OFF)
I/O[9] -> -FBEN (enable feedback LEDs on top of the board:0=ON, 1=OFF)

Oscillator is for IR LED brightness (PWM)

Debounce allows any input to be debounced -- change is accepted only if the input value is identical at two consecutive sampling times

'''


class Follower:
    
    def __init__(self, i2c_address=0x3E):
        self._bus = smbus.SMBus(1)
        self._i2c_address = i2c_address
        
        self.reset()
        
        test = self._read_word(REG_INTERRUPT_MASK_A)
        if test != 0xFF00:
            raise OSError('Could not talk to SX1509')
        
        self._write_byte(REG_DIR_A,  0xFF) # All sensor lines are inputs
        self._write_byte(REG_DIR_B,  0xFC) # Enable IR and Enable Feedback are outputs
        self._write_byte(REG_DATA_B, 0x01) # IR LEDs off, Feedback on
        
        self._bar_strobe = False
        self._invert_bits = False
    
    def reset(self):
        self._write_byte(REG_RESET,0x12)
        self._write_byte(REG_RESET,0x34)
    
    def debounce_config(self,config_value):
        
        # First make sure clock is configured
        temp = self._read_byte(REG_MISC)
        if (temp & 0x70) == 0:            
            temp |= (1<<4) # ClkX = fOSC/(2^(1-1)) = fOSC/1 = fOSC (no divider)
            self._write_byte(REG_MISC, temp)            
            
        temp = self._read_byte(REG_CLOCK)
        if (temp & 0x60) == 0:
            temp |= (1<<6) # Default to internal osc
            self._write_byte(REG_CLOCK,temp)
            
        config_value &= 7 # 3-bit value. Higher = longer
        self._write_byte(REG_DEBOUNCE_CONFIG, config_value)
    
    def debounce_enabled(self,pin,is_enabled):
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
        
        # Assign values to each bit, -127 to 127, sum, and divide
                
        raw = self.get_raw()
          
        accumulator = 0
        bits_counted = 0
        
        for i in range(7,3,-1):
            if ((raw>>i)&1)==1:
                accumulator += ((-32 * (i - 3)) + 1)
                bits_counted += 1
        for i in range(4):
            if ((raw>>i)&1)==1:
                accumulator += ((32 * (4 - i)) - 1)
                bits_counted += 1
                
        if bits_counted>0:
            return accumulator/bits_counted
        else:
            return 0  
    
    def get_density(self):
        
        raw = self.get_raw()        
        bits_counted = 0
        
        for i in range(8):
            if ((raw>>i)&1)==1:
                bits_counted += 1
                
        return bits_counted
    
    def _read_byte(self,addr):
        return self._bus.read_byte_data(self._i2c_address,addr) 
    
    def _write_byte(self,addr,value):
        self._bus.write_byte_data(self._i2c_address,addr,value)    
        
    def _read_word(self,addr):
        # TODO
        pass
    
    def _write_word(self,addr,value):
        # TODO
        pass    
        
follower = Follower()

while True:
    print(follower.read_raw())
    time.sleep(0.5)
