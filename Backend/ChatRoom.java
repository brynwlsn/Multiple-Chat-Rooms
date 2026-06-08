import java.io.PrintWriter;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

public class ChatRoom {
    private String roomName;
    private String ownerName;
    // Menggunakan CopyOnWriteArrayList agar aman saat diakses banyak thread
    // bersamaan
    private List<ClientHandler> members = new CopyOnWriteArrayList<>();

    public ChatRoom(String roomName, String ownerName) {
        this.roomName = roomName;
        this.ownerName = ownerName;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public List<ClientHandler> getMembers() {
        return members;
    }

    // Method untuk menambahkan anggota ke dalam ruangan
    public void addMember(ClientHandler client) {
        members.add(client);
    }

    // Method untuk menghapus anggota dari ruangan
    public void removeMember(ClientHandler client) {
        members.remove(client);
    }

    // Method krusial: Mengirim pesan ke semua orang di ruangan ini
    public void broadcast(String message) {
        for (ClientHandler member : members) {
            member.sendMessage(message);
        }
    }

    // 🌟 TAMBAHKAN METHOD INI: Mengirimkan daftar user terbaru ke seluruh anggota
    // di room ini
    public void broadcastUserList() {
        StringBuilder sb = new StringBuilder("UPDATE_USER_LIST");
        for (ClientHandler member : members) {
            sb.append("|").append(member.getClientName());
        }

        // Kirimkan string hasil gabungan (Contoh: UPDATE_USER_LIST|jamsuy|user2) ke
        // semua member
        broadcast(sb.toString());
    }
}