import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.File; // For loading poster images

public class AvailableCoursesPage extends JPanel {
    private MainFrame main;
    private String studentAdmissionNo;
    private JPanel coursesPanel;
    private JScrollPane scrollPane;

    public AvailableCoursesPage(MainFrame main, String studentAdmissionNo) {
        this.main = main;
        this.studentAdmissionNo = studentAdmissionNo;
        setLayout(new BorderLayout());
        initUI();
    }

    private void initUI() {
        Color primary = new Color(52, 152, 219);
        // Define the hover color for the primary blue button
        Color primaryDark = new Color(41, 128, 185); 

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(primary);
        header.setPreferredSize(new Dimension(800, 60));
        JLabel lblTitle = new JLabel("Available Courses");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        header.add(lblTitle, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Content Panel for course cards
        coursesPanel = new JPanel();
        coursesPanel.setLayout(new BoxLayout(coursesPanel, BoxLayout.Y_AXIS)); 
        coursesPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); 
        coursesPanel.setBackground(new Color(245, 247, 250)); 

        scrollPane = new JScrollPane(coursesPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); 
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); 
        add(scrollPane, BorderLayout.CENTER);

        // Back button
        JPanel footer = new JPanel();
        JButton btnBack = new JButton("Back to Dashboard");
        
        // 🔑 MODIFICATION: Changed button colors to primary blue theme
        styleButton(btnBack, primary, primaryDark); 
        
        btnBack.addActionListener(e -> main.showPage("dashboard"));
        footer.add(btnBack);
        add(footer, BorderLayout.SOUTH);

        loadCourses();
    }

    // Call this to refresh courses whenever a new course is added, edited, or deleted
    public void refreshCourses() {
        loadCourses();
    }

    private void loadCourses() {
        coursesPanel.removeAll();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             // Filter out courses where is_deleted is TRUE (archived/logically deleted)
             ResultSet rs = stmt.executeQuery("SELECT * FROM courses WHERE is_deleted = FALSE ORDER BY course_date DESC")) {

            boolean foundCourses = false;
            while (rs.next()) {
                foundCourses = true;
                String courseId = rs.getString("course_id");
                String courseName = rs.getString("course_name");
                String mode = rs.getString("mode");
                java.util.Date date = rs.getDate("course_date");
                java.sql.Time time = rs.getTime("course_time");
                String poster = rs.getString("poster");

                JPanel card = createCourseCard(courseId, courseName, date, time, mode, poster);

                coursesPanel.add(card);
                coursesPanel.add(Box.createVerticalStrut(10)); // Spacer between cards
            }
            
            if (!foundCourses) {
                // Display message if no courses are available
                JLabel noCourses = new JLabel("No courses are currently available for registration. Check back soon!");
                noCourses.setFont(new Font("Segoe UI", Font.ITALIC, 16));
                noCourses.setForeground(Color.GRAY);
                
                // Add glue/strut to center the message vertically if the panel is large
                coursesPanel.add(Box.createVerticalGlue());
                coursesPanel.add(noCourses);
                coursesPanel.add(Box.createVerticalGlue());
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading courses: " + ex.getMessage());
        }

        coursesPanel.revalidate();
        coursesPanel.repaint();
        // Ensure scroll position is reset to top
        SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(0));
    }
    
    /**
     * Creates a single course card matching the MyCoursesPage style.
     */
    private JPanel createCourseCard(String courseId, String courseName, Date date, Time time, String mode, String poster) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setMaximumSize(new Dimension(750, 100)); // Fixed height for stacking
        card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        card.setBackground(Color.WHITE);
        card.setAlignmentX(Component.LEFT_ALIGNMENT); // Align left in the BoxLayout
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

        // --- Left: Poster Thumbnail ---
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

        // --- Center: Info ---
        JPanel infoPanel = new JPanel(new GridLayout(3, 1));
        infoPanel.setOpaque(false);
        JLabel lblName = new JLabel("<html><b>" + courseName + "</b></html>");
        JLabel lblDateTime = new JLabel("📅 Date: " + dateFormat.format(date) + " | 🕒 Time: " + timeFormat.format(time));
        JLabel lblMode = new JLabel("Mode: " + mode);
        infoPanel.add(lblName);
        infoPanel.add(lblDateTime);
        infoPanel.add(lblMode);
        card.add(infoPanel, BorderLayout.CENTER);

        // --- Right: Clickable Indicator ---
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        statusPanel.setOpaque(false);
        
        JLabel lblArrow = new JLabel(" > ");
        lblArrow.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblArrow.setForeground(new Color(52, 152, 219)); // Use primary color
        statusPanel.add(lblArrow);
        
        card.add(statusPanel, BorderLayout.EAST);
        
        // Click -> details page (CourseDetailsPage is for registration)
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                main.showCourseDetailsPage(courseId, studentAdmissionNo);
            }
        });
        
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