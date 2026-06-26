package maindatabase;

import javax.swing.*;    // For GUI components
import java.awt.event.*; // For ActionListener
import java.sql.*;       // For DB connection
import java.awt.*;       // For colors, fonts, layout

public class BorrowReturnGUI extends JFrame implements ActionListener {

    private JTextField borrowMemberField, borrowBookIdField;
    private JTextField returnCopyIdField, returnBookIdField;
    private JButton borrowButton, returnButton, viewCopiesButton, backButton;
    private String username;

    //Interface
    public BorrowReturnGUI(String user) {
        this.username = user;

        setTitle("Borrow / Return");
        setSize(450, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = (JPanel) this.getContentPane();
        panel.setBackground(new Color(222, 184, 135));
        panel.setLayout(new BorderLayout());

        JPanel center = new JPanel(new GridLayout(2, 1));
        center.setBackground(new Color(222, 184, 135));

        JPanel borrowPanel = new JPanel(new GridLayout(3, 2));
        borrowPanel.setBackground(new Color(222, 184, 135));
        borrowPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(90, 50, 20), 1), "Borrow Book"));

        borrowPanel.add(new JLabel("Member Username:"));
        borrowMemberField = new JTextField();
        borrowPanel.add(borrowMemberField);

        borrowPanel.add(new JLabel("Book ID:"));
        borrowBookIdField = new JTextField();
        borrowPanel.add(borrowBookIdField);

        borrowPanel.add(new JLabel());
        borrowButton = new JButton("Borrow");
        borrowButton.setBackground(new Color(245, 222, 179));
        borrowButton.setForeground(new Color(90, 50, 20));
        borrowButton.addActionListener(this);
        borrowPanel.add(borrowButton);

        JPanel returnPanel = new JPanel(new GridLayout(4, 2));
        returnPanel.setBackground(new Color(222, 184, 135));
        returnPanel.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createLineBorder(new Color(90, 50, 20), 1), "Return Book"));

        returnPanel.add(new JLabel("Book ID:"));
        returnBookIdField = new JTextField();
        returnPanel.add(returnBookIdField);

        returnPanel.add(new JLabel("Copy ID:"));
        returnCopyIdField = new JTextField();
        returnPanel.add(returnCopyIdField);

        returnPanel.add(new JLabel());
        returnButton = new JButton("Return");
        returnButton.setBackground(new Color(245, 222, 179));
        returnButton.setForeground(new Color(90, 50, 20));
        returnButton.addActionListener(this);
        returnPanel.add(returnButton);

        returnPanel.add(new JLabel());
        viewCopiesButton = new JButton("View Copies");
        viewCopiesButton.setBackground(new Color(245, 222, 179));
        viewCopiesButton.setForeground(new Color(90, 50, 20));
        viewCopiesButton.addActionListener(this);
        returnPanel.add(viewCopiesButton);

        center.add(borrowPanel);
        center.add(returnPanel);

        panel.add(center, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        backButton = new JButton("Back");
        backButton.setBackground(new Color(245, 222, 179));
        backButton.setForeground(new Color(90, 50, 20));
        backButton.addActionListener(this);
        south.add(backButton);

        panel.add(south, BorderLayout.SOUTH);

        setVisible(true);
    }

    //actions for buttons
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == borrowButton) {
            borrowForMember();
        } else if (e.getSource() == returnButton) {
            returnBook();
        } else if (e.getSource() == viewCopiesButton) {
            showCopiesWindow();
        } else if (e.getSource() == backButton) {
            new LibrarianPage(username);
            dispose();
        }
    }

    // borrowing a book for a member (librarian enters the member's name and book id)
    private void borrowForMember() {
        String memberUser = borrowMemberField.getText().trim();
        String bookIdText = borrowBookIdField.getText().trim();

        //check if the fields are empty
        if (memberUser.isEmpty() || bookIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }
        //check if the username is entered correctly
        if (!memberUser.startsWith("M") || memberUser.length() != 6) {
            JOptionPane.showMessageDialog(this, "Member username must start with 'M' and be 6 characters.");
            return;
        }

        int bookId;
        try {
            bookId = Integer.parseInt(bookIdText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Book ID must be a number.");
            return;
        }

        try {
            Connection con = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
            Statement stmt = con.createStatement();

            // making sure that the member exists 
            String memberSQL = "SELECT member_id FROM members WHERE username = '" + memberUser + "';";
            ResultSet rsMember = stmt.executeQuery(memberSQL);
            if (!rsMember.next()) {
                rsMember.close();
                con.close();
                JOptionPane.showMessageDialog(this, "Member not found.");
                return;
            }
            int memberId = rsMember.getInt("member_id");
            rsMember.close();

            // looking for available book copy
            String copySQL = "SELECT copy_id FROM copies WHERE book_id = " + bookId
                    + " AND status = 'available' LIMIT 1;";
            ResultSet rsCopy = stmt.executeQuery(copySQL);
            if (!rsCopy.next()) {
                rsCopy.close();
                con.close();
                JOptionPane.showMessageDialog(this, "No available copies for this book.");
                return;
            }
            int copyId = rsCopy.getInt("copy_id");
            rsCopy.close();

            // updating the copying state and the loans 
            String updateCopySQL = "UPDATE copies SET status = 'borrowed' WHERE copy_id = " + copyId + ";";
            stmt.executeUpdate(updateCopySQL);

            String loanSQL = "INSERT INTO loans (member_id, copy_id, loan_date, due_date) "
                    + "VALUES (" + memberId + ", " + copyId + ", date('now'), date('now','+14 days'));";
            stmt.executeUpdate(loanSQL);

            con.close();

            JOptionPane.showMessageDialog(this, "Book borrowed successfully.\nCopy ID: " + copyId);

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    // returning a book via copy id
    private void returnBook() {
        String copyIdText = returnCopyIdField.getText().trim();
        if (copyIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Copy ID.");
            return;
        }

        int copyId;
        try {
            copyId = Integer.parseInt(copyIdText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Copy ID must be a number.");
            return;
        }

        try {
            Connection con = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
            Statement stmt = con.createStatement();

            // Find active loan for this copy (loan with no return date)
            String loanSQL = "SELECT loan_id FROM loans WHERE copy_id = " + copyId + " AND return_date IS NULL;";
            ResultSet rsLoan = stmt.executeQuery(loanSQL);
            if (!rsLoan.next()) {
                rsLoan.close();
                con.close();
                JOptionPane.showMessageDialog(this, "No active loan found for this copy.");
                return;
            }
            int loanId = rsLoan.getInt("loan_id");
            rsLoan.close();

            String updateLoan = "UPDATE loans SET return_date = date('now') WHERE loan_id = " + loanId + ";";
            stmt.executeUpdate(updateLoan);

            String updateCopy = "UPDATE copies SET status = 'available' WHERE copy_id = " + copyId + ";";
            stmt.executeUpdate(updateCopy);

            con.close();

            JOptionPane.showMessageDialog(this, "Book returned successfully.");

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void showCopiesWindow() {
        String bookIdText = returnBookIdField.getText().trim();

        //check if the field is empty
        if (bookIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill the bookID field.");
            return;
        }
        // turn string to int to get book id
        int bookId;
        try {
            bookId = Integer.parseInt(bookIdText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Book ID must be a valid number.");
            return;
        }
        new ViewCopiesGUI(bookId);
    }
}
