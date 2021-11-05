import java.awt.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionEvent;
import java.io.IOException;


public class ReceiverGUI extends JFrame {
    private final Handler hand;
    String ip;
    int receiverPort;
    int senderPort;
    String fileName;
    int maxDataSize;
    int timeout;


    public ReceiverGUI(String[] args) {
        hand = new Handler();
        setTitle("Stop and Wait 3.0");
        setBounds(200, 200, 400, 400);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        components();
        if (args.length < 4) {
            System.out.println("Incomplete arguments, program shutting down.");
            System.exit(0);
        }
        ip = args[0];
        receiverPort = Integer.parseInt(args[2]);
        senderPort = Integer.parseInt(args[1]);
        fileName = args[3];
        maxDataSize = 64000;
        timeout = Integer.parseInt(args[5]);
    }

    private void bReceiveHandler(ActionEvent e) {
        boolean reliable=checkboxReliable.isSelected();
        if (bReceive.getText().equals("RECEIVE")) {
            try {
                new SwingWorker<Void, Void>() {
                    @Override
                    public Void doInBackground() {
                        try {

                            hand.setInOrderPacketLabel(lPacketReceive);
                            hand.startReceiving(ip,senderPort, receiverPort, fileName,reliable);
                        } catch (IOException exception) {
                            exception.printStackTrace();
                        }
                        return null;
                    }
                }.execute();
                bReceive.setText("TERMINATE");
            } catch (NumberFormatException exception) {
                JOptionPane.showMessageDialog(this, "Invalid port #", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            hand.stopReceiving();
            bReceive.setText("RECEIVE");
        }
    }

    private void components() {
        par = new JPanel();
        par.setBorder(new EmptyBorder(5, 5, 10, 10));

        setContentPane(par);
        par.setLayout(new BorderLayout(0,0));
        par.setBackground(Color.BLACK);


        pMain = new JPanel();
        pMain.setBorder(new EmptyBorder(5, 5, 10, 10));

        par.add(pMain, BorderLayout.CENTER);
        pMain.setLayout(new BoxLayout(pMain, BoxLayout.Y_AXIS));
        pMain.setBackground(Color.ORANGE);

        pSendAddy = new JPanel();
        pSendAddy.setBorder(new EmptyBorder(11, 14, 9, 16));
        pMain.add(pSendAddy);
        pSendAddy.setLayout(new BorderLayout(0, 0));


        pSendPort = new JPanel();
        pSendPort.setBorder(new EmptyBorder(5, 14, 4, 16));
        pMain.add(pSendPort);
        pSendPort.setLayout(new BorderLayout(0, 0));



        pReceiverPort = new JPanel();
        pReceiverPort.setBorder(new EmptyBorder(5, 15, 5, 15));
        pMain.add(pReceiverPort);
        pReceiverPort.setLayout(new BorderLayout(0, 0));



        pOtherC = new JPanel();
        pOtherC.setBorder(new EmptyBorder(10, 20, 10, 20));
        pMain.add(pOtherC);
        pOtherC.setLayout(new BorderLayout(0, 0));



        bReceive = new JButton("RECEIVE");
        bReceive.addActionListener(this::bReceiveHandler);
        pOtherC.add(bReceive, BorderLayout.EAST);
        bReceive.setBackground(Color.CYAN);

        pInfo = new JPanel();
        pMain.add(pInfo);
        pInfo.setBackground(Color.gray);
        lInfo = new JLabel("Sent in-order packets:");
        lInfo.setFont(new Font("Serif", Font.PLAIN,20));
        pInfo.add(lInfo);

        lPacketReceive = new JLabel("0");
        pInfo.add(lPacketReceive);
        checkboxReliable = new JCheckBox("Reliable");
        checkboxReliable.setSelected(true);
        pOtherC.add(checkboxReliable,BorderLayout.SOUTH);
        setVisible(true);
    }

    JPanel par;
    JPanel pMain;
    JLabel lPacketReceive;
    JPanel pSendPort;
    JPanel pInfo;
    JLabel lInfo;
    JPanel pOtherC;
    JButton bReceive;
    JPanel pSendAddy;
    JPanel pReceiverPort;
    JCheckBox checkboxReliable;


}