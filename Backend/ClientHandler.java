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
                        sendMessage("SYS_MSG|Selamat datang, " + this.clientName + "!");
                        break;

                    case "CREATE_ROOM":
                        String newRoomName = parts[1].trim();
                        if (!ChatServer.roomManager.containsKey(newRoomName)) {
                            // clientName secara otomatis menjadi owner saat membuat ruangan
                            ChatRoom newRoom = new ChatRoom(newRoomName, this.clientName);
                            ChatServer.roomManager.put(newRoomName, newRoom);

                            newRoom.addMember(this);
                            sendMessage("SYS_MSG|Berhasil membuat ruangan " + newRoomName);
                            newRoom.broadcast("SYS_MSG|" + this.clientName + " telah masuk.");
                        } else {
                            sendMessage("SYS_MSG|Gagal: Nama ruangan sudah dipakai!");
                        }
                        break;

                    case "JOIN_ROOM":
                        String joinRoomName = parts[1].trim();
                        ChatRoom targetJoin = ChatServer.roomManager.get(joinRoomName);

                        if (targetJoin != null) {
                            targetJoin.addMember(this);
                            sendMessage("SYS_MSG|Berhasil masuk ke ruangan " + joinRoomName);
                            targetJoin.broadcast("SYS_MSG|" + this.clientName + " tergabung.");
                        } else {
                            sendMessage("SYS_MSG|Gagal: Ruangan tidak ditemukan.");
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
                            chatRoom.broadcast("CHAT|" + this.clientName + "|" + chatContent);
                        } else {
                            sendMessage("SYS_MSG|Gagal: Ruangan tidak ditemukan.");
                        }
                        break;
                        
                    case "LEAVE_ROOM": // Fitur tambahan dasar agar user bisa keluar sendiri
                        if (parts.length < 2) break;
                        String leaveRoomName = parts[1].trim();
                        ChatRoom leaveRoom = ChatServer.roomManager.get(leaveRoomName);
                        
                        if (leaveRoom != null) {
                            leaveRoom.removeMember(this);
                            leaveRoom.broadcast("SYS_MSG|" + this.clientName + " telah keluar dari ruangan.");
                            sendMessage("SYS_MSG|Anda telah keluar dari ruangan " + leaveRoomName);
                        }
                        break;

                    // ==========================================
                    // FITUR OWNER: KICK DAN CLOSE_ROOM
                    // ==========================================
                    case "KICK":
                        if (parts.length < 3) {
                            sendMessage("SYS_MSG|Gagal: Format salah. Gunakan KICK|nama_room|nama_target");
                            break;
                        }
                        String kickRoomName = parts[1].trim();
                        String targetUserName = parts[2].trim();
                        ChatRoom kickRoom = ChatServer.roomManager.get(kickRoomName);

                        if (kickRoom != null) {
                            // 1. Validasi Otoritas: Cek apakah pengirim perintah adalah owner
                            if (kickRoom.getOwnerName().equals(this.clientName)) {
                                
                                // 2. Cari target user di dalam list anggota ruangan tersebut
                                ClientHandler targetClient = null;
                                for (ClientHandler member : kickRoom.getMembers()) {
                                    if (member.getClientName().equals(targetUserName)) {
                                        targetClient = member;
                                        break;
                                    }
                                }

                                // 3. Eksekusi pengeluaran jika target ditemukan
                                if (targetClient != null) {
                                    kickRoom.removeMember(targetClient);
                                    
                                    // Kirim sinyal khusus FORCE_LEAVE agar GUI target tahu ia harus kembali ke lobby
                                    targetClient.sendMessage("FORCE_LEAVE|Anda telah dikeluarkan oleh pemilik ruangan.");
                                    
                                    // Beritahu anggota lain yang tersisa di ruangan
                                    kickRoom.broadcast("SYS_MSG|" + targetUserName + " telah dikeluarkan dari ruangan.");
                                } else {
                                    sendMessage("SYS_MSG|Gagal: User " + targetUserName + " tidak ditemukan di ruangan.");
                                }
                            } else {
                                sendMessage("SYS_MSG|Gagal: Anda tidak memiliki hak akses. Hanya pemilik ruangan yang dapat melakukan KICK.");
                            }
                        } else {
                            sendMessage("SYS_MSG|Gagal: Ruangan tidak ditemukan.");
                        }
                        break;

                    case "CLOSE_ROOM":
                        if (parts.length < 2) {
                            sendMessage("SYS_MSG|Gagal: Format salah. Gunakan CLOSE_ROOM|nama_room");
                            break;
                        }
                        String closeRoomName = parts[1].trim();
                        ChatRoom closeRoom = ChatServer.roomManager.get(closeRoomName);

                        if (closeRoom != null) {
                            // 1. Validasi Otoritas: Cek apakah pengirim perintah adalah owner
                            if (closeRoom.getOwnerName().equals(this.clientName)) {
                                
                                // 2. Broadcast sinyal paksa keluar ke SEMUA anggota yang ada di ruangan tersebut
                                closeRoom.broadcast("FORCE_LEAVE|Ruangan telah ditutup secara permanen oleh pemilik.");
                                
                                // 3. Hapus ruangan dari memori (HashMap) server utama
                                ChatServer.roomManager.remove(closeRoomName);
                                System.out.println("DEBUG: Ruangan " + closeRoomName + " telah dihapus dari server.");
                                
                            } else {
                                sendMessage("SYS_MSG|Gagal: Anda tidak memiliki hak akses untuk menutup ruangan ini.");
                            }
                        } else {
                            sendMessage("SYS_MSG|Gagal: Ruangan tidak ditemukan.");
                        }
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
                // Mencari dan mengeluarkan user dari semua ruangan jika mereka terputus mendadak
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
}