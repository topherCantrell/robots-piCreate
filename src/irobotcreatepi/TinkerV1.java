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
		
		//robot.setLEDs(false, false, 128, 128);
		
				
		/*
		int [] data = robot.readSensorPackets(
				SENSOR_PACKET.P06_GROUP6);
		System.out.println(Arrays.toString(data));		
		
		while(true) {
			robot.readSensorPackets(SENSOR_PACKET.P06_GROUP6);
			int v = robot.getBumpsAndDrops(); // 7
			System.out.println("Bumps and Drops: "+v);
			System.out.println("  Caster:     "+robot.isWheeldropCaster());
			System.out.println("  Left:       "+robot.isWheeldropLeft());
			System.out.println("  Right:      "+robot.isWheeldropRight());
			System.out.println("  Bump Left:  "+robot.isBumpLeft());
			System.out.println("  Bump Right: "+robot.isBumpRight());
			//
			System.out.println("Wall seen:            "+robot.isWall()); // 8
			System.out.println("Cliff to the left:    "+robot.isCliffLeft()); // 9
			System.out.println("Cliff to front/left:  "+robot.isCliffFrontLeft()); // 10
			System.out.println("Cliff to front/right: "+robot.isCliffFrontRight()); // 11
			System.out.println("Cliff to right:       "+robot.isCliffRight()); //12
			System.out.println("Virtual wall seen:    "+robot.isVirtualWall()); // 13	 		
			//
			System.out.println("Low side drivers and wheel overcurrents: "+robot.getOvercurrents());
			System.out.println("  Left wheel overcurrent: "+robot.isLeftWheelOvercurrent());
			System.out.println("  Right wheel overcurrent: "+robot.isRightWheelOvercurrent());
			System.out.println("  LD2 overcurrent: "+robot.isLD2Overcurrent());
			System.out.println("  LD1 overcurrent: "+robot.isLD1Overcurrent());
			System.out.println("  LD0 overcurrent: "+robot.isLD0Overcurrent());
			//
			System.out.println("Infrared byte: "+robot.getInfraredByte());
			//
			System.out.println("Buttons: "+robot.getButtons());
			System.out.println("  Advance button pressed: "+robot.isButtonAdv());
			System.out.println("  Play button pressed: "+robot.isButtonPlay());
			//
			System.out.println("Distance travelled: "+robot.getDistanceTraveled());
			System.out.println("Angle turned: "+robot.getAngleTurned());
			//
			System.out.println("Charging state: "+robot.getChargingState());
			System.out.println("Battery voltage: "+robot.getVoltage());			
			System.out.println("Battery current: "+robot.getCurrent());
			System.out.println("Battery temperature: "+robot.getBatteryTemperature());
			System.out.println("Battery charge: "+robot.getBatteryCharge());
			System.out.println("Estimated battery capacity: "+robot.getBatteryCapacity());
			//
			System.out.println("Wall sensor value: "+robot.getWallSignal());
			System.out.println("Cliff Left sensor value: "+robot.getCliffLeftSignal());
			System.out.println("Cliff Front Left sensor value: "+robot.getCliffFrontLeftSignal());
			System.out.println("Cliff Front Right sensor value: "+robot.getCliffFrontRightSignal());
			System.out.println("Cliff Right sensor value: "+robot.getCliffRightSignal());
			//
			System.out.println("Cargo bay digital inputs: "+robot.getCargoBayDigitalInputs());
			System.out.println("  Cargo bay digital baud rage change: "+robot.isCargoBayBaudChange());
			System.out.println("  Cargo bay digital input 3 (pin 6): "+robot.isCargoBayIn3());
			System.out.println("  Cargo bay digital input 2 (pin 18): "+robot.isCargoBayIn2());
			System.out.println("  Cargo bay digital input 1 (pin 5): "+robot.isCargoBayIn1());
			System.out.println("  Cargo bay digital input 0 (pin 17): "+robot.isCargoBayIn0());
			//
			System.out.println("Cargo bay analog input (pin 25): "+robot.getCargoBayAnalogSignal());
			//
			System.out.println("Available charging sources: "+robot.getChargingSourcesAvailable());
			System.out.println("  Home base charger available: "+robot.isHomeBaseChargerAvailable());
			System.out.println("  Internal charger available: "+ robot.isInternalChargerAvailable());
			//
			System.out.println("Open Interface mode: "+robot.getOIMode());
			//
			System.out.println("Is song playing: "+robot.isSongPlaying());
			System.out.println("Playing song number: "+robot.getPlayingSongNumber());
			//
			System.out.println("Number of packets in the packet stream: "+robot.getNumberStreamPackets());
			//
			System.out.println("Last requested 'drive' velocity: "+robot.getRequestedVelocity());
			System.out.println("Last requested 'drive' radius: "+robot.getRequestedRadius());
			System.out.println("Last requested 'direct-drive' left wheel velocity: "+robot.getRequestedLeftVeloicty());
			System.out.println("Last requested 'direct-drive' right wheel velocity: "+robot.getRequestedRightVelocity());
						
			Thread.sleep(1000);
		}
		*/
		
		//robot.startStreamSensorPackets(SENSOR_PACKET.P02_GROUP2);
		
	
		for(int x=0;x<8;++x) {
			int [] dat = robot.readStreamSensorPackets(SENSOR_PACKET.P02_GROUP2);
			System.out.println(Arrays.toString(dat));
		}
		
		Thread.sleep(3000);
		
		for(int x=0;x<8;++x) {
			int [] dat = robot.readStreamSensorPackets(SENSOR_PACKET.P02_GROUP2);
			System.out.println(Arrays.toString(dat));
		}
	
		
		//robot.storeSong(0, 72,8, 76,8, 79,8);
		//Thread.sleep(1000);
		//robot.playSong(0);

	}

}
