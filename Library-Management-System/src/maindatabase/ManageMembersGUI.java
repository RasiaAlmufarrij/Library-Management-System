package maindatabase;

import javax.swing.*;    // For GUI components
import java.awt.event.*; // For ActionListener
import java.sql.*;       // For DB connection
import java.awt.*;       // For colors, fonts, layout
import javax.swing.table.DefaultTableModel;

public class ManageMembersGUI extends JFrame implements ActionListener {

    private JTable table;
    private DefaultTableModel model;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton addButton, updateButton, deleteButton, refreshButton, backButton;
    private String username;   

    public ManageMembersGUI(String user) {
        this.username = user;

        setTitle("Manage Members");
        setSize(650, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = (JPanel) this.getContentPane();
        panel.setBackground(new Color(222, 184, 135));
        panel.setLayout(new BorderLayout());

        JPanel north = new JPanel(new GridLayout(2, 2));
        north.setBackground(new Color(222, 184, 135));

        JLabel usernameLabel = new JLabel("Member Username (Mxxxxx):");
        usernameLabel.setForeground(new Color(50, 30, 10)); 
        north.add(usernameLabel);
        usernameField = new JTextField();
        north.add(usernameField);

        JLabel passwordLabel = new JLabel("Password (6 chars):");
        passwordLabel.setForeground(new Color(50, 30, 10));
        north.add(passwordLabel);
        passwordField = new JPasswordField();
        north.add(passwordField);

        panel.add(north, BorderLayout.NORTH);

        // members table in the middle
        model = new DefaultTableModel(new String[]{"Member ID", "Username"}, 0);
        table = new JTable(model);
        table.setBackground(new Color(255, 240, 220)); 
        table.setForeground(new Color(90, 50, 20));    
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.getTableHeader().setBackground(new Color(222, 184, 135));
        JScrollPane scroll = new JScrollPane(table);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        south.setBackground(new Color(222, 184, 135));

        addButton = new JButton("Add Member");
        addButton.setBackground(new Color(245, 222, 179));
        addButton.setForeground(new Color(90, 50, 20));
        addButton.addActionListener(this);
        south.add(addButton);

        updateButton = new JButton("Update Member");
        updateButton.setBackground(new Color(245, 222, 179));
        updateButton.setForeground(new Color(90, 50, 20));
        updateButton.addActionListener(this);
        south.add(updateButton);

        deleteButton = new JButton("Delete Member");
        deleteButton.setBackground(new Color(245, 222, 179));
        deleteButton.setForeground(new Color(90, 50, 20));
        deleteButton.addActionListener(this);
        south.add(deleteButton);

        refreshButton = new JButton("Refresh");
        refreshButton.setBackground(new Color(245, 222, 179));
        refreshButton.setForeground(new Color(90, 50, 20));
        refreshButton.addActionListener(this);
        south.add(refreshButton);

        backButton = new JButton("Back");
        backButton.setBackground(new Color(245, 222, 179));
        backButton.setForeground(new Color(90, 50, 20));
        backButton.addActionListener(this);
        south.add(backButton);

        panel.add(south, BorderLayout.SOUTH);

        loadMembers();

        setVisible(true);
    }

    //actions for buttons
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == addButton) {
            addMember();
        }
        else if (e.getSource() == updateButton) {
            updateMember();
        }
        else if (e.getSource() == deleteButton) {
            deleteMember();
        }
        else if (e.getSource() == refreshButton) {
            loadMembers();
            clearFields();
        }
        else if (e.getSource() == backButton) {
            new LibrarianPage(username);
            dispose();
        }
    }

    // load members into table (display)
    private void loadMembers() {
        model.setRowCount(0);

        try {
            Connection con = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT member_id, username FROM members ORDER BY member_id;");

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("member_id"),
                    rs.getString("username")
                });
            }

            rs.close();
            con.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading members: " + ex.getMessage());
        }
    }

    // add new member
    private void addMember() {
        String user = usernameField.getText().trim();
        String pass = String.valueOf(passwordField.getPassword());
        //check if user didn't fill all the fields
        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }
        //check if the username entered doesnt start with "M" or its length isn't 6
        if (user.length() != 6 || !user.startsWith("M")) {
            JOptionPane.showMessageDialog(this, "Username must start with 'M' and be exactly 6 characters.");
            return;
        }
        //check if the password length isn't 6

        if (pass.length() != 6) {
            JOptionPane.showMessageDialog(this, "Password must be exactly 6 characters.");
            return;
        }

        try {
            Connection con = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
            Statement stmt = con.createStatement();
            //check if username already exist
            String checkSQL = "SELECT * FROM members WHERE username = '" + user + "';";
            ResultSet rs = stmt.executeQuery(checkSQL);
            if (rs.next()) {
                rs.close();
                con.close();
                JOptionPane.showMessageDialog(this, "Username already exists.");
                return;
            }
            rs.close();
            
            // Encrypt password with SHA-256
            String hashedPass = hashPassword(pass);

            String insertSQL = "INSERT INTO members (username, password) VALUES (?, ?)";
            PreparedStatement pst = con.prepareStatement(insertSQL);
            pst.setString(1, user);
            pst.setString(2, hashedPass);
            pst.executeUpdate();
            pst.close();


            con.close();

            JOptionPane.showMessageDialog(this, "Member added successfully!");
            clearFields();
            loadMembers();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding member: " + ex.getMessage());
        }
    }

    // update a member's info
    private void updateMember() {
        int row = table.getSelectedRow();
        //check if the user selected a member
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a member first.");
            return;
        }

        int memberId = (int) model.getValueAt(row, 0);
        String user = usernameField.getText().trim();
        String pass = String.valueOf(passwordField.getPassword());
        //check if fields ar empty
        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }
        //check if the usename length isn't 6 or it doesn't start with "M"
        if (user.length() != 6 || !user.startsWith("M")) {
            JOptionPane.showMessageDialog(this, "Username must start with 'M' and be exactly 6 characters.");
            return;
        }
        //check if the password length isn't 6
        if (pass.length() != 6) {
            JOptionPane.showMessageDialog(this, "Password must be exactly 6 characters.");
            return;
        }

        try {
            Connection con = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
            Statement stmt = con.createStatement();
            // Check if username already used by another member
            String checkSQL = "SELECT member_id FROM members WHERE username = '" + user
                    + "' AND member_id <> " + memberId + ";";
            ResultSet rs = stmt.executeQuery(checkSQL);
            if (rs.next()) {
                rs.close();
                con.close();
                JOptionPane.showMessageDialog(this, "Username already exists for another member.");
                return;
            }
            rs.close();
            // Encrypt password again
            String hashedPass = hashPassword(pass);
 
            // Update record
            String updateSQL = "UPDATE members SET username = ?, password = ? WHERE member_id = ?";
            PreparedStatement pst = con.prepareStatement(updateSQL);
            pst.setString(1, user);
            pst.setString(2, hashedPass);
            pst.setInt(3, memberId);
            pst.executeUpdate();
            pst.close();


            con.close();

            JOptionPane.showMessageDialog(this, "Member updated successfully!");
            clearFields();
            loadMembers();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating member: " + ex.getMessage());
        }
    }

    // delete a member but checking for active loans first
    private void deleteMember() {
        int row = table.getSelectedRow();
        //check if the user selected a member
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a member first.");
            return;
        }

        int memberId = (int) model.getValueAt(row, 0);
        // Confirm deletion
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this member?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            Connection con = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
            Statement stmt = con.createStatement();
            //Check for active loans
            String checkLoanSQL = "SELECT COUNT(*) AS cnt FROM loans "
                    + "WHERE member_id = " + memberId + " AND return_date IS NULL;";
            ResultSet rsLoan = stmt.executeQuery(checkLoanSQL);
            // Cannot delete if they still have books
            if (rsLoan.next() && rsLoan.getInt("cnt") > 0) {
                rsLoan.close();
                con.close();
                JOptionPane.showMessageDialog(this,
                        "Cannot delete member. There are active loans for this member.");
                return;
            }
            rsLoan.close();
            // Delete their reservations first
            stmt.executeUpdate("DELETE FROM holds WHERE member_id = " + memberId + ";");
            // Delete member record
            stmt.executeUpdate("DELETE FROM members WHERE member_id = " + memberId + ";");

            con.close();

            JOptionPane.showMessageDialog(this, "Member deleted successfully!");
            clearFields();
            loadMembers();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting member: " + ex.getMessage());
        }
    }
    // Clear username and password fields
    private void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
    }
     // Hash password (SHA-256)
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
