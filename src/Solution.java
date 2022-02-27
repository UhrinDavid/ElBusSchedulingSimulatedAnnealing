import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class Solution {
    private ArrayList<Vehicle> vehicles;
    private Random random;

    public Solution() {
        vehicles = new ArrayList<>();
        this.random = new Random();
    }

    public Solution(Solution solution) {
        vehicles = new ArrayList<>();
        for (Vehicle vehicle :
                solution.vehicles) {
            vehicles.add(vehicle);
        }
        this.random = new Random();
    }

    public void addVehicle(Vehicle vehicle) {
        vehicles.add(vehicle);
    }

    public boolean removeVehicle(Vehicle vehicle) {
        // check if vehicle has no service trips except depo
        if (!vehicle.hasServiceTrips()) {
            vehicles.remove(vehicle);
            return true;
        }
        return false;
    }

    public ArrayList<Vehicle> getVehicles() {
        return vehicles;
    }

    public String toString() {
        StringBuilder solutionString = new StringBuilder("Solution: \n");
        solutionString.append("Number of vehicles used: ").append(vehicles.size()).append("\n");
        solutionString.append("\n");
        int vehicleIndex = 1;
        for ( Vehicle vehicle : vehicles ) {
            solutionString.append("Vehicle number: ").append(vehicleIndex).append("\n");
            solutionString.append(vehicle);
            solutionString.append("\n");
            vehicleIndex++;
        }
        return solutionString.toString();
    }

    public Solution findNext(Edge[][] matrixSTtoST, Edge[][] matrixSTtoCE, Edge[][] matrixCEtoST, ArrayList<ChargingEvent> chargingEvents) {
        if (this.vehicles.size() == 1) {
            return null;
        }
        Solution nextSolution = new Solution(this);
        // randomly choose vehicle from solution
        int randomIndex = random.nextInt(nextSolution.vehicles.size());
        Vehicle randomVehicle = nextSolution.vehicles.remove(randomIndex);

        // release vehicle's CEs
        for (ChargingEvent event : randomVehicle.getChargingEvents()) {
            event.setIsReserved(false);
        }
        randomVehicle.getChargingEvents().clear();

        ArrayList<ServiceTrip> removedTrips = randomVehicle.getServiceTrips();
        // strip removedTrips of begin and end depos - only serviceTrips that need to be served remain
        ServiceTrip removedEnd = removedTrips.remove(removedTrips.size() - 1);
        ServiceTrip removedStart = removedTrips.remove(0);

        // go over other Vehicles in Solution, try to assign any ST to another Vehicle
        boolean isAssignedMinOneST = false;
        int indexVehicle = 0;
        while (indexVehicle < nextSolution.vehicles.size() && removedTrips.size() > 0) {
            // vehicle to which we try to assign service trips from removedTrips
            // we keep this instance as a backup of vehicle's STs and CEs in case we need to revert iteration
            Vehicle originalVehicle = nextSolution.vehicles.get(indexVehicle);

            if (originalVehicle.getServiceTrips().size() < 3) {
                System.out.println("too little trips in vehicle before insertion process starts\n" + originalVehicle);
            }
            if (originalVehicle.getServiceTrips().get(0).getId() != 0) {
                System.out.println("having bad vehicle depo start before insertion process starts" + originalVehicle);
            }
            if (originalVehicle.getServiceTrips().get(originalVehicle.getServiceTrips().size() - 1).getId() != 0) {
                System.out.println("having bad vehicle depo end before insertion process starts" + originalVehicle);
            }
            Vehicle vehicle = new Vehicle(originalVehicle);
            ArrayList<ServiceTrip> removedTripsAfterInsert = vehicle.tryInsertTrips(removedTrips, matrixSTtoST, matrixSTtoCE, matrixCEtoST, chargingEvents);
//            System.out.println("after insert size " + removedTripsAfterInsert.size());
//            System.out.println("original size " + removedTrips.size());
            if (removedTripsAfterInsert.size() < removedTrips.size()) {
                if (!isAssignedMinOneST) {
                    isAssignedMinOneST = true;
                }
                if (vehicle.getServiceTrips().size() < 3) {
                    System.out.println("too little trips in adding new trips to vehicle\n" + vehicle);
                }
                if (vehicle.getServiceTrips().get(0).getId() != 0) {
                    System.out.println("adding bad trip in adding new trips to vehicle" + vehicle);
                }
                if (vehicle.getServiceTrips().get(vehicle.getServiceTrips().size() - 1).getId() != 0) {
                    System.out.println("having bad vehicle depo end in adding new trips to vehicle" + vehicle);
                }
                nextSolution.vehicles.set(indexVehicle, vehicle);
                removedTrips = removedTripsAfterInsert;
            } else {
                if (originalVehicle.getServiceTrips().get(0).getId() != 0) {
                    System.out.println("adding bad trip when no new trip added to vehicle" + originalVehicle);
                }
                if (originalVehicle.getServiceTrips().get(originalVehicle.getServiceTrips().size() - 1).getId() != 0) {
                    System.out.println("having bad vehicle depo end when no new trip added to vehicle" + originalVehicle);
                }
                nextSolution.vehicles.set(indexVehicle, originalVehicle);
            }
            indexVehicle++;
        }
        if (removedTrips.size() > 0) {
            LinkedList<Vehicle> vehiclesFromRemoved = new LinkedList<>();
            if (!isAssignedMinOneST) {
                // split vehicle
                Vehicle vehicleFromRemoved = new Vehicle(removedStart, removedEnd);
                vehicleFromRemoved.addServiceTrip(removedTrips.remove(removedTrips.size() - 1));
                vehiclesFromRemoved.add(vehicleFromRemoved);
            }
            // place first trip from removed to new vehicle, try to assign trips
            while(removedTrips.size() > 0) {
                Vehicle vehicleFromRemoved = new Vehicle(removedStart, removedEnd);
                vehicleFromRemoved.addServiceTrip(removedTrips.remove(removedTrips.size() - 1));
                removedTrips = vehicleFromRemoved.tryInsertTrips(removedTrips, matrixSTtoST, matrixSTtoCE, matrixCEtoST, chargingEvents);
                vehiclesFromRemoved.add(vehicleFromRemoved);
            };
            for (Vehicle v : vehiclesFromRemoved
                 ) {
                if (v.getServiceTrips().size() < 3) {
                    System.out.println("too little trips in removed rest cleanup\n" + v);
                }
                if (v.getServiceTrips().get(0).getId() !=0) {
                    System.out.println("adding bad trip in removed rest cleanup " + v);
                }
                if (v.getServiceTrips().get(v.getServiceTrips().size() - 1).getId() != 0) {
                    System.out.println("having bad vehicle depo end in removed rest cleanup " + v);
                }
                nextSolution.vehicles.add(v);
            }
        }
        return nextSolution;
    }
}
