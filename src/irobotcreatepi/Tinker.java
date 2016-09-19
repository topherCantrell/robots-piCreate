package irobotcreatepi;

import java.util.Arrays;

import irobotcreatepi.IRobotCreate.SENSOR_PACKET;

public class Tinker {

	public static void main(String[] args) throws Exception {
		
		//String devname = "/dev/ttyUSB0"; // Pi
		String devname = "COM4"; // PC
		
		System.out.println("USING PORT '"+devname+"'");
		IRobotCreate robot = new IRobotCreateSerial(devname);
		
		robot.setMode(IRobotCreate.MODE.PASSIVE); // Required at startup
		Thread.sleep(1000); // Wait for mode change	
		
		robot.setMode(IRobotCreate.MODE.FULL);
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
