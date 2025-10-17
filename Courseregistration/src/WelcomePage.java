import javax.swing.*;
import java.awt.*;

public class WelcomePage extends JPanel {
    private MainFrame main;

    public WelcomePage(MainFrame main) {
        this.main = main;
        setLayout(new GridBagLayout());
        setBackground(new Color(245, 247, 250));

        Color primary = new Color(52, 152, 219);
        Color hover = new Color(41, 128, 185);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitle = new JLabel("Welcome to Edura", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(primary);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(lblTitle, gbc);

       
        gbc.gridwidth = 1;

        JButton btnStudent = new JButton("I'm a Student");
        styleButton(btnStudent, primary, hover);
        gbc.gridy = 2; gbc.gridx = 0;
        add(btnStudent, gbc);

        JButton btnAdmin = new JButton("I'm an Admin");
        styleButton(btnAdmin, primary, hover);
        gbc.gridx = 1;
        add(btnAdmin, gbc);

        // Actions
        btnStudent.addActionListener(e -> main.showPage("studentoptions"));
        btnAdmin.addActionListener(e -> main.showPage("adminlogin"));
    }

    private void styleButton(JButton b, Color primary, Color hover) {
        b.setBackground(primary);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 16));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setPreferredSize(new Dimension(180, 45));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        b.addChangeListener(e -> {
            if (b.getModel().isRollover()) b.setBackground(hover);
            else b.setBackground(primary);
        });
    }
}
