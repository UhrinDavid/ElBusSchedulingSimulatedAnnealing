import com.sun.jdi.event.VMDeathEvent;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Application {



	public static void main(String[] args) throws FileNotFoundException {
		Application app = new Application("Pom");
		app.runSimulatedAnnealing();
	}

	private ArrayList<ChargingEvent> chargingEvents;
	private ArrayList<ServiceTrip> serviceTrips;
	private String dataSet;
	private Solution solution;
	private Solution bestFoundSolution;
	private EdgeSTtoST[][] matrixSTtoST;
	private EdgeSTtoCE[][] matrixSTtoCE;
	private EdgeCEtoST[][] matrixCEtoST;
	private Random random;


	public Application(String dataSet) throws FileNotFoundException {
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
	}

	private void runSimulatedAnnealing() {
		Solution nextSolution = new Solution(solution);

		//starting temperature
		double maxT = 1000;
		//temperature cooling schedule
		double tBeta = 1000;
		//max iterations at given temperature
		int maxQ = 1000;

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
		int randomIndex = random.nextInt(nextSolution.getVehicles().size());
		Vehicle randomVehicle = nextSolution.getVehicles().get(randomIndex);

		ServiceTrip removedTrip = removeTripForNextSol(randomVehicle);
		if (removedTrip != null) {
			boolean wasAdded = false;
			int vehicleIndex = 0;
			while (!wasAdded && vehicleIndex < nextSolution.getVehicles().size()) {
				if (randomIndex != vehicleIndex) {
					Vehicle vehicle = nextSolution.getVehicles().get(vehicleIndex);
					double batteryState = Vehicle.maxBatteryCapacity;
					int indexTrip = 1;
					int insertIndex = -1;
					boolean bustedCapacity = false;
					boolean iterateTrips = true;
					while (iterateTrips && indexTrip < vehicle.getServiceTrips().size()) {
						ServiceTrip trip = vehicle.getServiceTrips().get(indexTrip);
						int indexPrev = serviceTrips.indexOf(vehicle.getServiceTrips().get(indexTrip- 1));
						int indexNext = serviceTrips.indexOf(vehicle.getServiceTrips().get(indexTrip));

						if (removedTrip.getKoniec() < trip.getStart()) {
							if (trip.getStart() - removedTrip.getKoniec()
									- matrixSTtoST[indexPrev][indexNext].getTimeDistance() >= 0) {
								if (batteryState - matrixSTtoST[indexPrev][indexNext].getBatteryConsumption()
									- trip.getSpotreba() < Vehicle.minBatteryCapacity) {
									for (ChargingEvent event : chargingEvents) {

									}
								}
							} else {
								iterateTrips = false;
								continue;
							}
						}

						ArrayList<ChargingEvent> proceedingCEs = trip.getFollowingChargingEvents();
						//resolves charging before ST
						if (proceedingCEs.size() > 0) {
							for (int j = 0; j < proceedingCEs.size(); j++) {
								ChargingEvent event = proceedingCEs.get(j);
								int batteryIndex = chargingEvents.indexOf(event);
								if (j == 0) {
									batteryState -= matrixSTtoCE[indexPrev][batteryIndex].getBatteryConsumption();
									if (batteryState < Vehicle.minBatteryCapacity) {
										bustedCapacity = true;
									}
								}
								if (j != proceedingCEs.size()-1) {
									batteryState += event.getChargingSpeed()
											* (chargingEvents.get(batteryIndex+1).getStartTime() - event.getStartTime());
								} else {
									batteryState += event.getChargingSpeed()
											* (trip.getStart()
											- matrixCEtoST[batteryIndex][indexNext].getTimeDistance()
											- event.getStartTime());
									batteryState -= matrixCEtoST[batteryIndex][indexNext].getBatteryConsumption();
								}
							}
						} else {
							batteryState -= matrixSTtoST[indexPrev][indexNext].getBatteryConsumption();
						}
						batteryState -= trip.getSpotreba();
						if (batteryState < Vehicle.minBatteryCapacity) {
							bustedCapacity = true;
						}
					}
				}
			}
		}
	}

	public ServiceTrip removeTripForNextSol (Vehicle vehicle) {
		int randomIndexST = random.nextInt(vehicle.getServiceTrips().size()-2)+1;
		ArrayList<ChargingEvent> removedEvents =  serviceTrips.get(randomIndexST-1).getFollowingChargingEvents();
		for (ChargingEvent event : removedEvents) {
			event.setTaken(false);
		}
		removedEvents.clear();
		ServiceTrip removed = serviceTrips.remove(randomIndexST);
		removedEvents = removed.getFollowingChargingEvents();
		for (ChargingEvent event : removedEvents) {
			event.setTaken(false);
		}
		removedEvents.clear();
 		if (!vehicle.hasServiceTrips()) {
			return	removed;
		}
		// check if vehicle can still make trips without removed trip
		double batteryState = Vehicle.maxBatteryCapacity;
		for (int i = 1; i <  vehicle.getServiceTrips().size(); i++) {
			// energy consumption to start of ST
			int indexPrev = serviceTrips.indexOf(vehicle.getServiceTrips().get(i - 1));
			int indexNext = serviceTrips.indexOf(vehicle.getServiceTrips().get(i));
			ArrayList<ChargingEvent> proceedingCEs = vehicle.getServiceTrips().get(i - 1).getFollowingChargingEvents();
			//resolves charging before ST
			if (proceedingCEs.size() > 0) {
				for (int j = 0; j < proceedingCEs.size(); j++) {
					ChargingEvent event = proceedingCEs.get(j);
					int batteryIndex = chargingEvents.indexOf(event);
					if (j == 0) {
						batteryState -= matrixSTtoCE[indexPrev][batteryIndex].getBatteryConsumption();
						if (batteryState < Vehicle.minBatteryCapacity) {
							return null;
						}
					}
					if (j != proceedingCEs.size()-1) {
						batteryState += event.getChargingSpeed()
								* (chargingEvents.get(batteryIndex+1).getStartTime() - event.getStartTime());
					} else {
						batteryState += event.getChargingSpeed()
								* (vehicle.getServiceTrips().get(i).getStart()
								- matrixCEtoST[batteryIndex][indexNext].getTimeDistance()
								- event.getStartTime());
						batteryState -= matrixCEtoST[batteryIndex][indexNext].getBatteryConsumption();
						if (batteryState < Vehicle.minBatteryCapacity) {
							return null;
						}
					}
				}
			} else {
				batteryState -= matrixSTtoST[indexPrev][indexNext].getBatteryConsumption();
			}
			batteryState -= vehicle.getServiceTrips().get(i).getSpotreba();
			if (batteryState < Vehicle.minBatteryCapacity) {
				return null;
			}
		}
		return removed;
	}
}
