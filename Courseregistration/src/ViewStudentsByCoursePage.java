import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.MessageFormat;

public class ViewStudentsByCoursePage extends JPanel {
    private MainFrame main;
    
    // UI Components
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

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(bgColor);

        // Title
        JLabel lblTitle = new JLabel("View Students by Course", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(primary);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));
        
        headerPanel.add(lblTitle, BorderLayout.NORTH);

        // --- Search and Selected Course Panel ---
        JPanel searchSelectionPanel = new JPanel(new GridBagLayout());
        searchSelectionPanel.setBackground(bgColor);
        searchSelectionPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. Search Field
        gbc.gridx = 0; gbc.gridy = 0; 
        searchSelectionPanel.add(new JLabel("Search Course (ID/Name):"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.5;
        txtCourseSearch = new JTextField(20);
        txtCourseSearch.setPreferredSize(new Dimension(250, 30));
        searchSelectionPanel.add(txtCourseSearch, gbc);
        
        // 2. Search Button
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0;
        btnSearchCourse = new JButton("Search");
        styleButton(btnSearchCourse, primary, primaryDark, new Dimension(90, 30));
        searchSelectionPanel.add(btnSearchCourse, gbc);
        
        // 3. Status Label (Course ID)
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3; gbc.anchor = GridBagConstraints.WEST;
        lblSelectedCourseId = new JLabel("Selected Course: None");
        lblSelectedCourseId.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSelectedCourseId.setForeground(primaryDark);
        searchSelectionPanel.add(lblSelectedCourseId, gbc);
        
        headerPanel.add(searchSelectionPanel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // --- Table Setup ---
        String[] columns = {"Admission No", "Full Name", "Email", "Phone", "Semester", "Batch", "Department", "Class No"};
        
        // Custom model to make cells NON-EDITABLE
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // All cells are not editable
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

        // --- Button Panel ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        btnPanel.setBackground(bgColor);
        
        JButton btnPrint = new JButton("Print Details");
        styleButton(btnPrint, new Color(46, 204, 113), new Color(39, 174, 96), new Dimension(150, 35));
        
        JButton btnBack = new JButton("Back");
        styleButton(btnBack, primary, primaryDark, new Dimension(150, 35));

        btnPanel.add(btnPrint);
        btnPanel.add(btnBack);
        add(btnPanel, BorderLayout.SOUTH);

        // --- Event Handlers ---
        // 🔑 FIX: Call clearFields() when exiting the page
        btnBack.addActionListener(e -> {
            clearFields(); 
            main.showPage("admin");
        });
        
        btnPrint.addActionListener(e -> printTable());
        
        // Action to trigger course search and selection
        ActionListener searchAction = e -> searchAndSelectCourse(txtCourseSearch.getText());
        btnSearchCourse.addActionListener(searchAction);
        txtCourseSearch.addActionListener(searchAction); 
    }

    /**
     * Clears search field and resets the table view.
     */
    public void clearFields() {
        txtCourseSearch.setText("");
        lblSelectedCourseId.setText("Selected Course: None");
        currentCourseId = null;
        model.setRowCount(0);
    }
    
    // --- Data Loading Logic ---

    /**
     * Searches for a course and, if found uniquely, loads its students.
     */
    private void searchAndSelectCourse(String searchTerm) {
        if (searchTerm.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Course ID or Name to search.", "Search Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            // Search for active courses matching the term
            String sql = "SELECT course_id, course_name FROM courses WHERE is_deleted = FALSE AND (course_id = ? OR course_name LIKE ?)";
            PreparedStatement pst = conn.prepareStatement(sql);
            
            // Try exact ID match first
            pst.setString(1, searchTerm.trim()); 
            // Then generic name/ID match
            String likeTerm = "%" + searchTerm.trim() + "%";
            pst.setString(2, likeTerm); 
            
            ResultSet rs = pst.executeQuery();
            
            // Handle results
            if (!rs.isBeforeFirst()) {
                // No courses found
                lblSelectedCourseId.setText("Selected Course: None");
                currentCourseId = null;
                model.setRowCount(0);
                JOptionPane.showMessageDialog(this, "No active course found matching the search term.", "Course Not Found", JOptionPane.INFORMATION_MESSAGE);
            } else if (rs.next()) {
                // Course found (assumes unique result or takes the first)
                currentCourseId = rs.getString("course_id");
                String courseName = rs.getString("course_name");
                lblSelectedCourseId.setText("Selected Course: " + courseName + " (" + currentCourseId + ")");
                loadStudents(); // Load students for the found course
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error during course search: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Loads students registered for the currently selected course ID.
     */
    private void loadStudents() {
        model.setRowCount(0); // Clear existing table data

        if (currentCourseId == null) {
            return;
        }

        try (Connection conn = DBConnection.getConnection();
             // Join students (s) with course_registrations (cr)
             PreparedStatement pst = conn.prepareStatement(
                 "SELECT s.admission_no, s.full_name, s.email, s.phone, s.semester, s.batch, s.dept, s.class_no " +
                 "FROM students s " +
                 "JOIN course_registrations cr ON s.admission_no = cr.student_admission_no " +
                 "WHERE cr.course_id = ? AND cr.is_cancelled = FALSE " + // Filter for active and specific course
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
            
            // Update the status label with the count
            String currentLabel = lblSelectedCourseId.getText();
            lblSelectedCourseId.setText(currentLabel.replaceAll("\\s*\\([^)]*\\)$", "") + " (" + studentCount + " Students)");
            
            if (studentCount == 0) {
                 JOptionPane.showMessageDialog(this, "No active students are currently registered for this course.", "No Registrations", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading student details. Please verify your table schemas: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // --- Printing Logic ---

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

    // --- Utility Methods ---

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
}