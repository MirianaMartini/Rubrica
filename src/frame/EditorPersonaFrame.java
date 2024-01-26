package frame;

import classes.Persona;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EditorPersonaFrame extends JFrame {
    private JTextField nomeField, cognomeField, telefonoField, indirizzoField, etaField;
    private JButton salvaButton, annullaButton;
    private RubricaTelefonicaApp parent;
    private Persona persona;

    public EditorPersonaFrame(RubricaTelefonicaApp parent, Persona persona) {
        super("Editor classes.Persona");

        this.parent = parent;
        this.persona = persona;

        nomeField = new JTextField(persona.getNome());
        cognomeField = new JTextField(persona.getCognome());
        indirizzoField = new JTextField(persona.getIndirizzo());
        telefonoField = new JTextField(persona.getTelefono());
        if (persona.getEta() == 0) {
            etaField = new JTextField("");
        } else {
            etaField = new JTextField(Integer.toString(persona.getEta()));
        }

        salvaButton = new JButton("Salva");
        annullaButton = new JButton("Annulla");

        salvaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                persona.setNome(nomeField.getText());
                persona.setCognome(cognomeField.getText());
                persona.setIndirizzo(indirizzoField.getText());
                persona.setTelefono(telefonoField.getText());

                try {
                    int eta = Integer.parseInt(etaField.getText());
                    persona.setEta(eta);

                    parent.removePersona(persona);
                    parent.aggiungiPersona(persona);

                    dispose();

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(EditorPersonaFrame.this,
                            "L'età deve essere un numero intero valido.",
                            "Errore",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        annullaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        JPanel panel = new JPanel(new GridLayout(6, 2));
        panel.add(new JLabel("Nome"));
        panel.add(nomeField);
        panel.add(new JLabel("Cognome"));
        panel.add(cognomeField);
        panel.add(new JLabel("Indirizzo"));
        panel.add(indirizzoField);
        panel.add(new JLabel("Telefono"));
        panel.add(telefonoField);
        panel.add(new JLabel("Età"));
        panel.add(etaField);
        panel.add(salvaButton);
        panel.add(annullaButton);

        add(panel);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 200);
        setLocationRelativeTo(parent);
    }
}
