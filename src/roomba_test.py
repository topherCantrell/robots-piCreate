import serial
import time

# Roomba defaults to 115200 baud
roomba = serial.Serial('COM4',115200)

print("Sending OI START command (128). You should hear a high-pitch beep.")
roomba.write(b'\x80')
print("Sleeping 1 second (giving roomba time to change mode)")
time.sleep(1)

print("Sending SAFE MODE command (131).")
# Send SAFE MODE (131) or FULL MODE (132)
roomba.write(b'\x83')
print("Sleeping 1 second (giving roomba time to change mode)")
time.sleep(1)

print("Setting song 0 to three notes C,E,G.")
roomba.write(b'\x8C\x00\x03\x48\x20\x4C\x20\x4F\x20')

print("Setting song 1 to three notes G,E,C.")
roomba.write(b'\x8C\x01\x03\x4F\x10\x4C\x10\x48\x10')

print("Playing song 0")
roomba.write(b'\x8D\x00')
print("Sleeping 3 seconds for song to play")
time.sleep(3)

print("Playing song 1")
roomba.write(b'\x8D\x01')
print("Sleeping 3 seconds for song to play")
time.sleep(3)

print("Sending OI START command (128) going back to passive mode.")
roomba.write(b'\x80')
print("Sleeping 1 second (giving roomba time to change mode)")
time.sleep(1)

print("Sending STOP command (173). You should hear a low-pitch beep.")
roomba.write(b'\xAD')

print("Done")