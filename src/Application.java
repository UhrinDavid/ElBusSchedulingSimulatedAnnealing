import java.io.FileNotFoundException;
import java.util.ArrayList;

public class Application {



	public static void main(String[] args) {
		try {
			ArrayList<ChargingEvent> chargingEvents = ChargingEvent.createChargingEvents("./src/Datasets/Pom_chargEvents.csv");
			
			ArrayList<ServiceTrip> serviceTrips = ServiceTrip.loadServiceTripsFromFile("./src/Datasets/Pom_Trips.csv");

			Solution solution = new Solution();

			for (int tripIndex = 1; tripIndex < serviceTrips.size()-1; tripIndex++) {
				Vehicle vehicle = new Vehicle();
				vehicle.addServiceTrip(serviceTrips.get(tripIndex));
				solution.addVehicle(vehicle);
			}

			System.out.println(solution);


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		
	}

}
