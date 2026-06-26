
package maindatabase;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.*;
public class CreateAllTables {
   public CreateAllTables() {
        try {
            Connection con = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
            Statement stmt = con.createStatement();

            // Create books table
            String booksTable = "CREATE TABLE IF NOT EXISTS books (" +
                    "book_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "title TEXT NOT NULL, " +
                    "author TEXT NOT NULL, " +
                    "isbn TEXT UNIQUE NOT NULL, " +
                    "category TEXT NOT NULL" +
                    ");";
            stmt.executeUpdate(booksTable);

            // Create copies table
            String copiesTable = "CREATE TABLE IF NOT EXISTS copies (" +
                    "copy_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "book_id INTEGER NOT NULL, " +
                    "status TEXT NOT NULL, " +
                    "FOREIGN KEY(book_id) REFERENCES books(book_id)" +
                    ");";
            stmt.executeUpdate(copiesTable);

            // Create members table
            String membersTable = "CREATE TABLE IF NOT EXISTS members (" +
                    "member_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL" +
                    ");";
            stmt.executeUpdate(membersTable);

            // Create librarians table
            String librariansTable = "CREATE TABLE IF NOT EXISTS librarians (" +
                    "librarian_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL" +
                    ");";
            stmt.executeUpdate(librariansTable);

            // Create loans table
            String loansTable = "CREATE TABLE IF NOT EXISTS loans (" +
                    "loan_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "copy_id INTEGER NOT NULL, " +
                    "member_id INTEGER NOT NULL, " +
                    "loan_date DATE NOT NULL, " +
                    "due_date DATE NOT NULL, " +
                    "return_date DATE, " +
                    "FOREIGN KEY(copy_id) REFERENCES copies(copy_id), " +
                    "FOREIGN KEY(member_id) REFERENCES members(member_id)" +
                    ");";
            stmt.executeUpdate(loansTable);

            // Create fines table
            String finesTable = "CREATE TABLE IF NOT EXISTS fines (" +
                    "fine_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "loan_id INTEGER NOT NULL, " +
                    "amount REAL NOT NULL, " +
                    "paid BOOLEAN NOT NULL CHECK (paid IN (0,1)), " +
                    "fine_date DATE NOT NULL, " +
                    "FOREIGN KEY(loan_id) REFERENCES loans(loan_id)" +
                    ");";
            stmt.executeUpdate(finesTable);

            // Create holds table
            String holdsTable = "CREATE TABLE IF NOT EXISTS holds (" +
                "hold_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "book_id INTEGER NOT NULL, " +
                "member_id INTEGER NOT NULL, " +
                "hold_date DATE NOT NULL, " +
                "position INTEGER NOT NULL, " +
                "FOREIGN KEY(book_id) REFERENCES books(book_id), " +
                "FOREIGN KEY(member_id) REFERENCES members(member_id)" +
                ");";
            stmt.executeUpdate(holdsTable);

            System.out.println("All tables created successfully!");

            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
