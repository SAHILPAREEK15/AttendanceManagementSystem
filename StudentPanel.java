package com.attendance.gui;

import com.attendance.dao.StudentDAO;
import com.attendance.model.Student;
import com.attendance.util.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.regex.Pattern;

public class StudentPanel extends JPanel {
    private static final Pattern ID_PATTERN = Pattern.compile("[A-Za-z0-9]{1,20}");
    private static final Pattern ROLL_PATTERN = Pattern.compile("[A-Za-z0-9-]{1,20}");
    private static final Pattern NAME_PATTERN = Pattern.compile("[A-Za-z][A-Za-z .'-]{1,99}");

    private final DashboardFrame frame;
    private final StudentDAO dao = new StudentDAO();
    private final JTextField sidField = new JTextField(12);
    private final JTextField nameField = new JTextField(12);
    private final JTextField rollField = new JTextField(12);
    private final JTextField branchField = new JTextField(12);
    private final JTextField semField = new JTextField(12);
    private final JTextField searchField = new JTextField(18);
    private static final String[] COLUMNS = {"ID", "Student ID", "Name", "Roll No", "Branch", "Semester"};
    private final DefaultTableModel model = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
    private int selectedId = -1; // database id of the selected student

    public StudentPanel(DashboardFrame frame) {
        this.frame = frame;
        table.setRowSorter(sorter);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel form = new JPanel(new GridLayout(0, 4, 12, 12));
        form.setOpaque(false);
        form.add(new JLabel("Student ID:")); form.add(sidField);
        form.add(new JLabel("Name:"));       form.add(nameField);
        form.add(new JLabel("Roll No:"));    form.add(rollField);
        form.add(new JLabel("Branch:"));     form.add(branchField);
        form.add(new JLabel("Semester (1-8):")); form.add(semField);
        form.add(new JLabel(""));            form.add(new JLabel(""));

        JButton addBtn = new JButton("Add Student");
        JButton updBtn = new JButton("Update Student");
        JButton delBtn = new JButton("Delete Student");
        JButton clrBtn = new JButton("Clear");
        JButton backBtn = new JButton("Back to Dashboard");
        addBtn.addActionListener(e -> saveStudent(true));
        updBtn.addActionListener(e -> saveStudent(false));
        delBtn.addActionListener(e -> deleteStudent());
        clrBtn.addActionListener(e -> clearForm());
        backBtn.addActionListener(e -> frame.navigate("Dashboard"));
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        for (JButton b : new JButton[]{addBtn, updBtn, delBtn, clrBtn}) buttons.add(b);

        JPanel formCard = Theme.card(Theme.ACCENT, false);
        JPanel formBody = new JPanel(new BorderLayout(0, 14));
        formBody.setOpaque(false);
        formBody.add(form, BorderLayout.CENTER);
        formBody.add(buttons, BorderLayout.SOUTH);
        formCard.add(formBody, BorderLayout.CENTER);

        JButton searchBtn = new JButton("Search");
        JButton showAllBtn = new JButton("Show All");
        searchBtn.addActionListener(e -> applySearch());
        showAllBtn.addActionListener(e -> { searchField.setText(""); applySearch(); });
        searchField.addActionListener(e -> applySearch());
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchRow.setOpaque(false);
        searchRow.add(new JLabel("Search by ID, name, roll no or branch:"));
        searchRow.add(searchField);
        searchRow.add(searchBtn);
        searchRow.add(showAllBtn);
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refresh());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillFormFromSelection();
        });

        JPanel north = new JPanel(new BorderLayout(0, 16));
        north.setOpaque(false);
        north.add(Theme.pageHeader("Students", refreshBtn, backBtn), BorderLayout.NORTH);
        north.add(formCard, BorderLayout.CENTER);

        JPanel listCard = Theme.card(Theme.PURPLE, false);
        JPanel listHead = new JPanel(new BorderLayout());
        listHead.setOpaque(false);
        listHead.add(searchRow, BorderLayout.CENTER);
        listCard.add(listHead, BorderLayout.NORTH);
        listCard.add(new JScrollPane(table), BorderLayout.CENTER);

        add(north, BorderLayout.NORTH);
        add(listCard, BorderLayout.CENTER);
        Theme.apply(this);
    }

    public void refresh() {
        try {
            java.util.List<Student> list = dao.getAll();
            Object[][] rows = new Object[list.size()][];
            for (int i = 0; i < rows.length; i++) {
                Student s = list.get(i);
                rows[i] = new Object[]{s.id(), s.studentId(), s.name(), s.rollNo(), s.branch(), s.semester()};
            }
            // One data change event instead of one per row keeps the sorted table consistent
            table.clearSelection();
            model.setDataVector(rows, COLUMNS);
            hideIdColumn();
            applySearch();
        } catch (Exception ex) {
            DBConnection.showError(this, "load the student list", ex);
        }
    }

    // The database ID is needed for edit and delete but is not shown to the user
    private void hideIdColumn() {
        table.removeColumn(table.getColumn("ID"));
    }

    private void applySearch() {
        String q = searchField.getText().trim();
        sorter.setRowFilter(q.isEmpty() ? null
                : javax.swing.RowFilter.regexFilter("(?i)" + Pattern.quote(q), 1, 2, 3, 4));
    }

    private void saveStudent(boolean isNew) {
        if (!isNew && selectedId < 0) {
            Theme.warning(this, "Select a student in the table to update.");
            return;
        }
        Student s = readForm();
        if (s == null) return;
        try {
            if (isNew) dao.add(s); else dao.update(s);
            Theme.success(this, isNew ? "Student \"" + s.name() + "\" was added." : "Student details were updated.");
            clearForm();
            refresh();
        } catch (SQLIntegrityConstraintViolationException ex) {
            Theme.warning(this, "A student with this Student ID or Roll No already exists.");
        } catch (SQLException ex) {
            DBConnection.showError(this, isNew ? "add the student" : "update the student", ex);
        }
    }

    private void deleteStudent() {
        if (selectedId < 0) {
            Theme.warning(this, "Select a student in the table to delete.");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "Delete this student?\nTheir attendance records will also be removed.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            dao.delete(selectedId);
            Theme.success(this, "Student was deleted.");
            clearForm();
            refresh();
        } catch (SQLException ex) {
            DBConnection.showError(this, "delete the student", ex);
        }
    }

    // Returns null (and shows a message) when the form has invalid data
    private Student readForm() {
        String sid = sidField.getText().trim();
        String name = nameField.getText().trim();
        String roll = rollField.getText().trim();
        String branch = branchField.getText().trim();
        String sem = semField.getText().trim();
        if (sid.isEmpty() || name.isEmpty() || roll.isEmpty() || branch.isEmpty() || sem.isEmpty()) {
            Theme.warning(this, "Please fill in all the fields.");
            return null;
        }
        if (!ID_PATTERN.matcher(sid).matches()) {
            Theme.warning(this, "Student ID can contain only letters and numbers (up to 20 characters).");
            return null;
        }
        if (!NAME_PATTERN.matcher(name).matches()) {
            Theme.warning(this, "Name must start with a letter and contain only letters, spaces, dots, apostrophes or hyphens.");
            return null;
        }
        if (!ROLL_PATTERN.matcher(roll).matches()) {
            Theme.warning(this, "Roll No can contain only letters, numbers and hyphens (up to 20 characters).");
            return null;
        }
        if (branch.length() > 50) {
            Theme.warning(this, "Branch name is too long (maximum 50 characters).");
            return null;
        }
        int semester;
        try {
            semester = Integer.parseInt(sem);
        } catch (NumberFormatException ex) {
            Theme.warning(this, "Semester must be a number from 1 to 8.");
            return null;
        }
        if (semester < 1 || semester > 8) {
            Theme.warning(this, "Semester must be a number from 1 to 8.");
            return null;
        }
        return new Student(selectedId, sid, name, roll, branch, semester);
    }

    // Reads the row through the sorter so the correct student is picked even when the table is sorted or filtered
    private void fillFormFromSelection() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0 || viewRow >= table.getRowCount()) return;
        int row = table.convertRowIndexToModel(viewRow);
        selectedId = (Integer) model.getValueAt(row, 0);
        sidField.setText((String) model.getValueAt(row, 1));
        nameField.setText((String) model.getValueAt(row, 2));
        rollField.setText((String) model.getValueAt(row, 3));
        branchField.setText((String) model.getValueAt(row, 4));
        semField.setText(String.valueOf(model.getValueAt(row, 5)));
    }

    private void clearForm() {
        selectedId = -1;
        sidField.setText("");
        nameField.setText("");
        rollField.setText("");
        branchField.setText("");
        semField.setText("");
        table.clearSelection();
    }
}
