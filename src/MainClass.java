import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class MainClass {

    public static void main(String[] args) {
        String[] testDatasets = {"27", "26a29", "21a22a27"};
        boolean[] shouldReheats = {true, false};
        int[] maxComputingTimeMinutes = {2,4,6,8,10};
        int[] maxTs = {1,10,100,1000,10000};
        double[] tBetas = {0.9, 0.1, 0.01, 0.001};
        int[] maxQs = {1, 10, 100, 1000, 10000};
        for (String dataSet : testDatasets
             ) {
            int i = 0;
//            runSimulation(dataSet, true, 100, 10, 0.1, 1, "baseSetting__");

            for (boolean shouldReheatVal : shouldReheats
            ) {
                runSimulation(dataSet, shouldReheatVal, 300, 10, 0.9, 100, "shouldReheats_"+shouldReheatVal);
                i++;
            }
            i = 0;
            for (int maxTime : maxComputingTimeMinutes
            ) {
                runSimulation(dataSet, true, maxTime*60, 10, 0.9, 100, "maxComputingTimeMinutes_"+maxTime);
                i++;
            }
            i = 0;
            for (int maxT : maxTs
            ) {
                runSimulation(dataSet, true, 300, maxT, 0.9, 100, "maxTs_"+maxT);

                i++;
            }
            i = 0;
            for (double tBeta : tBetas
            ) {
                runSimulation(dataSet, true, 300, 10, tBeta, 100, "tBetas_"+tBeta);

                i++;
            }
            i = 0;
            for (int maxQ : maxQs
            ) {
                runSimulation(dataSet, true, 300, 10, 0.9, maxQ, "maxQs_"+maxQ);

                i++;
            }
        }

//        runSimulation("26a29_Z", true, 600, 100, 0.1, 1000);
    }

    public static void runSimulation(String dataSet, boolean shouldReheat, int maxComputingTimeSeconds, int maxT, double tBeta, int maxQ, String filePrefix) {
        System.out.println("new run");
        File file = new File("./src/results/run3/" + dataSet + "_"+  filePrefix + ".txt");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileWriter myWriter = null;
        try {
            myWriter = new FileWriter("./src/results/run3/" + dataSet +"_"+  filePrefix + ".txt");
            myWriter.write("shouldReheat: "+shouldReheat+" maxComputingTimeSeconds: " + maxComputingTimeSeconds + " maxT: " + maxT + " tBeta: " + tBeta + " maxQ: " + maxQ);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < 10; i++) {
            System.out.println("replication: "+i);
            SimulatedAnnealing saAlgorithm = null;
            try {
                saAlgorithm = new SimulatedAnnealing(shouldReheat, maxComputingTimeSeconds, maxT, tBeta, maxQ,
                        "./src/DatasetsNew/spoje_id_" + dataSet + "_Z.csv",
                        "./src/DatasetsNew/ChEvents_" + dataSet + "_Z.csv",
                        "./src/DatasetsNew/Cij_" + dataSet + "_Z.csv",
                        "./src/DatasetsNew/Cir_" + dataSet + "_Z.csv",
                        "./src/DatasetsNew/Cri_" + dataSet + "_Z.csv",
                        "./src/DatasetsNew/Tij_" + dataSet + "_Z.csv",
                        "./src/DatasetsNew/Tir_" + dataSet + "_Z.csv",
                        "./src/DatasetsNew/Tri_" + dataSet + "_Z.csv"
                );
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
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
