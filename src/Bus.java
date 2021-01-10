import java.util.ArrayList;

public class Bus {
    private ArrayList<ServiceTrip> serviceTrips;
    private ArrayList<ChargingEvent> chargingEvents;
    private double batteryCapacity;
    private  double maxBatteryCapacity;

    public Bus(double batteryCapacity) {
        serviceTrips = new ArrayList<>();
        chargingEvents = new ArrayList<>();
        this.batteryCapacity=batteryCapacity;
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

    public double getBatteryCapacity() {
        return batteryCapacity;
    }

    public void modifyBatteryCapacityBy(double modifyBy) {
        this.batteryCapacity += modifyBy;
    }
}
