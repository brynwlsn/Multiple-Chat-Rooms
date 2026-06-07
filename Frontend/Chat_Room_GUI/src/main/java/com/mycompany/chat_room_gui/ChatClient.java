/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.chat_room_gui;

import java.io.*;
import java.net.Socket;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class ChatClient {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String currentUsername;

    // Instance tunggal (Singleton) agar bisa diakses dari Frame mana saja
    private static ChatClient instance;
    private Object currentActiveFrame; // Untuk mendeteksi frame mana yang sedang aktif

    private ChatClient() {
    }

    public static ChatClient getInstance() {
        if (instance == null) {
            instance = new ChatClient();
        }
        return instance;
    }

    // Fungsi Utama untuk menyambungkan GUI ke Server teman Anda
    public boolean startConnection(String ipAddress, int port, String username) {
        try {
            this.currentUsername = username;
            this.socket = new Socket(ipAddress, port); // Menembak IP Server
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);

            // Kirim pesan identitas pertama kali ke server untuk disimpan ke Database backend
            // (Sesuaikan format teks ini dengan protokol kode backend milik teman Anda!)
            out.println("LOGIN|" + username);

            // Jalankan Thread background untuk mendengarkan kiriman chat masuk tanpa henti
            new Thread(new ServerListener()).start();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Fungsi untuk mengirimkan data teks ke Server
    public void sendMessage(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }

    public void setActiveFrame(Object frame) {
        this.currentActiveFrame = frame;
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    // Thread internal penunggu pesan masuk dari Server
    private class ServerListener implements Runnable {

        @Override
        public void run() {
            try {
                String response;
                while ((response = in.readLine()) != null) {
                    final String messageFromServer = response;

                    // Alirkan pesan dari server ke GUI secara aman (Thread-Safe)
                    SwingUtilities.invokeLater(() -> {
                        handleIncomingMessage(messageFromServer);
                    });
                }
            } catch (IOException e) {
                System.out.println("Koneksi terputus dari server.");
            }
        }
    }

    // Menangani logika pemisahan string perintah (Protocol Parsing) dari backend
    // Menangani logika pemisahan string perintah dari backend (Menggunakan pembatas Pipa '|')
    private void handleIncomingMessage(String rawMsg) {
        System.out.println("DEBUG (Terima dari Server): " + rawMsg);

        // Memisahkan pesan berdasarkan tanda pipa |
        String[] parts = rawMsg.split("\\|", 3);
        String command = parts[0].trim();

        if (command.equals("CHAT")) {
            // Format dari backend: CHAT|SenderName|MessageText
            String sender = parts[1].trim();
            String text = parts[2].trim();

            if (currentActiveFrame instanceof JFrame_Chat_Room_Interface) {
                JFrame_Chat_Room_Interface ui = (JFrame_Chat_Room_Interface) currentActiveFrame;
                if (!sender.equals(currentUsername)) {
                    ui.appendPeerMessage(sender, text);
                }
            }
        } else if (command.equals("SYS_MSG")) {
            // Format dari backend: SYS_MSG|PesanSistem
            String sysText = parts[1].trim();

            // Jika berada di layar chat, cetak sebagai pesan sistem di text pane
            if (currentActiveFrame instanceof JFrame_Chat_Room_Interface) {
                JFrame_Chat_Room_Interface ui = (JFrame_Chat_Room_Interface) currentActiveFrame;
                ui.appendSystemMessage(sysText);
            } else {
                // Jika di luar layar chat, tampilkan sebagai pop-up pemberitahuan interaktif
                JOptionPane.showMessageDialog(null, sysText, "Informasi Sistem", JOptionPane.INFORMATION_MESSAGE);
            }
        } else if (command.equals("FORCE_LEAVE")) {
            // Format dari backend jika room ditutup owner atau di-kick
            String reason = parts[1].trim();
            JOptionPane.showMessageDialog(null, reason, "Peringatan", JOptionPane.WARNING_MESSAGE);

            if (currentActiveFrame instanceof JFrame_Chat_Room_Interface) {
                JFrame_Chat_Room_Interface ui = (JFrame_Chat_Room_Interface) currentActiveFrame;
                ui.dispose(); // Tutup layar chat

                // Kembali ke Lobby secara otomatis sesuai spesifikasi rubrik jarkom
                JFrame_Chat_Room_Lobby lobby = new JFrame_Chat_Room_Lobby(currentUsername);
                lobby.setVisible(true);
            }
        } else if (rawMsg.equals("REFRESH_LOBBY")) {
            // Cek apakah user saat ini sedang membuka halaman Lobby
            if (currentActiveFrame instanceof JFrame_Chat_Room_Lobby) {
                JFrame_Chat_Room_Lobby lobbyUI = (JFrame_Chat_Room_Lobby) currentActiveFrame;

                // Jalankan fungsi refresh tabel secara aman di dalam Thread UI Swing
                SwingUtilities.invokeLater(() -> {
                    lobbyUI.refreshTableData();
                });
            }
        }
    }

    // Method untuk memberi tahu ChatClient frame mana yang sedang tampil di layar
    public void setCurrentActiveFrame(Object frame) {
        this.currentActiveFrame = frame;
    }
}
