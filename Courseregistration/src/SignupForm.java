import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.toedter.calendar.JDateChooser; 
import java.sql.SQLException; // Added for clarity in catch block

public class SignupForm extends JPanel {
    private JTextField txtAdmissionNo, txtRegNo, txtFullName, txtClassNo, txtPhone, txtEmail, txtBatch;
    private JComboBox<String> cmbGender, cmbDept, cmbSemester;
    private JTextField txtSecQ, txtSecA;
    private JPasswordField txtPass, txtConfirm;
    private JButton btnSignup, btnHome;
    private JDateChooser dobChooser;

    private MainFrame main; // Ensure MainFrame reference is stored

    public SignupForm(MainFrame main) {
        this.main = main; // Store the reference
        setLayout(new BorderLayout());
        Color bgColor = new Color(245, 247, 250);
        Color primary = new Color(52, 152, 219);
        Color primaryDark = new Color(41, 128, 185);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(bgColor);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel lblTitle = new JLabel("Create Profile");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(primary);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(lblTitle, gbc);

        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;

        // Left column
        int row = 1;
        panel.add(new JLabel("Admission Number:"), setGbc(gbc, 0, row));
        txtAdmissionNo = new JTextField(15);
        styleTextField(txtAdmissionNo);
        panel.add(txtAdmissionNo, setGbc(gbc, 1, row++));

        panel.add(new JLabel("University Reg No:"), setGbc(gbc, 0, row));
        txtRegNo = new JTextField(15);
        styleTextField(txtRegNo);
        panel.add(txtRegNo, setGbc(gbc, 1, row++));

        panel.add(new JLabel("Full Name:"), setGbc(gbc, 0, row));
        txtFullName = new JTextField(15);
        styleTextField(txtFullName);
        panel.add(txtFullName, setGbc(gbc, 1, row++));

        panel.add(new JLabel("Gender:"), setGbc(gbc, 0, row));
        cmbGender = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        cmbGender.setSelectedIndex(-1);
        styleCombo(cmbGender);
        panel.add(cmbGender, setGbc(gbc, 1, row++));

        panel.add(new JLabel("Date of Birth (dd/MM/yyyy):"), setGbc(gbc, 0, row));
        dobChooser = new JDateChooser();
        dobChooser.setDateFormatString("dd/MM/yyyy");
        dobChooser.setPreferredSize(new Dimension(160, 28));
        panel.add(dobChooser, setGbc(gbc, 1, row++));

        panel.add(new JLabel("Class No:"), setGbc(gbc, 0, row));
        txtClassNo = new JTextField(15);
        styleTextField(txtClassNo);
        panel.add(txtClassNo, setGbc(gbc, 1, row++));

        // Right column
        row = 1;
        panel.add(new JLabel("Department:"), setGbcRight(gbc, 2, row));
        cmbDept = new JComboBox<>(new String[]{
                "Computer Science Engineering",
                "Electronics and Communication Engineering",
                "Electrical and Electronics Engineering",
                "Mechanical Engineering"});
        cmbDept.setSelectedIndex(-1);
        styleCombo(cmbDept);
        panel.add(cmbDept, setGbcRight(gbc, 3, row++));

        panel.add(new JLabel("Semester:"), setGbcRight(gbc, 2, row));
        cmbSemester = new JComboBox<>(new String[]{"1", "2", "3", "4", "5", "6", "7", "8"});
        cmbSemester.setSelectedIndex(-1);
        styleCombo(cmbSemester);
        panel.add(cmbSemester, setGbcRight(gbc, 3, row++));

        panel.add(new JLabel("Batch (Optional):"), setGbcRight(gbc, 2, row));
        txtBatch = new JTextField(15);
        styleTextField(txtBatch);
        panel.add(txtBatch, setGbcRight(gbc, 3, row++));

        panel.add(new JLabel("Phone Number:"), setGbcRight(gbc, 2, row));
        txtPhone = new JTextField(15);
        styleTextField(txtPhone);
        panel.add(txtPhone, setGbcRight(gbc, 3, row++));

        panel.add(new JLabel("Email Address:"), setGbcRight(gbc, 2, row));
        txtEmail = new JTextField(15);
        styleTextField(txtEmail);
        panel.add(txtEmail, setGbcRight(gbc, 3, row++));

        panel.add(new JLabel("Password:"), setGbcRight(gbc, 2, row));
        txtPass = new JPasswordField(15);
        styleTextField(txtPass);
        panel.add(txtPass, setGbcRight(gbc, 3, row++));

        panel.add(new JLabel("Confirm Password:"), setGbcRight(gbc, 2, row));
        txtConfirm = new JPasswordField(15);
        styleTextField(txtConfirm);
        panel.add(txtConfirm, setGbcRight(gbc, 3, row++));

        // Security Q & A
        gbc.gridx = 0;
        gbc.gridy = row + 1;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(new JLabel("Security Question:"), gbc);
        gbc.gridy++;
        txtSecQ = new JTextField(20);
        styleTextField(txtSecQ);
        panel.add(txtSecQ, gbc);
        gbc.gridy++;
        panel.add(new JLabel("Answer:"), gbc);
        gbc.gridy++;
        txtSecA = new JTextField(20);
        styleTextField(txtSecA);
        panel.add(txtSecA, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setBackground(bgColor);
        btnSignup = new JButton("Sign Up");
        styleButton(btnSignup, primary, primaryDark);
        btnPanel.add(btnSignup);
        btnHome = new JButton("Back to Options"); // Changed text for clarity
        styleButton(btnHome, primary, primaryDark);
        btnPanel.add(btnHome);
        gbc.gridy++;
        panel.add(btnPanel, gbc);

        add(new JScrollPane(panel), BorderLayout.CENTER);

        // Actions
        btnHome.addActionListener(e -> {
            clearFields(); // Clear fields before navigating away
            main.showPage("studentoptions"); // Navigate to student options
        });
        btnSignup.addActionListener(e -> saveToDatabase(main));
    }

    /**
     * Clears all input fields in the form.
     */
    public void clearFields() {
        txtAdmissionNo.setText("");
        txtRegNo.setText("");
        txtFullName.setText("");
        txtClassNo.setText("");
        txtPhone.setText("");
        txtEmail.setText("");
        txtBatch.setText("");
        txtSecQ.setText("");
        txtSecA.setText("");
        txtPass.setText("");
        txtConfirm.setText("");
        
        dobChooser.setDate(null);
        
        // Reset ComboBoxes
        cmbGender.setSelectedIndex(-1);
        cmbDept.setSelectedIndex(-1);
        cmbSemester.setSelectedIndex(-1);
        
        // Scroll to top
        JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, this);
        if (scrollPane != null) {
            SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(0));
        }
    }

    private void saveToDatabase(MainFrame main) {
        String admissionNo = txtAdmissionNo.getText().trim();
        String regNo = txtRegNo.getText().trim();
        String fullName = txtFullName.getText().trim();
        String gender = (String) cmbGender.getSelectedItem();
        Date selectedDate = dobChooser.getDate();
        String classNo = txtClassNo.getText().trim();
        String dept = (String) cmbDept.getSelectedItem();
        String semester = (String) cmbSemester.getSelectedItem();
        String batch = txtBatch.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String pass = new String(txtPass.getPassword());
        String confirm = new String(txtConfirm.getPassword());
        String secQ = txtSecQ.getText().trim();
        String secA = txtSecA.getText().trim();

        // Validation (unchanged)
        if (admissionNo.isEmpty() || regNo.isEmpty() || fullName.isEmpty() || gender == null || selectedDate == null
                || classNo.isEmpty() || dept == null || semester == null || phone.isEmpty() || email.isEmpty()
                || pass.isEmpty() || confirm.isEmpty() || secQ.isEmpty() || secA.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields except Batch are mandatory!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!phone.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(this, "Phone number must be 10 digits!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.com$")) {
            JOptionPane.showMessageDialog(this, "Email must contain @ and end with .com!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!classNo.matches("\\d{1,2}")) {
            JOptionPane.showMessageDialog(this, "Class number must be 1–2 digits!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!pass.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dobForDb = dbFormat.format(selectedDate);

        // Database insertion
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO students (admission_no, reg_no, full_name, gender, dob, class_no, dept, semester, batch, phone, email, password, security_question, security_answer) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, admissionNo);
            pst.setString(2, regNo);
            pst.setString(3, fullName);
            pst.setString(4, gender);
            pst.setString(5, dobForDb);
            pst.setString(6, classNo);
            pst.setString(7, dept);
            pst.setString(8, semester);
            pst.setString(9, batch);
            pst.setString(10, phone);
            pst.setString(11, email);
            pst.setString(12, pass);
            pst.setString(13, secQ);
            pst.setString(14, secA);
            pst.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Profile created successfully! You can now log in.");
            clearFields(); // Clear fields after successful submission
            main.showPage("login");
        } catch (SQLException ex) {
            ex.printStackTrace();
            // Check for specific error, like duplicate key violation (Admission No)
            if (ex.getSQLState().startsWith("23")) { 
                 JOptionPane.showMessageDialog(this, "Creation failed: Admission Number or Registration Number already exists.", "Database Error", JOptionPane.ERROR_MESSAGE);
            } else {
                 JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Utility methods (unchanged)
    private GridBagConstraints setGbc(GridBagConstraints gbc, int x, int y) {
        GridBagConstraints g = (GridBagConstraints) gbc.clone();
        g.gridx = x;
        g.gridy = y;
        g.weightx = 0.5;
        return g;
    }

    private GridBagConstraints setGbcRight(GridBagConstraints gbc, int x, int y) {
        GridBagConstraints g = (GridBagConstraints) gbc.clone();
        g.gridx = x;
        g.gridy = y;
        g.weightx = 0.5;
        if (x == 2)
            g.insets = new Insets(6, 40, 6, 6);
        else
            g.insets = new Insets(6, 6, 6, 6);
        return g;
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setPreferredSize(new Dimension(160, 28));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
    }

    private void styleCombo(JComboBox<String> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setBackground(Color.WHITE);
        combo.setPreferredSize(new Dimension(160, 28));
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