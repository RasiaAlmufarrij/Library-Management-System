package maindatabase;

import javax.swing.*;    // For GUI components
import java.awt.event.*; // For ActionListener
import java.sql.*;       // For DB connection
import java.awt.*;       // For colors, fonts, layout

public class SignUpGUI extends JFrame implements ActionListener {

    private JTextField usernameField;
    private JPasswordField passwordField, confermpasswordField;
    private JButton registerButton, backButton;
//Interface

    public SignUpGUI() {
        setTitle("Member Sign Up");
        setSize(350, 180);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        JPanel panel = (JPanel) this.getContentPane();
        panel.setBackground(new Color(222, 184, 135));
        panel.setLayout(new GridLayout(4, 2));

        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Arial", Font.BOLD, 14));
        userLabel.setForeground(new Color(101, 67, 33));
        panel.add(userLabel);

        usernameField = new JTextField();
        usernameField.setToolTipText("Must start with 'M' and be exactly 6 characters.");
        panel.add(usernameField);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Arial", Font.BOLD, 14));
        passLabel.setForeground(new Color(101, 67, 33));
        panel.add(passLabel);

        passwordField = new JPasswordField();
        passwordField.setToolTipText("Password must be exactly 6 characters.");
        panel.add(passwordField);

        JLabel confpassLabel = new JLabel("Confirm Password:");
        confpassLabel.setFont(new Font("Arial", Font.BOLD, 14));
        confpassLabel.setForeground(new Color(101, 67, 33));
        panel.add(confpassLabel);
        confermpasswordField = new JPasswordField();
        panel.add(confermpasswordField);

        registerButton = new JButton("Register");
        registerButton.setBackground(new Color(245, 222, 179));
        registerButton.setForeground(new Color(90, 50, 20));
        registerButton.addActionListener(this);
        panel.add(registerButton);

        backButton = new JButton("Back");
        backButton.setBackground(new Color(245, 222, 179));
        backButton.setForeground(new Color(90, 50, 20));
        backButton.addActionListener(this);
        panel.add(backButton);

        setVisible(true);
    }

    //actions for buttons
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == backButton) {
            new LoginGUI();
            this.dispose();
            return;
        }

        // Read input values
        String username = usernameField.getText().trim();
        String password = String.valueOf(passwordField.getPassword());

        String confirmPassword = String.valueOf(confermpasswordField.getPassword());
        // Check if passwords match

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Password and Confirm Password do not match!");
            return;
        }

        // Username must be 6 chars and start with M
        if (username.length() != 6 || !username.startsWith("M")) {
            JOptionPane.showMessageDialog(this, "Username must start with 'M' and be exactly 6 characters.");
            return;
        }

        // Password must be 6 chars
        if (password.length() != 6) {
            JOptionPane.showMessageDialog(this, "Password must be exactly 6 characters.");
            return;
        }

        // Check empty fields
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }

        // Try inserting the new member
        if (insertMember(username, password)) {
            JOptionPane.showMessageDialog(this, "Registration successful!");
        } else {
            JOptionPane.showMessageDialog(this, "Username already exists or error occurred.");
        }
    }

    // Inserts a new member into the database
    private boolean insertMember(String username, String password) {
        try {
            Connection con = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");

            // password hashing
            String hashedPass = hashPassword(password);

            // Check if username exists already
            String checkSQL = "SELECT * FROM members WHERE username = ?";
            PreparedStatement checkStmt = con.prepareStatement(checkSQL);
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                rs.close();
                checkStmt.close();
                con.close();
                return false; // Username exists
            }
            rs.close();
            checkStmt.close();

            // Insert new member
            String insertSQL = "INSERT INTO members (username, password) VALUES (?, ?)";
            PreparedStatement insertStmt = con.prepareStatement(insertSQL);

            insertStmt.setString(1, username);
            insertStmt.setString(2, hashedPass);

            insertStmt.executeUpdate();
            insertStmt.close();
            con.close();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

// Hashes the password using SHA-256
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
