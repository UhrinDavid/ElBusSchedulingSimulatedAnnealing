import javax.swing.*;
import java.awt.*;

public class TextFieldPanel extends JPanel {
    private static final int TEXT_FIELD_WIDTH = 300;
    private static final int TEXT_FIELD_HEIGHT = 30;

    private final JTextField textField;

    public TextFieldPanel(String defaultValue, String labelText) {
        super();
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.setAlignmentY(Component.LEFT_ALIGNMENT);
        this.textField = new JTextField(defaultValue);
        this.textField.setMaximumSize(new Dimension(TEXT_FIELD_WIDTH, TEXT_FIELD_HEIGHT));
        JLabel label = new JLabel(labelText);
        this.add(label);
        this.add(textField);
    }

    public String getInputValue() {
        return textField.getText();
    }
}
