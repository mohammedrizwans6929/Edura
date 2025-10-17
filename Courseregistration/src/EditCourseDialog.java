import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EditCourseDialog extends JDialog {
    private JTextField txtCourseId, txtCourseName;
    private JTextArea txtDescription;
    private JComboBox<String> cmbMode;
    private JSpinner dateSpinner, timeSpinner;
    private JTextField txtPoster;
    private JLabel lblPreview;

    private String courseId;
    private ManageCoursesPage parentPage;

    private Color primary = new Color(52, 152, 219);
    private Color primaryDark = new Color(41, 128, 185);

    public EditCourseDialog(JFrame parent, String courseId, ManageCoursesPage parentPage) {
        super(parent, "Edit Course", true);
        this.courseId = courseId;
        this.parentPage = parentPage;

        setSize(500, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        initUI();
        loadCourseDetails();
    }

    private void initUI() {
        // ===== Header =====
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(primary);
        header.setPreferredSize(new Dimension(500, 60));

        JLabel lblTitle = new JLabel("Edit Course");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        header.add(lblTitle, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // ===== Form Panel =====
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // ID (not editable)
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Course ID:"), gbc);
        gbc.gridx = 1;
        txtCourseId = new JTextField(20);
        txtCourseId.setEditable(false);
        formPanel.add(txtCourseId, gbc);

        // Name
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Course Name:"), gbc);
        gbc.gridx = 1;
        txtCourseName = new JTextField(20);
        formPanel.add(txtCourseName, gbc);

        // Description
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        txtDescription = new JTextArea(4, 20);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        formPanel.add(new JScrollPane(txtDescription), gbc);

        // Date
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Date:"), gbc);
        gbc.gridx = 1;
        dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        formPanel.add(dateSpinner, gbc);

        // Time
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Time:"), gbc);
        gbc.gridx = 1;
        timeSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
        timeSpinner.setEditor(timeEditor);
        formPanel.add(timeSpinner, gbc);

        // Mode
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Mode:"), gbc);
        gbc.gridx = 1;
        cmbMode = new JComboBox<>(new String[]{"Online", "Offline"});
        formPanel.add(cmbMode, gbc);

        // Poster
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Poster:"), gbc);
        gbc.gridx = 1;
        JPanel posterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        txtPoster = new JTextField(15);
        txtPoster.setEditable(false);
        JButton btnBrowse = new JButton("Browse");
        styleButton(btnBrowse, primary, primaryDark);
        lblPreview = new JLabel();
        lblPreview.setPreferredSize(new Dimension(100, 80));
        lblPreview.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        btnBrowse.addActionListener(e -> choosePoster());

        posterPanel.add(txtPoster);
        posterPanel.add(btnBrowse);
        posterPanel.add(lblPreview);
        formPanel.add(posterPanel, gbc);

        add(formPanel, BorderLayout.CENTER);

        // ===== Footer Buttons =====
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton btnSave = new JButton("Update");
        styleButton(btnSave, new Color(46, 204, 113), new Color(39, 174, 96));
        btnSave.addActionListener(e -> updateCourse());

        JButton btnCancel = new JButton("Cancel");
        styleButton(btnCancel, new Color(231, 76, 60), new Color(192, 57, 43));
        btnCancel.addActionListener(e -> dispose());

        footer.add(btnSave);
        footer.add(btnCancel);
        add(footer, BorderLayout.SOUTH);
    }

    private void loadCourseDetails() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement("SELECT * FROM courses WHERE course_id = ?")) {
            pst.setString(1, courseId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                txtCourseId.setText(rs.getString("course_id"));
                txtCourseName.setText(rs.getString("course_name"));
                txtDescription.setText(rs.getString("description"));
                dateSpinner.setValue(rs.getDate("course_date"));
                timeSpinner.setValue(rs.getTime("course_time"));
                cmbMode.setSelectedItem(rs.getString("mode"));

                String poster = rs.getString("poster");
                txtPoster.setText(poster);
                if (poster != null && !poster.equals("No file chosen")) {
                    File posterFile = new File("posters/" + poster);
                    if (posterFile.exists()) {
                        ImageIcon icon = new ImageIcon(posterFile.getAbsolutePath());
                        Image img = icon.getImage().getScaledInstance(100, 80, Image.SCALE_SMOOTH);
                        lblPreview.setIcon(new ImageIcon(img));
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading course: " + ex.getMessage());
        }
    }

    private void choosePoster() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose Poster");
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String fileName = selectedFile.getName();
            File dest = new File("posters/" + fileName);

            try {
                Files.copy(selectedFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                txtPoster.setText(fileName);

                ImageIcon icon = new ImageIcon(dest.getAbsolutePath());
                Image img = icon.getImage().getScaledInstance(100, 80, Image.SCALE_SMOOTH);
                lblPreview.setIcon(new ImageIcon(img));
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving image: " + ex.getMessage());
            }
        }
    }

    private void updateCourse() {
        String name = txtCourseName.getText().trim();
        String desc = txtDescription.getText().trim();
        Date date = (Date) dateSpinner.getValue();
        Date time = (Date) timeSpinner.getValue();
        String mode = (String) cmbMode.getSelectedItem();
        String poster = txtPoster.getText().isEmpty() ? "No file chosen" : txtPoster.getText();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Course name cannot be empty.");
            return;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(
                     "UPDATE courses SET course_name = ?, description = ?, course_date = ?, course_time = ?, mode = ?, poster = ? WHERE course_id = ?")) {

            pst.setString(1, name);
            pst.setString(2, desc);
            pst.setDate(3, new java.sql.Date(date.getTime()));
            pst.setTime(4, new java.sql.Time(time.getTime()));
            pst.setString(5, mode);
            pst.setString(6, poster);
            pst.setString(7, courseId);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Course updated successfully!");
            parentPage.refresh();
            dispose();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating course: " + ex.getMessage());
        }
    }

    private void styleButton(JButton b, Color bg, Color hover) {
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(hover); }
            public void mouseExited(MouseEvent e) { b.setBackground(bg); }
        });
    }
}
