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

public class ServerSide implements Runnable {
    private JButton buttonEnter;
    private JTextField textField;
    private JLabel jLabel;
    private JPanel panelEnter;
    private JPanel panelListen;
    private JProgressBar progressBar;
    private JLabel portValue;
    private JButton newPort;
    private Thread socketThread;
    private String port;
    private DatagramSocket datagramSocket = null;
    public void createSocket(int port_no){
        try {
            this.datagramSocket = new DatagramSocket(port_no);
        }catch (SocketException exception){
            JOptionPane.showMessageDialog(null, exception.getMessage());
        }
    }
    public DatagramSocket getSocket(){
        return this.datagramSocket;
    }
    public void setPort(String portno){
        this.port = portno;
    }
    public String getPort(){
        return this.port;
    }
    public ServerSide(){
        DatagramSocket datagramSocket = null;
    }
    public ServerSide(JFrame jFrame){
        ServerSide server = new ServerSide();
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setTitle("Server");
        jFrame.setResizable(false);
        jFrame.setVisible(true);
        jFrame.setLocationRelativeTo(null);

        panelEnter = new JPanel();
        panelListen = new JPanel();
        panelEnter.setVisible(true);
        panelListen.setVisible(false);

        //panel Enter
        textField = new JTextField();
        jLabel = new JLabel("Enter the number of port to listen: ");
        buttonEnter = new JButton("Enter");
        panelEnter.setBorder(BorderFactory.createEmptyBorder(70,40,70,40));
        panelEnter.setLayout(new GridLayout(0,3));
        jFrame.add(panelEnter,BorderLayout.CENTER);
        panelEnter.add(jLabel);
        panelEnter.add(textField);
        panelEnter.add(buttonEnter);
        //samo unos brojeva
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar())){
                    e.consume();
                }
            }
        });
        buttonEnter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // progress bar
                if(textField.getText().length()==0){
                    JOptionPane.showMessageDialog(null,"Morate unijeti broj porta!!!");
                    return;
                }try{
                    if(Integer.parseInt(textField.getText())<1024) {
                        JOptionPane.showMessageDialog(null, "Please enter number greater than 1024," +
                                " because these ports are used for other services");
                        return;
                    }
                } catch (final NumberFormatException exception){
                    JOptionPane.showMessageDialog(null, "Port doesn't exist, maximal number of ports is 65535!");
                    return;
                }
                try {
                    if(Integer.parseInt(textField.getText()) > 65535){
                        JOptionPane.showMessageDialog(null, "Port doesn't exist, maximal number of ports is 65535!");
                        return;
                    };

                }
                catch (final NumberFormatException exception){
                    System.out.print(exception);
                    JOptionPane.showMessageDialog(null, "Port doesn't exist, maximal number of ports is 65535!");
                    return;
                }
                String port = textField.getText();
                server.setPort(port);
                jFrame.remove(panelEnter);
                jFrame.add(panelListen,BorderLayout.CENTER);
                panelListen.setVisible(true);
                portValue.setText(portValue.getText()+port);
                int socket_no = Integer.parseInt(server.getPort());
                server.createSocket(socket_no);
                //pravimo socket i za ovo moramo da koristimo novi thread, jer ED thread ne podrzava uporedno kreiranje socketa
                socketThread = new Thread(server);
                socketThread.start();
            }
        });

        //panel Listen
        progressBar = new JProgressBar();
        newPort = new JButton("Listen another port?");
        panelListen.setBorder(BorderFactory.createEmptyBorder(50,40,50,40));
        panelListen.setLayout(new GridLayout(3,0));
        panelListen.add(progressBar);
        final Timer t = new Timer(35, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                progressBar.setValue(progressBar.getValue() + 1);
                if (progressBar.getValue() == 100) {
                    progressBar.setValue(0);
                }
            }
        });

        t.start();

        portValue = new JLabel("Currently listening port: ", SwingConstants.CENTER);
        portValue.setFont(new Font("Arial",Font.BOLD,17));
        panelListen.add(portValue);
        panelListen.add(newPort);

        newPort.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                socketThread.interrupt();
                textField.setText("");
                portValue.setText("Currently listening port: ");
                panelListen.setVisible(false);
                jFrame.remove(panelListen);
                jFrame.add(panelEnter, BorderLayout.CENTER);
                panelEnter.setVisible(true);
                server.getSocket().close();
            }
        });
        jFrame.pack();
    }
    public void run(){
        try {
            System.out.println(this.getPort());
            //max duzina poruke koju moze da primi
            byte[] buffer = new byte[500];
            //beskonacno
            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                System.out.println("Waiting...");
                this.getSocket().receive(request);
                try{
                    String number = "";
                    System.out.println("Request incoming:" +new String(request.getData()).trim());
                    //posto se radi o UDP protokolu koji ne provjerava greske, pogotovo prilikom slanja brojeva,
                    // desava se da prilikom mjenjanja desetica (vise na dolje) ostaje broj bajtova za razliku tih desetica, stoga, prilikom konverzije u string na primjeru
                    // dobijamo da on pretvara poruku sa brojem 9 u broj 96, ako je prethodno koriscen broj 16
                    // to smo rijesili tako sto smo uzeli duzinu requsta koju klijent salje, i uvijek ce prvi bajtovi(koliko je duzina req) uvijek biti tacni
                    if(new String(request.getData()).trim().length()!=request.getLength()){
                        number = new String(request.getData()).trim().substring(0,request.getLength());
                    }else{
                        number = new String(request.getData()).trim();
                    }
                    BigInteger messageNumber = new BigInteger(number);
                    if(messageNumber.compareTo(BigInteger.ZERO)<0){
                        byte message[] = ("Not defined").getBytes(StandardCharsets.US_ASCII);
                        DatagramPacket reply = new DatagramPacket(message, message.length, request.getAddress(), request.getPort());
                        this.getSocket().send(reply);
                        continue;
                    }
                    String result = String.valueOf(Math.sqrt(messageNumber.doubleValue()));
                    byte [] message = ("The square root of " + number + " is "+ result).getBytes(StandardCharsets.US_ASCII);
                    DatagramPacket reply = new DatagramPacket(message, message.length,request.getAddress(),request.getPort());
                    this.getSocket().send(reply);
                }catch (NumberFormatException exception){
                    byte [] message = ("Not a number").getBytes(StandardCharsets.US_ASCII);
                    DatagramPacket reply = new DatagramPacket(message, message.length, request.getAddress(), request.getPort());
                    this.getSocket().send(reply);
                    continue;
                }
            }
        }
        catch (SocketException exception) {
            System.out.println("Socket on port no. " + this.getPort() + " closed");
            this.setPort("");
            return;
        } catch (IOException IOexception) {
            JOptionPane.showMessageDialog(null, "There is I/O problem: " + IOexception.getMessage());

        } finally{
            if (this.getSocket() != null){
                System.out.println("Done");
            }
        }
        System.out.println("Exit on while");
    }
    public static void main(String [] args){
        JFrame jFrame = new JFrame();
        new ServerSide(jFrame);
    }


}
