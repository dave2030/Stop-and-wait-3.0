import java.io.IOException;
import java.net.DatagramSocket;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.nio.file.Paths;
//["4455","3321","test.txt","1000000","30000"]
//netstat -aon | find /i "listening"
public class SenderHandler {


    public void startSending(String ip, int receiverPort,  int senderPort, String fileName,int timeout) throws IOException {
    try{
            int maxBytes = 64000;

            String contentsFromFile = getFileData(fileName);

            // Initialize  buffer and datagram logic
        byte[] buf = new byte[65535];
        byte[] bufData = new byte[1];
            DatagramSocket datagramSocket = new DatagramSocket(null);
            DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
            datagramSocket.bind(new InetSocketAddress(ip, senderPort));
            datagramSocket.setSoTimeout(timeout);
            System.out.println("Filename:" + fileName + " is being sent on address of " + ip + " with receiver port: " + receiverPort + " and sender port of " + senderPort);
            System.out.println("--------------------------------------------");
            // Start time and iteration of Datagrams
            long startingTime = System.currentTimeMillis();
            // Iterate amount of Datagrams to be sent + 1 extra for the EOT Datagram
            for (int i = 0; i < (contentsFromFile.length() / maxBytes) + 2; i++) {
                // Verify what type of datagram it is; i.s is it an EOT?
                bufData = verifyDatagram( contentsFromFile, maxBytes, i);
                //Send datagram
                datagramSocket.send(new DatagramPacket(bufData, bufData.length, InetAddress.getByName(ip), receiverPort));
                System.out.print("Verified datagram and sending datagram");
                // Now wait for ACK response. If we time out, we need to resend.
                i = resendDatagramPacket(datagramSocket,datagramPacket,i);
            }
            //Done
            System.out.println("Finished. Time: " + (System.currentTimeMillis() - startingTime) + "ms.");
            datagramSocket.close();
        } catch (NumberFormatException exception) {
            //When converting a string with improper format into a numeric.
            System.out.println("Insert the right amount of arguments");
            System.exit(0);
        }

    }


    // Read data from file stored in command line arguments and store it in strinb builder
    public static String getFileData(String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        Scanner scanner;
        try {
            scanner = new Scanner(Paths.get(fileName));
            while (scanner.hasNextLine()) {

                stringBuilder.append(scanner.nextLine()).append("\n");
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            //File not found
            System.out.println("File is not found.");
            System.exit(0);
        } catch (IOException e) {
          // When failing to read / write to file
            System.out.println("Failed to read / write file");
             e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    // COMEBACK Create byte size data depending on index of packet to be sent
    public static byte[] generateBuffer(String data, int maxSize, int indexOfPacket) {
        int endI = indexOfPacket == data.length() / maxSize ? data.length() : maxSize * (indexOfPacket + 1);
        byte[] bufData = new byte[maxSize + 1];

        for (int i = maxSize * indexOfPacket; i < endI; i++) {
            bufData[maxSize + i - endI] = (byte) data.charAt(i);
        }

        bufData[maxSize] = (byte) (indexOfPacket % 2);
        return bufData;
    }

    // Verify what type of datagram it is; i.s is it an EOT?
    public static byte[] verifyDatagram( String contentsFromFile , Integer maxBytes, Integer i ) {
        byte[] bufData;
        if (i < (contentsFromFile.length() / maxBytes) + 1) {
            //NOT an EOT - create byte array of data to store in Datagram
            bufData = generateBuffer(contentsFromFile, maxBytes, i);
        } else {

            // Generate EOT Datagram with a char of m & a sequence number of 3
            bufData = new byte[]{(byte) 'm', (byte) 3};
        }
        return bufData;
    }

     //If we time out, we need to resend datagram
    public static Integer resendDatagramPacket(DatagramSocket datagramSocket, DatagramPacket datagramPacket, Integer i  ) {
        try {

            System.out.println(" i = " + i);

            System.out.print(" -----> waiting for ACK ");

            datagramSocket.receive(datagramPacket); // Start receiving data and wait for ACK.
            int valueOfAcks = -1;

            // Get the ID of the ACK
            for (byte b : datagramPacket.getData()) {
                String letter = String.valueOf((char) b);
                if (letter.equals("0") || letter.equals("1") || letter.equals("3"))
                    valueOfAcks = Integer.parseInt(letter);
            }

            // If the ACK response is invalid, resend the datagram again
            if (valueOfAcks != i % 2 && valueOfAcks != 3) {
                System.out.println("-----> ACK is invalid, re-sending prev datagram again");
                i = i-1;
            } else {
                System.out.println("-----> ACK received");

            }

        } catch (SocketTimeoutException exception) {
            // When response times out, re-send the datagram
            System.out.println("-----> ACK timed out, re-sending prev datagram again");
            i = i - 1;
        } catch (IOException e) {
            //When failing to read / write to file
            System.out.println("-----> ACK timed out, failed to read / write to file");
            i = i - 1;
            e.printStackTrace();
            System.exit(0);
        }
       return i;
    }

}
