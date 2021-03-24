public class EdgeSTtoCE {
    private final int timeDistance;
    private final double batteryConsumption;

    public EdgeSTtoCE(int timeDistance, double batteryConsumption) {
        this.timeDistance = timeDistance;
        this.batteryConsumption = batteryConsumption;
    }

    public int getTimeDistance() {
        return timeDistance;
    }

    public double getBatteryConsumption() {
        return batteryConsumption;
    }
}
