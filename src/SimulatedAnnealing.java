import com.sun.jdi.event.VMDeathEvent;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import javax.swing.*;

public class SimulatedAnnealing {

	private ArrayList<ChargingEvent> chargingEvents;
	private ArrayList<ServiceTrip> serviceTrips;
	private String dataSet;
	private Solution solution;
	private Solution bestFoundSolution;
	private EdgeSTtoST[][] matrixSTtoST;
	private EdgeSTtoCE[][] matrixSTtoCE;
	private EdgeCEtoST[][] matrixCEtoST;
	private Random random;

	//starting temperature
	double maxT;
	//temperature cooling schedule
	double tBeta;
	//max iterations at given temperature
	int maxQ;

	public SimulatedAnnealing(String dataSet, double maxT, double tBeta, int maxQ) throws FileNotFoundException {
		this.dataSet = dataSet;
		chargingEvents = ChargingEvent.createChargingEvents("./src/Datasets/"+dataSet+"_chargEvents.csv");

		serviceTrips = ServiceTrip.loadServiceTripsFromFile("./src/Datasets/"+dataSet+"_Trips.csv");

		solution = new Solution();
		bestFoundSolution = new Solution(solution);

		this.random = new Random();

		for (int tripIndex = 1; tripIndex < serviceTrips.size()-1; tripIndex++) {
			Vehicle vehicle = new Vehicle(serviceTrips.get(0), serviceTrips.get(serviceTrips.size()-1));
			vehicle.addServiceTrip(serviceTrips.get(tripIndex));
			solution.addVehicle(vehicle);
		}


		// ServiceTrip to ServiceTrip matrix
		Scanner scannerTij = new Scanner(new File("./src/Datasets/"+dataSet+"_MatrixTij.csv"));
		Scanner scannerCij = new Scanner(new File("./src/Datasets/"+dataSet+"_MatrixCij.csv"));
		String lineTij = scannerTij.nextLine();
		String lineCij = scannerCij.nextLine();
		int matrixNumberOfLines = Integer.parseInt(lineTij);
		lineTij = scannerTij.nextLine();
		lineCij = scannerCij.nextLine();
		int matrixNumberOfRows = Integer.parseInt(lineTij);

		matrixSTtoST = new EdgeSTtoST[matrixNumberOfLines][matrixNumberOfRows];

		for (int i = 0; i < matrixNumberOfLines; i++) {
			lineTij = scannerTij.nextLine();
			lineCij = scannerCij.nextLine();
			Scanner rowScannerTij = new Scanner(lineTij);
			Scanner rowScannerCij = new Scanner(lineCij);
			rowScannerTij.useDelimiter(";");
			rowScannerCij.useDelimiter(";");
			for (int j = 0; j < matrixNumberOfRows; j++) {
				int timeDistance = Integer.parseInt(rowScannerTij.next());
				double batteryConsumption = Double.parseDouble( rowScannerCij.next());
				matrixSTtoST[i][j] = new EdgeSTtoST(timeDistance, batteryConsumption,
						timeDistance <= serviceTrips.get(j).getStanicaZaciatok() - serviceTrips.get(i).getKoniec());
			}
			rowScannerTij.close();
			rowScannerCij.close();
		}
		scannerTij.close();
		scannerCij.close();

		// ServiceTrip to ChargingEvent matrix
		Scanner scannerTir = new Scanner(new File("./src/Datasets/"+dataSet+"_MatrixTir.csv"));
		Scanner scannerCir = new Scanner(new File("./src/Datasets/"+dataSet+"_MatrixCir.csv"));
		String lineTir = scannerTir.nextLine();
		String lineCir = scannerCir.nextLine();
		matrixNumberOfLines = Integer.parseInt(lineTir);
		lineTir = scannerTir.nextLine();
		lineCir = scannerCir.nextLine();
		matrixNumberOfRows = Integer.parseInt(lineTir);

		matrixSTtoCE = new EdgeSTtoCE[matrixNumberOfLines][matrixNumberOfRows];

		for (int i = 0; i < matrixNumberOfLines; i++) {
			lineTir = scannerTir.nextLine();
			lineCir = scannerCir.nextLine();
			Scanner rowScannerTir = new Scanner(lineTir);
			Scanner rowScannerCir = new Scanner(lineCir);
			rowScannerTir.useDelimiter(";");
			rowScannerCir.useDelimiter(";");
			for (int j = 0; j < matrixNumberOfRows; j++) {
				int timeDistance = Integer.parseInt(rowScannerTir.next());
				double batteryConsumption = Double.parseDouble( rowScannerCir.next());
				matrixSTtoCE[i][j] = new EdgeSTtoCE(timeDistance, batteryConsumption);
			}
			rowScannerTir.close();
			rowScannerCir.close();
		}
		scannerTir.close();
		scannerCir.close();

		// ServiceTrip to ChargingEvent matrix
		Scanner scannerTrj = new Scanner(new File("./src/Datasets/"+dataSet+"_MatrixTrj.csv"));
		Scanner scannerCrj = new Scanner(new File("./src/Datasets/"+dataSet+"_MatrixCrj.csv"));
		String lineTrj = scannerTrj.nextLine();
		String lineCrj = scannerCrj.nextLine();
		matrixNumberOfLines = Integer.parseInt(lineTrj);
		lineTrj = scannerTrj.nextLine();
		lineCir = scannerCrj.nextLine();
		matrixNumberOfRows = Integer.parseInt(lineTrj);

		matrixCEtoST = new EdgeCEtoST[matrixNumberOfLines][matrixNumberOfRows];

		for (int i = 0; i < matrixNumberOfLines; i++) {
			lineTrj = scannerTrj.nextLine();
			lineCrj = scannerCrj.nextLine();
			Scanner rowScannerTrj = new Scanner(lineTrj);
			Scanner rowScannerCrj = new Scanner(lineCrj);
			rowScannerTrj.useDelimiter(";");
			rowScannerCrj.useDelimiter(";");
			for (int j = 0; j < matrixNumberOfRows; j++) {
				int timeDistance = Integer.parseInt(rowScannerTrj.next());
				double batteryConsumption = Double.parseDouble( rowScannerCrj.next());
				matrixCEtoST[i][j] = new EdgeCEtoST( timeDistance, batteryConsumption);
			}
			rowScannerTrj.close();
			rowScannerCrj.close();
		}
		scannerTrj.close();
		scannerCrj.close();


		System.out.println("Initial solution: \n");
		System.out.println(solution);

		this.maxT = maxT;
		this.tBeta = tBeta;
		this.maxQ = maxQ;
	}

	public void runSimulatedAnnealing() {
		Solution nextSolution = new Solution(solution);



		for (double t = maxT; t > 0.01; t /= ( 1 + t*tBeta)) {
			System.out.println("current temp: " + t + "\n");
			for (int q = 0; q < maxQ; q++) {
				findNext(nextSolution);
			}
		}

		int objectiveDelta = 0;

		int current_iteration = 0;
	}

	public void findNext(Solution nextSolution) {
		// randomly choose vehicle from solution
		int randomIndex = random.nextInt(nextSolution.getVehicles().size());
		Vehicle randomVehicle = nextSolution.getVehicles().remove(randomIndex);

		//remove vehicle's CEs
		for (ChargingEvent event : randomVehicle.getChargingEvents()) {
			event.setTaken(false);
		}
		randomVehicle.getChargingEvents().clear();

		ArrayList<ServiceTrip> removedTrips = randomVehicle.getServiceTrips();
		// strip removedTrips of begin and end depos
		removedTrips.remove(removedTrips.size()-1);
		removedTrips.remove(0);

		// go over other Vehicles in Solution, try to assign any ST to another Vehicle
		int indexVehicle = 0;
		while (indexVehicle < nextSolution.getVehicles().size() && removedTrips.size() > 0 ) {
			Vehicle originalVehicle = nextSolution.getVehicles().get(indexVehicle);
			// backup of current state of removedTrips
			ArrayList<ServiceTrip> removedCurrStateBackup = new ArrayList<>(removedTrips);

			// create a shallow copy of vehicles's CEs and STs in case we need to use original state of vehicle
			Vehicle vehicle = new Vehicle(originalVehicle);
			// clear CEs
			for (ChargingEvent event : vehicle.getChargingEvents()) {
				event.setTaken(false);
			}
			vehicle.getChargingEvents().clear();

			// try to find a time window between already existing trips in vehicle,
			// we don't take capacity deficiency into account at first, only after ST can be inserted timewise
			ArrayList<ServiceTrip> vehicleTrips = vehicle.getServiceTrips();

			int indexRemovedTrips = 0;
			int indexVehicleTrips = 0;
			boolean isNewAdded = false;

			// trips' lists begins and ends with depo
			while (indexVehicleTrips < vehicleTrips.size() - 1
					&& indexRemovedTrips < removedTrips.size()) {

				ServiceTrip currentTrip = vehicleTrips.get(indexVehicleTrips);
				ServiceTrip nextTrip = vehicleTrips.get(indexVehicleTrips+1);
				// find possible candidate from removedTrips to insert after ST at current index
				while ( indexRemovedTrips < removedTrips.size()
						&& currentTrip.getKoniec() >= removedTrips.get(indexRemovedTrips).getStart()) {
					indexRemovedTrips++;
				}

				// check if indexRemovedTrips is within removedTrips size
				if (indexRemovedTrips < removedTrips.size()) {
					ServiceTrip removedTrip = removedTrips.get(indexRemovedTrips);
					// check if we can insert removedTrips[indexRemovedTrips] between current and next trip timewise
					// if start of removed is greater or equal than end of current + time from ST to ST
					// && if start of next is greater or equal than end of removed + time from ST to ST
					if (removedTrip.getStart() >= currentTrip.getKoniec() + matrixSTtoST[currentTrip.getMatrixIndex()][removedTrip.getMatrixIndex()].getTimeDistance()
							&& nextTrip.getStart() >= removedTrip.getKoniec() + matrixSTtoST[removedTrip.getMatrixIndex()][nextTrip.getMatrixIndex()].getTimeDistance()) {
						// removedTrip is added between current and next, battery deficiency has to be checked after
						// adding all removedTrips to current vehicle that fit timewise, remove removedTrip from removedTrips
						removedTrips.remove(removedTrip);
						vehicleTrips.add(++indexVehicleTrips, removedTrip);
						if (!isNewAdded) {
							isNewAdded = true;
						}
					}
				}

				indexVehicleTrips++;
			}

			// check if any of removedTrips were added to current vehicle
			if (isNewAdded) {
				// generate battery deficiency at problematic STs

				boolean isAcceptable = false;
				double currentBatteryState = Vehicle.maxBatteryCapacity;

				/*double[]
				while (!isAcceptable) {


				}*/

			}

			indexVehicle++;
		}
	}
}
