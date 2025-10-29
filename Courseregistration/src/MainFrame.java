import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.net.URL; 
import java.util.Map; 

public class MainFrame extends JFrame {
    
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private HashMap<String, JPanel> pages;

    
    private AvailableCoursesPage availableCoursesPage;
    private MyCoursesPage myCoursesPage;
    private MyCertificatesPage myCertificatesPage;

  
    private CourseAttendancePage courseAttendancePage;

    private String currentAdmissionNo; 

    public MainFrame() {
        setTitle("Edura");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       
        setSize(800, 600); 
        setLocationRelativeTo(null);
        
      
        try {
            
            URL iconURL = getClass().getResource("/appicon1.png"); 
            if (iconURL != null) {
                Image icon = Toolkit.getDefaultToolkit().getImage(iconURL);
                setIconImage(icon);
            } else {
               
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
        
     
        addPage("welcome", new WelcomePage(this));
        addPage("studentoptions", new StudentOptionPage(this)); 
        addPage("login", new Login(this));
        addPage("signup", new SignupForm(this)); 
        addPage("adminlogin", new AdminLogin(this));
        addPage("reset", new ResetPassword(this));
        
      
        addPage("managecourses", new ManageCoursesPage(this)); 
        AdminPage adminPage = new AdminPage(this);
        addPage("admin", adminPage);
      
        addPage("managestudents", new ManageCoursesPage(this)); 
        addPage("viewstudents", new ViewStudentsByCoursePage(this));

        AddCoursePage addCoursePage = new AddCoursePage(this);
        addPage("addcourse", addCoursePage);
        
       
        courseAttendancePage = new CourseAttendancePage(this);
        addPage("courseattendance", courseAttendancePage);

        add(cardPanel);
        showPage("welcome");
        
      
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
       
        setVisible(true); 
    }
    
    public void addPage(String name, JPanel page) {
        pages.put(name, page);
        cardPanel.add(page, name);
    }

  
    public void showPage(String name) {
        if ("signup".equals(name)) {
            JPanel page = pages.get(name);
            if (page instanceof SignupForm) {
                ((SignupForm) page).clearFields();
            }
        }
        
        cardLayout.show(cardPanel, name);
    }

 
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
        String pageName = "coursedetails_" + courseId;
        
       
        if (pages.containsKey(pageName)) {
            cardPanel.remove(pages.get(pageName));
            pages.remove(pageName);
        }
        
        CourseDetailsPage courseDetails = new CourseDetailsPage(this, courseId, studentAdmissionNo);
        addPage(pageName, courseDetails); 
        showPage(pageName);
    }
    
    public JPanel getPage(String name) {
        return pages.get(name);
    }
    
 
    public AvailableCoursesPage getAvailableCoursesPage() {
        return availableCoursesPage;
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

   
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainFrame().finishSetup();
        });
    }

}
