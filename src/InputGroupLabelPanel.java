import javax.swing.*;
import java.awt.*;

public class InputGroupLabelPanel extends JPanel {
    public InputGroupLabelPanel(String labelName) {
        super();
        this.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.setAlignmentY(Component.CENTER_ALIGNMENT);
        Font labelFont = UIManager.getFont("Label.font");
        Label label = new Label(labelName);
        label.setFont(new Font(labelFont.getFontName(), Font.BOLD, labelFont.getSize() + 5));
        this.add(label);
    }
}
