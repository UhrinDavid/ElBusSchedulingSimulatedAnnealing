import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class Solution {
    private ArrayList<STsGroup> STsGroups;
    private Random random;

    public Solution() {
        STsGroups = new ArrayList<>();
        this.random = new Random();
    }

    public Solution(Solution solution) {
        STsGroups = new ArrayList<>();
        for (STsGroup STsGroup :
                solution.STsGroups) {
            STsGroups.add(new STsGroup(STsGroup));
        }
        this.random = new Random();
    }

    public void addVehicle(STsGroup STsGroup) {
        STsGroups.add(STsGroup);
    }

    public boolean removeVehicle(STsGroup STsGroup) {
        // check if STsGroup has no service trips except depo
        if (!STsGroup.hasServiceTrips()) {
            STsGroups.remove(STsGroup);
            return true;
        }
        return false;
    }

    public ArrayList<STsGroup> getVehicles() {
        return STsGroups;
    }

    public String toString() {
        StringBuilder solutionString = new StringBuilder("Solution: \n");
        solutionString.append("Number of STsGroups used: ").append(STsGroups.size()).append("\n");
        solutionString.append("\n");
        int vehicleIndex = 1;
        for ( STsGroup STsGroup : STsGroups) {
            solutionString.append("STsGroup number: ").append(vehicleIndex).append("\n");
            solutionString.append(STsGroup);
            solutionString.append("\n");
            vehicleIndex++;
        }
        return solutionString.toString();
    }

    public Solution findNext(Edge[][] matrixSTtoST, Edge[][] matrixSTtoCE, Edge[][] matrixCEtoST, ArrayList<ChargingEvent> chargingEvents) {
        if (this.STsGroups.size() == 1) {
            return null;
        }
        Solution nextSolution = new Solution(this);
        // randomly choose vehicle from solution
        int randomIndex = random.nextInt(nextSolution.STsGroups.size());
        STsGroup randomSTsGroup = nextSolution.STsGroups.remove(randomIndex);

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
        while (indexVehicle < nextSolution.STsGroups.size() && removedTrips.size() > 0) {
            // STsGroup to which we try to assign service trips from removedTrips
            // we keep this instance as a backup of STsGroup's STs and CEs in case we need to revert iteration
            STsGroup originalSTsGroup = nextSolution.STsGroups.get(indexVehicle);

            STsGroup STsGroup = new STsGroup(originalSTsGroup);
            ArrayList<ServiceTrip> removedTripsAfterInsert = STsGroup.tryInsertTrips(removedTrips, matrixSTtoST, matrixSTtoCE, matrixCEtoST, chargingEvents);
            if (removedTripsAfterInsert.size() < removedTrips.size()) {
                if (!isAssignedMinOneST) {
                    isAssignedMinOneST = true;
                }
                nextSolution.STsGroups.set(indexVehicle, STsGroup);
                removedTrips = removedTripsAfterInsert;
            } else {
                for (ChargingEvent cE : originalSTsGroup.getChargingEvents()
                     ) {
                    cE.setIsReserved(true);
                }
                nextSolution.STsGroups.set(indexVehicle, originalSTsGroup);
            }
            indexVehicle++;
        }
        if (removedTrips.size() > 0) {
            LinkedList<STsGroup> vehiclesFromRemoved = new LinkedList<>();
//            if (!isAssignedMinOneST) {
//                // split vehicle
//                STsGroup vehicleFromRemoved = new STsGroup(removedStart, removedEnd);
//                vehicleFromRemoved.addServiceTrip(removedTrips.remove(removedTrips.size() - 1));
//                vehiclesFromRemoved.add(vehicleFromRemoved);
//            }
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
                nextSolution.STsGroups.add(v);
            }
        }
        return nextSolution;
    }
}
