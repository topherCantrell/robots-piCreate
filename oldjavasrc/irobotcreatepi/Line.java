package irobotcreatepi;

public class Line {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		System.out.println("Here");
		
		SensorBar bar = new SensorBar(0x3E);
		
		bar.setBarStrobe();
		
		bar.clearInvertBits();
		
		bar.begin();
		
		while(true) {
			int raw = bar.getRaw();
			System.out.println(raw);
			Thread.sleep(1_000);
		}
        
        

	}

}
