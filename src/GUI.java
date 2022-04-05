import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class GUI extends JFrame implements ActionListener {
    private static final String WINDOW_TITLE = "Electric bus scheduling - Simulated Annealing.";
    private static final int WINDOW_WIDTH = 500;
    private static final int WINDOW_HEIGHT = 800;
    private static final int ELEMENT_GAP = 10;
    private static final int BORDER = 10;

    private static final String DATASET_LABEL = "Dataset";
    private static final String PARAMETERS_LABEL = "Parameters";
    private static final String START = "Start";
    private static final String TRIPS = "Trips";
    private static final String CHARGING_EVENTS = "Charging events";
    private static final String TRIP_TO_TRIP_TIME = "Trip to trip time matrix";
    private static final String TRIP_TO_TRIP_CONSUMPTION = "Trip to trip consumption matrix";
    private static final String TRIP_TO_CHARGING_EVENT_TIME = "Trip to charging event time matrix";
    private static final String TRIP_TO_CHARGING_EVENT_CONSUMPTION = "Trip to charging event consumption matrix";
    private static final String CHARGING_EVENT_TO_TRIP_TIME = "Charging event to trip time matrix";
    private static final String CHARGING_EVENT_TO_TRIP_CONSUMPTION = "Charging event to trip consumption matrix";

    private final String REHEAT = "Reheat";
    private final String MAX_COMPUTING_TIME_MINUTES = "Max computing time in minutes";
    private final String MAX_TEMPERATURE = "Max (starting) temperature";
    private final String MODIFICATOR_TEMPRETURE = "Temperature modificator";
    private final String ITERATIONS_ON_TEMPERATURE = "Iterations on temperature";
    private final String MAX_COMPUTING_TIME_MINUTES_DEFAULT = "2";
    private final String MAX_TEMPERATURE_DEFAULT = "100";
    private final String MODIFICATOR_TEMPRETURE_DEFAULT = "0.5";
    private final String ITERATIONS_ON_TEMPERATURE_DEFAULT = "1000";

    private static final String RESULT_LABEL = "Result";
    private static final String RESULT_PATH = "Result path";
    private static final String RESULT_FILE_NAME = "Result file name";
    private static final String RESULT_FILE_NAME_DEFAULT = "result";
    private static final String RESULT_FILE_TYPE = ".txt";

    private static final String FINISHED_RUN_MESSAGE = "Run has been sucessfuly finished!";

    private final JPanel startPanel;

    private final FileChooserPanel fCPTrips;
    private final FileChooserPanel fCPChargingEvents;
    private final FileChooserPanel fCPTripToTripTime;
    private final FileChooserPanel fCPTripToTripConsumption;;
    private final FileChooserPanel fCPTripToChargingEventTime;
    private final FileChooserPanel fCPTripToChargingEventConsumption;
    private final FileChooserPanel fCPChargingEventToTripTime;
    private final FileChooserPanel fCPChargingEventToTripConsumption;
    private final JCheckBox cBReheat;
    private final TextFieldPanel tFPMaxComputingTimeMinutes;
    private final TextFieldPanel tFPMaxTemperature;
    private final TextFieldPanel tFPModificatorTemperature;
    private final TextFieldPanel tFPIterationsOnTemperature;
    private final FileChooserPanel fCPResultPath;
    private final TextFieldPanel tFPResultFilename;
    private final JButton startButton;
    private boolean enabledStartButton = true;

    public GUI() {
        super(WINDOW_TITLE);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(WINDOW_WIDTH,WINDOW_HEIGHT);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (UnsupportedLookAndFeelException e) {
            System.out.println(e.getMessage());
        }
        catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
        catch (InstantiationException e) {
            System.out.println(e.getMessage());
        }
        catch (IllegalAccessException e) {
            System.out.println(e.getMessage());
        }

        this.startPanel = new JPanel();
        this.startPanel.setLayout(new BoxLayout(startPanel, BoxLayout.Y_AXIS));
        this.startPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.startPanel.setAlignmentY(Component.LEFT_ALIGNMENT);
        this.startPanel.setBorder(BorderFactory.createEmptyBorder(BORDER, BORDER, BORDER, BORDER));

        fCPTrips = new FileChooserPanel(TRIPS);
        fCPChargingEvents = new FileChooserPanel(CHARGING_EVENTS);
        fCPTripToTripTime = new FileChooserPanel(TRIP_TO_TRIP_TIME);
        fCPTripToTripConsumption = new FileChooserPanel(TRIP_TO_TRIP_CONSUMPTION);
        fCPTripToChargingEventTime = new FileChooserPanel(TRIP_TO_CHARGING_EVENT_TIME);
        fCPTripToChargingEventConsumption = new FileChooserPanel(TRIP_TO_CHARGING_EVENT_CONSUMPTION);
        fCPChargingEventToTripTime = new FileChooserPanel(CHARGING_EVENT_TO_TRIP_TIME);
        fCPChargingEventToTripConsumption = new FileChooserPanel(CHARGING_EVENT_TO_TRIP_CONSUMPTION);
        cBReheat = new JCheckBox(REHEAT);
        tFPMaxComputingTimeMinutes = new TextFieldPanel(MAX_COMPUTING_TIME_MINUTES_DEFAULT, MAX_COMPUTING_TIME_MINUTES);
        tFPMaxTemperature = new TextFieldPanel(MAX_TEMPERATURE_DEFAULT, MAX_TEMPERATURE);
        tFPModificatorTemperature = new TextFieldPanel(MODIFICATOR_TEMPRETURE_DEFAULT, MODIFICATOR_TEMPRETURE);
        tFPIterationsOnTemperature = new TextFieldPanel(ITERATIONS_ON_TEMPERATURE_DEFAULT, ITERATIONS_ON_TEMPERATURE);
        String resultPathDefault = " ";
        try {
            resultPathDefault = Path.of(".").toRealPath().toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        fCPResultPath = new FileChooserPanel(RESULT_PATH, resultPathDefault, JFileChooser.DIRECTORIES_ONLY);
        tFPResultFilename = new TextFieldPanel(RESULT_FILE_NAME_DEFAULT, RESULT_FILE_NAME);
        startButton = new JButton(START);
        startButton.addActionListener(this);
        startButton.setBackground(Color.GREEN);
        startButton.setBorderPainted(false);
        startButton.setEnabled(enabledStartButton);

        addToStartPanel(new InputGroupLabelPanel(DATASET_LABEL));
        addToStartPanel(fCPTrips);
        addToStartPanel(fCPChargingEvents);
        addToStartPanel(fCPTripToTripTime);
        addToStartPanel(fCPTripToTripConsumption);
        addToStartPanel(fCPTripToChargingEventTime);
        addToStartPanel(fCPTripToChargingEventConsumption);
        addToStartPanel(fCPChargingEventToTripTime);
        addToStartPanel(fCPChargingEventToTripConsumption);
        addToStartPanel(new InputGroupLabelPanel(PARAMETERS_LABEL));
        addToStartPanel(tFPMaxComputingTimeMinutes);
        addToStartPanel(tFPMaxTemperature);
        addToStartPanel(tFPModificatorTemperature);
        addToStartPanel(tFPIterationsOnTemperature);
        addToStartPanel(cBReheat);
        addToStartPanel(new InputGroupLabelPanel(RESULT_LABEL));
        addToStartPanel(fCPResultPath);
        addToStartPanel(tFPResultFilename);
        addToStartPanel(startButton);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.add(startPanel);
        this.add(scrollPane);
        this.setVisible(true);
    }

    private void addToStartPanel(Component comp) {
        this.startPanel.add(Box.createRigidArea(new Dimension(0, ELEMENT_GAP)));
        this.startPanel.add(comp);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        enabledStartButton = false;
        String fileNameTrips = fCPTrips.getSelectedPath();
        String fileNameChargingEvents = fCPChargingEvents.getSelectedPath();
        String fileNameTripToTripTime = fCPTripToTripTime.getSelectedPath();
        String fileNameTripToTripConsumption = fCPTripToTripConsumption.getSelectedPath();
        String fileNameTripToChargingEventTime = fCPTripToChargingEventTime.getSelectedPath();
        String fileNameTripToChargingEventConsumption = fCPTripToChargingEventConsumption.getSelectedPath();
        String fileNameChargingEventToTripTime = fCPChargingEventToTripTime.getSelectedPath();
        String fileNameChargingEventToTripConsumption = fCPChargingEventToTripConsumption.getSelectedPath();
        boolean reheat = cBReheat.isSelected();
        int maxComputingTimeSeconds = Integer.parseInt(tFPMaxComputingTimeMinutes.getInputValue())*60;
        double maxTemperature = Double.parseDouble(tFPMaxTemperature.getInputValue());
        double modificatorTemperature = Double.parseDouble(tFPModificatorTemperature.getInputValue());
        int iterationsOnTemperature = Integer.parseInt(tFPIterationsOnTemperature.getInputValue());
        String resultPath = fCPResultPath.getSelectedPath();
        String resultFileName = tFPResultFilename.getInputValue();
        String resultFinalPath = resultPath + "/" + resultFileName + RESULT_FILE_TYPE;
        try {
            SimulatedAnnealing.runSimulation(
                    reheat, maxComputingTimeSeconds, maxTemperature,
                    modificatorTemperature, iterationsOnTemperature,
                    fileNameTrips,
                    fileNameChargingEvents,
                    fileNameTripToTripConsumption,
                    fileNameTripToChargingEventConsumption,
                    fileNameChargingEventToTripConsumption,
                    fileNameTripToTripTime,
                    fileNameTripToChargingEventTime,
                    fileNameChargingEventToTripTime,
                    resultFinalPath
            );
        } catch (FileNotFoundException exception) {
            JOptionPane.showMessageDialog(null, exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        JOptionPane.showMessageDialog(this, FINISHED_RUN_MESSAGE);
        enabledStartButton = true;
    }
}
