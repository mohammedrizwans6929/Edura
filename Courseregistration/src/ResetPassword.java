import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ResetPassword extends JPanel {
    private JTextField txtAdmissionNo, txtAnswer;
    private JPasswordField txtNewPassword, txtConfirmPassword;
    private JLabel lblSecurityQuestion;
    private JButton btnFetch, btnReset, btnBack;
    private MainFrame main;

    public ResetPassword(MainFrame main) {
        this.main = main;

        Color bgColor = new Color(245, 247, 250);
        Color primary = new Color(52, 152, 219);
        Color primaryDark = new Color(41, 128, 185);

        setLayout(new BorderLayout());
        setBackground(bgColor);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(bgColor);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

       
        JLabel lblTitle = new JLabel("Reset Password");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(primary);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(lblTitle, gbc);

        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        int row = 1;

        
        panel.add(new JLabel("Admission No:"), setGbc(gbc, 0, row));
        txtAdmissionNo = new JTextField(15);
        styleTextField(txtAdmissionNo);
        panel.add(txtAdmissionNo, setGbc(gbc, 1, row));
        btnFetch = new JButton("Fetch");
        styleButton(btnFetch, primary, primaryDark);
        panel.add(btnFetch, setGbc(gbc, 2, row++));

       
        panel.add(new JLabel("Security Question:"), setGbc(gbc, 0, row));
        lblSecurityQuestion = new JLabel(" ");
        lblSecurityQuestion.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSecurityQuestion.setForeground(primaryDark);
        panel.add(lblSecurityQuestion, setGbc(gbc, 1, row++, 2));

      
        panel.add(new JLabel("Answer:"), setGbc(gbc, 0, row));
        txtAnswer = new JTextField(15);
        styleTextField(txtAnswer);
        panel.add(txtAnswer, setGbc(gbc, 1, row++, 2));

       
        panel.add(new JLabel("New Password:"), setGbc(gbc, 0, row));
        txtNewPassword = new JPasswordField(15);
        styleTextField(txtNewPassword);
        panel.add(txtNewPassword, setGbc(gbc, 1, row++, 2));

       
        panel.add(new JLabel("Confirm Password:"), setGbc(gbc, 0, row));
        txtConfirmPassword = new JPasswordField(15);
        styleTextField(txtConfirmPassword);
        panel.add(txtConfirmPassword, setGbc(gbc, 1, row++, 2));

      
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setBackground(bgColor);
        btnReset = new JButton("Reset Password");
        styleButton(btnReset, primary, primaryDark);
        btnPanel.add(btnReset);
        btnBack = new JButton("Back to Login");
        styleButton(btnBack, primary, primaryDark);
        btnPanel.add(btnBack);
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(btnPanel, gbc);

        add(panel, BorderLayout.CENTER);

       
        btnFetch.addActionListener(e -> fetchSecurityQuestion());
        btnReset.addActionListener(e -> resetPassword());
        btnBack.addActionListener(e -> {
            clearFields();
            main.showPage("login");
        });
    }

    private void fetchSecurityQuestion() {
        String admissionNo = txtAdmissionNo.getText().trim();
        if (admissionNo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter Admission Number first!");
            return;
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(
                     "SELECT security_question FROM students WHERE admission_no=?")) {
            pst.setString(1, admissionNo);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                lblSecurityQuestion.setText(rs.getString("security_question"));
            } else {
                JOptionPane.showMessageDialog(this, "Admission number not found!");
                lblSecurityQuestion.setText(" ");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }
    }

    private void resetPassword() {
        String admissionNo = txtAdmissionNo.getText().trim();
        String answer = txtAnswer.getText().trim();
        String newPass = new String(txtNewPassword.getPassword());
        String confirmPass = new String(txtConfirmPassword.getPassword());

        if (admissionNo.isEmpty() || answer.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required!");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match!");
            return;
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(
                     "SELECT security_answer, password FROM students WHERE admission_no=?")) {
            pst.setString(1, admissionNo);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String correctAnswer = rs.getString("security_answer");
                String currentPassword = rs.getString("password");

               
                if (answer.equals(correctAnswer)) {
                    if (newPass.equals(currentPassword)) {
                        JOptionPane.showMessageDialog(this, "Password must be new!");
                        return;
                    }

                    PreparedStatement update = con.prepareStatement(
                            "UPDATE students SET password=? WHERE admission_no=?");
                    update.setString(1, newPass);
                    update.setString(2, admissionNo);
                    update.executeUpdate();

                    JOptionPane.showMessageDialog(this, "Password reset successful!");
                    clearFields();
                    main.showPage("login");
                } else {
                    JOptionPane.showMessageDialog(this, "Incorrect answer! (case-sensitive)");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Admission number not found!");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }
    }

    private void clearFields() {
        txtAdmissionNo.setText("");
        txtAnswer.setText("");
        txtNewPassword.setText("");
        txtConfirmPassword.setText("");
        lblSecurityQuestion.setText(" ");
    }

    private GridBagConstraints setGbc(GridBagConstraints gbc, int x, int y) {
        return setGbc(gbc, x, y, 1);
    }

    private GridBagConstraints setGbc(GridBagConstraints gbc, int x, int y, int width) {
        GridBagConstraints g = (GridBagConstraints) gbc.clone();
        g.gridx = x;
        g.gridy = y;
        g.weightx = 0.5;
        g.gridwidth = width;
        return g;
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setPreferredSize(new Dimension(200, 28));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));
    }

    private void styleButton(JButton button, Color primary, Color primaryDark) {
        button.setBackground(primary);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createEmptyBorder(6, 15, 6, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(primaryDark);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(primary);
            }
        });
    }
}

