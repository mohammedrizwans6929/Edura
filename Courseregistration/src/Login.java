import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Login extends JPanel {
    private JTextField txtUser;
    private JPasswordField txtPass;
    private MainFrame main;  // Reference the main frame for navigation

    public Login(MainFrame main) {
        this.main = main;
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        setBackground(new Color(245, 247, 250));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // =============== TITLE ===================
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel lblTitle = new JLabel("Student Login", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(52, 152, 219));
        gbc.insets = new Insets(40, 6, 20, 6);
        add(lblTitle, gbc);

        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(6, 6, 6, 6);

        // =============== USERNAME ===================
        gbc.gridy = 1;
        gbc.gridx = 0;
        JLabel lblUser = new JLabel("Admission No:");
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        add(lblUser, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        txtUser = new JTextField();
        styleTextField(txtUser);
        add(txtUser, gbc);

        // =============== PASSWORD ===================
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        add(lblPass, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        txtPass = new JPasswordField();
        styleTextField(txtPass);
        add(txtPass, gbc);

        // =============== BUTTONS ===================
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttonPanel.setOpaque(false);

        JButton btnLogin = new JButton("Login");
        styleButton(btnLogin);

        JButton btnBack = new JButton("Back");
        styleButton(btnBack);

        buttonPanel.add(btnLogin);
        buttonPanel.add(btnBack);
        add(buttonPanel, gbc);

        // =============== FORGOT PASSWORD ===================
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel forgotPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        forgotPanel.setOpaque(false);
        JLabel lblForgot = new JLabel("<html><u>Forgot Password?</u></html>");
        lblForgot.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblForgot.setForeground(new Color(41, 128, 185));
        lblForgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        lblForgot.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                lblForgot.setForeground(new Color(21, 90, 150));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                lblForgot.setForeground(new Color(41, 128, 185));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (main != null) main.showPage("reset");
            }
        });

        forgotPanel.add(lblForgot);
        add(forgotPanel, gbc);

        // =============== ACTIONS ===================
        btnLogin.addActionListener(e -> doLogin());

        btnBack.addActionListener(e -> {
            clearFields();
            if (main != null) main.showPage("studentoptions");
        });

        txtPass.addActionListener(e -> doLogin());
    }

    private void doLogin() {
        String admissionNo = txtUser.getText().trim();
        String password = new String(txtPass.getPassword());

        if (admissionNo.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Admission No and Password.",
                    "Input required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "SELECT full_name FROM students WHERE admission_no = ? AND password = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, admissionNo);
            pst.setString(2, password);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    String fullName = rs.getString("full_name");
                    JOptionPane.showMessageDialog(this, "Login successful for " + fullName,
                            "Success", JOptionPane.INFORMATION_MESSAGE);

                    if (main != null) {
                        Dashboard dashboard = new Dashboard(main, admissionNo, fullName);
                        main.addPage("dashboard", dashboard);
                        main.showPage("dashboard");
                    }

                    clearFields();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid credentials.",
                            "Login failed", JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        txtUser.setText("");
        txtPass.setText("");
    }

    private void styleTextField(JTextField field) {
        field.setPreferredSize(new Dimension(220, 30));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(52, 152, 219), 2, true),
                        BorderFactory.createEmptyBorder(4, 6, 4, 6)
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                        BorderFactory.createEmptyBorder(4, 6, 4, 6)
                ));
            }
        });
    }

    private void styleButton(JButton b) {
        Color primary = new Color(52, 152, 219);
        Color primaryDark = new Color(41, 128, 185);
        b.setPreferredSize(new Dimension(120, 32));
        b.setBackground(primary);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addChangeListener(e -> {
            if (b.getModel().isRollover()) b.setBackground(primaryDark);
            else b.setBackground(primary);
        });
    }
}

