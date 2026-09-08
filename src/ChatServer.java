
import socketio.ServerSocket;
import socketio.Socket;

import java.util.ArrayList;

public class ChatServer {

    private int port;
    private int clientNr;
    private ArrayList<AnfragenHandler> anfrageHandlers = new ArrayList<>();
    private ServerSocket sSocket;

    public ChatServer() {
        port = 1234;
        try {
            sSocket = new ServerSocket(port);
            System.out.println("Server ist gestartet!");
        } catch (Exception e) {
            System.out.println(e);
        }

        try {
            clientNr = 0;
            while (true) {
                clientNr++;
                Socket cSocket = sSocket.accept();
                System.out.println("Client verbunden: " + clientNr);

                AnfragenHandler handler = new AnfragenHandler(cSocket, clientNr, this);
                anfrageHandlers.add(handler);

                new Thread(handler).start();
            }
        } catch (Exception e) {
            System.out.println("Fehler beim Verbinden eines Clients: " + e.getMessage());
        }
    }

    public synchronized void broadcastMessage(String message, AnfragenHandler sender) {
        System.out.println("Broadcast: " + message);
        for (AnfragenHandler handler : anfrageHandlers) {
            if (handler != sender) {
                handler.sendMessage(message);
            }
        }
    }

    public synchronized ArrayList<AnfragenHandler> getAlleTeilnehmer(){
        return anfrageHandlers;
    }

    public synchronized void anfrageEntfernen(AnfragenHandler handler) {
        anfrageHandlers.remove(handler);
        System.out.println("Handler entfernt: " + handler.getClientNr());

        if (anfrageHandlers.isEmpty()){
            System.out.println("Der Server wird geschlossen, da alle Teilnehmer den Chat verlassen haben.");
            beenden();
        }
    }

    public void beenden() {
        try {
            sSocket.close();
            System.out.println("Server wurde geschlossen.");
        } catch (Exception e) {
            System.out.println("Fehler beim Schließen des Servers: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new ChatServer();
    }
}
