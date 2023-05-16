import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;


public class ClientGUI extends JFrame {
    private JButton connectButton, disconnectButton;
    private JTextField inputField;
    private JTextArea chatArea;
    private Client client;
    
    public ClientGUI() {
        // Initializes the GUI. Set the theme (look and feel, as JAVA calls it) to NIMBUS
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        
        // This is the Chat Display Area
        chatArea = new JTextArea(20, 30);
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        chatArea.setBorder(new EmptyBorder(10, 10, 10, 10)); // Add padding

        // This is to make the font bigger and easily readable
        Font font = new Font("Arial", Font.PLAIN, 18);
        chatArea.setFont(font);

        // This is the Text Area for writing up messages
        inputField = new JTextField(30);
        inputField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String message = inputField.getText();
                client.sendMessage(message);
                inputField.setText("");
            }
        });
        inputField.setFont(font); // Uses the font from above

        // This is the Connect button
        connectButton = new JButton("LOGIN");
        connectButton.setBackground(new Color(152, 251, 152)); // Change button color
        connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String name = JOptionPane.showInputDialog( // after clicking "LOGIN", dialog box appears asking for the name of client
                        ClientGUI.this,
                        "Enter your name:",
                        "Connect to JOY CHAT server",
                        JOptionPane.PLAIN_MESSAGE
                );

                JOptionPane.showMessageDialog(ClientGUI.this,
                "Welcome "+ name +"!\n" +
                "To get started, simply type a message in the text field and press Enter.\n" +
                "To disconnect, click the 'LOGOUT' button.\n"+
                "Enjoy using JOY CHAT!",
                "Getting Started",
                JOptionPane.INFORMATION_MESSAGE);

                client = new Client(name);
                client.start();
            }
        });

        // This is the Disconnect button
        disconnectButton = new JButton("LOGOUT");
        disconnectButton.setBackground(new Color(255, 182, 193)); // Change button color
        disconnectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (client != null) {
                    client.disconnect();
                    client = null;
                }
            }
        });

        // Other stuff like padding and layout options
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(inputField, BorderLayout.SOUTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2));
        buttonPanel.add(connectButton);
        buttonPanel.add(disconnectButton);
        buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); 

        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        container.add(panel, BorderLayout.CENTER);
        container.add(buttonPanel, BorderLayout.NORTH);

        // Window Configurations
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 700);
        setTitle("JOY CHAT"); //hehe
        setVisible(true);
    }

    private class Client extends Thread {
        private static final String HOST = "localhost";
        private static final int PORT = 3000;
        private BufferedReader in;
        private PrintWriter out;
        private String name;
        private Socket socket;

        public Client(String name) {
            this.name = name;
        }

        public void run() {
            try {
                socket = new Socket(HOST,PORT);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println("CONNECT " + name);

                String serverMessage;
                while ((serverMessage = in.readLine()) != null) {
                    chatArea.append(serverMessage + "\n");
                    if (serverMessage.contains("has joined the chat room")) { //Sound is played on Client connect
                        playSound("connect.wav");
                    } else if (serverMessage.contains("has left the chat room")) { //Sound is played on Client disconnect
                        playSound("disconnect.wav");
                    } else {
                        playSound("discord.wav"); //Sound is played when a message is recieved
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /* This method is modified so I can create a JAR file that has access to the resourses.
        public void playSound(String soundName) { //this takes in a string as a "filepath" to find the file and play it
            try {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(soundName).getAbsoluteFile());
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.start();
            } catch(Exception ex) {
                System.out.println("Error with playing sound.");
                ex.printStackTrace();
            }
        }
        */

        public void playSound(String soundName) { 
            try {
                InputStream audioSrc = getClass().getResourceAsStream(soundName);
                BufferedInputStream bufferedIn = new BufferedInputStream(audioSrc);
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedIn);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.start();
            } catch(Exception ex) {
                System.out.println("Error with playing sound.");
                ex.printStackTrace();
            }
        }        

        private String lastMessage = null;
        private long lastMessageTime = 0;

        public void sendMessage(String message) {
            // THE ANTI-SPAM MACHINE!!!!
            // Stores last message and if the next message is the same as previous message
            // the client has to wait 10 seconds before sending the same message again.
            long now = System.currentTimeMillis();
            if (message.equals(lastMessage) && now - lastMessageTime < 10000) { 
                chatArea.append("Please wait 10 seconds before sending the same message again.\n");
                return;
            }

            //I'm just leaving this in here, but it is not necessary anymore because the GUI has disconnect and connect buttons
            if (message.equalsIgnoreCase("quit") || message.equalsIgnoreCase("disconnect")) {
                disconnect();
            } else {
                out.println("MESSAGE " + message);
                lastMessage = message;
                lastMessageTime = now;
            }
        }

        //Called when disconnect (LOGOUT) button is clicked.
        public void disconnect() {
            out.println("DISCONNECT");
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ClientGUI();
            }
        });
    }
}
