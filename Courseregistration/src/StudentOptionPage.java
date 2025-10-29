import javax.swing.*;
import java.awt.*;

public class StudentOptionPage extends JPanel {
    private MainFrame main;

    public StudentOptionPage(MainFrame main) {
        this.main = main;

        
        Color bgColor = new Color(245, 247, 250);
        Color primary = new Color(52, 152, 219);
        Color primaryDark = new Color(41, 128, 185);
        Color secondaryText = new Color(100, 100, 100);

        setLayout(new GridBagLayout());
        setBackground(bgColor);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

      
        JLabel lblTitle = new JLabel("Student Portal", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(primary);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; 
        add(lblTitle, gbc);

       
        gbc.gridwidth = 1;

     
        JButton btnLogin = new JButton("Login");
        JButton btnSignup = new JButton("Create Profile");
        styleMainButton(btnLogin, primary, primaryDark);
        styleMainButton(btnSignup, primary, primaryDark);

        Dimension mainBtnSize = new Dimension(220, 45);
        btnLogin.setPreferredSize(mainBtnSize);
        btnSignup.setPreferredSize(mainBtnSize);

        
        gbc.gridy = 2;
        gbc.gridx = 0;
        add(btnLogin, gbc);

        
        gbc.gridx = 1;
        add(btnSignup, gbc);

        
        JButton btnBack = new JButton("← Back");
        
        
        styleSmallButton(btnBack, primary, primaryDark); 

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2; 
        gbc.insets = new Insets(30, 15, 15, 15);
        add(btnBack, gbc);
        
    
        gbc.insets = new Insets(15, 15, 15, 15);

        
        btnLogin.addActionListener(e -> main.showPage("login"));
        btnSignup.addActionListener(e -> main.showPage("signup"));
        btnBack.addActionListener(e -> main.showPage("welcome"));
    }

    private void styleMainButton(JButton b, Color primary, Color primaryDark) {
        b.setBackground(primary);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 16));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        b.addChangeListener(e -> {
            if (b.getModel().isRollover()) b.setBackground(primaryDark);
            else b.setBackground(primary);
        });
    }

  
    private void styleSmallButton(JButton b, Color primary, Color primaryDark) {
        b.setBackground(primary);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        
        b.setPreferredSize(new Dimension(120, 32)); 
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));

        b.addChangeListener(e -> {
            if (b.getModel().isRollover()) b.setBackground(primaryDark);
            else b.setBackground(primary);
        });
    }

}

