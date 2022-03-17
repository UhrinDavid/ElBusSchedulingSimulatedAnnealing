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


                    g.setColor(Color.BLACK);
                    g.setFont(new Font("Bold", 1, 10));
                    g.drawString(""+STsGroup.getDepoStart().getId(), STsGroup.getDepoStart().getStart(), i*15+10);
                    g.setColor(Color.GREEN);
                    g.fillRect(STsGroup.getDepoStart().getStart(), i*15, (STsGroup.getDepoStart().getEnd() - STsGroup.getDepoStart().getStart()), 10);

                    g.setColor(Color.BLACK);
                    g.setFont(new Font("Bold", 1, 10));
                    g.drawString(""+STsGroup.getDepoEnd().getId(), STsGroup.getDepoEnd().getStart(), i*15+10);
                    g.setColor(Color.GREEN);
                    g.fillRect(STsGroup.getDepoEnd().getStart(), i*15, (STsGroup.getDepoEnd().getEnd() - STsGroup.getDepoEnd().getStart()), 10);

                    for (Map.Entry<Integer, STGroupVertex> vertex: group.getServiceTripsWithCEsVertices().entrySet()
                         ) {
                        g.setColor(Color.BLACK);
                        g.setFont(new Font("Bold", 1, 10));
                        if (vertex.getValue() instanceof ServiceTripVertex) {
                            g.setColor(Color.GREEN);

                            g.fillRect(vertex.getValue().getStart(), i*15, (vertex.getValue().getEnd() - vertex.getValue().getStart()), 10);

                            g.setColor(Color.BLACK);
                            g.drawString(""+((ServiceTripVertex) vertex.getValue()).getId(), vertex.getValue().getStart(), i*15+10);
                        } else if (((ChargingEventVertex) vertex.getValue()).isReserved()) {
                            g.setColor(Color.BLUE);
                            g.fillRect(vertex.getValue().getStart(), i*15, (vertex.getValue().getEnd() - vertex.getValue().getStart()), 10);

                            g.setColor(Color.BLACK);
                            g.drawString(((ChargingEventVertex) vertex.getValue()).getIndexCharger()+"_"+((ChargingEventVertex) vertex.getValue()).getIndexChargingEvent(), ((ChargingEventVertex) vertex.getValue()).getStart(), i*15+10);

                        } else {
                            g.setColor(Color.RED);
                            g.fillRect(vertex.getValue().getStart(), i*15, (vertex.getValue().getEnd() - vertex.getValue().getStart()), 10);

                            g.setColor(Color.BLACK);
                            g.drawString(((ChargingEventVertex) vertex.getValue()).getIndexCharger()+"_"+((ChargingEventVertex) vertex.getValue()).getIndexChargingEvent(), ((ChargingEventVertex) vertex.getValue()).getStart(), i*15+10);

                        }

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

    public GUISolution(STsGroup group) {
        super("group");
        this.setSize(500,500);
        Canvas c = new Canvas() {
            // paint the canvas
            public void paint(Graphics g)
            {
                int i = 2;
//                g.setColor(Color.GREEN);
//                for (int i = 0; i < solution.getsTsGroups().size(); i++) {
//                    STsGroup group = solution.getsTsGroups().get(i);


                    g.setColor(Color.BLACK);
                    g.setFont(new Font("Bold", 1, 10));
                    g.drawString(""+STsGroup.getDepoStart().getId(), STsGroup.getDepoStart().getStart(), i*15+10);
                    g.setColor(Color.GREEN);
                    g.fillRect(STsGroup.getDepoStart().getStart(), i*15, (STsGroup.getDepoStart().getEnd() - STsGroup.getDepoStart().getStart()), 10);

//                    g.setColor(Color.BLACK);
//                    g.setFont(new Font("Bold", 1, 14));
//                    g.drawString(""+STsGroup.getDepoEnd().getId(), STsGroup.getDepoEnd().getStart(), i*60+35);
                    g.setColor(Color.GREEN);
                    g.fillRect(STsGroup.getDepoEnd().getStart(), i*15, (STsGroup.getDepoEnd().getEnd() - STsGroup.getDepoEnd().getStart()), 10);

                    for (Map.Entry<Integer, STGroupVertex> vertex: group.getServiceTripsWithCEsVertices().entrySet()
                    ) {
//                        g.setColor(Color.BLACK);
//                        g.setFont(new Font("Bold", 1, 14));
                        if (vertex.getValue() instanceof ServiceTripVertex) {
//                            g.drawString(""+((ServiceTripVertex) vertex.getValue()).getId(), vertex.getValue().getStart(), i*60+35);
                            g.setColor(Color.GREEN);
                        } else if (((ChargingEventVertex) vertex.getValue()).isReserved()) {
                            g.setColor(Color.BLUE);
//                            g.drawString(((ChargingEventVertex) vertex.getValue()).getIndexCharger()+"_"+((ChargingEventVertex) vertex.getValue()).getIndexChargingEvent(), ((ChargingEventVertex) vertex.getValue()).getStart(), i*60+35);
                        } else {
                            g.setColor(Color.RED);
//                            g.drawString(/((ChargingEventVertex) vertex.getValue()).getIndexCharger()+"_"+((ChargingEventVertex) vertex.getValue()).getIndexChargingEvent(), ((ChargingEventVertex) vertex.getValue()).getStart(), i*60+35);
                        }
                        g.fillRect(vertex.getValue().getStart(), i*7, (vertex.getValue().getEnd() - vertex.getValue().getStart()), 5);

                    }
                }
//            }
        };
        c.setSize(1500, 1000);
        // set background
        c.setBackground(Color.white);
        JScrollPane scrPane = new JScrollPane(c);

        this.add(scrPane);
        this.setVisible(true);
    }
}
