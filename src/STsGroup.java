import java.util.*;

public class STsGroup {
    private ArrayList<ServiceTrip> serviceTrips;
    private LinkedList<ChargingEvent> assignedChargingEvents;
    final static double maxBatteryCapacity = 140;
    final static double minBatteryCapacity = 0;
    private Random random;

    public STsGroup(ServiceTrip depoStart, ServiceTrip depoEnd) {
        serviceTrips = new ArrayList<>();
        assignedChargingEvents = new LinkedList<>();
        this.serviceTrips.add(depoStart);
        this.serviceTrips.add(depoEnd);
        this.random = new Random();
    }

    public STsGroup(STsGroup STsGroup) {
        serviceTrips = new ArrayList<>(STsGroup.serviceTrips);
        this.assignedChargingEvents = new LinkedList<>(STsGroup.assignedChargingEvents);
        this.random = new Random();
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

    public void addServiceTrips (ArrayList<ServiceTrip> trips) {
        serviceTrips = trips;
    }

    public void removeServiceTrip(ServiceTrip serviceTrip) {
        serviceTrips.remove(serviceTrip);
    }

    public ArrayList<ServiceTrip> getServiceTrips() {
        return serviceTrips;
    }

    public LinkedList<ChargingEvent> getChargingEvents() {return assignedChargingEvents;}

    public String toString() {
        StringBuilder stringFinal = new StringBuilder();
        stringFinal.append("---------\n");
        stringFinal.append("trips: ").append(serviceTrips.size() - 2).append("\n");
        for ( ServiceTrip serviceTrip : serviceTrips ) {
            stringFinal.append("Service trip: ").append(serviceTrip.getId()).append("\n");
        }
        for ( ChargingEvent c : assignedChargingEvents ) {

            stringFinal.append("Charging event: ").append(c.getIndexCharger() + "_"+c.getIndexChargingEvent()).append(" Is reserved: " + (c.getIsReserved())).append("\n");
        }
        stringFinal.append("---------\n");
        return stringFinal.toString();
    }

    public boolean hasServiceTrips() {
        return serviceTrips.size() > 2;
    }

    public double checkForBatteryDeficiency(Edge[][] matrixSTtoST) {
        // check if there is enough battery capacity, if not, try to insert charging events
        double deficiencyAll = STsGroup.maxBatteryCapacity;
        int serviceTripIndex = 1;
        // create deficiency structure
        while (serviceTripIndex < serviceTrips.size()) {
            ServiceTrip trip = serviceTrips.get(serviceTripIndex);
            ServiceTrip tripPrevious = serviceTrips.get(serviceTripIndex - 1);
            deficiencyAll -= matrixSTtoST[tripPrevious.getIndex()][trip.getIndex()].getBatteryConsumption()
                    + trip.getConsumption();
            serviceTripIndex++;
        }
        return deficiencyAll;
    }

    public boolean tryChargeBetweenSTs(double deficiencyAll,  Edge[][] matrixSTtoST,  Edge[][] matrixSTtoCE, Edge[][] matrixCEtoST, LinkedList<LinkedList<ChargingEvent>> chargersWithChargingEvents) {
        double deficiencyLeft = deficiencyAll;
        double currentBatteryState = STsGroup.maxBatteryCapacity;
        int tripIndexCorrection = 1;
        ArrayList<ChargingEvent> usedEvents = new ArrayList<>();
        while (tripIndexCorrection < serviceTrips.size() && deficiencyLeft < 0) {
            ServiceTrip trip = serviceTrips.get(tripIndexCorrection);
            ServiceTrip tripPrevious = serviceTrips.get(tripIndexCorrection - 1);
            final Edge edgeSTtoST = matrixSTtoST[tripPrevious.getIndex()][trip.getIndex()];
            final double tripConsumption = edgeSTtoST.getBatteryConsumption()
                    + trip.getConsumption();
            // find suitable CE
            // get first possible index on charger
            Iterator<LinkedList<ChargingEvent>> chargerIt = chargersWithChargingEvents.iterator();
            boolean isChargedBeforeTrip = false;
            while (chargerIt.hasNext() && !isChargedBeforeTrip
            ) {
                LinkedList<ChargingEvent> chargingEvents = chargerIt.next();
                ListIterator<ChargingEvent> chargingEventIt = chargingEvents.listIterator();
                while (chargingEventIt.hasNext() && !isChargedBeforeTrip) {
                    ChargingEvent cE = chargingEventIt.next();
                    final Edge edgeSTtoCE = matrixSTtoCE[tripPrevious.getIndex()][cE.getIndexCharger()];
                    final Edge edgeCEtoST = matrixCEtoST[cE.getIndexCharger()][trip.getIndex()];
                    // we have time to make trip to charger and back to next ST, also enough battery to get to charger, event is not reserved
                    if (!cE.getIsReserved() && cE.getEndTime() > tripPrevious.getEnd() + edgeSTtoCE.getTimeDistance()
                            && cE.getStartTime() + edgeCEtoST.getTimeDistance() < trip.getStart()) {
                        final double consumptionSTtoCE = edgeSTtoCE.getBatteryConsumption();

                        if (currentBatteryState - consumptionSTtoCE > 0) {
                            final ChargingEvent firstInsertableCE = cE;
                            boolean shouldCheckNextEventOnCharger;
                            final double maxTimeBetweenSTs = trip.getStart() - tripPrevious.getEnd() - edgeSTtoCE.getTimeDistance() - edgeCEtoST.getTimeDistance();
                            final double consumptionDuringEvent = edgeSTtoCE.getBatteryConsumption() + edgeCEtoST.getBatteryConsumption();
                            double potentialBatteryState;
                            double maxChargingTimeOnChargingSequence = 0.0;
                            double maxChargeOnChargingSequence = 0.0;
                            do {
                                // check if there is succeeding event on same charger
                                // max charge until start of next event on charger, if event on charger is last
                                // we can charge until start of next trip
                                final double maxChargingTimeOnEvent = cE.getEndTime() - cE.getStartTime();
                                final double actualMaxChargingTimeBetweenSTs = Math.min(maxTimeBetweenSTs, maxChargingTimeOnChargingSequence + maxChargingTimeOnEvent);
                                maxChargeOnChargingSequence += (actualMaxChargingTimeBetweenSTs - maxChargeOnChargingSequence) * cE.getChargingSpeed();
                                maxChargingTimeOnChargingSequence = actualMaxChargingTimeBetweenSTs;
                                // end charging if:
                                //      - capacity of ST's battery is at full state
                                //      - total deficiency reaches zero
                                potentialBatteryState = Math.min(currentBatteryState - consumptionDuringEvent + maxChargeOnChargingSequence, STsGroup.maxBatteryCapacity);
                                final double potentialDeficiency = deficiencyLeft + potentialBatteryState - currentBatteryState + edgeSTtoST.getBatteryConsumption();
                                if (chargingEventIt.hasNext() && potentialBatteryState < STsGroup.maxBatteryCapacity && potentialDeficiency < 0) {
                                    cE = chargingEventIt.next();
                                    shouldCheckNextEventOnCharger = true;
                                } else {
                                    shouldCheckNextEventOnCharger = false;
                                }
                            } while (shouldCheckNextEventOnCharger && cE.getStartTime() + edgeCEtoST.getTimeDistance() < trip.getStart()
                                    && !cE.getIsReserved());
                            // check if we charge more than minimum required for next trip / more than is consumed to and from event
                            if (potentialBatteryState - trip.getConsumption() > 0
                                    && potentialBatteryState - trip.getConsumption() > currentBatteryState - tripConsumption) {
                                final double deficiencyModifier = -currentBatteryState + potentialBatteryState + edgeSTtoST.getBatteryConsumption();
                                deficiencyLeft += deficiencyModifier;
                                currentBatteryState = potentialBatteryState - trip.getConsumption();
                                ChargingEvent cEReserving = cE;
                                if (!cEReserving.getIsReserved()) {
                                    usedEvents.add(cEReserving);
                                    cEReserving.setIsReserved(true);
                                }
                                while (cEReserving != firstInsertableCE) {
                                    cEReserving = chargingEventIt.previous();
                                    usedEvents.add(cEReserving);
                                    cEReserving.setIsReserved(true);
                                };
                                isChargedBeforeTrip = true;
                            }
                        }
                    }
                }

            }
            if (!isChargedBeforeTrip) {
                currentBatteryState -= tripConsumption;
                if (currentBatteryState < 0) {
                    for (ChargingEvent usedEvent : usedEvents
                    ) {
                        usedEvent.setIsReserved(false);
                    }
                    return false;
                }
            }
            tripIndexCorrection++;
        }

        if (deficiencyLeft < 0) {
            for (ChargingEvent usedEvent : usedEvents
            ) {
                usedEvent.setIsReserved(false);
            }
            return false;
        }
        this.assignedChargingEvents = new LinkedList<>(usedEvents);
        return true;
    }

    public ArrayList<ServiceTrip> tryInsertTrips(ArrayList<ServiceTrip> removedTripsPa, Edge[][] matrixSTtoST, Edge[][] matrixSTtoCE, Edge[][] matrixCEtoST, LinkedList<LinkedList<ChargingEvent>> chargersWithChargingEvents) {
        int tripsBefore = serviceTrips.size();
        // backup of current state of removedTrips from vehicle we try to eliminate from the solution - in case we need to revert iteration
        ArrayList<ServiceTrip> removedTrips = new ArrayList<>(removedTripsPa);
        ArrayList<ServiceTrip> failedToInsertList = new ArrayList<>();

        // try to find a time window between already existing trips in vehicle,
        // we don't take capacity deficiency into account at first, only if ST can be inserted respecting time restrictions

        // trips' lists begins and ends with depo, we can insert between depo and first
        // but no after depo (therefore size - 1)
        while (removedTrips.size() > 0) {
            final int randomIndex = random.nextInt(removedTrips.size());
            ServiceTrip removedTrip = removedTrips.remove(randomIndex);
            boolean isAdded = false;
            int indexVehicleTrips = 0;
            while (indexVehicleTrips < serviceTrips.size() - 1 && !isAdded) {
                ServiceTrip currentTrip = serviceTrips.get(indexVehicleTrips);
                ServiceTrip nextTrip = serviceTrips.get(indexVehicleTrips + 1);

                // check if we can insert removedTrips[indexRemovedTrips] between current and next trip respecting time restrictions
                // if start of removed is greater or equal than end of current + time from ST to ST
                // && if start of next is greater or equal than end of removed + time from ST to ST
                if (matrixSTtoST[currentTrip.getIndex()][removedTrip.getIndex()] != null
                        && matrixSTtoST[removedTrip.getIndex()][nextTrip.getIndex()] != null) {
                    isAdded = true;
                    // removedTrip is added between current and next, battery deficiency has to be checked after
                    // adding all removedTrips to current vehicle that fit timewise, remove removedTrip from removedTrips
                    // release CEs of vehicle where we try to add removed ST
                    ArrayList<ChargingEvent> cEsBackup = new ArrayList<>(assignedChargingEvents);
                    for (ChargingEvent event : this.assignedChargingEvents) {
                        event.setIsReserved(false);
                    }
                    this.assignedChargingEvents.clear();
                    serviceTrips.add(++indexVehicleTrips, removedTrip);
                    // check if there is enough battery capacity, if not, try to insert charging events
                    double deficiencyAll = checkForBatteryDeficiency(matrixSTtoST);

                    // we need to insert charging events
                    if (deficiencyAll < 0 && !tryChargeBetweenSTs(deficiencyAll, matrixSTtoST, matrixSTtoCE, matrixCEtoST, chargersWithChargingEvents)) {

                            serviceTrips.remove(indexVehicleTrips--);
                            failedToInsertList.add(removedTrip);
                            assignedChargingEvents = new LinkedList<>(cEsBackup);
                            for (ChargingEvent event : this.assignedChargingEvents) {
                                event.setIsReserved(true);
                            }
                    }
                }
                indexVehicleTrips++;
            }
            if (!isAdded) {
                failedToInsertList.add(removedTrip);
            }
        }
        int tripDiff = serviceTrips.size() - tripsBefore;
        return failedToInsertList;
    }
}
