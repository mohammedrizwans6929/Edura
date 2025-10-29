import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class ProfilePage extends JPanel {
    private MainFrame main;
    private String admissionNo;

    // Labels for all profile data
    private JLabel lblAdmissionNo, lblRegNo, lblFullName, lblGender, lblDob, lblClassNo;
    private JLabel lblDept, lblSemester, lblBatch, lblPhone, lblEmail;

    public ProfilePage(MainFrame main, String admissionNo) {
        this.main = main;
        this.admissionNo = admissionNo;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        Color primary = new Color(52, 152, 219);
        Color primaryDark = new Color(41, 128, 185);

        // ===== TITLE =====
        JLabel lblTitle = new JLabel("My Profile", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(primary);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        add(lblTitle, BorderLayout.NORTH);

        // ===== SCROLLABLE PANEL =====
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Initialize labels
        lblAdmissionNo = new JLabel();
        lblRegNo = new JLabel();
        lblFullName = new JLabel();
        lblGender = new JLabel();
        lblDob = new JLabel();
        lblClassNo = new JLabel();
        lblDept = new JLabel();
        lblSemester = new JLabel();
        lblBatch = new JLabel();
        lblPhone = new JLabel();
        lblEmail = new JLabel();

        int y = 0;
        addRow(panel, gbc, y++, "Admission Number:", lblAdmissionNo);
        addRow(panel, gbc, y++, "University Reg No:", lblRegNo);
        addRow(panel, gbc, y++, "Full Name:", lblFullName);
        addRow(panel, gbc, y++, "Gender:", lblGender);
        addRow(panel, gbc, y++, "Date of Birth:", lblDob);
        addRow(panel, gbc, y++, "Class No:", lblClassNo);
        addRow(panel, gbc, y++, "Department:", lblDept);
        addRow(panel, gbc, y++, "Semester:", lblSemester);
        addRow(panel, gbc, y++, "Batch:", lblBatch);
        addRow(panel, gbc, y++, "Phone:", lblPhone);
        addRow(panel, gbc, y++, "Email:", lblEmail);

        // ===== BUTTONS =====
        JButton btnEdit = new JButton("Edit Profile");
        JButton btnBack = new JButton("Back to Dashboard");
        styleButton(btnEdit, primary, primaryDark);
        styleButton(btnBack, primary, primaryDark);

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(new Color(245, 247, 250));
        btnPanel.add(btnEdit);
        btnPanel.add(btnBack);
        add(btnPanel, BorderLayout.SOUTH);

        // ===== BUTTON ACTIONS =====
        btnEdit.addActionListener(e -> {
            EditProfilePage editPage = new EditProfilePage(main, admissionNo);
            main.addPage("editprofile", editPage);
            main.showPage("editprofile");
        });

        btnBack.addActionListener(e -> {
            Dashboard dashboard = new Dashboard(main, admissionNo, lblFullName.getText());
            main.addPage("dashboard", dashboard);
            main.showPage("dashboard");
        });

        // Load profile data
        loadStudentProfile();
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int y, String labelText, JLabel valueLabel) {
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.weightx = 0.3;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        valueLabel.setForeground(new Color(60, 60, 60));
        panel.add(valueLabel, gbc);
    }

    private void loadStudentProfile() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = """
                SELECT admission_no, reg_no, full_name, gender, dob, class_no,
                       dept, semester, batch, phone, email
                FROM students
                WHERE admission_no = ?
            """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, admissionNo);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                lblAdmissionNo.setText(rs.getString("admission_no"));
                lblRegNo.setText(rs.getString("reg_no"));
                lblFullName.setText(rs.getString("full_name"));
                lblGender.setText(rs.getString("gender"));
                lblDob.setText(rs.getString("dob"));
                lblClassNo.setText(rs.getString("class_no"));
                lblDept.setText(rs.getString("dept"));
                lblSemester.setText(rs.getString("semester"));
                lblBatch.setText(rs.getString("batch"));
                lblPhone.setText(rs.getString("phone"));
                lblEmail.setText(rs.getString("email"));
            } else {
                JOptionPane.showMessageDialog(this, "No profile found for this admission number!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading profile: " + e.getMessage());
        }
    }

    private void styleButton(JButton b, Color primary, Color primaryDark) {
        b.setBackground(primary);
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
                b.setBackground(primaryDark);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(primary);
            }
        });
    }
}

