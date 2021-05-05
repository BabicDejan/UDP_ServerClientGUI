import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigInteger;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class ClientSide implements Runnable{
    private JButton buttonEnter;
    private JTextField textFieldHost;
    private JTextField textFieldPort;
    private JLabel labelHost;
    private JLabel labelPort;
    private JPanel panelEnter;
    private JPanel panelMessage;

    private JLabel mLabelHost;
    private JLabel mLabelPortRec;
    private JLabel mLabelPortSend;
    private JTextField mFieldSend;
    private JTextField message;
    private JLabel labelMessage;
    private JButton buttonSend;
    private JLabel mHostVal;
    private JLabel mPortVal;

    private String portRecipient;
    private String portSender;
    private String hostRecipient;
    private String messageNumber;

    private JPanel host;
    private JPanel port;
    private JPanel button;

    private JPanel one;
    private JPanel two;
    private JPanel three;
    private JPanel four;
    private JPanel five;
    private JPanel six;

    private JButton buttonBack;

    private DatagramSocket datagramSocket = null;
    private Thread clThread;
    public ClientSide(){

    }

    public DatagramSocket getDatagramSocket(){
        return  this.datagramSocket;
    }

    public void setPortRecipient(String portRecipient) {
        this.portRecipient = portRecipient;
    }
    public String getPortRecipient(){
        return this.portRecipient;
    }

    public void setPortSender(String portSender){
        this.portSender = portSender;
    }
    public String getPortSender(){
        return this.portSender;
    }

    public void setHostRecipient(String host){
        this.hostRecipient = host;
    }

    public String getHostRecipient(){
        return this.hostRecipient;
    }

    public void setMessageNumber(String messageNumber){
        //seter pozivam tek posle validacije podatka!
        this.messageNumber = messageNumber;
    }

    public String getMessageNumber(){
        return this.messageNumber;
    }

    public ClientSide(JFrame jFrame){
        ClientSide client = new ClientSide();
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setTitle("Client");
        jFrame.setResizable(false);
        jFrame.setVisible(true);
        jFrame.setLocationRelativeTo(null);

        panelEnter = new JPanel();
        panelMessage = new JPanel();
        panelEnter.setVisible(true);
        panelMessage.setVisible(false);


        host = new JPanel(new GridLayout(1,2));
        port = new JPanel(new GridLayout(1,2));
        button = new JPanel(new GridLayout(1,1));

        //panel Enter
        panelEnter.setBorder(BorderFactory.createEmptyBorder(70,40,70,40));
        panelEnter.setLayout(new GridLayout(3,0));
        jFrame.add(panelEnter,BorderLayout.CENTER);

        // ************************************* PANEL ENTER CONTENT **********************

        textFieldHost = new JTextField();
        labelHost = new JLabel("Send the message to host: ");
        textFieldPort = new JTextField();
        labelPort = new JLabel("On port: ",SwingConstants.RIGHT);
        buttonEnter = new JButton("Enter");

        panelEnter.add(host);
        panelEnter.add(port);
        panelEnter.add(button);

        host.add(labelHost);
        host.add(textFieldHost);

        port.add(labelPort);
        port.add(textFieldPort);

        button.add(buttonEnter);


        //panel message
        //******************************************* PANEL MESSAGE CONTENT ******************************************

        mLabelHost = new JLabel("Send to: ");
        mHostVal = new JLabel();
        mLabelPortRec = new JLabel("On port no: ");
        mLabelPortSend = new JLabel("Send from port: ");
        mLabelPortSend.setForeground(Color.blue);
        mFieldSend = new JTextField();
        mFieldSend.setForeground(Color.blue);
        mPortVal = new JLabel();
        labelMessage = new JLabel("Enter the number to calculate its square root: ");
        message = new JTextField();
        buttonSend = new JButton("Send number");
        buttonBack = new JButton("Change recipient?");
        panelMessage.setBorder(BorderFactory.createEmptyBorder(70,20,70,20));
        panelMessage.setLayout(new GridLayout(6,0,2,2));

        one = new JPanel(new GridLayout(1,2));
        two = new JPanel(new GridLayout(1,2));
        three = new JPanel(new GridLayout(1,2));
        four = new JPanel(new GridLayout(1,2));
        five = new JPanel(new GridLayout(1,0));
        six = new JPanel(new GridLayout(1,0));

        panelMessage.add(one);
        panelMessage.add(two);
        panelMessage.add(four);
        panelMessage.add(three);
        panelMessage.add(five);
        panelMessage.add(six);

        one.add(mLabelHost);
        one.add(mHostVal);

        two.add(mLabelPortRec);
        two.add(mPortVal);

        three.add(labelMessage);
        three.add(message);

        four.add(mLabelPortSend);
        four.add(mFieldSend);

        five.add(buttonSend);

        six.add(buttonBack);

        CharInputRestriction restrict = new CharInputRestriction();
        textFieldPort.addKeyListener(restrict);
        mFieldSend.addKeyListener(restrict);

        buttonEnter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!validateEntries(textFieldPort.getText()) || !validateHost(textFieldHost.getText())){
                    return;
                }
                client.setHostRecipient(textFieldHost.getText());
                client.setPortRecipient(textFieldPort.getText());

                panelEnter.setVisible(false);
                jFrame.remove(panelEnter);
                jFrame.add(panelMessage, BorderLayout.CENTER);
                panelMessage.setVisible(true);
                mHostVal.setText(textFieldHost.getText());
                mHostVal.setForeground(Color.red);
                mPortVal.setText(textFieldPort.getText());
                mPortVal.setForeground(Color.red);

                jFrame.pack();

            }
        });

        buttonSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!validateEntries(mFieldSend.getText())){
                    return;
                }
                client.setPortSender(mFieldSend.getText());
                client.setMessageNumber(message.getText());
                //otvaranje thread za komunikaciju
                clThread = new Thread(client);
                clThread.start();
            }
        });

        buttonBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panelMessage.setVisible(false);
                jFrame.remove(panelMessage);
                jFrame.add(panelEnter,BorderLayout.CENTER);
                panelEnter.setVisible(true);
                textFieldHost.setText("");
                textFieldPort.setText("");
                jFrame.pack();
            }
        });

        jFrame.pack();

    }
    public Boolean validateHost(String host){
        if(host.length()==0) {
            JOptionPane.showMessageDialog(null,"You can't leave host field empty!");
            return false;
        }
        return true;
    }

    public Boolean validateEntries(String port){
        try{
            if(port.length() == 0){
                JOptionPane.showMessageDialog(panelEnter,"You must specify the number of the recipient port!");
                return false;
            }
            if(Integer.parseInt(port)<1024) {
                JOptionPane.showMessageDialog(null, "Please enter number greater than 1024," +
                        " because these ports are used for other services");
                return false;
            }
        } catch (final NumberFormatException exception){
            JOptionPane.showMessageDialog(null, "Port doesn't exist, maximal number of ports is 65535!");
            return false;
        }
        try{
            if(Integer.parseInt(port) > 65535){
                JOptionPane.showMessageDialog(null, "Port doesn't exist, maximal number of ports is 65535!");
                return false;
            }
        }catch (final NumberFormatException exception){
            System.out.print(exception);
            JOptionPane.showMessageDialog(null, "Port doesn't exist, maximal number of ports is 65535!");
            return false;
        }
        return true;
    }
//    public Boolean validateMessage(String message){
//        if(message.length()==0){
//            JOptionPane.showMessageDialog(null,"You can't send empty message!!!");
//        }
//        try{
//           new BigInteger(message);
//        }catch (NumberFormatException exception) {
//            JOptionPane.showMessageDialog(null,"You chose too big of a number!!");
//            return false;
//        }
//        return true;
//    }
    public class CharInputRestriction extends KeyAdapter {
        @Override
        public void keyTyped(KeyEvent e){

            if (!Character.isDigit(e.getKeyChar())) {
                    e.consume();
            }

        }
    }
    public void run() {
        try {
            datagramSocket = this.getDatagramSocket();
            datagramSocket = new DatagramSocket(Integer.valueOf(this.getPortSender()));
            System.out.println(this.getMessageNumber());
            byte[] message = this.getMessageNumber().trim().getBytes(StandardCharsets.US_ASCII);
            System.out.println(new String(message,StandardCharsets.US_ASCII));
            InetAddress aHost = InetAddress.getByName(this.getHostRecipient());
            int serverPort = Integer.valueOf(this.getPortRecipient()).intValue();
            DatagramPacket request = new DatagramPacket(message, message.length, aHost, serverPort);
            System.out.println(new String(request.getData()).trim());
            datagramSocket.send(request);
            System.out.println("Sent");
            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            datagramSocket.receive(reply);
            JOptionPane.showMessageDialog(panelMessage, "Server replied: "+ new String(reply.getData()).trim());
        } catch (SocketException e) {
            System.out.println("Socket " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO" + e.getMessage());
        } finally {
            if (datagramSocket!= null) {
                this.setMessageNumber("");
                datagramSocket.close();
            }
        }
    }
    public static void main(String [] args){
        JFrame jFrame = new JFrame();
        new ClientSide(jFrame);
    }
}

