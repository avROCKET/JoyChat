import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Server {
    private static final int PORT = 3000; // Change port here...
    
    /* Clients (ClientHandler instances) are stored on server-side in a 
     * synchronizedList, no two threads (clients) can excecute at the same time. */
    private static List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());

        public static void main(String[] args) throws IOException { // Entry Point: Initialize GUI and creates socket and new ClientHandler thread for each new connection.
        
        // Swing Framework: JFrame creates the window for the application, JTextArea is for the log messages.
        JFrame frame = new JFrame("JOY CHAT Server Log");
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        frame.add(new JScrollPane(textArea));
        frame.setSize(300, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            textArea.append("JOY CHAT Server is running...\n"); //Server has Initiated and is listening on PORT

            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket, textArea);
                clients.add(clientHandler);
                clientHandler.start();
            }
        }
    }

    /** 
     * broadcastMessage() ---
     * Loops over all the the ‘ClientHandler’ instances in the synchronized  ‘clients’ list and for each, it calls the ‘sendMessage’ method. 
     * In terms of synchronization, no two threads, in this case clients, can execute the ‘broadcastMessage’ method at the same time 
     * 
     * @param message Message sent from client.
     */
    public static void broadcastMessage(String message) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.sendMessage(message);
            }
        }
    }

    // To update ClientHandler list when clients disconnect. 
    public static void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String clientName;
        private JTextArea textArea;

        // Constructor: initiates 'socket' and 'textArea'
        public ClientHandler(Socket socket, JTextArea textArea) {
            this.socket = socket;
            this.textArea = textArea;
        }

        // Main method for ClientHandler
        public void run() {
            try {
                // Object "in" is a single line containing the PROTOCOL and message
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    // Object "in" is split into to parts, Protocol and Message
                    String[] tokens = inputLine.split(" ", 2);
                    String messageType = tokens[0]; //Protocol is token [0]

                    // Protocol Handler: This works by using token[0] to determine the messageType, which will call its respective handlers.
                    if ("CONNECT".equals(messageType)) {
                        handleConnect(tokens[1]); // Client name is token[1]
                    } else if ("MESSAGE".equals(messageType)) {
                        handleMessage(tokens[1]); // Message is token[1]
                    } else if ("DISCONNECT".equals(messageType)) {
                        handleDisconnect();
                        break;
                    }
                }
            } catch (IOException e) {
                textArea.append("Error in client communication: " + clientName + "\n"); // Error handling for abrupt disconnections
            } finally {
                try {
                    removeClient(this); // Removes this ClientHandler from the clients list, 
                    socket.close();     // and closes the client's socket.
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Called by broadcastMessage() method, to send THIS message from THIS client to ALL clients.
        // Essentially, client sends the message to the server which then uses broadcastMessage(), 
        // where it sends the message back to this client, plus the other clients.
        public void sendMessage(String message) {
            out.println(message);
        }

        // Handler for CONNECT protocol: This sets the clientName for this client, logs the connection, 
        // and broadcasts a message to all clients that this client has joined the chat room..
        public void handleConnect(String clientName) {
            this.clientName = clientName;
            textArea.append("Client connected: " + clientName + "\n");
            broadcastMessage(clientName + " has joined the chat room.");
        }

        // Handler for MESSAGE protocol: This will handle the messages sent by clients. The protocol 
        // handler will tell this handler that there is a message, the client's name and message 
        // will be passed on to be broadcasted to all clients.
        public void handleMessage(String message) {
            broadcastMessage(clientName + ": " + message);
        }

        // Handler for DISCONNECT protocol: Logs the disconnection and broadcasts a message to all clients.
        public void handleDisconnect() {
            textArea.append("Client disconnected: " + clientName + "\n");
            broadcastMessage(clientName + " has left the chat room.");
        }
    }
}