package maindatabase;

import javax.swing.*;    // For GUI components
import java.awt.event.*; // For ActionListener
import java.sql.*;       // For DB connection
import java.awt.*;       // For colors, fonts, layout
import javax.swing.table.DefaultTableModel;

public class FinesGUI extends JFrame implements ActionListener {

    private JTable finesTable;
    private DefaultTableModel model;
    private JLabel totalFinesLabel;
    private JButton payFineButton, backButton;
    private String username;

    //Interface
    public FinesGUI(String username) {
        this.username = username;

        setTitle("My Fines");
        setSize(700, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = (JPanel) this.getContentPane();
        panel.setBackground(new Color(222, 184, 135));
        panel.setLayout(new BorderLayout());

        //Create JTable
        String[] columns = {"Fine ID", "Loan ID", "Amount", "Status", "Date Issued"};
        model = new DefaultTableModel(columns, 0);
        finesTable = new JTable(model);
        finesTable.setBackground(new Color(255, 240, 220));
        finesTable.setForeground(new Color(90, 50, 20));
        finesTable.setFont(new Font("Arial", Font.PLAIN, 14));
        finesTable.getTableHeader().setBackground(new Color(222, 184, 135));
        JScrollPane scrollPane = new JScrollPane(finesTable);

        payFineButton = new JButton("Pay Selected Fine");
        payFineButton.setBackground(new Color(245, 222, 179));
        payFineButton.setForeground(new Color(90, 50, 20));
        payFineButton.addActionListener(this);

        backButton = new JButton("Back");
        backButton.setBackground(new Color(245, 222, 179));
        backButton.setForeground(new Color(90, 50, 20));
        backButton.addActionListener(this);

        totalFinesLabel = new JLabel("Total unpaid fines: $0.00");
        totalFinesLabel.setForeground(new Color(101, 67, 33));
        totalFinesLabel.setFont(new Font("Arial", Font.BOLD, 12));

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
        bottomPanel.add(payFineButton);
        bottomPanel.add(backButton);
        bottomPanel.add(totalFinesLabel);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        // Load fines from database and display them
        loadFines();

        setVisible(true);
    }

    //add overdue fines for any loans that don't have fines yet
    private void addOverdueFines(String username) {
        try {
            Connection con = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
            Statement stmt = con.createStatement();

            // Find all loans of this user that are overdue, not returned, and no fine yet
            String sqlFindOverdue
                    = "SELECT loan_id FROM loans "
                    + "WHERE member_id = (SELECT member_id FROM members WHERE username = '" + username + "') "
                    + "AND due_date < date('now') "
                    + "AND status != 'returned' "
                    + "AND loan_id NOT IN (SELECT loan_id FROM fines);";

            ResultSet rs = stmt.executeQuery(sqlFindOverdue);

            // For each such loan, insert a new fine record            
            while (rs.next()) {
                int loanId = rs.getInt("loan_id");

                String insertFine
                        = "INSERT INTO fines (loan_id, amount, paid, fine_date) "
                        + "VALUES (" + loanId + ", 20.0, 0, date('now'));";

                stmt.executeUpdate(insertFine);
            }

            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //load fines from database and update the table and label
    private void loadFines() {
        model.setRowCount(0);
        //call addOverdueFines function to add fines
        addOverdueFines(username);
        double totalUnpaid = 0.0;

        try {
            Connection con = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
            Statement stmt = con.createStatement();

            // Select all fines for this user, joining with loans and members
            String sql = "SELECT f.fine_id, f.loan_id, f.amount, f.paid, f.fine_date "
                    + "FROM fines f "
                    + "JOIN loans l ON f.loan_id = l.loan_id "
                    + "JOIN members m ON l.member_id = m.member_id "
                    + "WHERE m.username = '" + username + "';";
            ResultSet rs = stmt.executeQuery(sql);

            // add each fine to the table and sum unpaid fines
            while (rs.next()) {
                int fineId = rs.getInt("fine_id");
                int loanId = rs.getInt("loan_id");
                double amount = rs.getDouble("amount");
                int paid = rs.getInt("paid");
                String fineDate = rs.getString("fine_date");

                String statusText = (paid == 0) ? "Unpaid" : "Paid";

                model.addRow(new Object[]{fineId, loanId, amount, statusText, fineDate});

                if (paid == 0) {
                    totalUnpaid += amount;
                }
            }

            //update the total unpaid fines label
            totalFinesLabel.setText(String.format("Total unpaid fines: $%.2f", totalUnpaid));

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading fines.");
        }
    }

    //actions for buttons
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == payFineButton) {
            paySelectedFine();
        } else if (e.getSource() == backButton) {
            new MemberPage(username);
            dispose();
        }
    }

    //pay selected fine from the table
    private void paySelectedFine() {
        int row = finesTable.getSelectedRow();
        //check if user selected a fine
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a fine to pay.");
            return;
        }

        //check if fine is already paid
        String status = (String) model.getValueAt(row, 3);
        if ("Paid".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "This fine is already paid.");
            return;
        }

        int fineId = (int) model.getValueAt(row, 0);

        try {
            Connection con = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
            Statement stmt = con.createStatement();
            // update fine as paid
            String sql = "UPDATE fines SET paid = 1 WHERE fine_id = " + fineId + ";";
            int updated = stmt.executeUpdate(sql);
            if (updated > 0) {
                JOptionPane.showMessageDialog(this, "Fine paid successfully.");
                loadFines(); // Refresh table
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update fine.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error.");
        }
    }

}
