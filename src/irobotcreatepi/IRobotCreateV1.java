package irobotcreatepi;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *  For V1 the default comm is 57600, no parity, 1 stop, no flow.
 * 
 * You MUST send the passive mode command at startup. Set the mode to PASSIVE.
 * Then wait one second for the mode to change. THEN you can talk to the controller.
 */
public class IRobotCreateV1 {
		
	private InputStream is;
	private OutputStream os;
	
	private Map<SENSOR_PACKET,Integer[]> cachedSensorPackets = new HashMap<SENSOR_PACKET,Integer[]>();
	
	private void sendByte(int value) {
		try {
			os.write(value);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}	
	
	private int readByte() {
		try {
			while(true) {
				int ret = is.read();
				if(ret>=0) return ret;
				try{Thread.sleep(10);} catch (Exception e) {}
			}			
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
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
		
	public enum MODE {PASSIVE, SAFE, FULL}
	
	public enum BAUD {B300, B600, B1200, B2400, B4800, B9600, B14400, B19200, B28800, B38400, B57600, B115200}
	
	public enum DEMO {COVER, COVER_DOCK, SPOT_DOCK, MOUSE, DRIVE_FIGURE_EIGHT, WIMP, HOME, TAG, RACHELBEL, BANJO}
	
	public enum SENSOR_PACKET {
		
		P00_GROUP0(26), // 256 bytes  7-26
		P01_GROUP1(10), //  10 bytes  7-16
		P02_GROUP2(6),  //   6 bytes 17-20    
		P03_GROUP3(10), //  10 bytes 21-26
		P04_GROUP4(14), //  14 bytes 27-34
		P05_GROUP5(12), //  12 bytes 35-42
		P06_GROUP6(52), //  52 bytes  7-42
		
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
		
		int numBytes;
		
		SENSOR_PACKET(int numBytes) {
			this.numBytes = numBytes;
		}
		
		public int getNumBytes() {
			return numBytes;
		}
		
	}
	
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
	 * characters may not be received.<b>
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
     * built-in demos. 
     * <p>
     * Cover: Create attempts to cover an entire
     * room using a combination of behaviors,
     * such as random bounce, wall following,
     * and spiraling.
     * <p>
     * Cover and Dock: Identical to the Cover demo, with one
     * exception. If Create sees an infrared
     * signal from an iRobot Home Base, it
     * uses that signal to dock with the Home
     * Base and recharge itself.
     * <p>
     * Spot Cover: Create covers an area around its
     * starting position by spiraling outward,
     * then inward.
     * <p>
     * Mouse: Create drives in search of a wall. Once
     * a wall is found, Create drives along the
     * wall, traveling around circumference of
     * the room.
     * <p>
     * Drive Figure Eight: Create continuously drives in a figure 8
     * pattern.
     * <p>
     * Wimp: Create drives forward when pushed from
     * behind. If Create hits an obstacle while
     * driving, it drives away from the obstacle.
     * <p>
     * Home:Create drives toward an iRobot Virtual
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
     * the wall or another obstacle, it stops.
     * <p>
     * Tag: Identical to the Home demo, except
     * Create drives into multiple virtual walls
     * by bumping into one, turning around,
     * driving to the next virtual wall, bumping
     * into it and turning around to bump into
     * the next virtual wall.
     * <p>
     * Pachelbel: Create plays the notes of Pachelbel’s
     * Canon in sequence when cliff sensors
     * are activated. 
     * <p>
     * Banjo: Create plays a note of a chord for each
     * of its four cliff sensors. Select the
     * chord using the bumper, as follows:
     * <br>No bumper: G major.
     * <br>Right/left bumper: D major 7
     * <br>Both bumpers (center): C major 
     * <p>
	 * @param value desired demo
	 */
	public void runDemo(DEMO value) {
		sendByte(136);
		sendByte(value.ordinal());		
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
	
	/**
	 * Drive in a straight line. See drive().
	 * @param velocity robot velocity -500mm/s to 500mm/s
	 */
	public void driveStraight(int velocity) {	
		drive(velocity, 32768);
	}
	
	/**
	 * Spin in place. See drive().
	 * @param velocity robot velocity -500mm/s to 500mm/s
	 * @param clockWise true for clockwise or false for counter clockwise
	 */
	public void driveSpin(int velocity, boolean clockWise) {	
		if(clockWise) {
			drive(velocity,-1);
		} else {
			drive(velocity,1);
		}
	}
	
	/**
	 * This command controls Create’s drive wheels. It takes four
	 * data bytes, interpreted as two 16-bit signed values using
	 * two’s complement. The first two bytes specify the average
	 * velocity of the drive wheels in millimeters per second
	 * (mm/s), with the high byte being sent first. The next two
	 * bytes specify the radius in millimeters at which Create will
	 * turn. The longer radii make Create drive straighter, while
	 * the shorter radii make Create turn more. The radius is
	 * measured from the center of the turning circle to the center
	 * of Create. A Drive command with a positive velocity and a
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
	 * motion of Create’s drive wheels independently. It takes
	 * four data bytes, which are interpreted as two 16-bit signed
	 * values using two’s complement. The first two bytes specify
	 * the velocity of the right wheel in millimeters per second
	 * (mm/s), with the high byte sent first. The next two bytes
	 * specify the velocity of the left wheel, in the same format.
	 * A positive velocity makes that wheel drive forward, while a
	 * negative velocity makes it drive backward. 
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
	 * the Play and Advance LEDs is specified by two bits in the
	 * first data byte. The power LED is specified by two data
	 * bytes: one for the color and the other for the intensity. 
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
	 * initialization of the bootloader
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
	 * with variable power. With each data byte, you specify the
	 * PWM duty cycle for the low side driver (max 128). For
	 * example, if you want to control a driver with 25% of battery
	 * voltage, choose a duty cycle of 128 * 25% = 32.
	 * @param out0 duty cycle 0-128
	 * @param out1 duty cycle 0-127
	 * @param out2 duty cycle 0-128
	 */
	public void setLowSideDrivers(int out0, int out1, int out2) {
		sendByte(144);
		sendByte(out2);
		sendByte(out1);
		sendByte(out0);
	}
	
	/**
	 * This command lets you control the three low side drivers. The
	 * state of each driver is specified by one bit in the data byte.
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
	 * that is specified in fractions of a second. The number of data
	 * bytes varies, depending on the length of the song specified.
	 * A one note song is specified by four data bytes. For each
	 * additional note within a song, add two data bytes.
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
		int [] ret = new int[packetID.getNumBytes()];
		for(int x=0;x<ret.length;++x) {
			ret[x] = readByte();
		}
		return ret;
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
			sendByte(id.ordinal());
			totalSize += id.getNumBytes();
		}
		int [] ret = new int[totalSize];
		for(int x=0;x<ret.length;++x) {
			ret[x] = readByte();
		}
		return ret;
	}
	
	public void streamSensorPackets(SENSOR_PACKET ... packetIDs) {	
		// TODO
	}
	
	public void pauseSensorStream() {
		sendByte(150);
		sendByte(0);
	}
	public void resumeSensorStream() {
		sendByte(150);
		sendByte(1);
	}
	
	public void storeScript(int ... script) {
		sendByte(152);
		sendByte(script.length);
		for(int val : script) {
			sendByte(val);
		}
	}
	
	public void runScript() {
		sendByte(153);
	}
	
	public int[] readStoreScript() {
		sendByte(154);
		// TODO read script
		return null;
	}
	
	public void scriptWaitTime(int time) {
		sendByte(155);
		sendByte(time);
	}
	
	public void scriptWaitDistance(int distance) {
		int[] d = twoByteSigned(distance);
		sendByte(156);
		sendByte(d[0]);
		sendByte(d[1]);
	}
	
	public void scriptWaitAngle(int angle) {
		int[] a = twoByteSigned(angle);
		sendByte(156);
		sendByte(a[0]);
		sendByte(a[1]);
	}
	
	public void scriptWaitEvent(EVENT event, boolean inverse) {
		int e = event.ordinal()+1;
		if(inverse) e=256-e;
		sendByte(158);
		sendByte(e);
	}	
	
	// When you read a sensor packet the result is cached and can be decoded
	// with these getters.
	
	// TODO javadoc these
	
	public int getBumpsAndDrops() {
		Integer[] cache = cachedSensorPackets.get(SENSOR_PACKET.P07_BUMPS_AND_WHEEL_DROPS);
		return cache[0];
	}	
	public boolean isWheeldropCaster() {
		return (getBumpsAndDrops()&16)>0;
	}	
	public boolean isWheeldropLeft() {
		return (getBumpsAndDrops()&8)>0;
	}
	public boolean isWheeldropRight() {
		return (getBumpsAndDrops()&4)>0;
	}
	public boolean isBumpLeft() {
		return (getBumpsAndDrops()&2)>0;
	}
	public boolean isBumpRight() {
		return (getBumpsAndDrops()&1)>0;
	}	
	public boolean isWall() {
		Integer[] cache = cachedSensorPackets.get(SENSOR_PACKET.P08_WALL);
		return cache[0]>0;
	}
	public boolean isCliffLeft() {
		Integer[] cache = cachedSensorPackets.get(SENSOR_PACKET.P09_CLIFF_LEFT);
		return cache[0]>0;
	}
	public boolean isCliffFrontLeft() {
		Integer[] cache = cachedSensorPackets.get(SENSOR_PACKET.P10_CLIFF_FRONT_LEFT);
		return cache[0]>0;
	}
	public boolean isCliffFrontRight() {
		Integer[] cache = cachedSensorPackets.get(SENSOR_PACKET.P11_CLIFF_FRONT_RIGHT);
		return cache[0]>0;
	}
	public boolean isCliffRight() {
		Integer[] cache = cachedSensorPackets.get(SENSOR_PACKET.P12_CLIFF_RIGHT);
		return cache[0]>0;
	}
	public boolean isVirtualWall() {
		Integer[] cache = cachedSensorPackets.get(SENSOR_PACKET.P13_VIRTUAL_WALL);
		return cache[0]>0;
	}	
	public int getOvercurrents() {
		Integer[] cache = cachedSensorPackets.get(SENSOR_PACKET.P14_LOW_SIDE_DRIVER_AND_WHEEL_OVERCURRENTS);
		return cache[0];
	}
	public boolean isLeftWheelOvercurrent() {
		return (getOvercurrents()&16)>0;
	}
	public boolean isRightWheelOvercurrent() {
		return (getOvercurrents()&8)>0;		
	}
	public boolean isLD2Overcurrent() {
		return (getOvercurrents()&4)>0;
	}
	public boolean isLD1Overcurrent() {
		return (getOvercurrents()&1)>0;
	}
	public boolean isLD0Overcurrent() {
		return (getOvercurrents()&2)>0;
	}	
	public int getInfraredByte() {
		Integer[] cache = cachedSensorPackets.get(SENSOR_PACKET.P17_INFRARED_BYTE);
		return cache[0];
	}
	public int getButtons() {
		Integer[] cache = cachedSensorPackets.get(SENSOR_PACKET.P18_BUTTONS);
		return cache[0];
	}
	public boolean isButtonAdv() {
		return (getButtons()&4)>0;
	}
	public boolean isButtonPlay() {
		return (getButtons()&1)>0;
	}	
	
	// TODO
	public int getDistanceTraveled() {return 0;}
	public int getAngleTurned() {return 0;}	
	public int getChargingState() {return 0;}
	public int getVoltage() {return 0;}
	public int getCurrent() {return 0;}
	public int getBatteryTemperature() {return 0;}
	public int getBatteryCharge() {return 0;}
	public int getBatteryCapacity() {return 0;}	
	public int getWallSignal() {return 0;}
	public int getCliffLeftSignal() {return 0;}
	public int getCliffFrontLeftSignal() {return 0;}
	public int getCliffFrontRightSignal() {return 0;}
	public int getCliffRightSignal() {return 0;}
	public int getCagoBayDigitalInputs() {return 0;}	
	public boolean isCargoBayIn0() {return false;}
	public boolean isCargoBayIn1() {return false;}
	public boolean isCargoBayIn2() {return false;}
	public boolean isCargoBayIn3() {return false;}
	public boolean isCargoBayBaudChange() {return false;}	
	public int getCargoBayAnalogSignal() {return 0;}
	public int getChargingSourcesAvaialable() {return 0;}	
	public boolean isHomeBaseChargerAvailable() {return false;}
	public boolean isInternalChargerAvailable() {return false;}	
	public MODE getOIMode() {return null;}
	public int getPlayingSongNumber() {return 0;}
	public boolean isSongPlaying() {return false;}
	public int getNumberStreamPackets() {return 0;}
	public int getRequestedVelocity() {return 0;}
	public int getRequestedRadius() {return 0;}
	public int getRequestedRightVelocity() {return 0;}
	public int getRequestedLeftVeloicty() {return 0;}	

}
