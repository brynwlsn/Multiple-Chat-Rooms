import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseManager {

    // Sesuaikan dengan konfigurasi SQL Server kamu
    // encrypt=true;trustServerCertificate=true; sangat penting untuk versi JDBC
    // terbaru agar tidak kena error SSL
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=MultipleChatRoom;encrypt=true;trustServerCertificate=true;";

    // Default SSMS biasanya menggunakan otentikasi Windows,
    // Tapi jika kamu pakai SQL Server Authentication (sa)', masukkan di sini:
    private static final String USER = "sa";
    private static final String PASS = "passwordSQLAnda";

    // Method untuk mendapatkan koneksi
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    // 1. Menyimpan User Baru (LOGIN)
    public static void saveUser(String username) {
        // Query SQL Server: Cek dulu apakah ada, jika tidak baru insert (pengganti
        // INSERT IGNORE)
        String query = "IF NOT EXISTS (SELECT * FROM users WHERE username = ?) " +
                "INSERT INTO users (username) VALUES (?)";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
            System.out.println("DB-LOG: User " + username + " disinkronisasi ke database.");

        } catch (SQLException e) {
            System.out.println("DB-ERROR (saveUser): " + e.getMessage());
        }
    }

    // 2. Menyimpan Room Baru (CREATE_ROOM)
    public static void saveRoom(String roomName, String ownerName) {
        String query = "INSERT INTO chat_rooms (room_name, owner_name) VALUES (?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, roomName);
            pstmt.setString(2, ownerName);
            pstmt.executeUpdate();
            System.out.println("DB-LOG: Room " + roomName + " disimpan ke database.");

        } catch (SQLException e) {
            System.out.println("DB-ERROR (saveRoom): " + e.getMessage());
        }
    }

    // 3. Menyimpan Pesan (SEND_MSG)
    public static void saveMessage(String roomName, String senderName, String messageText) {
        String query = "INSERT INTO messages (room_name, sender_name, message_text) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, roomName);
            pstmt.setString(2, senderName);
            pstmt.setString(3, messageText);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("DB-ERROR (saveMessage): " + e.getMessage());
        }
    }

    // 4. Menghapus Room dan isinya (CLOSE_ROOM)
    public static void deleteRoom(String roomName) {
        String query = "DELETE FROM chat_rooms WHERE room_name = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, roomName);
            pstmt.executeUpdate();
            System.out.println("DB-LOG: Room " + roomName + " dihapus dari database.");

        } catch (SQLException e) {
            System.out.println("DB-ERROR (deleteRoom): " + e.getMessage());
        }
    }

    // Metode Baru 1: Menyimpan data ketika user masuk ke sebuah ruangan
    public static void saveParticipant(String roomName, String username) {
        String query = "INSERT INTO room_participants (room_name, username) VALUES (?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, roomName);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
            System.out.println("DB-LOG: User " + username + " dicatat sebagai peserta di room " + roomName);
        } catch (SQLException e) {
            System.out.println("DB-ERROR (saveParticipant): " + e.getMessage());
        }
    }

    // Metode Baru 2: Menghapus data ketika user keluar/dikick dari ruangan
    public static void deleteParticipant(String roomName, String username) {
        String query = "DELETE FROM room_participants WHERE room_name = ? AND username = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, roomName);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
            System.out.println("DB-LOG: User " + username + " dihapus dari peserta room " + roomName);
        } catch (SQLException e) {
            System.out.println("DB-ERROR (deleteParticipant): " + e.getMessage());
        }
    }

    // Ambil seluruh daftar ruangan yang ada untuk Lobby
    public static java.util.List<String[]> getAllRooms() {
        java.util.List<String[]> roomList = new java.util.ArrayList<>();
        String query = "SELECT room_name, owner_name FROM chat_rooms";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query);
                java.sql.ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                // Simpan format [Nama Room, Owner] ke list
                roomList.add(new String[] { rs.getString("room_name"), rs.getString("owner_name") });
            }
        } catch (SQLException e) {
            System.out.println("DB-ERROR (getAllRooms): " + e.getMessage());
        }
        return roomList;
    }

    // Mencari tahu owner dari suatu room, return null jika room tidak terdaftar
    public static String getRoomOwner(String roomName) {
        String query = "SELECT owner_name FROM chat_rooms WHERE room_name = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, roomName);
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("owner_name");
                }
            }
        } catch (SQLException e) {
            System.out.println("DB-ERROR (getRoomOwner): " + e.getMessage());
        }
        return null; // Room tidak ditemukan di database
    }
}