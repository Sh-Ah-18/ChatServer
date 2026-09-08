

import socketio.Socket;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ChatClientGUI extends JFrame {

    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 1234;
    private ArrayList<String> teilnehmerliste = new ArrayList<>();

    private Socket cSocket;
    private JPanel chatPanel;
    private JTextField messageField;
    private JButton sendButton;
    private String username;
    private DefaultListModel<String> participantsListModel;
    private Map<String, Color> userColors = new HashMap<>();
    private Random colorGenerator = new Random();

    public ChatClientGUI() {
        // GUI-Setup
        setTitle("Chat Client - Mega UI");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Kopfzeile
        JLabel headerLabel = new JLabel("Willkommen im Chat", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerLabel.setOpaque(true);
        headerLabel.setBackground(new Color(30, 136, 229));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(headerLabel, BorderLayout.NORTH);

        // Chat-Bereich
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(new Color(245, 245, 245));
        JScrollPane chatScrollPane = new JScrollPane(chatPanel);
        chatScrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(chatScrollPane, BorderLayout.CENTER);

        // Nachrichten-Eingabebereich
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        messageField = new JTextField();
        messageField.setFont(new Font("Arial", Font.PLAIN, 16));
        bottomPanel.add(messageField, BorderLayout.CENTER);
        sendButton = new JButton("Senden");
        sendButton.setFont(new Font("Arial", Font.BOLD, 16));
        bottomPanel.add(sendButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        // Teilnehmerliste
        participantsListModel = new DefaultListModel<>();
        JList<String> participantsList = new JList<>(participantsListModel);
        participantsList.setFont(new Font("Arial", Font.PLAIN, 14));
        participantsList.setBackground(new Color(240, 240, 240));
        participantsList.setFixedCellHeight(30);
        participantsList.setBorder(BorderFactory.createTitledBorder("Teilnehmer"));
        add(new JScrollPane(participantsList), BorderLayout.EAST);

        // Verbindung zum Server herstellen
        initializeConnection();

        // Sende-Button und Enter-Taste
        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());
    }

    private void initializeConnection() {
        try {
            String host = JOptionPane.showInputDialog(this, "Geben Sie die IP des Servers ein:", DEFAULT_HOST);
            String portString = JOptionPane.showInputDialog(this, "Geben Sie den Port des Servers ein:", DEFAULT_PORT);
            int port = portString != null ? Integer.parseInt(portString) : DEFAULT_PORT;

            cSocket = new Socket(host, port);
            if (!cSocket.connect()) {
                JOptionPane.showMessageDialog(this, "Verbindung fehlgeschlagen!", "Fehler", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }

            username = JOptionPane.showInputDialog(this, "Geben Sie Ihren Anzeigenamen ein:");
            if (username == null || username.trim().isEmpty()) {
                username = "Unbekannt";
            }
            cSocket.write(username + "\n");

            cSocket.readLine(); // fur die Abfrage

            String alleTeilnehmer = cSocket.readLine();

            String [] temp = alleTeilnehmer.split(",");

            teilnehmerliste.add("Ich (" + username + ")       ");

            for (String teilnehmer : temp) {
                if (!teilnehmer.trim().isEmpty() && !teilnehmer.equalsIgnoreCase(username)) {
                    teilnehmerliste.add(teilnehmer);
                }
            }

            refreshParticipants();

            Thread receivingThread = new Thread(() -> {
                try {
                    String message;
                    while ((message = cSocket.readLine()) != null) {
                        processMessage(message);

                        if (message.contains("hat den Chat betreten.")) {
                            String newUser = message.split(" ")[0];
                            if (!teilnehmerliste.contains(newUser)) {
                                teilnehmerliste.add(newUser);
                                refreshParticipants();
                            }

                        } else if (message.contains("hat den Chat verlassen.")) {
                            String user = message.split(" ")[0];
                            teilnehmerliste.remove(user);
                            refreshParticipants();
                        }
                    }
                } catch (IOException e) {
                    showMessage("Verbindung verloren.", "System", Color.RED, true);
                }
            });
            receivingThread.start();

        } catch (IOException e) {
            showMessage("Fehler: " + e.getMessage(), "System", Color.RED, true);
        }
    }

    private void processMessage(String message) {
        // Prüfe, ob die Nachricht das Trennzeichen ":" enthält
        if (!message.contains(":")) {
            // Falls die Nachricht kein ":" enthält, wird sie als Systemnachricht behandelt
            showMessage(message, "System", Color.GRAY, false);
            return;
        }

        String[] parts = message.split(":", 2);

        if (parts.length < 2) {

            showMessage(message, "System", Color.GRAY, false);
            return;
        }

        String sender = parts[0].trim();
        String content = parts[1].trim();

        // Weise dem Sender eine Farbe zu, falls noch nicht geschehen
        Color color = userColors.computeIfAbsent(sender, k -> generateRandomColor());

        // Zeige die Nachricht an
        showMessage(content, sender, color, false);
    }


    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            try {
                cSocket.write(message + "\n");
                showMessage(message, username, Color.BLUE, true);
                messageField.setText("");
            } catch (IOException e) {
                showMessage("Fehler beim Senden.", "System", Color.RED, true);
            }
        }
    }

    private void refreshParticipants() {
        participantsListModel.clear();
        for (String participant : teilnehmerliste) {
            participantsListModel.addElement(participant);
        }
    }

    private void showMessage(String message, String sender, Color color, boolean isOwnMessage) {
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BorderLayout());
        messagePanel.setBackground(chatPanel.getBackground());

        JLabel messageLabel;
        if (!username.equalsIgnoreCase(sender)){
            messageLabel = new JLabel("<html><p style='width: 300px;' >" + sender + ": " + message + "</p></html>");
        }else{
            messageLabel = new JLabel("<html><p style='width: 300px;' >" + message + "</p></html>");
        }
        messageLabel.setOpaque(true);
        messageLabel.setBackground(isOwnMessage ? new Color(220, 248, 198) : color);
        messageLabel.setForeground(isOwnMessage ? Color.BLACK : Color.WHITE);
        messageLabel.setBorder(new EmptyBorder(10, 10, 10, 10));

        if (isOwnMessage) {
            messagePanel.add(messageLabel, BorderLayout.EAST);
        } else {
            messagePanel.add(messageLabel, BorderLayout.WEST);
        }

        chatPanel.add(messagePanel);
        chatPanel.revalidate();
        chatPanel.repaint();
    }

    private Color generateRandomColor() {
        Color color;
        do {
            // Generiere eine zufällige Farbe
            color = new Color(colorGenerator.nextInt(256), colorGenerator.nextInt(256), colorGenerator.nextInt(256));
        } while (!isReadableOnWhite(color)); // Wiederhole, falls die Farbe schlecht lesbar ist
        return color;
    }

    private boolean isReadableOnWhite(Color color) {
        // Berechnung der Helligkeit (Luminanz) der Farbe
        double luminance = (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;

        // Farben mit hoher Luminanz (> 0.8) sind zu hell für weißen Text
        return luminance < 0.8;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChatClientGUI clientGUI = new ChatClientGUI();
            clientGUI.setVisible(true);
        });
    }
}
