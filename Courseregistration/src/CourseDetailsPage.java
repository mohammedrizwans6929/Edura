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

    public CourseDetailsPage(MainFrame main, String courseId, String studentAdmissionNo) {
        this.main = main;
        this.courseId = courseId;
        this.studentAdmissionNo = studentAdmissionNo;
        setLayout(new BorderLayout());
        initUI();
    }

    private void initUI() {
        Color primary = new Color(52, 152, 219);

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

        // Poster Label setup (Note: We remove the fixed sizesetPreferredSize here
        // so the BoxLayout respects the image's dimensions inside the scroll pane)
        JLabel lblPoster = new JLabel();
        lblPoster.setHorizontalAlignment(SwingConstants.CENTER);
        lblPoster.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblPoster.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY)); // Border helps visualize the image area
        
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

        // 🔴 FIX: Make the page scrollable by wrapping content in JScrollPane
        JScrollPane mainScrollPane = new JScrollPane(content);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(16); // Smooth scrolling
        mainScrollPane.setBorder(BorderFactory.createEmptyBorder()); // Remove default border
        add(mainScrollPane, BorderLayout.CENTER);

        // Footer with Back and Register
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        footer.setBackground(Color.WHITE);

        JButton btnBack = new JButton("Back");
        styleButton(btnBack, new Color(231, 76, 60), new Color(192, 57, 43));
        btnBack.addActionListener(e -> main.showPage("availablecourses"));

        btnRegister = new JButton("Register");
        styleButton(btnRegister, primary, primary.darker());
        btnRegister.addActionListener(e -> registerForCourse());

        footer.add(btnBack);
        footer.add(btnRegister);

        add(footer, BorderLayout.SOUTH);

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
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat tf = new SimpleDateFormat("HH:mm");
                lblDate.setText("📅 Date: " + df.format(date));
                lblTime.setText("🕒 Time: " + tf.format(time));
                lblMode.setText("Mode: " + rs.getString("mode"));

                txtDescription.setText(rs.getString("description"));

                String poster = rs.getString("poster");
                
                // 🔴 FIX: Poster loading and scaling for full view
                if (poster != null && !poster.equals("No file chosen")) {
                    File posterFile = new File("posters/" + poster);
                    if (posterFile.exists()) {
                        ImageIcon icon = new ImageIcon(posterFile.getAbsolutePath());
                        Image img = icon.getImage();

                        int originalWidth = icon.getIconWidth();
                        int originalHeight = icon.getIconHeight();
                        
                        // Set a practical max size for display inside the scroll pane
                        int maxWidth = 700;
                        int maxHeight = 500;
                        int newWidth = originalWidth;
                        int newHeight = originalHeight;

                        // Only scale down if the image is too large for the screen/panel
                        if (originalWidth > maxWidth || originalHeight > maxHeight) {
                            double widthRatio = (double) maxWidth / originalWidth;
                            double heightRatio = (double) maxHeight / originalHeight;
                            double ratio = Math.min(widthRatio, heightRatio); // Maintain aspect ratio

                            newWidth = (int) (originalWidth * ratio);
                            newHeight = (int) (originalHeight * ratio);
                            
                            Image scaledImg = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                            lblPoster.setIcon(new ImageIcon(scaledImg));
                        } else {
                            // Image fits, show full quality
                            lblPoster.setIcon(icon);
                        }
                        
                        // Adjust the label size to fit the displayed image
                        lblPoster.setPreferredSize(new Dimension(newWidth, newHeight));
                        
                    } else {
                        lblPoster.setText("Poster File Not Found");
                    }
                } else {
                    lblPoster.setText("No Poster Available");
                    lblPoster.setPreferredSize(new Dimension(400, 100)); // Default size when no image
                }

            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading course details: " + ex.getMessage());
        }
    }

    private void checkIfAlreadyRegistered() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(
                     "SELECT * FROM registrations WHERE admission_no = ? AND course_id = ?")) {
            pst.setString(1, studentAdmissionNo);
            pst.setString(2, courseId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                // Student already registered 
                btnRegister.setEnabled(false);
                btnRegister.setText("Already Registered ✅");
                btnRegister.setBackground(new Color(46, 204, 113)); // Green
                btnRegister.setCursor(Cursor.getDefaultCursor());
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error checking registration: " + ex.getMessage());
        }
    }

    private void registerForCourse() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(
                     "INSERT INTO registrations (admission_no, course_id) VALUES (?, ?)")) {
            pst.setString(1, studentAdmissionNo);
            pst.setString(2, courseId);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "You have successfully registered for this course!");
            checkIfAlreadyRegistered(); // Update button state immediately

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error during registration: " + ex.getMessage());
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
            public void mouseEntered(MouseEvent e) { 
                if (b.isEnabled()) b.setBackground(hover);
            }
            public void mouseExited(MouseEvent e) { 
                if (b.isEnabled() && !b.getText().equals("Already Registered ✅")) b.setBackground(bg);
            }
        });
    }
}