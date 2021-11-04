import javax.swing.*;
import java.io.*;
import java.net.*;

public class Handler {
    private JLabel inOrderPacketLabel;
    DatagramSocket datagramSocket;
    
    public  void startReceiving(String address, int senderPort, int receiverPort, String outputFileName, boolean reliable) throws IOException {
        System.out.println("Starting to receive on address: " + address + " at port: " + receiverPort + " with output going to: " + outputFileName + " and ACKS to port: " + senderPort);
        StringBuilder data;
        // If it does not exist, we need to create a new file
        if (new File(outputFileName).createNewFile()) {
            System.out.println("File not found, created.");
        }
        // Initialize Datagram logic
        datagramSocket = new DatagramSocket(null);
        datagramSocket.bind(new InetSocketAddress(address, receiverPort));
        byte[] buf = new byte[65535];
        DatagramPacket datagramPocket = new DatagramPacket(buf, buf.length);

        //Final string to write to received.txt file after reading all datagrams from Sender
        StringBuilder builtStringFinal = new StringBuilder();

        // COMEBACK Used for dropping each 10th packet in unreliable mode and counting packets in-order and received
        int packetCount = 0;
        //Count in order packets
        int inOrderPacketAmount = 0;

        while (true) {
            try {
                System.out.println("Awaiting data...");

                //Start receiving data
                datagramSocket.receive(datagramPocket);
                packetCount++;

                if (reliable || packetCount % 10 != 0) {
                   //Build string based on  received datagram
                    data = buildDatagramString(datagramPocket);

                    // String that will be used for writing to received.txt
                    builtStringFinal.append(data);

                    //Sequence number will be 0 or 1 based on the assignment requirements. 1 will be E0T.
                    int sequenceNumber = datagramPocket.getData()[datagramPocket.getLength() - 1];

                    // Verify if packet is an EOT datagram or not
                    inOrderPacketAmount =  verifyDatagram(data, sequenceNumber, builtStringFinal,  outputFileName, inOrderPacketAmount, inOrderPacketLabel);

                    // Send ACK to Sender
                    String ackVal = "ACK " + sequenceNumber;
                    System.out.println("Data received and sending ACK " + sequenceNumber);
                    datagramSocket.send(new DatagramPacket(ackVal.getBytes(), ackVal.getBytes().length, InetAddress.getByName(address), senderPort));
                } else {
                    System.out.println("Not reliable - hanging until time out");
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
        datagramSocket.close();
    }

    //Build string based on packets
    public static StringBuilder buildDatagramString (DatagramPacket datagramPocket){
        StringBuilder finalBuiltString = new StringBuilder();
        System.out.println("datagramPocket.getLength()" +   datagramPocket.getLength());
        for (int i = 0; i < datagramPocket.getLength(); i++) {
//        System.out.println("datagramPocket.getData()[i]" +   datagramPocket.getData()[i]);
            if (datagramPocket.getData()[i] >= 9) {
                finalBuiltString.append((char) datagramPocket.getData()[i]);
                System.out.println("String builder data" +   finalBuiltString);
            }
        }
        return finalBuiltString;
    }

    //Verify if EOT datagram or not / Write to received.txt
    public static Integer verifyDatagram(StringBuilder data, Integer sequenceNumber,StringBuilder builtStringFinal,
                                         String outputFileName, int inOrderPacketAmount, JLabel inOrderPacketLabel  ){
        //if EOT Datagram
        //Check for EOT char (m) with a sequence number of 3
        if (data.toString().contains("m") && sequenceNumber == 3) {
            PrintWriter printWriter = null;
            try {
                //Write to output file
                printWriter = new PrintWriter(new FileWriter(outputFileName));
                printWriter.print(builtStringFinal);
                //Closed print writer to avoid resource leaks
                printWriter.close();
                inOrderPacketLabel.setText(inOrderPacketAmount + "");
                //Reset packet count
                inOrderPacketAmount = 0;
            } catch (IOException e) {
                //If there is a failure in writing
                e.printStackTrace();
            }

        } else {
            // If it is NOT an EOT Datagram
            inOrderPacketAmount = inOrderPacketAmount + 1;
            inOrderPacketLabel.setText(inOrderPacketAmount + "");
        }
        return inOrderPacketAmount;
    }

    // Assign JLabel after GUI
    public void setInOrderPacketLabel(JLabel label) {
        this.inOrderPacketLabel = label;
    }


}

