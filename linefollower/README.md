# SparkFun Line Follower Array

I2C. Requires 5V supply, but the hookup-guide suggests the signals are defaulted to 3.3V, which
should work with 5V and 3.3V boards. 

https://www.sparkfun.com/products/13582

https://learn.sparkfun.com/tutorials/sparkfun-line-follower-array-hookup-guide

The data sheet

https://cdn.sparkfun.com/assets/learn_tutorials/4/3/4/sx150x_789.pdf

Schematic

https://cdn.sparkfun.com/datasheets/Sensors/Infrared/SparkFun%20Line%20Follower%20Array_v10.pdf

```
sudo i2cdetect -y 1
```

My board is at the default 3E

Linux commands to talk I2C:

```
i2cset -y 1 0x3E 4 55
i2cget -y 1 0x3E r
```

Note that the value is not persisted over power cycle. I wonder if any are.


