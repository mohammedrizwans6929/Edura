import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.io.File; 

public class MyCoursesPage extends JPanel {
    private MainFrame main;
    private String studentAdmissionNo;
    
    private JPanel upcomingCoursesPanel;
    private JPanel completedCoursesPanel;
    
    private Color primary = new Color(52, 152, 219);
    private Color danger = new Color(231, 76, 60);

    public MyCoursesPage(MainFrame main, String studentAdmissionNo) {
        this.main = main;
        this.studentAdmissionNo = studentAdmissionNo;
        setLayout(new BorderLayout());
        initUI();
        loadCourses();
    }

    private void initUI() {
        // ===== Header =====
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(primary);
        header.setPreferredSize(new Dimension(800, 60));

        JLabel lblTitle = new JLabel("My Courses");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        header.add(lblTitle, BorderLayout.WEST);

        add(header, BorderLayout.NORTH);

        // ===== Tabs and Scrollable Content =====
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        // --- Upcoming Courses Panel ---
        upcomingCoursesPanel = createCardContainerPanel();
        JScrollPane upcomingScroll = new JScrollPane(upcomingCoursesPanel);
        upcomingScroll.getVerticalScrollBar().setUnitIncrement(16);
        tabs.addTab("🗓️ Upcoming Courses", upcomingScroll);

        // --- Completed Courses Panel ---
        completedCoursesPanel = createCardContainerPanel();
        JScrollPane completedScroll = new JScrollPane(completedCoursesPanel);
        completedScroll.getVerticalScrollBar().setUnitIncrement(16);
        tabs.addTab("✅ Completed Courses", completedScroll);

        // Add the tabs panel to the main frame
        add(tabs, BorderLayout.CENTER);

        // ===== Back Button at Bottom =====
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnBack = new JButton("Back to Dashboard");
        styleButton(btnBack, primary, primary.darker(), new Dimension(180, 35));
        btnBack.addActionListener(e -> main.showPage("dashboard"));
        bottomPanel.add(btnBack);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /** Creates a JPanel configured to hold stacked course cards. */
    private JPanel createCardContainerPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        panel.setBackground(new Color(245, 247, 250));
        return panel;
    }

    // --- Course Loading Logic ---

    public void loadCourses() {
        upcomingCoursesPanel.removeAll();
        completedCoursesPanel.removeAll();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(
                     "SELECT c.course_id, c.course_name, c.course_date, c.course_time, c.mode, c.poster " +
                     "FROM courses c " +
                     "JOIN registrations r ON c.course_id = r.course_id " +
                     "WHERE r.admission_no = ? AND c.is_deleted = FALSE " + 
                     "ORDER BY c.course_date ASC")) {

            pst.setString(1, studentAdmissionNo);
            ResultSet rs = pst.executeQuery();
            
            Date today = new Date();
            
            boolean foundUpcoming = false;
            boolean foundCompleted = false;

            while (rs.next()) {
                String courseId = rs.getString("course_id");
                String courseName = rs.getString("course_name");
                Date courseDate = rs.getDate("course_date");
                Time courseTime = rs.getTime("course_time");
                String mode = rs.getString("mode");
                String poster = rs.getString("poster");
                
                // Combine date and time for determining Upcoming/Completed status
                Calendar courseCal = Calendar.getInstance();
                courseCal.setTime(courseDate);
                courseCal.set(Calendar.HOUR_OF_DAY, courseTime.getHours());
                courseCal.set(Calendar.MINUTE, courseTime.getMinutes());
                Date courseDateTime = courseCal.getTime();

                boolean isUpcoming = courseDateTime.after(today);

                // Determine final status for completed courses
                String finalStatus = "COMPLETED"; // Default status
                if (!isUpcoming) {
                    finalStatus = getCourseAttendanceStatus(conn, courseId, studentAdmissionNo);
                }

                // Create the card
                JPanel courseCard = createCourseCard(courseId, courseName, courseDate, courseTime, mode, poster, isUpcoming, finalStatus);
                
                if (isUpcoming) {
                    upcomingCoursesPanel.add(courseCard);
                    upcomingCoursesPanel.add(Box.createVerticalStrut(10));
                    foundUpcoming = true;
                } else {
                    completedCoursesPanel.add(courseCard);
                    completedCoursesPanel.add(Box.createVerticalStrut(10));
                    foundCompleted = true;
                }
            }
            
            if (!foundUpcoming) addNoCourseMessage(upcomingCoursesPanel, "You have no upcoming registered courses.");
            if (!foundCompleted) addNoCourseMessage(completedCoursesPanel, "You have not completed any registered courses yet.");


        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading courses: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        
        upcomingCoursesPanel.revalidate();
        completedCoursesPanel.revalidate();
        upcomingCoursesPanel.repaint();
        completedCoursesPanel.repaint();
    }
    
    /**
     * Checks attendance for a finished course to determine if status is COMPLETED or ABSENT.
     * Assumes one 'Present' is sufficient for 'Completed'.
     */
    private String getCourseAttendanceStatus(Connection conn, String courseId, String admissionNo) throws SQLException {
        // Check for any 'Present' record
        String sql = "SELECT COUNT(*) FROM attendance WHERE course_id = ? AND admission_no = ? AND status = 'Present'";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, courseId);
            pst.setString(2, admissionNo);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next() && rs.getInt(1) > 0) {
                return "COMPLETED";
            }
        }
        // If no 'Present' records were found, the student is marked as ABSENT
        return "ABSENT";
    }

    private void addNoCourseMessage(JPanel panel, String message) {
        JLabel lblMessage = new JLabel(message, SwingConstants.CENTER);
        lblMessage.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblMessage.setForeground(Color.GRAY);
        panel.add(Box.createVerticalStrut(50));
        panel.add(lblMessage);
    }

    /**
     * Creates a visually appealing course card.
     */
    private JPanel createCourseCard(String courseId, String courseName, Date courseDate, Time courseTime, String mode, String poster, boolean isUpcoming, String finalStatus) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setMaximumSize(new Dimension(750, 100));
        card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        card.setBackground(Color.WHITE);
        card.setAlignmentX(Component.LEFT_ALIGNMENT); 
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

        // --- Left: Poster Thumbnail ---
        JLabel lblPoster = new JLabel();
        lblPoster.setPreferredSize(new Dimension(80, 80));
        lblPoster.setHorizontalAlignment(SwingConstants.CENTER);
        lblPoster.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Load and scale poster
        if (poster != null && !poster.trim().isEmpty() && !poster.equals("No file chosen")) {
            File posterFile = new File("posters/" + poster);
            if (posterFile.exists()) {
                ImageIcon icon = new ImageIcon(posterFile.getAbsolutePath());
                Image img = icon.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH);
                lblPoster.setIcon(new ImageIcon(img));
            } else {
                lblPoster.setText("No File");
                lblPoster.setFont(new Font("Segoe UI", Font.ITALIC, 10));
            }
        } else {
            lblPoster.setText("No Poster");
            lblPoster.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        }
        card.add(lblPoster, BorderLayout.WEST);

        // --- Center: Info ---
        JPanel infoPanel = new JPanel(new GridLayout(3, 1));
        infoPanel.setOpaque(false);
        JLabel lblName = new JLabel("<html><b>" + courseName + "</b></html>");
        JLabel lblDateTime = new JLabel("📅 " + dateFormat.format(courseDate) + " | 🕒 " + timeFormat.format(courseTime));
        JLabel lblMode = new JLabel("Mode: " + mode);
        infoPanel.add(lblName);
        infoPanel.add(lblDateTime);
        infoPanel.add(lblMode);
        card.add(infoPanel, BorderLayout.CENTER);

        // --- Right: Status/Arrow Indicator ---
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        statusPanel.setOpaque(false);
        
        JLabel lblStatus;
        if (isUpcoming) {
            lblStatus = new JLabel("UPCOMING");
            lblStatus.setForeground(primary);
        } else {
            // 🔴 FIX: Set status based on finalStatus (ABSENT or COMPLETED)
            lblStatus = new JLabel(finalStatus);
            if (finalStatus.equals("ABSENT")) {
                 lblStatus.setForeground(danger); // Red for absent
            } else {
                 lblStatus.setForeground(new Color(39, 174, 96)); // Green for completed
            }
        }
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusPanel.add(lblStatus);
        
        // Add a simple indicator that it is clickable
        JLabel lblArrow = new JLabel(" > ");
        lblArrow.setFont(new Font("Segoe UI", Font.BOLD, 18));
        statusPanel.add(lblArrow);
        
        card.add(statusPanel, BorderLayout.EAST);
        
        // 🎯 Add mouse listener to navigate to the details page
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Navigate to the registered details page where cancellation is possible
                main.showCourseRegisteredDetails(courseId, studentAdmissionNo);
            }
        });
        
        return card;
    }
    
    // --- Utility Methods ---

    private void styleButton(JButton b, Color bg, Color hover, Dimension size) {
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setPreferredSize(size);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(hover); }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) { b.setBackground(bg); }
        });
    }
}