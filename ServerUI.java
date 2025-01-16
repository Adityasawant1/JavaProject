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
    private JTextArea logArea,reportArea;    
    private JTextField inputField;
    private JButton sendButton, pendingButton, solveButton, doneButton,generateReportButton;
    private JTable clientTable;
    private DefaultTableModel tableModel;
    private Connection dbConnection;
    private ServerSocket serverSocket;
    private final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    
    public ServerUI() {
        // Set up the frame
       super("Server Dashboard");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());



         // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(45, 45, 45));
        JLabel headerLabel = new JLabel("Server Dashboard", JLabel.CENTER);
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(headerLabel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Tabbed Panel for Main Content
        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel messagingPanel = new JPanel(new BorderLayout());
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);
        inputField = new JTextField();
        sendButton = new JButton("Send");

          JPanel clientButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
          pendingButton = new JButton("Pending");
          solveButton = new JButton("Mark Solved");
          doneButton = new JButton("Mark Done");
          clientButtonPanel.add(pendingButton);
          clientButtonPanel.add(solveButton);
          clientButtonPanel.add(doneButton);
          

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        messagingPanel.add(logScrollPane, BorderLayout.CENTER);
        messagingPanel.add(inputPanel, BorderLayout.SOUTH);
        tabbedPane.addTab("Messaging", messagingPanel);

        // Add TabbedPane to Frame
        add(tabbedPane, BorderLayout.CENTER);

        // Client Management Tab
        JPanel clientManagementPanel = new JPanel(new BorderLayout());
        String[] columnNames = {"ID", "Device Name", "Message","Date","Created At", "Resolved At", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0);
        clientTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(clientTable);
        
        clientManagementPanel.add(tableScrollPane, BorderLayout.CENTER);
        clientManagementPanel.add(clientButtonPanel, BorderLayout.SOUTH);
        tabbedPane.addTab("Client Management", clientManagementPanel);
        
       // Report Generation Tab
        JPanel reportPanel = new JPanel(new BorderLayout());
        reportArea = new JTextArea();
        reportArea.setEditable(false);
        JScrollPane reportScrollPane = new JScrollPane(reportArea);
        generateReportButton = new JButton("Generate Report");

        reportPanel.add(reportScrollPane, BorderLayout.CENTER);
        reportPanel.add(generateReportButton, BorderLayout.SOUTH);
        tabbedPane.addTab("Report Generation", reportPanel);
        // Footer Panel
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel footerLabel = new JLabel("Developed by Aditya | 2025", JLabel.RIGHT);
        footerPanel.add(footerLabel);
        add(footerPanel, BorderLayout.SOUTH);

        
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
            JOptionPane.showMessageDialog(this, "Connection Failed", "Warning", JOptionPane.WARNING_MESSAGE);
            
            System.exit(1);
        }
    }
    private void loadDataFromDatabase() {
    String query = "SELECT id, device_name, message, status, timestamp, created_at, resolved_at FROM ClientData ORDER BY CASE status WHEN 'Pending' THEN 1 WHEN 'Solved' THEN 2 WHEN 'Done' THEN 3 ELSE 4 END, id";
    try (Statement stmt = dbConnection.createStatement();
         ResultSet rs = stmt.executeQuery(query)) {
        tableModel.setRowCount(0); // Clear existing rows
        while (rs.next()) {
            int id = rs.getInt("id");
            String deviceName = rs.getString("device_name");
            String message = rs.getString("message");
            String status = rs.getString("status");
            Timestamp timestamp = rs.getTimestamp("timestamp");
            Timestamp createdAt = rs.getTimestamp("created_at");
            Timestamp resolvedAt = rs.getTimestamp("resolved_at");

            // Format timestamp to show only date
            String timestampDate = timestamp != null ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(timestamp) : "";

            // Format created_at and resolved_at to show only time
            String createdAtTime = createdAt != null ? new java.text.SimpleDateFormat("HH:mm:ss").format(createdAt) : "";
            String resolvedAtTime = resolvedAt != null ? new java.text.SimpleDateFormat("HH:mm:ss").format(resolvedAt) : "";

            // Add the data to the table model
            tableModel.addRow(new Object[]{id,deviceName,message,timestampDate,createdAtTime,resolvedAtTime,status});
        }
        appendMessage("Data loaded and sorted by status successfully.\n");
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
        if (e.getSource() == generateReportButton) {
            generateReport();
        }
    }

  private void generateReport() {
        StringBuilder report = new StringBuilder();
        String query = "SELECT status, COUNT(*) as count FROM ClientData GROUP BY status";
        try (Statement stmt = dbConnection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("count");
                report.append("Status: ").append(status).append(" - ").append(count).append("\n");
            }
            reportArea.setText(report.toString());
        } catch (SQLException e) {
            appendMessage("Error generating report: " + e.getMessage() + "\n");
        }
    }

    // Method to update client status in the table and database
    private void updateClientStatus(String status) {
    int selectedRow = clientTable.getSelectedRow();
    if (selectedRow != -1) {
        int id = (int) tableModel.getValueAt(selectedRow, 0);
        updateStatusInDatabase(id, status);
        loadDataFromDatabase(); // Reload and re-sort the data
        appendMessage("Status updated to " + status + " for ID: " + id + ".\n");
    } else {
        JOptionPane.showMessageDialog(this, "Please select a client from the table.", "Warning", JOptionPane.WARNING_MESSAGE);
    }
}



    // Update the status in the database
   private void updateStatusInDatabase(int id, String status) {
    String query;
    if (status.equals("Done")) {
        // Store only time in 'resolved_at' when status is 'Done'
        query = "UPDATE ClientData SET status = ?, resolved_at = CURRENT_TIME WHERE id = ?";
    } else {
        // Clear the 'resolved_at' field when status is not 'Done'
        query = "UPDATE ClientData SET status = ?, resolved_at = NULL WHERE id = ?";
    }
    try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
        pstmt.setString(1, status);
        pstmt.setInt(2, id);
        pstmt.executeUpdate();
        appendMessage("Status updated to '" + status + "' for ID: " + id + ".\n");
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
                appendMessage("Client connected: " + clientSocket.getInetAddress().getHostAddress() + "\n");

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
            logArea.append("\n[" + timestamp + "] " + message);
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
                    appendMessage("Message from client: \n" + message + "\n");

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
                    storeClientData(message);
                }
            } catch (IOException e) {
                appendMessage("Client disconnected: " + e.getMessage() + "\n");
            } finally {
                close();
            }
        }

    private void storeClientData(String combinedMessage) {
    try {
        // Parse the combined message
        String[] lines = combinedMessage.split("\n");
        String timestampLine = lines[0].split(": ", 2)[1];
        String deviceName = lines[1].split(": ", 2)[1];
        String userMessage = lines[2].split(": ", 2)[1];
        String ipAddress = lines[3].split(": ", 2)[1];

        // Convert the parsed timestamp to java.sql.Time (only the time)
        java.util.Date parsedDate = new java.text.SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH).parse(timestampLine);
        java.sql.Time sqlTime = new java.sql.Time(parsedDate.getTime()); // Only the time part

        // Insert the parsed data into the database and retrieve the generated id
        String query = "INSERT INTO ClientData (timestamp, device_name, message, ip_address, status, created_at) VALUES (?, ?, ?, ?, 'Pending', ?)";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setDate(1, new java.sql.Date(parsedDate.getTime())); // Set date
            pstmt.setString(2, deviceName);
            pstmt.setString(3, userMessage);
            pstmt.setString(4, ipAddress);
            pstmt.setTime(5, sqlTime); // Set only the time
            pstmt.executeUpdate();

            // Retrieve the generated id
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1); // Retrieve the generated id
                    appendMessage("\nClient data stored in database successfully with status 'Pending' and ID: " + id + ".\n");

                    // Add client data to the table
                    SwingUtilities.invokeLater(() -> {
                        tableModel.insertRow(0, new Object[]{id, deviceName, userMessage, "Pending"}); // Insert at the top
                    });
                }
            }
        }
    } catch (Exception e) {
        appendMessage("Error parsing or storing client data: " + e.getMessage() + "\n");
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

