import time
import irobot_create
import sensor_packets

# Connect to the roomba
# roomba = irobot_create.Create('/dev/ttyUSB0') # On the Raspberry Pi 
roomba = irobot_create.Create('COM4') # On my PC
    
# We won't be driving. Pick the "safe" mode.
roomba.set_mode_safe()    

# Note middle C
roomba.set_song(1,[ [60,8] ])

# Note D above middle C
roomba.set_song(2,[ [62,8] ])

# Note E above middle C
roomba.set_song(3,[ [64,8] ])

# Mary had a Little Lamb
#roomba.set_song(0, [
#    [64,24],[62,24],[60,24],[62,24],[64,24],[64,24],[64,24],
#    [0,24],
#    [62,24],[62,24],[62,24],
#    [0,24],
#    [64,24],[64,24],[64,24],
#    [0,24],
#    [64,24], [62,24],[60,24],
#    [62,24],[64,24],[64,24],[64,24],
#    [64,24],[62,24],[62,24],[64,24],[62,24],[60,24]
#    ])

# Here is another way
LEN  = 24
C    = [60,LEN]
D    = [62,LEN]
E    = [64,LEN]
REST = [ 0,LEN]
#
roomba.set_song(0,[E,D,C,D,E,E,E,REST,
                   D,D,D,REST,
                   E,E,E,REST,
                   E,D,C,D,E,E,E,E,D,D,E,D,C])

# Use this packet to read the buttons over and over
sens = sensor_packets.Buttons()

while True:
    roomba.get_sensor_packet(sens)
    if sens.is_day:
        roomba.play_song(1)
    elif sens.is_hour:
        roomba.play_song(2)
    elif sens.is_minute:
        roomba.play_song(3)      
    elif sens.is_clean:
        roomba.play_song(0)  
    elif sens.is_dock:
        break
    
# Graceful sleep (allows for charging)
roomba.set_mode_passive()
time.sleep(1)
roomba.set_mode_stop()
    