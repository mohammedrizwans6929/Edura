import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AdminPage extends JPanel {
    private MainFrame main;
    private Color primary = new Color(52, 152, 219);
    private Color primaryDark = new Color(41, 128, 185);
    private Color danger = new Color(231, 76, 60); 

    public AdminPage(MainFrame main) {
        this.main = main;

        setLayout(new GridBagLayout());
        setBackground(new Color(245, 247, 250));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

      
        JLabel lblTitle = new JLabel("Admin Dashboard");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(primary);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(lblTitle, gbc);

        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.CENTER;

      
        JButton btnAddCourse = new JButton("Add Course");
        styleButton(btnAddCourse, primary, primaryDark);
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(btnAddCourse, gbc);

        JButton btnManageCourses = new JButton("Manage Courses");
        styleButton(btnManageCourses, primary, primaryDark);
        gbc.gridx = 1;
        gbc.gridy = 1;
        add(btnManageCourses, gbc);

      
        JButton btnViewStudents = new JButton("View Students By Course");
        styleButton(btnViewStudents, primary, primaryDark);
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(btnViewStudents, gbc);
        
        JButton btnAttendance = new JButton("Course Attendance");
        styleButton(btnAttendance, primary, primaryDark);
        gbc.gridx = 1;
        gbc.gridy = 2;
        add(btnAttendance, gbc);
        
  
        JButton btnLogout = new JButton("Logout");
        styleSmallButton(btnLogout, danger, danger.darker()); 
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        add(btnLogout, gbc);

    
        btnAddCourse.addActionListener(e -> main.showPage("addcourse"));
        btnManageCourses.addActionListener(e -> main.showPage("managecourses"));
        btnViewStudents.addActionListener(e -> main.showPage("viewstudents"));
        btnAttendance.addActionListener(e -> main.showPage("courseattendance"));
        
      
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to log out?",
                    "Logout Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
               
                main.showPage("adminlogin"); 
            }
        });
    }

    private void styleButton(JButton b, Color primary, Color primaryDark) {
        b.setBackground(primary);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 16));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setPreferredSize(new Dimension(220, 45));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(primaryDark); }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) { b.setBackground(primary); }
        });
    }

    private void styleSmallButton(JButton b, Color bg, Color hover) {
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setPreferredSize(new Dimension(120, 35));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(hover); }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) { b.setBackground(bg); }
        });
    }

}

