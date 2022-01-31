import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class ServiceTrip {
	
	private final int id;
	private final int line;
	private final int trip;
	private final int start;
	private final int end;
	private final int duration;
	private final double distance;
	private final double consumption;
	private final int startStation;
	private final int endStation;

	private final int matrixIndex;

	
	public ServiceTrip(int id, int line, int trip, int start, int end, int duration, double distance, double consumption,
					   int startStation, int endStation, int matrixIndex) {
		this.id = id;
		this.line = line;
		this.trip = trip;
		this.start = start;
		this.end = end;
		this.duration = duration;
		this.distance = distance;
		this.consumption = consumption;
		this.startStation = startStation;
		this.endStation = endStation;
		this.matrixIndex = matrixIndex;
	}

	public ServiceTrip(ServiceTrip trip) {
		this.id = trip.id;
		this.line = trip.line;
		this.trip = trip.trip;
		this.start = trip.start;
		this.end = trip.end;
		this.duration = trip.duration;
		this.distance = trip.distance;
		this.consumption = trip.consumption;
		this.startStation = trip.startStation;
		this.endStation = trip.endStation;
		matrixIndex = trip.matrixIndex;
	}

	public int getId() {
		return id;
	}

	public int getLine() {
		return line;
	}

	public int getTrip() {
		return trip;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public int getDuration() {
		return duration;
	}

	public double getDistance() {
		return distance;
	}

	public double getConsumption() {
		return consumption;
	}

	public int getStartStation() {
		return startStation;
	}

	public int getEndStation() {
		return endStation;
	}

	public int getMatrixIndex() {return matrixIndex; }
	
	public static ArrayList<ServiceTrip> loadServiceTripsFromFile(String fileName) throws FileNotFoundException {
		ArrayList<ServiceTrip> serviceTrips = new ArrayList<>();
		Scanner scanner = new Scanner(new File(fileName));
		scanner.nextLine();
	    while (scanner.hasNextLine()) {
	        String line = scanner.nextLine();
	        try (Scanner rowScanner = new Scanner(line)) {
	            rowScanner.useDelimiter(";");
	            int par1 = Integer.parseInt(rowScanner.next());
	            int par2 = Integer.parseInt(rowScanner.next());
	            int par3 = Integer.parseInt(rowScanner.next());
	            int par4 = Integer.parseInt(rowScanner.next());
	            int par5 = Integer.parseInt(rowScanner.next());
	            int par6 = Integer.parseInt(rowScanner.next());
	            double par7 = Double.parseDouble(rowScanner.next());  
	            double par8 = Double.parseDouble(rowScanner.next());  
	            int par9 = Integer.parseInt(rowScanner.next());
	            int par10 = Integer.parseInt(rowScanner.next());
	            
	            serviceTrips.add(new ServiceTrip(par1, par2, par3, par4, par5,
	            								 par6, par7, par8, par9, par10, serviceTrips.size()));
	        }
	    }
	    scanner.close();
	    return serviceTrips;
	}
}
