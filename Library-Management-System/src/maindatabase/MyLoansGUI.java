package maindatabase;

import javax.swing.*;    // For GUI components
import java.awt.event.*; // For ActionListener
import java.sql.*;       // For DB connection
import java.awt.*;       // For colors, fonts, layout
import javax.swing.table.DefaultTableModel;

public class MyLoansGUI extends JFrame implements ActionListener {

    private JButton returnBtn, renewBtn,backButton;;
    private JTable table;
    private DefaultTableModel model;
    private String username;

    public MyLoansGUI(String username) {
        this.username = username;

        setTitle("My Loans");
        setSize(700, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        JPanel panel = (JPanel) this.getContentPane();
        panel.setBackground(new Color(222, 184, 135));
        panel.setLayout(new BorderLayout());

        //create JTable
        String[] columns = {"Loan ID", "Title", "Copy ID", "Borrow Date", "Due Date","Return Date", "Status"};
        model = new DefaultTableModel(columns, 0);

        table = new JTable(model);
        table.setBackground(new Color(255, 240, 220)); 
        table.setForeground(new Color(90, 50, 20));    
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.getTableHeader().setBackground(new Color(222, 184, 135));
        JScrollPane scroll = new JScrollPane(table);
        panel.add(scroll, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));

        returnBtn = new JButton("Return Book");
        returnBtn.setBackground(new Color(245, 222, 179));
        returnBtn.setForeground(new Color(90, 50, 20));
        renewBtn = new JButton("Renew Loan");
        renewBtn.setBackground(new Color(245, 222, 179));
        renewBtn.setForeground(new Color(90, 50, 20));
        backButton = new JButton("Back");
        backButton.setBackground(new Color(245, 222, 179));
        backButton.setForeground(new Color(90, 50, 20));
        bottom.add(returnBtn);
        bottom.add(renewBtn);
        bottom.add(backButton);
        panel.add(bottom, BorderLayout.SOUTH);

        returnBtn.addActionListener(this);
        renewBtn.addActionListener(this);
        backButton.addActionListener(this);

        //Update overdue loans first
        detectOverdues();
        //Load this member’s loans into the table
        loadLoans();

        setVisible(true);
    }

    //actions for buttons
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == returnBtn) {
            returnBook();
        } else if (e.getSource() == renewBtn) {
            renewLoan();
        }
        else{
            new MemberPage(username);
            dispose();
        }
    }

    //Detect Overdue Loans
    private void detectOverdues() {
        try {
            Connection con = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
            Statement stmt = con.createStatement();

            String sql
                    = "UPDATE loans SET status = 'overdue' "
                    + "WHERE status = 'borrowed' AND due_date < date('now');";

            stmt.executeUpdate(sql);
            con.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Load Loans
    private void loadLoans() {
        model.setRowCount(0);

        try {
            Connection con = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
            Statement stmt = con.createStatement();
             
            //get the member loans
            String sql
                    = "SELECT l.loan_id, b.title, l.copy_id, l.loan_date, l.due_date,l.return_date, l.status "
                    + "FROM loans l "
                    + "JOIN members m ON l.member_id = m.member_id "
                    + "JOIN copies c ON l.copy_id = c.copy_id "
                    + "JOIN books b ON c.book_id = b.book_id "
                    + "WHERE m.username = '" + username + "';";

            ResultSet rs = stmt.executeQuery(sql);

            //add results to table
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("loan_id"),
                    rs.getString("title"),
                    rs.getInt("copy_id"),
                    rs.getString("loan_date"),
                    rs.getString("due_date"),
                    rs.getString("return_date"),
                    rs.getString("status")
                });
            }

            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Return Book and mark copy as available and mark loan as returned
    private void returnBook() {
        int row = table.getSelectedRow();
        //make sure user selected a loan
        if (row == -1) {
            JOptionPane.showMessageDialog(null, "Select a loan first.");
            return;
        }
        String status = (String) model.getValueAt(row, 6);
        
         // check if loan status is borrowd because only borrowed books can be returned
         if (!status.equals("borrowed")) {
            JOptionPane.showMessageDialog(null, "Only borrowed books can be returned.");
            return;
        }
        int loanId = (int) model.getValueAt(row, 0);
        int copyId = (int) model.getValueAt(row, 2);
        

        try {
            Connection con = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
            Statement stmt = con.createStatement();
            // Update loan status and return date
            String sql1 = "UPDATE loans SET status = 'returned' WHERE loan_id = " + loanId + ";";
            String sql2 = "UPDATE loans SET return_date = date('now') WHERE loan_id = " + loanId + ";";
            // Make copy available again
            String sql3 = "UPDATE copies SET status = 'available' WHERE copy_id = " + copyId + ";";
            stmt.executeUpdate(sql1);
            stmt.executeUpdate(sql2);
            stmt.executeUpdate(sql3);
            
              // Get book info for notifications
             String bookQuery = 
            "SELECT b.book_id, b.title FROM books b JOIN copies c ON b.book_id = c.book_id WHERE c.copy_id = " + copyId + ";";
        ResultSet rsBook = stmt.executeQuery(bookQuery);

        int bookId = -1;
        String bookTitle = " ";
         if (rsBook.next()) {
            bookId = rsBook.getInt("book_id");
            bookTitle = rsBook.getString("title");
        }
        rsBook.close();

            // Check if anyone is waiting for this copy
            String findNextHold
                    = "SELECT member_id FROM holds "
                    + "WHERE book_id = " + bookId + " "
                    + "ORDER BY position ASC LIMIT 1;";

            ResultSet rsHold = stmt.executeQuery(findNextHold);

             // If someone is next in queue, notify them
            if (rsHold.next()) {

                int nextMemberId = rsHold.getInt("member_id");

                // Escape quotes in book title to avoid SQL errors
                String safeBookTitle = bookTitle.replace("'", "''");
                //Insert notification for the next member
                String insertNotif =
                "INSERT INTO notifications (member_id, message, timestamp, seen) VALUES (" +
                nextMemberId + ", " +
                "'Your reserved book \"" + safeBookTitle + "\"is now available to borrow.', " +
                "datetime('now'), 0);";

                stmt.executeUpdate(insertNotif);

                //Remove the hold for that member ONLY
                String deleteHold="DELETE FROM holds WHERE book_id = " + bookId + " AND member_id = " + nextMemberId + ";";

                stmt.executeUpdate(deleteHold);}
            
                JOptionPane.showMessageDialog(null, "Book returned successfully!");
                loadLoans();// Refresh table

            }catch (SQLException e) {
            e.printStackTrace();
        }
        }
    
        //Renew Loan by adding 7 days to the due date
    private void renewLoan() {
        int row = table.getSelectedRow();
        //ccheck if user selected a row
        if (row == -1) {
            JOptionPane.showMessageDialog(null, "Select a loan first.");
            return;
        }

        int loanId = (int) model.getValueAt(row, 0);
        String status = (String) model.getValueAt(row, 6);
        
        // check if loan status is borrowd because only borrowed books can be renewd
        if (!status.equals("borrowed")) {
            JOptionPane.showMessageDialog(null, "Only borrowed books can be renewed.");
            return;
        }

        try {
            Connection con = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
            Statement stmt = con.createStatement();

            // Extend due date by 7 days
            String sql
                    = "UPDATE loans SET due_date = date(due_date, '+7 days') "
                    + "WHERE loan_id = " + loanId + ";";

            stmt.executeUpdate(sql);

            JOptionPane.showMessageDialog(null, "Loan renewed for 7 more days.");
            loadLoans();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
