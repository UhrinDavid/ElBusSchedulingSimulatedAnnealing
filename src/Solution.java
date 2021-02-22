import java.util.ArrayList;

public class Solution {
    private ArrayList<Vehicle> vehicles;
    public Solution() {
        vehicles = new ArrayList<>();
    }

    public void addVehicle(Vehicle vehicle) {
        vehicles.add(vehicle);
    }

    public String toString() {
        StringBuilder solutionString = new StringBuilder("Solution: \n");
        solutionString.append("Number of vehicles used: ").append(vehicles.size()).append("\n");
        int vehicleIndex = 1;
        for ( Vehicle vehicle : vehicles ) {
            solutionString.append("Vehicle number: ").append(vehicleIndex).append("\n");
            solutionString.append(vehicle);
            vehicleIndex++;
        }
        return solutionString.toString();
    }


}
