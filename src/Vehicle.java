import java.util.ArrayList;

public class Vehicle {
    private ArrayList<ServiceTrip> serviceTrips;
    private ArrayList<ChargingEvent> chargingEvents;
    private double currentBatteryCapacity;
    static double maxBatteryCapacity = 140;
    static double minBatteryCapacity = 0;

    public Vehicle() {
        serviceTrips = new ArrayList<>();
        chargingEvents = new ArrayList<>();
        this.currentBatteryCapacity =maxBatteryCapacity;
    }

    public void addServiceTrip (ServiceTrip serviceTrip) {
        serviceTrips.add(serviceTrip);
    }

    public void addChargingEvent (ChargingEvent chargingEvent) {
        chargingEvents.add(chargingEvent);
    }

    public void removeServiceTrip(ServiceTrip serviceTrip) {
        serviceTrips.remove(serviceTrip);
    }

    public void removeChargingEvent(ChargingEvent chargingEvent) {
        chargingEvents.remove(chargingEvent);
    }

    public ArrayList<ServiceTrip> getServiceTrips() {
        return serviceTrips;
    }

    public ArrayList<ChargingEvent> getChargingEvents() {
        return chargingEvents;
    }

    public double getCurrentBatteryCapacity() {
        return currentBatteryCapacity;
    }

    public void modifyBatteryCapacityBy(double delta) {
        this.currentBatteryCapacity += delta;
    }

    public String toString() {
        StringBuilder vehicleString = new StringBuilder();
        for ( ServiceTrip serviceTrip : serviceTrips ) {
            vehicleString.append("Service trip: ").append(serviceTrip.getId()).append("\n");
        }
        for (ChargingEvent chargingEvent : chargingEvents ) {
            vehicleString.append("Charging event: ").append(chargingEvent.getIndexChargingEvent()).append("\n");
        }
        return vehicleString.toString();
    }
}
