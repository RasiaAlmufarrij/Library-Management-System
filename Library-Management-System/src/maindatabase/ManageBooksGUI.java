package maindatabase;

import javax.swing.*;    // For GUI components
import java.awt.event.*; // For ActionListener
import java.sql.*;       // For DB connection
import java.awt.*;       // For colors, fonts, layout
import javax.swing.table.DefaultTableModel;

public class ManageBooksGUI extends JFrame implements ActionListener {

    private JTextField titleField, authorField, isbnField, categoryField, copiesField;
    private JTable table;
    private DefaultTableModel model;
    private JButton addButton, updateButton, deleteButton, addCopiesButton, deleteCopiesButton, refreshButton, backButton;
    private String username;

    //Interface
    public ManageBooksGUI(String user) {
        this.username = user;

        setTitle("Manage Books");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = (JPanel) this.getContentPane();
        panel.setBackground(new Color(222, 184, 135));
        panel.setLayout(new BorderLayout());

        JPanel north = new JPanel(new GridLayout(2, 5));
        north.setBackground(new Color(222, 184, 135));

        JLabel titleLabel = new JLabel("Title:");
        titleLabel.setForeground(new Color(50, 30, 10));
        north.add(titleLabel);
        titleField = new JTextField();
        north.add(titleField);

        JLabel authorLabel = new JLabel("Author:");
        authorLabel.setForeground(new Color(50, 30, 10));
        north.add(authorLabel);

        authorField = new JTextField();
        north.add(authorField);

        JLabel isbnLabel = new JLabel("ISBN:");
        isbnLabel.setForeground(new Color(50, 30, 10));
        north.add(isbnLabel);
        isbnField = new JTextField();
        north.add(isbnField);

        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setForeground(new Color(50, 30, 10));
        north.add(categoryLabel);
        categoryField = new JTextField();
        north.add(categoryField);

        JLabel copiesLabel = new JLabel("Copies Number:");
        copiesLabel.setForeground(new Color(50, 30, 10));
        north.add(copiesLabel);
        copiesField = new JTextField("0");
        north.add(copiesField);
        panel.add(north, BorderLayout.NORTH);

        // table displaying books , in the middle 
        String[] columns = {"Book ID", "Title", "Author", "ISBN", "Category", "Available Copies"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);
        table.setBackground(new Color(255, 240, 220));
        table.setForeground(new Color(90, 50, 20));
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.getTableHeader().setBackground(new Color(222, 184, 135));
        JScrollPane scroll = new JScrollPane(table);
        panel.add(scroll, BorderLayout.CENTER);

        // buttons in the bottom 
        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        south.setBackground(new Color(222, 184, 135));
        addButton = new JButton("Add Book");
        addButton.setBackground(new Color(245, 222, 179));
        addButton.setForeground(new Color(90, 50, 20));
        addButton.addActionListener(this);
        south.add(addButton);

        updateButton = new JButton("Update Book");
        updateButton.setBackground(new Color(245, 222, 179));
        updateButton.setForeground(new Color(90, 50, 20));
        updateButton.addActionListener(this);
        south.add(updateButton);

        deleteButton = new JButton("Delete Book");
        deleteButton.setBackground(new Color(245, 222, 179));
        deleteButton.setForeground(new Color(90, 50, 20));
        deleteButton.addActionListener(this);
        south.add(deleteButton);

        addCopiesButton = new JButton("Add Copies");
        addCopiesButton.setBackground(new Color(245, 222, 179));
        addCopiesButton.setForeground(new Color(90, 50, 20));
        addCopiesButton.addActionListener(this);
        south.add(addCopiesButton);

        deleteCopiesButton = new JButton("Delete Copies");
        deleteCopiesButton.setBackground(new Color(245, 222, 179));
        deleteCopiesButton.setForeground(new Color(90, 50, 20));
        deleteCopiesButton.addActionListener(this);
        south.add(deleteCopiesButton);

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
        // load books from the database into the table
        loadBooks();
        setVisible(true);
    }

    //actions for buttons
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addButton) {
            addBook();
        } else if (e.getSource() == updateButton) {
            updateBook();
        } else if (e.getSource() == deleteButton) {
            deleteBook();
        } else if (e.getSource() == addCopiesButton) {
            addCopies();
        } else if (e.getSource() == deleteCopiesButton) {
            deleteCopies();
        } else if (e.getSource() == refreshButton) {
            loadBooks();
        } else if (e.getSource() == backButton) {
            new LibrarianPage(username);
            dispose();
        }
    }

    // displaying books from the database 
    private void loadBooks() {
        model.setRowCount(0);

        try {
            Connection con = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
            Statement stmt = con.createStatement();
            //get books from the database
            String sql = "SELECT b.book_id, b.title, b.author, b.isbn, b.category, "
                    + "(SELECT COUNT(*) FROM copies c WHERE c.book_id = b.book_id AND c.status = 'available') AS available_copies "
                    + "FROM books b;";
            ResultSet rs = stmt.executeQuery(sql);

            //add results to the table
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
            con.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading books: " + ex.getMessage());
        }
    }

    // adding a new book from user in the database
    private void addBook() {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String isbn = isbnField.getText().trim();
        String category = categoryField.getText().trim();
        String copiesText = copiesField.getText().trim();

        //check if user filled all the fields
        if (title.isEmpty() || author.isEmpty() || isbn.isEmpty() || category.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }
        int copiesToAdd = 0;
        try {
            copiesToAdd = Integer.parseInt(copiesText);
            //if the number of copies is a negative number then it is 0
            if (copiesToAdd < 0) {
                copiesToAdd = 0;
            }
            //only accept input if it was a number
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Copies number must be a number.");
            return;
        }

        try {
            Connection con = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
            Statement stmt = con.createStatement();
            // checking that the isbn is not repeated
            String checkSQL = "SELECT * FROM books WHERE isbn = '" + isbn + "';";
            ResultSet rsCheck = stmt.executeQuery(checkSQL);
            if (rsCheck.next()) {
                rsCheck.close();
                con.close();
                JOptionPane.showMessageDialog(this, "A book with this ISBN already exists.");
                return;
            }
            rsCheck.close();

            // entring a book
            String insertBookSQL = "INSERT INTO books (title, author, isbn, category) "
                    + "VALUES ('" + title + "', '" + author + "', '" + isbn + "', '" + category + "');";
            stmt.executeUpdate(insertBookSQL);

            // new book id 
            ResultSet rsId = stmt.executeQuery("SELECT last_insert_rowid() AS id;");
            int bookId = -1;
            if (rsId.next()) {
                bookId = rsId.getInt("id");
            }
            rsId.close();

            // adding copies to table
            for (int i = 0; i < copiesToAdd; i++) {
                String copySQL = "INSERT INTO copies (book_id, status) VALUES (" + bookId + ", 'available');";
                stmt.executeUpdate(copySQL);
            }
            con.close();
            JOptionPane.showMessageDialog(this, "Book added successfully!");
            clearFields();
            loadBooks();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding book: " + ex.getMessage());
        }
    }

    // updating a book's info
    private void updateBook() {
        int row = table.getSelectedRow();
        //check if user selected a book
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a book from the table first.");
            return;
        }

        int bookId = (int) model.getValueAt(row, 0);
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String isbn = isbnField.getText().trim();
        String category = categoryField.getText().trim();

        try {
            Connection con = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
            Statement stmt = con.createStatement();
            // take current book data from database
            String selectSQL = "SELECT title, author, isbn, category FROM books WHERE book_id = " + bookId + ";";
            ResultSet rs = stmt.executeQuery(selectSQL);

            String currentTitle = "";
            String currentAuthor = "";
            String currentIsbn = "";
            String currentCategory = "";

            if (rs.next()) {
                currentTitle = rs.getString("title");
                currentAuthor = rs.getString("author");
                currentIsbn = rs.getString("isbn");
                currentCategory = rs.getString("category");
            }
            rs.close();
            // Use input if provided, else keep current
            String finalTitle = title.isEmpty() ? currentTitle : title;
            String finalAuthor = author.isEmpty() ? currentAuthor : author;
            String finalIsbn = isbn.isEmpty() ? currentIsbn : isbn;
            String finalCategory = category.isEmpty() ? currentCategory : category;

            String updateSQL = "UPDATE books SET "
                    + "title = '" + finalTitle + "', "
                    + "author = '" + finalAuthor + "', "
                    + "isbn = '" + finalIsbn + "', "
                    + "category = '" + finalCategory + "' "
                    + "WHERE book_id = " + bookId + ";";
            stmt.executeUpdate(updateSQL);
            con.close();
            JOptionPane.showMessageDialog(this, "Book updated successfully!");
            loadBooks();
            clearFields();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating book: " + ex.getMessage());
        }
    }

    // deleting a book while checking for loans on that book
    private void deleteBook() {
        int row = table.getSelectedRow();
        //check if user selected a book
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a book from the table first.");
            return;
        }

        int bookId = (int) model.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this book?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            Connection con = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
            Statement stmt = con.createStatement();

            // checking for the active loans on copies, if there is it can't be deleted
            String checkLoanSQL = "SELECT COUNT(*) AS cnt "
                    + "FROM loans l JOIN copies c ON l.copy_id = c.copy_id "
                    + "WHERE c.book_id = " + bookId + " AND l.return_date IS NULL;";
            ResultSet rsLoan = stmt.executeQuery(checkLoanSQL);
            if (rsLoan.next() && rsLoan.getInt("cnt") > 0) {
                rsLoan.close();
                con.close();
                JOptionPane.showMessageDialog(this,
                        "Cannot delete book. There are active loans for this book.");
                return;
            }
            rsLoan.close();

            // deleting holds , copies , books
            stmt.executeUpdate("DELETE FROM holds WHERE book_id = " + bookId + ";");
            stmt.executeUpdate("DELETE FROM copies WHERE book_id = " + bookId + ";");
            stmt.executeUpdate("DELETE FROM books WHERE book_id = " + bookId + ";");
            con.close();
            JOptionPane.showMessageDialog(this, "Book deleted successfully!");
            loadBooks();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting book: " + ex.getMessage());
        }
    }

    // adding new copies to an existing book
    private void addCopies() {
        int row = table.getSelectedRow();
        //make sure user selected a copy
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a book from the table first.");
            return;
        }

        int bookId = (int) model.getValueAt(row, 0);
        String copiesText = copiesField.getText().trim();

        int copiesToAdd;
        try {
            copiesToAdd = Integer.parseInt(copiesText);
            //number of copies must be positive
            if (copiesToAdd <= 0) {
                JOptionPane.showMessageDialog(this, "Copies to Add must be > 0.");
                return;
            }
            //the input must be a number
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Copies to Add must be a number.");
            return;
        }

        try {
            Connection con = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
            Statement stmt = con.createStatement();
            //add the number of copies
            for (int i = 0; i < copiesToAdd; i++) {
                String copySQL = "INSERT INTO copies (book_id, status) VALUES (" + bookId + ", 'available');";
                stmt.executeUpdate(copySQL);
            }

            con.close();

            JOptionPane.showMessageDialog(this, "Copies added successfully!");
            loadBooks();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding copies: " + ex.getMessage());
        }
    }

    // delete available copies of a book
    private void deleteCopies() {
        int row = table.getSelectedRow();
        //make sure the user selected a book
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a book from the table first.");
            return;
        }

        int bookId = (int) model.getValueAt(row, 0);
        String copiesText = copiesField.getText().trim();

        int copiesToDelete;
        try {
            copiesToDelete = Integer.parseInt(copiesText);
            //number of copies must be more than 0
            if (copiesToDelete <= 0) {
                JOptionPane.showMessageDialog(this, "Copies to delete must be > 0.");
                return;
            }
            //input must be a number
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Copies to delete must be a number.");
            return;
        }

        try {
            Connection con = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
            Statement stmt = con.createStatement();

            // counting available copies for this book
            String countSQL = "SELECT COUNT(*) AS cnt FROM copies "
                    + "WHERE book_id = " + bookId + " AND status = 'available';";
            ResultSet rsCount = stmt.executeQuery(countSQL);
            int available = 0;
            if (rsCount.next()) {
                available = rsCount.getInt("cnt");
            }
            rsCount.close();
            //check the copies to be deleted is more than the available copies
            if (copiesToDelete > available) {
                con.close();
                JOptionPane.showMessageDialog(this,
                        "Not enough available copies to delete.\nAvailable: " + available);
                return;
            }

            // delete copies 
            String delSQL = "DELETE FROM copies WHERE copy_id IN ("
                    + "SELECT copy_id FROM copies "
                    + "WHERE book_id = " + bookId + " AND status = 'available' "
                    + "LIMIT " + copiesToDelete
                    + ");";
            stmt.executeUpdate(delSQL);

            con.close();

            JOptionPane.showMessageDialog(this, "Copies deleted successfully!");
            loadBooks();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting copies: " + ex.getMessage());
        }
    }

    //clear all fields
    private void clearFields() {
        titleField.setText("");
        authorField.setText("");
        isbnField.setText("");
        categoryField.setText("");
        copiesField.setText("0");
    }
}
