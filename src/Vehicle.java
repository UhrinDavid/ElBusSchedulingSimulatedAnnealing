import java.util.ArrayList;

public class Vehicle {
    private ArrayList<ServiceTrip> serviceTrips;
    private ArrayList<ChargingEvent> chargingEvents;
    final static double maxBatteryCapacity = 140;
    final static double minBatteryCapacity = 0;

    public Vehicle(ServiceTrip depoStart, ServiceTrip depoEnd) {
        serviceTrips = new ArrayList<>();
        chargingEvents = new ArrayList<>();
        this.serviceTrips.add(depoStart);
        this.serviceTrips.add(depoEnd);
    }

    public Vehicle(Vehicle vehicle) {
        serviceTrips = new ArrayList<>(vehicle.serviceTrips);
        chargingEvents = new ArrayList<>(vehicle.chargingEvents);
    }


    public void addServiceTrip (ServiceTrip serviceTrip) {
        boolean isAdded = false;
        int index = 1;
        while (!isAdded) {
            if (serviceTrip.getStart() < serviceTrips.get(index).getStart()) {
                isAdded = true;
                serviceTrips.add(index,serviceTrip);
            }
            index++;
        }
    }

    public void removeServiceTrip(ServiceTrip serviceTrip) {
        serviceTrips.remove(serviceTrip);
    }

    public ArrayList<ServiceTrip> getServiceTrips() {
        return serviceTrips;
    }

    public ArrayList<ChargingEvent> getChargingEvents() {return chargingEvents;}

    public String toString() {
        StringBuilder vehicleString = new StringBuilder();
        for ( ServiceTrip serviceTrip : serviceTrips ) {
            vehicleString.append("Service trip: ").append(serviceTrip.getId()).append("\n");
        }
        return vehicleString.toString();
    }

    public boolean hasServiceTrips() {
        return serviceTrips.size() > 2;
    }
}
