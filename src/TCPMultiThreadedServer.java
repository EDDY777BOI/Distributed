import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;


public class TCPMultiThreadedServer {
    private static final AtomicInteger clientCount = new AtomicInteger(0);

    public static void main(String[] args) {
        int port = 5000;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[*] Multi-threaded server running on port " + port);
            while (true)
                new Thread(new ClientHandler(serverSocket.accept(), clientCount.incrementAndGet())).start();
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
        System.out.println("[*] Client #" + clientNumber + " connected.");
        try (DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
             DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream())) {

            String fileName;
            while (!(fileName = dis.readUTF()).equalsIgnoreCase("exit")) {
                File file = new File(fileName);
                if (file.exists()) {
                    byte[] buffer = Files.readAllBytes(file.toPath());
                    dos.writeInt(buffer.length);
                    dos.write(buffer);
                    System.out.println("[*] Sent file: " + fileName + " to Client #" + clientNumber);
                } else {
                    dos.writeInt(0);
                    System.out.println("[!] Client #" + clientNumber + " requested missing file: " + fileName);
                }
                dos.writeInt(clientNumber);
                dos.flush();
            }
            System.out.println("[*] Client #" + clientNumber + " disconnected.");
        } catch (IOException e) {
            System.out.println("[!] Client #" + clientNumber + " connection lost.");
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ignored) {}
        }
    }
}
