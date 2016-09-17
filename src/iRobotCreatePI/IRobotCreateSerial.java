package iRobotCreatePI;

import java.io.IOException;
import java.io.OutputStream;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

public class IRobotCreateSerial extends IRobotCreate {
	
	private SerialPort serialPort;
	private OutputStream os;
	//private InputStream is;
	
	public IRobotCreateSerial(String port) throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, IOException {
		
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier("COM3");
		serialPort = (SerialPort) portIdentifier.open("CreatePi",2000);					
		serialPort.setSerialPortParams(57600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
		
		os = serialPort.getOutputStream();
		//is = serialPort.getInputStream();
		
	}
	
	@Override
	public void sendByte(int value) {
		try {
			os.write(value);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	

}
