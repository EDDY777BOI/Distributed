import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TCPMultiThreadedServer {
    private static final AtomicInteger clientCount = new AtomicInteger(0); // Thread-safe client counter

    public static void main(String[] args) {
        int port = 5000;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[*] Multi-threaded server running on port " + port);

            while (true) { // Keeps accepting multiple clients
                Socket clientSocket = serverSocket.accept();
                int clientNumber = clientCount.incrementAndGet(); // Assign a unique client number
                System.out.println("[*] Client #" + clientNumber + " connected.");
                new Thread(new ClientHandler(clientSocket, clientNumber)).start(); // Handle each client in a new thread
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final int clientNumber;

    public ClientHandler(Socket socket, int clientNumber) {
        this.clientSocket = socket;
        this.clientNumber = clientNumber;
    }

    @Override
    public void run() {
        try (
                DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream())
        ) {
            while (true) { // Keep listening for multiple requests
                String fileName = dis.readUTF(); // Read file request

                if (fileName.equalsIgnoreCase("exit")) {
                    System.out.println("[*] Client #" + clientNumber + " disconnected.");
                    break; // Exit loop when client sends "exit"
                }

                File file = new File(fileName);
                if (file.exists()) {
                    dos.writeInt((int) file.length()); // Send file size
                    byte[] buffer = new byte[(int) file.length()];

                    try (FileInputStream fis = new FileInputStream(file)) {
                        fis.read(buffer);
                    }
                    dos.write(buffer); // Send file
                    dos.writeInt(clientNumber); // Send client number
                    dos.flush();

                    System.out.println("[*] Sent file: " + fileName + " to Client #" + clientNumber);

                } else {
                    dos.writeInt(0); // File not found
                    dos.writeInt(clientNumber); // Still send client number
                    dos.flush();
                    System.out.println("[!] Client #" + clientNumber + " requested missing file: " + fileName);
                }
            }
        } catch (IOException e) {
            System.out.println("[!] Client #" + clientNumber + " connection lost.");
        } finally {
            try {
                clientSocket.close(); // Close socket after client exits
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
