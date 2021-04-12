import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class ServiceTrip {
	
	private int id;
	private final int linka;
	private final int spoj;
	private final int start;
	private final int koniec;
	private final int trvanie;
	private final double dlzka;
	private final double spotreba;
	private final int stanicaZaciatok;
	private final int stanicaKoniec;

	private final int matrixIndex;

	
	public ServiceTrip(int id, int linka, int spoj, int start, int koniec, int trvanie, double dlzka, double spotreba,
					   int stanicaZaciatok, int stanicaKoniec, int matrixIndex) {
		this.id = id;
		this.linka = linka;
		this.spoj = spoj;
		this.start = start;
		this.koniec = koniec;
		this.trvanie = trvanie;
		this.dlzka = dlzka;
		this.spotreba = spotreba;
		this.stanicaZaciatok = stanicaZaciatok;
		this.stanicaKoniec = stanicaKoniec;
		this.matrixIndex = matrixIndex;
	}

	public ServiceTrip(ServiceTrip trip) {
		this.id = trip.id;
		this.linka = trip.linka;
		this.spoj = trip.spoj;
		this.start = trip.start;
		this.koniec = trip.koniec;
		this.trvanie = trip.trvanie;
		this.dlzka = trip.dlzka;
		this.spotreba = trip.spotreba;
		this.stanicaZaciatok = trip.stanicaZaciatok;
		this.stanicaKoniec = trip.stanicaKoniec;
		matrixIndex = trip.matrixIndex;
	}

	public int getId() {
		return id;
	}

	public int getLinka() {
		return linka;
	}

	public int getSpoj() {
		return spoj;
	}

	public int getStart() {
		return start;
	}

	public int getKoniec() {
		return koniec;
	}

	public int getTrvanie() {
		return trvanie;
	}

	public double getDlzka() {
		return dlzka;
	}

	public double getSpotreba() {
		return spotreba;
	}

	public int getStanicaZaciatok() {
		return stanicaZaciatok;
	}

	public int getStanicaKoniec() {
		return stanicaKoniec;
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
