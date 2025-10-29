import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class AdminLogin extends JPanel {
    private JTextField txtUser;
    private JPasswordField txtPass;
    private JButton btnLogin;  
    private MainFrame main;

    public AdminLogin(MainFrame main) {
        this.main = main;
        setLayout(new GridBagLayout());
        setBackground(new Color(245, 247, 250));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

     
        JLabel lblTitle = new JLabel("Admin Login", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(52, 152, 219));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 6, 20, 6);
        add(lblTitle, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(6, 6, 6, 6);

        
        gbc.gridy = 1; gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Username:"), gbc);
        txtUser = new JTextField(15);
        styleField(txtUser);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(txtUser, gbc);

      
        gbc.gridy = 2; gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Password:"), gbc);
        txtPass = new JPasswordField(15);
        styleField(txtPass);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(txtPass, gbc);

  
        btnLogin = new JButton("Login");
        styleButton(btnLogin);
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(15, 6, 6, 6);
        add(btnLogin, gbc);

       
        JButton btnBack = new JButton("Back to Home");
        styleButton(btnBack);
        gbc.gridy = 4;
        gbc.insets = new Insets(6, 6, 20, 6);
        add(btnBack, gbc);

       
        btnLogin.addActionListener(e -> doLogin());
        btnBack.addActionListener(e -> {
            clearFields();
            main.showPage("welcome");
        });

       
        SwingUtilities.invokeLater(() -> {
            JRootPane rootPane = SwingUtilities.getRootPane(this);
            if (rootPane != null) {
                rootPane.setDefaultButton(btnLogin);
            }
        });
    }

    private void doLogin() {
        String username = txtUser.getText().trim();
        String password = new String(txtPass.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter both username and password.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM admins WHERE username=? AND password=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                clearFields();
                AdminPage adminPage = new AdminPage(main);
                main.addPage("adminpage", adminPage);
                main.showPage("adminpage");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid admin credentials!", "Error", JOptionPane.ERROR_MESSAGE);
                clearFields();
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
            clearFields();
        }
    }

    private void clearFields() {
        txtUser.setText("");
        txtPass.setText("");
    }

    private void styleField(JTextField field) {
        field.setPreferredSize(new Dimension(200, 30));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200,200,200), 1, true),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(52, 152, 219), 2, true),
                        BorderFactory.createEmptyBorder(4, 7, 4, 7)
                ));
            }
            @Override public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(200,200,200), 1, true),
                        BorderFactory.createEmptyBorder(5, 8, 5, 8)
                ));
            }
        });
    }

    private void styleButton(JButton b) {
        Color primary = new Color(52, 152, 219);
        Color hover = new Color(41, 128, 185);
        b.setPreferredSize(new Dimension(200, 35));
        b.setBackground(primary);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addChangeListener(e -> {
            if (b.getModel().isRollover()) b.setBackground(hover);
            else b.setBackground(primary);
        });
    }
}


