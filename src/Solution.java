import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

public class Solution {
    private ArrayList<STsGroup> sTsGroups;
    private ArrayList<ChargingEvent> chargingEvents;
    private Random random;

    private Edge[][] matrixSTtoST;
    private Edge[][] matrixSTtoCE;
    private Edge[][] matrixCEtoST;

    public Solution(String fileTrips, String fileChargers,
                    String fileEnergySTToST, String fileEnergySTToCE, String fileEnergyCEToST, String fileTimeSTToST,
                    String fileTimeSTToCE, String fileTimeCEToST) throws FileNotFoundException {
        sTsGroups = new ArrayList<>();
        this.random = new Random();
        ArrayList<ServiceTrip> serviceTrips;
        chargingEvents = ChargingEvent.createChargingEvents(fileChargers);
        serviceTrips = ServiceTrip.loadServiceTripsFromFile(fileTrips);

        for (int tripIndex = 1; tripIndex < serviceTrips.size() - 1; tripIndex++) {
            STsGroup STsGroup = new STsGroup(serviceTrips.get(0), serviceTrips.get(serviceTrips.size() - 1));
            STsGroup.addServiceTrip(serviceTrips.get(tripIndex));
            addVehicle(STsGroup);
        }


        // ServiceTrip to ServiceTrip matrix
        Scanner scannerTij = new Scanner(new File(fileTimeSTToST));
        Scanner scannerCij = new Scanner(new File(fileEnergySTToST));
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
                double batteryConsumption = Double.parseDouble(rowScannerCij.next());
                // check if such edge is possible (timewise), if not insert null
                if (serviceTrips.get(i).getEnd() + timeDistance > serviceTrips.get(j).getStart()) {
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

        //  TODO: if event is on same charger as previous and has same start time, don't create edge
        // ServiceTrip to ChargingEvent matrix
        Scanner scannerTir = new Scanner(new File(fileTimeSTToCE));
        Scanner scannerCir = new Scanner(new File(fileEnergySTToCE));
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
                double batteryConsumption = Double.parseDouble(rowScannerCir.next());
                if (serviceTrips.get(i).getEnd() + timeDistance > chargingEvents.get(j).getStartTime()) {
                    matrixSTtoCE[i][j] = null;
                } else {
                    matrixSTtoCE[i][j] = new Edge(timeDistance, batteryConsumption);
                }
            }
            rowScannerTir.close();
            rowScannerCir.close();
        }
        scannerTir.close();
        scannerCir.close();

        // ServiceTrip to ChargingEvent matrix
        Scanner scannerTrj = new Scanner(new File(fileTimeCEToST));
        Scanner scannerCrj = new Scanner(new File(fileEnergyCEToST));
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
                double batteryConsumption = Double.parseDouble(rowScannerCrj.next());
                if (chargingEvents.get(i).getStartTime() + timeDistance > serviceTrips.get(j).getStart()) {
                    matrixCEtoST[i][j] = null;
                } else {
                    matrixCEtoST[i][j] = new Edge(timeDistance, batteryConsumption);
                }
            }
            rowScannerTrj.close();
            rowScannerCrj.close();
        }
        scannerTrj.close();
        scannerCrj.close();
    }

    public Solution(Solution solution) {
        sTsGroups = new ArrayList<>();
        for (STsGroup STsGroup :
                solution.sTsGroups) {
            sTsGroups.add(new STsGroup(STsGroup));
        }
        chargingEvents = new ArrayList<>(solution.chargingEvents);
        matrixSTtoST = solution.matrixSTtoST;
        matrixCEtoST = solution.matrixCEtoST;
        matrixSTtoCE = solution.matrixSTtoCE;
        this.random = new Random();
    }

    public void addVehicle(STsGroup STsGroup) {
        sTsGroups.add(STsGroup);
    }

    public boolean removeVehicle(STsGroup STsGroup) {
        // check if STsGroup has no service trips except depo
        if (!STsGroup.hasServiceTrips()) {
            sTsGroups.remove(STsGroup);
            return true;
        }
        return false;
    }

    public ArrayList<STsGroup> getVehicles() {
        return sTsGroups;
    }

    public String toString() {
        int freeEvents = 0;
        for (ChargingEvent ev: chargingEvents
        ) {
            if (!ev.getIsReserved()) {
                freeEvents++;
            } else {
                System.out.println(
                        ev.getMatrixIndex());
            }
        }
        System.out.println("free: " + freeEvents + " total: " + chargingEvents.size());
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
        int totalTrips = 0;
        for (STsGroup gr: sTsGroups
             ) {
            totalTrips+=gr.getServiceTrips().size()-2;
        }
        if (this.sTsGroups.size() == 1) {
            return null;
        }
        Solution nextSolution = new Solution(this);
        // randomly choose vehicle from solution
        int randomIndex = random.nextInt(nextSolution.sTsGroups.size());
        STsGroup randomSTsGroup = nextSolution.sTsGroups.remove(randomIndex);

        // release vehicle's CEs
        for (ChargingEvent event : randomSTsGroup.getChargingEvents()) {
            event.setIsReserved(false);
        }
        randomSTsGroup.getChargingEvents().clear();

        ArrayList<ServiceTrip> removedTrips = randomSTsGroup.getServiceTrips();
        // strip removedTrips of begin and end depos - only serviceTrips that need to be served remain
        ServiceTrip removedEnd = removedTrips.remove(removedTrips.size() - 1);
        ServiceTrip removedStart = removedTrips.remove(0);

        // go over other Vehicles in Solution, try to assign any ST to another STsGroup
        boolean isAssignedMinOneST = false;
        int indexVehicle = 0;
        while (indexVehicle < nextSolution.sTsGroups.size() && removedTrips.size() > 0) {
            // STsGroup to which we try to assign service trips from removedTrips
            // we keep this instance as a backup of STsGroup's STs and CEs in case we need to revert iteration
            STsGroup originalSTsGroup = nextSolution.sTsGroups.get(indexVehicle);

            STsGroup STsGroup = new STsGroup(originalSTsGroup);
            ArrayList<ServiceTrip> removedTripsAfterInsert = STsGroup.tryInsertTrips(removedTrips, matrixSTtoST, matrixSTtoCE, matrixCEtoST, chargingEvents);
            if (removedTripsAfterInsert.size() < removedTrips.size()) {
                if (!isAssignedMinOneST) {
                    isAssignedMinOneST = true;
                }
                nextSolution.sTsGroups.set(indexVehicle, STsGroup);
                removedTrips = removedTripsAfterInsert;
            } else {
                for (ChargingEvent cE : originalSTsGroup.getChargingEvents()
                     ) {
                    cE.setIsReserved(true);
                }
                nextSolution.sTsGroups.set(indexVehicle, originalSTsGroup);
            }
            indexVehicle++;
        }
        if (removedTrips.size() > 0) {
            LinkedList<STsGroup> vehiclesFromRemoved = new LinkedList<>();
            if (!isAssignedMinOneST) {
                // split vehicle
                STsGroup vehicleFromRemoved = new STsGroup(removedStart, removedEnd);
                vehicleFromRemoved.addServiceTrip(removedTrips.remove(removedTrips.size() - 1));
                vehiclesFromRemoved.add(vehicleFromRemoved);
            }
            // place first trip from removed to new vehicle, try to assign trips
            while(removedTrips.size() > 0) {
                STsGroup STsGroupFromRemoved = new STsGroup(removedStart, removedEnd);
                STsGroupFromRemoved.addServiceTrip(removedTrips.remove(removedTrips.size() - 1));
                if (removedTrips.size() > 0) {
                    removedTrips = STsGroupFromRemoved.tryInsertTrips(removedTrips, matrixSTtoST, matrixSTtoCE, matrixCEtoST, chargingEvents);
                }
                vehiclesFromRemoved.add(STsGroupFromRemoved);
            };
            for (STsGroup v : vehiclesFromRemoved
                 ) {
                nextSolution.sTsGroups.add(v);
            }
        }
        int totalTripsAfter = 0;
        for (STsGroup gr: nextSolution.sTsGroups
        ) {
            totalTripsAfter+=gr.getServiceTrips().size()-2;
        }
        if (totalTripsAfter < totalTrips) {
            System.out.println("loss of trips " + totalTrips + " after " + totalTripsAfter);
        }
        return nextSolution;
    }
}
