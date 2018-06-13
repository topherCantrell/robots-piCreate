import time
import irobot_create

# Connect to the roomba
roomba = irobot_create.Create('/dev/ttyUSB0') # On the Raspberry Pi 
    
# We are driving, but stop automatically if we encounter problems
roomba.set_mode_safe()    

DRIVE_SPEED = 300
DRIVE_TIME = 5

TURN_SPEED = 300
TURN_TIME = 2

for i in range(4):
    roomba.set_drive_straight(DRIVE_SPEED)
    time.sleep(DRIVE_TIME)
    
    roomba.set_drive_spin_ccw(TURN_SPEED)
    time.sleep(TURN_TIME)
    
roomba.set_drive_stop()

# Try precision driving from drive_util

# How did you determine the drive times and speeds? Do the times depend on the
# speeds? Are the errors larger when the drive time is longer? What about when
# the battery drains down a bit?
    
# Graceful sleep (allows for charging)
roomba.set_mode_passive()
time.sleep(1)
roomba.set_mode_stop()
    