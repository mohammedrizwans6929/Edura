import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddCoursePage extends JPanel {
    private MainFrame main;
    private JTextField txtCourseId, txtCourseName;
    private JTextArea txtDescription;
    private JComboBox<String> cmbMode;
    private JSpinner dateSpinner, timeSpinner;
    private JTextField txtPoster;
    private JLabel lblPreview;

    private Color primary = new Color(52, 152, 219);
    private Color primaryDark = new Color(41, 128, 185);

    public AddCoursePage(MainFrame main) {
        this.main = main;
        setLayout(new BorderLayout());
        initUI();
    }

    private void initUI() {
        // ===== Header =====
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(primary);
        header.setPreferredSize(new Dimension(800, 60));

        JLabel lblTitle = new JLabel("Add New Course");
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
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Course ID
        formPanel.add(new JLabel("Course ID:"), gbc);
        gbc.gridx = 1;
        txtCourseId = new JTextField(20);
        formPanel.add(txtCourseId, gbc);

        // Course Name
        gbc.gridy++;
        gbc.gridx = 0;
        formPanel.add(new JLabel("Course Name:"), gbc);
        gbc.gridx = 1;
        txtCourseName = new JTextField(20);
        formPanel.add(txtCourseName, gbc);

        // Description
        gbc.gridy++;
        gbc.gridx = 0;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        txtDescription = new JTextArea(4, 20);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        formPanel.add(new JScrollPane(txtDescription), gbc);

        // Date chooser
        gbc.gridy++;
        gbc.gridx = 0;
        formPanel.add(new JLabel("Course Date:"), gbc);
        gbc.gridx = 1;
        dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        formPanel.add(dateSpinner, gbc);

        // Time chooser
        gbc.gridy++;
        gbc.gridx = 0;
        formPanel.add(new JLabel("Course Time:"), gbc);
        gbc.gridx = 1;
        timeSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
        timeSpinner.setEditor(timeEditor);
        formPanel.add(timeSpinner, gbc);

        // Mode
        gbc.gridy++;
        gbc.gridx = 0;
        formPanel.add(new JLabel("Mode:"), gbc);
        gbc.gridx = 1;
        cmbMode = new JComboBox<>(new String[]{"Online", "Offline"});
        formPanel.add(cmbMode, gbc);

        // Poster
        gbc.gridy++;
        gbc.gridx = 0;
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
        JButton btnSave = new JButton("Save Course");
        styleButton(btnSave, new Color(46, 204, 113), new Color(39, 174, 96));
        btnSave.addActionListener(e -> saveCourse());

        JButton btnBack = new JButton("Back");
        styleButton(btnBack, primary, primaryDark);
        btnBack.addActionListener(e -> main.showPage("admin"));

        footer.add(btnSave);
        footer.add(btnBack);
        add(footer, BorderLayout.SOUTH);
    }

    private void choosePoster() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose Course Poster");
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

    private void saveCourse() {
        String courseId = txtCourseId.getText().trim();
        String courseName = txtCourseName.getText().trim();
        String description = txtDescription.getText().trim();
        Date date = (Date) dateSpinner.getValue();
        Date time = (Date) timeSpinner.getValue();
        String mode = (String) cmbMode.getSelectedItem();
        String poster = txtPoster.getText().isEmpty() ? "No file chosen" : txtPoster.getText();

        if (courseId.isEmpty() || courseName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Course ID and Name are required.");
            return;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(
                     "INSERT INTO courses (course_id, course_name, description, course_date, course_time, mode, poster) VALUES (?, ?, ?, ?, ?, ?, ?)")) {

            pst.setString(1, courseId);
            pst.setString(2, courseName);
            pst.setString(3, description);
            pst.setDate(4, new java.sql.Date(date.getTime()));
            pst.setTime(5, new java.sql.Time(time.getTime()));
            pst.setString(6, mode);
            pst.setString(7, poster);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Course added successfully!");
            clearFields();

            // Refresh Available Courses Page if open
            AvailableCoursesPage acp = (AvailableCoursesPage) main.getPage("availablecourses");
            if (acp != null) {
                acp.refreshCourses();
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error adding course: " + ex.getMessage());
        }
    }

    private void clearFields() {
        txtCourseId.setText("");
        txtCourseName.setText("");
        txtDescription.setText("");
        txtPoster.setText("");
        lblPreview.setIcon(null);
        dateSpinner.setValue(new Date());
        timeSpinner.setValue(new Date());
        cmbMode.setSelectedIndex(0);
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
