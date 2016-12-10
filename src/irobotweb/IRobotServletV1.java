package irobotweb;

import java.util.List;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import irobotcreatepi.IRobotCreateV1;
import irobotcreatepi.IRobotCreateV1.EVENT;
import irobotcreatepi.IRobotCreateV1.SENSOR_PACKET;

//import irobotcreatepi.IRobotCreateV1;

public class IRobotServletV1  extends HttpServlet
{
    
	private static final long serialVersionUID = 1L;
	
	private static IRobotCreateV1 robot;
	static {
		try {
			
			// Are we running on the pi or on the PC?
			File f = new File("/etc");
			String devname;
			if(f.exists()) {
				devname = "/dev/ttyUSB0"; // Pi
			} else {
				devname = "COM12"; // PC
			}			
			
			CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(devname);
			SerialPort serialPort = (SerialPort) portIdentifier.open("CreatePi",2000);					
			serialPort.setSerialPortParams(57600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
			serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);		
			serialPort.setInputBufferSize(256);		
			
			System.out.println("USING PORT '"+devname+"'");
			
			robot = new IRobotCreateV1(serialPort.getInputStream(),serialPort.getOutputStream());
			
			robot.setMode(IRobotCreateV1.MODE.PASSIVE); // Required at startup
			Thread.sleep(1000); // Wait for mode change	
			
			robot.setMode(IRobotCreateV1.MODE.FULL);
			//robot.setMode(IRobotCreateV1.MODE.SAFE);
			Thread.sleep(1000);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}	
	
	List<String> parseCommand(String com) {
		List<String> ret = new ArrayList<String>();
		
		int i = com.indexOf('(');
		if(i<0) {
			ret.add(com.toUpperCase().trim());
			return ret;
		}
		int j = com.indexOf(')');
		ret.add(com.substring(0, i).toUpperCase().trim());
		String params = com.substring(i+1,j);
		String[] pas = params.split(",");
		for(String p : pas) {
			ret.add(p.toUpperCase().trim());
		}
		
		return ret;
	}
	
	void parseScriptCommand(String com, List<Integer> script) {
		
		if(com.trim().isEmpty()) return;
		
		List<String> sc = parseCommand(com);
		switch(sc.get(0)) {
		case "DRIVE":
			int [] v = IRobotCreateV1.twoByteSigned(Integer.parseInt(sc.get(1)));
			int [] r;
			if(sc.size()>2) {
				r = IRobotCreateV1.twoByteSigned(Integer.parseInt(sc.get(2)));
			} else {
				r = IRobotCreateV1.twoByteSigned(32768);
			}
			script.add(137);
			script.add(v[0]);script.add(v[1]);
			script.add(r[0]);script.add(r[1]);
			break;
		case "WAITTIME":
			int time = Integer.parseInt(sc.get(1));
			script.add(155);
			script.add(time);
			break;
		case "WAITDISTANCE":
			int[] d = IRobotCreateV1.twoByteSigned(Integer.parseInt(sc.get(1)));
			script.add(156);
			script.add(d[0]);
			script.add(d[1]);
			break;
		case "WAITANGLE":
			int[] a = IRobotCreateV1.twoByteSigned(Integer.parseInt(sc.get(1)));
			script.add(157);
			script.add(a[0]);
			script.add(a[1]);
			break;
		case "WAITPLAYBUTTON":
			script.add(158);
			script.add(EVENT.PLAY_BUTTON.ordinal()+1);
			break;
		case "WAITADVANCEBUTTON":
			script.add(158);
			script.add(EVENT.ADVANCE_BUTTON.ordinal()+1);
			break;
		case "WAITBUMP":
			script.add(158);
			script.add(EVENT.BUMP.ordinal()+1);
			break;
		default:
			throw new RuntimeException("Unknown script command '"+com+"'");
		}
	}
	
	List<Integer> parseScript(String scriptString) {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		String[] ss = scriptString.split(";");
		for(String s : ss) {
			parseScriptCommand(s,ret);
		}
		return ret;
	}

	@Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		
		String command = request.getParameter("command");
		String script = request.getParameter("script");
		
		// TODO setLEDs, playSong
		
		if(command!=null) {
			List<String> com = parseCommand(command);
			switch(com.get(0)) {
			case "DRIVE":
				int vel = Integer.parseInt(com.get(1));
				int rad = 32768;
				if(com.size()>2) {
					rad = Integer.parseInt(com.get(2));
				}
				robot.drive(vel, rad);
				response.getWriter().println("{}");
				break;			
			default:
				robot.readSensorPackets(SENSOR_PACKET.P02_GROUP2,SENSOR_PACKET.P03_GROUP3); // 17-26
				String ret = "{"+
				    "\"distance\":"+robot.getDistanceTraveled()+","+
				    "\"angle\":"+robot.getAngleTurned()+","+
				    "\"batteryCharge\":"+robot.getBatteryCharge()+","+
				    "\"batteryCapacity\":"+robot.getBatteryCapacity()+","+
				    "\"current\":"+robot.getCurrent()+","+
				    "\"chargingState\":"+robot.getChargingState()+"}";
				
				response.setContentType("application/json");
				response.getWriter().println(ret);			
			}
			
			robot.readSensorPackets(SENSOR_PACKET.P02_GROUP2,SENSOR_PACKET.P03_GROUP3); // 17-26
			String ret = "{"+
			    "\"distance\":"+robot.getDistanceTraveled()+","+
			    "\"angle\":"+robot.getAngleTurned()+","+
			    "\"batteryCharge\":"+robot.getBatteryCharge()+","+
			    "\"batteryCapacity\":"+robot.getBatteryCapacity()+","+
			    "\"current\":"+robot.getCurrent()+","+
			    "\"chargingState\":"+robot.getChargingState()+"}";
			
			response.getWriter().println(ret);			
			
		} else if(script!=null) {
			List<Integer> sc = parseScript(script+";drive(0)");
			robot.storeScript(sc);
			robot.runScript();			
			
			response.getWriter().println("{}");	
			
		}
		
		response.setContentType("application/json");
		
    }

}
