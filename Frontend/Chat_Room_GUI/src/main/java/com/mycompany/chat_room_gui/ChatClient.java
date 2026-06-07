/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.chat_room_gui;

import java.io.*;
import java.net.Socket;
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
            out.println("LOGIN:" + username);

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
    private void handleIncomingMessage(String rawMsg) {
        // Contoh analisis protokol pesan: "CHAT:Budi:Halo apa kabar"
        if (rawMsg.startsWith("CHAT:")) {
            String[] parts = rawMsg.split(":", 3);
            String sender = parts[1];
            String text = parts[2];

            // Jika kita sedang berada di Layar Chat Room Interface, tampilkan chatnya!
            if (currentActiveFrame instanceof JFrame_Chat_Room_Interface) {
                JFrame_Chat_Room_Interface ui = (JFrame_Chat_Room_Interface) currentActiveFrame;
                if (!sender.equals(currentUsername)) {
                    ui.appendPeerMessage(sender, text); // Muncul di rata kiri
                }
            }
        }
        // Anda bisa menambahkan else if untuk BROADCAST:USER_JOIN, ROOM_CREATED, KICKED, dll. [cite: 13, 18, 19]
    }
}
