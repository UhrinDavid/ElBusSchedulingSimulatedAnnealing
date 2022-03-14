import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class SimulatedAnnealing {
    private Solution solution;
    // TODO: use random as singleton class
    private Random random;
    private boolean shouldReheat;
    private int maxComputingTimeSeconds;
    private long totalTime;

    //starting temperature
    private double maxT;
    //temperature modifier
    private double tBeta;
    //max iterations at given temperature
    private int maxQ;

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
//                    System.out.println("new solution: " + nextSolution.getVehicles().size());
//                System.out.println("new solution: " + nextSolution);
                    if (nextSolution == null) {
                        return;
                    }
                    if (nextSolution.getsTsGroups().size() <= solutionCurrent.getsTsGroups().size()) {
                        if (!isAcceptedSolutionOnTemperature) {
                            isAcceptedSolutionOnTemperature = true;
                        }
                        solutionCurrent = nextSolution;
                        if (solutionCurrent.getsTsGroups().size() < solution.getsTsGroups().size()) {
                            solution = solutionCurrent;
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
//                            System.out.println("accepted worse: " + nextSolution.getVehicles().size());
                            if (!isAcceptedSolutionOnTemperature) {
                                isAcceptedSolutionOnTemperature = true;
                            }
                            solutionCurrent = nextSolution;
                        } else {
                            solutionCurrent.resetChargersForSolution();
                        }
                    }
                }
                System.out.println("temp: "+ currentTemperature);
                System.out.println(solution.getsTsGroups().size());
                currentTemperature /= 1 + tBeta * currentTemperature;
                if (isAcceptedSolutionOnTemperature && currentTemperature > 0.1) {
                    shouldContinueSA = true;
                } else if (isFoundBetterSinceLastReheating && shouldReheat) {
                    currentTemperature = maxT;
                    isFoundBetterSinceLastReheating = false;
                    shouldContinueSA = true;
                    System.out.println("reheating");
                }
            } while (shouldContinueSA && System.currentTimeMillis() < endAfterTime);
            totalTime = System.currentTimeMillis();
    }

    public Solution getSolution() {
        return solution;
    }

    public String toString() {
//        return "solution length: " + solution.getVehicles().size();
        return "solution: \n" + solution + "solution length: \n" + solution.getsTsGroups().size() + "\n";
    }
}
