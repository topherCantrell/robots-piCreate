import serial
import time

class Create(object):

    def __init__(self, port_name):
        self.roomba = serial.Serial(port_name,115200)
        self.set_mode_passive()        
        
    def _signed_word_to_bytes(self,value):
        # The roomba uses 16 bit signed words, MSB first
        if value<0:
            value = value + 65536
        return bytes([(value>>8)&255,value&255])
            
    # Mode
        
    def set_mode_passive(self):
        self.roomba.write(b'\x80')
        time.sleep(1)
        
    def set_mode_safe(self):
        self.roomba.write(b'\x83')
        time.sleep(1)
    
    def set_mode_full(self):
        self.roomba.write(b'\x84')
        time.sleep(1)
        
    def set_mode_stop(self):
        self.roomba.write(b'\xAD')        
        
    def set_mode_reset(self):
        self.roomba.write(b'\x07')
        
    # Drive
    
    def set_drive(self,velocity_mms,radius_mm):
        # Special radius cases (handled in other functions)
        # straight = 32767 (forward) or -32768 (backwards)
        # spin CW = radius -1
        # spin CCW = radius 1
        cmd = b'\x89'+self._signed_word_to_bytes(velocity_mms)+self._signed_word_to_bytes(radius_mm)
        self.roomba.write(cmd)
        
    def set_drive_stop(self):
        self.set_drive(0,0)
    
    def set_drive_straight(self,velocity_mms):
        self.set_drive(velocity_mms,32767)
        
    def set_drive_spin_cw(self,velocity_mms):
        self.set_drive(velocity_mms,-1)
        
    def set_drive_spin_ccw(self,velocity_mms):
        self.set_drive(velocity_mms,1)
        
    def set_drive_direct(self,left_mms,right_mms):        
        # mm/s 
        # The manual says range is -500 to +500, but my roomba goes higher
        cmd = b'\x91'+ self._signed_word_to_bytes(right_mms)+self._signed_word_to_bytes(left_mms)
        self.roomba.write(cmd)
    
    def set_drive_pwm(self,left_pwm,right_pwm):
        # pwm 
        # The manual says range is -255 to +255, but my roomba goes higher
        cmd = b'\x92'+ self._signed_word_to_bytes(right_pwm)+self._signed_word_to_bytes(left_pwm)
        self.roomba.write(cmd)
        
    # LEDs
    
    def set_leds(self,check,dock,spot,debris,power_color,power_intensity):
        # individual LEDs are on/off
        # power color and intensity are 0-255
        ind = 0
        if check:
            ind = ind | 8
        if dock:
            ind = ind | 4
        if spot:
            ind = ind | 2
        if debris:
            ind = ind | 1        
        cmd = bytes([0x8B,ind,power_color,power_intensity])        
        self.roomba.write(cmd)
        
    # Music
    
    def set_song(self,number,notes):
        # number: 0,1,2, or 3
        # notes are pairs of midiNumber/duration
        #   midiNumber: 31 through 127 (outside this range is a silence)
        #   duration: 1/64ths of a second
        cmd = b'\x8C' + bytes([number,len(notes)])
        for note in notes:
            cmd = cmd + bytes([note[0],note[1]])
        self.roomba.write(cmd)          
    
    def play_song(self,number):
        # number: 0,1,2, or 3
        cmd = b'\x8D' + bytes([number])
        self.roomba.write(cmd)    
    
    # Sensors
    
    def get_sensor_packet(self, sensor_object):
        cmd = b'\x8E' + bytes([sensor_object.ID])
        self.roomba.write(cmd)
        data = self.roomba.read(sensor_object.SIZE)
        sensor_object.decode(data,0)        
    
    def get_sensor_multi_packets(self, sensor_objects):
        # x95
        pass
        
    # TODO Asynchronous sensors: stream start, pause, stop
    
if __name__ == '__main__':
    
    #import sensor_packets
    import sensor_groups
    
    roomba = Create('/dev/ttyUSB0')
    
    roomba.set_mode_safe()    
      
    sens = sensor_groups.Group_107()
    roomba.get_sensor_packet(sens)
    print(sens)    
    
    '''  
    roomba.set_drive_spin_ccw(100)
    time.sleep(2)
    roomba.set_drive_stop()
    
    sens = sensor_packets.Angle()
    roomba.get_sensor_packet(sens)
    print(sens)
    
    roomba.set_drive_spin_cw(150)
    time.sleep(2)
    roomba.set_drive_stop()
    
    sens = sensor_packets.Angle()
    roomba.get_sensor_packet(sens)
    print(sens)    
            
    time.sleep(2)
    '''
    
    roomba.set_mode_passive()
    time.sleep(1)
    roomba.set_mode_stop()
        