# iRobotCreate and Raspberry Pi

## The iRobot Create platform

All of the Roomba robots have the "Open Interface" built into them. You can hack any of the models to get access to the
serial port.

The "Create" products are Roomba robots the iRobot company made just for hobbyists. The serial port is available through a
provided serial cable (and through the cargo-bay connector in the first series of "Creates".

There are two versions of the serial interface. The older models use this protocol:

http://www.irobot.com/filelibrary/pdfs/hrd/create/Create%20Open%20Interface_v2.pdf

Newer hardware, including the iRobot Create2 use this protocol:

http://www.irobotweb.com/~/media/MainSite/PDFs/About/STEM/Create/iRobot_Roomba_600_Open_Interface_Spec.pdf

This is the landing page for Create:

http://www.irobot.com/About-iRobot/STEM/Create-2.aspx

Here is a project page for Create and the Raspberry Pi:
http://www.irobot.com/~/media/MainSite/PDFs/About/STEM/Create/RaspberryPi_Tutorial.pdf

![](https://github.com/topherCantrell/robots-iRobotCreate/blob/master/art/iRobotDIN.jpg)

USB to TTL Serial Cable (adafruit)

https://www.adafruit.com/products/954

Red: power, 
Black: ground, 
White:RX into USB port, 
Green:TX out of USB port

On the iCreate1 data goes TO the robot on pin 1 of the cargo bay connector

The cable that came with the iCreate did not have power connected through. The wires were not connected to the plug.
But the cable from adafruit does supply power correctly.

5V converter
https://www.adafruit.com/products/1385

![](https://github.com/topherCantrell/robots-iRobotCreate/blob/master/art/buck.jpg)

![](https://github.com/topherCantrell/robots-iRobotCreate/blob/master/art/roombaDIN.jpg)

![](https://github.com/topherCantrell/robots-iRobotCreate/blob/master/art/usbserial.jpg)

![](https://github.com/topherCantrell/robots-iRobotCreate/blob/master/art/picreate1.jpg)

![](https://github.com/topherCantrell/robots-iRobotCreate/blob/master/art/picreate2.jpg)

![](https://github.com/topherCantrell/robots-iRobotCreate/blob/master/art/picreate1b.jpg)

![](https://github.com/topherCantrell/robots-iRobotCreate/blob/master/art/picreate2b.jpg)

https://developer.mbed.org/cookbook/iRobot-Create-Robot

http://www.societyofrobots.com/member_tutorials/book/export/html/350


TODO get a remote, virtual walls

## Challenges

  - Play a song
  - Make and play 4 songs
  - Attach the songs to buttons
  
  - Forward till bump, wall, light, cliff, virtual wall
  
  - Dock algorithm
  
  - Turn to degree (45,90,180). Make routines.
  
  - Drive to distance. Make routine.
  
  - Drive in a square.
  - Navigate obstacles.
  - Knock over cups.

  - Judge distance with encoders (use tape measure). Make routines.
  - Judge turn with encoders. Make routines.
  
## Remote control

Use a websocket for low latency. 

Tank drive

Sensor page
