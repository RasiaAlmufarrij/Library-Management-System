package maindatabase;

import javax.swing.*;    // For GUI components
import java.awt.event.*; // For ActionListener
import java.sql.*;       // For DB connection
import java.awt.*;       // For colors, fonts, layout
import javax.swing.table.DefaultTableModel;

public class ReservationsGUI extends JFrame implements ActionListener {

    private String username;
    private JTable table;
    private DefaultTableModel model;
    private JButton refreshButton, cancelHoldButton, backButton;

    //Interface
    public ReservationsGUI(String username) {
        this.username = username;

        setTitle("My Reservations");
        setSize(700, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        JPanel panel = (JPanel) this.getContentPane();
        panel.setBackground(new Color(222, 184, 135));
        panel.setLayout(new BorderLayout());

        // create JTable
        String[] columns = {"Hold ID", "Book Title", "Hold Date", "Position"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);
        table.setBackground(new Color(255, 240, 220));
        table.setForeground(new Color(90, 50, 20));
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.getTableHeader().setBackground(new Color(222, 184, 135));
        JScrollPane scrollPane = new JScrollPane(table);

        refreshButton = new JButton("Refresh");
        refreshButton.setBackground(new Color(245, 222, 179));
        refreshButton.setForeground(new Color(90, 50, 20));
        cancelHoldButton = new JButton("Cancel Hold");
        cancelHoldButton.setBackground(new Color(245, 222, 179));
        cancelHoldButton.setForeground(new Color(90, 50, 20));
        backButton = new JButton("Back");
        backButton.setBackground(new Color(245, 222, 179));
        backButton.setForeground(new Color(90, 50, 20));

        refreshButton.addActionListener(this);
        cancelHoldButton.addActionListener(this);
        backButton.addActionListener(this);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        buttonPanel.add(refreshButton);
        buttonPanel.add(cancelHoldButton);
        buttonPanel.add(backButton);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Load reservations and check notifications
        loadReservations();

        setVisible(true);
    }

    // Load the reservations (holds) of the current user
    private void loadReservations() {
        model.setRowCount(0);
        try {
            Connection con = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
            Statement stmt = con.createStatement();

            // get member id
            String getMemberIdSQL = "SELECT member_id FROM members WHERE username = '" + username + "';";
            ResultSet rsMember = stmt.executeQuery(getMemberIdSQL);

            int memberId = rsMember.getInt("member_id");
            rsMember.close();

            // Load holds and book titles
            String sql = "SELECT h.hold_id, b.title, h.hold_date, h.position "
                    + "FROM holds h JOIN books b ON h.book_id = b.book_id "
                    + "WHERE h.member_id = " + memberId + " "
                    + "ORDER BY h.hold_date DESC;";
            ResultSet rs = stmt.executeQuery(sql);

            //add results to table
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("hold_id"),
                    rs.getString("title"),
                    rs.getString("hold_date"),
                    rs.getInt("position")
                });
            }
            rs.close();

            // Check if the user has any unseen notifications
            checkNotifications(memberId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Check for notifications, if the book is available
    private void checkNotifications(int memberId) {
        try {
            Connection con = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
            Statement stmt = con.createStatement();

            // Get unseen notifications for this user
            String notifSQL = "SELECT notification_id, message FROM notifications WHERE member_id = " + memberId + " AND seen = 0;";
            ResultSet rsNotif = stmt.executeQuery(notifSQL);

            // Loop through notifications
            while (rsNotif.next()) {
                int notifId = rsNotif.getInt("notification_id");
                String message = rsNotif.getString("message");

                // Show popup message
                JOptionPane.showMessageDialog(this, message, "Notification", JOptionPane.INFORMATION_MESSAGE);

                // Mark notification as seen
                String updateSQL = "UPDATE notifications SET seen = 1 WHERE notification_id = " + notifId + ";";
                stmt.executeUpdate(updateSQL);
            }
            rsNotif.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //actions for buttons
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == refreshButton) {
            loadReservations();
        } else if (e.getSource() == cancelHoldButton) {
            cancelSelectedHold();
        } else if (e.getSource() == backButton) {
            new MemberPage(username);
            dispose();
        }
    }

    // Cancel a selected hold
    private void cancelSelectedHold() {
        int row = table.getSelectedRow();
        //check if user selected a hold
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a hold to cancel.");
            return;
        }

        int holdId = (int) model.getValueAt(row, 0);

        try {
            Connection con = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
            Statement stmt = con.createStatement();

            // Delete the hold from the database
            String deleteSQL = "DELETE FROM holds WHERE hold_id = " + holdId + ";";
            stmt.executeUpdate(deleteSQL);

            JOptionPane.showMessageDialog(this, "Hold cancelled.");
            loadReservations();//refresh the table

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
