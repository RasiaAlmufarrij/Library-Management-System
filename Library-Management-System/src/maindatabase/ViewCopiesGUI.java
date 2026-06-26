
package maindatabase;

import java.sql.*;
import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;
public class ViewCopiesGUI extends JFrame {

    private JTable table;
    private DefaultTableModel model;
    private int bookId;

    public ViewCopiesGUI(int bookId) {
        this.bookId = bookId;

        setTitle("Copies of Book ID: " + bookId);
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        JPanel panel = (JPanel) this.getContentPane();
        panel.setLayout(new BorderLayout());
        String[] columns={"Copy ID", "Status"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);
        table.setBackground(new Color(255, 240, 220)); 
        table.setForeground(new Color(90, 50, 20));    
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.getTableHeader().setBackground(new Color(222, 184, 135));   
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.getTableHeader().setBackground(new Color(222, 184, 135));
        table.getTableHeader().setForeground(new Color(90, 50, 20));
        JScrollPane scrollPane = new JScrollPane(table);


        panel.add(scrollPane, BorderLayout.CENTER);

        loadCopies();

        setVisible(true);
    }

    private void loadCopies() {
        model.setRowCount(0);  // Clear existing rows

        String sql = "SELECT copy_id, status FROM copies WHERE book_id = " + bookId;

        try{
             Connection con = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int copyId = rs.getInt("copy_id");
                String status = rs.getString("status");

                model.addRow(new Object[]{copyId, status});
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading copies: " + ex.getMessage());
        }
    }
}
