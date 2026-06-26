package maindatabase;
/*
SQLITE-JDBC JAR file is in the folder of the project,
add it to the libraries of the this project
 */
public class MainDataBase {

    public static void main(String[] args) {
        /*
        accounts for trial
        Member1 username: M12345
               Password: 123456

        Member2 username: M12121
               Password: 123456
        
        Member3 username: M11223
               Password: 123456
        
        Librarian username: L12345
                  Password: 123456
         */

//we Only ran this class once to create the tables for the database, no need to run it again
//new CreateAllTables();

// ******run the code to start the app*****
        new LoginGUI();

    }
}
