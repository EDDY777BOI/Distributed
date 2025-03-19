import java.io.*;
import java.net.*;

public class UDPClient {
    public static void main(String[] args) {
        String serverAddress = "127.0.0.1";
        int port = 5000;
        String fileName = "test.txt";

        try (DatagramSocket clientSocket = new DatagramSocket()) {
            byte[] sendBuffer = fileName.getBytes();
            InetAddress serverIP = InetAddress.getByName(serverAddress);
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverIP, port);
            clientSocket.send(sendPacket);

            byte[] receiveBuffer = new byte[4096];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            clientSocket.receive(receivePacket);

            String receivedData = new String(receivePacket.getData(), 0, receivePacket.getLength());

            if (receivedData.equals("File not found")) {
                System.out.println("[!] File not found on server.");
            } else {
                FileOutputStream fos = new FileOutputStream("received_" + fileName);
                fos.write(receivePacket.getData(), 0, receivePacket.getLength());
                fos.close();
                System.out.println("[*] File received successfully.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
