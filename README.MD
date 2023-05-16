# JOY CHAT

JOY CHAT is a simple Java-based chat application that uses sockets for communication between a server and multiple clients. This application includes a server program and a client program, each with a GUI interface. 

## Functions:
The **Server** *manages* and *broadcasts* messages to all connected clients. The GUI logs every system event.\
The **Clients** can *connect*, *send messages*, and *disconnect* from the server. The GUI allows for a simple user interface.\

## Features:
**Client-Server Chat System:** Clients can send messages to the server, which the server then broadcasts these messages to all other connected clients.\
**Sound Notifications:** Sounds are played on client connect, disconnect, and when sending/receiving messages.\
**Anti-Spam Feature:** The client has to wait for 10 seconds before sending the same message again.\
**GUI Interface:** The server GUI displays a log of client connections and disconnections, along with other system messages.\
The client interface includes 2 buttons to connect and disconnect, a chat display frame and text area for messages.\

## Getting Started:
**To Compile**
1. Run the *compile.bat* script. Process will automatically compile and cleanup.
2. *Server.jar* and *Client.jar* will appear in the same folder.

**Server**
1. Run the *Server.jar* file, the server log GUI will appear.
2. The server will start listening to client connections on port 3000

**Client**
1. Run the *Client.jar* file, the chat interface will appear.
2. Click "LOGIN" and enter your name.
3. Send messages by typing them in the text area and hit enter to send.
4. Disconnect by clicking the "LOGOUT" Button.

## Requirements:
 - Java Development Kit (JDK)

## Limitations:
- Port and IP Address are hard-coded to 3000 and 'localhost'. You need to edit and recompile to make any changes.

## Future Enhancements:
- Private messaging
- User Authentication
- GUI improvements
