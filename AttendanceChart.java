package com.attendance.gui;

import javax.swing.*;
import java.awt.*;

// Two-bar chart: Present (green) vs Absent (red)
public class AttendanceChart extends JPanel {
    private int present;
    private int absent;

    public AttendanceChart() {
        setOpaque(false);
        setPreferredSize(new Dimension(380, 230));
    }

    public void setValues(int present, int absent) {
        this.present = present;
        this.absent = absent;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        if (present + absent == 0) {
            g2.setColor(Theme.MUTED);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
            String msg = "No attendance recorded yet";
            g2.drawString(msg, w / 2 - g2.getFontMetrics().stringWidth(msg) / 2, h / 2);
            g2.dispose();
            return;
        }

        int baseY = h - 34;
        int chartHeight = baseY - 30;
        int max = Math.max(present, absent);
        int barWidth = Math.min(90, w / 4);
        drawBar(g2, w / 2 - barWidth - 30, baseY, barWidth, chartHeight, present, max, Theme.GREEN, "Present");
        drawBar(g2, w / 2 + 30, baseY, barWidth, chartHeight, absent, max, Theme.RED, "Absent");

        g2.setColor(new Color(229, 231, 235));
        g2.drawLine(20, baseY, w - 20, baseY);
        g2.dispose();
    }

    private void drawBar(Graphics2D g2, int x, int baseY, int barWidth, int chartHeight,
                         int value, int max, Color color, String label) {
        int barHeight = max == 0 ? 0 : (int) Math.round(chartHeight * (value / (double) max));
        g2.setColor(color);
        g2.fillRoundRect(x, baseY - barHeight, barWidth, barHeight, 10, 10);

        FontMetrics fm = g2.getFontMetrics();
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.setColor(Theme.TEXT);
        String v = String.valueOf(value);
        g2.drawString(v, x + barWidth / 2 - fm.stringWidth(v) / 2, baseY - barHeight - 6);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g2.setColor(Theme.MUTED);
        g2.drawString(label, x + barWidth / 2 - fm.stringWidth(label) / 2, baseY + 18);
    }
}
