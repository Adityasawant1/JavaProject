import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class ServerUI extends JFrame implements ActionListener {
    private JTextArea messageArea;
    private JTextField inputField;
    private JButton sendButton, pendingButton, solveButton, doneButton;
    private JTable clientTable;
    private DefaultTableModel tableModel;
    private Connection dbConnection;
    private ServerSocket serverSocket;
    private final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    
    public ServerUI() {
        // Set up the frame
        super("Server Application");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        
        // Create message area and input field
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        scrollPane.setBounds(20, 20, 540, 200);
        
        inputField = new JTextField();
        sendButton = new JButton("Send");
        pendingButton = new JButton("Pending Work");
        solveButton = new JButton("Mark as Solved");
        doneButton = new JButton("Mark as Done");
        
        // Create table to display client data
        String[] columnNames = {"Client IP", "Device Name", "Message", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0);
        clientTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(clientTable);
        tableScrollPane.setBounds(20, 230, 540, 150);
        
        // Set bounds for components
        inputField.setBounds(20, 400, 400, 30);
        sendButton.setBounds(430, 400, 100, 30);
        pendingButton.setBounds(20, 440, 150, 30);
        solveButton.setBounds(180, 440, 150, 30);
        doneButton.setBounds(340, 440, 150, 30);
        
        // Add components to the frame
        add(scrollPane);
        add(inputField);
        add(sendButton);
        add(pendingButton);
        add(solveButton);
        add(doneButton);
        add(tableScrollPane);
        
        // Register action listeners
        sendButton.addActionListener(this);
        pendingButton.addActionListener(this);
        solveButton.addActionListener(this);
        doneButton.addActionListener(this);
        
        initializeDatabase();
        loadDataFromDatabase();
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
            System.exit(1);
        }
    }
    private void loadDataFromDatabase() {
    String query = "SELECT ip_address, device_name, message, status FROM ClientData";
      try (Statement stmt = dbConnection.createStatement();
           ResultSet rs = stmt.executeQuery(query)) {
          while (rs.next()) {
              String ipAddress = rs.getString("ip_address");
              String deviceName = rs.getString("device_name");
              String message = rs.getString("message");
              String status = rs.getString("status");

              // Add the data to the table model
              tableModel.addRow(new Object[]{ipAddress, deviceName, message, status});
          }
          appendMessage("Existing data loaded into the table successfully.\n");
      } catch (SQLException e) {
          appendMessage("Error loading data from database: " + e.getMessage() + "\n");
      }
   }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == sendButton) {
            String message = inputField.getText().trim();
            if (message.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a message before sending.", "Warning", JOptionPane.WARNING_MESSAGE);
            } else {
                synchronized (clients) {
                    if (clients.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "No clients connected.", "Info", JOptionPane.INFORMATION_MESSAGE);
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
                inputField.setText("");
            }
        }

        if (e.getSource() == pendingButton) {
            updateClientStatus("Pending");
        }
        if (e.getSource() == solveButton) {
            updateClientStatus("Solved");
        }
        if (e.getSource() == doneButton) {
            updateClientStatus("Done");
        }
    }

    // Method to update client status in the table and database
    private void updateClientStatus(String status) {
        int selectedRow = clientTable.getSelectedRow();
        if (selectedRow != -1) {
            String ipAddress = (String) tableModel.getValueAt(selectedRow, 0);
            String deviceName = (String) tableModel.getValueAt(selectedRow, 1);
            String message = (String) tableModel.getValueAt(selectedRow, 2);

            // Update status in the database
            updateStatusInDatabase(ipAddress, status);

            // Update status in the table
            tableModel.setValueAt(status, selectedRow, 3);
            appendMessage("Status updated to " + status + " for " + deviceName + " (" + ipAddress + ").\n");
        } else {
            JOptionPane.showMessageDialog(this, "Please select a client from the table.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    // Update the status in the database
    private void updateStatusInDatabase(String ipAddress, String status) {
        String query = "UPDATE ClientData SET status = ? WHERE ip_address = ?";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setString(1, status);
            pstmt.setString(2, ipAddress);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            appendMessage("Error updating status: " + e.getMessage() + "\n");
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
        // Get the current time and format it
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss");
        String timestamp = sdf.format(new java.util.Date());
        
        // Append timestamp with message to the message area
        SwingUtilities.invokeLater(() -> {
            messageArea.append("[" + timestamp + "] " + message);
        });
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
                    java.util.Date currentDate = new java.util.Date(); // Store the current date

                    // Store the information in the database and table
                    storeClientData(currentDate, deviceName, message, ipAddress);
                }
            } catch (IOException e) {
                appendMessage("Client disconnected: " + e.getMessage() + "\n");
            } finally {
                close();
            }
        }

        private void storeClientData(java.util.Date timestamp, String deviceName, String message, String ipAddress) {
            String query = "INSERT INTO ClientData (timestamp, device_name, message, ip_address, status) VALUES (?, ?, ?, ?, 'Pending')";
            try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
                pstmt.setTimestamp(1, new java.sql.Timestamp(timestamp.getTime())); // Ensure timestamp is saved correctly
                pstmt.setString(2, deviceName);
                pstmt.setString(3, message);
                pstmt.setString(4, ipAddress);
                pstmt.executeUpdate();
                appendMessage("Client data stored in database successfully with status 'Pending'.\n");

                // Add client to table
                SwingUtilities.invokeLater(() -> {
                    tableModel.addRow(new Object[]{ipAddress, deviceName, message, "Pending"});
                });
            } catch (SQLException e) {
                appendMessage("Error storing client data: " + e.getMessage() + "\n");
            }
        }

        public boolean isActive() {
            return active;
        }

        public void sendMessage(String message) {
            try {
                dos.writeUTF(message);
            } catch (IOException e) {
                appendMessage("Error sending message to client: " + e.getMessage() + "\n");
            }
        }

        private void close() {
            try {
                clientSocket.close();
                appendMessage("Client connection closed.\n");
            } catch (IOException e) {
                appendMessage("Error closing client connection: " + e.getMessage() + "\n");
            }
        }
    }

    public static void main(String[] args) {
        // Start the server and UI in separate threads
        SwingUtilities.invokeLater(() -> {
            ServerUI server = new ServerUI();
            new Thread(server::startServer).start();
        });
    }
}

