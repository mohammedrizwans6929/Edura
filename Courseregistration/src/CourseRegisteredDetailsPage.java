import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.io.File;

public class CourseRegisteredDetailsPage extends JPanel {
    private MainFrame main;
    private String courseId;
    private String studentAdmissionNo;
    private JButton btnCancelRegistration;
    
    private Color primary = new Color(52, 152, 219);
    private Color danger = new Color(231, 76, 60);

    
    private Date courseDateTime;
    private String courseName;

    public CourseRegisteredDetailsPage(MainFrame main, String studentAdmissionNo, String courseId) {
        this.main = main;
        this.courseId = courseId;
        this.studentAdmissionNo = studentAdmissionNo;
        setLayout(new BorderLayout());
        initUI();
        loadCourseDetails(); 
    }

    private void initUI() {
       
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(primary);
        header.setPreferredSize(new Dimension(800, 60));

        JLabel lblTitle = new JLabel("Registered Course Details");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        header.add(lblTitle, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        content.setBackground(new Color(245, 247, 250));

        JLabel lblPoster = new JLabel();
        lblPoster.setHorizontalAlignment(SwingConstants.CENTER);
        lblPoster.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblName = new JLabel();
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 24));
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
        
        JTextArea txtDescription = new JTextArea(10, 40);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setEditable(false);
        txtDescription.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtDescription.setBorder(BorderFactory.createTitledBorder("Description"));
        
        content.add(lblPoster);
        content.add(Box.createVerticalStrut(20));
        content.add(lblName);
        content.add(Box.createVerticalStrut(10));
        content.add(lblDate);
        content.add(lblTime);
        content.add(lblMode);
        content.add(Box.createVerticalStrut(20));
        content.add(new JScrollPane(txtDescription));

        JScrollPane mainScrollPane = new JScrollPane(content);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainScrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(mainScrollPane, BorderLayout.CENTER);

       
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        footer.setBackground(Color.WHITE);

        JButton btnBack = new JButton("Back to My Courses");
        styleButton(btnBack, primary, primary.darker(), new Dimension(180, 35));
        btnBack.addActionListener(e -> main.showMyCoursesPage()); 

        btnCancelRegistration = new JButton("Cancel Registration");
        styleButton(btnCancelRegistration, danger, danger.darker(), new Dimension(180, 35));
        btnCancelRegistration.addActionListener(e -> cancelRegistration());

        footer.add(btnBack);
        footer.add(btnCancelRegistration);

        add(footer, BorderLayout.SOUTH);
    }

    

    private void loadCourseDetails() {
       
        JPanel contentPanel = (JPanel)((JScrollPane)getComponent(1)).getViewport().getView();
        JLabel lblPoster = (JLabel) contentPanel.getComponent(0);
        JLabel lblName = (JLabel) contentPanel.getComponent(2);
        JLabel lblDate = (JLabel) contentPanel.getComponent(4);
        JLabel lblTime = (JLabel) contentPanel.getComponent(5);
        JLabel lblMode = (JLabel) contentPanel.getComponent(6);
        JTextArea txtDescription = (JTextArea) ((JScrollPane)contentPanel.getComponent(8)).getViewport().getView();
        
        try (Connection conn = DBConnection.getConnection();
              PreparedStatement pst = conn.prepareStatement("SELECT * FROM courses WHERE course_id = ?")) {
            pst.setString(1, courseId);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                this.courseName = rs.getString("course_name"); 
                lblName.setText(this.courseName);

                Date date = rs.getDate("course_date");
                Time time = rs.getTime("course_time");
                
               
                Calendar courseCal = Calendar.getInstance();
                courseCal.setTimeInMillis(date.getTime() + time.getTime());
                this.courseDateTime = courseCal.getTime();

                SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy");
                SimpleDateFormat tf = new SimpleDateFormat("hh:mm a");
                lblDate.setText("📅 Date: " + df.format(date));
                lblTime.setText("🕒 Time: " + tf.format(time));
                lblMode.setText("Mode: " + rs.getString("mode"));

                txtDescription.setText(rs.getString("description"));

                String poster = rs.getString("poster");
                loadPoster(lblPoster, poster);
                
                checkCancellationStatus();

            } else {
                lblName.setText("Course not found.");
                btnCancelRegistration.setEnabled(false);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading course details: " + ex.getMessage());
        }
    }

    private void loadPoster(JLabel lblPoster, String poster) {
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

    private void checkCancellationStatus() {
        if (this.courseDateTime == null) {
            btnCancelRegistration.setEnabled(false);
            return;
        }

        long courseTimeMillis = this.courseDateTime.getTime();
     
        long deadlineMillis = courseTimeMillis - TimeUnit.DAYS.toMillis(1);
        long currentTimeMillis = new Date().getTime();

        if (currentTimeMillis > deadlineMillis) {
            
            btnCancelRegistration.setEnabled(false);
            btnCancelRegistration.setText("Deadline Passed");
            btnCancelRegistration.setBackground(Color.GRAY);
        } else {
            btnCancelRegistration.setEnabled(true);
            btnCancelRegistration.setText("Cancel Registration");
            btnCancelRegistration.setBackground(danger);
        }
    }

   
    private void cancelRegistration() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel registration for " + this.courseName + "? This action is permanent and makes the slot available to others.",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection();
                
                 PreparedStatement pst = conn.prepareStatement(
                     "UPDATE course_registrations SET is_cancelled = TRUE, cancellation_date = CURRENT_TIMESTAMP " +
                     "WHERE student_admission_no = ? AND course_id = ? AND is_cancelled = FALSE")) {
                
                pst.setString(1, studentAdmissionNo);
                pst.setString(2, courseId);
                
                if (pst.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(this, "Registration successfully cancelled.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    
                  
                    btnCancelRegistration.setEnabled(false);
                    btnCancelRegistration.setText("Cancelled");
                    btnCancelRegistration.setBackground(Color.DARK_GRAY);

                    
                    main.showMyCoursesPage();
                    if (main.getAvailableCoursesPage() != null) {
                        main.getAvailableCoursesPage().refreshCourses();
                    }
                } else {
                    
                    JOptionPane.showMessageDialog(this, "Error: Active registration record not found. It may be already cancelled or the DB state is inconsistent.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
              
                JOptionPane.showMessageDialog(this, "Database error during cancellation: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
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
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { 
                if (b.isEnabled()) b.setBackground(hover); 
            }
            public void mouseExited(MouseEvent e) { 
                if (b.isEnabled()) b.setBackground(bg); 
            }
        });
    }

}
