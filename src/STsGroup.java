import java.util.*;

public class STsGroup {
    private TreeMap<Integer, STGroupVertex> serviceTripsWithCEsVertices;
    final static double maxBatteryCapacity = 140;
    final static double minBatteryCapacity = 0;
    static private ServiceTripVertex depoStart = null;
    static private ServiceTripVertex depoEnd = null;
    private Random random;

    public static void setDepoStart(ServiceTripVertex depoStart) {
        if (STsGroup.depoStart == null) {
            STsGroup.depoStart = depoStart;
        }
    }

    public static void setDepoEnd(ServiceTripVertex depoEnd) {
        if (STsGroup.depoEnd == null) {
            STsGroup.depoEnd = depoEnd;
        }
    }

    public static ServiceTripVertex getDepoStart() {
        return depoStart;
    }

    public static ServiceTripVertex getDepoEnd() {
        return depoEnd;
    }

    public STsGroup(ServiceTripVertex initialServiceTrip) {
        serviceTripsWithCEsVertices = new TreeMap<>();
        this.serviceTripsWithCEsVertices.put(initialServiceTrip.getStart(), initialServiceTrip);
        this.random = new Random();
    }

    public STsGroup(STsGroup sTsGroup) {
        serviceTripsWithCEsVertices = new TreeMap<>(sTsGroup.serviceTripsWithCEsVertices);
        this.random = sTsGroup.random;
    }

    public String toString() {
        StringBuilder stringFinal = new StringBuilder();
        stringFinal.append("---------\n");
        for ( Map.Entry<Integer, STGroupVertex> serviceTripData : serviceTripsWithCEsVertices.entrySet()) {
            STGroupVertex vertex = serviceTripData.getValue();
            if (vertex instanceof ServiceTripVertex) {
                stringFinal.append("Service trip: ").append(((ServiceTripVertex) vertex).getId()).append("\n");
            } else {
                ChargingEventVertex chargingEventVertex = (ChargingEventVertex) vertex;
                stringFinal.append("Charging event: ")
                        .append(chargingEventVertex.getIndexCharger()+"_"+chargingEventVertex.getIndexChargingEvent())
                        .append("\n");
            }
        }
        stringFinal.append("---------\n");
        return stringFinal.toString();
    }

    public double checkForBatteryDeficiency() {
        // check if there is enough battery capacity, if not, try to insert charging events
        double deficiencyAll = STsGroup.maxBatteryCapacity;
        ServiceTripVertex tripPrevious = STsGroup.getDepoStart();
        int serviceTripIndex = 0;
        int tripsSize = serviceTripsWithCEsVertices.size();
        // create deficiency structure
        for (Map.Entry<Integer, STGroupVertex> tripEntry : serviceTripsWithCEsVertices.entrySet()
             ) {
            ServiceTripVertex trip = tripsSize - 1 > serviceTripIndex ? (ServiceTripVertex) tripEntry.getValue() : STsGroup.getDepoEnd();
            deficiencyAll -= tripPrevious.getEdgeForSubsequentTrip(trip.getId()).getBatteryConsumption()
                    + trip.getConsumption();
            tripPrevious = trip;
            serviceTripIndex++;
        }
        return deficiencyAll;
    }

    public boolean tryChargeBetweenSTs(double deficiencyAll, LinkedList<TreeMap<Integer, ChargingEventVertex>> chargersWithChargingEvents) {
        double deficiencyLeft = deficiencyAll;
        double currentBatteryState = STsGroup.maxBatteryCapacity;
        TreeMap<Integer, ChargingEventVertex> usedEvents = new TreeMap<>();
        ServiceTripVertex trip = (ServiceTripVertex) serviceTripsWithCEsVertices.firstEntry().getValue();
        ServiceTripVertex tripPrevious = STsGroup.getDepoStart();
        do {
            final Edge edgeSTtoST = tripPrevious.getEdgeForSubsequentTrip(trip.getId());
            final double tripConsumption = edgeSTtoST.getBatteryConsumption()
                    + trip.getConsumption();
            // find suitable CE
            // get first possible index on charger
            Iterator<TreeMap<Integer, ChargingEventVertex>> chargerIt = chargersWithChargingEvents.iterator();
            int indexCharger = 0;
            boolean isChargedBeforeTrip = false;
            while (chargerIt.hasNext() && !isChargedBeforeTrip
            ) {
                TreeMap<Integer, ChargingEventVertex> chargingEventsOnCharger = chargerIt.next();
                final Edge edgeSTtoCE = tripPrevious.getEdgeForSubsequentCE(indexCharger);
                final Edge edgeCEtoST = trip.getEdgeForPreviousCE(indexCharger);
                ChargingEventVertex cE = chargingEventsOnCharger
                        .ceilingEntry(tripPrevious.getEnd() + edgeSTtoCE.getTimeDistance()).getValue();
                if (cE != null && cE.getStart() + edgeCEtoST.getTimeDistance() < trip.getStart()
                        && currentBatteryState - edgeSTtoCE.getBatteryConsumption() > 0) {
                    final int firstInsertableCEKey = cE.getIndexChargingEvent();
                    int lastInsertableCEKey;
                    boolean shouldCheckEventOnCharger = true;
                    final double maxTimeBetweenSTs = trip.getStart() - tripPrevious.getEnd() - edgeSTtoCE.getTimeDistance() - edgeCEtoST.getTimeDistance();
                    final double consumptionDuringEvent = edgeSTtoCE.getBatteryConsumption() + edgeCEtoST.getBatteryConsumption();
                    double potentialBatteryState;
                    double maxChargingTimeOnChargingSequence = 0.0;
                    double maxChargeOnChargingSequence = 0.0;
                    do {
                        lastInsertableCEKey = cE.getIndexChargingEvent();
                        // check if there is succeeding event on same charger
                        // max charge until start of next event on charger, if event on charger is last
                        // we can charge until start of next trip
                        final double maxChargingTimeOnEvent = cE.getEnd() - cE.getStart();
                        final double actualMaxChargingTimeBetweenSTs = Math.min(maxTimeBetweenSTs, maxChargingTimeOnChargingSequence + maxChargingTimeOnEvent);
                        maxChargeOnChargingSequence += (actualMaxChargingTimeBetweenSTs - maxChargeOnChargingSequence) * cE.getChargingSpeed();
                        maxChargingTimeOnChargingSequence = actualMaxChargingTimeBetweenSTs;
                        // end charging if:
                        //      - capacity of ST's battery is at full state
                        //      - total deficiency reaches zero
                        potentialBatteryState = Math.min(currentBatteryState - consumptionDuringEvent + maxChargeOnChargingSequence, STsGroup.maxBatteryCapacity);
                        final double potentialDeficiency = deficiencyLeft + potentialBatteryState - currentBatteryState + edgeSTtoST.getBatteryConsumption();
                        if (potentialBatteryState < STsGroup.maxBatteryCapacity && potentialDeficiency < 0) {
                            shouldCheckEventOnCharger = true;
                        } else {
                            shouldCheckEventOnCharger = false;
                        }
                        cE = chargingEventsOnCharger.ceilingEntry(cE.getEnd()).getValue();
                    } while (cE != null && shouldCheckEventOnCharger && cE.getStart() + edgeCEtoST.getTimeDistance() < trip.getStart()
                            && !cE.isReserved());
                    // check if we charge more than minimum required for next trip / more than is consumed to and from event
                    if (potentialBatteryState - trip.getConsumption() > 0
                            && potentialBatteryState - trip.getConsumption() > currentBatteryState - tripConsumption) {
                        final double deficiencyModifier = -currentBatteryState + potentialBatteryState + edgeSTtoST.getBatteryConsumption();
                        deficiencyLeft += deficiencyModifier;
                        currentBatteryState = potentialBatteryState - trip.getConsumption();
                        SortedMap<Integer, ChargingEventVertex> eventsToAdd =
                                chargingEventsOnCharger.subMap(firstInsertableCEKey, lastInsertableCEKey +1);
                        for (Map.Entry<Integer, ChargingEventVertex> cEToReserve : eventsToAdd.entrySet()
                             ) {
                            cEToReserve.getValue().setReserved(true);
                        }
                        usedEvents.putAll(eventsToAdd);
                        isChargedBeforeTrip = true;
                    }
                }
            }
            if (!isChargedBeforeTrip) {
                currentBatteryState -= tripConsumption;
                if (currentBatteryState < 0) {
                    for (Map.Entry<Integer, ChargingEventVertex> usedEvent : usedEvents.entrySet()
                    ) {
                        usedEvent.getValue().setReserved(false);
                    }
                    return false;
                }
            }
            tripPrevious = trip;
            Map.Entry<Integer, STGroupVertex> nextVertex = serviceTripsWithCEsVertices.higherEntry(trip.getStart());
            trip = nextVertex == null ? null : (ServiceTripVertex) nextVertex.getValue();
        } while (deficiencyLeft < 0 && trip != null);

        if (deficiencyLeft < 0) {
            for (Map.Entry<Integer, ChargingEventVertex> usedEvent : usedEvents.entrySet()
            ) {
                usedEvent.getValue().setReserved(false);
            }
            return false;
        }
        this.serviceTripsWithCEsVertices.putAll(usedEvents);
        return true;
    }

    public void releaseChargingEvents () {
        Iterator<Map.Entry<Integer, STGroupVertex>> vertexIt = this.serviceTripsWithCEsVertices.entrySet().iterator();
        while (vertexIt.hasNext()){
            Map.Entry<Integer, STGroupVertex> vertex = vertexIt.next();
            if (vertex.getValue() instanceof ChargingEventVertex) {
                ChargingEventVertex vertexCE = (ChargingEventVertex) vertex.getValue();
                vertexIt.remove();
                vertexCE.setReserved(false);
            }
        }
    }

    public boolean isEmpty () {
        return serviceTripsWithCEsVertices.isEmpty();
    }

    public void reserveAssignedCEs() {
        for (Map.Entry<Integer, STGroupVertex> vertex: serviceTripsWithCEsVertices.entrySet()
             ) {
            if (vertex.getValue() instanceof ChargingEventVertex) {
                ((ChargingEventVertex) vertex).setReserved(true);
            }
        }
    }

    public STsGroup tryInsertTrips(STsGroup removedGroup, LinkedList<TreeMap<Integer, ChargingEventVertex>> chargersWithChargingEvents) {
        STsGroup backupGroup = new STsGroup(this);
        // backup of current state of removedTrips from vehicle we try to eliminate from the solution - in case we need to revert iteration
        TreeMap<Integer, STGroupVertex> removedTrips = removedGroup.serviceTripsWithCEsVertices;
        TreeMap<Integer, STGroupVertex> failedToInsertList = new TreeMap<>();
        int removedTripsInitialSize = removedTrips.size();

        // try to find a time window between already existing trips in vehicle,
        // we don't take capacity deficiency into account at first, only if ST can be inserted respecting time restrictions

        // trips' lists begins and ends with depo, we can insert between depo and first
        // but no after depo (therefore size - 1)
        while (removedTrips.size() > 0) {
//            final int randomIndex = random.nextInt(removedTrips.size());
            ServiceTripVertex removedTrip = (ServiceTripVertex) removedTrips.pollFirstEntry().getValue();
            LinkedList<ChargingEventVertex> cEsBackup = new LinkedList<>();
            Iterator<Map.Entry<Integer, STGroupVertex>> vertexIt = this.serviceTripsWithCEsVertices.entrySet().iterator();
             while (vertexIt.hasNext()){
                 Map.Entry<Integer, STGroupVertex> vertex = vertexIt.next();
                if (vertex.getValue() instanceof ChargingEventVertex) {
                    ChargingEventVertex vertexCE = (ChargingEventVertex) vertex.getValue();
                    vertexIt.remove();
                    vertexCE.setReserved(false);
                    cEsBackup.add(vertexCE);
                }
            }
             Map.Entry<Integer, STGroupVertex> vertexBefore = serviceTripsWithCEsVertices
                     .floorEntry(removedTrip.getStart());
            ServiceTripVertex tripBefore = vertexBefore == null ? STsGroup.getDepoStart() : (ServiceTripVertex) vertexBefore.getValue();
            Map.Entry<Integer, STGroupVertex> vertexAfter = serviceTripsWithCEsVertices
                    .ceilingEntry(tripBefore.getEnd());
            ServiceTripVertex tripAfter = vertexAfter == null ? STsGroup.getDepoEnd() : (ServiceTripVertex) vertexAfter.getValue();
            if (
                    tripBefore.getEdgeForSubsequentTrip(removedTrip.getId()) != null
                && removedTrip.getEdgeForSubsequentTrip(tripAfter.getId()) != null
            ) {
                serviceTripsWithCEsVertices.put(removedTrip.getStart(), removedTrip);
                double deficiencyAll = checkForBatteryDeficiency();
                if (deficiencyAll < 0 && !tryChargeBetweenSTs(deficiencyAll, chargersWithChargingEvents)) {
                    serviceTripsWithCEsVertices.remove(removedTrip.getStart());
                    failedToInsertList.put(removedTrip.getStart(), removedTrip);
                    for (ChargingEventVertex event : cEsBackup) {
                        event.setReserved(true);
                        serviceTripsWithCEsVertices.put(event.getIndexChargingEvent(), event);
                    }
                }
            } else {
                failedToInsertList.put(removedTrip.getStart(), removedTrip);
            }
        }
        removedGroup.serviceTripsWithCEsVertices = failedToInsertList;
        if (failedToInsertList.size() == removedTripsInitialSize) {
            return  backupGroup;
        }
        return this;
    }

    public LinkedList<STsGroup> splitRemainingTrips(boolean isAssignedMinOneST,
                                                    LinkedList<TreeMap<Integer, ChargingEventVertex>> chargersWithChargingEvents) {
        LinkedList<STsGroup> vehiclesFromRemoved = new LinkedList<>();
        ServiceTripVertex vertex = (ServiceTripVertex) this.serviceTripsWithCEsVertices.pollLastEntry().getValue();
        if (!isAssignedMinOneST) {
            // split vehicle
            STsGroup vehicleFromRemoved = new STsGroup(vertex);
            vehiclesFromRemoved.add(vehicleFromRemoved);
        }
        Map.Entry<Integer, STGroupVertex> vertexEntry = this.serviceTripsWithCEsVertices.pollLastEntry();
        vertex = vertexEntry == null ? null : (ServiceTripVertex) vertexEntry.getValue();
        // place first trip from removed to new vehicle, try to assign trips
        while(vertex != null) {
            STsGroup STsGroupFromRemoved = new STsGroup(vertex);
            if (!this.serviceTripsWithCEsVertices.isEmpty()) {
                STsGroupFromRemoved.tryInsertTrips(this, chargersWithChargingEvents);
            }
            vehiclesFromRemoved.add(STsGroupFromRemoved);
        };
        return vehiclesFromRemoved;
    }

    public TreeMap<Integer, STGroupVertex> getServiceTripsWithCEsVertices() {
        return serviceTripsWithCEsVertices;
    }
}
