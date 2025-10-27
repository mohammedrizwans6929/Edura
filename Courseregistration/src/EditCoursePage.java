import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;

public class EditCoursePage extends JPanel {
    private MainFrame main;
    private String courseId;
    private ManageCoursesPage parentPage; // 🔴 ADDED: Reference to the parent page

    private JTextField txtCourseId, txtCourseName, txtDate, txtTime, txtMode, txtCoordinator1, txtCoordinator2;
    private JTextArea txtDescription;
    private JLabel lblPosterPreview;
    private JButton btnChooseImage, btnSave, btnBack;
    private File selectedImageFile;

    /**
     * 🔴 UPDATED CONSTRUCTOR: Added parentPage argument as required by MainFrame.
     */
    public EditCoursePage(MainFrame main, String courseId, ManageCoursesPage parentPage) {
        this.main = main;
        this.courseId = courseId;
        this.parentPage = parentPage; // 🔴 INITIALIZED: Store the parent reference

        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        JLabel lblTitle = new JLabel("Edit Course: " + courseId, SwingConstants.CENTER); // Include ID in title
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(52, 152, 219));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        add(lblTitle, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(245, 247, 250));

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);
        Dimension fieldSize = new Dimension(300, 32);
        Color primary = new Color(52, 152, 219);
        Color hover = new Color(41, 128, 185);

        int y = 0;

        // --- Course ID ---
        gbc.gridx = 0; gbc.gridy = y;
        formPanel.add(new JLabel("Course ID:"), gbc);
        txtCourseId = new JTextField();
        styleField(txtCourseId, fieldFont, fieldSize);
        txtCourseId.setEditable(false);
        gbc.gridx = 1;
        formPanel.add(txtCourseId, gbc);
        y++;

        // --- Course Name ---
        gbc.gridx = 0; gbc.gridy = y;
        formPanel.add(new JLabel("Course Name:"), gbc);
        txtCourseName = new JTextField();
        styleField(txtCourseName, fieldFont, fieldSize);
        gbc.gridx = 1;
        formPanel.add(txtCourseName, gbc);
        y++;

        // --- Description ---
        gbc.gridx = 0; gbc.gridy = y;
        formPanel.add(new JLabel("Description:"), gbc);
        txtDescription = new JTextArea(4, 20);
        txtDescription.setFont(fieldFont);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        gbc.gridx = 1;
        formPanel.add(new JScrollPane(txtDescription), gbc);
        y++;

        // --- Date ---
        gbc.gridx = 0; gbc.gridy = y;
        formPanel.add(new JLabel("Date (YYYY-MM-DD):"), gbc); // Added format hint
        txtDate = new JTextField();
        styleField(txtDate, fieldFont, fieldSize);
        gbc.gridx = 1;
        formPanel.add(txtDate, gbc);
        y++;

        // --- Time ---
        gbc.gridx = 0; gbc.gridy = y;
        formPanel.add(new JLabel("Time (HH:MM:SS):"), gbc); // Added format hint
        txtTime = new JTextField();
        styleField(txtTime, fieldFont, fieldSize);
        gbc.gridx = 1;
        formPanel.add(txtTime, gbc);
        y++;

        // --- Mode ---
        gbc.gridx = 0; gbc.gridy = y;
        formPanel.add(new JLabel("Mode:"), gbc);
        txtMode = new JTextField(); // Consider using a JComboBox if modes are fixed (e.g., Online/Physical)
        styleField(txtMode, fieldFont, fieldSize);
        gbc.gridx = 1;
        formPanel.add(txtMode, gbc);
        y++;

        // --- Coordinators ---
        gbc.gridx = 0; gbc.gridy = y;
        formPanel.add(new JLabel("Coordinator 1:"), gbc);
        txtCoordinator1 = new JTextField();
        styleField(txtCoordinator1, fieldFont, fieldSize);
        gbc.gridx = 1;
        formPanel.add(txtCoordinator1, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y;
        formPanel.add(new JLabel("Coordinator 2:"), gbc);
        txtCoordinator2 = new JTextField();
        styleField(txtCoordinator2, fieldFont, fieldSize);
        gbc.gridx = 1;
        formPanel.add(txtCoordinator2, gbc);
        y++;

        // --- Poster ---
        gbc.gridx = 0; gbc.gridy = y;
        formPanel.add(new JLabel("Course Poster:"), gbc);
        JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        imagePanel.setBackground(new Color(245, 247, 250));
        lblPosterPreview = new JLabel("No Image");
        lblPosterPreview.setPreferredSize(new Dimension(200, 130));
        lblPosterPreview.setHorizontalAlignment(SwingConstants.CENTER);
        lblPosterPreview.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        btnChooseImage = createButton("Choose Image", primary, hover);
        imagePanel.add(lblPosterPreview);
        imagePanel.add(btnChooseImage);
        gbc.gridx = 1;
        formPanel.add(imagePanel, gbc);
        y++;

        // --- Buttons ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        buttonPanel.setBackground(new Color(245, 247, 250));

        btnSave = createButton("Save Changes", primary, hover);
        btnBack = createButton("Back", new Color(108, 122, 137), new Color(86, 96, 107)); // Changed color for back
        btnBack.setPreferredSize(new Dimension(100, 32));

        buttonPanel.add(btnSave);
        buttonPanel.add(btnBack);

        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(buttonPanel, gbc);

        // Events
        btnChooseImage.addActionListener(e -> chooseImage());
        // 🔴 FIX: Back button should navigate back to the main management page
        btnBack.addActionListener(e -> main.showPage("managecourses"));
        btnSave.addActionListener(e -> saveChanges());

        loadCourseDetails();
    }

    private void styleField(JTextField field, Font font, Dimension size) {
        field.setFont(font);
        field.setPreferredSize(size);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
    }

    private JButton createButton(String text, Color primary, Color hover) {
        JButton b = new JButton(text);
        b.setBackground(primary);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { b.setBackground(hover); }
            @Override
            public void mouseExited(MouseEvent e) { b.setBackground(primary); }
        });
        return b;
    }

    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Images", "jpg", "png", "jpeg"));
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedImageFile = chooser.getSelectedFile();
            ImageIcon icon = new ImageIcon(new ImageIcon(selectedImageFile.getAbsolutePath())
                    .getImage().getScaledInstance(200, 130, Image.SCALE_SMOOTH));
            lblPosterPreview.setText("");
            lblPosterPreview.setIcon(icon);
        }
    }

    private void loadCourseDetails() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM courses WHERE course_id=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, courseId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                txtCourseId.setText(rs.getString("course_id"));
                txtCourseName.setText(rs.getString("course_name"));
                txtDescription.setText(rs.getString("description"));
                // NOTE: Using rs.getString directly for date/time assumes the database 
                // stores them in the exact format required by the JTextFields.
                txtDate.setText(rs.getString("course_date")); 
                txtTime.setText(rs.getString("course_time"));
                txtMode.setText(rs.getString("mode"));
                txtCoordinator1.setText(rs.getString("coordinator1"));
                txtCoordinator2.setText(rs.getString("coordinator2"));

                // Assuming the database column is named 'poster' and stores BLOB
                byte[] imageBytes = rs.getBytes("poster");
                if (imageBytes != null) {
                    ImageIcon icon = new ImageIcon(new ImageIcon(imageBytes)
                            .getImage().getScaledInstance(200, 130, Image.SCALE_SMOOTH));
                    lblPosterPreview.setIcon(icon);
                    lblPosterPreview.setText("");
                    // Keep selectedImageFile null unless a new image is chosen
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading course: " + e.getMessage());
        }
    }

    private void saveChanges() {
        if (txtCourseName.getText().trim().isEmpty() || txtDate.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Course Name and Date are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            
            // Build the SQL query dynamically based on whether a new image was selected
            String sql;
            if (selectedImageFile != null) {
                sql = "UPDATE courses SET course_name=?, description=?, course_date=?, course_time=?, mode=?, coordinator1=?, coordinator2=?, poster=? WHERE course_id=?";
            } else {
                sql = "UPDATE courses SET course_name=?, description=?, course_date=?, course_time=?, mode=?, coordinator1=?, coordinator2=? WHERE course_id=?";
            }
            
            PreparedStatement pst = conn.prepareStatement(sql);
            int paramIndex = 1;
            
            pst.setString(paramIndex++, txtCourseName.getText().trim());
            pst.setString(paramIndex++, txtDescription.getText().trim());
            pst.setString(paramIndex++, txtDate.getText().trim());
            pst.setString(paramIndex++, txtTime.getText().trim());
            pst.setString(paramIndex++, txtMode.getText().trim());
            pst.setString(paramIndex++, txtCoordinator1.getText().trim());
            pst.setString(paramIndex++, txtCoordinator2.getText().trim());

            if (selectedImageFile != null) {
                FileInputStream fis = new FileInputStream(selectedImageFile);
                pst.setBinaryStream(paramIndex++, fis, (int) selectedImageFile.length());
            }

            pst.setString(paramIndex++, courseId);

            int updated = pst.executeUpdate();
            if (updated > 0) {
                JOptionPane.showMessageDialog(this, "Course updated successfully! Please refresh list.");
                
                // 🔴 FIX: Refresh the parent page list
                if (parentPage != null) {
                    parentPage.refresh();
                }
                
                // Navigate back to the Manage Courses page
                main.showPage("managecourses");
            } else {
                JOptionPane.showMessageDialog(this, "No changes were made!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving changes: " + e.getMessage());
        }
    }
}
