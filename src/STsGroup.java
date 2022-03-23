import java.util.*;

public class STsGroup {
    private TreeMap<Integer, STGroupVertex> serviceTripsWithCEsVertices;
    final static double maxBatteryCapacity = 30;
    final static double minBatteryCapacity = 0;
    static private ServiceTripVertex depoStart = null;
    static private ServiceTripVertex depoEnd = null;
    private final Random random;

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
//        stringFinal.append("---------\n");
        boolean isFirst = true;
        for (Map.Entry<Integer, STGroupVertex> serviceTripData : serviceTripsWithCEsVertices.entrySet()) {
            STGroupVertex vertex = serviceTripData.getValue();
            if (isFirst) {
                isFirst = false;
            } else {
                stringFinal.append("->");
            }
            if (vertex instanceof ServiceTripVertex) {
                stringFinal.append(((ServiceTripVertex) vertex).getId());
            } else {
                ChargingEventVertex chargingEventVertex = (ChargingEventVertex) vertex;
                stringFinal
                        .append(chargingEventVertex.getIndexCharger() + "_" + chargingEventVertex.getIndexChargingEvent());
            }
        }
//        stringFinal.append("\n---------\n");
        return stringFinal.toString();
    }

    public double checkForBatteryDeficiency() {
        // check if there is enough battery capacity, if not, try to insert charging events
        double deficiencyAll = STsGroup.maxBatteryCapacity;
        ServiceTripVertex tripPrevious = STsGroup.getDepoStart();
        // create deficiency structure
        for (Map.Entry<Integer, STGroupVertex> tripEntry : serviceTripsWithCEsVertices.entrySet()
        ) {
            ServiceTripVertex trip = (ServiceTripVertex) tripEntry.getValue();
            deficiencyAll -= tripPrevious.getEdgeForSubsequentTrip(trip.getId()).getBatteryConsumption()
                    + trip.getConsumption();
            tripPrevious = trip;
        }
        ServiceTripVertex trip = STsGroup.getDepoEnd();
        deficiencyAll -= tripPrevious.getEdgeForSubsequentTrip(trip.getId()).getBatteryConsumption()
                + trip.getConsumption();
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
            boolean isChargedBeforeTrip = false;
            LinkedList<TreeMap<Integer, ChargingEventVertex>> randomOrderedChargers = new LinkedList<>(chargersWithChargingEvents);
            Collections.shuffle(randomOrderedChargers);
            Iterator<TreeMap<Integer, ChargingEventVertex>> chargerIt = randomOrderedChargers.iterator();
            while (chargerIt.hasNext() && !isChargedBeforeTrip
            ) {
                TreeMap<Integer, ChargingEventVertex> chargingEventsOnCharger = chargerIt.next();
                final int indexCharger= chargingEventsOnCharger.firstEntry().getValue().getIndexCharger();
                final Edge edgeSTtoCE = tripPrevious.getEdgeForSubsequentCE(indexCharger);
                final Edge edgeCEtoST = trip.getEdgeForPreviousCE(indexCharger);
                ChargingEventVertex cE = null;
                do {
                    Map.Entry<Integer, ChargingEventVertex> cEEntry = chargingEventsOnCharger
                            .ceilingEntry(cE == null ? tripPrevious.getEnd() + edgeSTtoCE.getTimeDistance() : cE.getStart() + 1);
                    cE = cEEntry == null ? null : cEEntry.getValue();
                    if (cE != null && cE.getStart() + edgeCEtoST.getTimeDistance() < trip.getStart()
                            && currentBatteryState - edgeSTtoCE.getBatteryConsumption() > 0 && !cE.isReserved()) {
                        final int firstInsertableCEKey = cE.getStart();
                        int lastInsertableCEKey;
                        boolean shouldCheckEventOnCharger;
                        final double maxTimeBetweenSTs = trip.getStart() - tripPrevious.getEnd() - edgeSTtoCE.getTimeDistance() - edgeCEtoST.getTimeDistance();
                        final double consumptionDuringEvent = edgeSTtoCE.getBatteryConsumption() + edgeCEtoST.getBatteryConsumption();
                        double potentialBatteryState;
                        double maxChargingTimeOnChargingSequence = 0.0;
                        double maxChargeOnChargingSequence = 0.0;
                        do {
                            lastInsertableCEKey = cE.getStart();
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
                            shouldCheckEventOnCharger = potentialBatteryState < STsGroup.maxBatteryCapacity && potentialDeficiency < 0;
                            cEEntry = chargingEventsOnCharger.ceilingEntry(cE.getEnd());
                            cE = cEEntry == null ? null : cEEntry.getValue();
                        } while (cE != null && shouldCheckEventOnCharger && cE.getStart() + edgeCEtoST.getTimeDistance() < trip.getStart()
                                && !cE.isReserved());
                        // check if we charge more than minimum required for next trip / more than is consumed to and from event
                        if (potentialBatteryState - trip.getConsumption() > 0
                                && potentialBatteryState - trip.getConsumption() > currentBatteryState - tripConsumption) {
                            final double deficiencyModifier = -currentBatteryState + potentialBatteryState + edgeSTtoST.getBatteryConsumption();
                            deficiencyLeft += deficiencyModifier;
                            currentBatteryState = potentialBatteryState - trip.getConsumption();
                            TreeMap<Integer, ChargingEventVertex> eventsToAdd =
                                    new TreeMap<>(chargingEventsOnCharger.subMap(firstInsertableCEKey, false, lastInsertableCEKey, firstInsertableCEKey != lastInsertableCEKey));
                            ChargingEventVertex firstEvent = chargingEventsOnCharger.get(firstInsertableCEKey);
                            int firstPossibleStart = Math.max(firstEvent.getStart(), tripPrevious.getEnd() + edgeSTtoCE.getTimeDistance());
                            eventsToAdd.put(firstPossibleStart, firstEvent);
                            for (Map.Entry<Integer, ChargingEventVertex> cEToReserve : eventsToAdd.entrySet()
                            ) {
                                cEToReserve.getValue().setReserved(true);
                            }
                            usedEvents.putAll(eventsToAdd);
                            isChargedBeforeTrip = true;
                        }
                    }

                } while (cE != null && !isChargedBeforeTrip);
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
            trip = nextVertex == null ? STsGroup.getDepoEnd() : (ServiceTripVertex) nextVertex.getValue();
        } while (deficiencyLeft < 0 && tripPrevious != STsGroup.getDepoEnd());

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

    public void releaseChargingEvents() {
        Iterator<Map.Entry<Integer, STGroupVertex>> vertexIt = this.serviceTripsWithCEsVertices.entrySet().iterator();
        while (vertexIt.hasNext()) {
            Map.Entry<Integer, STGroupVertex> vertex = vertexIt.next();
            if (vertex.getValue() instanceof ChargingEventVertex) {
                ChargingEventVertex vertexCE = (ChargingEventVertex) vertex.getValue();
                vertexIt.remove();
                vertexCE.setReserved(false);
            }
        }
    }

    public boolean isEmpty() {
        return serviceTripsWithCEsVertices.isEmpty();
    }

    public void reserveAssignedCEs() {
        for (Map.Entry<Integer, STGroupVertex> vertex : serviceTripsWithCEsVertices.entrySet()
        ) {
            if (vertex.getValue() instanceof ChargingEventVertex) {
                ((ChargingEventVertex) vertex.getValue()).setReserved(true);
            }
        }
    }

    public int getNrTrips() {
        int trips = 0;
        for (Map.Entry<Integer, STGroupVertex> vertex : serviceTripsWithCEsVertices.entrySet()
        ) {
            if (vertex.getValue() instanceof ServiceTripVertex) {
                trips++;
            }
        }
        return trips;
    }

    public ServiceTripVertex tryInsertOrExchangeTrip(ServiceTripVertex trip, LinkedList<TreeMap<Integer, ChargingEventVertex>> chargersWithChargingEvents) {
        TreeMap<Integer, STGroupVertex> cEsBackup = new TreeMap<>();
        Iterator<Map.Entry<Integer, STGroupVertex>> vertexIt = this.serviceTripsWithCEsVertices.entrySet().iterator();
        while (vertexIt.hasNext()) {
            Map.Entry<Integer, STGroupVertex> vertex = vertexIt.next();
            if (vertex.getValue() instanceof ChargingEventVertex) {
                ChargingEventVertex vertexCE = (ChargingEventVertex) vertex.getValue();
                cEsBackup.put(vertex.getKey(), vertex.getValue());
                vertexIt.remove();
                vertexCE.setReserved(false);
            }
        }
        Map.Entry<Integer, STGroupVertex> vertexBefore = serviceTripsWithCEsVertices
                .floorEntry(trip.getStart());
        ServiceTripVertex tripBefore = vertexBefore == null ? STsGroup.getDepoStart() : (ServiceTripVertex) vertexBefore.getValue();
        Map.Entry<Integer, STGroupVertex> vertexAfter = serviceTripsWithCEsVertices
                .ceilingEntry(tripBefore.getEnd());
        ServiceTripVertex tripAfter = vertexAfter == null ? STsGroup.getDepoEnd() : (ServiceTripVertex) vertexAfter.getValue();
        if (
                tripBefore.getEdgeForSubsequentTrip(trip.getId()) != null
                        && trip.getEdgeForSubsequentTrip(tripAfter.getId()) != null
        ) {
            serviceTripsWithCEsVertices.put(trip.getStart(), trip);
            double deficiencyAll = checkForBatteryDeficiency();
            if (deficiencyAll < minBatteryCapacity && !tryChargeBetweenSTs(deficiencyAll, chargersWithChargingEvents)) {
                serviceTripsWithCEsVertices.remove(trip.getStart());
                for (Map.Entry<Integer, STGroupVertex> event : cEsBackup.entrySet()) {
                    ChargingEventVertex val = (ChargingEventVertex) event.getValue();
                    val.setReserved(true);
                    serviceTripsWithCEsVertices.put(event.getKey(), event.getValue());
                }
                return trip;
            } else {
                return null;
            }
        } else if (tripBefore.getEdgeForSubsequentTrip(trip.getId()) != null
                && trip.getEdgeForSubsequentTrip(tripAfter.getId()) == null
                && serviceTripsWithCEsVertices
                .higherEntry(tripAfter.getStart()) != null
                && trip.getEdgeForSubsequentTrip(((ServiceTripVertex) serviceTripsWithCEsVertices
                .higherEntry(tripAfter.getStart()).getValue()).getId()) != null) {
            serviceTripsWithCEsVertices.remove(tripAfter.getStart());

            serviceTripsWithCEsVertices.put(trip.getStart(), trip);
            double deficiencyAll = checkForBatteryDeficiency();
            if (deficiencyAll < minBatteryCapacity && !tryChargeBetweenSTs(deficiencyAll, chargersWithChargingEvents)) {
                serviceTripsWithCEsVertices.remove(trip.getStart());
                serviceTripsWithCEsVertices.put(tripAfter.getStart(), tripAfter);
                for (Map.Entry<Integer, STGroupVertex> event : cEsBackup.entrySet()) {
                    ChargingEventVertex val = (ChargingEventVertex) event.getValue();
                    val.setReserved(true);
                    serviceTripsWithCEsVertices.put(event.getKey(), event.getValue());
                }
                return trip;
            } else {
                return tripAfter;
            }
        } else {
            for (Map.Entry<Integer, STGroupVertex> event : cEsBackup.entrySet()) {
                ChargingEventVertex val = (ChargingEventVertex) event.getValue();
                val.setReserved(true);
                serviceTripsWithCEsVertices.put(event.getKey(), event.getValue());
            }
            return trip;
        }
    }

    public LinkedList<STGroupVertex> tryInsertTrips(LinkedList<STGroupVertex> removedTrips, LinkedList<TreeMap<Integer, ChargingEventVertex>> chargersWithChargingEvents) {
        // backup of current state of removedTrips from vehicle we try to eliminate from the solution - in case we need to revert iteration
        LinkedList<STGroupVertex> failedToInsertList = new LinkedList<>();
        int removedTripsInitialSize = removedTrips.size();
        // try to find a time window between already existing trips in vehicle,
        // we don't take capacity deficiency into account at first, only if ST can be inserted respecting time restrictions

        boolean isAtLeast1Insert = false;
        // trips' lists begins and ends with depo, we can insert between depo and first
        // but no after depo (therefore size - 1)
        while (removedTrips.size() > 0) {
            final int randomIndex = random.nextInt(removedTrips.size());
            ServiceTripVertex removedTrip = (ServiceTripVertex) removedTrips.remove(randomIndex);
            TreeMap<Integer,STGroupVertex> cEsBackup = new TreeMap<>();
            Iterator<Map.Entry<Integer, STGroupVertex>> vertexIt = this.serviceTripsWithCEsVertices.entrySet().iterator();
            while (vertexIt.hasNext()){
                Map.Entry<Integer, STGroupVertex> vertex = vertexIt.next();
                if (vertex.getValue() instanceof ChargingEventVertex) {
                    ChargingEventVertex vertexCE = (ChargingEventVertex) vertex.getValue();
                    cEsBackup.put(vertex.getKey(), vertex.getValue());
                    vertexIt.remove();
                    vertexCE.setReserved(false);
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
                if (deficiencyAll < minBatteryCapacity && !tryChargeBetweenSTs(deficiencyAll, chargersWithChargingEvents)) {
                    serviceTripsWithCEsVertices.remove(removedTrip.getStart());
                    failedToInsertList.add(removedTrip);
                    for (Map.Entry<Integer, STGroupVertex> event : cEsBackup.entrySet()) {
                        ChargingEventVertex val = (ChargingEventVertex) event.getValue();
                        val.setReserved(true);
                        serviceTripsWithCEsVertices.put(event.getKey(), event.getValue());
                    }
                } else {
                    if (!isAtLeast1Insert) {
                        isAtLeast1Insert = true;
                    }
                }
            }
            else {
                for (Map.Entry<Integer, STGroupVertex> event : cEsBackup.entrySet()) {
                    ChargingEventVertex val = (ChargingEventVertex) event.getValue();
                    val.setReserved(true);
                    serviceTripsWithCEsVertices.put(event.getKey(), event.getValue());
                }
                failedToInsertList.add(removedTrip);
            }
        }
        return failedToInsertList;
    }

    public static LinkedList<STsGroup> splitRemainingTrips(LinkedList<STGroupVertex> trips,
                                                    LinkedList<TreeMap<Integer, ChargingEventVertex>> chargersWithChargingEvents) {
        LinkedList<STsGroup> vehiclesFromRemoved = new LinkedList<>();
        // place first trip from removed to new vehicle, try to assign trips
        while (!trips.isEmpty()) {
            STsGroup STsGroupFromRemoved = new STsGroup((ServiceTripVertex) trips.remove(0));
            if (!trips.isEmpty()) {
                trips = STsGroupFromRemoved.tryInsertTrips(trips, chargersWithChargingEvents);
            }
            vehiclesFromRemoved.add(STsGroupFromRemoved);
        }
        return vehiclesFromRemoved;
    }

    public TreeMap<Integer, STGroupVertex> getServiceTripsWithCEsVertices() {
        return new TreeMap<>(serviceTripsWithCEsVertices);
    }

    public boolean hasAllCEsReserved() {
        for (Map.Entry<Integer, STGroupVertex> vertex : serviceTripsWithCEsVertices.entrySet()
        ) {
            STGroupVertex item = vertex.getValue();
            if (item instanceof ChargingEventVertex) {
                if (!((ChargingEventVertex) item).isReserved()) {
                    return false;
                }
            }
        }
        return true;
    }

    public double validateGroupBattery() {
        STGroupVertex itemPrevious = STsGroup.getDepoStart();

        double battery = STsGroup.maxBatteryCapacity;
        int i = 0;
        for (Map.Entry<Integer, STGroupVertex> vertex : serviceTripsWithCEsVertices.entrySet()
        ) {
            STGroupVertex item = vertex.getValue();
            if (item instanceof ChargingEventVertex) {
                if (itemPrevious instanceof ServiceTripVertex) {
                    Edge edge = ((ServiceTripVertex) itemPrevious).getEdgeForSubsequentCE(((ChargingEventVertex) item).getIndexCharger());
                    battery -= edge.getBatteryConsumption();
                    if (battery < 0) {
                        System.out.println("st failed: " + i + " out of: " + serviceTripsWithCEsVertices.size());
                        return battery;
                    }
                } else {
                    battery += (itemPrevious.getEnd() - itemPrevious.getStart()) * ((ChargingEventVertex) itemPrevious).getChargingSpeed();
                }
            } else {
                if (itemPrevious instanceof ChargingEventVertex) {
                    battery += (itemPrevious.getEnd() - itemPrevious.getStart()) * ((ChargingEventVertex) itemPrevious).getChargingSpeed();
                    Edge edge = ((ServiceTripVertex) item).getEdgeForPreviousCE(((ChargingEventVertex) itemPrevious).getIndexCharger());
                    battery -= edge.getBatteryConsumption();
                } else {
                    Edge edge = ((ServiceTripVertex) itemPrevious).getEdgeForSubsequentTrip(((ServiceTripVertex) item).getId());
                    battery -= edge.getBatteryConsumption();
                }
                battery -= ((ServiceTripVertex) item).getConsumption();
                if (battery < 0) {
                    System.out.println("st failed: " + i + " out of: " + serviceTripsWithCEsVertices.size());
                    return battery;
                }
            }
            itemPrevious = item;
            i++;
        }
        ServiceTripVertex item = STsGroup.getDepoEnd();
        if (itemPrevious instanceof ChargingEventVertex) {
            battery += (itemPrevious.getEnd() - itemPrevious.getStart()) * ((ChargingEventVertex) itemPrevious).getChargingSpeed();
            Edge edge = item.getEdgeForPreviousCE(((ChargingEventVertex) itemPrevious).getIndexCharger());
            battery -= edge.getBatteryConsumption();
        } else {
            Edge edge = ((ServiceTripVertex) itemPrevious).getEdgeForSubsequentTrip(item.getId());
            battery -= edge.getBatteryConsumption();
        }
        battery -= item.getConsumption();
        if (battery < 0) {
            System.out.println("st failed: " + i + " out of: " + serviceTripsWithCEsVertices.size());
            return battery;
        }
        return battery;
    }
}
