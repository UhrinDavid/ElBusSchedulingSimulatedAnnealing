import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class GUISolution extends JFrame {

    public GUISolution(Solution solution) {
        super("Electric bus scheduling - Simulated Annealing, Solution.");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(500,500);
        Canvas c = new Canvas() {
            // paint the canvas
            public void paint(Graphics g)
            {
                g.setColor(Color.GREEN);
                for (int i = 0; i < solution.getsTsGroups().size(); i++) {
                    STsGroup group = solution.getsTsGroups().get(i);
                    boolean isOdd = false;
                    for (Map.Entry<Integer, STGroupVertex> vertex: group.getServiceTripsWithCEsVertices().entrySet()
                         ) {
                        g.setColor(Color.BLACK);
                        g.setFont(new Font("Bold", 1, 14));
                        if (vertex.getValue() instanceof ServiceTripVertex) {
                            g.drawString(""+((ServiceTripVertex) vertex.getValue()).getId(), vertex.getValue().getStart(), i*60+35);
                            g.setColor(Color.GREEN);
                        } else if (((ChargingEventVertex) vertex.getValue()).isReserved()) {
                            g.setColor(Color.BLUE);
                            g.drawString(((ChargingEventVertex) vertex.getValue()).getIndexCharger()+"_"+((ChargingEventVertex) vertex.getValue()).getIndexChargingEvent(), ((ChargingEventVertex) vertex.getValue()).getStart(), i*60+35);
                        } else {
                            g.setColor(Color.RED);
                            g.drawString(((ChargingEventVertex) vertex.getValue()).getIndexCharger()+"_"+((ChargingEventVertex) vertex.getValue()).getIndexChargingEvent(), ((ChargingEventVertex) vertex.getValue()).getStart(), i*60+35);
                        }
                        g.fillRect(vertex.getValue().getStart(), i*60, (vertex.getValue().getEnd() - vertex.getValue().getStart()), 20);

                    }
                }
            }
        };
        c.setSize(1500, 1000);
        // set background
        c.setBackground(Color.white);
        JScrollPane scrPane = new JScrollPane(c);

        this.add(scrPane);
        this.setVisible(true);
    }
}
