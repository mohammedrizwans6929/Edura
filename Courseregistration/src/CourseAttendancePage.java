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
    private String currentCourseName = null;

    private Color primary = new Color(52, 152, 219);
    private Color primaryDark = new Color(41, 128, 185);
    private Color success = new Color(46, 204, 113);
    private Color error = new Color(231, 76, 60);

    public CourseAttendancePage(MainFrame main) {
        this.main = main;
        this.attendanceDate = new Date(); 
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        initUI();
    }

    private void initUI() {
       
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(primary);
        topPanel.setPreferredSize(new Dimension(800, 110));

       
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 10));
        titlePanel.setBackground(primary);
        JLabel lblTitle = new JLabel("Course Attendance Recording");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        titlePanel.add(lblTitle);
        
        JLabel lblDate = new JLabel("Date: " + new SimpleDateFormat("yyyy-MM-dd").format(attendanceDate));
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblDate.setForeground(Color.WHITE);
        titlePanel.add(lblDate);
        
        topPanel.add(titlePanel, BorderLayout.NORTH);

        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        searchPanel.setBackground(primary);
        
        txtCourseSearch = new JTextField(25);
        txtCourseSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtCourseSearch.setPreferredSize(new Dimension(250, 30));
        
        JButton btnSearch = new JButton("Load Course");
        styleSearchButton(btnSearch, new Color(241, 196, 15), new Color(243, 156, 18));
        btnSearch.addActionListener(e -> searchCoursesForSelection(txtCourseSearch.getText())); // 🎯 MODIFIED ACTION

        searchPanel.add(new JLabel("Course ID/Name: ")).setForeground(Color.WHITE);
        searchPanel.add(txtCourseSearch);
        searchPanel.add(btnSearch);
        
        topPanel.add(searchPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

      
        String[] columns = {"Admission No", "Full Name", "Class No", "Status"};
        
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
               
                return column == 3; 
            }
        };
        
        table = new JTable(model);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        
       
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Present", "Absent"});
        table.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(statusCombo));
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        add(scrollPane, BorderLayout.CENTER);

       
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        bottomPanel.setBackground(getBackground());
        
        lblStatus = new JLabel("Please load a course to record attendance.");
        lblStatus.setForeground(primaryDark);
        
        JButton btnSave = new JButton("Save Attendance");
        styleButton(btnSave, success, success.darker(), new Dimension(160, 40));
        btnSave.addActionListener(e -> saveAttendance());
        
        JButton btnFinalize = new JButton("Finalize Results 🎓");
        styleButton(btnFinalize, primary, primaryDark, new Dimension(160, 40));
        btnFinalize.addActionListener(e -> finalizeResults());
        
        JButton btnBack = new JButton("Back");
        styleButton(btnBack, error, error.darker(), new Dimension(100, 40));
        
        btnBack.addActionListener(e -> {
            clearFields();
            main.showPage("admin");
        });

        bottomPanel.add(lblStatus);
        bottomPanel.add(btnSave);
        bottomPanel.add(btnFinalize); 
        bottomPanel.add(btnBack);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
   
    public void clearFields() {
        txtCourseSearch.setText("");
        model.setRowCount(0);
        currentCourseId = null;
        currentCourseName = null;
        lblStatus.setText("Please load a course to record attendance.");
    }

   

    private void searchCoursesForSelection(String searchTerm) {
        if (searchTerm.trim().isEmpty()) {
            lblStatus.setText("Enter a Course ID or Name.");
            return;
        }
        
        model.setRowCount(0);
        currentCourseId = null;
        lblStatus.setText("Searching for '" + searchTerm + "'...");

        try (Connection conn = DBConnection.getConnection()) {
           
            String sql = "SELECT course_id, course_name FROM courses WHERE is_deleted = FALSE AND (LOWER(course_id) LIKE ? OR LOWER(course_name) LIKE ?)";
            PreparedStatement pst = conn.prepareStatement(sql);
            
            String likeTerm = "%" + searchTerm.trim().toLowerCase() + "%";
            pst.setString(1, likeTerm); 
            pst.setString(2, likeTerm); 
            
            ResultSet rs = pst.executeQuery();
            
            Vector<CourseOption> courseOptions = new Vector<>();
            while (rs.next()) {
                courseOptions.add(new CourseOption(rs.getString("course_id"), rs.getString("course_name")));
            }

            if (courseOptions.isEmpty()) {
                lblStatus.setText("Error: Course not found or archived.");
                JOptionPane.showMessageDialog(this, "No active course found matching the search term.", "Course Not Found", JOptionPane.INFORMATION_MESSAGE);
            } else if (courseOptions.size() == 1) {
                
                selectCourse(courseOptions.firstElement().id, courseOptions.firstElement().name);
            } else {
              
                showCourseSelectionDialog(courseOptions);
            }
        } catch (SQLException ex) {
            lblStatus.setText("Database error during course search.");
            ex.printStackTrace();
        }
    }
    
    private void showCourseSelectionDialog(Vector<CourseOption> courseOptions) {
        String[] displayOptions = courseOptions.stream()
            .map(c -> c.id + " - " + c.name)
            .toArray(String[]::new);

        String selectedValue = (String) JOptionPane.showInputDialog(
            this,
            "Multiple courses found. Select one to load attendance:",
            "Course Selection",
            JOptionPane.QUESTION_MESSAGE,
            null,
            displayOptions,
            displayOptions[0]
        );

        if (selectedValue != null) {
            String selectedId = selectedValue.substring(0, selectedValue.indexOf(" - "));
            CourseOption selectedCourse = courseOptions.stream()
                .filter(c -> c.id.equals(selectedId))
                .findFirst()
                .orElse(null);

            if (selectedCourse != null) {
                selectCourse(selectedCourse.id, selectedCourse.name);
            }
        } else {
            lblStatus.setText("Course loading cancelled.");
        }
    }

    private void selectCourse(String courseId, String courseName) {
        this.currentCourseId = courseId;
        this.currentCourseName = courseName;
        lblStatus.setText("Course loaded: " + currentCourseName + " (" + currentCourseId + ")");
        loadStudentsForAttendance();
    }
    
  
    
    private void loadStudentsForAttendance() {
        if (currentCourseId == null) return;
        
        model.setRowCount(0);

        try (Connection conn = DBConnection.getConnection();
            
             PreparedStatement pst = conn.prepareStatement(
                 "SELECT s.admission_no, s.full_name, s.class_no FROM students s " +
                 "JOIN course_registrations cr ON s.admission_no = cr.student_admission_no " +
                 "WHERE cr.course_id = ? AND cr.is_cancelled = FALSE " +
                 "ORDER BY s.full_name")) {

            pst.setString(1, currentCourseId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                String admissionNo = rs.getString("admission_no");
                String fullName = rs.getString("full_name");
                String classNo = rs.getString("class_no"); 
                
                String existingStatus = getExistingAttendance(admissionNo, currentCourseId);
                
              
                model.addRow(new Object[]{
                    admissionNo,
                    fullName,
                    classNo, 
                    existingStatus.isEmpty() ? "Present" : existingStatus
                });
            }
            lblStatus.setText("Loaded " + model.getRowCount() + " students for attendance.");

        } catch (SQLException ex) {
            lblStatus.setText("Error loading student list. Check DB connection/schema.");
            ex.printStackTrace();
        }
    }
    
  
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
            
       
            String sql = "REPLACE INTO attendance (course_id, admission_no, date_recorded, status) VALUES (?, ?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(sql);

            for (int i = 0; i < model.getRowCount(); i++) {
                String admissionNo = (String) model.getValueAt(i, 0);
                String status = (String) model.getValueAt(i, 3);

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
                   
                }
            }
        }
    }
    
   
    private void finalizeResults() {
        if (currentCourseId == null) {
            JOptionPane.showMessageDialog(this, "Please load a course first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "This will finalize all student outcomes for course " + currentCourseId + " based on attendance. This makes results permanent and available for certification. Continue?",
                "Confirm Finalization", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        Connection conn = null;
        int finalizedCount = 0;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            
            
            String selectStudentsSql = 
                "SELECT DISTINCT admission_no FROM attendance " +
                "WHERE course_id = ? AND status = 'Present'";
            
          
            String upsertSql = 
                "REPLACE INTO course_results (admission_no, course_id, status, completion_date) " +
                "VALUES (?, ?, 'Completed', CURRENT_DATE())"; 

            PreparedStatement selectPst = conn.prepareStatement(selectStudentsSql);
            PreparedStatement upsertPst = conn.prepareStatement(upsertSql);
            
            selectPst.setString(1, currentCourseId);
            
            try (ResultSet rs = selectPst.executeQuery()) {
                while (rs.next()) {
                    String admissionNo = rs.getString("admission_no");
                    
                    upsertPst.setString(1, admissionNo);
                    upsertPst.setString(2, currentCourseId);
                    upsertPst.addBatch();
                    finalizedCount++;
                }
            }
            
            if (finalizedCount > 0) {
                upsertPst.executeBatch();
                conn.commit();
                lblStatus.setText("Results finalized: " + finalizedCount + " students completed.");
                JOptionPane.showMessageDialog(this, finalizedCount + " student results finalized to 'Completed' for certification!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                conn.rollback();
                lblStatus.setText("Results finalized: 0 students completed (no 'Present' records found).");
                JOptionPane.showMessageDialog(this, "No students found with 'Present' attendance to finalize.", "Warning", JOptionPane.WARNING_MESSAGE);
            }

        } catch (SQLException ex) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rbEx) {
                   
                }
            }
            lblStatus.setText("Database error during finalization.");
            JOptionPane.showMessageDialog(this, "Database error during finalization: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                   
                }
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
            public void mouseEntered(MouseEvent e) { b.setBackground(hover); }
            public void mouseExited(MouseEvent e) { b.setBackground(bg); }
        });
    }
    
    private void styleSearchButton(JButton b, Color bg, Color hover) {
   
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
    
  
    private static class CourseOption {
        String id;
        String name;

        public CourseOption(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

}
