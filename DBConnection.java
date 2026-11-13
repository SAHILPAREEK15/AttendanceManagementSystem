package com.attendance.util;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // CHANGE the password to your MySQL root password
    private static final String URL =
            "jdbc:mysql://localhost:3306/attendance_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "Mysql@0006";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Shows a plain-language message to the user; the technical details go to the console for developers
    public static void showError(Component parent, String action, Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(parent,
                "Could not " + action + ". Please try again.\n"
                        + "If the problem continues, make sure MySQL is running.",
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}
