

import socketio.Socket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class ChatClient {

    private int port;

    private String host;

    private Socket cSocket;

    ChatClient() {
        port = 1234;

        host = "127.0.0.1";
        System.out.println("Client ist gestartet!");
        try {
            cSocket = new Socket(host, port);


        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void kommunizieren() {

        Scanner sc = new Scanner(System.in);

        try {
            if (!cSocket.connect()) {
                System.out.println("Verbindung zum Server konnte nicht hergestellt werden!");
            } else {
                System.out.println("Verbindung zum Server ist aufgebaut!");
            }

            System.out.println(cSocket.readLine());

            String name = sc.nextLine();

            cSocket.write(name + "\n");

            Thread nachrichtenThread = new Thread(() -> {
                try {
                    String incomingMessage;
                    while ((incomingMessage = cSocket.readLine()) != null) {
                        System.out.println(incomingMessage);
                    }
                } catch (IOException e) {
                    System.out.println("Connection closed.");
                }
            });
            nachrichtenThread.start();

            System.out.println("Kannst jetzt einfach schreiben");

            String eingabe;
            while (true){

                eingabe = sc.nextLine();

                System.out.println();

                cSocket.write(eingabe + "\n");

                if (eingabe.toLowerCase().contains("over")) break;


            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public boolean beenden(String beendenKeyword) {
        boolean over = false;
        if (beendenKeyword.contains("over".toLowerCase())) {
            over = true;
            try {
                cSocket.close();
            } catch (Exception e) {
                System.out.println(e);
            }
            System.out.println("Die Verbdingung zum Server wurde geschlossen!");
        }
        return over;
    }

    public static void main(String[] args) {
        ChatClient ec = new ChatClient();

        ec.kommunizieren();
    }
}
