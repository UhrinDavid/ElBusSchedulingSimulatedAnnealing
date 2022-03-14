public class ChargingEventVertex extends ChargingEventData implements STGroupVertex {
    private boolean isReserved = false;

    public ChargingEventVertex(ChargingEventData chargingEventData) {
        super(chargingEventData.getIndexCharger(), chargingEventData.getIndexChargingEvent(),
                chargingEventData.getStart(), chargingEventData.getEnd(), chargingEventData.getChargingSpeed());
    }

    public boolean isReserved() {
        return isReserved;
    }

    public void setReserved(boolean reserved) {
        isReserved = reserved;
    }
}
