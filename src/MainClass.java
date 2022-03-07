import java.io.FileNotFoundException;

public class MainClass {

    public static void main(String[] args) {
////        GUI applicationGUI = new GUI();
//        for (int j = 0; j < 10; j++) {
//            double t = Math.pow(10,j);
//            System.out.println("##################\n t = "+t);
            for (int i = 0; i < 1; i++) {
                SimulatedAnnealing saAlgorithm = null;
                try {
                    String dataSet = "27_Z";
                    saAlgorithm = new SimulatedAnnealing(true, 600, 50, 0.01, 1000,
                            "./src/DatasetsNew/spoje_id_" + dataSet + ".csv",
                            "./src/DatasetsNew/ChEvents_" + dataSet + ".csv",
                            "./src/DatasetsNew/Cij_" + dataSet + ".csv",
                            "./src/DatasetsNew/Cir_" + dataSet + ".csv",
                            "./src/DatasetsNew/Cri_" + dataSet + ".csv",
                            "./src/DatasetsNew/Tij_" + dataSet + ".csv",
                            "./src/DatasetsNew/Tir_" + dataSet + ".csv",
                            "./src/DatasetsNew/Tri_" + dataSet + ".csv"
                    );
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                saAlgorithm.runSimulatedAnnealing();
                System.out.println("Run nr.: " + (i + 1));
                System.out.println(saAlgorithm);
            }
            System.out.println("##################\n");
//        }
    }
}
