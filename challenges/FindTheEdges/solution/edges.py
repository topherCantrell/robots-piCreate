import time
import irobot_create
import sensor_packets

# Connect to the roomba
roomba = irobot_create.Create('/dev/ttyUSB0') # On the Raspberry Pi 
#roomba = irobot_create.Create('COM4') # On my PC    
    
# SAFE mode automatically stops when we bump something. FULL does not.
roomba.set_mode_full()

# A sensor packet for distance
distance_packet = sensor_packets.Distance()

# A sensor packet for wall detection
sens = sensor_packets.BumpsAndWheelDrops()

# Read the distance before we start. This clears out any value so far.
roomba.get_sensor_packet(distance_packet)

# Start rolling forward
roomba.set_drive_straight(200)

while True:
    
    # Read the bump sensor
    roomba.get_sensor_packet(sens)
    if sens.bump_right or sens.bump_left:
        # If we hit the wall then break out of this loop
        break   
    
    # A short delay to keep stress off the serial link
    time.sleep(0.1)
    
# Halt the wheels
roomba.set_drive_stop()

# Get the distance traveled
roomba.get_sensor_packet(distance_packet)
print(distance_packet.since_last_mm)

# I put the robot 3 feet from the wall. The final distance read was:
# 928 mm = 36.5 inches = roughly 3 feet

# Graceful sleep (allows for charging)
roomba.set_mode_passive()
time.sleep(1)
roomba.set_mode_stop()
