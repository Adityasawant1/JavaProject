import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.Date;
import com.toedter.calendar.JDateChooser;
import java.text.SimpleDateFormat;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.awt.Desktop;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import javax.swing.SwingUtilities;


public class ServerUI extends JFrame implements ActionListener {
    private JComboBox<String> labDropdown;  // Declare at class level
    private JDateChooser startDateChooser, endDateChooser; 
    private JTextArea logArea,reportArea;    
    private JTextField inputField;
    private JButton sendButton, pendingButton, solveButton, doneButton,generateReportButton,printReportButton;
    private JTable clientTable,reportTable;
    private DefaultTableModel tableModel,reportTableModel;
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
        headerLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 24));

        headerPanel.add(headerLabel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Tabbed Panel for Main Content
        JTabbedPane tabbedPane = new JTabbedPane();

        // Messaging Panel
        JPanel messagingPanel = new JPanel(new BorderLayout());
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);
        inputField = new JTextField();
        sendButton = new JButton("Send");

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        messagingPanel.add(logScrollPane, BorderLayout.CENTER);
        messagingPanel.add(inputPanel, BorderLayout.SOUTH);
        tabbedPane.addTab("Messaging", messagingPanel);

        // Client Management Tab
        JPanel clientManagementPanel = new JPanel(new BorderLayout());
        String[] columnNames = {"ID", "Device Name", "Lab Name", "Message", "Date", "Created At", "Resolved At", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0){
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // Disable editing
            }
        };
        clientTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(clientTable);

        JPanel clientButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        pendingButton = new JButton("Pending");
        solveButton = new JButton("Mark Solved");
        doneButton = new JButton("Mark Done");
        clientButtonPanel.add(pendingButton);
        clientButtonPanel.add(solveButton);
        clientButtonPanel.add(doneButton);

        clientManagementPanel.add(tableScrollPane, BorderLayout.CENTER);
        clientManagementPanel.add(clientButtonPanel, BorderLayout.SOUTH);
        tabbedPane.addTab("Client Management", clientManagementPanel);

        // Report Generation Tab
        JPanel reportPanel = new JPanel(new BorderLayout());
        String[] reportColumnNames = {"ID", "Device Name", "Message", "IP Address","Lab", "Timestamp", "Resolved At", "Status"};
        reportTableModel = new DefaultTableModel(reportColumnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Disable editing
            }
        };
         reportTable = new JTable(reportTableModel);

        // Use the JTable inside JScrollPane
        JScrollPane reportScrollPane = new JScrollPane(reportTable);
        generateReportButton = new JButton("Generate Report");
        printReportButton = new JButton("Print Report"); // New Print Button

        // Lab Selection Dropdown
        String[] labs = {"All", "Lab 1", "Lab 2", "Lab 3", "Lab 4","Lab 5","Lab 6"};
        labDropdown = new JComboBox<>(labs);
        labDropdown.setSelectedIndex(0);

        // Increase the width of the ComboBox
        labDropdown.setPreferredSize(new Dimension(150, 25));

        // Date Pickers
        startDateChooser = new JDateChooser();
        endDateChooser = new JDateChooser();
        startDateChooser.setDate(new Date());
        endDateChooser.setDate(new Date());

        startDateChooser.setPreferredSize(new Dimension(150, 25));
        endDateChooser.setPreferredSize(new Dimension(150, 25));

        // Panel for Lab Selection & Date Filtering
        JPanel labSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        labSelectionPanel.add(new JLabel("Select Lab:"));
        labSelectionPanel.add(labDropdown);
        labSelectionPanel.add(new JLabel("Start Date:"));
        labSelectionPanel.add(startDateChooser);
        labSelectionPanel.add(new JLabel("End Date:"));
        labSelectionPanel.add(endDateChooser);

        // Panel for buttons (Generate Report & Print Report)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(generateReportButton);
        buttonPanel.add(printReportButton); // Added Print Button




        reportPanel.add(labSelectionPanel, BorderLayout.NORTH);
        reportPanel.add(reportScrollPane, BorderLayout.CENTER);
        reportPanel.add(buttonPanel, BorderLayout.SOUTH); // Add button panel at bottom
        tabbedPane.addTab("Report Generation", reportPanel);

        // Footer Panel
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel footerLabel = new JLabel("Developed by Aditya | 2025", JLabel.RIGHT);
        footerPanel.add(footerLabel);
        add(footerPanel, BorderLayout.SOUTH);

        // Add TabbedPane to Frame
        add(tabbedPane, BorderLayout.CENTER);

        // Register action listeners
        sendButton.addActionListener(this);
        pendingButton.addActionListener(this);
        solveButton.addActionListener(this);
        doneButton.addActionListener(this);
        generateReportButton.addActionListener(this);
        printReportButton.addActionListener(this);
        
        initializeDatabase();
        loadDataFromDatabase();
        setVisible(true);
    }



            private void initializeDatabase() {
                try {
                    String url = "jdbc:mysql://localhost:3306/ClientServer";
                    String username = "root";
                    String password = "root";
                    dbConnection = DriverManager.getConnection(url, username, password);
                    appendMessage("Database connected successfully.\n");
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Connection Failed", "Warning", JOptionPane.WARNING_MESSAGE);
                    
                    System.exit(1);
                }
            }
            private void loadDataFromDatabase() {
            String query = "SELECT id, device_name,lab_name, message, status, timestamp, created_at, resolved_at FROM ClientData ORDER BY CASE status WHEN 'Pending' THEN 1 WHEN 'Solved' THEN 2 WHEN 'Done' THEN 3 ELSE 4 END, id";
            try (Statement stmt = dbConnection.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                tableModel.setRowCount(0); // Clear existing rows
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String deviceName = rs.getString("device_name");
                    String labname=rs.getString("lab_name");
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
                    tableModel.addRow(new Object[]{id,deviceName,labname,message,timestampDate,createdAtTime,resolvedAtTime,status});
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
          if (e.getSource() == printReportButton) {
              GenerateReportPDF();
          }
      }
        private void GenerateReportPDF() {
          String selectedLab = (String) labDropdown.getSelectedItem();
          Date startDate = startDateChooser.getDate();
          Date endDate = endDateChooser.getDate();

          if (startDate == null || endDate == null) {
              JOptionPane.showMessageDialog(this, "Please select both start and end dates.", "Error", JOptionPane.ERROR_MESSAGE);
              return;
          }

          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
          String startDateStr = sdf.format(startDate);
          String endDateStr = sdf.format(endDate);

          String query = "SELECT * FROM ClientData WHERE timestamp BETWEEN ? AND ?";
          boolean isSpecificLabSelected = !selectedLab.equals("All");

          if (isSpecificLabSelected) {
              query += " AND lab_name = ?";
          }

          try (PreparedStatement stmt = dbConnection.prepareStatement(query)) {
              stmt.setString(1, startDateStr + " 00:00:00");
              stmt.setString(2, endDateStr + " 23:59:59");

              if (isSpecificLabSelected) {
                  stmt.setString(3, selectedLab);
              }

              ResultSet rs = stmt.executeQuery();

              String path = "Report.pdf";
              OutputStream file = new FileOutputStream(new File(path));
              Document document = new Document();
              PdfWriter.getInstance(document, file);
              document.open();

              // PDF Metadata
              document.addTitle("Lab Report");
              document.addSubject("Lab Report Data");
              document.addKeywords("Lab, Report, Data");
              document.addAuthor("System Generated");
              document.addCreator("Automated Report Generator");

              // Title
              document.add(new Paragraph("Lab Report", com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 18, com.itextpdf.text.Font.BOLD, com.itextpdf.text.BaseColor.BLACK)));

              document.add(new Paragraph("Lab: " + selectedLab));
              document.add(new Paragraph("Report Duration: " + startDateStr + " to " + endDateStr));
              document.add(new Paragraph("\n"));

              // Creating Table
              PdfPTable table = new PdfPTable(8); // 7 columns for all data fields
              table.setWidthPercentage(100);
              table.setSpacingBefore(10f);
              table.setSpacingAfter(10f);

              // Table Headers
              String[] headers = {"ID", "Device", "Message","Lab","IP","Date", "Resolved At", "Status"};
              for (String header : headers) {
                  PdfPCell cell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
                  cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                  cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                  table.addCell(cell);
              }

              boolean hasData = false;
              while (rs.next()) {
                  hasData = true;
                  table.addCell(String.valueOf(rs.getInt("id")));
                  table.addCell(rs.getString("device_name"));
                  table.addCell(rs.getString("message"));
                  table.addCell(rs.getString("lab_name"));
                  table.addCell(rs.getString("ip_address"));            
                  table.addCell(rs.getDate("timestamp").toString());
                  table.addCell(rs.getTime("resolved_at") != null ? rs.getTime("resolved_at").toString() : "N/A");
                  table.addCell(rs.getString("status"));
              }

              if (hasData) {
                  document.add(table);
              } else {
                  document.add(new Paragraph("No records found for the selected criteria.", FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.RED)));
              }

              document.close();
              file.close();

              Desktop desktop = Desktop.getDesktop();
              desktop.open(new File(path)); // Open the PDF file automatically

              } catch (SQLException | IOException | DocumentException e) {
                  e.printStackTrace();
                  JOptionPane.showMessageDialog(this, "Error generating PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
              }
          }

          private void generateReport() {
            String selectedLab = (String) labDropdown.getSelectedItem();
            Date startDate = startDateChooser.getDate();
            Date endDate = endDateChooser.getDate();

            if (startDate == null || endDate == null) {
                JOptionPane.showMessageDialog(this, "Please select both start and end dates.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String startDateStr = sdf.format(startDate);
            String endDateStr = sdf.format(endDate);

            String query = "SELECT * FROM ClientData WHERE timestamp BETWEEN ? AND ?";
            boolean isSpecificLabSelected = !selectedLab.equals("All");

            if (isSpecificLabSelected) {
                query += " AND lab_name = ?";
            }

            try (PreparedStatement stmt = dbConnection.prepareStatement(query)) {
                stmt.setString(1, startDateStr + " 00:00:00");
                stmt.setString(2, endDateStr + " 23:59:59");

                if (isSpecificLabSelected) {
                    stmt.setString(3, selectedLab);
                }

                ResultSet rs = stmt.executeQuery();

                // Clear existing rows
                reportTableModel.setRowCount(0);

                boolean hasData = false;
                while (rs.next()) {
                    hasData = true;
                    Object[] rowData = {
                        rs.getInt("id"),
                        rs.getString("device_name"),
                        rs.getString("message"),
                        rs.getString("ip_address"),
                        rs.getString("lab_name"),
                        rs.getDate("timestamp"),
                        rs.getTime("resolved_at"),
                         rs.getString("status")
                    };
                    reportTableModel.addRow(rowData);
                }

                if (!hasData) {
                    JOptionPane.showMessageDialog(this, "No records found for the selected criteria.", "Info", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error generating report: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
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
                    serverSocket = new ServerSocket(8000);
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
            // Get current time in IST (HH:mm:ss)
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            String timestamp = ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).format(formatter);

            // Append timestamp with message to the log area
            SwingUtilities.invokeLater(() -> {
                logArea.append("[" + timestamp + "] " + message + "\n");
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
            String labNumber = lines[1].split(": ", 2)[1]; // Extract Lab number
            String deviceName = lines[2].split(": ", 2)[1];
            String userMessage = lines[3].split(": ", 2)[1];
            String ipAddress = lines[4].split(": ", 2)[1];

            // Convert the parsed timestamp to java.sql.Time (only the time)
            java.util.Date parsedDate = new java.text.SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH).parse(timestampLine);
            
         
            // Insert the parsed data into the database and retrieve the generated id
            String query = "INSERT INTO ClientData (timestamp, lab_name, device_name, message, ip_address, status, created_at) VALUES (?, ?, ?, ?, ?, 'Pending', CURRENT_TIME)";
            try (PreparedStatement pstmt = dbConnection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setDate(1, new java.sql.Date(parsedDate.getTime())); // Set date
                pstmt.setString(2, labNumber); // Set Lab Number
                pstmt.setString(3, deviceName);
                pstmt.setString(4, userMessage);
                pstmt.setString(5, ipAddress);
                pstmt.executeUpdate();

                // Retrieve the generated id
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1); // Retrieve the generated id
                        appendMessage("\nClient data stored in database successfully with status 'Pending' and ID: " + id + ".\n");

                        // Add client data to the table
                        SwingUtilities.invokeLater(() -> {
                        tableModel.insertRow(0, new Object[]{
                            id, 
                            deviceName, 
                            labNumber, 
                            userMessage, 
                            new java.sql.Date(parsedDate.getTime()), // Convert to SQL Date
                            new java.sql.Time(System.currentTimeMillis()), // Created At (CURRENT_TIMESTAMP)
                            null, // Resolved At (Initially NULL)
                            "Pending" // Status
                        });
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

