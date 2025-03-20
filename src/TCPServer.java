import java.io.*;
import java.net.*;
import java.nio.file.Files;

// Project lab1
public class TCPServer {
    public static void main(String[] args) {
        int port = 5000;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[*] Single-threaded server running on port " + port);
            System.out.println("[*] Waiting for a client...");

            Socket clientSocket = serverSocket.accept();    // Accept one client
            System.out.println("[*] Client connected.");

            handleClient(clientSocket);                     // Process client requests
            System.out.println("[*] Client disconnected. Server shutting down.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) throws IOException {
        try (
                DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream())
        ) {
            String fileName;
            while (!(fileName = dis.readUTF()).equalsIgnoreCase("exit")) {
                File file = new File(fileName);
                if (file.exists()) {
                    byte[] buffer = Files.readAllBytes(file.toPath()); // Read file directly
                    dos.writeInt(buffer.length);
                    dos.write(buffer);
                    System.out.println("[*] Sent file: " + fileName);
                } else {
                    dos.writeInt(0);
                    System.out.println("[!] File not found: " + fileName);
                }
                dos.flush();
            }
            System.out.println("[*] Client requested to exit.");
        } catch (EOFException e) {
            System.out.println("[!] Client disconnected unexpectedly.");
        } finally {
            clientSocket.close();
        }
    }

}
