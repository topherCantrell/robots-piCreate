## 9/17/2016 ##

I am making a robot for the FRC team at Columbia to program against. Having a few of those will allow the students to tinker with programming without having to have full blown hardware. I will implement the same Java interfaces as the official hardware.

First to get my PC talking to the iCreate through a FTDI prop plug.

Blue wire is data TO the robot. Green wire is data FROM the robot.

I wrote serial port code in java to talk to the robot through the FTDI prop plug.

I'll use a USB plug from the pi too. That will free up the onboard pi serial port for communication when there is no network (like at Columbia High).

Now running the java code from the pi. Easy to do. WinSCP copies the compile class files to the pi. I can compile in Eclipse.

Power may be an issue. I wish I could power the pi separately so I could kill power to the create without rebooting the pi. Probably not a big deal ... just keep it powered up. I'll keep the wallwart plug in case.