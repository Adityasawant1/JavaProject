import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;

class ClientUI extends JFrame implements ActionListener {
    JLabel l1;
    JTextArea message;
    JTextArea receivedMessages;
    JButton send, ext;
    Socket s;
    DataOutputStream dos;
    DataInputStream dis;

    ClientUI(String title) {
        super(title);
        setLayout(null);
        setSize(700, 700);
        setVisible(true);
        setLocation(250, 200);

        l1 = new JLabel("Mention Problem:");
        ext = new JButton("EXIT");
        send = new JButton("SEND");
        message = new JTextArea("");
        receivedMessages = new JTextArea();

        // Add borders and set receivedMessages as non-editable
        message.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        receivedMessages.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        receivedMessages.setEditable(false);

        // Wrap the JTextAreas in JScrollPanes
        JScrollPane messageScrollPane = new JScrollPane(message);
        JScrollPane receivedScrollPane = new JScrollPane(receivedMessages);

        add(l1);
        add(messageScrollPane);
        add(receivedScrollPane);
        add(send);
        add(ext);

        send.addActionListener(this);
        ext.addActionListener(this);

        l1.setBounds(10, 10, 150, 30);
        messageScrollPane.setBounds(10, 50, 300, 100); // For sending messages
        receivedScrollPane.setBounds(10, 200, 300, 200); // For displaying received messages
        send.setBounds(10, 160, 80, 20);
        ext.setBounds(100, 160, 80, 20);

        try {
            // Attempt to connect to the server
            s = new Socket("localhost", 5000); // Replace "localhost" with server IP if needed
            dos = new DataOutputStream(s.getOutputStream());
            dis = new DataInputStream(s.getInputStream());

            // Start a thread to listen for messages from the server
            new Thread(this::listenForMessages).start();
        } catch (ConnectException ce) {
            
            JOptionPane.showMessageDialog(this, "Unable to connect to the server. Please try again later.", "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0); // Exit the application gracefully
        } catch (IOException e) {
            e.printStackTrace();
        }

        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getSource() == send) {
                // Get the message, IP address, and device name
                String userMessage = message.getText();

                if (userMessage.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Mention the problem.", "Warning", JOptionPane.WARNING_MESSAGE);
                return; // Exit the method to prevent sending an empty message
                }
                InetAddress inetAddress = InetAddress.getLocalHost();
                String ipAddress = inetAddress.getHostAddress();
                String deviceName = inetAddress.getHostName();
                java.util.Date currentDate = new java.util.Date();

                // Combine all information into a single message
                String combinedMessage = "Timestamp: " + currentDate +
                                         "\nDevice Name: " + deviceName +
                                         "\nMessage: " + userMessage +
                                         "\nIP Address: " + ipAddress;

                dos.writeUTF(combinedMessage);

                // Clear the message input field and show confirmation
                message.setText("");
                JOptionPane.showMessageDialog(null, "Message sent to server.");
            }

            if (e.getSource() == ext) {
                dos.writeUTF("exit");
                System.exit(0);
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private void listenForMessages() {
        try {
            while (true) {
                String serverMessage = dis.readUTF();
                receivedMessages.append("Server: " + serverMessage + "\n");
            }
        } catch (IOException e) {
            receivedMessages.append("Connection to server lost.\n");
        }
    }

    public static void main(String args[]) {
        new ClientUI("Client Application");
    }
}

