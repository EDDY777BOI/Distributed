import java.io.*;
import java.net.*;

// Multithreaded UDP Client
public class UDPClient {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 5001;
    private static final String EOF_MARKER = "EOF_SIGNAL";

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("[*] Connected to server. Type 'exit' to quit.");

            while (true) {
                System.out.print("[*] Enter file name to request (or 'exit' to quit): ");
                String fileName = reader.readLine();

                if (fileName.equalsIgnoreCase("exit")) {
                    System.out.println("[*] Exiting...");
                    break;
                }

                byte[] buffer = fileName.getBytes();
                DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(SERVER_IP), SERVER_PORT);
                socket.send(requestPacket);

                FileOutputStream fos = new FileOutputStream(fileName);
                buffer = new byte[4096];
                DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);

                while (true) {
                    socket.receive(responsePacket);
                    String response = new String(responsePacket.getData(), 0, responsePacket.getLength());

                    if ("FILE_NOT_FOUND".equals(response)) {
                        System.out.println("[!] File not found on server.");
                        break;
                    }

                    if (EOF_MARKER.equals(response)) {
                        System.out.println("[*] File '" + fileName + "' received successfully.");
                        break;
                    }

                    fos.write(responsePacket.getData(), 0, responsePacket.getLength());
                }
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}