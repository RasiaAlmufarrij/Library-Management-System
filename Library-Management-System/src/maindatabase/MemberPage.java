package maindatabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class MemberPage extends JFrame implements ActionListener {
    private JLabel l1;
    private JButton searchButton, loansButton, reservationsButton, finesButton, logoutButton;
    private String username;
    public MemberPage(String user) {
        this.username=user;
        setTitle("Member Dashboard");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = (JPanel) this.getContentPane();
        panel.setBackground(new Color(222, 184, 135)); 
        panel.setLayout(new GridLayout(6, 1, 5, 5));

        l1 = new JLabel("Pick a service:");
        l1.setForeground(new Color(101, 67, 33));
        l1.setFont(new Font("Arial", Font.BOLD, 18));
        l1.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(l1);

        searchButton = new JButton("Search Book");
        searchButton.setBackground(new Color(245, 222, 179));
        searchButton.setForeground(new Color(90, 50, 20));
        searchButton.addActionListener(this);
        panel.add(searchButton);

        loansButton = new JButton("My Loans");
        loansButton.setBackground(new Color(245, 222, 179));
        loansButton.setForeground(new Color(90, 50, 20));
        loansButton.addActionListener(this);
        panel.add(loansButton);

        reservationsButton = new JButton("My Reservations");
        reservationsButton.setBackground(new Color(245, 222, 179));
        reservationsButton.setForeground(new Color(90, 50, 20));
        reservationsButton.addActionListener(this);
        panel.add(reservationsButton);

        finesButton = new JButton("My Fines");
        finesButton.setBackground(new Color(245, 222, 179));
        finesButton.setForeground(new Color(90, 50, 20));
        finesButton.addActionListener(this);
        panel.add(finesButton);

        logoutButton = new JButton("Log Out");
        logoutButton.setBackground(new Color(245, 222, 179));
        logoutButton.setForeground(new Color(90, 50, 20));
        logoutButton.addActionListener(this);
        panel.add(logoutButton);

        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == searchButton) {
            new SearchBooksGUI(username);
            dispose();
        }

        else if (e.getSource() == loansButton) {
            new MyLoansGUI(username);
            dispose();
        }

        else if (e.getSource() == reservationsButton) {
            new ReservationsGUI(username);
            dispose();
        }

        else if (e.getSource() == finesButton) {
            new FinesGUI(username);
            dispose();
        }

        else if (e.getSource() == logoutButton) {
            new LoginGUI();
            dispose();
        }
    }
}
