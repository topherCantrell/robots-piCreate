import serial
import time

class Create(object):

    def __init__(self, port_name):
        self.roomba = serial.Serial(port_name,115200)
        # To connect to the robot:
        # You must set the PASSIVE mode to activate the serial interface.
        #
        # When you are done with the robot:
        # You set the PASSIVE mode
        # Then the STOP command
        
    def _signed_word_to_bytes(self,value):
        # The roomba uses 16 bit signed words, MSB first
        if value<0:
            value = value + 65536
        return bytes([(value>>8)&255,value&255])
    
    def _signed_word_from_bytes(self,value):
        value = (value[0]<<8) | value[1]
        if value>32767:
            value = value - 65536
        return value
            
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
        cmd = b'\x8C' + bytes([number,len(notes)])
        for note in notes:
            cmd = cmd + bytes([note[0],note[1]])
        self.roomba.write(cmd)          
    
    def play_song(self,number):
        cmd = b'\x8D' + bytes([number])
        self.roomba.write(cmd)    
    
    # Sensors TODO
