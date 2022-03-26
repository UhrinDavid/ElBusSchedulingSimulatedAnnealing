import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;

public class GUI extends JFrame implements ActionListener {

    private final JPanel startPanel;

    private final JTextField tFDSPrefix;
    private final JTextField tFStartingTemperature;
    private final JTextField tFTemperatureBeta;
    private final JTextField tFIterationsAtTemp;
    private final JButton startButton;

    private final SimulatedAnnealing saAlgorithm = null;

    public GUI() {
        super("Electric bus scheduling - Simulated Annealing.");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(300,300);

        this.startPanel = new JPanel();
        this.startPanel.setLayout(new BoxLayout(startPanel, BoxLayout.Y_AXIS));
        this.startPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.startPanel.setAlignmentY(Component.CENTER_ALIGNMENT);

        this.tFDSPrefix = new JTextField("26");
        this.tFDSPrefix.setMaximumSize(new Dimension(200, 30));
        this.tFDSPrefix.setAlignmentX(Component.CENTER_ALIGNMENT);

        this.tFStartingTemperature = new JTextField("1000");
        this.tFStartingTemperature.setMaximumSize(new Dimension(200, 30));
        this.tFStartingTemperature.setAlignmentX(Component.CENTER_ALIGNMENT);

        this.tFTemperatureBeta = new JTextField("1000");
        this.tFTemperatureBeta.setMaximumSize(new Dimension(200, 30));
        this.tFTemperatureBeta.setAlignmentX(Component.CENTER_ALIGNMENT);

        this.tFIterationsAtTemp = new JTextField("100");
        this.tFIterationsAtTemp.setMaximumSize(new Dimension(200, 30));
        this.tFIterationsAtTemp.setAlignmentX(Component.CENTER_ALIGNMENT);

        this.startButton = new JButton("Start");
        this.startButton.setMaximumSize(new Dimension(100, 30));
        this.startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.startButton.addActionListener(this);

        JLabel label1 = new JLabel("Dataset prefix");
        label1.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel label2 = new JLabel("Starting temperature");
        label2.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel label3 = new JLabel("Temperature beta");
        label3.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel label4 = new JLabel("Iterations at each temperature");
        label4.setAlignmentX(Component.CENTER_ALIGNMENT);

        this.startPanel.add(Box.createRigidArea(new Dimension(10, 10)));
        this.startPanel.add(label1);
        this.startPanel.add(tFDSPrefix);
        this.startPanel.add(label2);
        this.startPanel.add(tFStartingTemperature);
        this.startPanel.add(label3);
        this.startPanel.add(tFTemperatureBeta);
        this.startPanel.add(label4);
        this.startPanel.add(tFIterationsAtTemp);
        this.startPanel.add(Box.createRigidArea(new Dimension(10, 10)));
        this.startPanel.add(startButton);

        this.add(startPanel);
        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String dataSet = tFDSPrefix.getText();
        double maxT = Double.parseDouble(tFStartingTemperature.getText());
        double tBeta = Double.parseDouble(tFTemperatureBeta.getText());
        int maxQ = Integer.parseInt(tFIterationsAtTemp.getText());

//        try {
////            saAlgorithm = new SimulatedAnnealing(dataSet, maxT, tBeta, maxQ);
////            saAlgorithm.runSimulatedAnnealing();
////            System.out.println("final: \n" + saAlgorithm);
//
//        } catch (FileNotFoundException exception) {
//            JOptionPane.showMessageDialog(null, exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
//        }
    }
}
