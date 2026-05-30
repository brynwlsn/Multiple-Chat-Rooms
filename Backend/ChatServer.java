import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class ChatServer {
    private static final int PORT = 12345;
    
    // "Database" in-memory kita: Map dari NamaRoom ke Objek ChatRoom
    // ConcurrentHashMap digunakan agar aman dari tabrakan antar Thread
    public static Map<String, ChatRoom> roomManager = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("Server Chat Multi-Room berjalan di port " + PORT + "...");
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Klien baru terhubung: " + clientSocket.getInetAddress());
                
                // Serahkan klien ini ke thread terpisah agar server bisa melayani klien lain
                ClientHandler clientThread = new ClientHandler(clientSocket);
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}