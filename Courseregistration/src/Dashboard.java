import javax.swing.*;
import java.awt.*;
import java.awt.event.*; // Keep for ActionListener and MouseAdapter

public class Dashboard extends JPanel {
    private MainFrame main;
    private String admissionNo;     
    private String fullName;  

    private Color primary = new Color(52, 152, 219);
    private Color primaryDark = new Color(41, 128, 185);

    public Dashboard(MainFrame main, String admissionNo, String fullName) {
        this.main = main;
        this.admissionNo = admissionNo;
        this.fullName = fullName;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // ===== Top Header =====
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(primary);
        header.setPreferredSize(new Dimension(800, 60));

        JLabel title = new JLabel("Welcome, " + fullName);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

        JButton btnLogout = new JButton("Logout");
        styleLogoutButton(btnLogout);
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to log out?",
                    "Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                main.showPage("login");
            }
        });

        header.add(title, BorderLayout.WEST);
        header.add(btnLogout, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ===== Center Navigation Panel (Reduced to 4 Rows) =====
        // 🔴 FIX: Changed GridLayout from 5 to 4 rows
        JPanel centerPanel = new JPanel(new GridLayout(4, 1, 10, 10)); 
        centerPanel.setBorder(BorderFactory.createEmptyBorder(40, 250, 40, 250)); // Adjusted padding
        centerPanel.setBackground(Color.WHITE);

        JButton btnProfile = new JButton(" View Profile");
        JButton btnMyCourses = new JButton(" My Courses");
        JButton btnAvailableCourses = new JButton(" Available Courses");
        JButton btnCertifications = new JButton(" Certifications");
        // JButton btnTransactions = new JButton("💳 Transactions"); 🔴 REMOVED

        // 🔴 FIX: Use the smaller styling method for all nav buttons
        styleSmallNavButton(btnProfile);
        styleSmallNavButton(btnMyCourses);
        styleSmallNavButton(btnAvailableCourses);
        styleSmallNavButton(btnCertifications);
        // styleSmallNavButton(btnTransactions); 🔴 REMOVED

        centerPanel.add(btnProfile);
        centerPanel.add(btnMyCourses);
        centerPanel.add(btnAvailableCourses);
        centerPanel.add(btnCertifications);
        // centerPanel.add(btnTransactions); 🔴 REMOVED

        add(centerPanel, BorderLayout.CENTER);

        // ===== Button Actions =====
        btnProfile.addActionListener(e -> main.showProfilePage(admissionNo));

        btnMyCourses.addActionListener(e -> {
            main.setCurrentStudent(admissionNo);
            main.showMyCoursesPage();
        });

        btnAvailableCourses.addActionListener(e -> {
            main.setCurrentStudent(admissionNo);
            main.showAvailableCoursesPage();
        });

        btnCertifications.addActionListener(e -> {
            main.setCurrentStudent(admissionNo);
            main.showMyCertificatesPage();
        });
        
        // Removed Transactions action listener
    }

    /**
     * 🔴 NEW: Smaller, standard style for the primary navigation buttons.
     */
    private void styleSmallNavButton(JButton btn) {
        btn.setFocusPainted(false);
        // Reduced font size and padding for a smaller button
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14)); 
        btn.setBackground(primary);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15)); // Smaller padding
        btn.setPreferredSize(new Dimension(180, 40)); // Standardized size hint
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(primaryDark);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(primary);
            }
        });
    }

    private void styleLogoutButton(JButton btn) {
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(new Color(231, 76, 60));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(192, 57, 43));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(231, 76, 60));
            }
        });
    }
}