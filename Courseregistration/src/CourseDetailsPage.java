import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.io.File; // Needed for checking poster file existence

public class CourseDetailsPage extends JPanel {
    private MainFrame main;
    private String courseId;
    private String studentAdmissionNo;
    private JButton btnRegister;
    // Define the Green color used for 'Already Registered'
    private final Color SUCCESS_GREEN = new Color(46, 204, 113);
    private final Color PRIMARY_BLUE = new Color(52, 152, 219);

    public CourseDetailsPage(MainFrame main, String courseId, String studentAdmissionNo) {
        this.main = main;
        this.courseId = courseId;
        this.studentAdmissionNo = studentAdmissionNo;
        setLayout(new BorderLayout());
        initUI();
    }

    private void initUI() {
        // Use defined color constant
        Color primary = PRIMARY_BLUE;

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(primary);
        header.setPreferredSize(new Dimension(800, 60));

        JLabel lblTitle = new JLabel("Course Details");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        header.add(lblTitle, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Content panel - will be placed inside JScrollPane
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        content.setBackground(new Color(245, 247, 250)); // Light background for contrast

        // Poster Label setup
        JLabel lblPoster = new JLabel();
        lblPoster.setHorizontalAlignment(SwingConstants.CENTER);
        lblPoster.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblPoster.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JLabel lblName = new JLabel();
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 24)); // Larger font for name
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblDate = new JLabel();
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblDate.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTime = new JLabel();
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblTime.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblMode = new JLabel();
        lblMode.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblMode.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Description Area
        JTextArea txtDescription = new JTextArea(10, 40); // Initial size hint for description
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setEditable(false);
        txtDescription.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtDescription.setBorder(BorderFactory.createTitledBorder("Description"));

        // --- Add components to content panel ---
        content.add(lblPoster);
        content.add(Box.createVerticalStrut(20));
        content.add(lblName);
        content.add(Box.createVerticalStrut(10));
        content.add(lblDate);
        content.add(lblTime);
        content.add(lblMode);
        content.add(Box.createVerticalStrut(20));
        content.add(new JScrollPane(txtDescription));

        // Make the page scrollable
        JScrollPane mainScrollPane = new JScrollPane(content);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainScrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(mainScrollPane, BorderLayout.CENTER);

        // Footer with Back and Register
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        footer.setBackground(Color.WHITE);

        JButton btnBack = new JButton("Back");
        styleButton(btnBack, new Color(231, 76, 60), new Color(192, 57, 43));
        // Use main.showPage("availablecourses") to show the main page
        btnBack.addActionListener(e -> main.showPage("availablecourses"));

        btnRegister = new JButton("Register");
        styleButton(btnRegister, primary, primary.darker());
        btnRegister.addActionListener(e -> registerForCourse());

        footer.add(btnBack);
        footer.add(btnRegister);

        add(footer, BorderLayout.SOUTH);

        // Load data on page initialization
        loadCourseDetails(lblPoster, lblName, lblDate, lblTime, lblMode, txtDescription);
        checkIfAlreadyRegistered();
    }

    private void loadCourseDetails(JLabel lblPoster, JLabel lblName, JLabel lblDate,
                                   JLabel lblTime, JLabel lblMode, JTextArea txtDescription) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement("SELECT * FROM courses WHERE course_id = ?")) {
            pst.setString(1, courseId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                lblName.setText(rs.getString("course_name"));

                java.util.Date date = rs.getDate("course_date");
                java.sql.Time time = rs.getTime("course_time");
                SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy"); // Match AvailableCoursesPage format
                SimpleDateFormat tf = new SimpleDateFormat("hh:mm a");       // Match AvailableCoursesPage format
                lblDate.setText(" Date: " + df.format(date));
                lblTime.setText(" Time: " + tf.format(time));
                lblMode.setText("Mode: " + rs.getString("mode"));

                txtDescription.setText(rs.getString("description"));

                String poster = rs.getString("poster");

                // Poster loading and scaling logic
                if (poster != null && !poster.equals("No file chosen")) {
                    File posterFile = new File("posters/" + poster);
                    if (posterFile.exists()) {
                        ImageIcon icon = new ImageIcon(posterFile.getAbsolutePath());
                        Image img = icon.getImage();

                        int originalWidth = icon.getIconWidth();
                        int originalHeight = icon.getIconHeight();

                        int maxWidth = 700;
                        int maxHeight = 500;
                        int newWidth = originalWidth;
                        int newHeight = originalHeight;

                        if (originalWidth > maxWidth || originalHeight > maxHeight) {
                            double widthRatio = (double) maxWidth / originalWidth;
                            double heightRatio = (double) maxHeight / originalHeight;
                            double ratio = Math.min(widthRatio, heightRatio);

                            newWidth = (int) (originalWidth * ratio);
                            newHeight = (int) (originalHeight * ratio);

                            Image scaledImg = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                            lblPoster.setIcon(new ImageIcon(scaledImg));
                        } else {
                            lblPoster.setIcon(icon);
                        }

                        lblPoster.setPreferredSize(new Dimension(newWidth, newHeight));

                    } else {
                        lblPoster.setText("Poster File Not Found");
                        lblPoster.setPreferredSize(new Dimension(400, 100));
                    }
                } else {
                    lblPoster.setText("No Poster Available");
                    lblPoster.setPreferredSize(new Dimension(400, 100));
                }

            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading course details: " + ex.getMessage());
        }
    }

    /**
     * Checks the course_registrations table for an ACTIVE (is_cancelled = FALSE) registration.
     */
    private void checkIfAlreadyRegistered() {
        String sql = "SELECT * FROM course_registrations WHERE student_admission_no = ? AND course_id = ? AND is_cancelled = FALSE";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, studentAdmissionNo);
            pst.setString(2, courseId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                // Student already actively registered
                btnRegister.setEnabled(false);
                btnRegister.setText("Already Registered ✅");
                btnRegister.setBackground(SUCCESS_GREEN);
                btnRegister.setCursor(Cursor.getDefaultCursor());
            } else {
                // Not registered or registration was cancelled
                btnRegister.setEnabled(true);
                btnRegister.setText("Register");
                btnRegister.setBackground(PRIMARY_BLUE);
                btnRegister.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error checking registration status: " + ex.getMessage());
        }
    }

    /**
     * Inserts a new active registration record into the course_registrations table.
     */
    private void registerForCourse() {
        String sql = "INSERT INTO course_registrations (student_admission_no, course_id, is_cancelled) VALUES (?, ?, FALSE)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, studentAdmissionNo);
            pst.setString(2, courseId);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "You have successfully registered for this course! 🎉");
            
            // Update the UI and refresh the AvailableCoursesPage
            checkIfAlreadyRegistered(); 

            // THIS IS THE CRITICAL BLOCK that requires the method in MainFrame.java
            if (main.getAvailableCoursesPage() != null) {
                main.getAvailableCoursesPage().refreshCourses();
            }

        } catch (SQLIntegrityConstraintViolationException ex) {
            JOptionPane.showMessageDialog(this, "You are already registered for this course (or a previous cancellation exists).", "Registration Error", JOptionPane.ERROR_MESSAGE);
            checkIfAlreadyRegistered();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error during registration: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void styleButton(JButton b, Color bg, Color hover) {
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setPreferredSize(new Dimension(150, 40)); // Consistent size for action buttons
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                // Do not apply hover to the 'Already Registered' button
                if (b.isEnabled()) b.setBackground(hover);
            }
            public void mouseExited(MouseEvent e) {
                // Restore original color, but keep the green if already registered
                if (b.isEnabled()) {
                    if (b.getText().equals("Already Registered ✅")) {
                        b.setBackground(SUCCESS_GREEN);
                    } else {
                        b.setBackground(bg);
                    }
                } else if (b.getText().equals("Already Registered ✅")) {
                     b.setBackground(SUCCESS_GREEN);
                } else {
                    b.setBackground(bg);
                }
            }
        });
    }
}