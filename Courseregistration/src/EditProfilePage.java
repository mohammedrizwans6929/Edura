import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import com.toedter.calendar.JDateChooser; 
import java.text.SimpleDateFormat;
import java.util.Date; // Required java.util.Date

public class EditProfilePage extends JPanel {
    private MainFrame main;
    private String admissionNo;

    private JTextField txtRegNo, txtFullName, txtClassNo, txtBatch, txtPhone, txtEmail;
    private JComboBox<String> cmbGender, cmbDept, cmbSemester;
    private JDateChooser dateChooser;

    public EditProfilePage(MainFrame main, String admissionNo) {
        this.main = main;
        this.admissionNo = admissionNo;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        Color primary = new Color(52, 152, 219);
        Color primaryDark = new Color(41, 128, 185);
        Color success = new Color(46, 204, 113); // Green for Save
        Color error = new Color(231, 76, 60);    // Red for Cancel

        JLabel lblTitle = new JLabel("Edit Profile", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(primary);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        add(lblTitle, BorderLayout.NORTH);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ===== Fields =====
        txtRegNo = new JTextField();
        txtFullName = new JTextField();
        cmbGender = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        dateChooser = new JDateChooser(); 
        dateChooser.setDateFormatString("yyyy-MM-dd");
        txtClassNo = new JTextField();
        cmbDept = new JComboBox<>(new String[]{
            "Computer Science Engineering",
            "Information Technology",
            "Electronics and Communication",
            "Mechanical Engineering",
            "Electrical Engineering",
            "Civil Engineering",
            "Other"
        });
        cmbSemester = new JComboBox<>(new String[]{
            "1", "2", "3", "4", "5", "6", "7", "8"
        });
        txtBatch = new JTextField();
        txtPhone = new JTextField();
        txtEmail = new JTextField();

        int y = 0;
        addRow(panel, gbc, y++, "University Reg No:", txtRegNo);
        addRow(panel, gbc, y++, "Full Name:", txtFullName);
        addRow(panel, gbc, y++, "Gender:", cmbGender);
        addRow(panel, gbc, y++, "Date of Birth:", dateChooser);
        addRow(panel, gbc, y++, "Class No:", txtClassNo);
        addRow(panel, gbc, y++, "Department:", cmbDept);
        addRow(panel, gbc, y++, "Semester:", cmbSemester);
        addRow(panel, gbc, y++, "Batch:", txtBatch);
        addRow(panel, gbc, y++, "Phone:", txtPhone);
        addRow(panel, gbc, y++, "Email:", txtEmail);

        // ===== Buttons =====
        JButton btnSave = new JButton("Save Changes");
        JButton btnCancel = new JButton("Cancel");
        
        // 🔴 FIX: Set distinct colors
        styleButton(btnSave, success, success.darker());
        styleButton(btnCancel, error, error.darker());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10)); // Added flow layout for spacing
        btnPanel.setBackground(new Color(245, 247, 250));
        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);
        add(btnPanel, BorderLayout.SOUTH);

        btnSave.addActionListener(e -> saveChanges());
        
        // 🔴 FIX: Cancel button logic
        btnCancel.addActionListener(e -> {
            // Re-initialize and show the profile page to ensure it's fresh if needed, 
            // or simply rely on the main app's navigation if 'profile' is static.
            main.showProfilePage(admissionNo); // Use showProfilePage to ensure proper initialization
        });

        loadProfileData();
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int y, String labelText, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.weightx = 0.3;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.gridy = y;
        gbc.weightx = 0.7;
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(field, gbc);
    }

    private void loadProfileData() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM students WHERE admission_no = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, admissionNo);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                txtRegNo.setText(rs.getString("reg_no"));
                txtFullName.setText(rs.getString("full_name"));
                
                // Set ComboBox selection, handle potential null/mismatch
                String gender = rs.getString("gender");
                if (gender != null) cmbGender.setSelectedItem(gender);

                // ✅ Load Date for JDateChooser
                Date dob = rs.getDate("dob");
                if (dob != null) {
                    dateChooser.setDate(dob);
                }

                txtClassNo.setText(rs.getString("class_no"));
                
                // Set ComboBox selection, handle potential null/mismatch
                String dept = rs.getString("dept");
                if (dept != null) cmbDept.setSelectedItem(dept);
                
                String semester = rs.getString("semester");
                if (semester != null) cmbSemester.setSelectedItem(semester);
                
                txtBatch.setText(rs.getString("batch"));
                txtPhone.setText(rs.getString("phone"));
                txtEmail.setText(rs.getString("email"));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading profile: " + e.getMessage());
        }
    }

    private void saveChanges() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = """
                UPDATE students
                SET reg_no=?, full_name=?, gender=?, dob=?, class_no=?, dept=?, semester=?, batch=?, phone=?, email=?
                WHERE admission_no=?
            """;
            PreparedStatement stmt = conn.prepareStatement(sql);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            // Check if dateChooser has a date selected
            String dob = (dateChooser.getDate() != null) ? sdf.format(dateChooser.getDate()) : null;

            stmt.setString(1, txtRegNo.getText());
            stmt.setString(2, txtFullName.getText());
            stmt.setString(3, cmbGender.getSelectedItem().toString());
            
            // Handle DOB being null (if allowed by DB schema)
            if (dob != null) {
                stmt.setString(4, dob);
            } else {
                stmt.setNull(4, Types.DATE);
            }
            
            stmt.setString(5, txtClassNo.getText());
            stmt.setString(6, cmbDept.getSelectedItem().toString());
            stmt.setString(7, cmbSemester.getSelectedItem().toString());
            stmt.setString(8, txtBatch.getText());
            stmt.setString(9, txtPhone.getText());
            stmt.setString(10, txtEmail.getText());
            stmt.setString(11, admissionNo);
            
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Profile updated successfully!");

            // 🔴 FIX: Call showProfilePage to ensure the ProfilePage is refreshed and displayed
            main.showProfilePage(admissionNo);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating profile: " + e.getMessage());
        }
    }

    private void styleButton(JButton b, Color bg, Color hover) {
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setPreferredSize(new Dimension(180, 38));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(hover);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(bg);
            }
        });
    }

}
