import java.util.ArrayList;

public class Solution {
    private ArrayList<Vehicle> vehicles;



    public Solution() {
        vehicles = new ArrayList<>();
    }

    public Solution(Solution solution) {
        vehicles = new ArrayList<>();
        for (Vehicle vehicle :
                solution.vehicles) {
            vehicles.add(new Vehicle(vehicle));
        }
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


}
