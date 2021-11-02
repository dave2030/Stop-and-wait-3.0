import java.awt.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionEvent;
import java.io.IOException;


public class SenderGUI extends JFrame {
    private final SenderHandler sendHand;

    public SenderGUI(String[] args) {
        sendHand = new SenderHandler();
        setTitle("Stop and Wait 3.0");
        setBounds(200, 200, 400, 400);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        components();
    }


    private void bReceiveHandler(ActionEvent e) {
        if (bSend.getText().equals("SEND")) {
            try {
                int sendinP = Integer.parseInt(tranSenP.getText());
                int receivinP = Integer.parseInt(tranReceivP.getText());
                String addy = tRecAddress.getText();
                String out = tOutFile.getText();
                System.out.println("yo matt " + addy + " " +  sendinP +" " + receivinP + " " + out);

                new SwingWorker<Void, Void>() {
                    @Override
                    public Void doInBackground() {
                        try {
                            sendHand.startSending(addy, receivinP, sendinP, out, true);
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
        pSendAddy.setBorder(new EmptyBorder(11, 14, 9, 16));
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
        pSendPort.setBorder(new EmptyBorder(5, 14, 4, 16));
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
        pReceiverPort.setBorder(new EmptyBorder(5, 15, 5, 15));
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
        pOutFile.setBorder(new EmptyBorder(5, 15, 20, 15));
        pMain.add(pOutFile);
        pOutFile.setLayout(new BorderLayout(0, 0));

        lOutFile = new JLabel("Output File To Be Transferred:");
        lOutFile.setBorder(new EmptyBorder(0, 0, 0, 0));
        lOutFile.setFont(new Font("Serif", Font.PLAIN,20));
        pOutFile.add(lOutFile, BorderLayout.WEST);

        tOutFile = new JTextField();
        tOutFile.setText("received.txt");
        tOutFile.setColumns(10);
        pOutFile.add(tOutFile, BorderLayout.SOUTH);

        pOtherC = new JPanel();
        pOtherC.setBorder(new EmptyBorder(10, 20, 10, 20));
        pMain.add(pOtherC);
        pOtherC.setLayout(new BorderLayout(0, 0));



        bSend = new JButton("SEND");
        bSend.addActionListener(this::bReceiveHandler);
        pOtherC.add(bSend, BorderLayout.EAST);

        setVisible(true);
    }

    JPanel par;
    JPanel head;
    JPanel pMain;
    JLabel lPacketReceive;
    JPanel pSendPort;
    JLabel labReceivPort;
    JTextField tranReceivP;
    JPanel pOutFile;
    JLabel lOutFile;
    JTextField tOutFile;
    JPanel pInfo;
    JLabel lInfo;
    JPanel pOtherC;
    JButton bSend;
    JPanel pSendAddy;
    JLabel lSendAddy;
    JTextField tRecAddress;
    JPanel pReceiverPort;
    JLabel labSendPort;
    JTextField tranSenP;


}