import javax.swing.*;
import java.io.*;
import java.net.*;

public class Handler {
    private JLabel inOrderPacketLabel;
    DatagramSocket ds;
    
    public void startReceiving(String address, int senderPort, int receiverPort, String outputFileName, boolean reliable) throws IOException {
        System.out.println("Starting to receive on address: " + address + " at port: " + receiverPort + " with output going to: " + outputFileName + " and ACKS to port: " + senderPort);
        StringBuilder data;
        // If it does not exist, we need to create a new file
        if (new File(outputFileName).createNewFile()) {
            System.out.println("File not found, created.");
        }
        // Initialize Datagram logic
        ds = new DatagramSocket(null);
        ds.bind(new InetSocketAddress(address, receiverPort));
        byte[] buf = new byte[65535];
        DatagramPacket dp = new DatagramPacket(buf, buf.length);

        //Final string to write to received.txt file after reading all datagrams from Sender
        StringBuilder finalData = new StringBuilder();

        // COMEBACK Used for dropping each 10th packet in unreliable mode and counting packets in-order and received
        int packetCount = 0;
        //Count in order packets
        int inOrderPacketCount = 0;

        while (true) {
            try {
                System.out.println("Awaiting data...");

                //Start receiving data
                ds.receive(dp);
                packetCount++;

                if (reliable || packetCount % 10 != 0) {
                   //Build string based on  received datagram
                    data = buildString(dp);

                    // String that will be used for writing to received.txt
                    finalData.append(data);

                    //Sequence number will be 0 or 1 based on the assignment requirements. 1 will be E0T.
                    int sequenceNumber = dp.getData()[dp.getLength() - 1];

                    // Verify if packet is an EOT datagram or not
                    inOrderPacketCount =  verifyDatagram(data, sequenceNumber, finalData,  outputFileName, inOrderPacketCount, inOrderPacketLabel);

                    // Send ACK to Sender
                    String ack = "ACK " + sequenceNumber;
                    System.out.println("Data received and sending ACK " + sequenceNumber);
                    ds.send(new DatagramPacket(ack.getBytes(), ack.getBytes().length, InetAddress.getByName(address), senderPort));
                }
                //Reminder: Based on the assignment's pdf, Sender will drop every 10th packet without setting it if it is unreliable
            } catch (IOException exception) {
                //If there is a failure in reading/writing
                break;
            }
        }
    }

    // Disconnect
    public void stopReceiving() {
        ds.close();
    }

    //Build string based on packets
    public static StringBuilder buildString (DatagramPacket dp){
        StringBuilder data = new StringBuilder();
        System.out.println("dp.getLength()" +   dp.getLength());
        for (int i = 0; i < dp.getLength(); i++) {
//        System.out.println("dp.getData()[i]" +   dp.getData()[i]);
            if (dp.getData()[i] >= 9) {
                data.append((char) dp.getData()[i]);
                System.out.println("String builder data" +   data);
            }
        }
        return data;
    }

    //Verify if EOT datagram or not / Write to received.txt
    public static Integer verifyDatagram(StringBuilder data, Integer sequenceNumber,StringBuilder finalData,
                                         String outputFileName, int inOrderPacketCount, JLabel inOrderPacketLabel  ){
        //if EOT Datagram
        //Check for EOT char (m) with a sequence number of 3
        if (data.toString().contains("m") && sequenceNumber == 3) {
            PrintWriter writer = null;
            try {
                //Write to output file
                writer = new PrintWriter(new FileWriter(outputFileName));
                writer.print(finalData);
                //Closed print writer to avoid resource leaks
                writer.close();
                inOrderPacketLabel.setText(inOrderPacketCount + "");
                //Reset packet count
                inOrderPacketCount = 0;
            } catch (IOException e) {
                //If there is a failure in writing
                e.printStackTrace();
            }

        } else {
            // If it is NOT an EOT Datagram
            inOrderPacketCount = inOrderPacketCount + 1;
            inOrderPacketLabel.setText(inOrderPacketCount + "");
        }
        return inOrderPacketCount;
    }

    // Assign JLabel after GUI
    public void setInOrderPacketLabel(JLabel label) {
        this.inOrderPacketLabel = label;
    }


}

