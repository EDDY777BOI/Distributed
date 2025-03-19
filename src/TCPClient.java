import java.io.*;
import java.net.*;

public class TCPClient {
    public static void main(String[] args) {
        String serverAddress = "127.0.0.1";
        int port = 5000;

        try (Socket socket = new Socket(serverAddress, port);
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
             DataInputStream dis = new DataInputStream(socket.getInputStream());
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("[*] Connected to server. Type 'exit' to quit.");

            while (true) {
                System.out.print("[*] Enter file name to request (or 'exit' to quit): ");
                String fileName = userInput.readLine();

                if (fileName.equalsIgnoreCase("exit")) {
                    System.out.println("[*] Closing connection...");
                    break;
                }

                dos.writeUTF(fileName);  // Send file request to server

                int fileSize = dis.readInt();
                if (fileSize > 0) {
                    byte[] buffer = new byte[fileSize];
                    dis.readFully(buffer);

                    FileOutputStream fos = new FileOutputStream("received_" + fileName);
                    fos.write(buffer);
                    fos.close();

                    System.out.println("[*] File '" + fileName + "' received successfully.");
                } else {
                    System.out.println("[!] File not found on server.");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
