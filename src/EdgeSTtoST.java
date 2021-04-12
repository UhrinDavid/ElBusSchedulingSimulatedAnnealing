import java.util.ArrayList;

public class EdgeSTtoST {
    private final int timeDistance;
    private final double batteryConsumption;
    private final boolean isAccesible;
    private ArrayList<ChargingEvent> accesibleCEs;

    public EdgeSTtoST(int timeDistance, double batteryConsumption, boolean isAccesible) {
        this.timeDistance = timeDistance;
        this.batteryConsumption = batteryConsumption;
        this.isAccesible = isAccesible;
        this.accesibleCEs = new ArrayList<>();
    }

    public int getTimeDistance() {
        return timeDistance;
    }

    public double getBatteryConsumption() {
        return batteryConsumption;
    }

    public boolean isAccesible() {
        return isAccesible;
    }

    public ArrayList<ChargingEvent> getAccesibleCEs() {
        return accesibleCEs;
    }
}
