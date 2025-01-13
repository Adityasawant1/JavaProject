import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ServerUI extends JFrame implements ActionListener {
    private JTextArea messageArea;
    private JTextField inputField;
    private JButton sendButton;
    private JButton pendingButton;
    private Connection dbConnection;

    private ServerSocket serverSocket;
    private final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>()); // Thread-safe list for client handlers

    public ServerUI() {
        // Set up the frame
        super("Server Application");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        // Create a text area to display messages
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        // Add the text area to a scroll pane
        JScrollPane scrollPane = new JScrollPane(messageArea);

        // Create input field and buttons
        inputField = new JTextField();
        sendButton = new JButton("Send");
        pendingButton = new JButton("Pending Work");

        // Set bounds for components
        scrollPane.setBounds(20, 20, 440, 300);
        inputField.setBounds(20, 340, 340, 30);
        sendButton.setBounds(370, 340, 90, 30);
        pendingButton.setBounds(20, 400, 150, 30);

        // Add components to the frame
        add(scrollPane);
        add(inputField);
        add(sendButton);
        add(pendingButton);

        // Register action listener for the send button
        sendButton.addActionListener(this);
        initializeDatabase();
        // Display the frame
        setVisible(true);
    }
      private void initializeDatabase() {
        try {
            String url = "jdbc:mysql://localhost:3306/ClientServer";
            String username = "root";
            String password = "Aditya@2005";
            dbConnection = DriverManager.getConnection(url, username, password);
            appendMessage("Database connected successfully.\n");
        } catch (SQLException e) {
            appendMessage("Error connecting to database: " + e.getMessage() + "\n");
            System.exit(1); // Exit if the database connection fails
        }
    }

      @Override
      public void actionPerformed(ActionEvent e) {
          if (e.getSource() == sendButton) {
              String message = inputField.getText().trim(); // Trim to avoid spaces being considered as valid input
              if (message.isEmpty()) {
                  // Show a dialog if the input field is empty
                  JOptionPane.showMessageDialog(this, "Please enter a message before sending.", "Warning", JOptionPane.WARNING_MESSAGE);
              } else {
                  synchronized (clients) {
                      if (clients.isEmpty()) {
                          // Show a dialog if no clients are connected
                          JOptionPane.showMessageDialog(this, "Client Not Connected", "Info", JOptionPane.INFORMATION_MESSAGE);
                      } else {
                          for (ClientHandler client : clients) {
                              if (client.isActive()) {
                                  client.sendMessage(message);
                              } else {
                                  clients.remove(client);
                              }
                          }
                          appendMessage("Message to clients: " + message + "\n");
                      }
                  }
                  inputField.setText(""); // Clear the input field after sending the message
              }
          }
      }



    public void startServer() {
        try {
            serverSocket = new ServerSocket(5000);
            appendMessage("Server started, waiting for clients...\n");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                appendMessage("\nClient connected: " + clientSocket.getInetAddress().getHostAddress() + "\n");

                // Create a new ClientHandler for each client
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                clientHandler.start(); // Start the client handler thread
            }
        } catch (IOException e) {
            appendMessage("Error: " + e.getMessage() + "\n");
        }
    }

    private void appendMessage(String message) {
        messageArea.append(message);
    }
     private class ClientHandler extends Thread {
        private final Socket clientSocket;
        private DataOutputStream dos;
        private boolean active;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
            this.active = true;
            try {
                dos = new DataOutputStream(clientSocket.getOutputStream());
            } catch (IOException e) {
                appendMessage("Error initializing client handler: " + e.getMessage() + "\n");
                active = false;
            }
        }

       @Override
        public void run() {
            try (DataInputStream dis = new DataInputStream(clientSocket.getInputStream())) {
                String message;
                while (active) {
                    message = dis.readUTF();
                    appendMessage("Message from client: " + message + "\n");

                    if (message.equalsIgnoreCase("exit")) {
                        appendMessage("Client disconnected!\n");
                        active = false;
                        break;
                    }

                    // Extract client details
                    String ipAddress = clientSocket.getInetAddress().getHostAddress();
                    String deviceName = clientSocket.getInetAddress().getHostName();
                    java.util.Date currentDate = new java.util.Date();

                    // Store the information in the database
                    storeClientData(currentDate, deviceName, message, ipAddress);
                }
            } catch (IOException e) {
                appendMessage("Client disconnected: " + e.getMessage() + "\n");
            } finally {
                close();
            }
        }
       private void storeClientData(java.util.Date timestamp, String deviceName, String message, String ipAddress) {
                  String query = "INSERT INTO ClientData (timestamp, device_name, message, ip_address) VALUES (?, ?, ?, ?)";
                  try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
                      pstmt.setTimestamp(1, new java.sql.Timestamp(timestamp.getTime()));
                      pstmt.setString(2, deviceName);
                      pstmt.setString(3, message);
                      pstmt.setString(4, ipAddress);
                      pstmt.executeUpdate();
                      appendMessage("Client data stored in database successfully.\n");
                  } catch (SQLException e) {
                      appendMessage("Error storing client data: " + e.getMessage() + "\n");
                  }
         }


        public void sendMessage(String message) {
            try {
                if (active) {
                    dos.writeUTF(message);
                }
            } catch (IOException e) {
                appendMessage("Error sending message to client: " + e.getMessage() + "\n");
                active = false;
                close();
            }
        }

        public boolean isActive() {
            return active;
        }

        private void close() {
            try {
                active = false;
                if (dos != null) dos.close();
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                appendMessage("Error closing client connection: " + e.getMessage() + "\n");
            }
        }
    }

    public static void main(String[] args) {
        ServerUI serverUI = new ServerUI();
        serverUI.startServer();
    }
}

