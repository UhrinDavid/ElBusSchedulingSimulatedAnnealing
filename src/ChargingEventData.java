import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Scanner;

public class ChargingEventData {
	private final int indexCharger;
	private final int indexChargingEvent;
	private final int start;
	private final int end;
	private final double chargingSpeed;

	public ChargingEventData(int indexCharger, int indexChargingEvent, int start, int end, double chargingSpeed) {
		this.indexCharger = indexCharger;
		this.indexChargingEvent = indexChargingEvent;
		this.start = start;
		this.end = end;
		this.chargingSpeed = chargingSpeed;
	}

	public ChargingEventData(ChargingEventData event) {
		this.indexCharger = event.indexCharger;
		this.indexChargingEvent = event.indexChargingEvent;
		this.start = event.start;
		this.end = event.end;
		this.chargingSpeed = event.chargingSpeed;
	}
	
	public int getIndexCharger() {
		return indexCharger;
	}

	public int getIndexChargingEvent() {
		return indexChargingEvent;
	}

	public int getStart() {
		return start;
	}

	public double getChargingSpeed() {
		return chargingSpeed;
	}

	public int getEnd() {
		return end;
	}

	public static LinkedList<ChargingEventData> createChargingEvents(String fileName) throws FileNotFoundException {
		LinkedList<ChargingEventData> chargingEventData = new LinkedList<>();
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
	            
	            chargingEventData.add(new ChargingEventData(par1, par2, par3, par4, par5));
	        }
	    }
	    scanner.close();
	    return chargingEventData;
	}
}
