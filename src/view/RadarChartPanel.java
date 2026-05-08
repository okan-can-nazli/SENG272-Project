package view;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.List;
import javax.swing.*;
import model.Dimension;

// bonus feature - draws a radar/spider chart using Java2D
// one axis per dimension, scores plotted 0-5
public class RadarChartPanel extends JPanel {
    private final List<Dimension> dimensions;
    private static final int CHART_SIZE = 280;
    private static final int RADIUS     = 100;

    public RadarChartPanel(List<Dimension> dimensions) {
        this.dimensions = dimensions;
        setPreferredSize(new java.awt.Dimension(CHART_SIZE + 280, CHART_SIZE + 40));
        setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, CHART_SIZE + 40));
        setBackground(new Color(245, 245, 248));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (dimensions == null || dimensions.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int n  = dimensions.size();
        int cx = CHART_SIZE / 2 + 40; // shifted right to leave room for labels
        int cy = CHART_SIZE / 2 + 20;

        // white background circle
        g2.setColor(Color.WHITE);
        g2.fillOval(cx - RADIUS - 10, cy - RADIUS - 10, (RADIUS + 10) * 2, (RADIUS + 10) * 2);

        // draw grid rings for each score level 1-5
        for (int level = 1; level <= 5; level++) {
            double r = RADIUS * level / 5.0;
            Path2D poly = buildPolygon(n, cx, cy, r);
            g2.setColor(new Color(210, 225, 245, 100));
            g2.fill(poly);
            g2.setColor(new Color(185, 205, 230));
            g2.setStroke(new BasicStroke(0.8f));
            g2.draw(poly);

            // scale number on vertical axis
            if (level < 5) {
                g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
                g2.setColor(new Color(160, 165, 185));
                g2.drawString(String.valueOf(level), cx + 3, (int) (cy - r + 4));
            }
        }

        // axis lines from center to each point
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(new Color(170, 185, 210));
        for (int i = 0; i < n; i++) {
            double angle = axisAngle(i, n);
            g2.drawLine(cx, cy, (int) (cx + RADIUS * Math.cos(angle)), (int) (cy + RADIUS * Math.sin(angle)));
        }

        // data polygon filled with transparent blue
        Path2D data = new Path2D.Double();
        for (int i = 0; i < n; i++) {
            double score = dimensions.get(i).calculateScore();
            double r     = RADIUS * score / 5.0;
            double angle = axisAngle(i, n);
            double px    = cx + r * Math.cos(angle);
            double py    = cy + r * Math.sin(angle);
            if (i == 0) data.moveTo(px, py); else data.lineTo(px, py);
        }
        data.closePath();

        g2.setColor(new Color(33, 150, 243, 65));
        g2.fill(data);
        g2.setColor(new Color(33, 150, 243));
        g2.setStroke(new BasicStroke(2.2f));
        g2.draw(data);

        // dots and labels at each data point
        for (int i = 0; i < n; i++) {
            double score = dimensions.get(i).calculateScore();
            double r     = RADIUS * score / 5.0;
            double angle = axisAngle(i, n);
            int px = (int) (cx + r * Math.cos(angle));
            int py = (int) (cy + r * Math.sin(angle));

            // blue dot with white center
            g2.setColor(new Color(33, 150, 243));
            g2.fillOval(px - 5, py - 5, 10, 10);
            g2.setColor(Color.WHITE);
            g2.fillOval(px - 3, py - 3, 6, 6);

            // label outside the circle
            double lx = cx + (RADIUS + 22) * Math.cos(angle);
            double ly = cy + (RADIUS + 22) * Math.sin(angle);

            String dimName  = dimensions.get(i).getName();
            String scoreStr = String.format("%.2f", score);
            FontMetrics fm  = g2.getFontMetrics(new Font("SansSerif", Font.BOLD, 11));
            int tw = fm.stringWidth(dimName);

            // align text left/right/center based on which side of the chart
            int lxi;
            if      (Math.cos(angle) >  0.3) lxi = (int) lx;
            else if (Math.cos(angle) < -0.3) lxi = (int) lx - tw;
            else                              lxi = (int) lx - tw / 2;

            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.setColor(new Color(45, 55, 85));
            g2.drawString(dimName, lxi, (int) ly);

            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.setColor(new Color(33, 150, 243));
            int sw2 = g2.getFontMetrics().stringWidth(scoreStr);
            g2.drawString(scoreStr, lxi + (tw - sw2) / 2, (int) ly + 13);
        }

        g2.dispose();
    }

    // first axis points up, rest go clockwise
    private double axisAngle(int i, int n) {
        return 2 * Math.PI * i / n - Math.PI / 2;
    }

    private Path2D buildPolygon(int n, int cx, int cy, double r) {
        Path2D p = new Path2D.Double();
        for (int i = 0; i < n; i++) {
            double angle = axisAngle(i, n);
            double x = cx + r * Math.cos(angle);
            double y = cy + r * Math.sin(angle);
            if (i == 0) p.moveTo(x, y); else p.lineTo(x, y);
        }
        p.closePath();
        return p;
    }
}