import java.util.*;

public class STsGroup {
    private TreeMap<Integer, ServiceTripVertex> assignedServiceTrips;
    private TreeMap<Integer, ChargingEventVertex> assignedChargingEvents;
    final static double maxBatteryCapacity = 140;
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
        assignedServiceTrips = new TreeMap<>();
        this.assignedServiceTrips.put(initialServiceTrip.getStart(), initialServiceTrip);
        assignedChargingEvents = new TreeMap<>();
        this.random = new Random();
    }

    public STsGroup(STsGroup sTsGroup) {
        assignedServiceTrips = new TreeMap<>(sTsGroup.assignedServiceTrips);
        assignedChargingEvents = new TreeMap<>(sTsGroup.assignedChargingEvents);
        this.random = sTsGroup.random;
    }

    public String toString() {
        StringBuilder stringFinal = new StringBuilder();
//        stringFinal.append("---------\n");
        boolean isFirst = true;
        TreeMap<Integer, STGroupVertex> finalGroupSequence = new TreeMap<>(assignedServiceTrips);
        finalGroupSequence.putAll(assignedChargingEvents);

        for (Map.Entry<Integer, STGroupVertex> serviceTripData : finalGroupSequence.entrySet()) {
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
        for (Map.Entry<Integer, ServiceTripVertex> tripEntry : assignedServiceTrips.entrySet()
        ) {
            ServiceTripVertex trip = tripEntry.getValue();
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
        ServiceTripVertex trip = assignedServiceTrips.firstEntry().getValue();
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
            Map.Entry<Integer, ServiceTripVertex> nextVertex = assignedServiceTrips.higherEntry(trip.getStart());
            trip = nextVertex == null ? STsGroup.getDepoEnd() : nextVertex.getValue();
        } while (deficiencyLeft < 0 && tripPrevious != STsGroup.getDepoEnd());

        if (deficiencyLeft < 0) {
            for (Map.Entry<Integer, ChargingEventVertex> usedEvent : usedEvents.entrySet()
            ) {
                usedEvent.getValue().setReserved(false);
            }
            return false;
        }
        this.assignedChargingEvents.putAll(usedEvents);
        return true;
    }

    private void unreserveChargingEvents() {
        Iterator<Map.Entry<Integer, ChargingEventVertex>> vertexIt = this.assignedChargingEvents.entrySet().iterator();
        while (vertexIt.hasNext()) {
            Map.Entry<Integer, ChargingEventVertex> vertex = vertexIt.next();
            ChargingEventVertex vertexCE = vertex.getValue();
            vertexIt.remove();
            vertexCE.setReserved(false);
        }
    }

    public void releaseChargingEvents() {
        unreserveChargingEvents();
        assignedChargingEvents.clear();
    }

    public void reserveAssignedCEs() {
        for (Map.Entry<Integer, ChargingEventVertex> vertex : assignedChargingEvents.entrySet()
        ) {
            vertex.getValue().setReserved(true);
        }
    }

    private TreeMap<Integer, ChargingEventVertex> createAssignedCEsBackup() {
        return new TreeMap<>(assignedChargingEvents);
    }

    private void assignAndReserveCEsFromBackup(TreeMap<Integer, ChargingEventVertex> cEsBackup) {
        assignedChargingEvents = new TreeMap<>(cEsBackup);
        reserveAssignedCEs();
    }

    public int getNrTrips() {
        return assignedServiceTrips.size();
    }

    public ServiceTripVertex tryInsertOrReplaceTrip(ServiceTripVertex trip, LinkedList<TreeMap<Integer, ChargingEventVertex>> chargersWithChargingEvents, boolean tryReplace) {
        Map.Entry<Integer, ServiceTripVertex> vertexBefore = assignedServiceTrips
                .floorEntry(trip.getStart());
        ServiceTripVertex tripBefore = vertexBefore == null ? STsGroup.getDepoStart() : vertexBefore.getValue();
        Map.Entry<Integer, ServiceTripVertex> vertexAfter = assignedServiceTrips
                .ceilingEntry(tripBefore.getEnd());
        ServiceTripVertex tripAfter = vertexAfter == null ? STsGroup.getDepoEnd() : vertexAfter.getValue();
        if (
                tripBefore.getEdgeForSubsequentTrip(trip.getId()) != null
                        && trip.getEdgeForSubsequentTrip(tripAfter.getId()) != null
        ) {
            unreserveChargingEvents();
            TreeMap<Integer, ChargingEventVertex> assignedCEsBackup = createAssignedCEsBackup();
            assignedServiceTrips.put(trip.getStart(), trip);
            double deficiencyAll = checkForBatteryDeficiency();
            if (deficiencyAll < minBatteryCapacity && !tryChargeBetweenSTs(deficiencyAll, chargersWithChargingEvents)) {
                assignedServiceTrips.remove(trip.getStart());
                assignAndReserveCEsFromBackup(assignedCEsBackup);
                return trip;
            } else {
                return null;
            }
        } else if (tryReplace && tripBefore.getEdgeForSubsequentTrip(trip.getId()) != null
                && trip.getEdgeForSubsequentTrip(tripAfter.getId()) == null
                && assignedServiceTrips
                .higherEntry(tripAfter.getStart()) != null
                && trip.getEdgeForSubsequentTrip(assignedServiceTrips
                .higherEntry(tripAfter.getStart()).getValue().getId()) != null) {
            return tryReplaceTrip(trip, chargersWithChargingEvents, tripAfter);
        }else if (tryReplace && tripBefore.getEdgeForSubsequentTrip(trip.getId()) == null
                && trip.getEdgeForSubsequentTrip(tripAfter.getId()) != null
                && assignedServiceTrips
                .lowerEntry(tripBefore.getStart()) != null
                && trip.getEdgeForSubsequentTrip(assignedServiceTrips
                .lowerEntry(tripBefore.getStart()).getValue().getId()) != null) {
            return tryReplaceTrip(trip, chargersWithChargingEvents, tripBefore);
        } else {
            return trip;
        }
    }

    private ServiceTripVertex tryReplaceTrip(ServiceTripVertex tripToInsert, LinkedList<TreeMap<Integer, ChargingEventVertex>> chargersWithChargingEvents, ServiceTripVertex tripToReplace) {
        unreserveChargingEvents();
        TreeMap<Integer, ChargingEventVertex> assignedCEsBackup = createAssignedCEsBackup();
        assignedServiceTrips.remove(tripToReplace.getStart());
        assignedServiceTrips.put(tripToInsert.getStart(), tripToInsert);
        double deficiencyAll = checkForBatteryDeficiency();
        if (deficiencyAll < minBatteryCapacity && !tryChargeBetweenSTs(deficiencyAll, chargersWithChargingEvents)) {
            assignedServiceTrips.remove(tripToInsert.getStart());
            assignedServiceTrips.put(tripToReplace.getStart(), tripToReplace);
            assignAndReserveCEsFromBackup(assignedCEsBackup);
            return tripToInsert;
        } else {
            return tripToReplace;
        }
    }

    public LinkedList<ServiceTripVertex> tryInsertTrips(LinkedList<ServiceTripVertex> removedTrips, LinkedList<TreeMap<Integer, ChargingEventVertex>> chargersWithChargingEvents) {
        LinkedList<ServiceTripVertex> failedToInsertList = new LinkedList<>();
        while (removedTrips.size() > 0) {
            final int randomIndex = random.nextInt(removedTrips.size());
            ServiceTripVertex removedTrip = removedTrips.remove(randomIndex);
            ServiceTripVertex returnedFromInsert = tryInsertOrReplaceTrip(removedTrip, chargersWithChargingEvents, false);
            if (returnedFromInsert != null) {
                failedToInsertList.add(removedTrip);
            }
        }
        return failedToInsertList;
    }

    public static LinkedList<STsGroup> splitRemainingTrips(LinkedList<ServiceTripVertex> trips,
                                                    LinkedList<TreeMap<Integer, ChargingEventVertex>> chargersWithChargingEvents) {
        LinkedList<STsGroup> vehiclesFromRemoved = new LinkedList<>();
        while (!trips.isEmpty()) {
            STsGroup STsGroupFromRemoved = new STsGroup(trips.remove(0));
            if (!trips.isEmpty()) {
                trips = STsGroupFromRemoved.tryInsertTrips(trips, chargersWithChargingEvents);
            }
            vehiclesFromRemoved.add(STsGroupFromRemoved);
        }
        return vehiclesFromRemoved;
    }

    public TreeMap<Integer, STGroupVertex> getServiceTripsWithCEsVertices() {
        TreeMap<Integer, STGroupVertex> sequence = new TreeMap<>(assignedServiceTrips);
        sequence.putAll(assignedChargingEvents);
        return sequence;
    }

    public TreeMap<Integer, ServiceTripVertex> getServiceTrips() {
        return new TreeMap<>(assignedServiceTrips);
    }

    public boolean hasAllCEsReserved() {
        for (Map.Entry<Integer, ChargingEventVertex> vertex : assignedChargingEvents.entrySet()
        ) {
            ChargingEventVertex item = vertex.getValue();
            if (!item.isReserved()) {
                return false;
            }
        }
        return true;
    }

    public double validateGroupBattery() {
        STGroupVertex itemPrevious = STsGroup.getDepoStart();
        TreeMap<Integer, STGroupVertex> serviceTripsWithCEsVertices = new TreeMap<>(assignedServiceTrips);
        serviceTripsWithCEsVertices.putAll(assignedChargingEvents);
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
