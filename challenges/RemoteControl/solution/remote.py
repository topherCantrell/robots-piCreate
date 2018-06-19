import time
import irobot_create
import sensor_packets


# Connect to the roomba
roomba = irobot_create.Create('/dev/ttyUSB0') # On the Raspberry Pi 
#roomba = irobot_create.Create('COM4') # On my PC    
    
# SAFE mode automatically stops when we bump something. FULL does not.
roomba.set_mode_safe()

sens = sensor_packets.InfraredCharacterOmni()

while True:
    roomba.get_sensor_packet(sens)
    print(sens)
    time.sleep(1)

# I put the robot 3 feet from the wall. The final distance read was:
# 928 mm = 36.5 inches = roughly 3 feet

# Graceful sleep (allows for charging)
roomba.set_mode_passive()
time.sleep(1)
roomba.set_mode_stop()
