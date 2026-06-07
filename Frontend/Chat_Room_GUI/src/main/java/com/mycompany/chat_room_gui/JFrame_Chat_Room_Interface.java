/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.chat_room_gui;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 *
 * @author ignas
 */
public class JFrame_Chat_Room_Interface extends javax.swing.JFrame {

    private String username;
    private String roomName;
    private boolean isOwner; // Menentukan hak akses sesuai spesifikasi rubrik

    private JTextPane chatTextPane;
    private JList<String> userJList;
    private DefaultListModel<String> listModel;
    private JTextField messageField;

    // Tombol khusus Owner
    private JButton btnKick;
    private JButton btnCloseRoom;

    public JFrame_Chat_Room_Interface(String username, String roomName, boolean isOwner) {
        this.username = username;
        this.roomName = roomName;
        this.isOwner = isOwner;

        setTitle("MyChat Room - [" + roomName + "]");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(750, 520);
        setLocationRelativeTo(null);
        setResizable(false);

        // Background Utama (Gradient gelap, senada dengan Login & Lobby)
        GradientPanel mainPanel = new GradientPanel();
        mainPanel.setLayout(null);
        setContentPane(mainPanel);

        // 1. Sesuaikan posisi Label Judul Room Chat (Digeser ke kanan sedikit pada koordinat X: 75)
        JLabel lblRoomTitle = new JLabel("Room: " + roomName);
        lblRoomTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblRoomTitle.setForeground(Color.WHITE);
        lblRoomTitle.setBounds(75, 15, 400, 30);
        mainPanel.add(lblRoomTitle);

        // 2. TAMBAHKAN TOMBOL KEMBALI (Back Button) di koordinat X: 20
        JButton btnBack = new JButton("←") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2d.setColor(new Color(255, 255, 255, 30));
                } else {
                    g2d.setColor(new Color(255, 255, 255, 10));
                }
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnBack.setForeground(Color.WHITE);
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setFocusPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.setBounds(20, 15, 45, 30);
        mainPanel.add(btnBack);

        // 3. AKSI TOMBOL KEMBALI (Kirim Sinyal LEAVE_ROOM ke Backend)
        btnBack.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin keluar dari room chat?", "Keluar Room", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                // PERBAIKAN 1: Beritahu backend server bahwa user ini keluar dari room via Socket
                ChatClient.getInstance().sendMessage("LEAVE_ROOM|" + roomName);
                backToLobby();
            }
        });

        // 2. JTextPane untuk Riwayat Chat (Memenuhi Syarat Rata Kanan/Kiri)
        chatTextPane = new JTextPane();
        chatTextPane.setEditable(false);
        chatTextPane.setBackground(new Color(24, 30, 43));
        chatTextPane.setForeground(Color.WHITE);
        chatTextPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JScrollPane chatScrollPane = new JScrollPane(chatTextPane);
        chatScrollPane.setBounds(25, 60, 480, 340);
        chatScrollPane.setBorder(BorderFactory.createLineBorder(new Color(50, 60, 80)));
        mainPanel.add(chatScrollPane);

        // 3. JList untuk Daftar Pengguna di Sebelah Kanan
        JLabel lblUserList = new JLabel("User List");
        lblUserList.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblUserList.setForeground(Color.WHITE);
        lblUserList.setBounds(530, 20, 100, 20);
        mainPanel.add(lblUserList);

        listModel = new DefaultListModel<>();
        listModel.addElement(username + " (You)");
        // PERBAIKAN 2: Menghapus data simulasi palsu agar daftar murni terisi dari aktivitas jaringan database backend

        userJList = new JList<>(listModel);
        userJList.setBackground(new Color(34, 41, 56));
        userJList.setForeground(Color.WHITE);
        userJList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane userScrollPane = new JScrollPane(userJList);
        userScrollPane.setBounds(530, 60, 180, 240);
        userScrollPane.setBorder(BorderFactory.createLineBorder(new Color(50, 60, 80)));
        mainPanel.add(userScrollPane);

        // 4. Input Pesan & Tombol Kirim
        messageField = new JTextField();
        messageField.setBackground(new Color(34, 41, 56));
        messageField.setForeground(Color.WHITE);
        messageField.setCaretColor(Color.WHITE);
        messageField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        messageField.setBounds(25, 415, 380, 40);
        mainPanel.add(messageField);

        JButton btnSend = new JButton("Send") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setPaint(new GradientPaint(0, 0, new Color(45, 115, 215), 0, getHeight(), new Color(30, 85, 165)));
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        btnSend.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSend.setForeground(Color.WHITE);
        btnSend.setContentAreaFilled(false);
        btnSend.setBorderPainted(false);
        btnSend.setBounds(415, 415, 90, 40);
        btnSend.setCursor(new Cursor(Cursor.HAND_CURSOR));
        mainPanel.add(btnSend);

        // 5. Tombol Fitur Akses Owner (Kick & Tutup Room)
        // 5. Tombol Fitur Akses Owner (Kick & Tutup Room)
        btnKick = new JButton("Kick User");
        btnKick.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnKick.setBackground(new Color(180, 40, 40)); // Warna Merah
        btnKick.setForeground(Color.WHITE);
        btnKick.setContentAreaFilled(true);  // 👑 TAMBAHKAN INI agar warna background keluar
        btnKick.setBorderPainted(false);     // 👑 TAMBAHKAN INI agar tidak di-override OS
        btnKick.setFocusPainted(false);
        btnKick.setBounds(530, 315, 180, 35);
        btnKick.setCursor(new Cursor(Cursor.HAND_CURSOR));
        mainPanel.add(btnKick);

        btnCloseRoom = new JButton("Tutup Room 👑");
        btnCloseRoom.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCloseRoom.setBackground(new Color(210, 105, 30)); // Warna Oranye/Cokelat
        btnCloseRoom.setForeground(Color.WHITE);
        btnCloseRoom.setContentAreaFilled(true);  // 👑 TAMBAHKAN INI agar warna background keluar
        btnCloseRoom.setBorderPainted(false);     // 👑 TAMBAHKAN INI agar tidak di-override OS
        btnCloseRoom.setFocusPainted(false);
        btnCloseRoom.setBounds(530, 365, 180, 35);
        btnCloseRoom.setCursor(new Cursor(Cursor.HAND_CURSOR));
        mainPanel.add(btnCloseRoom);

        // [VALIDASI RUBRIK] Sembunyikan tombol jika user BUKAN owner
        if (!isOwner) {
            btnKick.setVisible(false);
            btnCloseRoom.setVisible(false);
        }

        // --- ACTION LISTENERS ---
        btnSend.addActionListener(e -> appendMessage());
        messageField.addActionListener(e -> appendMessage());

        // Aksi Kick User (Hanya Owner yang bisa mengeksekusi)
        btnKick.addActionListener(e -> {
            String selectedUser = userJList.getSelectedValue();
            if (selectedUser != null) {
                // Hilangkan imbuhan "(You)" atau spasi jika ada pencocokan string nama
                String targetName = selectedUser.replace(" (You)", "").trim();

                if (targetName.equals(username)) {
                    JOptionPane.showMessageDialog(this, "Anda tidak bisa menendang diri Anda sendiri!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menendang " + targetName + "?", "Konfirmasi Kick", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    // PERBAIKAN 3: Kirim protokol jaringan KICK ke server backend teman Anda
                    ChatClient.getInstance().sendMessage("KICK|" + roomName + "|" + targetName);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Pilih user aktif dari JList yang ingin di-kick!");
            }
        });

        // Aksi Tutup Room
        btnCloseRoom.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin menutup ruangan ini secara permanen?", "Konfirmasi Tutup Room", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                // PERBAIKAN 4: Kirim protokol jaringan CLOSE_ROOM ke server backend teman Anda
                ChatClient.getInstance().sendMessage("CLOSE_ROOM|" + roomName);
            }
        });

        // Beritahu sistem jaringan bahwa layar chat room inilah yang sekarang sedang aktif di monitor
        ChatClient.getInstance().setActiveFrame(this);
    }

    // Poin Nilai Plus: Mengatur chat kiriman sendiri agar RATA KANAN
    private void appendMessage() {
        String text = messageField.getText().trim();
        if (!text.isEmpty()) {
            // Kirim pesan asli ke server backend (Format: SEND_MSG|NamaRoom|IsiPesan)
            ChatClient.getInstance().sendMessage("SEND_MSG|" + roomName + "|" + text);

            // Cetak di layar sendiri (Rata Kanan)
            SimpleAttributeSet right = new SimpleAttributeSet();
            StyleConstants.setAlignment(right, StyleConstants.ALIGN_RIGHT);
            StyleConstants.setForeground(right, new Color(130, 200, 255));
            StyleConstants.setFontSize(right, 14);

            try {
                StyledDocument doc = chatTextPane.getStyledDocument();
                int length = doc.getLength();
                doc.insertString(length, "You: " + text + "\n", right);
                doc.setParagraphAttributes(length, text.length() + 6, right, false);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            messageField.setText("");
        }
    }

    private void backToLobby() {
        this.dispose();
        JFrame_Chat_Room_Lobby lobby = new JFrame_Chat_Room_Lobby(username);
        lobby.setVisible(true);
    }

    // Gradient Background
    class GradientPanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color color1 = new Color(19, 26, 43);
            Color color2 = new Color(34, 41, 56);
            GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    // 1. Untuk mencetak pesan dari pengguna lain (Rata Kiri)
    public void appendPeerMessage(String sender, String text) {
        SimpleAttributeSet left = new SimpleAttributeSet();
        StyleConstants.setAlignment(left, StyleConstants.ALIGN_LEFT);
        StyleConstants.setForeground(left, Color.WHITE);
        StyleConstants.setFontSize(left, 14);

        try {
            StyledDocument doc = chatTextPane.getStyledDocument();
            int length = doc.getLength();
            doc.insertString(length, sender + ": " + text + "\n", left);
            doc.setParagraphAttributes(length, sender.length() + text.length() + 3, left, false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // 2. Untuk mencetak pemberitahuan sistem (Rata Tengah / Warna Abu-abu)
    public void appendSystemMessage(String sysText) {
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        StyleConstants.setForeground(center, Color.LIGHT_GRAY);
        StyleConstants.setItalic(center, true);

        try {
            StyledDocument doc = chatTextPane.getStyledDocument();
            int length = doc.getLength();
            doc.insertString(length, "[SISTEM] " + sysText + "\n", center);
            doc.setParagraphAttributes(length, sysText.length() + 10, center, false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
