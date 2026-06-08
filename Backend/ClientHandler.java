import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String clientName;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    // Method untuk mengirim teks dari server ke klien ini
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    // Getter untuk mengambil nama klien, dibutuhkan saat mencari target KICK
    public String getClientName() {
        return this.clientName;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String message;
            // Terus membaca selama klien belum putus koneksi
            while ((message = in.readLine()) != null) {
                System.out.println("DEBUG (Terima dari klien): " + message);

                // PARSING PROTOKOL
                String[] parts = message.split("\\|", 3);
                String command = parts[0].trim();

                switch (command) {
                    case "LOGIN":
                        this.clientName = parts[1].trim();

                        // Simpan user ke database
                        DatabaseManager.saveUser(this.clientName);

                        sendMessage("SYS_MSG|Selamat datang, " + this.clientName + "!");
                        break;

                    case "CREATE_ROOM":
                        String newRoomName = parts[1].trim();
                        if (!ChatServer.roomManager.containsKey(newRoomName)) {
                            ChatRoom newRoom = new ChatRoom(newRoomName, this.clientName);
                            ChatServer.roomManager.put(newRoomName, newRoom);
                            DatabaseManager.saveRoom(newRoomName, this.clientName);

                            newRoom.addMember(this); // Masuk room
                            DatabaseManager.saveParticipant(newRoomName, this.clientName);

                            sendMessage("OPEN_ROOM|" + newRoomName + "|true");

                            // 🌟 PEMBARUAN: Siarkan list user terbaru (pasti baru ada 1 orang yaitu Owner)
                            newRoom.broadcastUserList();

                            newRoom.broadcast("SYS_MSG|" + this.clientName + " telah masuk.");
                            broadcastLobbyRefresh();
                        } else {
                            sendMessage("SYS_MSG|Gagal: Nama ruangan sudah dipakai!");
                        }
                        break;

                    case "JOIN_ROOM":
                        String joinRoomName = parts[1].trim();

                        // KUNCI UTAMA: Jika room tidak ada di memori RAM (mungkin karena server habis
                        // di-restart)
                        if (!ChatServer.roomManager.containsKey(joinRoomName)) {
                            // Cek ke database apakah room ini sebenarnya ada dan siapa owner-nya
                            String ownerInDb = DatabaseManager.getRoomOwner(joinRoomName);

                            if (ownerInDb != null) {
                                // Jika ada di database, hidupkan kembali room tersebut ke dalam RAM Server!
                                ChatRoom revivedRoom = new ChatRoom(joinRoomName, ownerInDb);
                                ChatServer.roomManager.put(joinRoomName, revivedRoom);
                                System.out
                                        .println("LOG: Memulihkan room [" + joinRoomName + "] dari database ke RAM.");
                            }
                        }

                        // Jalankan logika join room seperti biasa
                        if (ChatServer.roomManager.containsKey(joinRoomName)) {
                            ChatRoom room = ChatServer.roomManager.get(joinRoomName);

                            room.addMember(this);
                            DatabaseManager.saveParticipant(joinRoomName, this.clientName);

                            boolean isOwnerOfRoom = room.getOwnerName().equalsIgnoreCase(this.clientName);
                            sendMessage("OPEN_ROOM|" + joinRoomName + "|" + isOwnerOfRoom);

                            room.broadcastUserList();
                            room.broadcast("SYS_MSG|" + this.clientName + " telah masuk.");
                        } else {
                            // Eror ini hanya akan keluar jika room benar-benar tidak ada di RAM maupun
                            // Database
                            sendMessage("SYS_MSG|Gagal: Ruangan tidak ditemukan di database!");
                        }
                        break;
                    case "SEND_MSG":
                        if (parts.length < 3) {
                            sendMessage("SYS_MSG|Gagal: Format pesan salah.");
                            break;
                        }

                        String targetRoomName = parts[1].trim();
                        String chatContent = parts[2];
                        ChatRoom chatRoom = ChatServer.roomManager.get(targetRoomName);

                        if (chatRoom != null) {
                            // Simpan riwayat chat ke database
                            DatabaseManager.saveMessage(targetRoomName, this.clientName, chatContent);

                            chatRoom.broadcast("CHAT|" + this.clientName + "|" + chatContent);
                        } else {
                            sendMessage("SYS_MSG|Gagal: Ruangan tidak ditemukan.");
                        }
                        break;

                    case "LEAVE_ROOM": // Fitur tambahan dasar agar user bisa keluar sendiri
                        if (parts.length < 2)
                            break;
                        String leaveRoomName = parts[1].trim();
                        ChatRoom leaveRoom = ChatServer.roomManager.get(leaveRoomName);

                        if (leaveRoom != null) {
                            leaveRoom.removeMember(this);
                            DatabaseManager.deleteParticipant(leaveRoomName, this.clientName);
                            leaveRoom.broadcast("SYS_MSG|" + this.clientName + " telah keluar dari ruangan.");
                            sendMessage("SYS_MSG|Anda telah keluar dari ruangan " + leaveRoomName);
                        }
                        break;

                    // ==========================================
                    // FITUR OWNER: KICK DAN CLOSE_ROOM
                    // ==========================================
                    // 🌟 DISESUAIKAN: Mengikuti kiriman "KICK" dari JFrame_Chat_Room_Interface
                    case "KICK":
                        String currentRoomName = parts[1].trim(); // Indeks 1 adalah nama room
                        String userToKick = parts[2].trim(); // Indeks 2 adalah nama target user

                        System.out
                                .println("LOG: Memproses KICK terhadap " + userToKick + " di room " + currentRoomName);

                        if (ChatServer.roomManager.containsKey(currentRoomName)) {
                            ChatRoom room = ChatServer.roomManager.get(currentRoomName);

                            // 1. Cari handler milik user yang akan di-kick
                            ClientHandler targetHandler = null;
                            for (ClientHandler member : room.getMembers()) {
                                if (member.getClientName().equalsIgnoreCase(userToKick)) {
                                    targetHandler = member;
                                    break;
                                }
                            }

                            if (targetHandler != null) {
                                // 2. Kirim perintah keluar paksa ke client target
                                targetHandler
                                        .sendMessage("FORCE_LEAVE|Anda telah dikeluarkan dari ruangan oleh Owner.");

                                // 3. Hapus user dari list member di RAM Server
                                room.removeMember(targetHandler);

                                // 4. KUNCI UTAMA: Siarkan daftar user terbaru ke anggota yang tersisa!
                                room.broadcastUserList();

                                // 5. Beri tahu sisa anggota melalui pesan sistem
                                room.broadcast("SYS_MSG|" + userToKick + " telah dikeluarkan dari ruangan.");
                            } else {
                                System.out.println("LOG: Target user tidak ditemukan di dalam room.");
                            }
                        }
                        break;

                    case "CLOSE_ROOM":
                        String roomToClose = parts[1].trim();
                        System.out.println("LOG: Memproses penutupan room [" + roomToClose + "] oleh Owner.");

                        if (ChatServer.roomManager.containsKey(roomToClose)) {
                            ChatRoom room = ChatServer.roomManager.get(roomToClose);

                            // 1. KUNCI UTAMA: Usir paksa SEMUA member yang ada di dalam room tersebut!
                            // Kita gunakan copy list agar tidak terjadi ConcurrentModificationException
                            java.util.List<ClientHandler> allMembers = new java.util.ArrayList<>(room.getMembers());
                            for (ClientHandler member : allMembers) {
                                // Kirim perintah FORCE_LEAVE agar GUI client otomatis menutup layar chat
                                member.sendMessage("FORCE_LEAVE|Ruangan ini telah ditutup secara permanen oleh Owner.");
                                room.removeMember(member);
                            }

                            // 2. Hapus room dari memori RAM Server
                            ChatServer.roomManager.remove(roomToClose);

                            // 3. Hapus room secara permanen dari Database SQL Server
                            DatabaseManager.deleteRoom(roomToClose);

                            // 4. SEBARKAN REFRESH: Perintahkan semua user yang ada di Lobby untuk
                            // memperbarui tabel mereka
                            broadcastLobbyRefresh();
                        }
                        break;

                    case "GET_ROOMS":
                        // 1. Ambil list dari database SQL Server
                        java.util.List<String[]> dbRooms = DatabaseManager.getAllRooms();

                        // 2. Gabungkan data menjadi format: ROOM_LIST|RoomA~Owner1|RoomB~Owner2
                        StringBuilder sb = new StringBuilder("ROOM_LIST");
                        for (String[] r : dbRooms) {
                            sb.append("|").append(r[0]).append("~").append(r[1]);
                        }

                        // 3. Kirim balik ke client yang meminta
                        sendMessage(sb.toString());
                        break;

                    default:
                        System.out.println("Perintah tidak dikenali: " + command);
                }
            }
        } catch (IOException e) {
            System.out.println("Koneksi terputus dengan: " + clientName);
        } finally {
            // Pembersihan jika klien keluar secara paksa/mati lampu
            try {
                // Mencari dan mengeluarkan user dari semua ruangan jika mereka terputus
                // mendadak
                for (ChatRoom room : ChatServer.roomManager.values()) {
                    if (room.getMembers().contains(this)) {
                        room.removeMember(this);
                        room.broadcast("SYS_MSG|" + this.clientName + " terputus dari server.");
                    }
                }
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Fungsi untuk menyiarkan perintah refresh ke semua client yang sedang
    // terhubung
    private void broadcastLobbyRefresh() {
        // Mengambil semua thread yang sedang berjalan di JVM
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t instanceof ClientHandler) {
                ClientHandler handler = (ClientHandler) t;
                // Kirim protokol khusus REFRESH_LOBBY ke semua komputer client
                handler.sendMessage("REFRESH_LOBBY");
            }
        }
    }
}