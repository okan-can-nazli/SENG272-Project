package view;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.AppSession;
import model.Dimension;
import model.Scenario;

// step 5 - final results screen
// shows weighted averages per dimension, radar chart, and gap analysis
public class Step5AnalysePanel extends JPanel {
    private final AppSession session;
    private final MainFrame  mainFrame;
    private JPanel contentArea;

    public Step5AnalysePanel(AppSession session, MainFrame mainFrame) {
        this.session   = session;
        this.mainFrame = mainFrame;
        initUI();
    }

    private void initUI() {
        setBackground(new Color(245, 245, 248));
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(25, 60, 25, 60));

        JLabel title = makeTitle("Step 5: Analyse Results");
        add(title, BorderLayout.NORTH);

        contentArea = new JPanel();
        contentArea.setBackground(new Color(245, 245, 248));
        contentArea.setLayout(new BoxLayout(contentArea, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(contentArea);
        scroll.setBorder(new EmptyBorder(14, 0, 0, 0));
        scroll.getViewport().setBackground(new Color(245, 245, 248));
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        add(scroll, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setBackground(new Color(245, 245, 248));
        JButton back    = makeSecondary("\u2190 Back");
        JButton restart = makePrimary("New Session");
        back.addActionListener(e -> mainFrame.goBack(5));
        restart.addActionListener(e -> mainFrame.goToStep(1));
        btnRow.add(back);
        btnRow.add(restart);
        add(btnRow, BorderLayout.SOUTH);
    }

    // rebuilds everything when user arrives at this step
    public void refresh() {
        contentArea.removeAll();
        Scenario scenario = session.getSelectedScenario();
        if (scenario == null) return;

        List<Dimension> dims = scenario.getDimensions();

        // 5a: progress bars for each dimension score
        addSectionHeader("5a. Dimension-Based Weighted Averages");
        contentArea.add(Box.createVerticalStrut(8));

        Dimension weakest = null;
        double minScore = Double.MAX_VALUE;
        for (Dimension d : dims) {
            double s = d.calculateScore();
            addProgressRow(d, s);
            contentArea.add(Box.createVerticalStrut(4));
            // track the worst dimension for gap analysis
            if (s < minScore) { minScore = s; weakest = d; }
        }

        contentArea.add(Box.createVerticalStrut(20));

        // 5b: radar chart bonus
        addSectionHeader("5b. Radar Chart \u2014 Bonus");
        contentArea.add(Box.createVerticalStrut(8));

        RadarChartPanel radar = new RadarChartPanel(dims);
        radar.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentArea.add(radar);

        contentArea.add(Box.createVerticalStrut(20));

        // 5c: gap analysis for the worst dimension
        addSectionHeader("5c. Gap Analysis");
        contentArea.add(Box.createVerticalStrut(8));
        if (weakest != null) addGapCard(weakest, minScore);

        contentArea.revalidate();
        contentArea.repaint();
    }

    private void addSectionHeader(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 15));
        l.setForeground(new Color(33, 150, 243));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentArea.add(l);
    }

    private void addProgressRow(Dimension dim, double score) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(new Color(245, 245, 248));
        row.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 42));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(new EmptyBorder(2, 0, 2, 0));

        String label = dim.getName() + "  (coeff: " + dim.getCoefficient() + ")";
        JLabel nameLabel = new JLabel(label);
        nameLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        nameLabel.setPreferredSize(new java.awt.Dimension(240, 30));
        row.add(nameLabel, BorderLayout.WEST);

        // bar color based on score range
        Color barColor;
        if      (score >= 4.0) barColor = new Color(67, 160, 71);
        else if (score >= 3.0) barColor = new Color(251, 192, 45);
        else                   barColor = new Color(229, 57, 53);

        // max is 500 because score*100 gives better precision than just 0-5
        JProgressBar bar = new JProgressBar(0, 500);
        bar.setValue((int) (score * 100));
        bar.setStringPainted(true);
        bar.setString(String.format("%.2f / 5.00", score));
        bar.setFont(new Font("SansSerif", Font.BOLD, 12));
        bar.setForeground(barColor);
        bar.setBackground(new Color(220, 222, 230));
        bar.setBorder(BorderFactory.createEmptyBorder());
        row.add(bar, BorderLayout.CENTER);

        contentArea.add(row);
    }

    private void addGapCard(Dimension dim, double score) {
        double gap = 5.0 - score;

        // quality label based on score
        String label; Color labelColor;
        if      (score >= 4.5) { label = "Excellent";         labelColor = new Color(67, 160, 71);  }
        else if (score >= 3.5) { label = "Good";              labelColor = new Color(124, 179, 66); }
        else if (score >= 2.5) { label = "Needs Improvement"; labelColor = new Color(251, 140, 0);  }
        else                   { label = "Poor";              labelColor = new Color(229, 57, 53);  }

        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 57, 53), 2),
            new EmptyBorder(16, 22, 16, 22)
        ));
        card.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 190));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        addGapRow(card, "Dimension:",             dim.getName(),                       Color.BLACK);
        addGapRow(card, "Score:",                 String.format("%.2f / 5.00", score), new Color(229, 57, 53));
        addGapRow(card, "Gap (5.0 \u2212 score):",String.format("%.2f", gap),          new Color(255, 87, 34));
        addGapRow(card, "Quality Level:",         label,                               labelColor);
        card.add(Box.createVerticalStrut(8));

        JLabel msg = new JLabel("This dimension has the lowest score and requires the most improvement.");
        msg.setFont(new Font("SansSerif", Font.ITALIC, 12));
        msg.setForeground(new Color(110, 110, 135));
        msg.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(msg);

        contentArea.add(card);
    }

    private void addGapRow(JPanel parent, String key, String value, Color valueColor) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        row.setBackground(Color.WHITE);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel kl = new JLabel(key);
        kl.setFont(new Font("SansSerif", Font.BOLD, 13));
        JLabel vl = new JLabel(value);
        vl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        vl.setForeground(valueColor);
        row.add(kl);
        row.add(vl);
        parent.add(row);
    }

    private JLabel makeTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 22));
        l.setForeground(new Color(35, 35, 60));
        return l;
    }

    private JButton makePrimary(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setBackground(new Color(33, 150, 243));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setPreferredSize(new java.awt.Dimension(150, 40));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton makeSecondary(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.PLAIN, 14));
        b.setBackground(new Color(238, 238, 245));
        b.setForeground(new Color(80, 80, 100));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setPreferredSize(new java.awt.Dimension(110, 40));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}