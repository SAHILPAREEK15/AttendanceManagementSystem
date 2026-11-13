package com.attendance.gui;

import com.attendance.model.StudentSummary;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.util.List;
import java.util.regex.Pattern;

// Table showing each student's present count, total classes and attendance percentage
public class SummaryTable extends JTable {
    private static final String[] COLUMNS = {
            "Student ID", "Roll No", "Student Name", "Branch", "Semester", "Present", "Total Classes", "Attendance %"
    };
    private static final int[] WIDTHS = {90, 80, 160, 170, 80, 80, 110, 120};
    private final DefaultTableModel model;
    private final TableRowSorter<DefaultTableModel> sorter;

    public SummaryTable() {
        super(new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });
        model = (DefaultTableModel) getModel();
        sorter = new TableRowSorter<>(model);
        setRowSorter(sorter);
        applyLayout();
    }

    // setDataVector rebuilds the columns, so renderers and widths must be set again after each reload
    private void applyLayout() {
        Theme.styleTable(this);
        for (int i = 0; i < WIDTHS.length && i < getColumnCount(); i++) {
            getColumnModel().getColumn(i).setPreferredWidth(WIDTHS[i]);
        }
    }

    public void reload(List<StudentSummary> list) {
        Object[][] rows = new Object[list.size()][];
        for (int i = 0; i < rows.length; i++) {
            StudentSummary s = list.get(i);
            Object percent = s.hasClasses() ? (Object) s.percentage() : null;
            rows[i] = new Object[]{s.studentId(), s.rollNo(), s.name(), s.branch(),
                    s.semester(), s.present(), s.total(), percent};
        }
        // One data change event instead of one per row keeps the sorted table consistent
        clearSelection();
        model.setDataVector(rows, COLUMNS);
        applyLayout();
    }

    // Filters rows where ID, roll number or name contains the text. Blank text shows all rows.
    public void filter(String text) {
        String q = text == null ? "" : text.trim();
        sorter.setRowFilter(q.isEmpty() ? null
                : RowFilter.regexFilter("(?i)" + Pattern.quote(q), 0, 1, 2));
    }
}
