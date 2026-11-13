package com.attendance.gui;

import com.attendance.dao.UserDAO;
import com.attendance.model.User;
import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final JTextField userField = new JTextField(18);
    private final JPasswordField passField = new JPasswordField(18);
    private final JCheckBox showPassword = new JCheckBox("Show password");
    private final JLabel errorLabel = new JLabel(" ");
    private final char defaultEcho = passField.getEchoChar();

    public LoginFrame() {
        setTitle("Attendance Management System - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(480, 560);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(Theme.BG);
        setLayout(new GridBagLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        form.setPreferredSize(new Dimension(400, 470));

        JLabel logo = new JLabel("AMS", SwingConstants.CENTER);
        logo.setOpaque(true);
        logo.setBackground(Theme.ACCENT);
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("SansSerif", Font.BOLD, 20));
        logo.setPreferredSize(new Dimension(64, 64));

        JLabel title = new JLabel("Attendance Management System", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        JLabel sub = Theme.muted("Sign in to continue");
        sub.setHorizontalAlignment(SwingConstants.CENTER);

        styleField(userField);
        styleField(passField);
        showPassword.setBackground(Color.WHITE);
        showPassword.addActionListener(e -> passField.setEchoChar(showPassword.isSelected() ? (char) 0 : defaultEcho));
        errorLabel.setForeground(Theme.RED);

        JButton loginBtn = new JButton("Login");
        JButton clearBtn = new JButton("Clear");
        Theme.styleButton(loginBtn);
        Theme.styleButton(clearBtn);
        loginBtn.addActionListener(e -> login());
        clearBtn.addActionListener(e -> clearForm());
        getRootPane().setDefaultButton(loginBtn);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttons.setBackground(Color.WHITE);
        buttons.add(loginBtn);
        buttons.add(clearBtn);

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.insets = new Insets(0, 0, 0, 0);
        int row = 0;
        GridBagConstraints logoConstraints = (GridBagConstraints) g.clone();
        logoConstraints.fill = GridBagConstraints.NONE; // logo keeps its size and stays centered
        logoConstraints.gridy = row++;
        form.add(logo, logoConstraints);
        g.gridy = row++; g.insets = new Insets(14, 0, 2, 0); form.add(title, g);
        g.gridy = row++; g.insets = new Insets(0, 0, 0, 0); form.add(sub, g);
        g.gridy = row++; g.insets = new Insets(26, 0, 4, 0); form.add(fieldLabel("Username"), g);
        g.gridy = row++; g.insets = new Insets(0, 0, 0, 0); form.add(userField, g);
        g.gridy = row++; g.insets = new Insets(14, 0, 4, 0); form.add(fieldLabel("Password"), g);
        g.gridy = row++; g.insets = new Insets(0, 0, 0, 0); form.add(passField, g);
        g.gridy = row++; g.insets = new Insets(6, 0, 0, 0); form.add(showPassword, g);
        g.gridy = row++; g.insets = new Insets(8, 0, 0, 0); form.add(errorLabel, g);
        g.gridy = row++; g.insets = new Insets(18, 0, 0, 0); form.add(buttons, g);

        add(form);
        Theme.apply(form);
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setForeground(Theme.MUTED);
        return l;
    }

    private void styleField(JTextField f) {
        f.setFont(new Font("SansSerif", Font.PLAIN, 14));
        f.setPreferredSize(new Dimension(300, 38));
    }

    private void clearForm() {
        userField.setText("");
        passField.setText("");
        errorLabel.setText(" ");
        userField.requestFocus();
    }

    private void login() {
        String username = userField.getText().trim();
        String password = new String(passField.getPassword());
        if (username.isEmpty()) {
            errorLabel.setText("Please enter your username.");
            userField.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            errorLabel.setText("Please enter your password.");
            passField.requestFocus();
            return;
        }
        try {
            User user = new UserDAO().login(username, password);
            if (user == null) {
                errorLabel.setText("Incorrect username or password.");
                passField.setText("");
                passField.requestFocus();
                return;
            }
            new DashboardFrame(user.username()).setVisible(true);
            dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
            errorLabel.setText("Could not connect to the database. Check that MySQL is running.");
        }
    }
}
