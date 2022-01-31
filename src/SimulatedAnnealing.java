import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class SimulatedAnnealing {

	private ArrayList<ChargingEvent> chargingEvents;
	private ArrayList<ServiceTrip> serviceTrips;
	private String dataSet;
	private Solution solution;
	private Edge[][] matrixSTtoST;
	private Edge[][] matrixSTtoCE;
	private Edge[][] matrixCEtoST;
	private Random random;

	//starting temperature
	private double maxT;
	//temperature modifier
	private double tBeta;
	//max iterations at given temperature
	private int maxQ;

	public SimulatedAnnealing(String dataSet, double maxT, double tBeta, int maxQ) throws FileNotFoundException {
		this.dataSet = dataSet;
		chargingEvents = ChargingEvent.createChargingEvents("./src/Datasets/"+dataSet+"_chargEvents.csv");
		serviceTrips = ServiceTrip.loadServiceTripsFromFile("./src/Datasets/"+dataSet+"_Trips.csv");

		solution = new Solution();

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

		matrixSTtoST = new Edge[matrixNumberOfLines][matrixNumberOfRows];

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
				matrixSTtoST[i][j] = new Edge(timeDistance, batteryConsumption);
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

		matrixSTtoCE = new Edge[matrixNumberOfLines][matrixNumberOfRows];

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
				matrixSTtoCE[i][j] = new Edge(timeDistance, batteryConsumption);
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

		matrixCEtoST = new Edge[matrixNumberOfLines][matrixNumberOfRows];

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
				matrixCEtoST[i][j] = new Edge( timeDistance, batteryConsumption);
			}
			rowScannerTrj.close();
			rowScannerCrj.close();
		}
		scannerTrj.close();
		scannerCrj.close();

		this.maxT = maxT;
		this.tBeta = tBeta;
		this.maxQ = maxQ;
	}

	public void runSimulatedAnnealing() {
		Solution solutionCurrentTemperature = solution;
		boolean isFoundBetterSolution;
		double currentTemperature = maxT;
		do {
			isFoundBetterSolution = false;
			for (int q = 0; q < maxQ; q++) {
				Solution nextSolution = findNext(solutionCurrentTemperature);
				System.out.println("new solution: " + nextSolution);
				if (nextSolution == null) {
					return;
				}
				if (nextSolution.getVehicles().size() <= solutionCurrentTemperature.getVehicles().size()) {
					System.out.println("updated solution");
					solutionCurrentTemperature = nextSolution;
					solution = nextSolution;
					if (!isFoundBetterSolution) {
						isFoundBetterSolution = true;
					}
				} else {
					double pAcceptNext = Math.exp(
							-((nextSolution.getVehicles().size() - solutionCurrentTemperature.getVehicles().size())
							/ currentTemperature)
					);
					double generatedValue = random.nextDouble();
					if (generatedValue <= pAcceptNext) {
						solutionCurrentTemperature = nextSolution;
					}
				}
			}
			currentTemperature /= 1 + tBeta * currentTemperature;
			System.out.println("running...\n");
		} while(isFoundBetterSolution);
	}

	public Solution findNext(Solution solutionPa) {
		if (solutionPa.getVehicles().size() == 1) {
			return null;
		}
		Solution nextSolution = new Solution(solutionPa);
		// randomly choose vehicle from solution
		int randomIndex = random.nextInt(nextSolution.getVehicles().size());
		Vehicle randomVehicle = nextSolution.getVehicles().remove(randomIndex);

		// release vehicle's CEs
		/*for (ChargingEvent event : randomVehicle.getChargingEvents()) {
			event.setTaken(false);
		}
		randomVehicle.getChargingEvents().clear();*/

		ArrayList<ServiceTrip> removedTrips = randomVehicle.getServiceTrips();
		// strip removedTrips of begin and end depos - only serviceTrips that need to be served remain
		ServiceTrip removedStart = removedTrips.remove(removedTrips.size()-1);
		ServiceTrip removedEnd = removedTrips.remove(0);

		// go over other Vehicles in Solution, try to assign any ST to another Vehicle
		int indexVehicle = 0;
		while (indexVehicle < nextSolution.getVehicles().size() && removedTrips.size() > 0 ) {
			// vehicle to which we try to assign service trips from removedTrips
			// we keep this instance as a backup of vehicle's STs and CEs in case we need to revert iteration
			Vehicle originalVehicle = nextSolution.getVehicles().get(indexVehicle);
			// backup of current state of removedTrips from vehicle we try to eliminate from the solution - in case we need to revert iteration
			ArrayList<ServiceTrip> removedCurrStateBackup = new ArrayList<>(removedTrips);

			// create a shallow copy of vehicle with it's CEs and STs in case we need to use original state of vehicle
			Vehicle vehicle = new Vehicle(originalVehicle);

			// try to find a time window between already existing trips in vehicle,
			// we don't take capacity deficiency into account at first, only if ST can be inserted respecting time restrictions
			ArrayList<ServiceTrip> vehicleTrips = vehicle.getServiceTrips();

			int indexRemovedTrips = 0;
			int indexVehicleTrips = 0;
			// flag is true if we add at least one new ST to the current vehicle
			boolean isNewAdded = false;

			// trips' lists begins and ends with depo, we can insert between depo and first
			// but no after depo (therefore size - 1)
			while (indexVehicleTrips < vehicleTrips.size() - 1
					&& indexRemovedTrips < removedTrips.size()) {

				ServiceTrip currentTrip = vehicleTrips.get(indexVehicleTrips);
				ServiceTrip nextTrip = vehicleTrips.get(indexVehicleTrips+1);
				// find possible candidate from removedTrips to insert after ST at current index
				// try to find index in removedTrips, whose corresponding CE starts after the end of current trip
				while ( indexRemovedTrips < removedTrips.size()
						&& currentTrip.getEnd() >= removedTrips.get(indexRemovedTrips).getStart()) {
					indexRemovedTrips++;
				}

				// check if indexRemovedTrips is within removedTrips size, if not we don't have a candidate
				if (indexRemovedTrips < removedTrips.size()) {
					ServiceTrip removedTrip = removedTrips.get(indexRemovedTrips);
					// check if we can insert removedTrips[indexRemovedTrips] between current and next trip respecting time restrictions
					// if start of removed is greater or equal than end of current + time from ST to ST
					// && if start of next is greater or equal than end of removed + time from ST to ST
					if (removedTrip.getStart() >= currentTrip.getEnd() + matrixSTtoST[currentTrip.getMatrixIndex()][removedTrip.getMatrixIndex()].getTimeDistance()
							&& nextTrip.getStart() >= removedTrip.getEnd() + matrixSTtoST[removedTrip.getMatrixIndex()][nextTrip.getMatrixIndex()].getTimeDistance()) {
						// removedTrip is added between current and next, battery deficiency has to be checked after
						// adding all removedTrips to current vehicle that fit timewise, remove removedTrip from removedTrips
						System.out.println("adding trip");
						removedTrips.remove(removedTrip);
						System.out.println("removed " + removedTrip.toString());
						vehicleTrips.add(++indexVehicleTrips, removedTrip);
						System.out.println("trips " + vehicleTrips);
						if (!isNewAdded) {
							isNewAdded = true;
						}
						continue;
					}
				}
				indexVehicleTrips++;
			}

			// check if any of removedTrips were added to current vehicle
			/*if (isNewAdded) {
				// release CEs of vehicle where we try to add removed ST's
				for (ChargingEvent event : vehicle.getChargingEvents()) {
					event.setTaken(false);
				}
				vehicle.getChargingEvents().clear();

				// check if there is enough battery capacity, if not, try to insert charging events, finally
				boolean isAcceptable = false;
				while (!isAcceptable) {
					double currentBatteryState = Vehicle.maxBatteryCapacity;
					int serviceTripIndex = 0;
					int chargingEventIndex = 0;
					while (serviceTripIndex < vehicleTrips.size()) {

					}
				}

			}*/
			nextSolution.getVehicles().set(indexVehicle, vehicle);
			indexVehicle++;
		}
		if (randomVehicle.getServiceTrips().size() > 0) {
			removedTrips.add(removedEnd);
			removedTrips.add(0, removedStart);
			nextSolution.getVehicles().add(randomVehicle);
		}
		return nextSolution;
	}

	public String toString() {
		return "solution: \n" + solution + "solution length: \n" + solution.getVehicles().size() +"\n";
	}
}
