package maindatabase;

import javax.swing.*;    // For GUI components
import java.awt.event.*; // For ActionListener
import java.sql.*;       // For DB connection
import java.awt.*;       // For colors, fonts, layout
import javax.swing.table.DefaultTableModel;

public class ManageFinesGUI extends JFrame implements ActionListener {

    private JTable table;
    private DefaultTableModel model;
    private JButton updateButton, deleteButton, refreshButton, backButton;
    private String username;
//Interface

    public ManageFinesGUI(String user) {
        this.username = user;

        setTitle("Manage Fines");
        setSize(650, 380);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = (JPanel) getContentPane();
        panel.setBackground(new Color(222, 184, 135));
        panel.setLayout(new BorderLayout());

        model = new DefaultTableModel(
                new String[]{"Fine ID", "Member", "Amount", "Status"}, 0);

        table = new JTable(model);
        table.setBackground(new Color(255, 240, 220));
        table.setForeground(new Color(90, 50, 20));
        table.getTableHeader().setBackground(new Color(222, 184, 135));

        JScrollPane scroll = new JScrollPane(table);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout());
        bottom.setBackground(new Color(222, 184, 135));

        updateButton = new JButton("Toggle Status");
        updateButton.setBackground(new Color(245, 222, 179));
        updateButton.setForeground(new Color(90, 50, 20));
        updateButton.addActionListener(this);

        deleteButton = new JButton("Delete Fine");
        deleteButton.setBackground(new Color(245, 222, 179));
        deleteButton.setForeground(new Color(90, 50, 20));
        deleteButton.addActionListener(this);

        refreshButton = new JButton("Refresh");
        refreshButton.setBackground(new Color(245, 222, 179));
        refreshButton.setForeground(new Color(90, 50, 20));
        refreshButton.addActionListener(this);

        backButton = new JButton("Back");
        backButton.setBackground(new Color(245, 222, 179));
        backButton.setForeground(new Color(90, 50, 20));
        backButton.addActionListener(this);

        bottom.add(updateButton);
        bottom.add(deleteButton);
        bottom.add(refreshButton);
        bottom.add(backButton);

        panel.add(bottom, BorderLayout.SOUTH);

        loadFines();
        setVisible(true);
    }

    // load all fines from fines table
    private void loadFines() {
        model.setRowCount(0);

        try {
            Connection con = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
            Statement stmt = con.createStatement();
            // query to get fines for each memeber
            String sql
                    = "SELECT f.fine_id, m.username, f.amount, f.paid "
                    + "FROM fines f "
                    + "JOIN loans l ON f.loan_id = l.loan_id "
                    + "JOIN members m ON l.member_id = m.member_id;";
            ResultSet rs = stmt.executeQuery(sql);

            // load each row into table
            while (rs.next()) {
                int paidValue = rs.getInt("paid");
                String paidStatus = (paidValue == 1) ? "Paid" : "Unpaid";
                model.addRow(new Object[]{
                    rs.getInt("fine_id"),
                    rs.getString("username"),
                    rs.getDouble("amount"),
                    paidStatus
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //actions for buttons
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == updateButton) {
            int row = table.getSelectedRow();
            //check if the user selected a fine
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a fine first.");
                return;
            }

            int fineId = (int) model.getValueAt(row, 0);
            String current = (String) model.getValueAt(row, 3);
            // convert to numeric value 1/0 to paid/unpaid
            String newStatus = current.equals("paid") ? "unpaid" : "paid";
            try {
                Connection con = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
                Statement stmt = con.createStatement();
                //update the fine status
                stmt.executeUpdate("UPDATE fines SET status='" + newStatus + "' WHERE fine_id=" + fineId + ";");
                loadFines();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } // delete selected fine
        else if (e.getSource() == deleteButton) {

            int row = table.getSelectedRow();
            //check if the user selected a fine
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a fine first.");
                return;
            }

            int fineId = (int) model.getValueAt(row, 0);

            try {
                Connection con = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
                Statement stmt = con.createStatement();
                stmt.executeUpdate("DELETE FROM fines WHERE fine_id=" + fineId + ";");
                loadFines();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } //reload data
        else if (e.getSource() == refreshButton) {
            loadFines();
        } //return to librarian page
        else if (e.getSource() == backButton) {
            new LibrarianPage(username);
            dispose();
        }
    }
}
