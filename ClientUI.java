import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.sql.*;

class ClientUI extends JFrame implements ActionListener {
    JLabel l1, labLabel, noteLabel;
    JTextArea message;
    JTextArea receivedMessages;
    JButton send, ext;
    JComboBox<String> labDropdown;
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
        labLabel = new JLabel("Select Lab:");
        noteLabel = new JLabel("Note: Message can be sent only once until the mentioned error gets solved.");
        noteLabel.setForeground(Color.RED);
        ext = new JButton("EXIT");
        send = new JButton("SEND");
        message = new JTextArea("");
        receivedMessages = new JTextArea();
        
        receivedMessages.setEditable(false);
        message.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        receivedMessages.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JScrollPane messageScrollPane = new JScrollPane(message);
        JScrollPane receivedScrollPane = new JScrollPane(receivedMessages);
        
        labDropdown = new JComboBox<>(new String[]{"Default","Lab 1", "Lab 2", "Lab 3", "Lab 4","Lab 5","Lab 6"});
        
        add(l1);
        add(labLabel);
        add(labDropdown);
        add(noteLabel);
        add(messageScrollPane);
        add(receivedScrollPane);
        add(send);
        add(ext);

        send.addActionListener(this);
        ext.addActionListener(this);

        noteLabel.setBounds(10, 5, 500, 20); // Move note to the top
        l1.setBounds(10, 60, 150, 30);
        labLabel.setBounds(10, 30, 150, 30);
        labDropdown.setBounds(100, 30, 150, 30);
        messageScrollPane.setBounds(10, 100, 250, 100);
        send.setBounds(10, 220, 80, 20);
        ext.setBounds(100, 220, 80, 20);
        receivedScrollPane.setBounds(10, 270, 250, 100);

        NetworkConnection();
        
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getSource() == send) {
                InetAddress inetAddress = InetAddress.getLocalHost();
                String ipAddress = inetAddress.getHostAddress();
                String deviceName = inetAddress.getHostName();
                String selectedLab = (String) labDropdown.getSelectedItem();
                java.util.Date currentDate = new java.util.Date();
                
                // Check if "Default" is selected
                if ("Default".equals(selectedLab)) {
                    JOptionPane.showMessageDialog(this, "Please select a valid lab.", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
              
                String userMessage = message.getText();
                if (userMessage.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Mention the problem.", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                String combinedMessage = "Timestamp: " + currentDate +
                                         "\nLab: " + selectedLab +
                                         "\nDevice Name: " + deviceName +
                                         "\nMessage: " + userMessage +
                                         "\nIP Address: " + ipAddress;
                
                dos.writeUTF(combinedMessage);
                message.setText("");
                JOptionPane.showMessageDialog(this, "Message sent to server.");
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
    private void NetworkConnection(){
    try {
            s = new Socket("192.168.0.107", 5000);
            dos = new DataOutputStream(s.getOutputStream());
            dis = new DataInputStream(s.getInputStream());
            new Thread(this::listenForMessages).start();
        } catch (ConnectException ce) {
            JOptionPane.showMessageDialog(this, "Unable to connect to the server. Please try again later.", "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
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

