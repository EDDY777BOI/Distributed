import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Multithreaded UDP Server
public class UDPServer {
    private static final int PORT = 5001;
    private static final String EOF_MARKER = "EOF_SIGNAL";
    private static int clientCount = 0;

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(5);

        try (DatagramSocket serverSocket = new DatagramSocket(PORT)) {
            System.out.println("[*] Multi-threaded server running on port " + PORT);

            while (true) {
                byte[] receiveBuffer = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                serverSocket.receive(receivePacket);

                clientCount++;
                int assignedClientId = clientCount;
                System.out.println("[*] Client #" + assignedClientId + " connected.");

                executor.execute(new ClientHandler(serverSocket, receivePacket, assignedClientId));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        private DatagramSocket serverSocket;
        private DatagramPacket receivePacket;
        private int clientId;

        public ClientHandler(DatagramSocket socket, DatagramPacket packet, int clientId) {
            this.serverSocket = socket;
            this.receivePacket = packet;
            this.clientId = clientId;
        }

        @Override
        public void run() {
            try {
                String fileName = new String(receivePacket.getData(), 0, receivePacket.getLength());
                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();

                File file = new File(fileName);
                System.out.println("[*] Client #" + clientId + " requested file: " + fileName);

                if (file.exists()) {
                    FileInputStream fis = new FileInputStream(file);
                    byte[] fileData = new byte[4096];
                    int bytesRead;

                    while ((bytesRead = fis.read(fileData)) != -1) {
                        DatagramPacket sendPacket = new DatagramPacket(fileData, bytesRead, clientAddress, clientPort);
                        serverSocket.send(sendPacket);
                    }
                    fis.close();

                    // Send EOF marker to indicate file transfer completion
                    byte[] eofBytes = EOF_MARKER.getBytes();
                    DatagramPacket eofPacket = new DatagramPacket(eofBytes, eofBytes.length, clientAddress, clientPort);
                    serverSocket.send(eofPacket);
                    System.out.println("[*] Sent file: " + fileName + " to Client #" + clientId);
                } else {
                    String errorMessage = "FILE_NOT_FOUND";
                    DatagramPacket errorPacket = new DatagramPacket(errorMessage.getBytes(), errorMessage.length(), clientAddress, clientPort);
                    serverSocket.send(errorPacket);
                    System.out.println("[!] File not found: " + fileName);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
