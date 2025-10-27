import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Dashboard extends JPanel {
    private MainFrame main;
    private String admissionNo;
    private String fullName;

    private Color primary = new Color(52, 152, 219);
    private Color primaryDark = new Color(41, 128, 185);
    private Color danger = new Color(231, 76, 60);

    public Dashboard(MainFrame main, String admissionNo, String fullName) {
        this.main = main;
        this.admissionNo = admissionNo;
        this.fullName = fullName;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250)); // Light background for contrast

        // ===== Top Header (Unchanged for welcome message) =====
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(primary);
        header.setPreferredSize(new Dimension(800, 60));

        JLabel title = new JLabel("Welcome, " + fullName);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

        JButton btnLogout = new JButton("Logout");
        styleLogoutButton(btnLogout, danger, danger.darker()); // Use red styling
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

        // ===== Center Navigation Panel (Admin Page Style) =====
        JPanel centerContainer = new JPanel(new GridBagLayout());
        centerContainer.setBackground(new Color(245, 247, 250));
        centerContainer.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 1
        JButton btnProfile = new JButton(" View Profile");
        styleLargeNavButton(btnProfile, primary, primaryDark);
        gbc.gridx = 0;
        gbc.gridy = 0;
        centerContainer.add(btnProfile, gbc);

        JButton btnMyCourses = new JButton(" My Courses");
        styleLargeNavButton(btnMyCourses, primary, primaryDark);
        gbc.gridx = 1;
        gbc.gridy = 0;
        centerContainer.add(btnMyCourses, gbc);

        // Row 2
        JButton btnAvailableCourses = new JButton(" Available Courses");
        styleLargeNavButton(btnAvailableCourses, primary, primaryDark);
        gbc.gridx = 0;
        gbc.gridy = 1;
        centerContainer.add(btnAvailableCourses, gbc);

        JButton btnCertifications = new JButton(" Certifications");
        styleLargeNavButton(btnCertifications, primary, primaryDark);
        gbc.gridx = 1;
        gbc.gridy = 1;
        centerContainer.add(btnCertifications, gbc);

        add(centerContainer, BorderLayout.CENTER);

        // ===== Button Actions (Unchanged) =====
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
    }

    /**
     * Replaces styleSmallNavButton with a larger style, similar to AdminPage.styleButton.
     */
    private void styleLargeNavButton(JButton b, Color primary, Color primaryDark) {
        b.setBackground(primary);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 16));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setPreferredSize(new Dimension(220, 45)); // Large size for dashboard buttons
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(primaryDark); }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) { b.setBackground(primary); }
        });
    }

    /**
     * Updated Logout button style to be consistent.
     */
    private void styleLogoutButton(JButton b, Color bg, Color hover) {
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(hover); }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) { b.setBackground(bg); }
        });
    }

}
