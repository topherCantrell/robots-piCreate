package iRobotCreatePI;

public abstract class IRobotCreate {
	
	/**
	 * The default comm is 57600, no parity, 1 stop, no flow.
	 * 
	 * You MUST send the passive mode command at startup. Set the mode to PASSIVE.
	 * Then wait one second for the mode to change. THEN you can talk to the controller.
	 */
		
	public enum MODE {PASSIVE, SAFE, FULL}
	
	public enum BAUD {B300, B600, B1200, B2400, B4800, B9600, B14400, B19200, B28800, B38400, B57600, B115200}
	
	public enum DEMO {COVER, COVER_DOCK, SPOT_DOCK, MOUSE, DRIVE_FIGURE_EIGHT, WIMP, HOME, TAG, RACHELBEL, BANJO}
	
	public abstract void sendByte(int value);	
			
	public void setBaud(BAUD rate) {	
		// At 115200 there must be at least 200us between characters
		BAUD[] bauds = BAUD.values();
		for(int x=0;x<bauds.length;++x) {
			if(bauds[x] == rate) {
				sendByte(129);
				sendByte(x);
				return;
			}
		}
		throw new RuntimeException("Didn't find baud '"+rate+"'");
	}
			
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
	
	public void abortDemo() {
		sendByte(136);
		sendByte(255);
	}
	
	public void setDemo(DEMO value) {
		DEMO[] demos = DEMO.values();
		for(int x=0;x<demos.length;++x) {
			if(demos[x]==value) {
				sendByte(136);
				sendByte(x);
				return;
			}
		}
		throw new RuntimeException("Unhandled demo '"+value+"'");
	}
	
	public void drive() {
		// TODO		
	}
	
	public void driveDirect() {
		// TODO
	}
	
	public void setLEDs(boolean adv, boolean play, int powerColor, int powerIntensity) {
		// NOTE. The "advance" light doesn't seem to be controllable from here. I tried all bits.
		int advPlay = 255;
		if(adv) advPlay = advPlay | 8;
		if(play) advPlay = advPlay | 2;
		sendByte(139);
		sendByte(advPlay);
		sendByte(powerColor);
		sendByte(powerIntensity);
	}
	
	public void setDigitalOutputs(boolean out0, boolean out1, boolean out2) {
		// TODO
	}
	
	public void setLowSideDrivers(int out0, int out1, int out2) {
		// TODO
	}
	
	public void setLowSideDriversFull(boolean out0, boolean out1, boolean out2) {
		// TODO
	}
	
	public void sendIRByte(int value) {
		// TODO	
	}

}
