import irobot_create
import time

# Connect to the roomba
roomba = irobot_create.Roomba('/dev/ttyUSB0') # On the Raspberry Pi 
#roomba = irobot_create.Create('COM4') # On my PC 

# Drive forward a little ways
roomba.set_drive_straight(200)
time.sleep(2)

# Turn around
roomba.set_drive_spin_cw(200)
time.sleep(2.2)

# Drive back to start
roomba.set_drive_straight(200)
time.sleep(2)
    
roomba.set_drive_stop()   

# Disconnect from the roomba
roomba.close()