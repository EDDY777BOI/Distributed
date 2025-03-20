import java.io.*;
import java.net.*;

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
            while (true) { // Keep listening for multiple requests from the same client
                String fileName = dis.readUTF();

                if (fileName.equalsIgnoreCase("exit")) {
                    System.out.println("[*] Client requested to exit.");
                    break; // Stop loop when client wants to exit
                }

                File file = new File(fileName);
                if (file.exists()) {
                    dos.writeInt((int) file.length()); // Send file size
                    byte[] buffer = new byte[(int) file.length()];

                    try (FileInputStream fis = new FileInputStream(file)) {
                        fis.read(buffer);
                    }
                    dos.write(buffer); // Send file
                    dos.flush();
                    System.out.println("[*] Sent file: " + fileName);
                } else {
                    dos.writeInt(0); // File not found
                    dos.flush();
                    System.out.println("[!] File not found: " + fileName);
                }
            }
        } catch (EOFException e) {
            System.out.println("[!] Client disconnected unexpectedly.");
        } finally {
            clientSocket.close(); // Close the connection properly
        }
    }
}
