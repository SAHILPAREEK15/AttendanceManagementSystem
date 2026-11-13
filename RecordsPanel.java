package com.attendance.gui;

import com.attendance.dao.AttendanceDAO;
import com.attendance.model.AttendanceRecord;
import com.attendance.util.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class RecordsPanel extends JPanel {
    private final AttendanceDAO dao = new AttendanceDAO();
    private final JTextField dateField = new JTextField(11);
    private final JTextField searchField = new JTextField(14);
    private final JTextField summarySearch = new JTextField(18);
    private final DefaultTableModel dailyModel = new DefaultTableModel(
            new Object[]{"Date", "Student ID", "Roll No", "Student Name", "Status"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable dailyTable = new JTable(dailyModel);
    private final SummaryTable summaryTable = new SummaryTable();

    public RecordsPanel(DashboardFrame frame) {
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JButton backBtn = new JButton("Back to Dashboard");
        backBtn.addActionListener(e -> frame.navigate("Dashboard"));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Daily Records", buildDailyTab());
        tabs.addTab("Student Summary", buildSummaryTab());

        add(Theme.pageHeader("Attendance Records", backBtn), BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        Theme.apply(this);
    }

    private JPanel buildDailyTab() {
        JButton searchBtn = new JButton("Search");
        JButton filterBtn = new JButton("Filter by Date");
        JButton clearBtn = new JButton("Clear Filter");
        JButton refreshBtn = new JButton("Refresh");
        searchBtn.addActionListener(e -> loadDaily("", searchField.getText().trim()));
        filterBtn.addActionListener(e -> loadDaily(dateField.getText().trim(), ""));
        clearBtn.addActionListener(e -> {
            dateField.setText("");
            searchField.setText("");
            loadDaily("", "");
        });
        refreshBtn.addActionListener(e -> refresh());

        JPanel controls = new JPanel(new GridLayout(2, 1, 0, 8));
        controls.setOpaque(false);
        controls.add(row(new JLabel("Date (yyyy-mm-dd):"), dateField, filterBtn, clearBtn));
        controls.add(row(new JLabel("Name / Roll:"), searchField, searchBtn, refreshBtn));
        return tab(controls, new JScrollPane(dailyTable));
    }

    private JPanel buildSummaryTab() {
        JButton searchBtn = new JButton("Search");
        JButton showAllBtn = new JButton("Show All");
        JButton refreshBtn = new JButton("Refresh");
        searchBtn.addActionListener(e -> summaryTable.filter(summarySearch.getText()));
        summarySearch.addActionListener(e -> summaryTable.filter(summarySearch.getText()));
        showAllBtn.addActionListener(e -> {
            summarySearch.setText("");
            summaryTable.filter("");
        });
        refreshBtn.addActionListener(e -> loadSummary());

        return tab(row(new JLabel("Search by ID, name or roll no:"), summarySearch, searchBtn, showAllBtn, refreshBtn),
                new JScrollPane(summaryTable));
    }

    private JPanel row(JComponent... items) {
        JPanel r = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        r.setOpaque(false);
        for (JComponent c : items) r.add(c);
        return r;
    }

    private JPanel tab(JComponent controls, JComponent body) {
        JPanel tab = new JPanel(new BorderLayout(0, 12));
        tab.setBackground(Color.WHITE);
        tab.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        tab.add(controls, BorderLayout.NORTH);
        tab.add(body, BorderLayout.CENTER);
        return tab;
    }

    // Reloads both tabs using the current filter boxes
    public void refresh() {
        loadDaily(dateField.getText().trim(), searchField.getText().trim());
        loadSummary();
    }

    private void loadSummary() {
        try {
            summaryTable.reload(dao.getStudentSummaries());
        } catch (Exception ex) {
            DBConnection.showError(this, "load the attendance summary", ex);
        }
    }

    private void loadDaily(String date, String search) {
        if (!date.isEmpty()) {
            try {
                LocalDate.parse(date);
            } catch (DateTimeParseException ex) {
                Theme.warning(this, "Date must be in yyyy-mm-dd format, for example 2026-10-26.");
                return;
            }
        }
        try {
            List<AttendanceRecord> rows = dao.getRecords(date, search);
            dailyModel.setRowCount(0);
            for (AttendanceRecord r : rows) {
                dailyModel.addRow(new Object[]{r.date(), r.studentId(), r.rollNo(), r.name(), r.status()});
            }
        } catch (Exception ex) {
            DBConnection.showError(this, "load attendance records", ex);
        }
    }
}
