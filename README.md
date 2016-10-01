# iRobotCreate and Raspberry Pi

## The iRobot Create platform ##

All of the Roomba robots have the "Open Interface" built into them. You can hack any of the models to get access to the
serial port.

The "Create" products are Roomba robots the iRobot company made just for hobbyists. The serial port is available through a
provided serial cable (and through the cargo-bay connector in the first series of "Creates".

There are two versions of the serial interface. The older models use this protocol:

[http://www.irobot.com/filelibrary/pdfs/hrd/create/Create%20Open%20Interface_v2.pdf](http://www.irobot.com/filelibrary/pdfs/hrd/create/Create%20Open%20Interface_v2.pdf)

Newer hardware, including the iRobot Create2 use this protocol:

[http://www.irobotweb.com/~/media/MainSite/PDFs/About/STEM/Create/iRobot_Roomba_600_Open_Interface_Spec.pdf](http://www.irobotweb.com/~/media/MainSite/PDFs/About/STEM/Create/iRobot_Roomba_600_Open_Interface_Spec.pdf)

This is the landing page for Create:

[http://www.irobot.com/About-iRobot/STEM/Create-2.aspx](http://www.irobot.com/About-iRobot/STEM/Create-2.aspx)

Here is a project page for Create and the Raspberry Pi:
[http://www.irobot.com/~/media/MainSite/PDFs/About/STEM/Create/RaspberryPi_Tutorial.pdf](http://www.irobot.com/~/media/MainSite/PDFs/About/STEM/Create/RaspberryPi_Tutorial.pdf)

![](https://github.com/topherCantrell/iRobotCreatePI/blob/master/art/iRobotDIN.jpg)

USB to TTL Serial Cable (adafruit)

[https://www.adafruit.com/products/954](https://www.adafruit.com/products/954)

Red: power, 
Black: ground, 
White:RX into USB port, 
Green:TX out of USB port

5V converter
[https://www.adafruit.com/products/1385](https://www.adafruit.com/products/1385)

![](https://github.com/topherCantrell/iRobotCreatePI/blob/master/art/buck.jpg)

![](https://github.com/topherCantrell/iRobotCreatePI/blob/master/art/roombaDIN.jpg)

![](https://github.com/topherCantrell/iRobotCreatePI/blob/master/art/usbserial.jpg)
