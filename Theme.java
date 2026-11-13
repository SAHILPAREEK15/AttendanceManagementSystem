package com.attendance.gui;

import com.attendance.model.StudentSummary;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class Theme {
    public static final Color SIDEBAR = new Color(30, 41, 59);
    public static final Color SIDEBAR_HOVER = new Color(51, 65, 85);
    public static final Color ACCENT = new Color(37, 99, 235);
    public static final Color BG = new Color(243, 244, 248);
    public static final Color TEXT = new Color(17, 24, 39);
    public static final Color MUTED = new Color(107, 114, 128);
    public static final Color GREEN = new Color(22, 163, 74);
    public static final Color RED = new Color(220, 38, 38);
    public static final Color PURPLE = new Color(147, 51, 234);
    private static final Color LIGHT_BORDER = new Color(229, 231, 235);

    private Theme() {}

    // Metal look and feel is used because it respects button colors
    public static void install() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) { }
    }

    public static void styleButton(JButton b) {
        String t = b.getText();
        Color base = ACCENT;
        Color fg = Color.WHITE;
        if (t.equals("Mark Present")) base = GREEN;
        else if (t.equals("Mark Absent")) base = RED;
        else if (t.startsWith("Back") || t.startsWith("Clear") || t.startsWith("Refresh") || t.startsWith("Reset")) {
            base = LIGHT_BORDER;
            fg = TEXT;
        }
        final Color normal = base;
        b.setBackground(normal);
        b.setForeground(fg);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(normal.darker()); }
            @Override public void mouseExited(MouseEvent e) { b.setBackground(normal); }
        });
    }

    public static void styleTable(JTable t) {
        t.setRowHeight(34);
        t.setFont(new Font("SansSerif", Font.PLAIN, 13));
        t.setForeground(TEXT);
        t.setBackground(Color.WHITE);
        t.setGridColor(LIGHT_BORDER);
        t.setShowVerticalLines(false);
        t.setSelectionBackground(new Color(219, 234, 254));
        t.setSelectionForeground(TEXT);
        JTableHeader h = t.getTableHeader();
        h.setBackground(new Color(249, 250, 251));
        h.setForeground(MUTED);
        h.setFont(new Font("SansSerif", Font.BOLD, 12));
        for (int i = 0; i < t.getColumnCount(); i++) {
            String name = t.getColumnName(i);
            if ("Status".equals(name)) t.getColumnModel().getColumn(i).setCellRenderer(new StatusRenderer());
            if ("Attendance %".equals(name)) t.getColumnModel().getColumn(i).setCellRenderer(new PercentRenderer());
        }
    }

    // Applies button, table and text field styling to everything inside a container
    public static void apply(Container c) {
        if (c instanceof JPanel && c.getBackground().equals(UIManager.getColor("Panel.background"))) {
            c.setBackground(BG);
        }
        for (Component comp : c.getComponents()) {
            if (comp instanceof JButton b) styleButton(b);
            else if (comp instanceof JTable t) styleTable(t);
            else if (comp instanceof JScrollPane sp) sp.getViewport().setBackground(Color.WHITE);
            else if (comp instanceof JTextField f) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(209, 213, 219)),
                        BorderFactory.createEmptyBorder(6, 8, 6, 8)));
            }
            if (comp instanceof Container inner) apply(inner);
        }
    }

    // White card with a light border and a colored accent (left edge for stat cards, top edge for panels)
    public static JPanel card(Color accent, boolean leftSide) {
        JPanel c = new JPanel(new BorderLayout());
        c.setBackground(Color.WHITE);
        javax.swing.border.Border accentLine = leftSide
                ? BorderFactory.createMatteBorder(0, 5, 0, 0, accent)
                : BorderFactory.createMatteBorder(4, 0, 0, 0, accent);
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LIGHT_BORDER), accentLine),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));
        return c;
    }

    public static JPanel statCard(String title, JLabel value, Color accent) {
        JPanel c = card(accent, true);
        JLabel t = new JLabel(title);
        t.setForeground(MUTED);
        t.setFont(new Font("SansSerif", Font.BOLD, 13));
        value.setFont(new Font("SansSerif", Font.BOLD, 28));
        value.setForeground(accent);
        c.add(t, BorderLayout.NORTH);
        c.add(value, BorderLayout.CENTER);
        return c;
    }

    public static JLabel pageTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 24));
        l.setForeground(TEXT);
        return l;
    }

    // Page title on the left, action buttons on the right
    public static JPanel pageHeader(String title, JButton... buttons) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(pageTitle(title), BorderLayout.WEST);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        for (JButton b : buttons) right.add(b);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    public static JLabel muted(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(MUTED);
        return l;
    }

    public static String percentText(double value) {
        return String.format("%.1f%%", value);
    }

    public static void success(java.awt.Component parent, String text) {
        JOptionPane.showMessageDialog(parent, text, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void warning(java.awt.Component parent, String text) {
        JOptionPane.showMessageDialog(parent, text, "Please Check", JOptionPane.WARNING_MESSAGE);
    }

    // Colors PRESENT green and ABSENT red
    static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                                                       boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, value, selected, focus, row, col);
            String s = String.valueOf(value);
            setForeground(s.equals("PRESENT") ? GREEN : s.equals("ABSENT") ? RED : TEXT);
            setFont(new Font("SansSerif", Font.BOLD, 12));
            return this;
        }
    }

    // Shows percentages in green at or above the limit, red below it, and a dash when there are no classes
    static class PercentRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                                                       boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, value, selected, focus, row, col);
            if (value instanceof Number n) {
                double p = n.doubleValue();
                setText(percentText(p));
                setForeground(p < StudentSummary.LOW_LIMIT ? RED : GREEN);
            } else {
                setText("-");
                setForeground(MUTED);
            }
            setFont(new Font("SansSerif", Font.BOLD, 12));
            return this;
        }
    }
}
