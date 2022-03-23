import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class MatrixEdges {
    private final Edge[][] edges;

    private MatrixEdges(String filenameTimeDistance, String filenameBatteryConsumption) throws FileNotFoundException {
        Scanner scannerTimeDistance = new Scanner(new File(filenameTimeDistance));
        Scanner scannerBatteryConsumption = new Scanner(new File(filenameBatteryConsumption));
        String lineTimeDistance = scannerTimeDistance.nextLine();
        String lineBatteryConsumption = scannerBatteryConsumption.nextLine();
        int matrixNumberOfLines = Integer.parseInt(lineTimeDistance);
        lineTimeDistance = scannerTimeDistance.nextLine();
        lineBatteryConsumption = scannerBatteryConsumption.nextLine();
        int matrixNumberOfRows = Integer.parseInt(lineTimeDistance);

        edges = new Edge[matrixNumberOfLines][matrixNumberOfRows];

        for (int i = 0; i < matrixNumberOfLines; i++) {
            lineTimeDistance = scannerTimeDistance.nextLine();
            lineBatteryConsumption = scannerBatteryConsumption.nextLine();
            Scanner rowScannerTimeDistance = new Scanner(lineTimeDistance);
            Scanner rowScannerBatteryConsumption = new Scanner(lineBatteryConsumption);
            rowScannerTimeDistance.useDelimiter(";");
            rowScannerBatteryConsumption.useDelimiter(";");
            for (int j = 0; j < matrixNumberOfRows; j++) {
                int timeDistance = Integer.parseInt(rowScannerTimeDistance.next());
                double batteryConsumption = Double.parseDouble( rowScannerBatteryConsumption.next());
                edges[i][j] = new Edge(timeDistance, batteryConsumption);
            }
            rowScannerTimeDistance.close();
            rowScannerBatteryConsumption.close();
        }
        scannerTimeDistance.close();
        scannerBatteryConsumption.close();
    }

    public Edge getEdge(int x, int y) {
        return edges[x][y];
    }
}
