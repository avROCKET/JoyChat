import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Server {
    private static final int PORT = 3000;
    private static List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    //Clients are stored on server-side in a synchronizedList

    public static void main(String[] args) throws IOException {
        //Initiates a GUI, using Swing. The server just has a simple GUI with a JFrame and TextArea to display server logs.
        //Instead of [System.out.println], [textArea.append] is used to print logs.
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

    //Broadcast function allows messages to be synchronized and sent to all clients
    public static void broadcastMessage(String message) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.sendMessage(message);
            }
        }
    }

    public static void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String clientName;
        private JTextArea textArea;

        public ClientHandler(Socket socket, JTextArea textArea) {
            this.socket = socket;
            this.textArea = textArea;
        }

        public void run() {
            try {
                //Object "in" is a single line containing the PROTOCOL and message
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    //Object "in" is split into to parts, Protocol and Message
                    String[] tokens = inputLine.split(" ", 2);
                    String messageType = tokens[0]; //Protocol is token [0]

                    //Protocol Handler: This works by using token[0] to determine the messageType, which will call its respective handlers.
                    if ("CONNECT".equals(messageType)) {
                        handleConnect(tokens[1]); //Client name is token[1]
                    } else if ("MESSAGE".equals(messageType)) {
                        handleMessage(tokens[1]); //Message is token[1]
                    } else if ("DISCONNECT".equals(messageType)) {
                        handleDisconnect();
                        break;
                    }
                }
            } catch (IOException e) {
                textArea.append("Error in client communication: " + clientName + "\n"); //Error handling for abrupt disconnections
            } finally {
                try {
                    removeClient(this);
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        // Handler for CONNECT protocol: This will synchronize all client names that have joined and broadcast the names of the clients that joined.
        public void handleConnect(String clientName) {
            this.clientName = clientName;
            textArea.append("Client connected: " + clientName + "\n");
            broadcastMessage(clientName + " has joined the chat room.");
        }

        // Handler for MESSAGE protocol: This will handle the messages sent by clients. The protocol handler will tell this handler that there is a message,
        // the client's name and message will be passed on to be broadcasted to all clients.
        public void handleMessage(String message) {
            broadcastMessage(clientName + ": " + message);
        }

        // Handler for DISCONNECT protocol: This will synchronize all the client names that have left and broadcast the names of the clients that left.
        public void handleDisconnect() {
            textArea.append("Client disconnected: " + clientName + "\n");
            broadcastMessage(clientName + " has left the chat room.");
        }
    }
}