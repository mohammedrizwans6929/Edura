import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.File;
import java.util.Calendar; // Required for date/time comparison

public class AvailableCoursesPage extends JPanel {
    private MainFrame main;
    private String studentAdmissionNo;
    
    private JTabbedPane tabbedPane;
    private JPanel upcomingCoursesPanel;
    private JPanel completedCoursesPanel;
    
    private Color primary = new Color(52, 152, 219);
    private Color primaryDark = new Color(41, 128, 185);
    private final Color SUCCESS_GREEN = new Color(39, 174, 96); // For COMPLETED status
    private final Color DANGER_RED = new Color(231, 76, 60);    // For ABSENT status

    public AvailableCoursesPage(MainFrame main, String studentAdmissionNo) {
        this.main = main;
        this.studentAdmissionNo = studentAdmissionNo;
        setLayout(new BorderLayout());
        initUI();
    }

    private void initUI() {
        // ===== Header =====
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(primary);
        header.setPreferredSize(new Dimension(800, 60));
        JLabel lblTitle = new JLabel("Available Courses");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        header.add(lblTitle, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // ===== Tabbed Pane Setup =====
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        // --- 1. Upcoming Courses Tab ---
        upcomingCoursesPanel = createCardContainerPanel();
        JScrollPane upcomingScroll = new JScrollPane(upcomingCoursesPanel);
        upcomingScroll.getVerticalScrollBar().setUnitIncrement(16);
        upcomingScroll.setBorder(BorderFactory.createEmptyBorder());
        tabbedPane.addTab(" Upcoming Courses", upcomingScroll);

        // --- 2. Completed Courses Tab ---
        completedCoursesPanel = createCardContainerPanel();
        JScrollPane completedScroll = new JScrollPane(completedCoursesPanel);
        completedScroll.getVerticalScrollBar().setUnitIncrement(16);
        completedScroll.setBorder(BorderFactory.createEmptyBorder());
        tabbedPane.addTab(" Past Courses", completedScroll);
        
        add(tabbedPane, BorderLayout.CENTER);

        // ===== Footer (Back button) =====
        JPanel footer = new JPanel();
        JButton btnBack = new JButton("Back to Dashboard");

        styleButton(btnBack, primary, primaryDark);

        btnBack.addActionListener(e -> main.showPage("dashboard"));
        footer.add(btnBack);
        add(footer, BorderLayout.SOUTH);

        loadCourses();
    }
    
    /** Creates a JPanel configured to hold stacked course cards. */
    private JPanel createCardContainerPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        panel.setBackground(new Color(245, 247, 250));
        return panel;
    }

    public void refreshCourses() {
        loadCourses();
    }

    private void loadCourses() {
        upcomingCoursesPanel.removeAll();
        completedCoursesPanel.removeAll();

        // Get current timestamp for comparison
        long currentTimeMillis = System.currentTimeMillis(); 

        // SQL to fetch ALL non-registered, non-deleted courses.
        // It fetches any course that the student does NOT have an active registration for.
        String sql = 
            "SELECT c.* FROM courses c " +
            "LEFT JOIN course_registrations cr ON c.course_id = cr.course_id " +
            "AND cr.student_admission_no = ? AND cr.is_cancelled = FALSE " + 
            "WHERE c.is_deleted = FALSE AND cr.registration_id IS NULL " + 
            "ORDER BY c.course_date ASC, c.course_time ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studentAdmissionNo); 

            try (ResultSet rs = pstmt.executeQuery()) {
                boolean foundUpcoming = false;
                boolean foundCompleted = false;

                while (rs.next()) {
                    String courseId = rs.getString("course_id");
                    String courseName = rs.getString("course_name");
                    String mode = rs.getString("mode");
                    java.util.Date date = rs.getDate("course_date");
                    java.sql.Time time = rs.getTime("course_time");
                    String poster = rs.getString("poster");

                    // CORE ROBUST SEPARATION LOGIC
                    Calendar courseCalendar = Calendar.getInstance();
                    courseCalendar.setTime(date); 
                    
                    // Add the time component's milliseconds to the date's milliseconds
                    courseCalendar.setTimeInMillis(date.getTime() + time.getTime()); 
                    
                    // Add 1 hour buffer (3600000 milliseconds) to define the course 'finish time'
                    long courseEndTimestamp = courseCalendar.getTimeInMillis() + 3600000;
                    
                    // If the course's estimated end time is LATER than the current time, it's upcoming.
                    boolean isUpcoming = courseEndTimestamp > currentTimeMillis;
                    
                    // Determine final status for the card display
                    String finalStatus = "PAST";
                    if (!isUpcoming) {
                        // For past courses the student is NOT currently registered for, 
                        // check if they were previously registered and attended.
                        finalStatus = getStudentCourseStatus(conn, courseId);
                    }

                    JPanel card = createCourseCard(courseId, courseName, date, time, mode, poster, isUpcoming, finalStatus);

                    if (isUpcoming) {
                        upcomingCoursesPanel.add(card);
                        upcomingCoursesPanel.add(Box.createVerticalStrut(10)); // Spacer
                        foundUpcoming = true;
                    } else {
                        completedCoursesPanel.add(card);
                        completedCoursesPanel.add(Box.createVerticalStrut(10)); // Spacer
                        foundCompleted = true;
                    }
                }
                
                // Add empty state messages and glue to keep content centered
                if (!foundUpcoming) {
                    addNoCourseMessage(upcomingCoursesPanel, "No upcoming courses available for registration.");
                } else {
                    upcomingCoursesPanel.add(Box.createVerticalGlue());
                }
                
                if (!foundCompleted) {
                    addNoCourseMessage(completedCoursesPanel, "No past courses are currently available for registration.");
                } else {
                    completedCoursesPanel.add(Box.createVerticalGlue());
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading courses: " + ex.getMessage());
        }

        upcomingCoursesPanel.revalidate();
        upcomingCoursesPanel.repaint();
        completedCoursesPanel.revalidate();
        completedCoursesPanel.repaint();
        
        // Ensure scroll position is reset to top for the currently visible tab
        SwingUtilities.invokeLater(() -> {
            JScrollPane currentScroll = (JScrollPane) tabbedPane.getSelectedComponent();
            if (currentScroll != null) {
                currentScroll.getVerticalScrollBar().setValue(0);
            }
        });
    }
    
    /**
     * Checks if the student was previously registered for a course and what the outcome was.
     * Statuses: "COMPLETED", "ABSENT", or "EXPIRED" (default, meaning no record found).
     */
    private String getStudentCourseStatus(Connection conn, String courseId) throws SQLException {
        // First, check if the student was ever registered (cancelled or not).
        String regSql = "SELECT COUNT(*) FROM course_registrations WHERE course_id = ? AND student_admission_no = ?";
        try (PreparedStatement regPst = conn.prepareStatement(regSql)) {
            regPst.setString(1, courseId);
            regPst.setString(2, studentAdmissionNo);
            
            try (ResultSet regRs = regPst.executeQuery()) {
                if (regRs.next() && regRs.getInt(1) == 0) {
                    // Student was NEVER registered.
                    return "EXPIRED"; 
                }
            }
        }
        
        // Student was registered at some point (cancelled or currently inactive).
        // Check for attendance.
        String attSql = "SELECT COUNT(*) FROM attendance WHERE course_id = ? AND admission_no = ? AND status = 'Present'";
        try (PreparedStatement attPst = conn.prepareStatement(attSql)) {
            attPst.setString(1, courseId);
            attPst.setString(2, studentAdmissionNo);
            
            try (ResultSet attRs = attPst.executeQuery()) {
                if (attRs.next() && attRs.getInt(1) > 0) {
                    return "COMPLETED";
                }
            }
        }
        
        // If registered but no 'Present' record was found, mark as absent.
        return "ABSENT";
    }

    private void addNoCourseMessage(JPanel panel, String message) {
        JLabel noCourses = new JLabel(message);
        noCourses.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        noCourses.setForeground(Color.GRAY);
        noCourses.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(Box.createVerticalGlue());
        panel.add(noCourses);
        panel.add(Box.createVerticalStrut(20)); // Padding
        panel.add(Box.createVerticalGlue());
    }

    /**
     * Creates a single course card. Registration is ONLY enabled for upcoming courses.
     */
    private JPanel createCourseCard(String courseId, String courseName, Date date, Time time, String mode, String poster, boolean isUpcoming, String status) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setMaximumSize(new Dimension(750, 100)); 
        card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        card.setBackground(Color.WHITE);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Cursor setup
        if (isUpcoming) {
            card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            card.setCursor(Cursor.getDefaultCursor());
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

        // --- Left: Poster Thumbnail (logic remains the same) ---
        JLabel lblPoster = new JLabel();
        lblPoster.setPreferredSize(new Dimension(80, 80));
        lblPoster.setHorizontalAlignment(SwingConstants.CENTER);
        lblPoster.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
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

        // --- Center: Info (logic remains the same) ---
        JPanel infoPanel = new JPanel(new GridLayout(3, 1));
        infoPanel.setOpaque(false);
        JLabel lblName = new JLabel("<html><b>" + courseName + "</b></html>");
        JLabel lblDateTime = new JLabel("📅 Date: " + dateFormat.format(date) + " | 🕒 Time: " + timeFormat.format(time));
        JLabel lblMode = new JLabel("Mode: " + mode);
        infoPanel.add(lblName);
        infoPanel.add(lblDateTime);
        infoPanel.add(lblMode);
        card.add(infoPanel, BorderLayout.CENTER);

        // --- Right: Status Indicator ---
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        statusPanel.setOpaque(false);

        JLabel lblStatus = new JLabel(status);
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        // 🔑 Dynamic Status Coloring
        if (isUpcoming) {
             lblStatus.setText("REGISTER");
             lblStatus.setForeground(SUCCESS_GREEN); // Green for action
        } else if (status.equals("COMPLETED")) {
             lblStatus.setForeground(SUCCESS_GREEN);
        } else if (status.equals("ABSENT")) {
             lblStatus.setForeground(DANGER_RED);
        } else { // PAST / EXPIRED
             lblStatus.setForeground(Color.GRAY);
        }
        
        statusPanel.add(lblStatus);
        
        JLabel lblArrow = new JLabel(" > ");
        lblArrow.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblArrow.setForeground(isUpcoming ? primary : Color.LIGHT_GRAY);
        statusPanel.add(lblArrow);

        card.add(statusPanel, BorderLayout.EAST);

        // Click handler: ONLY enable if it is an upcoming course
        if (isUpcoming) {
            card.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    main.showCourseDetailsPage(courseId, studentAdmissionNo);
                }
            });
        }

        return card;
    }

    private void styleButton(JButton b, Color bg, Color hover) {
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setPreferredSize(new Dimension(180, 35)); // Consistent button size
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if (b.isEnabled()) b.setBackground(hover);}
            public void mouseExited(MouseEvent e) { if (b.isEnabled()) b.setBackground(bg);}
        });
    }
}