package com.attendance.gui;

import com.attendance.dao.AttendanceDAO;
import com.attendance.dao.StudentDAO;
import com.attendance.model.Student;
import com.attendance.util.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class AttendancePanel extends JPanel {
    private static final String PRESENT = "PRESENT";
    private static final String ABSENT = "ABSENT";

    private final DashboardFrame frame;
    private final StudentDAO studentDAO = new StudentDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    // Future dates are blocked by the spinner's maximum value
    private final JSpinner dateSpinner = new JSpinner(
            new SpinnerDateModel(new Date(), null, new Date(), Calendar.DAY_OF_MONTH));
    private final JLabel modeLabel = new JLabel(" ");
    private final JLabel countLabel = new JLabel(" ");
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Student ID", "Roll No", "Student Name", "Status"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    public AttendancePanel(DashboardFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        dateSpinner.addChangeListener(e -> refresh());

        JButton markPresent = new JButton("Mark Present");
        JButton markAbsent = new JButton("Mark Absent");
        JButton allPresent = new JButton("Mark All Present");
        JButton allAbsent = new JButton("Mark All Absent");
        JButton saveBtn = new JButton("Save Attendance");
        JButton resetBtn = new JButton("Reset");
        JButton backBtn = new JButton("Back to Dashboard");
        markPresent.addActionListener(e -> markSelected(PRESENT));
        markAbsent.addActionListener(e -> markSelected(ABSENT));
        allPresent.addActionListener(e -> markAll(PRESENT));
        allAbsent.addActionListener(e -> markAll(ABSENT));
        saveBtn.addActionListener(e -> save());
        resetBtn.addActionListener(e -> {
            dateSpinner.setValue(new Date());
            refresh();
        });
        backBtn.addActionListener(e -> frame.navigate("Dashboard"));

        JPanel dateRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        dateRow.setOpaque(false);
        dateRow.add(new JLabel("Date:"));
        dateRow.add(dateSpinner);
        dateRow.add(resetBtn);
        dateRow.add(backBtn);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionRow.setOpaque(false);
        for (JButton b : new JButton[]{markPresent, markAbsent, allPresent, allAbsent, saveBtn}) actionRow.add(b);

        JPanel controls = new JPanel(new BorderLayout(0, 12));
        controls.setOpaque(false);
        controls.add(dateRow, BorderLayout.NORTH);
        controls.add(actionRow, BorderLayout.CENTER);

        JPanel statusRow = new JPanel(new BorderLayout());
        statusRow.setOpaque(false);
        modeLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        statusRow.add(modeLabel, BorderLayout.WEST);
        statusRow.add(countLabel, BorderLayout.EAST);

        JPanel card = Theme.card(Theme.ACCENT, false);
        JPanel head = new JPanel(new BorderLayout(0, 12));
        head.setOpaque(false);
        head.add(controls, BorderLayout.NORTH);
        head.add(statusRow, BorderLayout.SOUTH);
        card.add(head, BorderLayout.NORTH);
        card.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(Theme.pageTitle("Mark Attendance"), BorderLayout.NORTH);

        add(north, BorderLayout.NORTH);
        add(card, BorderLayout.CENTER);
        Theme.apply(this);
    }

    public void refresh() {
        LocalDate date = selectedDate();
        try {
            Map<String, String> saved = attendanceDAO.getStatusesForDate(date.toString());
            model.setRowCount(0);
            for (Student s : studentDAO.getAll()) {
                // Students without a saved status default to PRESENT
                model.addRow(new Object[]{s.studentId(), s.rollNo(), s.name(),
                        saved.getOrDefault(s.studentId(), PRESENT)});
            }
            modeLabel.setText(saved.isEmpty()
                    ? "New attendance for " + date
                    : "Attendance already saved for " + date + ". Edit and save to update it.");
            updateCounts();
        } catch (Exception ex) {
            DBConnection.showError(this, "load students for attendance", ex);
        }
    }

    private LocalDate selectedDate() {
        Date value = (Date) dateSpinner.getValue();
        return value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void markSelected(String status) {
        int[] rows = table.getSelectedRows();
        if (rows.length == 0) {
            Theme.warning(this, "Select one or more students in the table first.");
            return;
        }
        for (int r : rows) model.setValueAt(status, r, 3);
        updateCounts();
    }

    private void markAll(String status) {
        if (model.getRowCount() == 0) {
            Theme.warning(this, "There are no students to mark. Add students first.");
            return;
        }
        for (int r = 0; r < model.getRowCount(); r++) model.setValueAt(status, r, 3);
        updateCounts();
    }

    private void save() {
        LocalDate date = selectedDate();
        if (date.isAfter(LocalDate.now())) {
            Theme.warning(this, "Attendance cannot be saved for a future date.");
            return;
        }
        if (model.getRowCount() == 0) {
            Theme.warning(this, "There are no students to mark. Add students first.");
            return;
        }
        try {
            boolean alreadySaved = !attendanceDAO.getStatusesForDate(date.toString()).isEmpty();
            if (alreadySaved) {
                int ok = JOptionPane.showConfirmDialog(this,
                        "Attendance for " + date + " is already saved.\nReplace it with these values?",
                        "Confirm Update", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (ok != JOptionPane.YES_OPTION) return;
            }
            Map<String, String> statuses = new LinkedHashMap<>();
            for (int i = 0; i < model.getRowCount(); i++) {
                statuses.put((String) model.getValueAt(i, 0), (String) model.getValueAt(i, 3));
            }
            attendanceDAO.saveForDate(date.toString(), statuses);
            int present = countStatus(PRESENT);
            Theme.success(this, "Attendance " + (alreadySaved ? "updated" : "saved") + " for " + date
                    + ".\nPresent: " + present + ", Absent: " + (statuses.size() - present));
            refresh();
        } catch (Exception ex) {
            DBConnection.showError(this, "save attendance", ex);
        }
    }

    private int countStatus(String status) {
        int n = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            if (status.equals(model.getValueAt(i, 3))) n++;
        }
        return n;
    }

    private void updateCounts() {
        int present = countStatus(PRESENT);
        int total = model.getRowCount();
        countLabel.setText("Present: " + present + "   Absent: " + (total - present) + "   Total: " + total);
    }
}
