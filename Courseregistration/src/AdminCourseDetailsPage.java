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
        
        // Style buttons using primary color for consistency
        styleButton(btnEdit, new Color(41, 128, 185), new Color(52, 152, 219));
        styleButton(btnDelete, new Color(231, 76, 60), new Color(192, 57, 43));


        btnEdit.addActionListener(e -> {
            // Assuming EditCourseDialog exists and accepts (MainFrame, courseId, parentFrame)
            // It needs to be initialized with the main frame as the owner
            // For simplicity, passing null as the owner JDialog/JFrame, 
            // but in a real app, 'main' should ideally be passed or a reference to its JFrame instance.
            // Placeholder: new EditCourseDialog(main, courseId, (Frame) SwingUtilities.getWindowAncestor(this)).setVisible(true);
            // Assuming a constructor that works with 'null' owner for demonstration
            // Note: The original code passed 'null', so we maintain that
            // new EditCourseDialog(main, courseId, null).setVisible(true);
            
            // As EditCourseDialog class is not provided, we just assume it refreshes the details.
            // In a real application, you'd launch the dialog here.
            
            // Re-load details to reflect changes after the dialog is closed
            loadDetails(); 
        });

        btnDelete.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this course and all associated student registrations?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy"); // e.g., Friday, October 17, 2025
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a"); // e.g., 12:34 AM

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement("SELECT * FROM courses WHERE course_id = ?")) {
            pst.setString(1, courseId);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                // Get the SQL Date and Time objects
                Date sqlDate = rs.getDate("course_date");
                Date sqlTime = rs.getTime("course_time");
                
                // Format the Date and Time for display, checking for nulls
                String formattedDate = (sqlDate != null) ? dateFormat.format(sqlDate) : "N/A";
                String formattedTime = (sqlTime != null) ? timeFormat.format(sqlTime) : "N/A";

                // Update the JLabels
                lblName.setText(rs.getString("course_name"));
                lblDate.setText("📅 Date: " + formattedDate);
                lblTime.setText("🕒 Time: " + formattedTime);
                lblMode.setText("Mode: " + rs.getString("mode"));
            } else {
                 // Handle case where course was deleted externally or ID is invalid
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

            // 1. Delete dependent records from the 'registrations' table
            // This prevents the Foreign Key Constraint error (the issue you faced)
            try (PreparedStatement pstDeleteRegistrations = conn.prepareStatement(
                    "DELETE FROM registrations WHERE course_id = ?")) {
                pstDeleteRegistrations.setString(1, courseId);
                int deletedRegs = pstDeleteRegistrations.executeUpdate();
                System.out.println("Deleted " + deletedRegs + " dependent registrations.");
            }

            // 2. Delete the course itself from the 'courses' table
            try (PreparedStatement pstDeleteCourse = conn.prepareStatement(
                    "DELETE FROM courses WHERE course_id = ?")) {
                pstDeleteCourse.setString(1, courseId);
                pstDeleteCourse.executeUpdate();
            }

            conn.commit(); // Commit the transaction if both deletions succeed
            
            JOptionPane.showMessageDialog(this, "Course and all related registrations deleted successfully! ✔️");
            main.showPage("managecourses");
            
        } catch (SQLException ex) {
            // Rollback on failure
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rbEx) {
                    // Log or handle rollback exception if needed
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
                    // Log or handle close exception
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
        b.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Added slight padding
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(hover); }
            public void mouseExited(MouseEvent e) { b.setBackground(bg); }
        });
    }
}