

import socketio.Socket;

public class AnfragenHandler implements Runnable {

    private Socket cSocket;
    private int clientNr;
    private String name;
    private ChatServer server;

    public AnfragenHandler(Socket socket, int clientNr, ChatServer server) {
        this.cSocket = socket;
        this.clientNr = clientNr;
        this.server = server;
    }


    public int getClientNr() {
        return clientNr;
    }

    public String getName() {
        return name;
    }


    @Override
    public void run() {
        try {
            cSocket.write("Geben Sie Ihren Anzeigenamen ein:\n");
            name = cSocket.readLine();

            if (name == null || name.trim().isEmpty()) {
                cSocket.write("Ungültiger Name. Verbindung wird beendet.\n");
                return;
            }

            String alleTeilnehmer = "";

            for (AnfragenHandler temp: server.getAlleTeilnehmer()){
                alleTeilnehmer = alleTeilnehmer + "," + temp.getName();
            }

            cSocket.write(alleTeilnehmer + "\n");

            System.out.println(name + " hat sich verbunden.");
            server.broadcastMessage(name + " hat den Chat betreten.", this);

            String message;
            while ((message = cSocket.readLine()) != null) {

                if (message.contains(":")) {
                    message = message.replace(":", "");
                }
                System.out.println(name + ": " + message);
                server.broadcastMessage(name + ": " + message, this);

                if ("over".equalsIgnoreCase(message)) {
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Verbindung mit Client " + clientNr + " unterbrochen: " + e.getMessage());
        } finally {
            beenden();
        }
    }

    public void sendMessage(String message) {
        try {
            cSocket.write(message + "\n");
        } catch (Exception e) {
            System.out.println("Fehler beim Senden an Client " + clientNr + ": " + e.getMessage());
        }
    }

    private void beenden() {
        try {
            server.broadcastMessage(name + " hat den Chat verlassen.", this);
            server.anfrageEntfernen(this);
            cSocket.close();
        } catch (Exception e) {
            System.out.println("Fehler beim Schließen der Verbindung: " + e.getMessage());
        }
        System.out.println("Client " + clientNr + " wurde getrennt.");
    }
}
