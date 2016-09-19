package irobotcreatepi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

public class IRobotCreateSerial extends IRobotCreate {
	
	private SerialPort serialPort;
	private OutputStream os;
	private InputStream is;
	
	public IRobotCreateSerial(String port) throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, IOException {
		
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(port);
		serialPort = (SerialPort) portIdentifier.open("CreatePi",2000);					
		serialPort.setSerialPortParams(57600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
		serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
		
		serialPort.setInputBufferSize(256);
		
		os = serialPort.getOutputStream();
		is = serialPort.getInputStream();
		
	}
	
	@Override
	public void sendByte(int value) {
		try {
			os.write(value);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public int readByte() {
		try {
			while(true) {
				int ret = is.read();
				if(ret>=0) return ret;
				try{Thread.sleep(10);} catch (Exception e) {}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
