import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;

public class GUISolution extends JFrame {

    public GUISolution(Solution solution) {
        super("Electric bus scheduling - Simulated Annealing, Solution.");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(500,500);
        Canvas c = new Canvas() {
            // paint the canvas
            public void paint(Graphics g)
            {
                // set color to red
                g.setColor(Color.GREEN);
                for (int i = 0; i < solution.getVehicles().size(); i++) {
                    STsGroup group = solution.getVehicles().get(i);
                    boolean isOdd = false;
                    for (ServiceTrip trip: group.getServiceTrips()
                         ) {
                        g.setColor(Color.GREEN);
                        g.fillRect(trip.getStart(), i*40, (trip.getEnd() - trip.getStart()), 20);
                        g.setColor(Color.BLACK);

                        g.setFont(new Font("Bold", 1, 14));
                        g.drawString(""+trip.getId(), trip.getStart(), i*40+15);
                    }
                    for (ChargingEvent ce: group.getChargingEvents()
                    ) {
                        g.setColor(Color.BLUE);
                        g.fillRect(ce.getStartTime(), i*40 + 5, (ce.getEndTime() - ce.getStartTime()), 20);
                        g.setColor(Color.BLACK);

                        g.setFont(new Font("Bold", 1, 14));
                        g.drawString(""+ce.getMatrixIndex(), ce.getStartTime(), i*40+20);
                    }
                }
            }
        };
        c.setSize(1500, solution.getVehicles().size() * 50);
        // set background
        c.setBackground(Color.white);
        JScrollPane scrPane = new JScrollPane(c);

        this.add(scrPane);
        this.setVisible(true);
    }
}
