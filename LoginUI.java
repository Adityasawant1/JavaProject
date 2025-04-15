import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.sql.*;

class ClientUI extends JFrame implements ActionListener {
    JLabel l1, labLabel;
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
        
        add(messageScrollPane);
        add(receivedScrollPane);
        add(send);
        add(ext);

        send.addActionListener(this);
        ext.addActionListener(this);

        
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

                if ("Default".equals(selectedLab)) {
                    JOptionPane.showMessageDialog(this, "Please select a valid lab.", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String userMessage = message.getText();
                if (userMessage.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Mention the problem.", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String combinedMessage = "Flag: 0" +
                                         "\nTimestamp: " + currentDate +
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
            s = new Socket("localhost", 8000);
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

   
}
class LoginUI extends JFrame implements ActionListener {
    JLabel userLabel, passLabel;
    JTextField userField;
    JPasswordField passField;
    JButton loginButton;
    Socket socket;
    DataOutputStream dos;
    DataInputStream dis;

    LoginUI() {
        setTitle("Login Page");
        setLayout(null);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        userLabel = new JLabel("Username:");
        passLabel = new JLabel("Password:");
        userField = new JTextField();
        passField = new JPasswordField();
        loginButton = new JButton("Login");

        userLabel.setBounds(50, 50, 100, 30);
        userField.setBounds(150, 50, 150, 30);
        passLabel.setBounds(50, 100, 100, 30);
        passField.setBounds(150, 100, 150, 30);
        loginButton.setBounds(150, 150, 100, 30);

        add(userLabel);
        add(userField);
        add(passLabel);
        add(passField);
        add(loginButton);

        loginButton.addActionListener(this);

        try {
            socket = new Socket("localhost", 8000); // Connect to server
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Server not available", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        setVisible(true);
    }

    @Override
   public void actionPerformed(ActionEvent e) {
    try {
        String username = userField.getText();
        String password = new String(passField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and Password cannot be empty!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Format login message with flag 1
        String loginMessage = "Flag: 1\nUsername: " + username + "\nPassword: " + password;

        // Send login credentials to server
        dos.writeUTF(loginMessage);
        dos.flush();

        // Read server response
        String response = dis.readUTF();
        if (response.equals("LOGIN_SUCCESS")) {
            JOptionPane.showMessageDialog(this, "Login Successful!");
            dispose(); // Close the login window
            new ClientUI("Client Application"); // Open Client UI
        } else {
            JOptionPane.showMessageDialog(this, "Invalid Credentials", "Error", JOptionPane.ERROR_MESSAGE);
        }
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}


    public static void main(String[] args) {
        new LoginUI();
    }
}
