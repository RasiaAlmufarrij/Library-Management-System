package maindatabase;

import javax.swing.*;    // For GUI components
import java.awt.event.*; // For ActionListener
import java.sql.*;       // For DB connection
import java.awt.*;       // For colors, fonts, layout
import javax.swing.table.DefaultTableModel;

public class ManageReservationsGUI extends JFrame implements ActionListener {

    private JTable table;
    private DefaultTableModel model;
    private JButton deleteButton, refreshButton, backButton;
    private String username;

    //Interface
    public ManageReservationsGUI(String user) {
        this.username = user;

        setTitle("Manage Reservations");
        setSize(700, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = (JPanel) this.getContentPane();
        panel.setBackground(new Color(222, 184, 135));
        panel.setLayout(new BorderLayout());

        String[] cols = {"Hold ID", "Member", "Book Title", "Hold Date", "Position"};
        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);
        table.setBackground(new Color(255, 240, 220));
        table.setForeground(new Color(90, 50, 20));
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.getTableHeader().setBackground(new Color(222, 184, 135));
        JScrollPane scroll = new JScrollPane(table);

        panel.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.setBackground(new Color(222, 184, 135));

        deleteButton = new JButton("Delete Reservation");
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

        bottom.add(deleteButton);
        bottom.add(refreshButton);
        bottom.add(backButton);

        panel.add(bottom, BorderLayout.SOUTH);

        loadReservations();
        setVisible(true);
    }
    
    // Loads all reservations from the database into the table
    private void loadReservations() {
        model.setRowCount(0);

        try {
            Connection con = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
            Statement stmt = con.createStatement();
            //query to get all the reservations info
            String sql =
            "SELECT h.hold_id, m.username, b.title, h.hold_date, h.position " +
            "FROM holds h " +
            "JOIN members m ON h.member_id = m.member_id " +
            "JOIN books b ON b.book_id = h.book_id " +
            "ORDER BY h.hold_date DESC;";
            ResultSet rs = stmt.executeQuery(sql);
            
            //loop through each reservation and add it to the table
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("hold_id"),
                    rs.getString("username"),
                    rs.getString("title"),
                    rs.getString("hold_date"),
                    rs.getInt("position")
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //actions for buttons
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == deleteButton) {
            int row = table.getSelectedRow();
            //check if user selected a reservation
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a reservation first.");
                return;
            }

            int holdId = (int) model.getValueAt(row, 0);

            try {
                Connection con = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
                Statement stmt = con.createStatement();
                
                //delete a reservation from database
                stmt.executeUpdate("DELETE FROM holds WHERE hold_id = " + holdId + ";");
                JOptionPane.showMessageDialog(this, "Reservation deleted.");
                loadReservations();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        else if (e.getSource() == refreshButton) {
            loadReservations();
        }

        else if (e.getSource() == backButton) {
            new LibrarianPage(username);
            dispose();
        }
    }
}
