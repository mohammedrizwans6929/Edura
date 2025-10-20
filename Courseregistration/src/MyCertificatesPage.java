import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern; 
import java.awt.Desktop; 

// Apache PDFBox Imports (Requires Library in Classpath!)
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;

public class MyCertificatesPage extends JPanel {
    private MainFrame main;
    private String admissionNo;
    private JPanel listPanel;

    private final Color primary = new Color(52, 152, 219);
    private final Color primaryDark = new Color(41, 128, 185);
    private final Color success = new Color(39, 174, 96);
    private final Color successDark = new Color(27, 128, 77);

    public MyCertificatesPage(MainFrame main, String admissionNo) {
        this.main = main;
        this.admissionNo = admissionNo;
        setLayout(new BorderLayout());
        initUI();
    }

    private void initUI() {
        // ===== Header =====
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(primary);
        header.setPreferredSize(new Dimension(800, 60));

        JLabel lblTitle = new JLabel("My Certificates");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

        header.add(lblTitle, BorderLayout.WEST);
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

        // ===== Footer (Back Button) =====
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15));
        footer.setBackground(new Color(245, 247, 250));

        JButton btnBack = new JButton("Back to Dashboard");
        styleButton(btnBack, primary, primaryDark, new Dimension(180, 40));
        btnBack.addActionListener(e -> main.showPage("dashboard"));

        footer.add(btnBack);
        add(footer, BorderLayout.SOUTH);
    }

    /**
     * Fetches courses eligible for certification by checking the 'course_results' table.
     */
    public void loadCertificates() {
        listPanel.removeAll();
        boolean found = false;

        try (Connection conn = DBConnection.getConnection(); // Assumes DBConnection is available
             PreparedStatement pst = conn.prepareStatement(
                     // SQL: Joins with course_results to find 'Passed'/'Completed' courses
                     "SELECT c.course_id, c.course_name, r.completion_date " +
                             "FROM courses c " +
                             "JOIN course_results r ON c.course_id = r.course_id " +
                             "WHERE r.admission_no = ? AND c.is_deleted = FALSE " +
                             "AND r.status IN ('Passed', 'Completed') " +
                             "ORDER BY r.completion_date DESC")) {

            pst.setString(1, admissionNo);
            ResultSet rs = pst.executeQuery();
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");

            while (rs.next()) {
                found = true;
                String courseId = rs.getString("course_id");
                String courseName = rs.getString("course_name");
                
                // Get completion date directly from results table
                Date completionDate = rs.getDate("completion_date"); 

                JPanel certificateCard = createCertificateCard(courseId, courseName, dateFormat.format(completionDate));
                listPanel.add(certificateCard);
                listPanel.add(Box.createVerticalStrut(10));
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading certificates: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        if (!found) {
            JLabel lblMessage = new JLabel("No courses found that are officially marked as Passed/Completed for certification.", SwingConstants.CENTER);
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

        JLabel lblDate = new JLabel("Completed on: " + dateStr);
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
     * Generates and downloads the certificate as a PDF using Apache PDFBox.
     */
    private void downloadCertificate(String courseId, String courseName) {
        String studentFullName = "";
        String completionDateStr = "";

        // 1. Fetch Student Full Name and Completion Date from students and course_results
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(
                 "SELECT s.full_name, r.completion_date " +
                 "FROM students s " +
                 "JOIN course_results r ON s.admission_no = r.admission_no " +
                 "WHERE s.admission_no = ? AND r.course_id = ? AND r.status IN ('Passed', 'Completed')")) {

            pst.setString(1, admissionNo);
            pst.setString(2, courseId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                studentFullName = rs.getString("full_name");
                Date completionDate = rs.getDate("completion_date");
                completionDateStr = new SimpleDateFormat("MMMM dd, yyyy").format(completionDate);
            } else {
                JOptionPane.showMessageDialog(this, "Course status not found in results table.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error during certificate generation: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. Define File Path and Create Directory 
        
        // Dynamic Path Construction 
        String dirPath = "certificates" + File.separator + 
                         "Certificate_" + courseId + File.separator + 
                         admissionNo + File.separator + 
                         courseName.toLowerCase().replaceAll(Pattern.quote(" "), "_").replaceAll("[^a-z0-9_]", ""); // Clean folder name
        
        // Create a unique filename based on student name and course ID.
        String cleanedName = studentFullName.replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll("\\s+", "_");
        String uniqueFileName = cleanedName + "_Cert_" + courseId + ".pdf";
        
        File outputDir = new File(dirPath);
        File outputFile = new File(outputDir, uniqueFileName); 

        // Create the entire directory structure if it does not exist
        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                JOptionPane.showMessageDialog(this, "Failed to create download directory: " + outputDir.getAbsolutePath(), "File Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        // 3. PDF Generation using Apache PDFBox
        try (PDDocument document = new PDDocument()) {
            
            // Landscape Mode (Fix for PDFBox 3.x)
            PDRectangle landscapeRect = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
            PDPage page = new PDPage(landscapeRect);
            document.addPage(page);

            float width = page.getMediaBox().getWidth(); 
            float height = page.getMediaBox().getHeight(); 

            // Calculate center points for the new landscape layout
            float centerX = width / 2f;
            float marginX = 50f;
            float marginY = 50f;
            float borderInset = 20f; 

            // Set up Fonts
            PDType1Font FONT_TITLE = new PDType1Font(FontName.HELVETICA_BOLD);
            PDType1Font FONT_HEADER = new PDType1Font(FontName.HELVETICA_BOLD_OBLIQUE);
            PDType1Font FONT_BODY = new PDType1Font(FontName.HELVETICA);
            PDType1Font FONT_NAME = new PDType1Font(FontName.HELVETICA_BOLD_OBLIQUE);
            
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                
                // ----------------------------------------------------
                // 3.1 Template Layout
                // ----------------------------------------------------

                // Outer Border (Black)
                contentStream.setLineWidth(5);
                contentStream.setStrokingColor(Color.BLACK);
                contentStream.addRect(marginX, marginY, width - 2 * marginX, height - 2 * marginY);
                contentStream.stroke();

                // Inner Border/Text Box (Gold color approximation)
                contentStream.setLineWidth(1);
                contentStream.setStrokingColor(new Color(205, 175, 80)); 
                contentStream.addRect(marginX + borderInset, marginY + borderInset, 
                                      width - 2 * (marginX + borderInset), height - 2 * (marginY + borderInset));
                contentStream.stroke();
                
                // Text coordinates 
                float currentY = height - 100f; 

                // Main Title: CERTIFICATE OF PARTICIPATION
                TextAlignment.drawCenteredText(contentStream, FONT_TITLE, 30f, "CERTIFICATE OF PARTICIPATION", currentY, centerX);
                currentY -= 50f;
                
                // Award Line
                TextAlignment.drawCenteredText(contentStream, FONT_BODY, 18f, "THIS CERTIFICATE IS AWARDED TO", currentY, centerX);
                currentY -= 50f;

                // Student Name
                TextAlignment.drawCenteredText(contentStream, FONT_NAME, 45f, studentFullName.toUpperCase(), currentY, centerX);
                currentY -= 60f;
                
                // Participation Text
                String participationText1 = "for actively participating in the event,";
                String participationText2 = "\"" + courseName.toUpperCase() + "\", organized by";
                // --- EDITED TEXT: REPLACED HARDCODED ORGANIZATION NAME WITH PLACEHOLDER ---
                String participationText3 = "ORGANIZING BODY"; 
                // --------------------------------------------------------------------------

                TextAlignment.drawCenteredText(contentStream, FONT_BODY, 14f, participationText1, currentY, centerX);
                currentY -= 20f;
                TextAlignment.drawCenteredText(contentStream, FONT_BODY, 14f, participationText2, currentY, centerX);
                currentY -= 20f;
                TextAlignment.drawCenteredText(contentStream, FONT_BODY, 14f, participationText3, currentY, centerX);
                currentY -= 20f;
                
                // Date
                String dateLine = "on " + completionDateStr + ".";
                TextAlignment.drawCenteredText(contentStream, FONT_BODY, 14f, dateLine, currentY, centerX);
                
                // Signature Block (Bottom)
                float signatureY = 100f;
                float signatureLineLength = 200f;
                float leftSigX = centerX - 200f;
                float rightSigX = centerX + 200f;

                // Signature 1 Line
                contentStream.setLineWidth(1);
                contentStream.setStrokingColor(Color.BLACK);
                contentStream.moveTo(leftSigX - signatureLineLength/2, signatureY); 
                contentStream.lineTo(leftSigX + signatureLineLength/2, signatureY); 
                contentStream.stroke();
                // --- EDITED TEXT: REPLACED HARDCODED NAMES WITH PLACEHOLDERS ---
                TextAlignment.drawCenteredText(contentStream, FONT_BODY, 12f, "TITLE 1", signatureY - 15f, leftSigX);
                TextAlignment.drawCenteredText(contentStream, FONT_BODY, 12f, "AUTHORIZED SIGNATORY 1", signatureY - 30f, leftSigX);


                // Signature 2 Line
                contentStream.moveTo(rightSigX - signatureLineLength/2, signatureY); 
                contentStream.lineTo(rightSigX + signatureLineLength/2, signatureY); 
                contentStream.stroke();
                TextAlignment.drawCenteredText(contentStream, FONT_BODY, 12f, "TITLE 2", signatureY - 15f, rightSigX);
                TextAlignment.drawCenteredText(contentStream, FONT_BODY, 12f, "AUTHORIZED SIGNATORY 2", signatureY - 30f, rightSigX);
                // --------------------------------------------------------------------------

            }

            document.save(outputFile);
            
            // Automatic File Opening 
            boolean openedSuccessfully = false;
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                try {
                    Desktop.getDesktop().open(outputFile);
                    openedSuccessfully = true;
                } catch (IOException ex) {
                    System.err.println("Could not open file automatically: " + ex.getMessage());
                }
            }
            
            // Show a simple confirmation message
            String successMessage = openedSuccessfully 
                ? "✅ Certificate generated successfully and opened in your default viewer."
                : "✅ Certificate generated successfully.\n\nFile location: " + outputFile.getAbsolutePath();
            
            JOptionPane.showMessageDialog(this, successMessage, "Download Complete", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to generate PDF file: " + e.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // -------------------------------------------------------------
    // UTILITY CLASS FOR TEXT ALIGNMENT IN PDFBox
    // -------------------------------------------------------------

    /**
     * Helper methods for aligning text on the PDPageContentStream.
     */
    private static class TextAlignment {
        public static void drawCenteredText(PDPageContentStream contentStream, PDType1Font font, float fontSize, String text, float y, float centerX) throws IOException {
            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            float textWidth = font.getStringWidth(text) / 1000 * fontSize;
            contentStream.newLineAtOffset(centerX - textWidth / 2f, y);
            contentStream.showText(text);
            contentStream.endText();
        }
        
        public static void drawRightText(PDPageContentStream contentStream, PDType1Font font, float fontSize, String text, float y, float rightX) throws IOException {
            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            float textWidth = font.getStringWidth(text) / 1000 * fontSize;
            contentStream.newLineAtOffset(rightX - textWidth, y);
            contentStream.showText(text);
            contentStream.endText();
        }
        
        public static void drawLeftText(PDPageContentStream contentStream, PDType1Font font, float fontSize, String text, float y, float leftX) throws IOException {
            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.newLineAtOffset(leftX, y);
            contentStream.showText(text);
            contentStream.endText();
        }
    }
    
    // -------------------------------------------------------------
    // STYLING UTILITY
    // -------------------------------------------------------------
    
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