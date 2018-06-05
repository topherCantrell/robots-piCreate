import smbus
import time

class Follower:
    
    def __init__(self, i2c_address=0x3E):
        self._bus = smbus.SMBus(1)
        self._i2c_address = i2c_address
        
    def read_raw(self):
        value = self._bus.read_byte_data(self._i2c_address,0x11) 
        return value
    
follower = Follower()

while True:
    print(follower.read_raw())
    time.sleep(0.5)
