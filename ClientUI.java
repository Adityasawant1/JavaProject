import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.sql.*;

class ClientUI extends JFrame implements ActionListener {
    JLabel l1, noteLabel;
    JTextArea message;
    JTextArea receivedMessages;
    JButton send, ext;
    Socket s;
    DataOutputStream dos;
    DataInputStream dis;
    Connection dbConnection;

    ClientUI(String title) {
        super(title);
        setLayout(null);
        setSize(700, 700);
        setLocation(250, 200);

        l1 = new JLabel("Mention Problem:");
        noteLabel = new JLabel("Note: Message can be sent only once until the mentioned error gets solved.");
        noteLabel.setForeground(Color.RED);
        ext = new JButton("EXIT");
        send = new JButton("SEND");
        message = new JTextArea("");
        receivedMessages = new JTextArea();

        message.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        receivedMessages.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        receivedMessages.setEditable(false);

        JScrollPane messageScrollPane = new JScrollPane(message);
        JScrollPane receivedScrollPane = new JScrollPane(receivedMessages);

        add(l1);
        add(noteLabel);
        add(messageScrollPane);
        add(receivedScrollPane);
        add(send);
        add(ext);

        send.addActionListener(this);
        ext.addActionListener(this);

        l1.setBounds(10, 10, 150, 30);
        noteLabel.setBounds(10, 30, 500, 20);
        messageScrollPane.setBounds(10, 60, 300, 100);
        receivedScrollPane.setBounds(10, 220, 300, 200);
        send.setBounds(10, 180, 80, 20);
        ext.setBounds(100, 180, 80, 20);

        try {
            // Initialize database connection
            dbConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/ClientServer", "root", "Aditya@2005");

            // Initialize socket connection
            s = new Socket("localhost", 5000);
            dos = new DataOutputStream(s.getOutputStream());
            dis = new DataInputStream(s.getInputStream());

            // Start a thread to listen for messages from the server
            new Thread(this::listenForMessages).start();
        } catch (ConnectException ce) {
            JOptionPane.showMessageDialog(this, "Unable to connect to the server. Please try again later.", "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        } catch (SQLException | IOException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getSource() == send) {
                try {
                    InetAddress inetAddress = InetAddress.getLocalHost();
                    String ipAddress = inetAddress.getHostAddress();
                    String deviceName = inetAddress.getHostName();
                    java.util.Date currentDate = new java.util.Date();

                    String query = "SELECT COUNT(*) AS count FROM ClientData WHERE device_name = ? AND status = 'Pending'";
                    try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
                        pstmt.setString(1, deviceName);
                        try (ResultSet rs = pstmt.executeQuery()) {
                            if (rs.next() && rs.getInt("count") > 0) {
                                JOptionPane.showMessageDialog(this, "First request is still pending.", "Warning", JOptionPane.WARNING_MESSAGE);
                                return;
                            }
                        }
                    }

                    String userMessage = message.getText();
                    if (userMessage.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Mention the problem.", "Warning", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    String combinedMessage = "Timestamp: " + currentDate +
                                             "\nDevice Name: " + deviceName +
                                             "\nMessage: " + userMessage +
                                             "\nIP Address: " + ipAddress;

                    dos.writeUTF(combinedMessage);
                    message.setText("");
                    JOptionPane.showMessageDialog(this, "Message sent to server.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            if (e.getSource() == ext) {
                dos.writeUTF("exit");
                closeResources();
                System.exit(0);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void listenForMessages() {
        try {
            while (true) {
                String serverMessage = dis.readUTF();
                SwingUtilities.invokeLater(() -> receivedMessages.append("Server: " + serverMessage + "\n"));
            }
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> receivedMessages.append("Connection to server lost.\n"));
        }
    }

    private void closeResources() {
        try {
            if (dis != null) dis.close();
            if (dos != null) dos.close();
            if (s != null) s.close();
            if (dbConnection != null) dbConnection.close();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new ClientUI("Client Application");
    }
}

