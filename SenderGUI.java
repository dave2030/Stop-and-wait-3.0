import java.awt.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;


public class SenderGUI extends JFrame {
    private final SenderHandler sendHand;

    public SenderGUI(String[] args) {
        sendHand = new SenderHandler();
        setTitle("Stop and Wait 3.0");
        setBounds(200, 200, 400, 500);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        components();
    }

    private static boolean available(int port) {
        System.out.println("Checking port " + port);
        try (var ss = new ServerSocket(port); var ds = new DatagramSocket(port)) {
            System.out.println("Port " + port + " is available (not alive) ");
            return true;
        } catch (IOException e) {
            System.out.println("Port " + port + " is not available (alive) ");
            return false;
        }
    }



    private void bSendHandler(ActionEvent e) {
        if (bSend.getText().equals("SEND")) {
            try {
                int sendinP = Integer.parseInt(tranSenP.getText());
                int receivinP = Integer.parseInt(tranReceivP.getText());
                String addy = tRecAddress.getText();
                String out = tOutFile.getText();
                int timeoutval=Integer.parseInt(ptimeOut.getText());


                new SwingWorker<Void, Void>() {
                    @Override
                    public Void doInBackground() {
                        try {
                            sendHand.startSending(addy, receivinP, sendinP, out,timeoutval);
                        } catch (IOException exception) {
                            exception.printStackTrace();
                        }
                        return null;
                    }
                }.execute();
                bSend.setText("TERMINATE");
            } catch (NumberFormatException exception) {
                JOptionPane.showMessageDialog(this, "Invalid port #", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {

            bSend.setText("SEND");
        }
    }
    private void bisAliveHandler(ActionEvent e) {
        if (bisAlive.getText().equals("Is Alive?")) {
                int receivinP = Integer.parseInt(tranReceivP.getText());
                available(receivinP);
        }
    }

    private void components() {
        par = new JPanel();
        par.setBorder(new EmptyBorder(5, 5, 10, 10));

        setContentPane(par);
        par.setLayout(new BorderLayout(0,0));
        par.setBackground(Color.BLUE);



        pMain = new JPanel();
        pMain.setBorder(new EmptyBorder(5, 5, 10, 10));
        pMain.setBackground(Color.ORANGE);

        par.add(pMain, BorderLayout.CENTER);
        pMain.setLayout(new BoxLayout(pMain, BoxLayout.Y_AXIS));

        pSendAddy = new JPanel();
        pSendAddy.setBorder(new EmptyBorder(5, 5, 10, 10));
        pMain.add(pSendAddy);
        pSendAddy.setLayout(new BorderLayout(0, 0));

        lSendAddy = new JLabel("IP Address of Sender:");
        lSendAddy.setFont(new Font("Serif", Font.PLAIN,20));
        lSendAddy.setBorder(new EmptyBorder(0, 0, 0, 0));
        pSendAddy.add(lSendAddy, BorderLayout.WEST);

        tRecAddress = new JTextField();
        tRecAddress.setText("127.0.0.1");
        pSendAddy.add(tRecAddress, BorderLayout.SOUTH);
        tRecAddress.setColumns(10);

        pSendPort = new JPanel();
        pSendPort.setBorder(new EmptyBorder(5, 5, 10, 10));
        pMain.add(pSendPort);
        pSendPort.setLayout(new BorderLayout(0, 0));

        labReceivPort = new JLabel("UDP Receiver Port:");
        labReceivPort.setFont(new Font("Serif", Font.PLAIN,20));
        labReceivPort.setBorder(new EmptyBorder(0, 0, 0, 0));
        pSendPort.add(labReceivPort, BorderLayout.WEST);

        tranReceivP = new JTextField();
        tranReceivP.setText("3321");
        tranReceivP.setColumns(10);
        pSendPort.add(tranReceivP, BorderLayout.SOUTH);

        pReceiverPort = new JPanel();
        pReceiverPort.setBorder(new EmptyBorder(5, 5, 10, 10));
        pMain.add(pReceiverPort);
        pReceiverPort.setLayout(new BorderLayout(0, 0));

        labSendPort = new JLabel("UDP Sender Port:");
        labSendPort.setBorder(new EmptyBorder(0, 0, 0, 0));
        labSendPort.setFont(new Font("Serif", Font.PLAIN,20));
        pReceiverPort.add(labSendPort, BorderLayout.WEST);

        tranSenP = new JTextField();
        tranSenP.setText("4455");
        tranSenP.setColumns(10);
        pReceiverPort.add(tranSenP, BorderLayout.SOUTH);

        pOutFile = new JPanel();
        pOutFile.setBorder(new EmptyBorder(5, 5, 10, 10));
        pMain.add(pOutFile);
        pOutFile.setLayout(new BorderLayout(0, 0));

        lOutFile = new JLabel("Output File To Be Transferred:");
        lOutFile.setBorder(new EmptyBorder(0, 0, 0, 0));
        lOutFile.setFont(new Font("Serif", Font.PLAIN,20));
        pOutFile.add(lOutFile, BorderLayout.WEST);

        tOutFile = new JTextField();
        tOutFile.setText("test.txt");
        tOutFile.setColumns(10);
        pOutFile.add(tOutFile, BorderLayout.SOUTH);

        timeout = new JPanel();
        timeout.setBorder(new EmptyBorder(5, 5, 10, 10));
        pMain.add(timeout);
        timeout.setLayout(new BorderLayout(0, 0));


        itimeOut = new JLabel("Timeout (ms):");
        itimeOut.setBorder(new EmptyBorder(0, 0, 0, 0));
        itimeOut.setFont(new Font("Serif", Font.PLAIN,20));
        timeout.add(itimeOut, BorderLayout.WEST);

        ptimeOut = new JTextField();
        ptimeOut.setText("90000000");
        ptimeOut.setColumns(10);
        timeout.add(ptimeOut, BorderLayout.SOUTH);

        //checkboxReliable = new JCheckBox("Reliable");
       // checkboxReliable.setSelected(true);
        //checkboxReliable.setBackground(Color.gray);
       // checkboxReliable.addActionListener(this::bSendHandler);
        pOtherC = new JPanel();
        pOtherC.setBorder(new EmptyBorder(5, 5, 10, 10));
        pMain.add(pOtherC);
        pOtherC.setLayout(new BorderLayout(0, 0));




        bSend = new JButton("SEND");
        bSend.addActionListener(this::bSendHandler);
        bSend.setBackground(Color.cyan);
        pOtherC.add(bSend, BorderLayout.EAST);
        pOtherC.setBackground(Color.gray);

        bisAlive=new JButton("Is Alive?");
        bisAlive.addActionListener(this::bisAliveHandler);
        bisAlive.setBackground(Color.YELLOW);
        pOtherC.add(bisAlive,BorderLayout.WEST);




        setVisible(true);
    }

    JPanel par;
    JPanel pMain;
    JPanel pSendPort;
    JLabel labReceivPort;
    JTextField tranReceivP;
    JPanel pOutFile;
    JLabel lOutFile;
    JTextField tOutFile;
    JPanel pOtherC;
    JButton bSend;
    JButton bisAlive;
    JPanel pSendAddy;
    JLabel lSendAddy;
    JTextField tRecAddress;
    JPanel pReceiverPort;
    JLabel labSendPort;
    JTextField tranSenP;
    JLabel itimeOut;
    JTextField ptimeOut;
    JPanel timeout;

}