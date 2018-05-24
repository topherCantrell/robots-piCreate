package irobotcreatepi;

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

public class SensorBar {
	
	private static final int REG_RESET = 0;
	
	private I2CDevice device;
		
	public SensorBar(int address) throws UnsupportedBusNumberException, IOException {
		I2CBus i2c = I2CFactory.getInstance(I2CBus.BUS_1);
    	device = i2c.getDevice(0x3E);
    	
    	reset();
    	
	}
	
	public void reset() throws IOException {
		device.write(REG_RESET, (byte) 0x12);
		device.write(REG_RESET, (byte) 0x34);		
	}
	
	void begin() {}
	int getRaw() {return 0;}
	void getPosition() {}
	void getDensity() {}
	void setBarStrobe() {}
	void clearBarStrobe() {}
	void clearInvertBits() {}
	
	void debounceConfig(int value) {}
	void debounceEnable(int pin) {}
	void enableInterrupt(int pin, int riseFall) {}
	int getInterruptSource() {return 0;}
	void configClock(int oscSource, int oscPinFunction, int oscFreqOut, int oscDivider) {};

}
