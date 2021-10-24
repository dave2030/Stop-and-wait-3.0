import java.io.*;
import java.net.*;
import java.util.Scanner;
//Args: 127.0.0.1 4455 3321 test.txt 64000 30000
public class Sender {
    public static void main(String[] args) throws IOException {
        System.out.println(args.length);
        if (args.length != 6) {
            System.out.println("Incomplete arguments, program shutting down.");
            System.exit(0);
        }
        try {
            // Read arguments and parse them
            String ip = args[0];
            int receiverPort = Integer.parseInt(args[1]);
            int senderPort = Integer.parseInt(args[2]);
            String fileName = args[3];
            int maxDataSize = Integer.parseInt(args[4]);
            int timeout = Integer.parseInt(args[5]);

            // Initialize memory data
            String fileContent = getFileData(fileName);
            byte[] buf = new byte[1024];
            byte[] bufferData;

            // Initialize Datagram data
            DatagramPacket dp = new DatagramPacket(buf, 1024);
            DatagramSocket ds = new DatagramSocket(null);
            ds.bind(new InetSocketAddress(ip, senderPort));
            ds.setSoTimeout(timeout);

            System.out.println("Starting to send datagram from file " + fileName + " on address: " + ip + " at port: " + receiverPort + " while listening for ACKS at port: " + senderPort);

            // Start time and iteration of Datagrams
            long startTime = System.currentTimeMillis();
            // Iterate amount of Datagrams to be sent + 1 extra for the EOT Datagram
            for (int i = 0; i < (fileContent.length() / maxDataSize) + 2; i++) {
                // Check if we are on our last Datagram to send
                if (i < (fileContent.length() / maxDataSize) + 1) {
                    // Create byte array of data to store in Datagram
                    bufferData = generateDatagramPacketBuffer(fileContent, maxDataSize, i);
                } else {
                    // Otherwise create of our EOT Datagram which is a tab character with sequence 4 to signal EOT
                    bufferData = new byte[]{(byte) '\t', (byte) 4};
                }
                // Send data in DatagramPacket
                System.out.print("Sending datagram");
                ds.send(new DatagramPacket(bufferData, bufferData.length, InetAddress.getByName(ip), receiverPort));
                // Wait for ACK response and re-send packet if timed out
                try {
                    System.out.print(" and awaiting ACK... ");
                    ds.receive(dp); // Blocking call until data is received, if longer than timeout the error is thrown and we catch it
                    int ackReceived = -1;

                    // Get data and pull the last value which is the ID of the ACK
                    for (byte b : dp.getData()) {
                        String letter = String.valueOf((char) b);
                        if (letter.equals("0") || letter.equals("1") || letter.equals("4"))
                            ackReceived = Integer.parseInt(letter);
                    }
                    // If the incoming ACK is not the correct sequence response, re-send the datagram
                    if (ackReceived != i % 2 && ackReceived != 4) {
                        System.out.println("- Invalid ACK, re-sending previous datagram");
                        i--;
                    } else {
                        System.out.println("- Valid ACK received");

                    }
                } catch (SocketTimeoutException exception) { // If response times out, re-send the datagram
                    System.out.println("- ACK timed out, re-sending previous datagram");
                    i--;
                }
            }
            System.out.println("Total Transmission Time: " + (System.currentTimeMillis() - startTime) + "ms.");
            ds.close();
        } catch (NumberFormatException exception) {
            System.out.println("Invalid arguments, program shutting down.");
            System.exit(0);
        }
    }

    // Read data from file and store it in a string as is
    public static String getFileData(String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        Scanner scanner;
        try {
            scanner = new Scanner(new File(fileName));
            while (scanner.hasNextLine()) {
                stringBuilder.append(scanner.nextLine()).append("\n");
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Invalid file name, program shutting down.");
            System.exit(0);
        }
        return stringBuilder.toString();
    }

    // Create appropriate sized byte array of our String data based on index of packet to be sent
    public static byte[] generateDatagramPacketBuffer(String data, int maxSize, int packetIndex) {
        byte[] bufferData = new byte[maxSize + 1];
        int endIndex = packetIndex == data.length() / maxSize ? data.length() : maxSize * (packetIndex + 1);
        for (int index = maxSize * packetIndex; index < endIndex; index++) {
            bufferData[maxSize + index - endIndex] = (byte) data.charAt(index);
        }
        bufferData[maxSize] = (byte) (packetIndex % 2);
        return bufferData;
    }

}
