package com.attendance.gui;

import com.attendance.dao.AttendanceDAO;
import com.attendance.dao.StudentDAO;
import com.attendance.model.StudentSummary;
import com.attendance.util.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardFrame extends JFrame {
    private static final String DASHBOARD = "Dashboard";
    private static final String STUDENTS = "Students";
    private static final String MARK = "Mark Attendance";
    private static final String RECORDS = "Attendance Records";
    private static final String REPORTS = "Reports / Export";
    private static final String[] PAGES = {DASHBOARD, STUDENTS, MARK, RECORDS, REPORTS};
    private static final Map<String, String> ICONS = Map.of(
            DASHBOARD, "\u2302", STUDENTS, "\u263A", MARK, "\u2714",
            RECORDS, "\u25A4", REPORTS, "\u21E9");
    private static final Color NAV_TEXT = new Color(203, 213, 225);

    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();
    private String activePage = DASHBOARD;

    private final JLabel totalStudentsLbl = new JLabel("0");
    private final JLabel totalClassesLbl = new JLabel("0");
    private final JLabel presentTodayLbl = new JLabel("0");
    private final JLabel absentTodayLbl = new JLabel("0");
    private final JLabel percentLbl = new JLabel("-");
    private final JLabel lowCountLbl = new JLabel(" ");
    private final JPanel alertBanner = new JPanel(new BorderLayout());
    private final JLabel alertText = new JLabel();
    private final AttendanceChart chart = new AttendanceChart();
    private final DefaultTableModel lowModel = new DefaultTableModel(
            new Object[]{"Student Name", "Roll No", "Attendance %"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable lowTable = new JTable(lowModel);

    private final StudentDAO studentDAO = new StudentDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final StudentPanel studentPanel = new StudentPanel(this);
    private final AttendancePanel attendancePanel = new AttendancePanel(this);
    private final RecordsPanel recordsPanel = new RecordsPanel(this);
    private final ReportsPanel reportsPanel = new ReportsPanel(this);

    public DashboardFrame(String username) {
        setTitle("Attendance Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1120, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BG);
        Theme.styleTable(lowTable);

        add(buildSidebar(), BorderLayout.WEST);
        add(buildHeader(username), BorderLayout.NORTH);
        content.add(buildDashboard(), DASHBOARD);
        content.add(studentPanel, STUDENTS);
        content.add(attendancePanel, MARK);
        content.add(recordsPanel, RECORDS);
        content.add(reportsPanel, REPORTS);
        add(content, BorderLayout.CENTER);

        navigate(DASHBOARD);
    }

    // Single place that switches pages, reloads their data from MySQL and highlights the active menu item
    public void navigate(String page) {
        switch (page) {
            case DASHBOARD -> loadStats();
            case STUDENTS -> studentPanel.refresh();
            case MARK -> attendancePanel.refresh();
            case RECORDS -> recordsPanel.refresh();
            case REPORTS -> reportsPanel.refresh();
            default -> { }
        }
        activePage = page;
        for (Map.Entry<String, JButton> e : navButtons.entrySet()) {
            boolean active = e.getKey().equals(page);
            e.getValue().setBackground(active ? Theme.ACCENT : Theme.SIDEBAR);
            e.getValue().setForeground(active ? Color.WHITE : NAV_TEXT);
        }
        cards.show(content, page);
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(Theme.SIDEBAR);
        side.setPreferredSize(new Dimension(230, 0));

        JLabel brand = new JLabel("Attendance System");
        brand.setForeground(Color.WHITE);
        brand.setFont(new Font("SansSerif", Font.BOLD, 17));
        brand.setBorder(BorderFactory.createEmptyBorder(24, 20, 24, 20));
        brand.setAlignmentX(LEFT_ALIGNMENT);
        side.add(brand);

        for (String page : PAGES) {
            JButton b = navButton(ICONS.get(page) + "   " + page);
            hoverable(b, page);
            b.addActionListener(e -> navigate(page));
            navButtons.put(page, b);
            side.add(b);
        }

        side.add(Box.createVerticalGlue());
        JButton logout = navButton("\u23FB   Logout");
        hoverable(logout, "Logout");
        logout.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        side.add(logout);
        side.add(Box.createVerticalStrut(12));
        return side;
    }

    private JButton navButton(String text) {
        JButton b = new JButton(text);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setPreferredSize(new Dimension(230, 46));
        b.setMinimumSize(new Dimension(230, 46));
        b.setMaximumSize(new Dimension(230, 46));
        b.setAlignmentX(LEFT_ALIGNMENT);
        b.setBorder(BorderFactory.createEmptyBorder(0, 22, 0, 0));
        b.setBackground(Theme.SIDEBAR);
        b.setForeground(NAV_TEXT);
        b.setFont(new Font("SansSerif", Font.PLAIN, 14));
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // Hover lightens inactive items; the active item keeps its highlight
    private void hoverable(JButton b, String key) {
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!key.equals(activePage)) b.setBackground(Theme.SIDEBAR_HOVER);
            }
            @Override public void mouseExited(MouseEvent e) {
                b.setBackground(key.equals(activePage) ? Theme.ACCENT : Theme.SIDEBAR);
            }
        });
    }

    private JPanel buildHeader(String username) {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(Color.WHITE);
        h.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235)),
                BorderFactory.createEmptyBorder(12, 24, 12, 24)));
        JLabel welcome = new JLabel("Welcome, " + username);
        welcome.setFont(new Font("SansSerif", Font.BOLD, 15));
        JLabel date = Theme.muted("Today: " + LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")));
        h.add(welcome, BorderLayout.WEST);
        h.add(date, BorderLayout.EAST);
        return h;
    }

    private JPanel buildDashboard() {
        JPanel page = new JPanel(new BorderLayout(0, 20));
        page.setBackground(Theme.BG);
        page.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel titles = new JPanel(new GridLayout(2, 1));
        titles.setOpaque(false);
        titles.add(Theme.pageTitle("Admin Dashboard"));
        titles.add(Theme.muted("Attendance overview for all students"));
        JButton refresh = new JButton("Refresh");
        Theme.styleButton(refresh);
        refresh.addActionListener(e -> loadStats());
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.add(titles, BorderLayout.WEST);
        titleRow.add(refresh, BorderLayout.EAST);

        alertBanner.setBackground(new Color(254, 242, 242));
        alertBanner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, Theme.RED),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        alertText.setForeground(new Color(153, 27, 27));
        alertText.setFont(new Font("SansSerif", Font.BOLD, 13));
        alertBanner.add(alertText, BorderLayout.CENTER);
        alertBanner.setVisible(false);

        JPanel north = new JPanel(new BorderLayout(0, 14));
        north.setOpaque(false);
        north.add(titleRow, BorderLayout.NORTH);
        north.add(alertBanner, BorderLayout.SOUTH);

        JPanel stats = new JPanel(new GridLayout(1, 5, 14, 0));
        stats.setOpaque(false);
        stats.add(Theme.statCard("Total Students", totalStudentsLbl, Theme.ACCENT));
        stats.add(Theme.statCard("Total Classes", totalClassesLbl, Theme.PURPLE));
        stats.add(Theme.statCard("Present Today", presentTodayLbl, Theme.GREEN));
        stats.add(Theme.statCard("Absent Today", absentTodayLbl, Theme.RED));
        stats.add(Theme.statCard("Attendance %", percentLbl, Theme.ACCENT));

        JPanel chartCard = Theme.card(Theme.ACCENT, false);
        chartCard.setPreferredSize(new Dimension(400, 0));
        chartCard.add(sectionTitle("Present vs Absent (all records)"), BorderLayout.NORTH);
        chartCard.add(chart, BorderLayout.CENTER);

        JPanel lowHead = new JPanel(new BorderLayout());
        lowHead.setOpaque(false);
        lowHead.add(sectionTitle("Low Attendance (below 75%)"), BorderLayout.WEST);
        lowHead.add(lowCountLbl, BorderLayout.EAST);
        JPanel lowCard = Theme.card(Theme.RED, false);
        lowCard.add(lowHead, BorderLayout.NORTH);
        JScrollPane lowScroll = new JScrollPane(lowTable);
        lowScroll.getViewport().setBackground(Color.WHITE);
        lowCard.add(lowScroll, BorderLayout.CENTER);

        JPanel body = new JPanel(new BorderLayout(16, 0));
        body.setOpaque(false);
        body.add(chartCard, BorderLayout.WEST);
        body.add(lowCard, BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 20));
        center.setOpaque(false);
        center.add(stats, BorderLayout.NORTH);
        center.add(body, BorderLayout.CENTER);

        page.add(north, BorderLayout.NORTH);
        page.add(center, BorderLayout.CENTER);
        return page;
    }

    private JLabel sectionTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 16));
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        return l;
    }

    private void loadStats() {
        try {
            int total = studentDAO.count();
            int present = attendanceDAO.countToday("PRESENT");
            int absent = attendanceDAO.countToday("ABSENT");
            int allPresent = attendanceDAO.countEntries("PRESENT");
            int allAbsent = attendanceDAO.countEntries("ABSENT");
            int allMarked = allPresent + allAbsent;
            List<StudentSummary> low = attendanceDAO.getStudentSummaries().stream()
                    .filter(StudentSummary::isLow)
                    .sorted(Comparator.comparingDouble(StudentSummary::percentage))
                    .toList();

            totalStudentsLbl.setText(String.valueOf(total));
            totalClassesLbl.setText(String.valueOf(attendanceDAO.countClasses()));
            presentTodayLbl.setText(String.valueOf(present));
            absentTodayLbl.setText(String.valueOf(absent));
            percentLbl.setText(allMarked == 0 ? "-" : Theme.percentText(StudentSummary.percent(allPresent, allMarked)));
            chart.setValues(allPresent, allAbsent);

            lowModel.setRowCount(0);
            for (StudentSummary s : low) {
                lowModel.addRow(new Object[]{s.name(), s.rollNo(), s.percentage()});
            }
            lowCountLbl.setText(low.size() + " student(s)");
            alertBanner.setVisible(!low.isEmpty());
            alertText.setText(low.size() == 1
                    ? "1 student is below 75% attendance. Check the list below."
                    : low.size() + " students are below 75% attendance. Check the list below.");
        } catch (Exception ex) {
            DBConnection.showError(this, "load the dashboard", ex);
        }
    }
}
