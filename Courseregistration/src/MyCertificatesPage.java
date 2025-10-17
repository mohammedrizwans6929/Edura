import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.File; 

public class MyCertificatesPage extends JPanel {
    private MainFrame main;
    private String admissionNo;
    private JPanel listPanel;

    private Color primary = new Color(52, 152, 219);
    private Color primaryDark = new Color(41, 128, 185); // Define dark primary for hover
    private Color success = new Color(39, 174, 96);
    private Color successDark = new Color(27, 128, 77);

    public MyCertificatesPage(MainFrame main, String admissionNo) {
        this.main = main;
        this.admissionNo = admissionNo;
        setLayout(new BorderLayout());
        initUI();
    }

    private void initUI() {
        // ===== Header (Title only) =====
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(primary);
        header.setPreferredSize(new Dimension(800, 60));

        JLabel lblTitle = new JLabel("My Certificates");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

        header.add(lblTitle, BorderLayout.WEST);
        // ❌ REMOVED: btnBack from BorderLayout.EAST of the header
        add(header, BorderLayout.NORTH);

        // ===== List Panel (Center Content) =====
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        listPanel.setBackground(new Color(245, 247, 250));

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        // ===== Footer (New Back Button Location) =====
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15)); // Center flow, add vertical padding
        footer.setBackground(new Color(245, 247, 250));

        JButton btnBack = new JButton("Back to Dashboard");
        
        // 🔑 MODIFICATION 1: Changed button colors to primary blue theme
        styleButton(btnBack, primary, primaryDark, new Dimension(180, 40)); 
        
        btnBack.addActionListener(e -> main.showPage("dashboard"));

        footer.add(btnBack);
        // 🔑 MODIFICATION 2: Added footer panel to BorderLayout.SOUTH
        add(footer, BorderLayout.SOUTH);
    }

    /**
     * Fetches courses eligible for certification (Student has a "Present" attendance record).
     */
    public void loadCertificates() {
        listPanel.removeAll();
        boolean found = false;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(
                     "SELECT c.course_id, c.course_name, MAX(a.date_recorded) AS completion_date " +
                     "FROM courses c " +
                     "JOIN registrations r ON c.course_id = r.course_id " +
                     "JOIN attendance a ON c.course_id = a.course_id AND r.admission_no = a.admission_no " + 
                     "WHERE r.admission_no = ? AND c.is_deleted = FALSE " +
                     "AND a.status = 'Present' " + 
                     "GROUP BY c.course_id, c.course_name " + 
                     "ORDER BY completion_date DESC")) { 

            pst.setString(1, admissionNo);
            ResultSet rs = pst.executeQuery();
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");

            while (rs.next()) {
                found = true;
                String courseId = rs.getString("course_id");
                String courseName = rs.getString("course_name");
                
                // Use the latest attendance date recorded as the basis for the date shown
                Date completionDate = rs.getDate("completion_date"); 

                JPanel certificateCard = createCertificateCard(courseId, courseName, dateFormat.format(completionDate));
                listPanel.add(certificateCard);
                listPanel.add(Box.createVerticalStrut(10));
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading certificates: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        if (!found) {
            JLabel lblMessage = new JLabel("No courses found with recorded attendance eligible for certification.", SwingConstants.CENTER);
            lblMessage.setFont(new Font("Segoe UI", Font.ITALIC, 16));
            lblMessage.setForeground(Color.GRAY);
            listPanel.add(Box.createVerticalGlue());
            listPanel.add(lblMessage);
            listPanel.add(Box.createVerticalGlue());
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    /**
     * Creates a card for an individual certificate with a download button.
     */
    private JPanel createCertificateCard(String courseId, String courseName, String dateStr) {
        JPanel card = new JPanel(new BorderLayout(20, 0));
        card.setMaximumSize(new Dimension(750, 80));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        card.setBackground(Color.WHITE);

        // --- Info Panel ---
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setOpaque(false);
        
        JLabel lblName = new JLabel("Certificate for: " + courseName);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        // Show the latest attendance date as the recorded completion date
        JLabel lblDate = new JLabel("Status Recorded on: " + dateStr); 
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDate.setForeground(Color.GRAY);
        
        infoPanel.add(lblName);
        infoPanel.add(lblDate);
        card.add(infoPanel, BorderLayout.CENTER);

        // --- Action Panel ---
        JButton btnDownload = new JButton("⬇ Download PDF");
        styleButton(btnDownload, success, successDark, new Dimension(160, 40));
        
        btnDownload.addActionListener(e -> downloadCertificate(courseId, courseName));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setOpaque(false);
        actionPanel.add(btnDownload);
        card.add(actionPanel, BorderLayout.EAST);

        return card;
    }

    /**
     * Conceptual method for generating and downloading the certificate.
     */
    private void downloadCertificate(String courseId, String courseName) {
        // String fileName = "Certificate_" + courseId + "_" + admissionNo + ".pdf";
        
        File downloadDir = new File("certificates");
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }

        JOptionPane.showMessageDialog(this,
            "Certificate generation successful for:\n" + courseName + 
            "\n\n(A PDF file would normally be downloaded and saved to the 'certificates/' directory.)",
            "Download Complete", JOptionPane.INFORMATION_MESSAGE);
    }

    private void styleButton(JButton b, Color bg, Color hover, Dimension size) {
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
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
}