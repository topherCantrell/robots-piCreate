import time
import irobot_create

# Connect to the roomba
roomba = irobot_create.Create('/dev/ttyUSB0') # On the Raspberry Pi 
    
# We are driving, but stop automatically if we encounter problems
roomba.set_mode_safe()    

DRIVE_SPEED = 200
DRIVE_TIME = 2

TURN_SPEED = 200
TURN_TIME = 1.1

for i in range(4):
    roomba.set_drive_straight(DRIVE_SPEED)
    time.sleep(DRIVE_TIME)
    
    roomba.set_drive_spin_cw(TURN_SPEED)
    time.sleep(TURN_TIME)
    
roomba.set_drive_stop() 
    
# Graceful sleep (allows for charging)
roomba.set_mode_passive()
roomba.set_mode_stop()
    