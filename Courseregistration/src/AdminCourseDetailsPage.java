import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date; // Required for formatting sql.Date/Time

public class AdminCourseDetailsPage extends JPanel {
    private MainFrame main;
    private String courseId;
    private JLabel lblName, lblDate, lblTime, lblMode;
    private Color primary = new Color(52, 152, 219);

    public AdminCourseDetailsPage(MainFrame main, String courseId) {
        this.main = main;
        this.courseId = courseId;
        setLayout(new BorderLayout());
        initUI();
        loadDetails();
    }

    private void initUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(primary);
        header.setPreferredSize(new Dimension(800, 60));

        JLabel lblTitle = new JLabel("Course Details");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

        JButton btnBack = new JButton("Back");
        styleButton(btnBack, new Color(231, 76, 60), new Color(192, 57, 43));
        btnBack.addActionListener(e -> main.showPage("managecourses"));

        header.add(lblTitle, BorderLayout.WEST);
        header.add(btnBack, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        lblName = new JLabel();
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblDate = new JLabel();
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblTime = new JLabel();
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblMode = new JLabel();
        lblMode.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        content.add(lblName);
        content.add(Box.createVerticalStrut(15));
        content.add(lblDate);
        content.add(Box.createVerticalStrut(10));
        content.add(lblTime);
        content.add(Box.createVerticalStrut(10));
        content.add(lblMode);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnEdit = new JButton("Edit");
        JButton btnDelete = new JButton("Delete");
        
        styleButton(btnEdit, new Color(41, 128, 185), new Color(52, 152, 219));
        styleButton(btnDelete, new Color(231, 76, 60), new Color(192, 57, 43));


        btnEdit.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Edit function initiated. Navigation to dedicated Edit Page required.");
            // Logic to launch EditCoursePage/EditCourseDialog would go here
            loadDetails(); 
        });

        btnDelete.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to PERMANENTLY delete this course? This will remove ALL associated records (registrations, attendance, results).",
                    "Confirm Permanent Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                deleteCourse();
            }
        });

        buttonPanel.add(btnEdit);
        buttonPanel.add(btnDelete);

        add(content, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadDetails() {
        // Define formatters for date and time
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement("SELECT * FROM courses WHERE course_id = ?")) {
            pst.setString(1, courseId);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                // Get the SQL Date and Time objects
                Date sqlDate = rs.getDate("course_date");
                Date sqlTime = rs.getTime("course_time");
                
                // Format the Date and Time for display
                String formattedDate = (sqlDate != null) ? dateFormat.format(sqlDate) : "N/A";
                String formattedTime = (sqlTime != null) ? timeFormat.format(sqlTime) : "N/A";

                // Update the JLabels
                lblName.setText(rs.getString("course_name"));
                lblDate.setText("📅 Date: " + formattedDate);
                lblTime.setText("🕒 Time: " + formattedTime);
                lblMode.setText("Mode: " + rs.getString("mode"));
            } else {
                 lblName.setText("Course Not Found");
                 lblDate.setText("");
                 lblTime.setText("");
                 lblMode.setText("");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading details: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCourse() {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction for atomicity

            // --- Delete Dependent Records (Order is crucial to avoid FK violation) ---
            
            // 1. Delete from course_results (Certificates/Final Grades)
            try (PreparedStatement pstDeleteResults = conn.prepareStatement("DELETE FROM course_results WHERE course_id = ?")) {
                pstDeleteResults.setString(1, courseId);
                pstDeleteResults.executeUpdate();
            }

            // 2. Delete from attendance (Attendance History)
            try (PreparedStatement pstDeleteAttendance = conn.prepareStatement("DELETE FROM attendance WHERE course_id = ?")) {
                pstDeleteAttendance.setString(1, courseId);
                pstDeleteAttendance.executeUpdate();
            }

            // 3. Delete from course_registrations (Active Enrollments)
            try (PreparedStatement pstDeleteRegistrations = conn.prepareStatement(
                    "DELETE FROM course_registrations WHERE course_id = ?")) { 
                pstDeleteRegistrations.setString(1, courseId);
                pstDeleteRegistrations.executeUpdate();
            }

            // 4. Delete the course itself from the 'courses' table
            try (PreparedStatement pstDeleteCourse = conn.prepareStatement(
                    "DELETE FROM courses WHERE course_id = ?")) {
                pstDeleteCourse.setString(1, courseId);
                pstDeleteCourse.executeUpdate();
            }

            conn.commit(); // Commit the transaction if all deletions succeed
            
            JOptionPane.showMessageDialog(this, "Course and all related data deleted successfully! ✔️");
            main.showPage("managecourses");
            
        } catch (SQLException ex) {
            // Rollback on failure
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rbEx) {
                    // ignored
                }
            }
            JOptionPane.showMessageDialog(this, "Error deleting course: " + ex.getMessage() + "\nDatabase operation failed and was rolled back.", "Deletion Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            // Restore default auto-commit and close connection
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    // ignored
                }
            }
        }
    }

    private void styleButton(JButton b, Color bg, Color hover) {
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setPreferredSize(new Dimension(100, 35));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(hover); }
            public void mouseExited(MouseEvent e) { b.setBackground(bg); }
        });
    }

}
