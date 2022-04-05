import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class SimulatedAnnealing {
    private static final int REPLICATIONS = 2;

    private Solution solution;
    private long bestSolutionTime;
    private final Random random;
    private final boolean shouldReheat;
    private final int maxComputingTimeSeconds;
    private long totalTime;

    //starting temperature
    private final double maxT;
    //temperature modifier
    private final double tBeta;
    //max iterations at given temperature
    private final int maxQ;

    public SimulatedAnnealing(boolean shouldReheat, int maxComputingTimeSeconds, double maxT, double tBeta, int maxQ, String fileTrips, String fileChargers,
                              String fileEnergySTToST, String fileEnergySTToCE, String fileEnergyCEToST, String fileTimeSTToST,
                              String fileTimeSTToCE, String fileTimeCEToST) throws FileNotFoundException {
        this.shouldReheat = shouldReheat;
        this.maxComputingTimeSeconds = maxComputingTimeSeconds;
        totalTime = 0;

        solution = new Solution(fileTrips, fileChargers, fileEnergySTToST, fileEnergySTToCE, fileEnergyCEToST, fileTimeSTToST,
                fileTimeSTToCE, fileTimeCEToST);

        this.random = new Random();

        this.maxT = maxT;
        this.tBeta = tBeta;
        this.maxQ = maxQ;
    }

    public void runSimulatedAnnealing() {
            long startTime = System.currentTimeMillis();
            // currently 10 min
            long endAfterTime = maxComputingTimeSeconds * 1000 + startTime;

            Solution solutionCurrent = solution;
            boolean isAcceptedSolutionOnTemperature;
            boolean isFoundBetterSinceLastReheating = false;
            boolean shouldContinueSA;
            double currentTemperature = maxT;
            do {
                shouldContinueSA = false;
                isAcceptedSolutionOnTemperature = false;
                for (int q = 0; q < maxQ; q++) {
                    Solution nextSolution = solutionCurrent.findNext();
//                    System.out.println("new solution: " + nextSolution.getsTsGroups().size());
//                    System.out.println("new solution: " + nextSolution);
                    if (nextSolution == null) {
                        return;
                    }
                    if (nextSolution.getsTsGroups().size() <= solutionCurrent.getsTsGroups().size()) {
//                        System.out.println("accepted better: " + nextSolution.getsTsGroups().size());
                        if (!isAcceptedSolutionOnTemperature) {
                            isAcceptedSolutionOnTemperature = true;
                        }
                        solutionCurrent = nextSolution;
                        if (solutionCurrent.getsTsGroups().size() < solution.getsTsGroups().size()) {
                            solution = solutionCurrent;
                            bestSolutionTime = System.currentTimeMillis() - startTime;
                            if (!isFoundBetterSinceLastReheating) {
                                isFoundBetterSinceLastReheating = true;
                            }
                        }
                    } else {
                        double pAcceptNext = Math.exp(
                                -((nextSolution.getsTsGroups().size() - solutionCurrent.getsTsGroups().size())
                                        / currentTemperature)
                        );
                        double generatedValue = random.nextDouble();
                        if (generatedValue <= pAcceptNext) {
//                            System.out.println("accepted worse: " + nextSolution.getsTsGroups().size());
                            if (!isAcceptedSolutionOnTemperature) {
                                isAcceptedSolutionOnTemperature = true;
                            }
                            solutionCurrent = nextSolution;
                        } else {
//                            System.out.println("declined worse: " + nextSolution.getsTsGroups().size());
                            solutionCurrent.resetChargersForSolution();
                        }
                    }
                }
                System.out.println("temp: "+ currentTemperature);
                System.out.println(solution.getsTsGroups().size());
                currentTemperature /= 1 + tBeta * currentTemperature;
                if (isAcceptedSolutionOnTemperature && currentTemperature > 0.001) {
                    shouldContinueSA = true;
                } else if (isFoundBetterSinceLastReheating && shouldReheat) {
                    currentTemperature = maxT;
                    isFoundBetterSinceLastReheating = false;
                    shouldContinueSA = true;
//                    System.out.println("reheating");
                }
            } while (shouldContinueSA && System.currentTimeMillis() < endAfterTime);
        totalTime = System.currentTimeMillis() - startTime;
        for (STsGroup group : solution.getsTsGroups()
        ) {
            group.reserveAssignedCEs();
        }
        int i = 0;
        for (STsGroup group : solution.getsTsGroups()
             ) {
            double batteryValidation = group.validateGroupBatteryState();
            if (batteryValidation < 0) {
                System.out.println("invalid group: "+i+" battery: "+ batteryValidation);
            }
            i++;
        }
//        new Thread(() -> {
//         GUISolution guiSolution = new GUISolution(solution);
//        }).start();
    }

    public Solution getSolution() {
        return solution;
    }

    public String toString() {
//        return "solution length: " + solution.getVehicles().size();
        return  "\nsolution length: " + solution.getsTsGroups().size() + "\n" + "total time seconds: " + (totalTime / 1000.0) +"\n"
                + "solution reached in time seconds: " + (bestSolutionTime / 1000.0) +"\n" + solution;
    }

    public static void runSimulation(boolean shouldReheat, int maxComputingTimeSeconds, double maxT, double tBeta, int maxQ, String fileTrips, String fileChargers,
                                     String fileEnergySTToST, String fileEnergySTToCE, String fileEnergyCEToST, String fileTimeSTToST,
                                     String fileTimeSTToCE, String fileTimeCEToST, String resultFileName) throws FileNotFoundException {
        System.out.println("\n\nstarting new run ");
        File file = new File(resultFileName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileWriter myWriter = null;
        try {
            myWriter = new FileWriter(resultFileName);
            myWriter.write("shouldReheat: "+shouldReheat+" maxComputingTimeSeconds: " + maxComputingTimeSeconds + " maxT: " + maxT + " tBeta: " + tBeta + " maxQ: " + maxQ);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < REPLICATIONS; i++) {
            System.out.println("replication: "+i);
            SimulatedAnnealing saAlgorithm = null;
            saAlgorithm = new SimulatedAnnealing(shouldReheat, maxComputingTimeSeconds, maxT, tBeta, maxQ,
                    fileTrips,
                    fileChargers,
                    fileEnergySTToST,
                    fileEnergySTToCE,
                    fileEnergyCEToST,
                    fileTimeSTToST,
                    fileTimeSTToCE,
                    fileTimeCEToST
            );
            saAlgorithm.runSimulatedAnnealing();
//            System.out.println(saAlgorithm);
            try {
                myWriter.write("\n\nReplication nr.: " + (i + 1));
                myWriter.write(String.valueOf(saAlgorithm));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
