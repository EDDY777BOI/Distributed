import java.io.*;
import java.net.*;

public class UDPServer {
    public static void main(String[] args) {
        int port = 5000;

        try (DatagramSocket serverSocket = new DatagramSocket(port)) {
            System.out.println("[*] UDP Server is listening on port " + port);

            byte[] receiveBuffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            serverSocket.receive(receivePacket);

            String fileName = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("[*] Client requested file: " + fileName);

            InetAddress clientAddress = receivePacket.getAddress();
            int clientPort = receivePacket.getPort();

            File file = new File(fileName);
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                byte[] fileData = new byte[(int) file.length()];
                fis.read(fileData);
                fis.close();

                DatagramPacket sendPacket = new DatagramPacket(fileData, fileData.length, clientAddress, clientPort);
                serverSocket.send(sendPacket);
                System.out.println("[*] File sent successfully.");
            } else {
                byte[] errorMessage = "File not found".getBytes();
                DatagramPacket errorPacket = new DatagramPacket(errorMessage, errorMessage.length, clientAddress, clientPort);
                serverSocket.send(errorPacket);
                System.out.println("[!] File not found.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
