import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Scanner;

public class ChargingEvent {
	private final int indexCharger;
	private final int indexChargingEvent;
	private final int startTime;
	private final int endTime;
	private final double chargingSpeed;

	private boolean isReserved = false;

	public ChargingEvent(int indexCharger, int indexChargingEvent, int startTime, int endTime, double chargingSpeed) {
		this.indexCharger = indexCharger;
		this.indexChargingEvent = indexChargingEvent;
		this.startTime = startTime;
		this.endTime = endTime;
		this.chargingSpeed = chargingSpeed;
	}

	public ChargingEvent(ChargingEvent event) {
		this.indexCharger = event.indexCharger;
		this.indexChargingEvent = event.indexChargingEvent;
		this.startTime = event.startTime;
		this.endTime = event.endTime;
		this.chargingSpeed = event.chargingSpeed;
		this.isReserved = event.isReserved;
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

	public boolean getIsReserved() {
		return isReserved;
	}

	public void setIsReserved(boolean isReserved) {
		this.isReserved = isReserved;
	}

	public int getEndTime() {
		return endTime;
	}

	public static LinkedList<ChargingEvent> createChargingEvents(String fileName) throws FileNotFoundException {
		LinkedList<ChargingEvent> chargingEvents = new LinkedList<>();
		Scanner scanner = new Scanner(new File(fileName));
		scanner.nextLine();
	    while (scanner.hasNextLine()) {
	        String line = scanner.nextLine();
	        try (Scanner rowScanner = new Scanner(line)) {
	            rowScanner.useDelimiter(";");
	            int par1 = Integer.parseInt(rowScanner.next().replace("\"", ""));
	            int par2 = Integer.parseInt(rowScanner.next().replace("\"", ""));
	            int par3 = Integer.parseInt(rowScanner.next().replace("\"", ""));
				int par4 = Integer.parseInt(rowScanner.next().replace("\"", ""));
				rowScanner.next();
				rowScanner.next();
	            double par5 = Double.parseDouble(rowScanner.next().replace("\"", ""));
	            
	            chargingEvents.add(new ChargingEvent(par1, par2, par3, par4, par5));
	        }
	    }
	    scanner.close();
	    return chargingEvents;
	}
}
