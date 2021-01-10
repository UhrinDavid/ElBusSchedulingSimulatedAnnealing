import java.io.FileNotFoundException;
import java.util.ArrayList;

public class main {

	public static void main(String[] args) {
		try {
			ArrayList<ChargingEvent> chargingEvents = ChargingEvent.createChargingEvents("./src/Datasets/Pom_chargEvents.csv");
			for (ChargingEvent event : chargingEvents) {
				System.out.println(event.getIndexCharger());
			}
			
			ArrayList<ServiceTrip> serviceTrips = ServiceTrip.createServiceTrips("./src/Datasets/Pom_Trips.csv");
			for (ServiceTrip trip : serviceTrips) {
				System.out.println(trip.getId());
			}


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		
	}

}
