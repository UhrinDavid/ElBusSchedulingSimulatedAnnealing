import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class SimulatedAnnealing {

    private ArrayList<ChargingEvent> chargingEvents;
    private ArrayList<ServiceTrip> serviceTrips;
    private String dataSet;
    private Solution solution;
    private Edge[][] matrixSTtoST;
    private Edge[][] matrixSTtoCE;
    private Edge[][] matrixCEtoST;
    // TODO: use random as singleton class
    private Random random;

    //starting temperature
    private double maxT;
    //temperature modifier
    private double tBeta;
    //max iterations at given temperature
    private int maxQ;

    public SimulatedAnnealing(String dataSet, double maxT, double tBeta, int maxQ) throws FileNotFoundException {
        this.dataSet = dataSet;
        chargingEvents = ChargingEvent.createChargingEvents("./src/Datasets/" + dataSet + "_chargEvents.csv");
        serviceTrips = ServiceTrip.loadServiceTripsFromFile("./src/Datasets/" + dataSet + "_Trips.csv");

        solution = new Solution();

        this.random = new Random();

        for (int tripIndex = 1; tripIndex < serviceTrips.size() - 1; tripIndex++) {
            Vehicle vehicle = new Vehicle(serviceTrips.get(0), serviceTrips.get(serviceTrips.size() - 1));
            vehicle.addServiceTrip(serviceTrips.get(tripIndex));
            solution.addVehicle(vehicle);
        }


        // ServiceTrip to ServiceTrip matrix
        Scanner scannerTij = new Scanner(new File("./src/Datasets/" + dataSet + "_MatrixTij.csv"));
        Scanner scannerCij = new Scanner(new File("./src/Datasets/" + dataSet + "_MatrixCij.csv"));
        String lineTij = scannerTij.nextLine();
        String lineCij = scannerCij.nextLine();
        int matrixNumberOfLines = Integer.parseInt(lineTij);
        lineTij = scannerTij.nextLine();
        lineCij = scannerCij.nextLine();
        int matrixNumberOfRows = Integer.parseInt(lineTij);

        matrixSTtoST = new Edge[matrixNumberOfLines][matrixNumberOfRows];

        for (int i = 0; i < matrixNumberOfLines; i++) {
            lineTij = scannerTij.nextLine();
            lineCij = scannerCij.nextLine();
            Scanner rowScannerTij = new Scanner(lineTij);
            Scanner rowScannerCij = new Scanner(lineCij);
            rowScannerTij.useDelimiter(";");
            rowScannerCij.useDelimiter(";");
            for (int j = 0; j < matrixNumberOfRows; j++) {
                int timeDistance = Integer.parseInt(rowScannerTij.next());
                double batteryConsumption = Double.parseDouble(rowScannerCij.next());
                // check if such edge is possible (timewise), if not insert null
                if (serviceTrips.get(i).getEnd() + timeDistance > serviceTrips.get(j).getStart()) {
                    matrixSTtoST[i][j] = null;
                } else {
                    matrixSTtoST[i][j] = new Edge(timeDistance, batteryConsumption);
                }
            }
            rowScannerTij.close();
            rowScannerCij.close();
        }
        scannerTij.close();
        scannerCij.close();

        //  TODO: if event is on same charger as previous and has same start time, don't create edge
        // ServiceTrip to ChargingEvent matrix
        Scanner scannerTir = new Scanner(new File("./src/Datasets/" + dataSet + "_MatrixTir.csv"));
        Scanner scannerCir = new Scanner(new File("./src/Datasets/" + dataSet + "_MatrixCir.csv"));
        String lineTir = scannerTir.nextLine();
        String lineCir = scannerCir.nextLine();
        matrixNumberOfLines = Integer.parseInt(lineTir);
        lineTir = scannerTir.nextLine();
        lineCir = scannerCir.nextLine();
        matrixNumberOfRows = Integer.parseInt(lineTir);

        matrixSTtoCE = new Edge[matrixNumberOfLines][matrixNumberOfRows];

        for (int i = 0; i < matrixNumberOfLines; i++) {
            lineTir = scannerTir.nextLine();
            lineCir = scannerCir.nextLine();
            Scanner rowScannerTir = new Scanner(lineTir);
            Scanner rowScannerCir = new Scanner(lineCir);
            rowScannerTir.useDelimiter(";");
            rowScannerCir.useDelimiter(";");
            for (int j = 0; j < matrixNumberOfRows; j++) {
                int timeDistance = Integer.parseInt(rowScannerTir.next());
                double batteryConsumption = Double.parseDouble(rowScannerCir.next());
                if (serviceTrips.get(i).getEnd() + timeDistance > chargingEvents.get(j).getStartTime()) {
                    matrixSTtoCE[i][j] = null;
                } else {
                    matrixSTtoCE[i][j] = new Edge(timeDistance, batteryConsumption);
                }
            }
            rowScannerTir.close();
            rowScannerCir.close();
        }
        scannerTir.close();
        scannerCir.close();

        // ServiceTrip to ChargingEvent matrix
        Scanner scannerTrj = new Scanner(new File("./src/Datasets/" + dataSet + "_MatrixTrj.csv"));
        Scanner scannerCrj = new Scanner(new File("./src/Datasets/" + dataSet + "_MatrixCrj.csv"));
        String lineTrj = scannerTrj.nextLine();
        String lineCrj = scannerCrj.nextLine();
        matrixNumberOfLines = Integer.parseInt(lineTrj);
        lineTrj = scannerTrj.nextLine();
        lineCir = scannerCrj.nextLine();
        matrixNumberOfRows = Integer.parseInt(lineTrj);

        matrixCEtoST = new Edge[matrixNumberOfLines][matrixNumberOfRows];

        for (int i = 0; i < matrixNumberOfLines; i++) {
            lineTrj = scannerTrj.nextLine();
            lineCrj = scannerCrj.nextLine();
            Scanner rowScannerTrj = new Scanner(lineTrj);
            Scanner rowScannerCrj = new Scanner(lineCrj);
            rowScannerTrj.useDelimiter(";");
            rowScannerCrj.useDelimiter(";");
            for (int j = 0; j < matrixNumberOfRows; j++) {
                int timeDistance = Integer.parseInt(rowScannerTrj.next());
                double batteryConsumption = Double.parseDouble(rowScannerCrj.next());
                if (chargingEvents.get(i).getStartTime() + timeDistance > serviceTrips.get(j).getStart()) {
                    matrixCEtoST[i][j] = null;
                } else {
                    matrixCEtoST[i][j] = new Edge(timeDistance, batteryConsumption);
                }
            }
            rowScannerTrj.close();
            rowScannerCrj.close();
        }
        scannerTrj.close();
        scannerCrj.close();

        this.maxT = maxT;
        this.tBeta = tBeta;
        this.maxQ = maxQ;
    }

    public void runSimulatedAnnealing() {
        long startTime = System.currentTimeMillis();
        // currently 10 min
        long endAfterTime = 10 * 60 * 1000 + startTime;

        Solution solutionCurrent = solution;
        boolean isFoundBetterSolution;
        double currentTemperature = maxT;
        do {
            isFoundBetterSolution = false;
            for (int q = 0; q < maxQ; q++) {
                Solution nextSolution = solutionCurrent.findNext(matrixSTtoST, matrixSTtoCE, matrixCEtoST, chargingEvents);
                System.out.println("new solution: " + nextSolution.getVehicles().size());
//                System.out.println("new solution: " + nextSolution);
                if (nextSolution == null) {
                    return;
                }
                if (nextSolution.getVehicles().size() <= solutionCurrent.getVehicles().size()) {
                    if (!isFoundBetterSolution && nextSolution.getVehicles().size() < solutionCurrent.getVehicles().size()) {
                        isFoundBetterSolution = true;
                    }
                    solutionCurrent = nextSolution;
                    solution = nextSolution;
                } else {
                    double pAcceptNext = Math.exp(
                            -((nextSolution.getVehicles().size() - solutionCurrent.getVehicles().size())
                                    / currentTemperature)
                    );
                    double generatedValue = random.nextDouble();
                    if (generatedValue <= pAcceptNext) {
                        System.out.println("accepted worse: " + nextSolution.getVehicles().size());
                        solutionCurrent = nextSolution;
                    }
                }
            }
            currentTemperature /= 1 + tBeta * currentTemperature;
        } while (isFoundBetterSolution && System.currentTimeMillis() < endAfterTime);
    }

    public String toString() {
        return "solution: \n" + solution + "solution length: \n" + solution.getVehicles().size() + "\n";
    }
}
