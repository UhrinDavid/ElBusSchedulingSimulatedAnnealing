import java.util.ArrayList;

public class Vehicle {
    private ArrayList<ServiceTrip> serviceTrips;
    private ArrayList<ChargingEvent> assignedChargingEvents;
    final static double maxBatteryCapacity = 140;
    final static double minBatteryCapacity = 0;

    public Vehicle(ServiceTrip depoStart, ServiceTrip depoEnd) {
        serviceTrips = new ArrayList<>();
        assignedChargingEvents = new ArrayList<>();
        this.serviceTrips.add(depoStart);
        this.serviceTrips.add(depoEnd);
    }

    public Vehicle(Vehicle vehicle) {
        serviceTrips = new ArrayList<>(vehicle.serviceTrips);
        assignedChargingEvents = new ArrayList<>();
        for (ChargingEvent event : vehicle.assignedChargingEvents
             ) {
            assignedChargingEvents.add(new ChargingEvent(event));
        }
    }


    public void setAssignedChargingEvents(ArrayList<ChargingEvent> events) {
        this.assignedChargingEvents = new ArrayList<ChargingEvent>(events);
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

    public ArrayList<ChargingEvent> getChargingEvents() {return assignedChargingEvents;}

    public String toString() {
        StringBuilder stringFinal = new StringBuilder();
        stringFinal.append("---------\n");
        for ( ServiceTrip serviceTrip : serviceTrips ) {
            stringFinal.append("Service trip: ").append(serviceTrip.getId()).append("\n");
        }
        for ( ChargingEvent c : assignedChargingEvents ) {
            stringFinal.append("Charging event: ").append(c.getMatrixIndex()).append("\n");
        }
        stringFinal.append("---------\n");
        return stringFinal.toString();
    }

    public boolean hasServiceTrips() {
        return serviceTrips.size() > 2;
    }

    public double checkForBatteryDeficiency(Edge[][] matrixSTtoST) {
        // check if there is enough battery capacity, if not, try to insert charging events
        double deficiencyAll = Vehicle.maxBatteryCapacity;
        int serviceTripIndex = 1;
        // create deficiency structure
        while (serviceTripIndex < serviceTrips.size()) {
            ServiceTrip trip = serviceTrips.get(serviceTripIndex);
            ServiceTrip tripPrevious = serviceTrips.get(serviceTripIndex - 1);
            deficiencyAll -= matrixSTtoST[tripPrevious.getMatrixIndex()][trip.getMatrixIndex()].getBatteryConsumption()
                    + trip.getConsumption();
            serviceTripIndex++;
        }
        return deficiencyAll;
    }

    public boolean tryChargeBetweenSTs(double deficiencyAll,  Edge[][] matrixSTtoST,  Edge[][] matrixSTtoCE, Edge[][] matrixCEtoST, ArrayList<ChargingEvent> chargingEvents) {
        double deficiencyLeft = deficiencyAll;
        double currentBatteryState = Vehicle.maxBatteryCapacity;
        int tripIndexCorrection = 1;
        ArrayList<ChargingEvent> usedEvents = new ArrayList<>();
        while (tripIndexCorrection < serviceTrips.size() && deficiencyLeft < 0) {
            ServiceTrip trip = serviceTrips.get(tripIndexCorrection);
            ServiceTrip tripPrevious = serviceTrips.get(tripIndexCorrection - 1);
            final Edge edgeSTtoST = matrixSTtoST[tripPrevious.getMatrixIndex()][trip.getMatrixIndex()];
            final double tripConsumption = edgeSTtoST.getBatteryConsumption()
                    + trip.getConsumption();
            // find suitable CE
            // get first possible index on charger
            int indexCE = 0;
            boolean isFoundSuitableCharger = false;
            while (indexCE < chargingEvents.size() && !isFoundSuitableCharger
            ) {
                ChargingEvent cE = chargingEvents.get(indexCE);
                final Edge edgeSTtoCE = matrixSTtoCE[tripPrevious.getMatrixIndex()][cE.getIndexCharger()];
                final Edge edgeCEtoST = matrixCEtoST[cE.getIndexCharger()][trip.getMatrixIndex()];
                // we have time to make trip to charger and back to next ST, also enough battery to get to charger, event is not reserved
                if (!cE.getIsReserved() && edgeSTtoCE != null
                        && edgeCEtoST != null) {
                    final double consumptionSTtoCE = edgeSTtoCE.getBatteryConsumption();
                    if (currentBatteryState - consumptionSTtoCE > 0) {
                        final int indexFirstInsertableCE = indexCE;
                        boolean shouldCheckNextEventOnCharger;
                        final double maxTimeBetweenSTs = trip.getStart() - tripPrevious.getEnd() - edgeSTtoCE.getTimeDistance() - edgeCEtoST.getTimeDistance();
                        final double consumptionDuringEvent = edgeSTtoCE.getBatteryConsumption() + edgeCEtoST.getBatteryConsumption();
                        double potentialBatteryState;
                        double maxChargingTimeOverall = 0.0;
                        do {
                            // check if there is succeeding event on same charger
                            final boolean isNextEventOnCharger = chargingEvents.size() > indexCE + 1
                                    && chargingEvents.get(indexCE + 1).getIndexCharger() == chargingEvents.get(indexCE).getIndexCharger();
                            // max charge until start of next event on charger, if event on charger is last
                            // we can charge until start of next trip
                            final double maxChargingTimeOnEvent = isNextEventOnCharger
                                    ? chargingEvents.get(indexCE + 1).getStartTime() - chargingEvents.get(indexCE).getStartTime()
                                    : Double.POSITIVE_INFINITY;
                            maxChargingTimeOverall += maxChargingTimeOnEvent;
                            final double actualMaxChargingTimeBetweenSTs = Math.min(maxTimeBetweenSTs, maxChargingTimeOverall);
                            final double maxCharge = actualMaxChargingTimeBetweenSTs * chargingEvents.get(indexCE).getChargingSpeed();
                            potentialBatteryState = Math.min(currentBatteryState - consumptionDuringEvent + maxCharge, Vehicle.maxBatteryCapacity);
                            if (isNextEventOnCharger && potentialBatteryState < Vehicle.maxBatteryCapacity) {
                                shouldCheckNextEventOnCharger = true;
                            } else {
                                shouldCheckNextEventOnCharger = false;
                            }
                        } while (shouldCheckNextEventOnCharger && matrixCEtoST[chargingEvents.get(++indexCE).getIndexCharger()][trip.getMatrixIndex()] != null
                                && !chargingEvents.get(indexCE).getIsReserved());
                        // check if we charge more than minimum required for next trip / more than is consumed to and from event
                        if (potentialBatteryState - trip.getConsumption() > 0
                                && potentialBatteryState - trip.getConsumption() > currentBatteryState - tripConsumption) {
                            final double currBeforeTripStart = currentBatteryState - edgeSTtoST.getBatteryConsumption();
                            final double deficiencyModifier = -currBeforeTripStart + potentialBatteryState;
                            deficiencyLeft += deficiencyModifier;
                            currentBatteryState = potentialBatteryState - trip.getConsumption();
                            for (int i = indexFirstInsertableCE; i <= indexCE; i++) {
                                usedEvents.add(chargingEvents.get(i));
                                chargingEvents.get(i).setIsReserved(true);
                                this.assignedChargingEvents.add(chargingEvents.get(i));
                            }
                            isFoundSuitableCharger = true;
                        }
                    }
                }
                indexCE++;
            }
            if (!isFoundSuitableCharger) {
                currentBatteryState -= tripConsumption;
                if (currentBatteryState < 0) {
                    this.assignedChargingEvents.clear();
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
            this.assignedChargingEvents.clear();
            for (ChargingEvent usedEvent : usedEvents
            ) {
                usedEvent.setIsReserved(false);
            }
            return false;
        }
        return true;
    }

    public ArrayList<ServiceTrip> tryInsertTrips(ArrayList<ServiceTrip> removedTripsPa, Edge[][] matrixSTtoST, Edge[][] matrixSTtoCE, Edge[][] matrixCEtoST, ArrayList<ChargingEvent> chargingEvents) {
        // backup of current state of removedTrips from vehicle we try to eliminate from the solution - in case we need to revert iteration
        ArrayList<ServiceTrip> removedTrips = new ArrayList<>(removedTripsPa);


        // try to find a time window between already existing trips in vehicle,
        // we don't take capacity deficiency into account at first, only if ST can be inserted respecting time restrictions

        int indexRemovedTrips = 0;
        int indexVehicleTrips = 0;
        // flag is true if we add at least one new ST to the current vehicle
        boolean isNewAdded = false;

        // trips' lists begins and ends with depo, we can insert between depo and first
        // but no after depo (therefore size - 1)
        while (indexVehicleTrips < serviceTrips.size() - 1
                && indexRemovedTrips < removedTrips.size()) {
            ServiceTrip currentTrip = serviceTrips.get(indexVehicleTrips);
            ServiceTrip nextTrip = serviceTrips.get(indexVehicleTrips + 1);
            // find possible candidate from removedTrips to insert after ST at current index
            // try to find index in removedTrips, whose corresponding CE starts after the end of current trip
            while (indexRemovedTrips < removedTrips.size()
                    && currentTrip.getEnd() >= removedTrips.get(indexRemovedTrips).getStart()) {
                indexRemovedTrips++;
            }

            // check if indexRemovedTrips is within removedTrips size, if not we don't have a candidate
            if (indexRemovedTrips < removedTrips.size()) {
                ServiceTrip removedTrip = removedTrips.get(indexRemovedTrips);
                // check if we can insert removedTrips[indexRemovedTrips] between current and next trip respecting time restrictions
                // if start of removed is greater or equal than end of current + time from ST to ST
                // && if start of next is greater or equal than end of removed + time from ST to ST
                if (matrixSTtoST[currentTrip.getMatrixIndex()][removedTrip.getMatrixIndex()] != null
                        && matrixSTtoST[removedTrip.getMatrixIndex()][nextTrip.getMatrixIndex()] != null) {
                    // removedTrip is added between current and next, battery deficiency has to be checked after
                    // adding all removedTrips to current vehicle that fit timewise, remove removedTrip from removedTrips
                    removedTrips.remove(removedTrip);
                    serviceTrips.add(++indexVehicleTrips, removedTrip);
                    if (!isNewAdded) {
                        isNewAdded = true;
                    }
                    continue;
                }
            }
            indexVehicleTrips++;
        }

        // check if any of removedTrips were added to current vehicle
        if (isNewAdded) {
            // release CEs of vehicle where we try to add removed ST's
            for (ChargingEvent event : this.assignedChargingEvents) {
                event.setIsReserved(false);
            }
            this.assignedChargingEvents.clear();

            // check if there is enough battery capacity, if not, try to insert charging events
            double deficiencyAll = checkForBatteryDeficiency(matrixSTtoST);

            // we need to insert charging events
            if (deficiencyAll < 0) {
                if (tryChargeBetweenSTs(deficiencyAll, matrixSTtoST, matrixSTtoCE, matrixCEtoST, chargingEvents)) {
                    return removedTrips;
                } else {
                    return removedTripsPa;
                }
            }
            return removedTrips;
        }
        return removedTripsPa;
    }
}
