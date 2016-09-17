package irobotcreatepi;

public class Tinker {

	public static void main(String[] args) throws Exception {
		
		String devname = "/dev/ttyUSB0";
		//IRobotCreate robot = new IRobotCreateSerial("COM3");
		System.out.println("USING PORT '"+devname+"'");
		IRobotCreate robot = new IRobotCreateSerial(devname);
		
		robot.setMode(IRobotCreate.MODE.PASSIVE); // Required at startup
		Thread.sleep(1000); // Wait for mode change
		
		robot.setMode(IRobotCreate.MODE.FULL);
		robot.setLEDs(true, true, 0, 128);
		
		/*
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier("COM3");
		SerialPort serialPort = (SerialPort) portIdentifier.open("CreatePi",2000);					
		serialPort.setSerialPortParams(57600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
		
		OutputStream os = serialPort.getOutputStream();
		
		os.write(0x80); // START (go to passive mode)
		os.flush();
		
		Thread.sleep(1000);
		
		os.write(0x84); // FULL (go to full mode)
		//os.flush();
		
		//Thread.sleep(1000);
		
		os.write(0x8B); // LEDs
		os.write(8);
		os.write(0);
		os.write(128);
		//os.flush();
		
		Thread.sleep(1000);
		
		System.out.println("DONE");
		*/

	}

}
