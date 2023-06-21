import serial
import time
import sys

import threading

class Roomba(object):

    """
    
    The OpenInterface (OI from now on) has four modes:
      - Off     - This is the mode when the robot is first turned on
      - Passive - You can read sensors, but you can't control motors
      - Safe    - Full control, but cliff sensors override your commands
      - Full    - Full control, no safety features

    When the OI is in "Off" mode, send the "set_mode_passive" to wake the OI up. Then
    set "Safe" or "Full" mode as desired.
    
    """
    
    def __init__(self, port_name='/dev/ttyUSB0', baud=115200, stream_update_cb=None):
        """Create a new robot driver object.

        The older Create1 defaults to baud 57600. The Create2 defaults to 115200.

        Args:
            port_name: name of the port e.g. "COM4". Defaults to "/dev/ttyUSB0"
            baud: baud rate of the interface. Defaults to 115200 (Create2 default)
        """                

        self._buffer = b''
        self._stream_update_cb=stream_update_cb
        self._watch_for_stream = False
        self._num_stream_packets = 0

        self.roomba = serial.Serial(port_name,115200)        
        self.set_mode_passive()  
        self.set_mode_safe()

        th = threading.Thread(target=self._input_thread)
        th.daemon = True
        th.start()

    def _clear_input_buffer(self):
        self._buffer = b''

    def _wait_for_input(self, num_bytes, sleep_secs=0.25):
        while len(self._buffer)<num_bytes:
            time.sleep(sleep_secs)
        ret = self._buffer[:num_bytes]
        self._buffer = self._buffer[num_bytes:]
        return ret

    def _input_thread(self):        
        try:            
            while True:
                if self._watch_for_stream:
                    # Get in sync ... wait for a 19
                    while True:
                        d = self.roomba.read(1)
                        if d[0] == 19:
                            break                                   
                    size = self.roomba.read(1)[0]
                    data = self.roomba.read(size)
                    _ = self.roomba.read(1)[0] # TODO check this. For now, don't waste the time
                    self._num_stream_packets += 1
                    pos = 0
                    ok = True
                    for p in self._stream_packets:
                        if pos+p.SIZE+1 > size or p.ID != data[pos]:
                            ok = False
                            # This might be a left over spew from a previous run
                            # Just ignore this packet and wait for next                          
                            break
                        p.decode(data,pos+1)
                        pos += p.SIZE + 1
                    if ok:
                        self._stream_update_cb(self._stream_packets)
                else:
                    # Might be watching for the spew after a reset
                    d = self.roomba.read(1)
                    self._buffer += d
        finally:
            print('The input reader thread should not exit')
            
        
    def close(self):
        """Put OI in "passive" then "off" mode.

        The "set_mode_stop" command does not seem to work in any mode but "passive". This
        function sets the mode to "passive" then to "off".

        Call this when you are done with the robot. This allows for charging. 
        """
        self.set_mode_passive()
        time.sleep(1)
        self.set_mode_stop()      
        
    def _signed_word_to_bytes(self,value):
        # The OI uses 16 bit signed words, MSB first
        if value<0:
            value = value + 65536
        return bytes([(value>>8)&255,value&255])
            
    ##### Mode Commands
                
    def set_mode_passive(self):
        """Opcode 128: Set the mode to passive.

        The OI will play an ACK tone if going from "off" to "passive". Otherwise it is silent.

        The documentation calls this the "start" command. This is the only way to get the
        OI out of "Off" mode. In all other mode, it places the OI in "passive" mode. Thus we
        just call it set_mode_passive.        
        """
        self.roomba.write(b'\x80')
        # Give it a second to wake up if it is in "off" mode.
        time.sleep(1)

    def set_mode_reset(self):
        """Opcode 7: Reset the OI.

        This resets the robot as if you had removed and reinserted the battery. The default
        baud rate is reset, and the OI enters mode "Off".
        """
        self.roomba.write(b'\x07')
        
    def set_mode_stop(self):
        """Opcode 173: Set the mode to off.

        The OI will play a tone to indicate that it is shutting off.

        This does not seem to work unless the OI is in "passive" mode. Use the "close" method
        instead.
        """
        self.roomba.write(b'\xAD')

    def set_baud(self, code):
        """Opcode 129: Change the baud rate.

        The new rate persists until the robot is power cycled. The reset command above will 
        also set it back.

          code   Baud
          0      300
          1      600
          2      1200
          3      2400
          4      4800
          5      9600
          6      14400
          7      19200
          8      28800
          9      38400
          10     57600
          11     115200

        Args:
            code: an int value from the left side of the table above
        
        """
        
    def set_mode_safe(self):
        """Opcode 131: Enter safe mode.

        Opcode 130 does the same thing.        
        """
        self.roomba.write(b'\x83')
        time.sleep(1)
    
    def set_mode_full(self):
        """Opcode 132: Enter full mode.
        """        
        self.roomba.write(b'\x84')
        time.sleep(1)
    
    def set_power_down(self):
        """Opcode 133: Power down and enter passive mode.
        """
        self.roomba.write(b'\x85')
        time.sleep(1)
    
    ##### Cleaning Algorithms
    
    def set_clean_spot(self):
        # 134
        self.roomba.write(b'\x86')
    
    def set_clean_clean(self):
        # 135
        self.roomba.write(b'\x87')
    
    def set_clean_max_clean(self):
        # 136
        self.roomba.write(b'\x88')
        
    ##### Drive
    
    def set_drive(self,velocity_mms,radius_mm):
        # 137
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
        # 145      
        # mm/s 
        # The manual says range is -500 to +500, but my roomba goes higher
        cmd = b'\x91'+ self._signed_word_to_bytes(right_mms)+self._signed_word_to_bytes(left_mms)
        self.roomba.write(cmd)
    
    def set_drive_pwm(self,left_pwm,right_pwm):
        # 146
        # pwm 
        # The manual says range is -255 to +255, but my roomba goes higher
        cmd = b'\x92'+ self._signed_word_to_bytes(right_pwm)+self._signed_word_to_bytes(left_pwm)
        self.roomba.write(cmd)
        
    def set_seek_dock(self):
        # 143
        cmd = b'\x8F'
        self.roomba.write(cmd)
        
    ##### Cleaning Motors
    
    def set_cleaning_motors(self, main_brush_on, main_brush_outward, side_brush_on, side_brush_cw, vacuum_on):
        """Opcode 138: Control cleaning bruses and vacuum.

        Args:
            main_brush_on: True to start the main brush motor at 100% PWM or False to turn it off
            main_brush_outward: True to rotate the brush motor outward or False to rotate inward
            side_brush_on: True to start the side brush motor at 100% PWM or False to turn it off
            side_brush_cw: True to spin the side brush CW or False for CCW
            vacuum_on: True to start the vacuum at 100% PWM or False to turn it off
        """
        value = 0
        if main_brush_outward:
            value |= 16
        if side_brush_cw:
            value |= 8
        if main_brush_on:
            value |= 4
        if vacuum_on:
            value |= 2
        if side_brush_on:
            value |= 1

        self.roomba.write(bytes[0x8A, value])
    
    def set_cleaning_motors_pwm(self,main_brush_pwm,side_brush_pwm,vacuum_pwm):
        # 90 144
        # TODO
        pass
        
    ##### LED Control
    
    def set_leds(self,check,dock,spot,debris,power_color,power_intensity):
        # 139
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
        
    ##### Music Control
    
    def set_song(self,number,notes):
        # 140
        # number: 0,1,2, or 3
        # notes are pairs of midiNumber/duration
        #   midiNumber: 31 through 127 (outside this range is a silence)
        #   duration: 1/64ths of a second
        #
        # The documentation says song number are 0-4, which would be 5 total songs.
        # My experimentation shows only 0-3 are valid.
        #
        # Experimentation shows that each song can be at most 16 notes. If you pass in more
        # than 16 then the notes spill into the next song. This allows you to use song 0
        # as one big 16*4 note song.
        cmd = b'\x8C' + bytes([number,len(notes)])
        for note in notes:
            cmd = cmd + bytes([note[0],note[1]])
        self.roomba.write(cmd)          
    
    def play_song(self,number):
        # 141
        # number: 0,1,2, or 3
        cmd = b'\x8D' + bytes([number])
        self.roomba.write(cmd)    
    
    ##### Sensor Control
    
    def get_sensor_packet(self, sensor_object):
        # 142
        self._clear_input_buffer()
        cmd = b'\x8E' + bytes([sensor_object.ID])
        self.roomba.write(cmd)
        #
        data = self._wait_for_input(sensor_object.SIZE)
        sensor_object.decode(data,0)
    
    def get_sensor_multi_packets(self, sensor_objects):
        # 149
        self._clear_input_buffer()
        data = [0x95, len(sensor_objects)]
        total_to_read = 0
        for s in sensor_objects:
            total_to_read += s.SIZE
            data.append(s.ID)
        self.roomba.write(bytes(data))
        #
        data = self._wait_for_input(total_to_read)
        pos = 0
        for sensor_object in sensor_objects:
            sensor_object.decode(data,pos)
            pos += sensor_object.SIZE

    def start_packet_stream(self, sensor_objects):
        # 148
        self._clear_input_buffer()
        self._watch_for_stream = True
        # We need this in the decode
        self._stream_packets = sensor_objects
        data = [0x94, len(sensor_objects)]
        for s in sensor_objects:            
            data.append(s.ID)
        self.roomba.write(bytes(data))
        # data starts flowing in now ... one chunk every 15ms

    def pause_packet_stream(self):
        # 150
        cmd = b'\x96\x00'
        self.roomba.write(cmd)

    def resume_packet_stream(self):
        # 150
        cmd = b'\x96\x01'
        self.roomba.write(cmd)

# Handler for stream updates
def got_stream_update(packets):
    for p in packets:
        print(p)

    
if __name__ == '__main__':
    
    import sensor_groups
    import sensor_packets
        
    #roomba = Roomba('/dev/ttyUSB0')
    roomba = Roomba('COM3',stream_update_cb=got_stream_update)


    p1 = sensor_packets.Buttons()
    p2 = sensor_packets.ChargingState()
    p3 = sensor_packets.OIMode()
    roomba.get_sensor_multi_packets([p1, p2, p3])
    print(p1)
    print(p2)
    print(p3)    

    raise('OOPS')
            
    p1 = sensor_packets.Buttons()
    p2 = sensor_packets.ChargingState()
    p3 = sensor_packets.OIMode()

    #roomba.get_sensor_packet(p1)
    #roomba.get_sensor_packet(p2)
    #roomba.get_sensor_packet(p3)
    #roomba.get_sensor_multi_packets([p1,p2,p3])
    #print(p1)
    #print(p2)
    #print(p3)

    roomba.start_packet_stream([p3,p2,p1])
    time.sleep(5)
        
    roomba.close()

    time.sleep(15)
    print(roomba._num_stream_packets)
    