package irobotcreatepi;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import irobotcreatepi.IRobotCreateV1.SENSOR_PACKET;

/**
 * This class encapsulates the Create Open Interface (version 2) serial interface.
 * <p> 
 * For OI version 2 the default communication is 115200, no-parity-bit, 1-stop-bit, no-flow-control.
 * <p>
 * You MUST send the passive mode command to activate the interface. Do that by setting the 
 * mode to PASSIVE first thing. Then wait one second for the mode to change. 
 * THEN you can talk to the Create using the OI protocol.
 * <p>
 * There are several sensor-packets you can request from the Create. There are 6 groups of
 * sensor packets you can request. These are commonly-used groupings. There are three ways to
 * get sensor packet information:
 * <p><ul>
 * <li> request a single packet (or group) and get back all the raw bytes
 * <li> request a list of packets (or groups) and get back all the raw bytes
 * <li> schedule a list of packets (or groups) for Create to send back every 15ms. Then
 *      call a method to read the raw bytes when you want them.
 * </ul>
 * <p>
 * In all three cases the methods return the raw bytes. In all three cases the raw bytes 
 * are also cached and you can call the desired "get" packet to decode the raw bytes.
 * <p>
 * For instance: <br>
 *     robot.readSensorPackets(SENSOR_PACKET.P19_DISTANCE, SENSOR_PACKET.P20_ANGLE);
 *     int dist = robot.getDistanceTraveled(); // signed in mm
 *     int ang = robot.getAngleTurned(); // signed in degrees
 */
public class IRobotCreateV2 {
	
	private InputStream is;  // Serial input
	private OutputStream os; // Serial output
	
	private Map<SENSOR_PACKET,int[]> cachedSensorPackets = new HashMap<SENSOR_PACKET,int[]>();
		
	// Send a serial byte and promote IOExceptions to runtime (unchecked) exceptions
	private void sendByte(int value) {
		try {
			os.write(value);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}	
	
	// Wait for a byte and promote IOExceptions to runtime (unchecked) exceptions
	private int readByte() {
		try {
			while(true) {
				int ret = is.read();
				if(ret>=0) {
					//System.out.println(":"+ret);
					return ret;
				}
				try{Thread.sleep(100);} catch (Exception e) {}
			}			
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	// Used in multi-byte math
	private int[] twoByteSigned(int value) {
		int [] ret = new int[2];
		if(value<0) value=value+0x10000;
		value = value & 0xFFFF;
		ret[0] = value >> 8;
		ret[1] = value & 0xFF;
		return ret;
	}	
	private int fromTwoByteSigned(int high, int low) {
		int ret = (high<<8) | low;
		if(ret>0x7FFF) {
			ret = ret | 0xFFFF0000;
		}
		return ret;
	}
	
	/**
	 *  The Create OI modes.
	 */
	public enum MODE {OFF, PASSIVE, SAFE, FULL}
	
	/**
	 * Available serial baud rates (default is 115200).
	 */
	public enum BAUD {B300, B600, B1200, B2400, B4800, B9600, B14400, B19200, B28800, B38400, B57600, B115200}
			
	/**
	 * Create an object to talk to the iRobot OI version one.
	 * @param is the input stream from the robot
	 * @param os the output stream to the robot
	 */
	public IRobotCreateV2(InputStream is, OutputStream os) {
		this.is = is;
		this.os = os;
	}		
	
	/**
     * The Roomba OI has four operating modes: Off, Passive, Safe, and Full. After a battery change or when
     * power is first turned on, the OI is in "off" mode. When it is off, the OI listens at the default baud rate
     * (115200 or 19200 - see Serial Port Settings above) for an OI Start command. Once it receives the Start
     * command, you can enter into any one of the four operating modes by sending a mode command to the
     * OI. You can also switch between operating modes at any time by sending a command to the OI for the
     * operating mode that you want to use.
     * <p>
     * <b>Passive Mode</b><br>
     * Upon sending the Start command or any one of the cleaning mode commands (e.g., Spot, Clean, Seek
     * Dock), the OI enters into Passive mode. When the OI is in Passive mode, you can request and receive
     * sensor data using any of the sensor commands, but you cannot change the current command parameters
     * for the actuators (motors, speaker, lights, low side drivers, digital outputs) to something else. To change
     * how one of the actuators operates, you must switch from Passive mode to Full mode or Safe mode.
     * <p>
     * While in Passive mode, you can read Roomba’s sensors, watch Roomba perform a cleaning cycle, and
     * charge the battery.
     * <p>
     * In Passive mode, Roomba will go into power saving mode to conserve battery power after five minutes of
     * inactivity. To disable sleep, pulse the BRC pin low periodically before these five minutes expire. Each
     * pulse resets this five minute counter. (One example that would not cause the baud rate to inadvertently
     * change is to pulse the pin low for one second, every minute, but there are other periods and duty cycles
     * that would work, as well.)
     * <p>
     * <b>Safe Mode</b><br>
     * When you send a Safe command to the OI, Roomba enters into Safe mode. Safe mode gives you full
     * control of Roomba, with the exception of the following safety-related conditions:<ul>
     * <li>Detection of a cliff while moving forward (or moving backward with a small turning radius, less than
     * one robot radius).</li>
     * <li>Detection of a wheel drop (on any wheel).</li>
     * <li>Charger plugged in and powered.</li></ul>
     * Should one of the above safety-related conditions occur while the OI is in Safe mode, Roomba stops all
     * motors and reverts to the Passive mode.
     * <p>
     * If no commands are sent to the OI when in Safe mode, Roomba waits with all motors and LEDs off and
     * does not respond to button presses or other sensor input.
     * <p>
     * Note that charging terminates when you enter Safe Mode, and Roomba will not power save.
     * <p>
     * <b>Full Mode</b><br>
     * When you send a Full command to the OI, Roomba enters into Full mode. Full mode gives you complete
     * control over Roomba, all of its actuators, and all of the safety-related conditions that are restricted when
     * the OI is in Safe mode, as Full mode shuts off the cliff, wheel-drop and internal charger safety features.
     * To put the OI back into Safe mode, you must send the Safe command.
     * <p>
     * If no commands are sent to the OI when in Full mode, Roomba waits with all motors and LEDs off and
     * does not respond to button presses or other sensor input.
     * <p>
     * Note that charging terminates when you enter Full Mode, and Roomba will not power save.
	 * @param mode the desired mode
	 */
	public void setMode(MODE mode) {
		// You MUST send PASSIVE at power on
		// Beeps once when starting from OFF mode
		// Be sure to pause 1sec after starting from OFF mode
		switch(mode) {
		case FULL:
			sendByte(132);
			break;
		case PASSIVE:
			sendByte(128);
			break;
		case SAFE:
			sendByte(131);
			break;
		case OFF:
			sendByte(173);
			break;
		default:
			throw new RuntimeException("Unhandled mode '"+mode+"'");		
		}		
	}
	
	/**
	 * This command resets the robot, as if you had removed and reinserted the battery.
	 */
	public void reset() {
		sendByte(7);
	}
	
	/**
	 * This command sets the baud rate in bits per second (bps) at which OI commands and data are sent
	 * according to the baud code sent in the data byte. The default baud rate at power up is 115200 bps, but
	 * the starting baud rate can be changed to 19200 by following the method outlined on page 4. Once the
	 * baud rate is changed, it persists until Roomba is power cycled by pressing the power button or removing
	 * the battery, or when the battery voltage falls below the minimum required for processor operation. You
	 * must wait 100ms after sending this command before sending additional commands at the new baud rate.
	 * @param rate the desired baud rate
	 */
	public void setBaud(BAUD rate) {	
		sendByte(129);
		sendByte(rate.ordinal());		
	}
	
	/**
	 * This command starts the default cleaning mode. This is the same as pressing Roomba’s Clean button,
     * and will pause a cleaning cycle if one is already in progress.
     * <p>
     * NOTE: changes mode to PASSIVE
	 */
	public void cleanDefault() {
		sendByte(135);
	}
	
	/**
	 * This command starts the Max cleaning mode, which will clean until the battery is dead. This command
     * will pause a cleaning cycle if one is already in progress.
     * <p>
     * NOTE: changes mode to PASSIVE
	 */
	public void cleanMax() {
		sendByte(136);
	}
	
	/**
	 * This command starts the Spot cleaning mode. This is the same as pressing Roomba’s Spot button, and
     * will pause a cleaning cycle if one is already in progress.
     * <p>
     * NOTE: changes mode to PASSIVE
	 */
	public void cleanSpot() {
		sendByte(136);
	}
	
	/**
	 * This command directs Roomba to drive onto the dock the next time it encounters the docking beams.
	 * This is the same as pressing Roomba’s Dock button, and will pause a cleaning cycle if one is already in
	 * progress.
     * <p>
     * NOTE: changes mode to PASSIVE
	 */
	public void seekDock() {
		sendByte(143);
	}
	
	/**
	 * This command powers down Roomba. The OI can be in Passive, Safe, or Full mode to accept this
     * command.
     * <p>
     * NOTE: changes mode to PASSIVE
	 */
	public void powerDown() {
		sendByte(133);
	}
	
	public void clearSchedule() {		
	}
	
	public void setSchedule() {		
	}
	
	

}
