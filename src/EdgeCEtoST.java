public class EdgeCEtoST {
    private final int timeDistance;
    private final double batteryConsumption;

    public EdgeCEtoST(int timeDistance, double batteryConsumption) {
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
