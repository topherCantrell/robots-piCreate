Drew a black line on my desk pad. Power on.

```

sudo apt-get install i2c-tools

? for python ?

Raw data at 

i2cdetect -y 1

shows 3E

i2cget -y 1 0x3E 0x11  # Read RegDataA (sensors)

i2cget -y 1 0x3E 0x10  # Read RegDataB outputs. at startup 0
i2cget -y 1 0x3E 0x0E # Data directions ... both FF at startup (input pins)
i2cget -y 1 0x3E 0x0E # Data directions ... both FF at startup (input pins)

i2cset -y 1 0x3E 0x0E 0xFC

lights go out


i2cget -y 1 0x3E 0x10 # Value 3 (both outputs 1)
i2cset -y 1 0x3E 0x10 0 # both outputs 0 -- enabled

lights back on




```