package maindatabase;

import javax.swing.*;    // For GUI components
import java.awt.event.*; // For ActionListener
import java.sql.*;       // For DB connection
import java.awt.*;       // For colors, fonts, layout

public class LoginGUI extends JFrame implements ActionListener {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton, signUpButton;

    //Interface
    public LoginGUI() {
        setTitle("Library Login");
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        JPanel panel = (JPanel) this.getContentPane();
        panel.setBackground(new Color(222, 184, 135));
        panel.setLayout(new GridLayout(3, 2, 5, 5));

        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Arial", Font.BOLD, 14));
        userLabel.setForeground(new Color(101, 67, 33));
        panel.add(userLabel);

        usernameField = new JTextField();
        usernameField.setToolTipText("Username must start with M (member) or L (librarian) and be 6 characters long");
        panel.add(usernameField);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Arial", Font.BOLD, 14));
        passLabel.setForeground(new Color(101, 67, 33));
        panel.add(passLabel);

        passwordField = new JPasswordField();
        passwordField.setToolTipText("Password must be exactly 6 characters long");
        panel.add(passwordField);

        loginButton = new JButton("Login");
        loginButton.setBackground(new Color(245, 222, 179));
        loginButton.setForeground(new Color(90, 50, 20));
        loginButton.addActionListener(this);
        panel.add(loginButton);

        signUpButton = new JButton("Sign Up");
        signUpButton.setBackground(new Color(245, 222, 179));
        signUpButton.setForeground(new Color(90, 50, 20));
        signUpButton.addActionListener(this);
        panel.add(signUpButton);

        setVisible(true);
    }

    //actions for buttons
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {

            String username = usernameField.getText().trim();
            String password = String.valueOf(passwordField.getPassword());

            // Username must be 6 characters
            if (username.length() != 6) {
                JOptionPane.showMessageDialog(this, "Username must be exactly 6 characters.");
                return;
            }

            // Password must be 6 characters
            if (password.length() != 6) {
                JOptionPane.showMessageDialog(this, "Password must be exactly 6 characters.");
                return;
            }

            //if username starts with "M" take user to member page
            if (checkLogin(username, password)) {
                if (username.startsWith("M")) {
                    JOptionPane.showMessageDialog(this, "Welcome Member!");
                    new MemberPage(username);
                    dispose();

                    //if username starts with "L" take user to Librarian page
                } else if (username.startsWith("L")) {
                    JOptionPane.showMessageDialog(this, "Welcome Librarian!");
                    new LibrarianPage(username);
                    dispose();
                }
                //if the user eners an invalid username or password show this message
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password");
            }
        } else if (e.getSource() == signUpButton) {
            new SignUpGUI();
            this.dispose();
        }
    }

    // Checks if the username and password exist in DB
    private boolean checkLogin(String username, String password) {
        try {
            Connection con = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");

            // password hashing
            String hashedPass = hashPassword(password);
            String sql;

            // Check members table
            if (username.startsWith("M")) {
                sql = "SELECT * FROM members WHERE username = ? AND password = ?";
            } // Check librarians table
            else if (username.startsWith("L")) {
                sql = "SELECT * FROM librarians WHERE username = ? AND password = ?";
            } else {
                return false;
            }

            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, username);
            pst.setString(2, hashedPass);

            ResultSet rs = pst.executeQuery();
            boolean found = rs.next();

            rs.close();
            pst.close();
            con.close();

            return found;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Hash password with SHA-256
    public static String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes("UTF-8"));

            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
