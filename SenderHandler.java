import java.io.IOException;
import java.net.DatagramSocket;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.nio.file.Paths;
//["4455","3321","test.txt","1000000","30000"]
//netstat -aon | find /i "listening"
public class SenderHandler {


    public void startSending(String ip, int senderPort, int receiverPort, String fileName, boolean reliable) throws IOException {
    try{
            int maxDataSize = 64000;
            int timeout = 30000;

            String fileContent = getFileData(fileName);

            // Initialize  buffer and datagram logic
            byte[] buf = new byte[65535];
            byte[] bufferData = new byte[1];
            DatagramPacket dp = new DatagramPacket(buf, buf.length);
            DatagramSocket ds = new DatagramSocket(null);
            ds.bind(new InetSocketAddress(ip, senderPort));
            ds.setSoTimeout(timeout);

            System.out.println("Starting to send datagram from file " + fileName + " on address: " + ip + " at port: " + receiverPort + " while listening for ACKS at port: " + senderPort);

            // Start time and iteration of Datagrams
            long startTime = System.currentTimeMillis();
            // Iterate amount of Datagrams to be sent + 1 extra for the EOT Datagram
            for (int i = 0; i < (fileContent.length() / maxDataSize) + 2; i++) {
                // Verify what type of datagram it is; i.s is it an EOT?
                bufferData = verifyDatagram( fileContent, maxDataSize, i);

                System.out.print("Sending datagram");
                //Send datagram
                ds.send(new DatagramPacket(bufferData, bufferData.length, InetAddress.getByName(ip), receiverPort));
                // Now wait for ACK response. If we time out, we need to resend.
                i = reSendPacket(ds,dp,i);
            }
            //Done, no longer need the socket, close or else file descriptors will be leaked
            System.out.println("Total Transmission Time: " + (System.currentTimeMillis() - startTime) + "ms.");
            ds.close();
        } catch (NumberFormatException exception) {
            //When converting a string with improper format into a numeric.
            System.out.println("Invalid arguments, program shutting down.");
            System.exit(0);
        }

    }


    // Read data from file stored in command line arguments and store it in strinb builder
    public static String getFileData(String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        Scanner scanner;
        try {
            scanner = new Scanner(Paths.get("test.txt"));
            while (scanner.hasNextLine()) {
                System.out.println("Hello World");
                stringBuilder.append(scanner.nextLine()).append("\n");
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            //File not found
            System.out.println("Invalid file name, program shutting down.");
            System.exit(0);
        } catch (IOException e) {
          // When failing to read / write to file
             e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    // COMEBACK Create byte size data depending on index of packet to be sent
    public static byte[] generateDatagramPacketBuffer(String data, int maxSize, int packetIndex) {
        byte[] bufferData = new byte[maxSize + 1];
        int endIndex = packetIndex == data.length() / maxSize ? data.length() : maxSize * (packetIndex + 1);
        for (int index = maxSize * packetIndex; index < endIndex; index++) {
            bufferData[maxSize + index - endIndex] = (byte) data.charAt(index);
        }
        bufferData[maxSize] = (byte) (packetIndex % 2);
        return bufferData;
    }

    // Verify what type of datagram it is; i.s is it an EOT?
    public static byte[] verifyDatagram( String fileContent , Integer maxDataSize, Integer i ) {
        byte[] bufferData;
        if (i < (fileContent.length() / maxDataSize) + 1) {
            //NOT an EOT - create byte array of data to store in Datagram
            bufferData = generateDatagramPacketBuffer(fileContent, maxDataSize, i);
        } else {

            // Generate EOT Datagram with a char of m & a sequence number of 3
            bufferData = new byte[]{(byte) 'm', (byte) 3};
        }
        return bufferData;
    }

     //If we time out, we need to resend datagram
    public static Integer reSendPacket(DatagramSocket ds, DatagramPacket dp, Integer i  ) {
        try {
            System.out.println("i is" + i);
            System.out.print(" and awaiting ACK... ");
            ds.receive(dp); // Start receiving data and wait for ACK.
            int ackReceived = -1;

            // Get the ID of the ACK
            for (byte b : dp.getData()) {
                String letter = String.valueOf((char) b);
                if (letter.equals("0") || letter.equals("1") || letter.equals("3"))
                    ackReceived = Integer.parseInt(letter);
            }

            // If the ACK response is invalid, resend the datagram again
            if (ackReceived != i % 2 && ackReceived != 3) {
                System.out.println("- Invalid ACK, re-sending previous datagram");
                i = i-1;
            } else {
                System.out.println("- Valid ACK received");

            }

        } catch (SocketTimeoutException exception) {
            // When response times out, re-send the datagram
            System.out.println("- ACK timed out, re-sending previous datagram");
            i = i - 1;
        } catch (IOException e) {
            //When failing to read / write to file
            i = i - 1;
            e.printStackTrace();
            System.exit(0);
        }
       return i;
    }

}