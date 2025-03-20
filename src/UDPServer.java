import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UDPServer {
    private static final int PORT = 5001;
    private static final String EOF_MARKER = "EOF_SIGNAL";
    private static int clientCount = 0;

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(5);

        try (DatagramSocket serverSocket = new DatagramSocket(PORT)) {
            System.out.println("[*] UDP Server running on port " + PORT);
            byte[] buffer = new byte[1024];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                serverSocket.receive(packet);
                executor.execute(new ClientHandler(serverSocket, packet, ++clientCount));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        private final DatagramSocket serverSocket;
        private final DatagramPacket packet;
        private final int clientId;

        public ClientHandler(DatagramSocket socket, DatagramPacket packet, int clientId) {
            this.serverSocket = socket;
            this.packet = packet;
            this.clientId = clientId;
        }

        @Override
        public void run() {
            try {
                String fileName = new String(packet.getData(), 0, packet.getLength());
                InetAddress clientAddress = packet.getAddress();
                int clientPort = packet.getPort();
                File file = new File(fileName);

                System.out.println("[*] Client #" + clientId + " requested: " + fileName);
                if (file.exists()) sendFile(file, clientAddress, clientPort);
                else sendResponse("FILE_NOT_FOUND", clientAddress, clientPort);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void sendFile(File file, InetAddress address, int port) throws IOException {
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1)
                    serverSocket.send(new DatagramPacket(buffer, bytesRead, address, port));

                sendResponse(EOF_MARKER, address, port);
                System.out.println("[*] Sent file: " + file.getName() + " to Client #" + clientId);
            }
        }

        private void sendResponse(String message, InetAddress address, int port) throws IOException {
            byte[] data = message.getBytes();
            serverSocket.send(new DatagramPacket(data, data.length, address, port));
        }
    }
}
