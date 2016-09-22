package irobotcreatepi;

import java.util.Arrays;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import irobotcreatepi.IRobotCreateV1.SENSOR_PACKET;

public class TinkerV1 {

	public static void main(String[] args) throws Exception {
		
		//String devname = "/dev/ttyUSB0"; // Pi
		String devname = "COM4"; // PC
		
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(devname);
		SerialPort serialPort = (SerialPort) portIdentifier.open("CreatePi",2000);					
		serialPort.setSerialPortParams(57600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
		serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);		
		serialPort.setInputBufferSize(256);
		
		System.out.println("USING PORT '"+devname+"'");
		IRobotCreateV1 robot = new IRobotCreateV1(serialPort.getInputStream(),serialPort.getOutputStream());
		
		robot.setMode(IRobotCreateV1.MODE.PASSIVE); // Required at startup
		Thread.sleep(1000); // Wait for mode change	
		
		robot.setMode(IRobotCreateV1.MODE.FULL);
		Thread.sleep(1000);
		
		//robot.driveSpin(100,true);
		
		robot.setLEDs(false, false, 128, 128);
		
		robot.storeSong(0, 72,8, 76,8, 79,8);
		Thread.sleep(1000);
		robot.playSong(0);
		
		int [] data = robot.readSensorPackets(
				SENSOR_PACKET.P22_VOLTAGE, 
				SENSOR_PACKET.P23_CURRENT, 
				SENSOR_PACKET.P24_BATTERY_TEMPERATURE, 
				SENSOR_PACKET.P25_BATTERY_CHARGE, 
				SENSOR_PACKET.P26_BATTERY_CAPACITY);
		System.out.println(Arrays.toString(data));

	}

}
