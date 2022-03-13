import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

public class ServiceTripData {
	private final int index;
	private final int id;
	private final int start;
	private final int end;
	private final double distance;
	private final double consumption;

	public ServiceTripData(int index, int id, int start, int end, double distance, double consumption) {
		this.index = index;
		this.id = id;
		this.start = start;
		this.end = end;
		this.distance = distance;
		this.consumption = consumption;
	}

	public int getId() {
		return id;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public double getDistance() {
		return distance;
	}

	public double getConsumption() {
		return consumption;
	}

	public int getIndex() {return index; }
	
	public static LinkedList<ServiceTripData> loadServiceTripsFromFile(String fileName) throws FileNotFoundException {
		LinkedList<ServiceTripData> serviceTripData = new LinkedList<>();
		Scanner scanner = new Scanner(new File(fileName));
		scanner.nextLine();
	    while (scanner.hasNextLine()) {
	        String line = scanner.nextLine();
	        try (Scanner rowScanner = new Scanner(line)) {
	            rowScanner.useDelimiter(";");
	            int par1 = Integer.parseInt(rowScanner.next().replace("\"", ""));
	            int par2 = Integer.parseInt(rowScanner.next().replace("\"", ""));
				rowScanner.next();
				rowScanner.next();
				rowScanner.next();
				rowScanner.next();
				int par3 = Integer.parseInt(rowScanner.next().replace("\"", ""));
				int par4 = Integer.parseInt(rowScanner.next().replace("\"", ""));
	            double par5 = Double.parseDouble(rowScanner.next().replace("\"", ""));
	            double par6 = Double.parseDouble(rowScanner.next().replace("\"", ""));
	            
	            serviceTripData.add(new ServiceTripData(par1, par2, par3, par4, par5,
	            								 par6));
	        }
	    }
	    scanner.close();
	    return serviceTripData;
	}
}
