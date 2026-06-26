package maindatabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class LibrarianPage extends JFrame implements ActionListener {
    private JLabel l1;
    private JButton BooksButton, membersButton, reservationsButton, borrowReturnButton, finesButton, ReportsButton, logoutButton;
    private String username;
    
    public LibrarianPage(String user) {
        this.username=user;
        setTitle("Librarian Dashboard");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        JPanel panel = (JPanel) this.getContentPane();
        panel.setBackground(new Color(222, 184, 135)); 
        panel.setLayout(new GridLayout(8, 1, 5, 5));

        l1 = new JLabel("Pick a service:");
        l1.setForeground(new Color(101, 67, 33));
        l1.setFont(new Font("Arial", Font.BOLD, 18));
        l1.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(l1);

        BooksButton = new JButton("Books");
        BooksButton.setBackground(new Color(245, 222, 179));
        BooksButton.setForeground(new Color(90, 50, 20));  
        BooksButton.addActionListener(this);
        panel.add(BooksButton);

        membersButton = new JButton("Members");
        membersButton.setBackground(new Color(245, 222, 179));
        membersButton.setForeground(new Color(90, 50, 20)); 
        membersButton.addActionListener(this);
        panel.add(membersButton);
        
        borrowReturnButton = new JButton("borrow/Returns");
        borrowReturnButton.setBackground(new Color(245, 222, 179));
        borrowReturnButton.setForeground(new Color(90, 50, 20)); 
        borrowReturnButton.addActionListener(this);
        panel.add(borrowReturnButton);

        reservationsButton = new JButton("Reservations");
        reservationsButton.setBackground(new Color(245, 222, 179));
        reservationsButton.setForeground(new Color(90, 50, 20)); 
        reservationsButton.addActionListener(this);
        panel.add(reservationsButton);

        finesButton = new JButton("Fines");
        finesButton.setBackground(new Color(245, 222, 179));
        finesButton.setForeground(new Color(90, 50, 20)); 
        finesButton.addActionListener(this);
        panel.add(finesButton);
        
        ReportsButton = new JButton("Reports");
        ReportsButton.setBackground(new Color(245, 222, 179));
        ReportsButton.setForeground(new Color(90, 50, 20)); 
        ReportsButton.addActionListener(this);
        panel.add(ReportsButton);

        logoutButton = new JButton("Log Out");
        logoutButton.setBackground(new Color(245, 222, 179));
        logoutButton.setForeground(new Color(90, 50, 20)); 
        logoutButton.addActionListener(this);
        panel.add(logoutButton);

        setVisible(true);
    }


public void actionPerformed(ActionEvent e) {

    if (e.getSource() == BooksButton) {
        new ManageBooksGUI(username);
        dispose();
    }

    else if (e.getSource() == membersButton) {
        new ManageMembersGUI(username);
        dispose();
    }

    else if (e.getSource() == borrowReturnButton) {
        new BorrowReturnGUI(username);
        dispose();
    }

    else if (e.getSource() == reservationsButton) {
        new ManageReservationsGUI(username);
        dispose();
    }
    
    else if (e.getSource() == finesButton) {
        new ManageFinesGUI(username);
        dispose();
    }
    
    else if (e.getSource() == ReportsButton) {
        new ReportsGUI(username);
        dispose();
    }

    else if (e.getSource() == logoutButton) {
        new LoginGUI();
        dispose();
    }
}
}
    

