import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FileChooserPanel extends JPanel implements ActionListener {
    private static final String DEFAULT_PATH = "./";
    private static final String BUTTON_TEXT = "Choose file";

    private final JFileChooser fileChooser;
    private final JLabel labelSelectedPath;
    private final JButton buttonOpenDialog;

    public FileChooserPanel(String labelText) {
        this(labelText, " ", JFileChooser.FILES_ONLY);
    }

    public FileChooserPanel(String labelText, String defaultSelected, int chooserType) {
        super();
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.fileChooser = new JFileChooser(DEFAULT_PATH);
        fileChooser.setFileSelectionMode(chooserType);
        this.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.setAlignmentY(Component.LEFT_ALIGNMENT);


        buttonOpenDialog = new JButton(BUTTON_TEXT);
        buttonOpenDialog.addActionListener(this);
        labelSelectedPath = new JLabel(defaultSelected);

        JLabel label = new JLabel(labelText);
        this.add(label);
        this.add(buttonOpenDialog);
        this.add(labelSelectedPath);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
            int res = fileChooser.showOpenDialog(null);
            if (res == JFileChooser.APPROVE_OPTION) {
                labelSelectedPath.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
            else {
                labelSelectedPath.setText(" ");
            }
    }

    public String getSelectedPath() {
        return labelSelectedPath.getText();
    }
}
