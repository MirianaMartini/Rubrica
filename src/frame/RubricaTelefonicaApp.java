package frame;

import classes.Persona;
import classes.Utente;
import database.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.Vector;

public class RubricaTelefonicaApp extends JFrame { //JFrame finestra base di swing
    //variabili di istanza
    private DefaultTableModel tableModel; //modello di tabella default di Swing
    private Vector<Persona> rubrica = new Vector<>(); //Lista di oggetti persona contenuti in rubrica
    private Utente utenteC;

    //COSTRUTTORE
    public RubricaTelefonicaApp(Utente utenteCorrente) {
        super(utenteCorrente.getUsername()+": Rubrica Telefonica");
        utenteC = utenteCorrente;

        tableModel = new DefaultTableModel();
        //aggiungo le colonne nella tabella
        tableModel.addColumn("Nome");
        tableModel.addColumn("Cognome");
        tableModel.addColumn("Telefono");

        //GUI
        JTable table = new JTable(tableModel); //JTable - tabella swing che permette di visualizzare i dati
        JScrollPane scrollPane = new JScrollPane(table); //JScrollPane - barra di scorrimento
        //pulsanti
        JButton nuovoButton = new JButton("Nuovo");
        JButton modificaButton = new JButton("Modifica");
        JButton eliminaButton = new JButton("Elimina");

        //caricaDaFile();
        caricaDaDB(utenteCorrente);

        nuovoButton.addActionListener(new ActionListener() { //gestione del pulsante "nuovo" -> apre una finestra
            @Override
            public void actionPerformed(ActionEvent e) {
                apriFinestraEditor(new Persona());
            }
        });

        modificaButton.addActionListener(new ActionListener() { //gestione del pulsante "modifica"
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(RubricaTelefonicaApp.this,
                            "Seleziona una persona da modificare.",
                            "Errore",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    apriFinestraEditor(rubrica.get(selectedRow));
                }
            }
        });

        eliminaButton.addActionListener(new ActionListener() { //gestione del pulsante "elimina"
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(RubricaTelefonicaApp.this,
                            "Seleziona una persona da eliminare.",
                            "Errore",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    int choice = JOptionPane.showConfirmDialog(RubricaTelefonicaApp.this,
                            "Eliminare la persona " + rubrica.get(selectedRow).getNomeCompleto() + "?",
                            "Conferma eliminazione",
                            JOptionPane.YES_NO_OPTION);
                    if (choice == JOptionPane.YES_OPTION) {
                        rubrica.remove(selectedRow);
                        aggiornaTabella();
                    }
                }
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(nuovoButton);
        buttonPanel.add(modificaButton);
        buttonPanel.add(eliminaButton);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    //METODI
    public void caricaDaFile() {
        try (Scanner scanner = new Scanner(new File("data/informazioni.txt"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] tokens = line.split(";");
                if (tokens.length == 5) {
                    String nome = tokens[0];
                    String cognome = tokens[1];
                    String indirizzo = tokens[2];
                    String telefono = tokens[3];
                    int eta = Integer.parseInt(tokens[4]);

                    Persona persona = new Persona(nome, cognome, indirizzo, telefono, eta);
                    rubrica.add(persona);
                }
            }
            aggiornaTabella();
        } catch (FileNotFoundException e) {
            System.err.println("Errore durante il caricamento del file: " + e.getMessage());
        }
    }

    public void salvaSuFile() {
        try (PrintStream printStream = new PrintStream(new FileOutputStream("data/informazioni.txt"))) {
            for (Persona persona : rubrica) {
                printStream.println(persona.getNome() + ";" +
                        persona.getCognome() + ";" +
                        persona.getIndirizzo() + ";" +
                        persona.getTelefono() + ";" +
                        persona.getEta());
            }
        } catch (FileNotFoundException e) {
            //non fare nulla
        }
    }


    public void caricaDaDB(Utente utenteCorrente) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            // Seleziona le persone associate all'utente
            String query = "SELECT p.* FROM persone p " +
                    "JOIN rubrica_utenti_persone rup ON p.id = rup.persona_id " +
                    "JOIN utenti u ON u.username = rup.utente_id " +
                    "WHERE u.username = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                // Imposta il parametro nella query con l'ID dell'utente
                preparedStatement.setString(1, utenteCorrente.getUsername());

                // Esegui la query
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        // Leggi i dati della persona dal risultato della query
                        int idPersona = resultSet.getInt("id");
                        String nome = resultSet.getString("nome");
                        String cognome = resultSet.getString("cognome");
                        String indirizzo = resultSet.getString("indirizzo");
                        String telefono = resultSet.getString("telefono");
                        int eta = resultSet.getInt("eta");

                        // Crea un oggetto classes.Persona
                        Persona persona = new Persona(nome, cognome, indirizzo, telefono, eta);

                        // Aggiungi la persona alla lista della rubrica
                        rubrica.add(persona);
                    }

                    aggiornaTabella();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void salvaSuDB() {
        try (Connection connection = DatabaseConnection.getConnection()) { //ottengo la connessione
            connection.setAutoCommit(false);  // Inizia una transazione

            try {
                // Cancella dalla tabella persone tutte le persone associate all'utente loggato
                String deletePersonQuery = "DELETE FROM persone WHERE id IN (SELECT persona_id FROM rubrica_utenti_persone WHERE utente_id = ?)";
                try (PreparedStatement deletePersonStatement = connection.prepareStatement(deletePersonQuery)) { // Crea un oggetto PreparedStatement per l'esecuzione della query di cancellazione.
                    deletePersonStatement.setString(1, utenteC.getUsername()); //imposto il parametro della query
                    deletePersonStatement.executeUpdate(); //eseguo la query
                }

                // Cancella dalla tabella rubrica_utenti_persone le righe con utente_id uguale a quello loggato
                String deleteRelationshipQuery = "DELETE FROM rubrica_utenti_persone WHERE utente_id = ?";
                try (PreparedStatement deleteRelationshipStatement = connection.prepareStatement(deleteRelationshipQuery)) {
                    deleteRelationshipStatement.setString(1, utenteC.getUsername());
                    deleteRelationshipStatement.executeUpdate();
                }

                // Inserisce in entrambe le tabelle tutti i contatti presenti nella rubrica aggiornata
                String insertPersonQuery = "INSERT INTO persone (nome, cognome, indirizzo, telefono, eta) VALUES (?, ?, ?, ?, ?)";
                String insertRelationshipQuery = "INSERT INTO rubrica_utenti_persone (utente_id, persona_id) VALUES (?, ?)";

                for (Persona persona : rubrica) {
                    try (PreparedStatement insertPersonStatement = connection.prepareStatement(insertPersonQuery, PreparedStatement.RETURN_GENERATED_KEYS); //crea un oggetto per l'inserimento delle persone
                         PreparedStatement insertRelationshipStatement = connection.prepareStatement(insertRelationshipQuery)) { //crea un oggetto per l'inserimento nella tabella di mezzo
                        // Inserisce persona nella tabella persone
                        insertPersonStatement.setString(1, persona.getNome());
                        insertPersonStatement.setString(2, persona.getCognome());
                        insertPersonStatement.setString(3, persona.getIndirizzo());
                        insertPersonStatement.setString(4, persona.getTelefono());
                        insertPersonStatement.setInt(5, persona.getEta());
                        insertPersonStatement.executeUpdate();

                        // Ottiene l'id della persona appena inserita
                        try (ResultSet generatedKeys = insertPersonStatement.getGeneratedKeys()) {
                            if (generatedKeys.next()) { //per tutti gli ID generati
                                int personaId = generatedKeys.getInt(1); //ottengo l'id generato per la persona

                                // Inserisce la relazione nella tabella rubrica_utenti_persone
                                insertRelationshipStatement.setString(1, utenteC.getUsername());
                                insertRelationshipStatement.setInt(2, personaId);
                                insertRelationshipStatement.executeUpdate();
                            }
                        }
                    }
                }
                connection.commit();  // Conferma la transazione

            } catch (SQLException e) {
                connection.rollback();  // Rollback in caso di errore
                e.printStackTrace();
            } finally {
                connection.setAutoCommit(true);  // Ripristina l'autocommit
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void apriFinestraEditor(Persona persona) { //apre una finestra di modifica con i dati di una persona specifica
        EditorPersonaFrame editorFrame = new EditorPersonaFrame(this, persona); //this rappresenta l'istanza frame.RubricaTelefonicaApp corrente
        editorFrame.setVisible(true);
    }

    public void aggiornaTabella() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                tableModel.setRowCount(0);
                for (Persona persona : rubrica) {
                    tableModel.addRow(new Object[]{persona.getNome(), persona.getCognome(), persona.getTelefono()});
                }
                //salvaSuFile();
                salvaSuDB();
            }
        });
    }

    public void aggiungiPersona(Persona persona) {
        rubrica.add(persona);
        aggiornaTabella();
    }

    public void removePersona(Persona persona) {
        rubrica.remove(persona);
    }


}
