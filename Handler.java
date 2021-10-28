import javax.swing.*;
import java.io.*;
import java.net.*;

public class Handler {
    DatagramSocket ds;

    // JLabel to update on GUI based on in-order packets received
    private JLabel inOrderPacketLabel;
    
    //Build string based on packets
    public static StringBuilder buildString (DatagramPacket dp){
        StringBuilder data = new StringBuilder();
        System.out.println("dp.getLength()" +   dp.getLength());
        for (int i = 0; i < dp.getLength(); i++) {
//                        System.out.println("dp.getData()[i]" +   dp.getData()[i]);
            if (dp.getData()[i] >= 9) {
                data.append((char) dp.getData()[i]);
                System.out.println("String builder data" +   data);
            }
        }
        return data;
    }
    //Verify if EOT datagram or not
    public static Integer verifyDatagram(StringBuilder data, Integer sequenceNumber,StringBuilder finalData,
                                      String outputFileName, int inOrderPacketCount, JLabel inOrderPacketLabel  ){
        //if EOT Datagram
        //Check for tab char & sequence size of 4
        if (data.toString().contains("\t") && sequenceNumber == 4) {  
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
                e.printStackTrace();
            }

        } else {
            // If it is NOT an EOT Datagram
            inOrderPacketCount = inOrderPacketCount + 1;
            inOrderPacketLabel.setText(inOrderPacketCount + "");
        }
        return inOrderPacketCount;
    }
    
    public void startReceiving(String address, int senderPort, int receiverPort, String outputFileName, boolean reliable) throws IOException {
        System.out.println("Starting to receive on address: " + address + " at port: " + receiverPort + " with output going to: " + outputFileName + " and ACKS to port: " + senderPort);
        StringBuilder data;
        // If it does not exist, we need to create a new file
        if (new File(outputFileName).createNewFile()) {
            System.out.println("File not found, created.");
        }
        // Initialize Datagram
        ds = new DatagramSocket(null);
        ds.bind(new InetSocketAddress(address, receiverPort));
        byte[] buf = new byte[65535];
        DatagramPacket dp = new DatagramPacket(buf, buf.length);

        // COMEBACK Final data to write to file after reading all Datagrams
        StringBuilder finalData = new StringBuilder();

        // COMEBACK Used for dropping each 10th packet in unreliable mode and counting packets in-order and received
        int packetCount = 0;
        int inOrderPacketCount = 0;

        while (true) {
            try {
                System.out.println("Awaiting data...");

                //Start receiving dat
                ds.receive(dp);
                packetCount++;

                // COMEBACK If reliable is selected or if it is not a multiple of 10th datagram sent, handle it
                String received = new String(
                        dp.getData(), 0, dp.getLength());

                System.out.println("Reliable" +  reliable + received);
                if (reliable || packetCount % 10 != 0) {
                   //Build string based on datagram
                    data = buildString(dp);

                    // COMEBACK Add to our final data we will be writing
                    finalData.append(data.toString());

                    // COMEBACK Our sequence number to send back as an acknowledgement or read EOT
                    int sequenceNumber = dp.getData()[dp.getLength() - 1];

                    // Verify if packet is an EOT datagram or not
                    inOrderPacketCount =  verifyDatagram(data, sequenceNumber, finalData,  outputFileName, inOrderPacketCount, inOrderPacketLabel);

                    // Send ACK to Sender
                    String ack = "ACK " + sequenceNumber;
                    System.out.println("Data received and sending ACK " + sequenceNumber);
                    ds.send(new DatagramPacket(ack.getBytes(), ack.getBytes().length, InetAddress.getByName(address), senderPort));
                }
            } catch (IOException exception) {
                break;
            }
        }
    }

    // Assign JLabel after GUI i
    public void setInOrderPacketLabel(JLabel label) {
        this.inOrderPacketLabel = label;
    }

    // Disconnect
    public void stopReceiving() {
        ds.close();
    }

}

