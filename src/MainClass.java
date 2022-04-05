import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class MainClass {

    public static void main(String[] args) {
        GUI g = new GUI();
        String[] testDatasets = {"AllZa"};
//
////        String[] testDatasets = {"20a26a29a30", "20a29a30a31", "21a22a27", "26", "26a27a29", "26a29", "27", "AllZA", "busesAll", "TrolejbusyAll"};
//        for (String dataSet : testDatasets
//             ) {
//            runSimulation(dataSet, true, 600, 100, 0.9, 1000, "");
//        }
    }

    public static void runSimulation(String dataSet, boolean shouldReheat, int maxComputingTimeSeconds, int maxT, double tBeta, int maxQ, String filePrefix) {
        System.out.println("\n\nnew run " + dataSet + " " + filePrefix);
        File file = new File("./src/results/" + dataSet + "_"+  filePrefix + ".txt");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileWriter myWriter = null;
        try {
            myWriter = new FileWriter("./src/results/" + dataSet +"_"+  filePrefix + ".txt");
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
