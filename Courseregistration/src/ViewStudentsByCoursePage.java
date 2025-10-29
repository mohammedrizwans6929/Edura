import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.MessageFormat;
import java.util.Vector;

public class ViewStudentsByCoursePage extends JPanel {
    private MainFrame main;
    
    
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtCourseSearch; 
    private JButton btnSearchCourse;
    private JLabel lblSelectedCourseId; 
    
    private String currentCourseId = null; 

    public ViewStudentsByCoursePage(MainFrame main) {
        this.main = main;
        setLayout(new BorderLayout());

        Color bgColor = new Color(245, 247, 250);
        Color primary = new Color(52, 152, 219);
        Color primaryDark = new Color(41, 128, 185);

      
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(bgColor);

       
        JLabel lblTitle = new JLabel("View Students by Course", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(primary);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));
        
        headerPanel.add(lblTitle, BorderLayout.NORTH);

        
        JPanel searchSelectionPanel = new JPanel(new GridBagLayout());
        searchSelectionPanel.setBackground(bgColor);
        searchSelectionPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

       
        gbc.gridx = 0; gbc.gridy = 0; 
        searchSelectionPanel.add(new JLabel("Search Course (ID/Name):"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.5;
        txtCourseSearch = new JTextField(20);
        txtCourseSearch.setPreferredSize(new Dimension(250, 30));
        searchSelectionPanel.add(txtCourseSearch, gbc);
        
      
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0;
        btnSearchCourse = new JButton("Search");
        styleButton(btnSearchCourse, primary, primaryDark, new Dimension(90, 30));
        searchSelectionPanel.add(btnSearchCourse, gbc);
        
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3; gbc.anchor = GridBagConstraints.WEST;
        lblSelectedCourseId = new JLabel("Selected Course: None");
        lblSelectedCourseId.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSelectedCourseId.setForeground(primaryDark);
        searchSelectionPanel.add(lblSelectedCourseId, gbc);
        
        headerPanel.add(searchSelectionPanel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

      
        String[] columns = {"Admission No", "Full Name", "Email", "Phone", "Semester", "Batch", "Department", "Class No"};
        
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };

        table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(900,400));
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        add(scrollPane, BorderLayout.CENTER);

     
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        btnPanel.setBackground(bgColor);
        
        JButton btnPrint = new JButton("Print Details");
        styleButton(btnPrint, new Color(46, 204, 113), new Color(39, 174, 96), new Dimension(150, 35));
        
        JButton btnBack = new JButton("Back");
        styleButton(btnBack, primary, primaryDark, new Dimension(150, 35));

        btnPanel.add(btnPrint);
        btnPanel.add(btnBack);
        add(btnPanel, BorderLayout.SOUTH);

        
        btnBack.addActionListener(e -> {
            clearFields(); 
            main.showPage("admin");
        });
        
        btnPrint.addActionListener(e -> printTable());
        
        
        ActionListener searchAction = e -> searchCoursesForSelection(txtCourseSearch.getText());
        btnSearchCourse.addActionListener(searchAction);
        txtCourseSearch.addActionListener(searchAction); 
    }

   
    public void clearFields() {
        txtCourseSearch.setText("");
        lblSelectedCourseId.setText("Selected Course: None");
        currentCourseId = null;
        model.setRowCount(0);
    }
    
   
    private void searchCoursesForSelection(String searchTerm) {
        if (searchTerm.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Course ID or Name to search.", "Search Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
       
        lblSelectedCourseId.setText("Searching...");
        model.setRowCount(0);
        currentCourseId = null;

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
                lblSelectedCourseId.setText("Selected Course: None");
                JOptionPane.showMessageDialog(this, "No active course found matching the search term.", "Course Not Found", JOptionPane.INFORMATION_MESSAGE);
            } else if (courseOptions.size() == 1) {
               
                selectCourse(courseOptions.firstElement().id, courseOptions.firstElement().name);
            } else {
               
                showCourseSelectionDialog(courseOptions);
            }
        } catch (SQLException ex) {
            lblSelectedCourseId.setText("Selected Course: Error");
            JOptionPane.showMessageDialog(this, "Error during course search: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    private void showCourseSelectionDialog(Vector<CourseOption> courseOptions) {
        
        String[] displayOptions = courseOptions.stream()
            .map(c -> c.id + " - " + c.name)
            .toArray(String[]::new);

        String selectedValue = (String) JOptionPane.showInputDialog(
            this,
            "Multiple courses found. Select one to view students:",
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
            lblSelectedCourseId.setText("Selected Course: None");
        }
    }
    
  
    private void selectCourse(String courseId, String courseName) {
        this.currentCourseId = courseId;
        lblSelectedCourseId.setText("Selected Course: " + courseName + " (" + courseId + ")");
        loadStudents();
    }
    
   
    private void loadStudents() {
        model.setRowCount(0);

        if (currentCourseId == null) {
            return;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(
                 "SELECT s.admission_no, s.full_name, s.email, s.phone, s.semester, s.batch, s.dept, s.class_no " +
                 "FROM students s " +
                 "JOIN course_registrations cr ON s.admission_no = cr.student_admission_no " +
                 "WHERE cr.course_id = ? AND cr.is_cancelled = FALSE " + 
                 "ORDER BY s.full_name")) {

            pst.setString(1, currentCourseId);
            ResultSet rs = pst.executeQuery();
            int studentCount = 0;

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("admission_no"),
                    rs.getString("full_name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("semester"),
                    rs.getString("batch"),
                    rs.getString("dept"),
                    rs.getString("class_no")
                });
                studentCount++;
            }
          
            String currentLabel = lblSelectedCourseId.getText();
            lblSelectedCourseId.setText(currentLabel.replaceAll("\\s*\\([^)]*\\)$", "") + " (" + studentCount + " Students)");
            
            if (studentCount == 0) {
                 JOptionPane.showMessageDialog(this, "No active students are currently registered for this course.", "No Registrations", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading student details: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

   

    private void printTable() {
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "The table is empty. Load students before printing.", "Print Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (currentCourseId == null) {
            JOptionPane.showMessageDialog(this, "No course is currently selected.", "Print Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            MessageFormat header = new MessageFormat("Student Roster for: " + lblSelectedCourseId.getText());
            MessageFormat footer = new MessageFormat("Page {0}");
            
            boolean complete = table.print(JTable.PrintMode.FIT_WIDTH, header, footer);
            
            if (complete) {
                JOptionPane.showMessageDialog(this, "Printing complete.", "Print Status", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Printing cancelled or incomplete.", "Print Status", JOptionPane.WARNING_MESSAGE);
            }
        } catch (java.awt.print.PrinterException e) {
            JOptionPane.showMessageDialog(this, "Error during printing: " + e.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }

  
    private void styleButton(JButton b, Color primary, Color primaryDark, Dimension size){
        b.setBackground(primary);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI",Font.BOLD,14));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setPreferredSize(size);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e){ b.setBackground(primaryDark);}
            public void mouseExited(java.awt.event.MouseEvent e){ b.setBackground(primary);}
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
