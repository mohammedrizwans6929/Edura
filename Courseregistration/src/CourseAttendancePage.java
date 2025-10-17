import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.TimeUnit; 

public class CourseAttendancePage extends JPanel {
    private MainFrame main;
    private JTextField txtCourseSearch;
    private JTable table;
    private DefaultTableModel model;
    private JLabel lblStatus;
    private Date attendanceDate;
    private String currentCourseId = null;

    private Color primary = new Color(52, 152, 219);
    private Color primaryDark = new Color(41, 128, 185);
    private Color success = new Color(46, 204, 113);
    private Color error = new Color(231, 76, 60);

    public CourseAttendancePage(MainFrame main) {
        this.main = main;
        this.attendanceDate = new Date(); // Default attendance date is today
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        initUI();
    }

    private void initUI() {
        // ===== Top Panel (Header & Search) =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(primary);
        topPanel.setPreferredSize(new Dimension(800, 110));

        // 1. Title and Date
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 10));
        titlePanel.setBackground(primary);
        JLabel lblTitle = new JLabel("Course Attendance Recording");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        titlePanel.add(lblTitle);
        
        // Display today's date for attendance
        JLabel lblDate = new JLabel("Date: " + new SimpleDateFormat("yyyy-MM-dd").format(attendanceDate));
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblDate.setForeground(Color.WHITE);
        titlePanel.add(lblDate);
        
        topPanel.add(titlePanel, BorderLayout.NORTH);

        // 2. Search Bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        searchPanel.setBackground(primary);
        
        txtCourseSearch = new JTextField(25);
        txtCourseSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtCourseSearch.setPreferredSize(new Dimension(250, 30));
        
        JButton btnSearch = new JButton("Load Course");
        styleSearchButton(btnSearch, new Color(241, 196, 15), new Color(243, 156, 18));
        btnSearch.addActionListener(e -> searchAndLoadCourse());

        searchPanel.add(new JLabel("Course ID/Name: ")).setForeground(Color.WHITE);
        searchPanel.add(txtCourseSearch);
        searchPanel.add(btnSearch);
        
        topPanel.add(searchPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // ===== Center Panel (Table) =====
        String[] columns = {"Admission No", "Full Name", "Status"};
        
        // Custom model to handle the JComboBox in the "Status" column
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2; // Only the Status column is editable
            }
        };
        
        table = new JTable(model);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        // Set up JComboBox editor for the Status column
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Present", "Absent"});
        table.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(statusCombo));
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        add(scrollPane, BorderLayout.CENTER);

        // ===== Bottom Panel (Save and Back) =====
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        bottomPanel.setBackground(getBackground());
        
        lblStatus = new JLabel("Please load a course to record attendance.");
        lblStatus.setForeground(primaryDark);
        
        JButton btnSave = new JButton("Save Attendance");
        styleButton(btnSave, success, new Color(39, 174, 96), new Dimension(160, 40));
        btnSave.addActionListener(e -> saveAttendance());
        
        JButton btnBack = new JButton("Back");
        styleButton(btnBack, error, new Color(192, 57, 43), new Dimension(100, 40));
        
        // 🔑 FIX: Call clearFields() when exiting the page
        btnBack.addActionListener(e -> {
            clearFields();
            main.showPage("admin");
        });

        bottomPanel.add(lblStatus);
        bottomPanel.add(btnSave);
        bottomPanel.add(btnBack);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Clears search field and resets the table and status.
     */
    public void clearFields() {
        txtCourseSearch.setText("");
        model.setRowCount(0);
        currentCourseId = null;
        lblStatus.setText("Please load a course to record attendance.");
    }
    
    // --- Data Handlers ---

    private void searchAndLoadCourse() {
        String searchTerm = txtCourseSearch.getText().trim();
        if (searchTerm.isEmpty()) {
            lblStatus.setText("Enter a Course ID or Name.");
            return;
        }
        
        model.setRowCount(0); // Clear table
        currentCourseId = null;
        
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT course_id, course_name FROM courses WHERE is_deleted = FALSE AND (course_id = ? OR course_name LIKE ?)";
            PreparedStatement pst = conn.prepareStatement(sql);
            
            pst.setString(1, searchTerm);
            pst.setString(2, "%" + searchTerm + "%");
            
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                currentCourseId = rs.getString("course_id");
                String courseName = rs.getString("course_name");
                lblStatus.setText("Course loaded: " + courseName + " (" + currentCourseId + ")");
                loadStudentsForAttendance();
            } else {
                lblStatus.setText("Error: Course not found or archived.");
            }
        } catch (SQLException ex) {
            lblStatus.setText("Database error during course load.");
            ex.printStackTrace();
        }
    }
    
    private void loadStudentsForAttendance() {
        if (currentCourseId == null) return;
        
        model.setRowCount(0); // Clear table data

        try (Connection conn = DBConnection.getConnection();
              // Join students (s) with course_registrations (cr) and filter by is_cancelled = FALSE
              PreparedStatement pst = conn.prepareStatement(
                  "SELECT s.admission_no, s.full_name FROM students s " +
                  "JOIN course_registrations cr ON s.admission_no = cr.student_admission_no " +
                  "WHERE cr.course_id = ? AND cr.is_cancelled = FALSE " + 
                  "ORDER BY s.full_name")) {

            pst.setString(1, currentCourseId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                String admissionNo = rs.getString("admission_no");
                String fullName = rs.getString("full_name");
                
                // Check existing attendance for today
                String existingStatus = getExistingAttendance(admissionNo, currentCourseId);
                
                // Add row: [Admission No, Full Name, Status (Present/Absent)]
                // Default is "Present" if no record exists
                model.addRow(new Object[]{
                    admissionNo,
                    fullName,
                    existingStatus.isEmpty() ? "Present" : existingStatus
                });
            }
            lblStatus.setText("Loaded " + model.getRowCount() + " students. Ready to save attendance.");

        } catch (SQLException ex) {
            lblStatus.setText("Error loading student list. Check DB connection/schema.");
            ex.printStackTrace();
        }
    }
    
    /**
     * Checks the database for existing attendance record for today.
     */
    private String getExistingAttendance(String admissionNo, String courseId) {
        String status = "";
        String dateString = new SimpleDateFormat("yyyy-MM-dd").format(attendanceDate);
        
        try (Connection conn = DBConnection.getConnection();
              PreparedStatement pst = conn.prepareStatement(
                  "SELECT status FROM attendance WHERE course_id = ? AND admission_no = ? AND date_recorded = ?")) {
            
            pst.setString(1, courseId);
            pst.setString(2, admissionNo);
            pst.setString(3, dateString);
            
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                status = rs.getString("status");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return status;
    }

    private void saveAttendance() {
        if (currentCourseId == null || model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Please load a course with students before saving.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String dateString = new SimpleDateFormat("yyyy-MM-dd").format(attendanceDate);
        Connection conn = null;
        int savedCount = 0;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            
            // SQL for updating/inserting (UPSERT logic: REPLACE INTO or INSERT...ON DUPLICATE KEY UPDATE)
            String sql = "REPLACE INTO attendance (course_id, admission_no, date_recorded, status) VALUES (?, ?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(sql);

            for (int i = 0; i < model.getRowCount(); i++) {
                String admissionNo = (String) model.getValueAt(i, 0);
                String status = (String) model.getValueAt(i, 2);

                pst.setString(1, currentCourseId);
                pst.setString(2, admissionNo);
                pst.setString(3, dateString);
                pst.setString(4, status);
                
                pst.addBatch();
            }
            
            int[] results = pst.executeBatch();
            for (int result : results) {
                if (result >= 0) savedCount++;
            }

            conn.commit();
            lblStatus.setText("Successfully saved attendance for " + savedCount + " students.");
            JOptionPane.showMessageDialog(this, "Attendance successfully saved/updated!", "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException ex) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rbEx) {
                    // ignored
                }
            }
            lblStatus.setText("Failed to save attendance. Database error.");
            JOptionPane.showMessageDialog(this, "Failed to save attendance: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
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
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(hover); }
            public void mouseExited(MouseEvent e) { b.setBackground(bg); }
        });
    }
    
    private void styleSearchButton(JButton b, Color bg, Color hover) {
        // Special style for the search button
        b.setBackground(bg);
        b.setForeground(Color.BLACK);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setPreferredSize(new Dimension(120, 30));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(hover); }
            public void mouseExited(MouseEvent e) { b.setBackground(bg); }
        });
    }
}