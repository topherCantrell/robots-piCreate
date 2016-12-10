package irobotcreatepi;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class encapsulates the Create Open Interface (version 1) serial interface.
 * <p> 
 * For OI version 1 the default communication is 57600, no-parity-bit, 1-stop-bit, no-flow-control.
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
public class IRobotCreateV1 {
		
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
	public static int[] twoByteSigned(int value) {
		int [] ret = new int[2];
		if(value<0) value=value+0x10000;
		value = value & 0xFFFF;
		ret[0] = value >> 8;
		ret[1] = value & 0xFF;
		return ret;
	}	
	public static  int fromTwoByteSigned(int high, int low) {
		int ret = (high<<8) | low;
		if(ret>0x7FFF) {
			ret = ret | 0xFFFF0000;
		}
		return ret;
	}
	
	/**
	 * Create an object to talk to the iRobot OI version one.
	 * @param is the input stream from the robot
	 * @param os the output stream to the robot
	 */
	public IRobotCreateV1(InputStream is, OutputStream os) {
		this.is = is;
		this.os = os;
	}	
		
	/**
	 *  The Create OI modes.
	 */
	public enum MODE {OFF, PASSIVE, SAFE, FULL}
	
	/**
	 * Available serial baud rates (default is 57600).
	 */
	public enum BAUD {B300, B600, B1200, B2400, B4800, B9600, B14400, B19200, B28800, B38400, B57600, B115200}
	
	/**
	 * The Create demo actions.
	 */
	public enum DEMO {COVER, COVER_DOCK, SPOT_DOCK, MOUSE, DRIVE_FIGURE_EIGHT, WIMP, HOME, TAG, RACHELBEL, BANJO}
	
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
		P14_LOW_SIDE_DRIVER_AND_WHEEL_OVERCURRENTS(1), // 4:Left wheel, 3: Right wheel, 2:LD-2, 1:LD-0, 0:LD-1		
		P15_UNUSED(1),
		P16_UNUSED(1),		
		// Remote: 129=Left, 130=Forward, 131=Right, 132=Spot, 133=Max, 134=Small, 135=Medium, 136=Large/Clean, 137=PAUSE, 138=Power, 139=arc-forward-left, 140=arc-forward-right, 141=drive-stop
		// Scheduling Remote: 142=Send All, 143=Seek Dock
		// Home Base: 240=Reserved, 248=Red Buoy, 244=Green Buoy, 242=Force Field, 252=Red Buoy and Green Buoy, 250=Red Buoy and Force Field, 246=Green Buoy and Force Field, 254=Red Buoy, Green Buoy, and Force Field
		P17_INFRARED_BYTE(1), // 255=none,		
		P18_BUTTONS(1), // 2:Advance, 0:Play		
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
		P32_CARGO_BAY_DIGITIAL_INPUTS(1),  // 4:Baud rate change pin15, 3:In3 pin6, 2:In2 pin18, 1:In1 pin5, 0:In0 pin17
		P33_CARGO_BAY_ANALOG_SIGNAL(2), // pin4 10 bit value big endian
		P34_CHARGING_SOURCES_AVAILABLE(1), // 1:Home base, 0:Internal charger
		P35_OI_MODE(1),  // 0:Off, 1:Passive, 2:Safe, 3:Full
		P36_SONG_NUMBER(1), // 0-15
		P37_SONG_PLAYING(1), // 1=yes, 0=no
		P38_NUMBER_OF_STREAM_PACKETS(1), // Number of data stream packets is returned (0-43)
		P39_REQUESTED_VELOCITY(2), // Most recent requested drive velocity 2 byte signed big endian
		P40_REQUESTED_RADIUS(2), // Most recent requested drive radius 2 byte signed big endian
		P41_REQUESTED_RIGHT_VELOCITY(2), // Most recent requested drive direct right 2 byte signed big endian
		P42_REQUESTED_LEFT_VELOCITY(2); // Most recent requested drive direct left 2 byte signed big endian
		
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
	
	// The events you can "wait" on
	/**
     * The events you can have the Create wait on.
	 */
	public enum EVENT {
		WHEEL_DROP,
		FRONT_WHEEL_DROP,
		LEFT_WHEEL_DROP,
		RIGHT_WHEEL_DROP,
		BUMP,
		LEFT_BUMP,
		RIGHT_BUMP,
		VIRTUAL_WALL,
		WALL,
		CLIFF,
		LEFT_CLIFF,
		FRONT_LEFT_CLIFF,
		FRONT_RIGHT_CLIFF,
		RIGHT_CLIFF,
		HOME_BASE,
		ADVANCE_BUTTON,
		PLAY_BUTTON,
		DIGITAL_INPUT_0,
		DIGITAL_INPUT_1,
		DIGITAL_INPUT_2,
		DIGITAL_INPUT_3,
		PASSIVE_MODE
	}
			
	/**
	 * This command sets the baud rate in bits per second (bps)
	 * at which OI commands and data are sent according to the
	 * baud code sent in the data byte. The default baud rate at
	 * power up is 57600 bps, but the starting baud rate can
	 * be changed to 19200 by holding down the Play button
	 * while powering on Create until you hear a sequence
	 * of descending tones. Once the baud rate is changed, it
	 * persists until Create is power cycled by pressing the power
	 * button or removing the battery, or when the battery voltage
	 * falls below the minimum required for processor operation.
	 * You must wait 100ms after sending this command before
	 * sending additional commands at the new baud rate.
	 * <p>
	 * <b>Note: at a baud rate of 115200, there must be at least
	 * 200us between the onset of each character, or some
	 * characters may not be received.</b>
	 * @param rate the desired baud rate
	 */
	public void setBaud(BAUD rate) {	
		sendByte(129);
		sendByte(rate.ordinal());		
	}
			
	/**
	 * Create has four operating modes: Off, Passive, Safe, and
	 * Full. Create powers on in the Passive mode. The following
	 * commands change Create’s OI mode. 
	 * <p>
	 * PASSIVE: You must always send this Start command before
	 * sending any other commands to the robot. Create beeps once to
     * acknowledge it is starting from "off" mode.
	 * <p>
	 * SAFE: This command puts the OI into Safe mode, enabling user
	 * control of Create. It turns off all LEDs. The OI can be in
	 * Passive, Safe, or Full mode to accept this command.
	 * <p>
	 * FULL: This command gives you complete control over Create
	 * by putting the OI into Full mode, and turning off the cliff,
	 * wheel-drop and internal charger safety features. That is, in
	 * Full mode, Create executes any command that you send
	 * it, even if the internal charger is plugged in, or the robot
	 * senses a cliff or wheel drop.
	 * <p>
	 * You can not put the OI into OFF mode with a serial command
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
			throw new RuntimeException("OFF mode not supported by Create version 1.");
		default:
			throw new RuntimeException("Unhandled mode '"+mode+"'");		
		}		
	}
	
	/**
	 * Stops the demo that Create is currently
     * performing.
	 */
	public void abortDemo() {
		sendByte(136);
		sendByte(255);
	}
	
	/**
	 * The following are commands to start iRobot Create's
     * built-in demos.<ul>
     * 
     * <li>Cover: Create attempts to cover an entire
     * room using a combination of behaviors,
     * such as random bounce, wall following,
     * and spiraling.</li>
     * 
     * <li>Cover and Dock: Identical to the Cover demo, with one
     * exception. If Create sees an infrared
     * signal from an iRobot Home Base, it
     * uses that signal to dock with the Home
     * Base and recharge itself.</li>
     * 
     * <li>Spot Cover: Create covers an area around its
     * starting position by spiraling outward,
     * then inward.</li>
     * 
     * <li>Mouse: Create drives in search of a wall. Once
     * a wall is found, Create drives along the
     * wall, traveling around circumference of
     * the room.</li>
     * 
     * <li>Drive Figure Eight: Create continuously drives in a figure 8
     * pattern</li>
     * 
     * <li>Wimp: Create drives forward when pushed from
     * behind. If Create hits an obstacle while
     * driving, it drives away from the obstacle.</li>
     * 
     * <li>Home: Create drives toward an iRobot Virtual
     * Wall as long as the back and sides of
     * the virtual wall receiver are blinded by
     * black electrical tape.
     * <br>
     * A Virtual Wall emits infrared signals
     * that Create sees with its Omnidirectional
     * Infrared Receiver, located on top of the
     * bumper. 
     * <br>
     * If you want Create to home in on a
     * Virtual Wall, cover all but a small
     * opening in the front of the infrared
     * receiver with black electrical tape.
     * <br>
     * Create spins to locate a virtual wall,
     * then drives toward it. Once Create hits
     * the wall or another obstacle, it stops.</li>
     * 
     * <li>Tag: Identical to the Home demo, except
     * Create drives into multiple virtual walls
     * by bumping into one, turning around,
     * driving to the next virtual wall, bumping
     * into it and turning around to bump into
     * the next virtual wall.</li>
     * 
     * <li>Pachelbel: Create plays the notes of Pachelbel’s
     * Canon in sequence when cliff sensors
     * are activated.</li> 
     * 
     * <li>Banjo: Create plays a note of a chord for each
     * of its four cliff sensors. Select the
     * chord using the bumper, as follows:
     * <br>- No bumper: G major.
     * <br>- Right/left bumper: D major 7
     * <br>- Both bumpers (center): C major</li> 
	 * @param value desired demo
	 */
	public void runDemo(DEMO value) {
		sendByte(136);
		sendByte(value.ordinal());		
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
		System.out.println(Arrays.toString(v)+":"+Arrays.toString(r));
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
	 * This command controls the LEDs on Create. The state of
	 * the Play and Advance LEDs is specified by two boolean
	 * parameters. The power LED is controlled by two parameters:
	 * one for the color and the other for the intensity. 
	 * @param adv true for Advance light on
	 * @param play true for Play light on
	 * @param powerColor color of power LED 0=green to 255=red
	 * @param powerIntensity brightness 0 to 255
	 */
	public void setLEDs(boolean adv, boolean play, int powerColor, int powerIntensity) {
		int advPlay = 0;
		if(adv) advPlay = advPlay | 8;
		if(play) advPlay = advPlay | 2;
		sendByte(139);
		sendByte(advPlay);
		sendByte(powerColor);
		sendByte(powerIntensity);
	}
	
	/**
	 * This command controls the state of the 3 digital output
	 * pins on the 25 pin Cargo Bay Connector. The digital outputs
	 * can provide up to 20 mA of current.
	 * @param out0 pin 19
	 * @param out1 pin 7
	 * @param out2 pin 20
	 * <p>
	 * Warning: When the Robot is switched ON, the Digital
	 * Outputs are High for the first 3 seconds during the
	 * initialization of the boot loader.
	 */
	public void setDigitalOutputs(boolean out0, boolean out1, boolean out2) {
		int value = 0;
		if(out0) value=value|1;
		if(out1) value=value|2;
		if(out2) value=value|3;
		sendByte(147);
		sendByte(value);
	}
	
	/**
	 * This command lets you control the three low side drivers
	 * with variable power. Each parameter is the duty cycle 
	 * on the given pin from 0% to 100%. 
	 * @param out0 duty cycle 0.0 to 1.0 (0% to 100%)
	 * @param out1 duty cycle 0.0 to 1.0 (0% to 100%)
	 * @param out2 duty cycle 0.0 to 1.0 (0% to 100%)
	 */
	public void setLowSideDrivers(double out0, double out1, double out2) {
		sendByte(144);
		sendByte((int)(out2*128));
		sendByte((int)(out1*128));
		sendByte((int)(out0*128));
	}
	
	/**
	 * This command lets you control the three low side drivers. 
	 * Low side drivers 0 and 1 can provide up to 0.5A of current.
	 * Low side driver 2 can provide up to 1.5 A of current. If too
	 * much current is requested, the current is limited and the
	 * overcurrent flag is set (sensor packet 14).
	 * @param out0 true for 100%
	 * @param out1 true for 100%
	 * @param out2 true for 100%
	 */
	public void setLowSideDriversFull(boolean out0, boolean out1, boolean out2) {
		int value = 0;
		if(out0) value=value|1;
		if(out1) value=value|2;
		if(out2) value=value|3;
		sendByte(138);
		sendByte(value);
	}
	
	/**
	 * This command sends the requested byte out of low side
	 * driver 1 (pin 23 on the Cargo Bay Connector), using the
	 * format expected by iRobot Create’s IR receiver. You must
	 * use a preload resistor (suggested value: 100 ohms) in
	 * parallel with the IR LED and its resistor in order turn it on. 
	 * There is an example schematic in the interface document.
	 * @param value data byte to send
	 */
	public void sendIRByte(int value) {
		sendByte(151);
		sendByte(value);
	}
	
	/**
	 * This command lets you specify up to sixteen songs to the OI
	 * that you can play at a later time. Each song is associated
	 * with a song number. The Play command uses the song
	 * number to identify your song selection. Each song can
	 * contain up to sixteen notes. Each note is associated with a
	 * note number that uses MIDI note definitions and a duration
	 * that is specified in fractions of a second. The number of 
	 * parameters varies, depending on the length of the song specified.
	 * A one note song is specified by two parameters. For each
	 * additional note within a song, add two parameters.
	 * @param number song number (0-15)
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
		sendByte(packetID.ordinal());
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
	
	/**
	 * This command specifies a script to be played later. A script
	 * consists of OI commands and can be up to 100 bytes long.
	 * There is no flow control, but "wait" commands (see below)
	 * cause Create to hold its current state until the specified
	 * event is detected.
	 * @param script raw script of bytes
	 */
	public void storeScript(int ... script) {
		if(script.length>100) {
			throw new RuntimeException("100 bytes is the max script length.");
		}
		sendByte(152);
		sendByte(script.length);
		for(int val : script) {
			sendByte(val);
		}
	}
	public void storeScript(List<Integer> script) {
		if(script.size()>100) {
			throw new RuntimeException("100 bytes is the max script length.");
		}
		sendByte(152);
		sendByte(script.size());
		for(int val : script) {
			sendByte(val);
		}
	}
	
	/**
	 * This command loads a previously defined OI script into the
     * serial input queue for playback.
	 */
	public void runScript() {
		sendByte(153);
	}
	
	/**
	 * This command returns the values of a previously stored
	 * script, starting with the number of bytes in the script and
	 * followed by the script’s commands and data bytes. It first
	 * halts the sensor stream, if one has been started with a
	 * Stream or Pause/Resume Stream command.
	 * @return the stored script
	 */
	public int[] readStoreScript() {
		sendByte(154);
		int len = readByte();
		int[] ret = new int[len];
		for(int i=0;i<len;++i) {
			ret[i] = readByte();
		}
		return ret;
	}
	
	/**
	 * This command causes Create to wait for the specified time.
	 * During this time, Create’s state does not change, nor does
	 * it react to any inputs, serial or otherwise.
	 * @param time wait time in tenths of second with a resolution of 15 ms
	 */
	public void scriptWaitTime(int time) {
		sendByte(155);
		sendByte(time);
	}
	
	/**
	 * This command causes iRobot Create to wait until it has
	 * traveled the specified distance in mm. When Create travels
	 * forward, the distance is incremented. When Create travels
	 * backward, the distance is decremented. If the wheels
	 * are passively rotated in either direction, the distance is
	 * incremented. Until Create travels the specified distance,
	 * its state does not change, nor does it react to any inputs,
	 * serial or otherwise.
	 * <br>
	 * NOTE: This command resets the distance variable that is
	 * returned in Sensors packets 19, 2 and 6.
	 * @param distance signed distance in mm
	 */
	public void scriptWaitDistance(int distance) {
		int[] d = twoByteSigned(distance);
		sendByte(156);
		sendByte(d[0]);
		sendByte(d[1]);
	}
	
	/**
	 * This command causes Create to wait until it has rotated
	 * through specified angle in degrees. When Create turns
	 * counterclockwise, the angle is incremented. When Create
	 * turns clockwise, the angle is decremented. Until Create
	 * turns through the specified angle, its state does not change,
	 * nor does it react to any inputs, serial or otherwise.
	 * <br>
	 * NOTE: This command resets the angle variable that is
	 * returned in Sensors packets 20, 2 and 6.
	 * @param angle signed angle in degrees
	 */
	public void scriptWaitAngle(int angle) {
		int[] a = twoByteSigned(angle);
		sendByte(157);
		sendByte(a[0]);
		sendByte(a[1]);
	}
	
	/**
	 * This command causes Create to wait until it detects the
	 * specified event. Until the specified event is detected,
	 * Create’s state does not change, nor does it react to any
	 * inputs, serial or otherwise.
	 * @param event the event to wait on
	 * @param inverse true to flip the logic to wait until NOT the event
	 */
	public void scriptWaitEvent(EVENT event, boolean inverse) {
		int e = event.ordinal()+1;
		if(inverse) e=256-e;
		sendByte(158);
		sendByte(e);
	}	
	
	// When you read a sensor packet the result is cached and can be decoded
	// with these getters.
	
	// Sensor GETters.
	
	/**
	 * The state of the bumper (0 = no bump, 1 = bump) and wheel
	 * drop sensors (0 = wheel raised, 1 = wheel dropped) are sent
	 * as individual bits.	
	 * @return all five bits 4:drop-caster, 3:drop-left, 2:drop-right, 1:bump-left, 0-bump-right
	 */
	public int getBumpsAndDrops() {
		int[] cache = cachedSensorPackets.get(SENSOR_PACKET.P07_BUMPS_AND_WHEEL_DROPS);
		return cache[0];
	}	
	/**
	 * The state of the wheeldrop caster sensor.
	 * @return true if dropped
	 */
	public boolean isWheeldropCaster() {
		return (getBumpsAndDrops()&16)>0;
	}	
	/**
	 * The state of the wheeldrop left sensor.
	 * @return true if dropped
	 */
	public boolean isWheeldropLeft() {
		return (getBumpsAndDrops()&8)>0;
	}
	/**
	 * The state of the wheeldrop right sensor.
	 * @return true if dropped
	 */
	public boolean isWheeldropRight() {
		return (getBumpsAndDrops()&4)>0;
	}
	/**
	 * The state of the left bump sensor.
	 * @return true if dropped
	 */
	public boolean isBumpLeft() {
		return (getBumpsAndDrops()&2)>0;
	}
	/**
	 * The state of the right bump sensor.
	 * @return true if dropped
	 */
	public boolean isBumpRight() {
		return (getBumpsAndDrops()&1)>0;
	}	
	
	/**
	 * The state of the wall sensor.
	 * @return true if seen
	 */
	public boolean isWall() {
		int[] cache = cachedSensorPackets.get(SENSOR_PACKET.P08_WALL);
		return cache[0]>0;
	}
	
	/**
	 * The state of the cliff sensor on the left side of Create.
	 * @return true if cliff
	 */
	public boolean isCliffLeft() {
		int[] cache = cachedSensorPackets.get(SENSOR_PACKET.P09_CLIFF_LEFT);
		return cache[0]>0;
	}
	
	/**
	 * The state of the cliff sensor on the front left of Create.
	 * @return true if cliff
	 */
	public boolean isCliffFrontLeft() {
		int[] cache = cachedSensorPackets.get(SENSOR_PACKET.P10_CLIFF_FRONT_LEFT);
		return cache[0]>0;
	}
	
	/**
	 * The state of the cliff sensor on the front right of Create.
	 * @return true if cliff
	 */
	public boolean isCliffFrontRight() {
		int[] cache = cachedSensorPackets.get(SENSOR_PACKET.P11_CLIFF_FRONT_RIGHT);
		return cache[0]>0;
	}
	
	/**
	 * The state of the cliff sensor on the right side of Create.
	 * @return true if cliff
	 */
	public boolean isCliffRight() {
		int[] cache = cachedSensorPackets.get(SENSOR_PACKET.P12_CLIFF_RIGHT);
		return cache[0]>0;
	}
	
	/**
	 * The state of the virtual wall detector.
	 * Note that the force field on top of the Home Base also trips
	 * this sensor.
	 * @return true if wall
	 */
	public boolean isVirtualWall() {
		int[] cache = cachedSensorPackets.get(SENSOR_PACKET.P13_VIRTUAL_WALL);
		return cache[0]>0;
	}	
	
	/**
	 * The state of the three Low Side driver and two wheel
	 * overcurrent sensors are sent as individual bits (0 = no
	 * overcurrent, 1 = overcurrent). LDA 0.5A, LD1 0.5A, LD2 1.6A, Wheels 1.0A
	 * @return 5 bits 4:left-wheel, 3:right-wheel, 2:LD2, 1:LD0, 0:LD1
	 */
	public int getOvercurrents() {
		int[] cache = cachedSensorPackets.get(SENSOR_PACKET.P14_LOW_SIDE_DRIVER_AND_WHEEL_OVERCURRENTS);
		return cache[0];
	}
	/**
	 * The state of the overcurrent.
	 * @return true if overcurrent
	 */
	public boolean isLeftWheelOvercurrent() {
		return (getOvercurrents()&16)>0;
	}
	/**
	 * The state of the overcurrent.
	 * @return true if overcurrent
	 */
	public boolean isRightWheelOvercurrent() {
		return (getOvercurrents()&8)>0;		
	}
	/**
	 * The state of the overcurrent.
	 * @return true if overcurrent
	 */
	public boolean isLD2Overcurrent() {
		return (getOvercurrents()&4)>0;
	}
	/**
	 * The state of the overcurrent.
	 * @return true if overcurrent
	 */
	public boolean isLD1Overcurrent() {
		return (getOvercurrents()&1)>0;
	}
	/**
	 * The state of the overcurrent.
	 * @return true if overcurrent
	 */
	public boolean isLD0Overcurrent() {
		return (getOvercurrents()&2)>0;
	}	
	
	/**
	 * This value identifies the IR byte currently being received
	 * by iRobot Create. A value of 255 indicates that no IR byte
	 * is being received. These bytes include those sent by the
	 * Roomba Remote, the Home Base, Create robots using the
	 * Send IR command, and user-created devices.
	 * See the interface document for more information.
	 * @return the byte from the IR or 255 if none
	 */
	public int getInfraredByte() {
		int[] cache = cachedSensorPackets.get(SENSOR_PACKET.P17_INFRARED_BYTE);
		return cache[0];
	}
	
	/**
	 * The state of Create’s Play and Advance buttons are sent as
	 * individual bits (0 = button not pressed, 1 = button pressed).
	 * @return the bits 2:advance, 0:play
	 */
	public int getButtons() {
		int[] cache = cachedSensorPackets.get(SENSOR_PACKET.P18_BUTTONS);
		return cache[0];
	}
	/**
	 * State of the ADVANCE button.
	 * @return true if pressed
	 */
	public boolean isButtonAdv() {
		return (getButtons()&4)>0;
	}
	/**
	 * State of the PLAY button. 
	 * @return true if pressed
	 */
	public boolean isButtonPlay() {
		return (getButtons()&1)>0;
	}	
	
	/**
	 * The distance that Create has traveled in millimeters since the
	 * distance it was last requested. This is the same as the sum of the distance
	 * traveled by both wheels divided by two. Positive values indicate
	 * travel in the forward direction; negative values indicate travel
	 * in the reverse direction. If the value is not polled frequently
	 * enough, it is capped at its minimum or maximum.
	 * @return distance traveled in mm (signed)
	 */
	public int getDistanceTraveled() {
		int[] cache = cachedSensorPackets.get(SENSOR_PACKET.P19_DISTANCE);
		return fromTwoByteSigned(cache[0],cache[1]);
	}
	
	/**
	 * The angle in degrees that iRobot Create has turned since the
	 * angle was last requested. Counter-clockwise angles are positive and clockwise
	 * angles are negative. If the value is not polled frequently
	 * enough, it is capped at its minimum or maximum.
	 * <p>
	 * Range: -32768 – 32767
	 * <p>>
	 * NOTE: Create uses wheel encoders to measure distance
	 * and angle. If the wheels slip, the actual distance or angle
	 * traveled may differ from Create’s measurements.
	 * @return angle turned
	 */
	public int getAngleTurned()  {
		int[] cache = cachedSensorPackets.get(SENSOR_PACKET.P20_ANGLE);
		return fromTwoByteSigned(cache[0],cache[1]);
	}	
	
	/**
	 * This code indicates Create’s current charging state. 
	 * @return 0:not-charging, 1:reconditioning-charging, 2:full-charging, 3:trickle-charging, 4:waiting, 5:charging-fault
	 */
	public int getChargingState() {
		int[] cache = cachedSensorPackets.get(SENSOR_PACKET.P21_CHARGING_STATE);
		return cache[0];
	}
	
	/**
	 * This code indicates the voltage of Create’s battery in
     * millivolts (mV).
	 * @return battery voltage in mV
	 */
	public int getVoltage() {
		int[] cache = cachedSensorPackets.get(SENSOR_PACKET.P22_VOLTAGE);
		return fromTwoByteSigned(cache[0],cache[1]);
	}
	
	/**
	 * The current in milliamps (mA) flowing into or out of Create’s
	 * battery. Negative currents indicate that the current is flowing
	 * out of the battery, as during normal running. Positive currents
	 * indicate that the current is flowing into the battery, as during
	 * charging. 
	 * @return current in mA
	 */
	public int getCurrent() {
		int[] cache = cachedSensorPackets.get(SENSOR_PACKET.P23_CURRENT);
		return fromTwoByteSigned(cache[0],cache[1]);
	}
	
	/**
	 * The temperature of Create’s battery in degrees Celsius (signed).
	 * @return the battery temp in degrees C
	 */
	public int getBatteryTemperature() {
		int[] cache = cachedSensorPackets.get(SENSOR_PACKET.P24_BATTERY_TEMPERATURE);
		return cache[0];
	}
	
	/**
	 * The current charge of Create’s battery in milliamp-hours (mAh).
	 * The charge value decreases as the battery is depleted
	 * during running and increases when the battery is charged.
	 * Note that this value will not be accurate if you are using the
	 * alkaline battery pack.
	 * @return battery charge in mAh
	 */
	public int getBatteryCharge() {
		int[] cache = cachedSensorPackets.get(SENSOR_PACKET.P25_BATTERY_CHARGE);
		return fromTwoByteSigned(cache[0],cache[1]);
	}
	
	/**
	 * The estimated charge capacity of Create’s battery in milliamphours
	 * (mAh). Note that this value is inaccurate if you are using
	 * the alkaline battery pack.
	 * @return estimated batery capacity in mAh
	 */
	public int getBatteryCapacity() {
		int[] cache = cachedSensorPackets.get(SENSOR_PACKET.P26_BATTERY_CAPACITY);
		return fromTwoByteSigned(cache[0],cache[1]);
	}	
	
	/**
	 * The strength of the wall sensor’s signal.
	 * @return strength of signal 0-4095
	 */
	public int getWallSignal() {
		int[] cache = cachedSensorPackets.get(SENSOR_PACKET.P27_WALL_SIGNAL);
		return (cache[0]<<8) | cache[1];
	}
	
	/**
	 * The strength of the left cliff sensor’s signal.
	 * @return strength of signal 0-4095
	 */
	public int getCliffLeftSignal() {
		int[] cache = cachedSensorPackets.get(SENSOR_PACKET.P28_CLIFF_LEFT_SIGNAL);
		return (cache[0]<<8) | cache[1];
	}
	
    /**
     * The strength of the front left cliff sensor’s signal.
     * @return strength of signal 0-4095
     */
	public int getCliffFrontLeftSignal() {
		int[] cache = cachedSensorPackets.get(SENSOR_PACKET.P29_CLIFF_FRONT_LEFT_SIGNAL);
		return (cache[0]<<8) | cache[1];
	}
	
	/**
	 * The strength of the front right cliff sensor’s signal.
  	 * @return strength of signal 0-4095
	 */
	public int getCliffFrontRightSignal() {
		int[] cache = cachedSensorPackets.get(SENSOR_PACKET.P30_CLIFF_FRONT_RIGHT_SIGNAL);
		return (cache[0]<<8) | cache[1];
	}
	
	/**
	 * The strength of the right cliff sensor’s signal.
	 * @return strength of signal 0-4095
	 */
	public int getCliffRightSignal() {
		int[] cache = cachedSensorPackets.get(SENSOR_PACKET.P31_CLIFF_RIGHT_SIGNAL);
		return (cache[0]<<8) | cache[1];
	}
	
	/**
	 * The state of the digital inputs on the 25-pin Cargo Bay Connector
	 * are sent as individual bits (0 = low, 1 = high (5V)). Note that the
     * Baud Rate Change pin is active low; it is high by default.
     * <p>
     * Device Detect pin can be used to change Baud Rate. When
	 * device detect/baud rate change Bit is low, the Baud Rate
	 * is 19200. Otherwise it it 57600
	 * @return the input bits 4:baud-rate, 3:in3, 2:in2, 1:in1, 0:in0
	 */
	public int getCargoBayDigitalInputs() {
		int[] cache = cachedSensorPackets.get(SENSOR_PACKET.P32_CARGO_BAY_DIGITIAL_INPUTS);
		return cache[0];
	}	
	
	/**
	 * State of cargo-bay input 0.
	 * @return true if input is high
	 */
	public boolean isCargoBayIn0() {
		return (getCargoBayDigitalInputs()&1) > 0;
	}
	
	/**
	 * State of cargo-bay input 1.
	 * @return true if input is high
	 */
	public boolean isCargoBayIn1() {
		return (getCargoBayDigitalInputs()&2) > 0;
	}
	
	/**
	 * State of cargo-bay input 2.
	 * @return true if input is high
	 */
	public boolean isCargoBayIn2() {
		return (getCargoBayDigitalInputs()&4) > 0;
	}
	
	/**
	 * State of cargo-bay input 3.
	 * @return true if input is high
	 */
	public boolean isCargoBayIn3() {
		return (getCargoBayDigitalInputs()&8) > 0;
	}
	
	/**
	 * State of cargo-bay baud change.
	 * @return true if input is high
	 */
	public boolean isCargoBayBaudChange() {
		return (getCargoBayDigitalInputs()&16) > 0;
	}
	
	/**
	 * The 10-bit value of the analog input on the 25-pin Cargo Bay
	 * Connector. 0 = 0 volts; 1023 = 5
	 * volts. The analog input is on pin 4.
	 * @return the analog value 0 to 1023=5V
	 */
	public int getCargoBayAnalogSignal()  {
		int[] cache = cachedSensorPackets.get(SENSOR_PACKET.P33_CARGO_BAY_ANALOG_SIGNAL);
		return (cache[0]<<8) | cache[1];
	}
	
	/**
	 * iRobot Create’s connection to the Home Base and Internal
     * Charger are returned as individual bits, as below. 
	 * @return bits for all charging sources 1:Home-base, 0:Internal-charger
	 */
	public int getChargingSourcesAvailable() {
		int[] cache = cachedSensorPackets.get(SENSOR_PACKET.P34_CHARGING_SOURCES_AVAILABLE);
		return cache[0];
	}	
	
	/**
	 * Returns the availability of the home base charger.
	 * @return true if available
	 */
	public boolean isHomeBaseChargerAvailable() {
		return (getChargingSourcesAvailable()&2)>0;
	}
	
	/**
	 * Returns the availability of the internal charger.
	 * @return true if available
	 */
	public boolean isInternalChargerAvailable() {
		return (getChargingSourcesAvailable()&1)>0;
	}
	
	/**
	 * The current OI mode.
	 * @return the current OI mode.
	 */
	public MODE getOIMode() {
		int[] cache = cachedSensorPackets.get(SENSOR_PACKET.P35_OI_MODE);		
		return MODE.values()[cache[0]];
	}
	
	/**
	 * The currently selected OI song is returned.
	 * @return currently playing song
	 */
	public int getPlayingSongNumber() {
		int[] cache = cachedSensorPackets.get(SENSOR_PACKET.P36_SONG_NUMBER);
		return cache[0];
	}
	
	/**
	 * The state of the OI song player is returned.
	 * @return true if a song is playing
	 */
	public boolean isSongPlaying() {
		int[] cache = cachedSensorPackets.get(SENSOR_PACKET.P37_SONG_PLAYING);
		return cache[0]>0;
	}
	
	/**
	 * The number of data stream packets is returned. 
	 * @return number of packets
	 */
	public int getNumberStreamPackets() {
		int[] cache = cachedSensorPackets.get(SENSOR_PACKET.P38_NUMBER_OF_STREAM_PACKETS);
		return cache[0];
	}
	
	/**
	 * The velocity most recently requested with a Drive command. 
	 * @return the last requested velocity in mm/s (signed)
	 */
	public int getRequestedVelocity() {
		int[] cache = cachedSensorPackets.get(SENSOR_PACKET.P39_REQUESTED_VELOCITY);
		return fromTwoByteSigned(cache[0],cache[1]);
	}
	
	/**
	 * The radius most recently requested with a Drive command.
	 * @return the last requested radius in mm (signed)
	 */
	public int getRequestedRadius() {
		int[] cache = cachedSensorPackets.get(SENSOR_PACKET.P40_REQUESTED_RADIUS);
		return fromTwoByteSigned(cache[0],cache[1]);
	}
	
	/**
	 * The right wheel velocity most recently requested with a Drive
	 * Direct command.
	 * @return the last requested right wheel velocity in mm/s (signed)
	 */
	public int getRequestedRightVelocity() {
		int[] cache = cachedSensorPackets.get(SENSOR_PACKET.P41_REQUESTED_RIGHT_VELOCITY);
		return fromTwoByteSigned(cache[0],cache[1]);
	}
	
	/**
	 * The left wheel velocity most recently requested with a Drive
	 * Direct command.
	 * @return the last requested left wheel velocity in mm/s (signed)
	 */
	public int getRequestedLeftVeloicty() {
		int[] cache = cachedSensorPackets.get(SENSOR_PACKET.P42_REQUESTED_LEFT_VELOCITY);
		return fromTwoByteSigned(cache[0],cache[1]);
	}	

}
