package main;

import classes.Utente;
import database.DatabaseConnection;
import frame.RubricaTelefonicaApp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;




public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private RubricaTelefonicaApp rubricaPersonale;

    //COSTRUTTORE
    public LoginFrame() {
        super("Login");

        usernameField = new JTextField();
        passwordField = new JPasswordField();
        loginButton = new JButton("Login");

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                effettuaLogin(username, password);
            }
        });

        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("Username"));
        panel.add(usernameField);
        panel.add(new JLabel("Password"));
        panel.add(passwordField);
        panel.add(new JLabel());
        panel.add(loginButton);

        add(panel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 150);
        setLocationRelativeTo(null);
    }

    //METODI
    public boolean checkUtente(String username, String password) {
        String query = "SELECT * FROM utenti WHERE username = ? AND password = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void effettuaLogin(String username, String password) {
        if(checkUtente(username, password)){
            Utente utenteLoggato = new Utente(username, password);
            dispose(); // Chiudi la finestra del login
            rubricaPersonale = new RubricaTelefonicaApp(utenteLoggato);
        }
        else{
            JOptionPane.showMessageDialog(this,
                    "Login errato. Riprova.",
                    "Errore di Login",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    //MAIN
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginFrame app = new LoginFrame();
            app.setVisible(true);
        });
    }
}
