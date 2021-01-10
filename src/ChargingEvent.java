import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class ChargingEvent {
	
	private int indexCharger;
	private int indexChargingEvent;
	private int startTime;
	private double chargingSpeed;
	
	private ChargingEvent(int indexCharger, int indexChargingEvent, int startTime, double chargingSpeed) {
		this.indexCharger = indexCharger;
		this.indexChargingEvent = indexChargingEvent;
		this.startTime = startTime;
		this.chargingSpeed = chargingSpeed;
	}
	
	public int getIndexCharger() {
		return indexCharger;
	}

	public int getIndexChargingEvent() {
		return indexChargingEvent;
	}

	public int getStartTime() {
		return startTime;
	}

	public double getChargingSpeed() {
		return chargingSpeed;
	}
	
	public static ArrayList<ChargingEvent> createChargingEvents(String fileName) throws FileNotFoundException {
		ArrayList<ChargingEvent> chargingEvents = new ArrayList<ChargingEvent>();
		Scanner scanner = new Scanner(new File(fileName));
		scanner.nextLine();
	    while (scanner.hasNextLine()) {
	        String line = scanner.nextLine();
	        try (Scanner rowScanner = new Scanner(line)) {
	            rowScanner.useDelimiter(";");
	            int par1 = Integer.parseInt(rowScanner.next());
	            int par2 = Integer.parseInt(rowScanner.next());
	            int par3 = Integer.parseInt(rowScanner.next());
	            double par4 = Double.parseDouble(rowScanner.next());  
	            
	            chargingEvents.add(new ChargingEvent(par1, par2, par3, par4));
	        }
	    }
	    return chargingEvents;
	}
	

}
