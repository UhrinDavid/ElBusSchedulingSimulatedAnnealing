import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class ChargingEvent {
	private final int indexCharger;
	private final int indexChargingEvent;
	private final int startTime;
	private final double chargingSpeed;

	private boolean isReserved = false;

	private final int matrixIndex;

	public ChargingEvent(int indexCharger, int indexChargingEvent, int startTime, double chargingSpeed, int matrixIndex) {
		this.indexCharger = indexCharger;
		this.indexChargingEvent = indexChargingEvent;
		this.startTime = startTime;
		this.chargingSpeed = chargingSpeed;
		this.matrixIndex = matrixIndex;
	}

	public ChargingEvent(ChargingEvent event) {
		this.indexCharger = event.indexCharger;
		this.indexChargingEvent = event.indexChargingEvent;
		this.startTime = event.startTime;
		this.chargingSpeed = event.chargingSpeed;
		this.isReserved = event.isReserved;
		this.matrixIndex = event.matrixIndex;
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

	public int getMatrixIndex() { return  matrixIndex; }

	public static ArrayList<ChargingEvent> createChargingEvents(String fileName) throws FileNotFoundException {
		ArrayList<ChargingEvent> chargingEvents = new ArrayList<>();
		Scanner scanner = new Scanner(new File(fileName));
		scanner.nextLine();
	    while (scanner.hasNextLine()) {
	        String line = scanner.nextLine();
	        try (Scanner rowScanner = new Scanner(line)) {
	            rowScanner.useDelimiter(";");
	            int par1 = Integer.parseInt(rowScanner.next().replace("\"", ""));
	            int par2 = Integer.parseInt(rowScanner.next().replace("\"", ""));
	            int par3 = Integer.parseInt(rowScanner.next().replace("\"", ""));
				rowScanner.next();
				rowScanner.next();
				rowScanner.next();
	            double par4 = Double.parseDouble(rowScanner.next().replace("\"", ""));
	            
	            chargingEvents.add(new ChargingEvent(par1, par2, par3, par4, chargingEvents.size()));
	        }
	    }
	    scanner.close();
	    return chargingEvents;
	}
}
