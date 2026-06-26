package maindatabase;

import javax.swing.*;     // For GUI components
import java.awt.event.*;  // For ActionListener
import java.sql.*;        // For DB connection
import java.awt.*;        // For colors, fonts, layout
import java.io.FileWriter;//  For writing the CSV file
import java.util.ArrayList;//For storing table rows in memory before showing/exporting

public class ReportsGUI extends JFrame implements ActionListener {

    private JButton overdueBtn, mostBorrowedBtn, finesBtn, exportBtn, backBtn;
    private JTextArea reportArea;
    private String username;
    private ArrayList<String[]> lastReport;
    private String[] lastHeaders;

    //Interface
    public ReportsGUI(String user) {
        this.username = user;

        setTitle("Library Reports");
        setSize(600, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(222, 184, 135));

        JPanel topBtns = new JPanel(new GridLayout(1, 4));
        topBtns.setBackground(new Color(222, 184, 135));

        overdueBtn = new JButton("Overdue Loans");
        overdueBtn.setBackground(new Color(245, 222, 179));
        overdueBtn.setForeground(new Color(90, 50, 20));
        overdueBtn.addActionListener(this);
        
        mostBorrowedBtn = new JButton("Most Borrowed");
        mostBorrowedBtn.setBackground(new Color(245, 222, 179));
        mostBorrowedBtn.setForeground(new Color(90, 50, 20));
        mostBorrowedBtn.addActionListener(this);
                
        finesBtn = new JButton("Active Fines");
        finesBtn.setBackground(new Color(245, 222, 179));
        finesBtn.setForeground(new Color(90, 50, 20));
        finesBtn.addActionListener(this);
        
        exportBtn = new JButton("Export CSV");
        exportBtn.setBackground(new Color(245, 222, 179));
        exportBtn.setForeground(new Color(90, 50, 20));
        exportBtn.addActionListener(this);


        topBtns.add(overdueBtn);
        topBtns.add(mostBorrowedBtn);
        topBtns.add(finesBtn);
        topBtns.add(exportBtn);

        reportArea = new JTextArea();
        reportArea.setFont(new Font("Arial", Font.PLAIN, 14));
        reportArea.setEditable(false);

        backBtn = new JButton("Back");
        backBtn.setBackground(new Color(245, 222, 179));
        backBtn.setForeground(new Color(90, 50, 20));
        backBtn.addActionListener(this);

        panel.add(topBtns, BorderLayout.NORTH);
        panel.add(new JScrollPane(reportArea), BorderLayout.CENTER);
        panel.add(backBtn, BorderLayout.SOUTH);

        add(panel);
        setVisible(true);
    }

  
    // gets ALL loans that are overdue
    private ArrayList<String[]> getOverdueLoans() throws Exception {
        ArrayList<String[]> data = new ArrayList<>();

        try {
            Connection con = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
            Statement st = con.createStatement();

           //query to get all the loans of each member
            String sql =
                    "SELECT loans.loan_id, books.title, members.username, loans.due_date " +
                    "FROM loans " +
                    "JOIN copies ON loans.copy_id = copies.copy_id " +
                    "JOIN books ON copies.book_id = books.book_id " +
                    "JOIN members ON loans.member_id = members.member_id " +
                    "WHERE loans.return_date IS NULL AND loans.due_date < DATE('now');";

            ResultSet rs = st.executeQuery(sql);

            // Fill ArrayList with rows
            while (rs.next()) {
                data.add(new String[]{
                        rs.getString("loan_id"),
                        rs.getString("title"),
                        rs.getString("username"),
                        rs.getString("due_date")
                });
            }
        } catch (Exception ex) {
            throw new Exception("Error loading overdue loans.");
        }

        return data;
    }


    //get the most borrowed books
    private ArrayList<String[]> getMostBorrowedBooks() throws Exception {
        ArrayList<String[]> data = new ArrayList<>();

        try {
            Connection con = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
            Statement st = con.createStatement();

            // This query groups by book title to count how many loans used each book
            String sql =
                    "SELECT books.title, COUNT(loans.loan_id) AS borrow_count " +
                    "FROM loans " +
                    "JOIN copies ON loans.copy_id = copies.copy_id " +
                    "JOIN books ON copies.book_id = books.book_id " +
                    "GROUP BY books.title " +
                    "ORDER BY borrow_count DESC;";

            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                data.add(new String[]{
                        rs.getString("title"),
                        rs.getString("borrow_count")
                });
            }
        } catch (Exception ex) {
            throw new Exception("Error loading most borrowed books.");
        }

        return data;
    }


    // get ALL the unpaid fines
    private ArrayList<String[]> getActiveFines() throws Exception {
        ArrayList<String[]> data = new ArrayList<>();

        try {
            Connection con = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
            Statement st = con.createStatement();

            // Joining fines with loans and members so we can show username and fine amount
            String sql =
                    "SELECT members.username, fines.amount, fines.fine_date " +
                    "FROM fines " +
                    "JOIN loans ON fines.loan_id = loans.loan_id " +
                    "JOIN members ON loans.member_id = members.member_id " +
                    "WHERE fines.paid = 0;";

            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                data.add(new String[]{
                        rs.getString("username"),
                        rs.getString("amount"),
                        rs.getString("fine_date")
                });
            }
        } catch (Exception ex) {
            throw new Exception("Error loading fines.");
        }

        return data;
    }


    // Shows the report in the text area
    private void showReport(String title, String[] headers, ArrayList<String[]> data) {
        lastReport = data;
        lastHeaders = headers;

        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(title).append(" ===\n\n");

        for (String[] row : data) {
            sb.append(String.join(" | ", row)).append("\n");
        }

        reportArea.setText(sb.toString());
    }


    // Exports last shown report to CSV
    private void exportCSVFile() {
        if (lastReport == null || lastHeaders == null) {
            JOptionPane.showMessageDialog(this, "No report to export.");
            return;
        }

        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new java.io.File("report.csv"));
            int result = chooser.showSaveDialog(this);
            if (result != JFileChooser.APPROVE_OPTION) return;

            FileWriter fw = new FileWriter(chooser.getSelectedFile());

            fw.write(String.join(",", lastHeaders) + "\n");

            for (String[] row : lastReport) {
                fw.write(String.join(",", row) + "\n");
            }

            fw.close();
            JOptionPane.showMessageDialog(this, "Exported Successfully!");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error exporting CSV.");
        }
    }


    // ================================================================
    // BUTTON LISTENER
    // ================================================================

    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getSource() == overdueBtn)
                showReport("Overdue Loans",
                        new String[]{"Loan ID", "Title", "Username", "Due Date"},
                        getOverdueLoans());

            else if (e.getSource() == mostBorrowedBtn)
                showReport("Most Borrowed Books",
                        new String[]{"Title", "Borrow Count"},
                        getMostBorrowedBooks());

            else if (e.getSource() == finesBtn)
                showReport("Active Fines",
                        new String[]{"Username", "Amount", "Date"},
                        getActiveFines());

            else if (e.getSource() == exportBtn)
                exportCSVFile();

            else if (e.getSource() == backBtn) {
                new LibrarianPage(username);
                dispose();
            }

        } catch (Exception ex) {
            reportArea.setText("Error loading report.");
        }
    }
}


