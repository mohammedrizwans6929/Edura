import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.io.File;

public class MyCoursesPage extends JPanel {
    private MainFrame main;
    private String studentAdmissionNo;
    
    private JPanel upcomingCoursesPanel;
    private JPanel completedCoursesPanel;
    
    private Color primary = new Color(52, 152, 219);
    private Color danger = new Color(231, 76, 60);
    private final long ONE_HOUR_MS = 60 * 60 * 1000;
    public MyCoursesPage(MainFrame main, String studentAdmissionNo) {
        this.main = main;
        this.studentAdmissionNo = studentAdmissionNo;
        setLayout(new BorderLayout());
        initUI();
       
    }

    private void initUI() {
      
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(primary);
        header.setPreferredSize(new Dimension(800, 60));

        JLabel lblTitle = new JLabel("My Courses");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        header.add(lblTitle, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

       
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        upcomingCoursesPanel = createCardContainerPanel();
        JScrollPane upcomingScroll = new JScrollPane(upcomingCoursesPanel);
        upcomingScroll.getVerticalScrollBar().setUnitIncrement(16);
        tabs.addTab(" Upcoming Courses", upcomingScroll);

        completedCoursesPanel = createCardContainerPanel();
        JScrollPane completedScroll = new JScrollPane(completedCoursesPanel);
        completedScroll.getVerticalScrollBar().setUnitIncrement(16);
        tabs.addTab(" Completed Courses", completedScroll);

        add(tabs, BorderLayout.CENTER);

       
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnBack = new JButton("Back to Dashboard");
        styleButton(btnBack, primary, primary.darker(), new Dimension(180, 35));
        btnBack.addActionListener(e -> main.showPage("dashboard"));
        bottomPanel.add(btnBack);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createCardContainerPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        panel.setBackground(new Color(245, 247, 250));
        return panel;
    }

   
    public void loadCourses() {
        upcomingCoursesPanel.removeAll();
        completedCoursesPanel.removeAll();

        
        String sql = "SELECT c.course_id, c.course_name, c.course_date, c.course_time, c.mode, c.poster " +
                     "FROM courses c " +
                     "JOIN course_registrations cr ON c.course_id = cr.course_id " +
                     "WHERE cr.student_admission_no = ? AND cr.is_cancelled = FALSE AND c.is_deleted = FALSE " + 
                     "ORDER BY c.course_date ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, studentAdmissionNo);
            ResultSet rs = pst.executeQuery();
            
            Date currentTime = new Date();
            
            boolean foundUpcoming = false;
            boolean foundCompleted = false;

            while (rs.next()) {
                String courseId = rs.getString("course_id");
                String courseName = rs.getString("course_name");
                Date courseDate = rs.getDate("course_date");
                Time courseTime = rs.getTime("course_time");
                String mode = rs.getString("mode");
                String poster = rs.getString("poster");
                
              
                Calendar courseCal = Calendar.getInstance();
                courseCal.setTime(courseDate);
                courseCal.set(Calendar.HOUR_OF_DAY, courseTime.getHours());
                courseCal.set(Calendar.MINUTE, courseTime.getMinutes());
                
               
                long courseEndTimestamp = courseCal.getTimeInMillis() + ONE_HOUR_MS;
                
                boolean isUpcoming = currentTime.getTime() < courseEndTimestamp;

                
                String finalStatus = "N/A";
                if (!isUpcoming) {
                   
                    finalStatus = getCourseAttendanceStatus(conn, courseId, studentAdmissionNo);
                }

              
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
    

    private String getCourseAttendanceStatus(Connection conn, String courseId, String admissionNo) throws SQLException {
      
        String sql = "SELECT COUNT(*) FROM attendance WHERE course_id = ? AND admission_no = ? AND status = 'Present'";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, courseId);
            pst.setString(2, admissionNo);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next() && rs.getInt(1) > 0) {
                return "COMPLETED";
            }
        }
     
        return "ABSENT";
    }

    private void addNoCourseMessage(JPanel panel, String message) {
        JLabel lblMessage = new JLabel(message, SwingConstants.CENTER);
        lblMessage.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblMessage.setForeground(Color.GRAY);
        panel.add(Box.createVerticalStrut(50));
        panel.add(lblMessage);
    }

   
    private JPanel createCourseCard(String courseId, String courseName, Date courseDate, Time courseTime, String mode, String poster, boolean isUpcoming, String finalStatus) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setMaximumSize(new Dimension(750, 100));
        card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        card.setBackground(Color.WHITE);
        card.setAlignmentX(Component.LEFT_ALIGNMENT); 
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

     
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

      
        JPanel infoPanel = new JPanel(new GridLayout(3, 1));
        infoPanel.setOpaque(false);
        JLabel lblName = new JLabel("<html><b>" + courseName + "</b></html>");
        JLabel lblDateTime = new JLabel("📅 " + dateFormat.format(courseDate) + " | 🕒 " + timeFormat.format(courseTime));
        JLabel lblMode = new JLabel("Mode: " + mode);
        infoPanel.add(lblName);
        infoPanel.add(lblDateTime);
        infoPanel.add(lblMode);
        card.add(infoPanel, BorderLayout.CENTER);

      
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        statusPanel.setOpaque(false);
        
        JLabel lblStatus;
        if (isUpcoming) {
            lblStatus = new JLabel("UPCOMING");
            lblStatus.setForeground(primary);
        } else {
        
            lblStatus = new JLabel(finalStatus);
            if (finalStatus.equals("ABSENT")) {
                 lblStatus.setForeground(danger);
            } else {
                 lblStatus.setForeground(new Color(39, 174, 96));
            }
        }
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusPanel.add(lblStatus);
        
       
        JLabel lblArrow = new JLabel(" > ");
        lblArrow.setFont(new Font("Segoe UI", Font.BOLD, 18));
        statusPanel.add(lblArrow);
        
        card.add(statusPanel, BorderLayout.EAST);
        
      
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                main.showCourseRegisteredDetails(courseId, studentAdmissionNo);
            }
        });
        
        return card;
    }
    
   
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
