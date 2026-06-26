package maindatabase;

import javax.swing.*;    // For GUI components
import java.awt.event.*; // For ActionListener
import java.sql.*;       // For DB connection
import java.awt.*;       // For colors, fonts, layout
import javax.swing.table.DefaultTableModel;

public class SearchBooksGUI extends JFrame implements ActionListener {

    private JTextField searchField;
    private JComboBox filterCombo;
    private JButton searchButton, viewCopiesButton, borrowButton, holdButton, backButton;//new edit
    private JTable table;
    private DefaultTableModel model;
    private String username;

    //Interface
    public SearchBooksGUI(String user) {
        this.username = user;

        setTitle("Search Books");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = (JPanel) this.getContentPane();
        panel.setBackground(new Color(222, 184, 135));
        panel.setLayout(new BorderLayout());

        JPanel north = new JPanel(new FlowLayout(FlowLayout.CENTER));
        north.setBackground(new Color(222, 184, 135));
        JLabel label = new JLabel("Search by:");
        north.add(label);

        String[] filters = {"title", "author", "isbn", "category"};
        filterCombo = new JComboBox(filters);
        north.add(filterCombo);

        searchField = new JTextField(30);
        north.add(searchField);

        searchButton = new JButton("Search");
        searchButton.setBackground(new Color(245, 222, 179));
        searchButton.setForeground(new Color(90, 50, 20));
        searchButton.addActionListener(this);
        north.add(searchButton);
        panel.add(north, BorderLayout.NORTH);

        //creating JTable 
        String[] columns = {"Book ID", "Title", "Author", "ISBN", "Category", "Available Copies"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);
        table.setBackground(new Color(255, 240, 220));
        table.setForeground(new Color(90, 50, 20));
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.getTableHeader().setBackground(new Color(222, 184, 135));
        table.getTableHeader().setForeground(new Color(90, 50, 20));
        table.getSelectionModel().addListSelectionListener(e -> {
            borrowButton.setEnabled(true);
            holdButton.setEnabled(true);
        });
        JScrollPane scroll = new JScrollPane(table);

        panel.add(scroll, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        south.setBackground(new Color(222, 184, 135));

        viewCopiesButton = new JButton("View Copies");
        viewCopiesButton.setBackground(new Color(245, 222, 179));
        viewCopiesButton.setForeground(new Color(90, 50, 20));
        viewCopiesButton.addActionListener(this);
        south.add(viewCopiesButton);

        borrowButton = new JButton("Borrow Book");
        borrowButton.setBackground(new Color(245, 222, 179));
        borrowButton.setForeground(new Color(90, 50, 20));
        borrowButton.addActionListener(this);
        south.add(borrowButton);

        holdButton = new JButton("Place Hold");
        holdButton.setBackground(new Color(245, 222, 179));
        holdButton.setForeground(new Color(90, 50, 20));
        holdButton.addActionListener(this);
        south.add(holdButton);

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

        if (e.getSource() == searchButton) {
            searchBooks();
        } else if (e.getSource() == viewCopiesButton) {
            showCopiesWindow();
        } else if (e.getSource() == borrowButton) {
            borrowBook();
        } else if (e.getSource() == holdButton) {
            placeHold();
        } else if (e.getSource() == backButton) {
            new MemberPage(username);
            dispose();
        }
    }

    // search books function
    private void searchBooks() {
        model.setRowCount(0);

        String filter = filterCombo.getSelectedItem().toString();
        String input = searchField.getText().trim();

        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Enter a search value.");
            return;
        }

        try {

            Connection con = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
            Statement stmt = con.createStatement();

            // SQL query using the selected filter
            String sql
                    = "SELECT b.book_id, b.title, b.author, b.isbn, b.category, "
                    + "(SELECT COUNT(*) FROM copies c WHERE c.book_id = b.book_id AND c.status = 'available') AS available_copies "
                    + "FROM books b WHERE " + filter + " LIKE '%" + input + "%';";
            ResultSet rs = stmt.executeQuery(sql);

            // Add results to table
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("book_id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("isbn"),
                    rs.getString("category"),
                    rs.getInt("available_copies")
                });
            }

            rs.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // view copies of selected book
    private void showCopiesWindow() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a book first.");
            return;
        }

        int bookId = (int) model.getValueAt(row, 0);// get book id from table

        new ViewCopiesGUI(bookId);
    }

    // borrow selected book
    private void borrowBook() {

        int row = table.getSelectedRow();

        //make sure user selected a book
        if (row == -1) {
            JOptionPane.showMessageDialog(null, "Select a book first.");
            return;
        }
        int available = (int) model.getValueAt(row, 5);

        //no copies available
        if (available == 0) {
            JOptionPane.showMessageDialog(this,
                    "No copies available to borrow.\nBut you CAN place a hold.");
            borrowButton.setEnabled(false);
            return;
        }
        int bookId = (int) model.getValueAt(row, 0);

        try {
            Connection con = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
            Statement stmt = con.createStatement();

            // Find one available copy
            String findCopySQL
                    = "SELECT copy_id FROM copies WHERE book_id = " + bookId
                    + " AND status = 'available' LIMIT 1;";

            ResultSet rs = stmt.executeQuery(findCopySQL);

            //if no copies are found output a message
            if (!rs.next()) {
                JOptionPane.showMessageDialog(null, "No copies available!");
                return;
            }

            int copyId = rs.getInt("copy_id");
            rs.close();

            // Mark copy as borrowed
            String updateCopySQL
                    = "UPDATE copies SET status = 'borrowed' WHERE copy_id = " + copyId + ";";

            stmt.executeUpdate(updateCopySQL);

            // Insert loan record
            String insertLoanSQL
                    = "INSERT INTO loans (member_id, copy_id, loan_date, due_date) "
                    + "VALUES ((SELECT member_id FROM members WHERE username = '" + username + "'), "
                    + copyId + ", date('now'), date('now','+14 days'));";

            stmt.executeUpdate(insertLoanSQL);

            JOptionPane.showMessageDialog(null, "Book borrowed successfully!");
            searchBooks(); //refresh table

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    //place hold (waiting list)
    private void placeHold() {
        int row = table.getSelectedRow();

        //make sure user selected a book
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a book first.");
            return;
        }

        int available = (int) model.getValueAt(row, 5);

        // If copies are available user doesn't need to hold
        if (available > 0) {
            JOptionPane.showMessageDialog(this,
                    "Copies are available to borrow.\nYou don't need to place a hold.");
            holdButton.setEnabled(false);
            return;
        }
        int bookId = (int) model.getValueAt(row, 0);

        try {
            Connection con = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
            Statement stmt = con.createStatement();

            //get member id
            String memberSQL = "SELECT member_id FROM members WHERE username = '" + username + "';";
            ResultSet rsMember = stmt.executeQuery(memberSQL);
            int memberId = rsMember.getInt("member_id");
            rsMember.close();

            //calculate next position in queue
            String posSQL
                    = "SELECT COALESCE(MAX(position), 0) + 1 AS nextPos "
                    + "FROM holds WHERE book_id = " + bookId + ";";

            ResultSet rsPos = stmt.executeQuery(posSQL);
            rsPos.next();
            int position = rsPos.getInt("nextPos");
            rsPos.close();

            // Check if already placed a hold
            String checkHoldSQL = "SELECT * FROM holds WHERE book_id = ? AND member_id = ?;";
            try (PreparedStatement pst = con.prepareStatement(checkHoldSQL)) {
                pst.setInt(1, bookId);
                pst.setInt(2, memberId);
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "You already have a hold on this book.");
                    return;
                }
            }

            // Insert hold record
            String insertSQL
                    = "INSERT INTO holds (book_id, member_id, hold_date, position) VALUES ("
                    + bookId + ", " + memberId + ", date('now'), " + position + ");";

            stmt.executeUpdate(insertSQL);

            JOptionPane.showMessageDialog(this,
                    "Hold placed successfully!\nYour position in queue: " + position);
            searchBooks(); //refresh

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

}
