import smbus
import time
from sx1509_registers import REG_RESET, REG_INTERRUPT_MASK_A, REG_DIR_A, REG_DIR_B,\
    REG_DATA_B

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
    
    def reset(self):
        self._write_byte(REG_RESET,0x12)
        self._write_byte(REG_RESET,0x34)
    
    def debounce_config(self,config_value):
        # TODO
        pass
    
    def debounce_enabled(self,pin):
        # TODO
        pass    
    
    def config_clock(self,osc_source,osc_function,osc_freq,osc_divider):
        # TODO
        pass
    
    def set_bar_strobe(self):
        # TODO
        pass
    
    def clear_bar_strobe(self):
        # TODO
        pass
    
    def set_invert_bits(self):
        # TODO
        pass
    
    def clear_invert_bits(self):
        # TODO
        pass
    
    '''
    def enable_interrupt(self,pin,rise_fall):
        # TODO
        pass
    
    def interrupt_source(self):
        # TODO
        pass
        '''
    
    # ########################################## BAR FUNCTIONS
    
    def get_raw(self):
        # TODO
        return self._read_byte(0x11)
    
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
