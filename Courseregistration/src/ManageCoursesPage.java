import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.File; 

public class ManageCoursesPage extends JPanel {
    private MainFrame main;
    private JTabbedPane tabbedPane;
    private JPanel upcomingCoursesPanel;
    private JPanel completedCoursesPanel;
    private JPanel deletedCoursesPanel;
    
    // Search components
    private JTextField txtSearch;
    private JButton btnSearch;
    
    private Color primary = new Color(52, 152, 219);
    private Color primaryDark = new Color(41, 128, 185);

    public ManageCoursesPage(MainFrame main) {
        this.main = main;
        setLayout(new BorderLayout());
        initUI();
        loadCourses("upcoming", ""); // Load initially with no search term
    }

    private void initUI() {
        // ===== Top Container (Will hold Header and Search) =====
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setBackground(new Color(245, 247, 250)); // Set background for consistency
        
        // ===== 1. Header (Title and Back Button) =====
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(primary);
        header.setPreferredSize(new Dimension(800, 60));

        JLabel lblTitle = new JLabel("Manage Courses");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        
        JButton btnBack = new JButton("Back");
        styleButton(btnBack, new Color(231, 76, 60), new Color(192, 57, 43));
        btnBack.addActionListener(e -> main.showPage("admin"));

        JPanel headerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        headerButtons.setOpaque(false);
        headerButtons.add(btnBack);

        header.add(lblTitle, BorderLayout.WEST);
        header.add(headerButtons, BorderLayout.EAST);
        
        topContainer.add(header); // Add header to the top container

        // ---
        
        // ===== 2. Search Panel (Below Title) =====
        JPanel searchWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        searchWrapper.setBackground(new Color(245, 247, 250)); 
        searchWrapper.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); // Add vertical spacing

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        searchPanel.setOpaque(false);
        txtSearch = new JTextField(25); // Slightly wider text field
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        btnSearch = new JButton("Search");
        styleButton(btnSearch, new Color(52, 152, 219), new Color(41, 128, 185));
        btnSearch.setPreferredSize(new Dimension(90, 30));
        
        txtSearch.setPreferredSize(new Dimension(250, 30));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));

        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        
        searchWrapper.add(searchPanel); // Center the search bar horizontally
        topContainer.add(searchWrapper); // Add search bar below the header

        // ===== Add Top Container to main panel =====
        add(topContainer, BorderLayout.NORTH);


        // ===== Tabbed Pane (Center Content) =====
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setForeground(primaryDark);
        tabbedPane.setBackground(new Color(245, 247, 250));

        upcomingCoursesPanel = createCourseContainerPanel();
        completedCoursesPanel = createCourseContainerPanel();
        deletedCoursesPanel = createCourseContainerPanel();

        tabbedPane.addTab("🗓️ Upcoming", null, new JScrollPane(upcomingCoursesPanel), "Courses yet to happen");
        tabbedPane.addTab("✅ Completed", null, new JScrollPane(completedCoursesPanel), "Past courses");
        tabbedPane.addTab("🗑️ Deleted/Archived", null, new JScrollPane(deletedCoursesPanel), "Archived or logic-deleted courses");

        // Listener calls refresh() which handles search
        tabbedPane.addChangeListener(e -> {
            refresh();
        });
        
        // Search Button Action
        btnSearch.addActionListener(e -> refresh());
        
        // Allow pressing Enter in search field
        txtSearch.addActionListener(e -> refresh());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createCourseContainerPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1, 10, 10));
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return panel;
    }

    /**
     * Loads courses filtered by category and search term.
     */
    private void loadCourses(String category, String searchTerm) {
        JPanel targetPanel;
        String sql;
        
        Date currentDate = new Date();
        String dateString = new SimpleDateFormat("yyyy-MM-dd").format(currentDate);
        
        // Prepare search filter (applies to course name or ID)
        String filter = "";
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String term = "%" + searchTerm.trim().toLowerCase() + "%";
            filter = " AND (LOWER(course_name) LIKE '" + term + "' OR LOWER(course_id) LIKE '" + term + "')";
        }

        switch (category) {
            case "upcoming":
                targetPanel = upcomingCoursesPanel;
                sql = "SELECT * FROM courses WHERE is_deleted = FALSE AND course_date >= '" + dateString + 
                      "'" + filter + " ORDER BY course_date ASC, course_time ASC";
                break;
            case "completed":
                targetPanel = completedCoursesPanel;
                sql = "SELECT * FROM courses WHERE is_deleted = FALSE AND course_date < '" + dateString + 
                      "'" + filter + " ORDER BY course_date DESC, course_time DESC";
                break;
            case "deleted":
                targetPanel = deletedCoursesPanel;
                sql = "SELECT * FROM courses WHERE is_deleted = TRUE" + filter + " ORDER BY course_date DESC"; 
                break;
            default:
                return;
        }

        targetPanel.removeAll();

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (!rs.isBeforeFirst()) {
                JLabel noCourses = new JLabel("No " + category + " courses found" + (searchTerm.isEmpty() ? "." : " matching '" + searchTerm + "'."));
                noCourses.setFont(new Font("Segoe UI", Font.ITALIC, 16));
                noCourses.setForeground(Color.GRAY);
                JPanel wrapper = new JPanel(new GridBagLayout());
                wrapper.setBackground(targetPanel.getBackground());
                wrapper.add(noCourses);
                targetPanel.add(wrapper);
                
            } else {
                while (rs.next()) {
                    String courseId = rs.getString("course_id");
                    String name = rs.getString("course_name");
                    Date date = rs.getDate("course_date");
                    Time time = rs.getTime("course_time");
                    String mode = rs.getString("mode");
                    String poster = rs.getString("poster"); 

                    JPanel courseCard = createCourseCard(courseId, name, date, time, mode, poster, category);
                    targetPanel.add(courseCard);
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading " + category + " courses: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        targetPanel.revalidate();
        targetPanel.repaint();
        SwingUtilities.invokeLater(() -> {
            JScrollPane scroll = (JScrollPane) targetPanel.getParent().getParent();
            scroll.getVerticalScrollBar().setValue(0);
        });
    }
    
    private JPanel createCourseCard(String courseId, String name, Date date, Time time, String mode, String poster, String category) {
        JPanel courseCard = new JPanel(new BorderLayout(10, 0)); 
        courseCard.setBorder(BorderFactory.createLineBorder(primary, 1));
        courseCard.setBackground(Color.WHITE);
        courseCard.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        courseCard.setPreferredSize(new Dimension(600, 80));

        SimpleDateFormat displayDateFormat = new SimpleDateFormat("MMM dd, yyyy");
        SimpleDateFormat displayTimeFormat = new SimpleDateFormat("hh:mm a");
        
        // --- Leftmost Panel: Poster Thumbnail ---
        JLabel lblPoster = new JLabel();
        lblPoster.setPreferredSize(new Dimension(80, 80));
        lblPoster.setHorizontalAlignment(SwingConstants.CENTER);
        lblPoster.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        if (poster != null && !poster.trim().isEmpty() && !poster.equals("No file chosen")) {
            File posterFile = new File("posters/" + poster);
            if (posterFile.exists()) {
                ImageIcon icon = new ImageIcon(posterFile.getAbsolutePath());
                Image img = icon.getImage();
                Image scaledImg = img.getScaledInstance(70, 70, Image.SCALE_SMOOTH);
                lblPoster.setIcon(new ImageIcon(scaledImg));
            } else {
                lblPoster.setText("No File");
                lblPoster.setFont(new Font("Segoe UI", Font.ITALIC, 10));
            }
        } else {
            lblPoster.setText("No Poster");
            lblPoster.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        }
        courseCard.add(lblPoster, BorderLayout.WEST);

        // --- Center Panel: Info ---
        JPanel infoPanelWrapper = new JPanel(new BorderLayout());
        infoPanelWrapper.setOpaque(false);
        infoPanelWrapper.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));

        JPanel infoPanel = new JPanel(new GridLayout(0, 1));
        infoPanel.setOpaque(false);
        
        JLabel lblName = new JLabel(name);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblName.setForeground(primaryDark);
        
        String dateStr = (date != null) ? displayDateFormat.format(date) : "N/A";
        String timeStr = (time != null) ? displayTimeFormat.format(time) : "N/A";
        
        JLabel lblDateTime = new JLabel("📅 " + dateStr + " | 🕒 " + timeStr);
        lblDateTime.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JLabel lblMode = new JLabel("Mode: " + mode);
        lblMode.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        
        infoPanel.add(lblName);
        infoPanel.add(lblDateTime);
        infoPanel.add(lblMode);
        
        infoPanelWrapper.add(infoPanel, BorderLayout.CENTER);
        courseCard.add(infoPanelWrapper, BorderLayout.CENTER);


        // --- Right Panel: Actions or Arrow ---
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        rightPanel.setOpaque(false);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        
        if (category.equals("upcoming")) {
            JButton btnEdit = new JButton("Edit");
            styleActionButton(btnEdit, new Color(243, 156, 18), new Color(211, 84, 0));
            btnEdit.setPreferredSize(new Dimension(70, 25));
            btnEdit.addActionListener(e -> {
                main.showEditCoursePage(courseId, this); 
            });
            
            JButton btnDelete = new JButton("Delete");
            styleActionButton(btnDelete, new Color(231, 76, 60), new Color(192, 57, 43));
            btnDelete.setPreferredSize(new Dimension(70, 25));
            btnDelete.addActionListener(e -> archiveCourse(courseId));

            rightPanel.add(btnEdit);
            rightPanel.add(btnDelete);
            
            courseCard.setCursor(Cursor.getDefaultCursor());
            
        } else if (category.equals("deleted")) {
            // Restore option
            JButton btnRestore = new JButton("Restore");
            styleActionButton(btnRestore, new Color(46, 204, 113), new Color(39, 174, 96));
            btnRestore.setPreferredSize(new Dimension(80, 25));
            btnRestore.addActionListener(e -> restoreCourse(courseId));
            
            // Delete Permanently option
            JButton btnDeletePerm = new JButton("Delete Permanently");
            styleActionButton(btnDeletePerm, new Color(192, 57, 43), new Color(150, 40, 30)); 
            btnDeletePerm.setPreferredSize(new Dimension(140, 25));
            btnDeletePerm.addActionListener(e -> deletePermanently(courseId)); 
            
            rightPanel.add(btnRestore);
            rightPanel.add(btnDeletePerm);
            
            courseCard.setCursor(Cursor.getDefaultCursor());
            
        } else {
            // Default arrow for completed courses
            JLabel arrow = new JLabel("▶");
            arrow.setFont(new Font("Segoe UI", Font.BOLD, 18));
            arrow.setForeground(primary);
            arrow.setPreferredSize(new Dimension(30, 80));
            arrow.setHorizontalAlignment(SwingConstants.CENTER);
            rightPanel.setLayout(new BorderLayout());
            rightPanel.add(arrow, BorderLayout.CENTER);
        }
        
        courseCard.add(rightPanel, BorderLayout.EAST);
        
        // Mouse listener for highlighting
        courseCard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (! (e.getSource() instanceof JButton) && !category.equals("upcoming") && !category.equals("deleted")) {
                    main.showAdminCourseDetailsPage(courseId);
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                courseCard.setBackground(new Color(230, 240, 250));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                courseCard.setBackground(Color.WHITE);
            }
        });
        
        return courseCard;
    }

    // --- Action Methods ---

    private void archiveCourse(String courseId) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to archive this course? It will move to the Deleted/Archived tab.",
                "Confirm Archive", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pst = conn.prepareStatement("UPDATE courses SET is_deleted = TRUE WHERE course_id = ?")) {
                
                pst.setString(1, courseId);
                if (pst.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(this, "Course successfully archived.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refresh(); 
                    tabbedPane.setSelectedIndex(2);
                } else {
                    JOptionPane.showMessageDialog(this, "Course ID not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error archiving course: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
    
    private void restoreCourse(String courseId) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to restore this course? It will move back to the Upcoming or Completed tab.",
                "Confirm Restore", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pst = conn.prepareStatement("UPDATE courses SET is_deleted = FALSE WHERE course_id = ?")) {
                
                pst.setString(1, courseId);
                if (pst.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(this, "Course successfully restored.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refresh();
                } else {
                    JOptionPane.showMessageDialog(this, "Course ID not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error restoring course: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void deletePermanently(String courseId) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "WARNING: Are you sure you want to PERMANENTLY delete this course? This action cannot be undone and will delete all related student registrations.",
                "Confirm Permanent Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            Connection conn = null;
            try {
                conn = DBConnection.getConnection();
                conn.setAutoCommit(false); // Start transaction

                // 1. Delete dependent records (student registrations) first
                try (PreparedStatement pstDeleteRegs = conn.prepareStatement("DELETE FROM registrations WHERE course_id = ?")) {
                    pstDeleteRegs.setString(1, courseId);
                    pstDeleteRegs.executeUpdate();
                }

                // 2. Delete the course itself
                try (PreparedStatement pstDeleteCourse = conn.prepareStatement("DELETE FROM courses WHERE course_id = ?")) {
                    pstDeleteCourse.setString(1, courseId);
                    int rowsDeleted = pstDeleteCourse.executeUpdate();

                    if (rowsDeleted > 0) {
                        conn.commit(); // Commit transaction on success
                        JOptionPane.showMessageDialog(this, "Course permanently deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        refresh(); // Refresh the Deleted tab
                    } else {
                        conn.rollback();
                        JOptionPane.showMessageDialog(this, "Course ID not found for permanent deletion.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (SQLException ex) {
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException rbEx) {
                        // ignored
                    }
                }
                JOptionPane.showMessageDialog(this, "Database error during permanent deletion: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            } finally {
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
    }
    
    // --- Utility Methods ---

    /**
     * Public method to force a refresh of the currently displayed tab using the current search term.
     */
    public void refresh() {
        int index = tabbedPane.getSelectedIndex();
        String searchTerm = txtSearch.getText();
        
        String category;
        if (index == 0) category = "upcoming";
        else if (index == 1) category = "completed";
        else if (index == 2) category = "deleted";
        else return;
        
        loadCourses(category, searchTerm);
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
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(hover); }
            public void mouseExited(MouseEvent e) { b.setBackground(bg); }
        });
    }

    private void styleActionButton(JButton b, Color bg, Color hover) {
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 10));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(hover); }
            public void mouseExited(MouseEvent e) { b.setBackground(bg); }
        });
    }
}