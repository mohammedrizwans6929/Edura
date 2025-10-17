import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.net.URL; 

public class MainFrame extends JFrame {
    
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private HashMap<String, JPanel> pages;

    // Keep references to student-specific pages
    private AvailableCoursesPage availableCoursesPage;
    private MyCoursesPage myCoursesPage;
    private MyCertificatesPage myCertificatesPage;

    // Keep references to admin-specific pages
    private CourseAttendancePage courseAttendancePage;

    private String currentAdmissionNo;  // Logged-in student

    public MainFrame() {
        setTitle("Edura");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Set initial size
        setSize(800, 600); 
        setLocationRelativeTo(null);
        
        // --- Setup Frame Icon ---
        try {
            // Note: Updated path in the original code to "/appicon1.png"
            URL iconURL = getClass().getResource("/appicon1.png"); 
            if (iconURL != null) {
                Image icon = Toolkit.getDefaultToolkit().getImage(iconURL);
                setIconImage(icon);
            } else {
                // Correcting the warning message for consistency
                System.err.println("Warning: Icon file '/appicon1.png' not found on classpath. Frame icon not set.");
            }
        } catch (Exception e) {
            System.err.println("Error loading frame icon: " + e.getMessage());
        }
    }
    
    private void finishSetup() {
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        pages = new HashMap<>();
        
        // Initial pages and setup logic
        addPage("welcome", new WelcomePage(this));
        // Note: Using "studentoptions" key as per your provided code
        addPage("studentoptions", new StudentOptionPage(this)); 
        addPage("login", new Login(this));
        // Ensure the SignupForm instance is stored in pages
        addPage("signup", new SignupForm(this)); 
        addPage("adminlogin", new AdminLogin(this));
        addPage("reset", new ResetPassword(this));
        
        // Admin Pages Initialization
        addPage("managecourses", new ManageCoursesPage(this)); 
        AdminPage adminPage = new AdminPage(this);
        addPage("admin", adminPage);
        addPage("managestudents", new ManageCoursesPage(this)); 
        addPage("viewstudents", new ViewStudentsByCoursePage(this));

        AddCoursePage addCoursePage = new AddCoursePage(this);
        addPage("addcourse", addCoursePage);
        
        // Initialize Course Attendance Page
        courseAttendancePage = new CourseAttendancePage(this);
        addPage("courseattendance", courseAttendancePage);

        add(cardPanel);
        showPage("welcome");
        
        // Set the frame state to maximized (full screen)
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        // Make frame visible
        setVisible(true); 
    }
    
    public void addPage(String name, JPanel page) {
        pages.put(name, page);
        cardPanel.add(page, name);
    }

    /**
     * Shows the requested page and performs cleanup actions if necessary.
     */
    public void showPage(String name) {
        // 🔑 MODIFICATION: Check for "signup" and clear fields before navigating
        if ("signup".equals(name)) {
            JPanel page = pages.get(name);
            if (page instanceof SignupForm) {
                ((SignupForm) page).clearFields();
            }
        }
        
        // Show the page
        cardLayout.show(cardPanel, name);
    }

    // --- Dynamic Navigation Methods (omitted for brevity) ---

    public void showProfilePage(String admissionNo) {
        ProfilePage profilePage = new ProfilePage(this, admissionNo);
        addPage("profilePage_" + admissionNo, profilePage);
        showPage("profilePage_" + admissionNo);
    }
    
    public void showAdminCourseDetailsPage(String courseId) {
        AdminCourseDetailsPage detailsPage = new AdminCourseDetailsPage(this, courseId);
        addPage("admincoursedetails_" + courseId, detailsPage);
        showPage("admincoursedetails_" + courseId);
    }

    public void showEditCoursePage(String courseId, ManageCoursesPage parentPage) {
        EditCoursePage editPage = new EditCoursePage(this, courseId, parentPage);
        String pageName = "editcourse_" + courseId;
        
        if (pages.containsKey(pageName)) {
            cardPanel.remove(pages.get(pageName));
            pages.remove(pageName);
        }
        
        addPage(pageName, editPage);
        showPage(pageName);
    }

    public void showCourseRegisteredDetails(String courseId, String studentAdmissionNo) {
        CourseRegisteredDetailsPage detailsPage = new CourseRegisteredDetailsPage(this, studentAdmissionNo, courseId);
        String pageName = "registereddetails_" + courseId + "_" + studentAdmissionNo;
        
        if (pages.containsKey(pageName)) {
            cardPanel.remove(pages.get(pageName));
            pages.remove(pageName);
        }
        
        addPage(pageName, detailsPage);
        showPage(pageName);
    }

    public void setCurrentStudent(String admissionNo) {
        this.currentAdmissionNo = admissionNo;

        // Note: Creating these pages here ensures they are initialized once per login
        // and have access to the current student's admission number.
        if (availableCoursesPage == null) {
            availableCoursesPage = new AvailableCoursesPage(this, currentAdmissionNo);
            addPage("availablecourses", availableCoursesPage);
        }

        if (myCoursesPage == null) {
            myCoursesPage = new MyCoursesPage(this, currentAdmissionNo);
            addPage("mycourses", myCoursesPage);
        }
        
        if (myCertificatesPage == null) {
            myCertificatesPage = new MyCertificatesPage(this, currentAdmissionNo);
            addPage("mycertificates", myCertificatesPage);
        }
    }
    
    public void showCourseDetailsPage(String courseId, String studentAdmissionNo) {
        CourseDetailsPage courseDetails = new CourseDetailsPage(this, courseId, studentAdmissionNo);
        addPage("coursedetails_" + courseId, courseDetails);
        showPage("courseregistereddetails_" + courseId);
    }
    
    public JPanel getPage(String name) {
        return pages.get(name);
    }

    public void showAvailableCoursesPage() {
        if (availableCoursesPage != null) {
            availableCoursesPage.refreshCourses();
            showPage("availablecourses");
        }
    }
 
    public void showMyCoursesPage() {
        if (myCoursesPage != null) {
            myCoursesPage.loadCourses(); 
            showPage("mycourses");
        }
    }
    
    public void showMyCertificatesPage() {
        if (myCertificatesPage != null) {
            myCertificatesPage.loadCertificates(); 
            showPage("mycertificates");
        }
    }

    // --- Main Method (Direct Launch) ---
    
    public static void main(String[] args) {
        // Application is now launched directly on the EDT.
        SwingUtilities.invokeLater(() -> {
            new MainFrame().finishSetup();
        });
    }
}