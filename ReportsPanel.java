package com.attendance.gui;

import com.attendance.dao.AttendanceDAO;
import com.attendance.dao.StudentDAO;
import com.attendance.model.AttendanceRecord;
import com.attendance.model.StudentSummary;
import com.attendance.util.CsvExporter;
import com.attendance.util.DBConnection;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class ReportsPanel extends JPanel {
    private final DashboardFrame frame;
    private final StudentDAO studentDAO = new StudentDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final JLabel studentsLbl = new JLabel("0");
    private final JLabel classesLbl = new JLabel("0");
    private final JLabel percentLbl = new JLabel("-");
    private final JLabel lowLbl = new JLabel("0");
    private final SummaryTable summaryTable = new SummaryTable();

    public ReportsPanel(DashboardFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel stats = new JPanel(new GridLayout(1, 4, 14, 0));
        stats.setOpaque(false);
        stats.add(Theme.statCard("Total Students", studentsLbl, Theme.ACCENT));
        stats.add(Theme.statCard("Total Classes", classesLbl, Theme.PURPLE));
        stats.add(Theme.statCard("Overall Attendance", percentLbl, Theme.GREEN));
        stats.add(Theme.statCard("Below 75%", lowLbl, Theme.RED));

        JButton exportRecords = new JButton("Export Daily Records (CSV)");
        JButton exportSummary = new JButton("Export Student Summary (CSV)");
        JButton printBtn = new JButton("Print Student Summary");
        JButton refreshBtn = new JButton("Refresh");
        JButton backBtn = new JButton("Back to Dashboard");
        exportRecords.addActionListener(e -> exportDailyRecords());
        exportSummary.addActionListener(e -> exportStudentSummary());
        printBtn.addActionListener(e -> printSummary());
        refreshBtn.addActionListener(e -> refresh());
        backBtn.addActionListener(e -> frame.navigate("Dashboard"));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        for (JButton b : new JButton[]{exportRecords, exportSummary, printBtn}) actions.add(b);

        JPanel top = new JPanel(new BorderLayout(0, 16));
        top.setOpaque(false);
        top.add(Theme.pageHeader("Reports & Export", refreshBtn, backBtn), BorderLayout.NORTH);
        top.add(stats, BorderLayout.CENTER);

        JPanel card = Theme.card(Theme.ACCENT, false);
        card.add(actions, BorderLayout.NORTH);
        card.add(new JScrollPane(summaryTable), BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(card, BorderLayout.CENTER);
        Theme.apply(this);
    }

    public void refresh() {
        try {
            List<StudentSummary> summaries = attendanceDAO.getStudentSummaries();
            int allPresent = attendanceDAO.countEntries("PRESENT");
            int allMarked = allPresent + attendanceDAO.countEntries("ABSENT");
            studentsLbl.setText(String.valueOf(studentDAO.count()));
            classesLbl.setText(String.valueOf(attendanceDAO.countClasses()));
            percentLbl.setText(allMarked == 0 ? "-" : Theme.percentText(StudentSummary.percent(allPresent, allMarked)));
            lowLbl.setText(String.valueOf(summaries.stream().filter(StudentSummary::isLow).count()));
            summaryTable.reload(summaries);
        } catch (Exception ex) {
            DBConnection.showError(this, "load the report", ex);
        }
    }

    private void exportDailyRecords() {
        try {
            List<String[]> rows = new ArrayList<>();
            rows.add(new String[]{"Date", "Student ID", "Roll No", "Student Name", "Status"});
            for (AttendanceRecord r : attendanceDAO.getRecords("", "")) {
                rows.add(new String[]{r.date(), r.studentId(), r.rollNo(), r.name(), r.status()});
            }
            saveCsv("attendance_records.csv", rows, "Exported " + (rows.size() - 1) + " attendance records to:\n");
        } catch (Exception ex) {
            DBConnection.showError(this, "export the records", ex);
        }
    }

    private void exportStudentSummary() {
        try {
            List<String[]> rows = new ArrayList<>();
            rows.add(new String[]{"Student ID", "Roll No", "Student Name", "Branch", "Semester",
                    "Present", "Total Classes", "Attendance %"});
            for (StudentSummary s : attendanceDAO.getStudentSummaries()) {
                rows.add(new String[]{s.studentId(), s.rollNo(), s.name(), s.branch(),
                        String.valueOf(s.semester()), String.valueOf(s.present()), String.valueOf(s.total()),
                        s.hasClasses() ? Theme.percentText(s.percentage()) : "-"});
            }
            saveCsv("student_summary.csv", rows, "Exported the student summary to:\n");
        } catch (Exception ex) {
            DBConnection.showError(this, "export the summary", ex);
        }
    }

    // Asks where to save, makes sure the file ends in .csv, then writes it
    private void saveCsv(String defaultName, List<String[]> rows, String successPrefix) throws Exception {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(defaultName));
        chooser.setFileFilter(new FileNameExtensionFilter("CSV files (*.csv)", "csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getParentFile(), file.getName() + ".csv");
        }
        Path path = file.toPath();
        CsvExporter.write(path, rows);
        Theme.success(this, successPrefix + path);
    }

    private void printSummary() {
        try {
            boolean printed = summaryTable.print(JTable.PrintMode.FIT_WIDTH,
                    new MessageFormat("Student Attendance Summary"), new MessageFormat("Page {0}"));
            if (printed) Theme.success(this, "The student summary was sent to the printer.");
        } catch (java.awt.print.PrinterException ex) {
            DBConnection.showError(this, "print the summary", ex);
        }
    }
}
