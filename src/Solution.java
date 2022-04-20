import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Solution {
    private final LinkedList<STsGroup> sTsGroups;
    private final LinkedList<TreeMap<Integer, ChargingEventVertex>> chargersWithChargingEvents;
    private final int tripsNr;
    private final Random random;

    public Solution(String fileTrips, String fileChargers,
                    String fileEnergySTToST, String fileEnergySTToCE, String fileEnergyCEToST, String fileTimeSTToST,
                    String fileTimeSTToCE, String fileTimeCEToST) throws FileNotFoundException {
        sTsGroups = new LinkedList<>();
        this.random = new Random();
        LinkedList<ServiceTripData> serviceTripData;
        LinkedList<ChargingEventData> chargingEventData = ChargingEventData.createChargingEvents(fileChargers);
        serviceTripData = ServiceTripData.loadServiceTripsFromFile(fileTrips);
        tripsNr = serviceTripData.size() - 2;

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

        for (int tripIndex = 0; tripIndex < serviceTripData.size(); tripIndex++) {
            ServiceTripData trip = serviceTripData.get(tripIndex);
            TreeMap<Integer, Edge> edgesSubsequentTrips = new TreeMap<>();
            LinkedList<Edge> edgesSubsequentCes = new LinkedList<>();
            LinkedList<Edge> edgesPreviousCes = new LinkedList<>();

            for (int k = 0; k < chargersWithChargingEvents.size(); k++) {
                edgesPreviousCes.add(matrixCEtoST[k][tripIndex]);
                edgesSubsequentCes.add(matrixSTtoCE[tripIndex][k]);
            }
            for (int j = 0; j < serviceTripData.size(); j++) {
                if (matrixSTtoST[tripIndex][j] != null && trip.getStart() < serviceTripData.get(j).getStart()) {
                    edgesSubsequentTrips.put(serviceTripData.get(j).getId(), matrixSTtoST[tripIndex][j]);
                }
                if (matrixSTtoST[j][tripIndex] != null && trip.getStart() < serviceTripData.get(j).getStart()) {
                    edgesSubsequentTrips.put(serviceTripData.get(j).getId(), matrixSTtoST[tripIndex][j]);
                }
            }

            if (tripIndex == 0) {
                STsGroup.setDepoStart(new ServiceTripVertex(trip, edgesSubsequentTrips, edgesSubsequentCes, edgesPreviousCes));
            } else if (tripIndex == serviceTripData.size() - 1){
                STsGroup.setDepoEnd(new ServiceTripVertex(trip, edgesSubsequentTrips, edgesSubsequentCes, edgesPreviousCes));
            } else {
                STsGroup group = new STsGroup(new ServiceTripVertex(trip, edgesSubsequentTrips, edgesSubsequentCes, edgesPreviousCes));
                sTsGroups.add(group);
            }
        }
    }

    public Solution(Solution solution) {
        sTsGroups = new LinkedList<>();
        for (STsGroup STsGroup :
                solution.sTsGroups) {
            sTsGroups.add(new STsGroup(STsGroup));
        }
        chargersWithChargingEvents = solution.chargersWithChargingEvents;
        tripsNr = solution.tripsNr;
        this.random = new Random();
    }

    public void resetChargersForSolution() {
        for (TreeMap<Integer, ChargingEventVertex> charger : chargersWithChargingEvents
        ) {
            for (Map.Entry<Integer, ChargingEventVertex> cE: charger.entrySet()
            ) {
                cE.getValue().setReserved(false);
            }
        }
        for (STsGroup gr : sTsGroups
        ) {
            gr.reserveAssignedCEs();
        }
    }

    public String toString() {
        StringBuilder solutionString = new StringBuilder();
        for ( STsGroup STsGroup : sTsGroups) {
            solutionString.append("\n"+STsGroup);
        }
        return solutionString.toString();
    }

    public Solution findNext() {
        Solution nextSolution = new Solution(this);
        int randomIndex = random.nextInt(nextSolution.sTsGroups.size());
        STsGroup randomSTsGroup = nextSolution.sTsGroups.remove(randomIndex);
        randomSTsGroup.releaseChargingEvents();
        LinkedList<ServiceTripVertex> randomGroupTrips = new LinkedList<>(randomSTsGroup.getServiceTrips().values());
        LinkedList<ServiceTripVertex> leftoverTrips = new LinkedList<>();

        while (!randomGroupTrips.isEmpty()) {
            Collections.shuffle(nextSolution.sTsGroups);
            Iterator<STsGroup> groupsIt = nextSolution.sTsGroups.iterator();
            boolean isInserted = false;
            ServiceTripVertex removedTrip = randomGroupTrips.remove(0);
            while (groupsIt.hasNext() && !isInserted) {
                STsGroup groupNext = groupsIt.next();
                LinkedList<ServiceTripVertex> returnedTrips = groupNext.tryInsertOrReplaceTrip(
                        removedTrip, nextSolution.chargersWithChargingEvents, true);
                if (returnedTrips == null) {
                    isInserted = true;
                } else if (!returnedTrips.isEmpty() && returnedTrips.get(0) != removedTrip) {
                    isInserted = true;
                    leftoverTrips.addAll(returnedTrips);
                } else if (!groupsIt.hasNext()) {
                    leftoverTrips.add(removedTrip);
                }
            }
        }
        if (!leftoverTrips.isEmpty()) {
            Collections.shuffle(nextSolution.sTsGroups);
            Iterator<STsGroup> groupIt = nextSolution.sTsGroups.iterator();
            while (!leftoverTrips.isEmpty() && groupIt.hasNext()) {
                leftoverTrips = groupIt.next().tryInsertTrips(leftoverTrips, nextSolution.chargersWithChargingEvents);
            }
        }

        if (!leftoverTrips.isEmpty()) {
            LinkedList<STsGroup> vehiclesFromRemoved = STsGroup.splitRemainingTrips(leftoverTrips, nextSolution.chargersWithChargingEvents);
            nextSolution.sTsGroups.addAll(vehiclesFromRemoved);
        }
        return nextSolution;
    }

    public LinkedList<STsGroup> getsTsGroups() {
        return sTsGroups;
    }
}
