import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Solution {
    private LinkedList<STsGroup> sTsGroups;
    private LinkedList<TreeMap<Integer, ChargingEventVertex>> chargersWithChargingEvents;
    private Random random;

    public Solution(String fileTrips, String fileChargers,
                    String fileEnergySTToST, String fileEnergySTToCE, String fileEnergyCEToST, String fileTimeSTToST,
                    String fileTimeSTToCE, String fileTimeCEToST) throws FileNotFoundException {
        sTsGroups = new LinkedList<>();
        this.random = new Random();
        LinkedList<ServiceTripData> serviceTripData;
        LinkedList<ChargingEventData> chargingEventData = ChargingEventData.createChargingEvents(fileChargers);
        serviceTripData = ServiceTripData.loadServiceTripsFromFile(fileTrips);

        // ServiceTripData to ServiceTripData matrix
        Scanner scannerTij = new Scanner(new File(fileTimeSTToST));
        Scanner scannerCij = new Scanner(new File(fileEnergySTToST));
        String lineTij = scannerTij.nextLine();
        String lineCij = scannerCij.nextLine();
        int matrixNumberOfLines = Integer.parseInt(lineTij);
        lineTij = scannerTij.nextLine();
        lineCij = scannerCij.nextLine();
        int matrixNumberOfRows = Integer.parseInt(lineTij);

        Edge[][] matrixSTtoST = new Edge[matrixNumberOfLines][matrixNumberOfRows];

        for (int i = 0; i < matrixNumberOfLines; i++) {
            lineTij = scannerTij.nextLine();
            lineCij = scannerCij.nextLine();
            Scanner rowScannerTij = new Scanner(lineTij);
            Scanner rowScannerCij = new Scanner(lineCij);
            rowScannerTij.useDelimiter(";");
            rowScannerCij.useDelimiter(";");
            for (int j = 0; j < matrixNumberOfRows; j++) {
                int timeDistance = Integer.parseInt(rowScannerTij.next());
                double batteryConsumption = Double.parseDouble(rowScannerCij.next());
                // check if such edge is possible (timewise), if not insert null
                if (serviceTripData.get(i).getEnd() + timeDistance > serviceTripData.get(j).getStart()) {
                    matrixSTtoST[i][j] = null;
                } else {
                    matrixSTtoST[i][j] = new Edge(timeDistance, batteryConsumption);
                }
            }
            rowScannerTij.close();
            rowScannerCij.close();
        }
        scannerTij.close();
        scannerCij.close();

        // ServiceTripData to ChargingEventData matrix
        Scanner scannerTir = new Scanner(new File(fileTimeSTToCE));
        Scanner scannerCir = new Scanner(new File(fileEnergySTToCE));
        String lineTir = scannerTir.nextLine();
        String lineCir = scannerCir.nextLine();
        matrixNumberOfLines = Integer.parseInt(lineTir);
        lineTir = scannerTir.nextLine();
        lineCir = scannerCir.nextLine();
        matrixNumberOfRows = Integer.parseInt(lineTir);

        Edge[][] matrixSTtoCE = new Edge[matrixNumberOfLines][matrixNumberOfRows];

        for (int i = 0; i < matrixNumberOfLines; i++) {
            lineTir = scannerTir.nextLine();
            lineCir = scannerCir.nextLine();
            Scanner rowScannerTir = new Scanner(lineTir);
            Scanner rowScannerCir = new Scanner(lineCir);
            rowScannerTir.useDelimiter(";");
            rowScannerCir.useDelimiter(";");
            for (int j = 0; j < matrixNumberOfRows; j++) {
                int timeDistance = Integer.parseInt(rowScannerTir.next());
                double batteryConsumption = Double.parseDouble(rowScannerCir.next());
                    matrixSTtoCE[i][j] = new Edge(timeDistance, batteryConsumption);
            }
            rowScannerTir.close();
            rowScannerCir.close();
        }
        scannerTir.close();
        scannerCir.close();

        // ServiceTripData to ChargingEventData matrix
        Scanner scannerTrj = new Scanner(new File(fileTimeCEToST));
        Scanner scannerCrj = new Scanner(new File(fileEnergyCEToST));
        String lineTrj = scannerTrj.nextLine();
        String lineCrj = scannerCrj.nextLine();
        matrixNumberOfLines = Integer.parseInt(lineTrj);
        lineTrj = scannerTrj.nextLine();
        lineCir = scannerCrj.nextLine();
        matrixNumberOfRows = Integer.parseInt(lineTrj);

        Edge[][] matrixCEtoST = new Edge[matrixNumberOfLines][matrixNumberOfRows];

        for (int i = 0; i < matrixNumberOfLines; i++) {
            lineTrj = scannerTrj.nextLine();
            lineCrj = scannerCrj.nextLine();
            Scanner rowScannerTrj = new Scanner(lineTrj);
            Scanner rowScannerCrj = new Scanner(lineCrj);
            rowScannerTrj.useDelimiter(";");
            rowScannerCrj.useDelimiter(";");
            for (int j = 0; j < matrixNumberOfRows; j++) {
                int timeDistance = Integer.parseInt(rowScannerTrj.next());
                double batteryConsumption = Double.parseDouble(rowScannerCrj.next());
                matrixCEtoST[i][j] = new Edge(timeDistance, batteryConsumption);
            }
            rowScannerTrj.close();
            rowScannerCrj.close();
        }
        scannerTrj.close();
        scannerCrj.close();

        this.chargersWithChargingEvents = new LinkedList<>();
        for (ChargingEventData cE: chargingEventData
             ) {
            if (chargersWithChargingEvents.size() <= cE.getIndexCharger()) {
                TreeMap<Integer, ChargingEventVertex> newCharger = new TreeMap<>();
                chargersWithChargingEvents.add(newCharger);
            }
            chargersWithChargingEvents.get(cE.getIndexCharger()).put(cE.getStart(), new ChargingEventVertex(cE));
        }

        for (int i = 0; i < serviceTripData.size(); i++) {
            for (int j = 0; j < serviceTripData.size(); j++) {
                for (int k = 0; k < chargingEventData.size(); k++) {

                }
            }
        }

        STsGroup.getDepoStart(serviceTripData.get(0));
        STsGroup.setDepoEnd(serviceTripData.get(serviceTripData.size()));

        for (int tripIndex = 1; tripIndex < serviceTripData.size() - 1; tripIndex++) {
            STsGroup STsGroup = new STsGroup(serviceTripData.get(tripIndex));
            addVehicle(STsGroup);
        }
    }

    public Solution(Solution solution) {
        sTsGroups = new LinkedList<>();
        for (STsGroup STsGroup :
                solution.sTsGroups) {
            sTsGroups.add(new STsGroup(STsGroup));
        }
        chargersWithChargingEvents = solution.chargersWithChargingEvents;
        this.random = new Random();
    }

    public String toString() {
        StringBuilder solutionString = new StringBuilder("Solution: \n");
        solutionString.append("Number of STsGroups used: ").append(sTsGroups.size()).append("\n");
        solutionString.append("\n");
        int vehicleIndex = 1;
        for ( STsGroup STsGroup : sTsGroups) {
            solutionString.append("STsGroup number: ").append(vehicleIndex).append("\n");
            solutionString.append(STsGroup);
            solutionString.append("\n");
            vehicleIndex++;
        }
        return solutionString.toString();
    }

    public Solution findNext() {
        if (this.sTsGroups.size() == 1) {
            return null;
        }
        Solution nextSolution = new Solution(this);
        // randomly choose vehicle from solution
        int randomIndex = random.nextInt(nextSolution.sTsGroups.size());
        STsGroup randomSTsGroup = nextSolution.sTsGroups.remove(randomIndex);

        // release vehicle's CEs
        randomSTsGroup.releaseChargingEvents();

        // go over other Vehicles in Solution, try to assign any ST to another STsGroup
        boolean isAssignedMinOneST = false;
        int indexGroup = 0;
        while (indexGroup < nextSolution.sTsGroups.size() && !randomSTsGroup.isEmpty()) {
            // STsGroup to which we try to assign service trips from removedTrips
            // we keep this instance as a backup of STsGroup's STs and CEs in case we need to revert iteration
            STsGroup group = nextSolution.sTsGroups.get(indexGroup);

            STsGroup groupAfterTryInsert = group.tryInsertTrips(randomSTsGroup, chargersWithChargingEvents);
             if (group == groupAfterTryInsert && !isAssignedMinOneST) {
                isAssignedMinOneST = true;
            }
             nextSolution.sTsGroups.set(indexGroup, groupAfterTryInsert);
            indexGroup++;
        }
        if (!randomSTsGroup.isEmpty()) {
            LinkedList<STsGroup> vehiclesFromRemoved = randomSTsGroup.splitRemainingTrips(isAssignedMinOneST, chargersWithChargingEvents);
            nextSolution.sTsGroups.addAll(vehiclesFromRemoved);
        }
        return nextSolution;
    }
}
