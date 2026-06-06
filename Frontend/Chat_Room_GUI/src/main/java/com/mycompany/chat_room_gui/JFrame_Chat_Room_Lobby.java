/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.chat_room_gui;

/**
 *
 * @author ignas
 */
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class JFrame_Chat_Room_Lobby extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(JFrame_Chat_Room_Lobby.class.getName());
    private String currentUsername;

    /**
     * Creates new form JFrame_Lobby
     */
    public JFrame_Chat_Room_Lobby(String username) {
        this.currentUsername = username;

        setTitle("MyChat Lobby");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);
        setResizable(false);

        // Background Utama (Gradient gelap)
        GradientPanel mainPanel = new GradientPanel();
        mainPanel.setLayout(null); // Null layout untuk penempatan presisi sesuai mockup
        setContentPane(mainPanel);

        // 1. Label Judul Lobby
        JLabel titleLabel = new JLabel("MyChat Lobby");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(30, 20, 250, 40);
        mainPanel.add(titleLabel);

        // 2. Info Akun ("Connected as: ...")
        JLabel infoLabel = new JLabel("Connected as: " + currentUsername);
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        infoLabel.setForeground(new Color(150, 200, 255));
        infoLabel.setBounds(30, 60, 300, 20);
        mainPanel.add(infoLabel);

        // 3. Tombol "Buat Room Baru"
        JButton btnCreateRoom = new JButton("Buat Room Baru +") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setPaint(new GradientPaint(0, 0, new Color(45, 115, 215), 0, getHeight(), new Color(30, 85, 165)));
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        btnCreateRoom.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCreateRoom.setForeground(Color.WHITE);
        btnCreateRoom.setContentAreaFilled(false);
        btnCreateRoom.setBorderPainted(false);
        btnCreateRoom.setFocusPainted(false);
        btnCreateRoom.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCreateRoom.setBounds(480, 25, 180, 35);
        mainPanel.add(btnCreateRoom);

        // 4. Membuat Tabel Daftar Room (JTable)
        String[] columnNames = {"Room Name", "Occupancy"};
        Object[][] data = {
            {"General Chat", "25/50"},
            {"Programming Help", "5/10"},
            {"Fun Zone", "10/100"},
            {"Ngopi Santai", "12/30"}
        };

        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabel tidak bisa diedit manual oleh user
            }
        };

        JTable roomTable = new JTable(tableModel);
        roomTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        roomTable.setRowHeight(30);
        roomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // --- STYLING WARNA TABEL (DARK MODE NYAMAN DI MATA) ---
        roomTable.setBackground(new Color(28, 35, 49));
        roomTable.setForeground(new Color(220, 225, 235));
        roomTable.setGridColor(new Color(42, 52, 74));
        roomTable.setSelectionBackground(new Color(45, 115, 215));
        roomTable.setSelectionForeground(Color.WHITE);

        // --- REVOLUSI WARNA HEADER TABEL AGAR FIX GELAP DI NIMBUS LABELS ---
        roomTable.getTableHeader().setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                // Set warna background & tulisan atas tabel
                label.setBackground(new Color(20, 26, 38)); // Gelap pekat elegan
                label.setForeground(new Color(180, 190, 210)); // Tulisan abu-abu terang soft
                label.setFont(new Font("Segoe UI", Font.BOLD, 13));
                label.setHorizontalAlignment(SwingConstants.LEFT); // Rata kiri agar rapi
                
                // Memberikan padding/jarak tipis agar teks tidak terlalu menempel ke garis
                label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(42, 52, 74)), // Garis tepi gelap
                    BorderFactory.createEmptyBorder(5, 8, 5, 5) // Jarak teks inside
                ));
                
                return label;
            }
        });
        
        // Memastikan background utama header ikut tersinkronisasi
        roomTable.getTableHeader().setBackground(new Color(20, 26, 38));
        roomTable.getTableHeader().setOpaque(true);
        
        // --- KUNCI PERBAIKAN: INISIALISASI & SETT WARNA WADAH ---
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(30, 100, 630, 260); // Atur ukuran wadah dulu
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(42, 52, 74)));

        // Pasang warna background gelap ke JScrollPane dan Viewport-nya
        scrollPane.setBackground(new Color(28, 35, 49));
        scrollPane.getViewport().setBackground(new Color(28, 35, 49));

        // BARU MASUKKAN TABLE KE DALAM VIEWPORT SCROLLPANE
        scrollPane.setViewportView(roomTable);

        // Terakhir, masukkan JScrollPane utuh ke panel utama agar tampil
        mainPanel.add(scrollPane);

        // 5. Tombol "Join Room"
        JButton btnJoinRoom = new JButton("Join Room") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setPaint(new GradientPaint(0, 0, new Color(38, 50, 70), 0, getHeight(), new Color(24, 32, 46)));
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        btnJoinRoom.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnJoinRoom.setForeground(Color.WHITE);
        btnJoinRoom.setContentAreaFilled(false);
        btnJoinRoom.setBorderPainted(false);
        btnJoinRoom.setFocusPainted(false);
        btnJoinRoom.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnJoinRoom.setBounds(30, 390, 630, 45);
        mainPanel.add(btnJoinRoom);

        // Aksi Tombol Buat Room (Pop-up Nama Room)
        btnCreateRoom.addActionListener(e -> {
            String roomName = JOptionPane.showInputDialog(this, "Masukkan Nama Room Baru:", "Buat Room Baru", JOptionPane.PLAIN_MESSAGE);
            if (roomName != null && !roomName.trim().isEmpty()) {

                // Perubahan Navigasi ke Layar 3 otomatis jadi Owner
                this.dispose(); // Menutup frame Lobby
                JFrame_Chat_Room_Interface chatRoom = new JFrame_Chat_Room_Interface(currentUsername, roomName.trim(), true);
                chatRoom.setVisible(true);
            }
        });

        // Aksi Tombol Join Room
        btnJoinRoom.addActionListener(e -> {
            int selectedRow = roomTable.getSelectedRow();
            if (selectedRow != -1) {
                String selectedRoom = roomTable.getValueAt(selectedRow, 0).toString();

                // Perubahan Navigasi ke Layar 3
                this.dispose(); // Menutup frame Lobby
                JFrame_Chat_Room_Interface chatRoom = new JFrame_Chat_Room_Interface(currentUsername, selectedRoom, false);
                chatRoom.setVisible(true);

            } else {
                JOptionPane.showMessageDialog(this, "Pilih room terlebih dahulu dari tabel!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    // Panel khusus Background Gradient
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
