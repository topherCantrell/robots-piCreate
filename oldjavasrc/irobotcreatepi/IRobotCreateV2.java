package irobotcreatepi;

import java.io.InputStream;
import java.io.OutputStream;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
	public static  int[] twoByteSigned(int value) {
		int [] ret = new int[2];
		if(value<0) value=value+0x10000;
		value = value & 0xFFFF;
		ret[0] = value >> 8;
		ret[1] = value & 0xFF;
		return ret;
	}
	/*
	private int fromTwoByteSigned(int high, int low) {
		int ret = (high<<8) | low;
		if(ret>0x7FFF) {
			ret = ret | 0xFFFF0000;
		}
		return ret;
	}*/
	
	/**
	 *  The Create OI modes.
	 */
	public enum MODE {OFF, PASSIVE, SAFE, FULL}
	
	/**
	 * Available serial baud rates (default is 115200).
	 */
	public enum BAUD {B300, B600, B1200, B2400, B4800, B9600, B14400, B19200, B28800, B38400, B57600, B115200}
	
	// We could do some "first seven" magic here, but version 2 of the interface has some
	// group ids above 100. The using-code is more shareable with something like this.
	private static Map<SENSOR_PACKET,int[]> SENSOR_GROUPS = new HashMap<SENSOR_PACKET,int[]>();
	static {
		SENSOR_GROUPS.put(SENSOR_PACKET.P00_GROUP0, new int[]{7,26});
		SENSOR_GROUPS.put(SENSOR_PACKET.P01_GROUP1, new int[]{7,16});
		SENSOR_GROUPS.put(SENSOR_PACKET.P02_GROUP2, new int[]{17,20});
		SENSOR_GROUPS.put(SENSOR_PACKET.P03_GROUP3, new int[]{21,26});
		SENSOR_GROUPS.put(SENSOR_PACKET.P04_GROUP4, new int[]{27,34});
		SENSOR_GROUPS.put(SENSOR_PACKET.P05_GROUP5, new int[]{35,42});
		SENSOR_GROUPS.put(SENSOR_PACKET.P06_GROUP6, new int[]{7,42});
		//
		SENSOR_GROUPS.put(SENSOR_PACKET.P100_GROUP100, new int[]{7,58});
		SENSOR_GROUPS.put(SENSOR_PACKET.P101_GROUP101, new int[]{43,58});
		SENSOR_GROUPS.put(SENSOR_PACKET.P106_GROUP106, new int[]{46,51});
		SENSOR_GROUPS.put(SENSOR_PACKET.P107_GROUP107, new int[]{54,58});
	}	
	
	// Name and size (in bytes) of each sensor packet. The first 7 are groups of sensors.
	/**
	 * The Create sensor packets (the first 7 are groups of other sensor packets).
	 */
	public enum SENSOR_PACKET {
		P00_GROUP0(26),  //  26 bytes  7-26
		P01_GROUP1(10),  //  10 bytes  7-16
		P02_GROUP2(6),   //   6 bytes 17-20    
		P03_GROUP3(10),  //  10 bytes 21-26
		P04_GROUP4(14),  //  14 bytes 27-34
		P05_GROUP5(12),  //  12 bytes 35-42
		P06_GROUP6(52),  //  52 bytes  7-42
		//
		P07_BUMPS_AND_WHEEL_DROPS(1),  //4:Wheeldrop Caster, 3:Wheeldrop left, 2:Wheeldrop right, 1:Bump left, 0:Bump Right
		P08_WALL(1), // 0:Wall seen
		P09_CLIFF_LEFT(1), // 0:Cliff seen
		P10_CLIFF_FRONT_LEFT(1), // 0:Cliff seen
		P11_CLIFF_FRONT_RIGHT(1),// 0:Cliff seen	
		P12_CLIFF_RIGHT(1), // 0:Cliff seen
		P13_VIRTUAL_WALL(1), // 0:Virtual wall seen
		P14__OVERCURRENTS(1), // 4:Left wheel, 3: Right wheel, 2:Main brush, 1:Reserved, 0:Side brush		
		P15_DIRT_DETECT(1), // Dirt sensor level 0-255
		P16_UNUSED(1),	
		P17_INFRARED_OMNI_BYTE(1), // 255=none,	
		P18_BUTTONS(1), // 7:clock, 6:schedule, 5:day, 4:hour, 3:minute, 2:dock, 1:spot, 0:clean
		P19_DISTANCE(2), // Distance traveled in mm since last request. 2 byte signed (big endian)		
		P20_ANGLE(2), // Angle turned since last request. 2 byte signed (big endian)
		P21_CHARGING_STATE(1), // 0=Not charging, 1=Reconditioning charging, 2=Full charging, 3=Trickle charging, 4=Waiting, 5=Charging fault condition
		P22_VOLTAGE(2), // Battery voltage in mV. 2 byte signed (big endian)
		P23_CURRENT(2), // Current flowing in mA. 2 byte signed (big endian). Negative is normal use. Postive is charging.
		P24_BATTERY_TEMPERATURE(1), // Temperature in degrees celsius (-128 to 127)
		P25_BATTERY_CHARGE(2), // Current charge in mAh. 2 byte signed (big endian)
		P26_BATTERY_CAPACITY(2), // Estimated charge capacity in mAh. 2 byte signed (big endian)
		P27_WALL_SIGNAL(2), // Strength of sensor's signal (16 bit value, big endian, 0-4095)
		P28_CLIFF_LEFT_SIGNAL(2), // Strength of sensor's signal (16 bit value, big endian, 0-4095)
		P29_CLIFF_FRONT_LEFT_SIGNAL(2), // Strength of sensor's signal (16 bit value, big endian, 0-4095)
		P30_CLIFF_FRONT_RIGHT_SIGNAL(2), // Strength of sensor's signal (16 bit value, big endian, 0-4095)
		P31_CLIFF_RIGHT_SIGNAL(2), // Strength of sensor's signal (16 bit value, big endian, 0-4095)
		P32_UNUSED(1), // Cargo bay digital inputs (no longer available)
		P33_UNUSED(2), // Cargo bay analog input (no longer available)
		P34_CHARGING_SOURCES_AVAILABLE(1), // 1:Home base, 0:Internal charger
		P35_OI_MODE(1),  // 0:Off, 1:Passive, 2:Safe, 3:Full
		P36_SONG_NUMBER(1), // 0-15
		P37_SONG_PLAYING(1), // 1=yes, 0=no
		P38_NUMBER_OF_STREAM_PACKETS(1), // Number of data stream packets is returned (0-43)
		P39_REQUESTED_VELOCITY(2), // Most recent requested drive velocity 2 byte signed big endian
		P40_REQUESTED_RADIUS(2), // Most recent requested drive radius 2 byte signed big endian
		P41_REQUESTED_RIGHT_VELOCITY(2), // Most recent requested drive direct right 2 byte signed big endian
		P42_REQUESTED_LEFT_VELOCITY(2), // Most recent requested drive direct left 2 byte signed big endian			
		P43_LEFT_ENCODER_COUNTS(2),
		P44_RIGHT_ENCODER_COUNTS(2),
		P45_LIGHT_BUMPER(1), // 5:right, 4:front-right, 3:right-center, 2:left-center, 1:front-left, 0:left
		P46_LIGHT_BUMP_LEFT_SIGNAL(2), // (16 bit value, big endian, 0-4095)
		P47_LIGHT_BUMP_LEFT_FRONT_SIGNAL(2), // (16 bit value, big endian, 0-4095)
		P48_LIGHT_BUMP_LEFT_CENTER_SIGNAL(2), // (16 bit value, big endian, 0-4095)
		P49_LIGHT_BUMP_RIGHT_CENTER_SIGNAL(2), // (16 bit value, big endian, 0-4095)
		P50_LIGHT_BUMP_RIGHT_FRONT_SIGNAL(2), // (16 bit value, big endian, 0-4095)
		P51_LIGHT_BUMP_RIGHT_SIGNAL(2), // (16 bit value, big endian, 0-4095)		
		P52_INFRARED_LEFT_BYTE(1), 
		P53_INFRARED_RIGHT_BYTE(1),
		P54_LEFT_MOTOR_CURRENT(2), // 2 byte signed (big endian)
		P55_RIGHT_MOTOR_CURRENT(2), // 2 byte signed (big endian)
		P56_MAIN_BRUSH_MOTOR_CURRENT(2), // 2 byte signed (big endian)
		P57_SIDE_BRUSH_MOTOR_CURRENT(2), // 2 byte signed (big endian)
		P58_STASIS_CASTER(1), //1:stasis disabled, 0:stasis toggle
		
		P100_GROUP100(100,80),
		P101_GROUP101(101,28),
		P106_GROUP106(106,12),
		P107_GROUP107(107,9);
		
		int id = -1;
		int numBytes;		
		
		SENSOR_PACKET(int id, int numBytes) {
			this.id = id;			
			this.numBytes = numBytes;
		}
		
		SENSOR_PACKET(int numBytes) {
			this.numBytes = numBytes;
		}
		
		public int getNumBytes() {
			return numBytes;
		}
		
		public int getID() {
			if(id<0) return ordinal();
			return id;
		}
		
	}
			
	/**
	 * Create an object to talk to the iRobot OI version one.
	 * @param is the input stream from the robot
	 * @param os the output stream to the robot
	 */
	public IRobotCreateV2(InputStream is, OutputStream os) {
		//this.is = is;
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
	
	/**
	 * Clear the cleaning schedule.
	 */
	public void clearSchedule() {	
		sendByte(167);
		for(int x=0;x<15;++x) {
			sendByte(0);
		}
	}
	
	private int getRoombaDayNumber(DayOfWeek day) {
		// There is a "getValue" for the enum, but the returned value depends
		// on the type of calendar on the system. The Roomba expects Sunday
		// to be first, so we manually switch.
		switch(day) {
		case SUNDAY:
			return 0;
		case MONDAY:
			return 1;
		case TUESDAY:
			return 2;
		case WEDNESDAY:
			return 3;
		case THURSDAY:
			return 4;			
		case FRIDAY:
			return 5;
		case SATURDAY:
			return 6;
		default:
			throw new RuntimeException("Unknown day of the week "+day);		
		}
	}
	
	/**
	 * This command sends Roomba a new schedule.
	 * @param schedule map of days-to-clean-time (only hour and minute of time are used)
	 */
	public void setSchedule(Map<DayOfWeek, LocalTime> schedule) {		
		int [] data = new int[14];
		int bits = 0;
		for(Entry<DayOfWeek, LocalTime> entry : schedule.entrySet()) {
			int p = getRoombaDayNumber(entry.getKey());
			bits = bits | (1<<p);
			data[p*2] = entry.getValue().getHour();
			data[p*2+1] = entry.getValue().getMinute();			
		}
		
		sendByte(167);
		sendByte(bits);
		for(int i : data) {
			sendByte(i);
		}
	}
	
	/**
	 * Set the roomba's clock.
	 * @param day the day of the week
	 * @param time the time of day (only hour and minute are used
	 */
	public void setDayTime(DayOfWeek day, LocalTime time) {
		sendByte(168);
		sendByte(getRoombaDayNumber(day));
		sendByte(time.getHour());
		sendByte(time.getMinute());
	}
	
	/**
	 * Drive in a straight line.
	 * @param velocity robot velocity -500mm/s to 500mm/s
	 * @see #drive(int, int)
	 */
	public void driveStraight(int velocity) {	
		drive(velocity, 32768);
	}
	
	/**
	 * Spin in place.
	 * @param velocity robot velocity -500mm/s to 500mm/s
	 * @param clockWise true for clockwise or false for counter clockwise
	 * @see #drive(int, int)
	 */
	public void driveSpin(int velocity, boolean clockWise) {	
		if(clockWise) {
			drive(velocity,-1);
		} else {
			drive(velocity,1);
		}
	}
	
	/**
	 * This command controls Create's drive wheels. The first parameter
	 * specifies the average velocity of the drive wheels in millimeters 
	 * per second (mm/s). The next parameter specifies the radius in 
	 * millimeters at which Create will turn. The longer radii make Create 
	 * drive straighter, while the shorter radii make Create turn more. The 
	 * radius is measured from the center of the turning circle to the 
	 * center of Create. A Drive command with a positive velocity and a
	 * positive radius makes Create drive forward while turning
	 * toward the left. A negative radius makes Create turn toward
	 * the right. Special cases for the radius make Create turn
	 * in place or drive straight, as specified below. A negative
	 * velocity makes Create drive backward.
	 * <p>
	 * NOTE: Internal and environmental restrictions may prevent
	 * Create from accurately carrying out some drive commands.
	 * For example, it may not be possible for Create to drive at
	 * full speed in an arc with a large radius of curvature.
	 * <p>
	 * Special cases:<ul>
	 * <li>Straight = 32768 or 32767 = hex 8000 or 7FFF</li>
	 * <li>Turn in place clockwise = hex FFFF</li>
	 * <li>Turn in place counter-clockwise = hex 0001</li></ul>
	 * @param velocity robot velocity -500mm/s to 500mm/s
	 * @param radius robot turning radius -2000mm to 2000mm
	 */
	public void drive(int velocity, int radius) {	
		int [] v = twoByteSigned(velocity);
		int [] r = twoByteSigned(radius);
		//System.out.println(Arrays.toString(v)+":"+Arrays.toString(r));
		sendByte(137);
		sendByte(v[0]);
		sendByte(v[1]);
		sendByte(r[0]);
		sendByte(r[1]);		
	}
	
	/**
	 * This command lets you control the forward and backward
	 * motion of Create’s drive wheels independently. The first 
	 * parameter specifies the velocity of the right wheel in 
	 * millimeters per second (mm/s), with the high byte sent first. 
	 * The next parameter specifies the velocity of the left wheel, 
	 * in the same format. A positive velocity makes that wheel drive 
	 * forward, while a negative velocity makes it drive backward. 
	 * @velocityRight right wheel velocity -500 to 500 mm/s
	 * @velocityLeft left wheel velocity -500 to 500 mm/s
	 */
	public void driveDirect(int velocityRight, int velocityLeft) {
		int [] r = twoByteSigned(velocityRight);
		int [] l = twoByteSigned(velocityLeft);
		sendByte(145);
		sendByte(r[0]);
		sendByte(r[1]);
		sendByte(l[0]);
		sendByte(l[1]);	
	}
	
	/**
	 * This command lets you control the raw forward and backward motion of Roomba’s drive wheels
	 * independently. It takes two parameters, which are interpreted as two 16-bit signed values. The first 
	 * parameter specifies the PWM of the right wheel. The next parameter specifies the PWM of the left 
	 * wheel A positive PWM makes that wheel drive forward, while a negative PWM makes it drive backward. 
	 * @param rightPWM (-255 to 255)
	 * @param leftPWM (-255 to 255)
	 */
	public void drivePWM(int rightPWM, int leftPWM) {
		int [] r = twoByteSigned(rightPWM);
		int [] l = twoByteSigned(leftPWM);
		sendByte(146);
		sendByte(r[0]);
		sendByte(r[1]);
		sendByte(l[0]);
		sendByte(l[1]);	
	}
	
	/**
	 * This command lets you control the forward and backward motion of Roomba’s main brush, side brush,
	 * and vacuum independently. Motor velocity cannot be controlled with this command, all motors will run at
	 * maximum speed when enabled. The main brush and side brush can be run in either direction. The
	 * vacuum only runs forward.
	 * @param mainBrushOn true if main brush is on, false if main brush is off
	 * @param mainBrushInward true if main brush spins inwards, false if main brush spins outwards
	 * @param sideBrushOn true if side brush is on, false if side brush is off
	 * @param sideBrushCCW true if side brush spins counter-clockwise, false for clockwise
	 * @param vacuumOn true if the vacuum is on, false if it is off
	 */
	public void setMotors(boolean mainBrushOn, boolean mainBrushInward, boolean sideBrushOn, boolean sideBrushCCW, boolean vacuumOn) {
		int value = 0;
		if(sideBrushOn) value=value | 1;
		if(vacuumOn) value=value | 2;
		if(mainBrushOn) value=value | 4;
		if(!sideBrushCCW) value=value | 8;
		if(!mainBrushInward) value=value | 16;		
		sendByte(138);
		sendByte(value);
	}
	
	/**
	 * This command lets you control the speed of Roomba’s main brush, side brush, and vacuum
	 * independently. With each data byte, you specify the duty cycle for the low side driver (max 128). For
	 * example, if you want to control a motor with 25% of battery voltage, choose a duty cycle of 128 * 25%
	 * = 32. The main brush and side brush can be run in either direction. The vacuum only runs forward.
	 * Positive speeds turn the motor in its default (cleaning) direction. Default direction for the side brush is
	 * counterclockwise. Default direction for the main brush/flapper is inward.
	 * @param mainBrushPWM (-127 to 127)
	 * @param sideBrushPWM (-127 to 127)
	 * @param vacuumPWM (0 to 127)
	 */
	public void setMotorsPWM(int mainBrushPWM, int sideBrushPWM, int vacuumPWM) {
		int [] m = twoByteSigned(mainBrushPWM);
		int [] s = twoByteSigned(sideBrushPWM);
		int [] v = twoByteSigned(vacuumPWM);
		sendByte(144);
		sendByte(m[1]);
		sendByte(s[1]);
		sendByte(v[1]);
	}
	
	/**
	 * This command controls the LEDs on Create. The state of
	 * the CHECK, DOCK, SPOT, and DEBRIS LEDs is specified by boolean
	 * parameters. The power LED is controlled by two parameters:
	 * one for the color and the other for the intensity.
	 * @param checkRobot true for on (orange)
	 * @param dock true for on (green)
	 * @param spot true for on (green)
	 * @param debris (blue)
	 * @param powerColor color of power LED 0=green to 255=red
	 * @param powerIntensity brightness 0 to 255
	 */
	public void setLEDs(boolean checkRobot, boolean dock, boolean spot, boolean debris, int powerColor, int powerIntensity) {
		int value = 0;
		if(checkRobot) value=value | 8;
		if(dock) value=value | 4;
		if(spot) value=value | 2;
		if(debris) value=value | 1;
		sendByte(139);
		sendByte(value);
		sendByte(powerColor);
		sendByte(powerIntensity);
	}
	
	/**
	 * This command controls the state of the scheduling LEDs present on the Roomba 560 and 570. 
	 * @param days list of day LEDs to turn on (others are off)
	 * @param ledSchedule true for on
	 * @param ledClock true for on
	 * @param ledAM true for on
	 * @param ledPM true for on
	 * @param ledColon true for on
	 */
	public void setSchedulingLEDs(Set<DayOfWeek> days, boolean ledSchedule, boolean ledClock, boolean ledAM, boolean ledPM, boolean ledColon) {
		int vday = 0;
		for(DayOfWeek d : days) {
			vday = vday | (1<<getRoombaDayNumber(d));
		}
		int v = 0;
		if(ledSchedule) v=v|16;
		if(ledClock) v=v|8;
		if(ledAM) v=v|4;
		if(ledPM) v=v|2;
		if(ledColon) v=v|1;
		sendByte(162);
		sendByte(vday);
		sendByte(v);
	}
	
	/**
	 * This command controls the four 7 segment
     * displays on the Roomba 560 and 570. Digits are ordered
     * from left to right. Each bit in the value is an individual
     * segment LED.
     * <p>
     * NOTE: This opcode does not work in current Create 2 and Roomba 500/600 firmware versions.
	 * @param digit3 individual bits are segments
	 * @param digit2 individual bits are segments
	 * @param digit1 individual bits are segments
	 * @param digit0 individual bits are segments
	 */
	public void setRawDigitSegments(int digit3, int digit2, int digit1, int digit0) {
		sendByte(163);
		sendByte(digit3);
		sendByte(digit2);
		sendByte(digit1);
		sendByte(digit0);
	}
	
	/**
	 * This command lets you push Roomba’s buttons. The buttons will automatically release after 1/6th of a
	 * second.
	 * @param clock true to press button
	 * @param schedule true to press button
	 * @param day true to press button
	 * @param hour true to press button
	 * @param minute true to press button
	 * @param dock true to press button
	 * @param spot true to press button
	 * @param clean true to press button
	 */
	public void pushButtons(boolean clock, boolean schedule, boolean day, boolean hour, boolean minute, boolean dock, boolean spot, boolean clean) {
		int value = 0;
		if(clock) value=value|128;
		if(schedule) value=value|64;
		if(day) value=value|32;
		if(hour) value=value|16;
		if(minute) value=value|8;
		if(dock) value=value|4;
		if(spot) value=value|2;
		if(clean) value=value|1;
		sendByte(165);
		sendByte(value);
	}
	/**
	 * This command lets you push Roomba’s buttons. 
	 */
	public void pushClockButton() {
		sendByte(165);
		sendByte(128);
	}
	/**
	 * This command lets you push Roomba’s buttons. 
	 */
	public void pushScheduleButton() {
		sendByte(165);
		sendByte(64);
	}
	/**
	 * This command lets you push Roomba’s buttons. 
	 */
	public void pushDayButton() {
		sendByte(165);
		sendByte(32);
	}
	/**
	 * This command lets you push Roomba’s buttons. 
	 */
	public void pushHourButton() {
		sendByte(165);
		sendByte(16);
	}
	/**
	 * This command lets you push Roomba’s buttons. 
	 */
	public void pushMinuteButton() {
		sendByte(165);
		sendByte(8);
	}
	/**
	 * This command lets you push Roomba’s buttons. 
	 */
	public void pushDockButton() {
		sendByte(165);
		sendByte(4);
	}
	/**
	 * This command lets you push Roomba’s buttons. 
	 */
	public void pushSpotButton() {
		sendByte(165);
		sendByte(2);
	}
	/**
	 * This command lets you push Roomba’s buttons. 
	 */
	public void pushCleanButton() {
		sendByte(165);
		sendByte(1);
	}
	
	/**
	 * This command controls the four 7 segment displays on the Roomba 560 and 570 using ASCII character
	 * codes. Because a 7 segment display is not sufficient to display alphabetic characters properly, all
	 * characters are an approximation, and not all ASCII codes are implemented.
	 * @param a left most digit
	 * @param b next digit
	 * @param c next digit
	 * @param d right most digit
	 */
	public void setDigitDisplay(char a, char b, char c, char d) {
		sendByte(164);
		sendByte(a);
		sendByte(b);
		sendByte(c);
		sendByte(d);
	}
	
	/**
	 * This command lets you specify up to four songs to the OI
	 * that you can play at a later time. Each song is associated
	 * with a song number. The Play command uses the song
	 * number to identify your song selection. Each song can
	 * contain up to sixteen notes. Each note is associated with a
	 * note number that uses MIDI note definitions and a duration
	 * that is specified in fractions of a second. The number of 
	 * parameters varies, depending on the length of the song specified.
	 * A one note song is specified by two parameters. For each
	 * additional note within a song, add two parameters.
	 * @param number song number (0-3)
	 * @param notes pairs of midi/duration for each note. Midi
	 * note number 31-127 and duration in increments of 1/64th seconds. 
	 */
	public void storeSong(int number, int... notes) {
		sendByte(140);
		sendByte(number);
		sendByte(notes.length/2);
		for(int val : notes) {
			sendByte(val);
		}
	}
	
	/**
	 * This command lets you select a song to play from the songs
	 * added to iRobot Create using the Song command. You must
	 * add one or more songs to Create using the Song command
	 * in order for the Play command to work. Also, this command
	 * does not work if a song is already playing. Wait until a
	 * currently playing song is done before sending this command.
	 * Note that the "song playing" sensor packet can be used to
	 * check whether Create is ready to accept this command.
	 * @param number the song number (0-15)
	 */
	public void playSong(int number) {
		sendByte(141);
		sendByte(number);
	}
	
	/**
	 * This command requests the OI to send a packet of sensor
	 * data bytes. There are 43 different sensor data packets. Each
	 * provides a value of a specific sensor or group of sensors. 
	 * @param packetID requested packet 
	 * @return raw sensor bytes
	 */
	public int[] readSensorPacket(SENSOR_PACKET packetID) {
		sendByte(142);
		sendByte(packetID.getID());
		return readSensorData(packetID);
	}
	
	// Read and cache a single regular sensor packet or a group of regular sensor packets
	private int[] readSensorData(SENSOR_PACKET packetID) {		
		int [] ends = SENSOR_GROUPS.get(packetID);
		if(ends!=null) { // This is a group of packets		
			int [] ret = new int[packetID.getNumBytes()];
			int pos = 0;
			for(int i=ends[0]; i<=ends[1];++i) {
				SENSOR_PACKET gp = SENSOR_PACKET.values()[i];
				int [] pdata = readSensorData(gp);
				for(int x=0;x<pdata.length;++x) {
					ret[pos++] = pdata[x];
				}
			}
			return ret;
		} else { // This is a regular packet		
			int [] ret = new int[packetID.getNumBytes()];
			for(int x=0;x<ret.length;++x) {
				ret[x] = readByte();
			}		
			cachedSensorPackets.put(packetID, ret);		
			return ret;
		}
	}
	
	/**
	 * This command lets you ask for a list of sensor packets.
	 * The result is returned once, as in the Sensors command.
	 * The robot returns the packets in the order you specify. 
	 * @param packetIDs list of requested packets
	 * @return raw sensor bytes (in order)
	 */
	public int[] readSensorPackets(SENSOR_PACKET ... packetIDs) {
		sendByte(149);
		sendByte(packetIDs.length);
		int totalSize = 0;
		for(SENSOR_PACKET id : packetIDs) {
			sendByte(id.getID());
			totalSize += id.getNumBytes();
		}
		int [] ret = new int[totalSize];
		int pos = 0;
		for(SENSOR_PACKET id : packetIDs) {
			int [] pdata = readSensorData(id);
			for(int x=0;x<pdata.length;++x) {
				ret[pos++] = pdata[x];
			}			
		}		
		return ret;
	}
	
	/**
	 * This command starts a continuous stream of data packets.
	 * The list of packets requested is sent every 15 ms, which is
	 * the rate iRobot Create uses to update data.
	 * This is the best method of requesting sensor data if you
	 * are controlling Create over a wireless network (which has
	 * poor real-time characteristics) with software running on a
	 * desktop computer. 
	 * @param packetIDs list of requested packets
	 */
	public void startStreamSensorPackets(SENSOR_PACKET ... packetIDs) {	
		sendByte(148);
		sendByte(packetIDs.length);
		for(SENSOR_PACKET id : packetIDs) {
			sendByte(id.getID());
		}
	}
	
	/**
	 * Stops the stream without clearing the list
	 * of requested packets.
	 */
	public void pauseStreamSensorPackets() {
		sendByte(150);
		sendByte(0);
	}
	
	/**
	 * Starts the stream
	 * using the list of packets last requested.
	 */
	public void resumeStreamSensorPackets() {
		sendByte(150);
		sendByte(1);
	}
	
	/**
	 * Read the packets from the sensor stream.
	 * <p>
	 * If you have gotten out of sync with the stream then you can call
	 * this repeatedly to sync back up with the stream's header.
	 * @param packetIDs list of requested packets
	 * @return raw sensor bytes (in order) WITHOUT header or checksum or null if format error
	 */
	public int[] readStreamSensorPackets(SENSOR_PACKET ... packetIDs) {
		
		// Total return data size (just data ... no protocol bytes)
		int totalSize = 0;
		for(SENSOR_PACKET id : packetIDs) {			
			totalSize += id.getNumBytes();
		}
		int [] ret = new int[totalSize];
		
		int chk = 0;
		
		// Header byte must be 19		
		int retry = totalSize; // We will look for 19 to sync up
		int prot;
		while(true) {
			prot = readByte();
			if(prot==19) break;
			--retry;
			if(retry==0) return null;
		}
		chk += prot;chk = chk & 0xFF;
		
		prot = readByte(); // Total number of bytes
		chk += prot;chk = chk & 0xFF;
		
		// Make sure we agree with the robot on what is being sent
		if(prot!=totalSize+packetIDs.length) return null;
		
		// Fill up the return data
		int pos=0;		
		for(SENSOR_PACKET id : packetIDs) {
			prot = readByte();
			chk += prot;chk = chk & 0xFF;			
			if(prot!=id.getID()) return null;			
			int [] pdata = readSensorData(id);			
			for(int x=0;x<pdata.length;++x) {
				chk = chk + pdata[x];chk = chk & 0xFF;
				ret[pos++] = pdata[x];				
			}
			cachedSensorPackets.put(id, pdata);
		}
		
		// Now add in the checksum value at the end of the stream
		chk = chk + readByte();chk = chk & 0xFF;		
		
		if(chk!=0) return null;		
		
		return ret;
	}	
	
	// TODO sensors	

}
