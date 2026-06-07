package com.mycompany.chat_room_gui;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
/**
 *
 * @author ignas
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;

public class JFrame_Chat_Room_Login extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(JFrame_Chat_Room_Login.class.getName());

    /**
     * Creates new form JFrame_Chat_Room
     */
    public JFrame_Chat_Room_Login() {
        // Pengaturan dasar JFrame
        setTitle("MyChat App - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 550);
        setLocationRelativeTo(null); // Membuat window muncul di tengah layar
        setResizable(false);

        // Menambahkan custom panel dengan background gradient
        GradientPanel mainPanel = new GradientPanel();
        mainPanel.setLayout(new GridBagLayout());
        setContentPane(mainPanel);

        // Membuat Kotak Login (Card) yang melengkung
        LoginCard loginCard = new LoginCard();

        // Menambahkan Kotak Login ke komponen utama menggunakan GridBagLayout agar tetap di tengah
        mainPanel.add(loginCard);
    }

    // Panel khusus untuk Background Gradient (Gelap Kebiruan)
    class GradientPanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Warna gradient dari pojok kiri atas ke kanan bawah
            Color color1 = new Color(19, 26, 43);
            Color color2 = new Color(34, 41, 56);
            GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    // Panel khusus untuk Kotak/Card Login
    class LoginCard extends JPanel {

        private JTextField usernameField;
        private JButton connectButton;
        private JLabel statusLabel;

        public LoginCard() {
            setOpaque(false);
            setPreferredSize(new Dimension(300, 380));
            setLayout(null); // Menggunakan null layout untuk presisi posisi komponen internal

            // 1. Label Judul "Login"
            JLabel titleLabel = new JLabel("Login", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setBounds(0, 30, 300, 40);
            add(titleLabel);

            // 2. Label "Username"
            JLabel userLabel = new JLabel("Username");
            userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            userLabel.setForeground(new Color(200, 200, 200));
            userLabel.setBounds(30, 110, 240, 20);
            add(userLabel);

            // 3. Input Text / JTextField untuk Username
            usernameField = new JTextField() {
                // Efek rounded untuk text field
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(new Color(28, 35, 49));
                    g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                    g2d.dispose();
                    super.paintComponent(g);
                }
            };
            usernameField.setOpaque(false);
            usernameField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            usernameField.setForeground(Color.WHITE);
            usernameField.setCaretColor(Color.WHITE);
            usernameField.setBounds(30, 135, 240, 40);
            add(usernameField);

            // 4. Tombol "Connect" (Custom Styled Button)
            connectButton = new JButton("Connect") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    // Warna biru glossy/semi-transparent
                    g2d.setPaint(new GradientPaint(0, 0, new Color(30, 85, 165), 0, getHeight(), new Color(20, 60, 125)));
                    g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
                    g2d.dispose();
                    super.paintComponent(g);
                }
            };
            connectButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
            connectButton.setForeground(Color.WHITE);
            connectButton.setContentAreaFilled(false);
            connectButton.setBorderPainted(false);
            connectButton.setFocusPainted(false);
            connectButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            connectButton.setBounds(30, 210, 240, 45);
            add(connectButton);

            // 5. Label Status Koneksi (Glow/Dot Indikator)
            statusLabel = new JLabel("● Disconnected - Enter Username", SwingConstants.CENTER);
            statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            statusLabel.setForeground(new Color(150, 150, 150));
            statusLabel.setBounds(0, 320, 300, 20);
            add(statusLabel);

            // Logika Tombol saat diklik
            // Logika Tombol saat diklik (Di dalam file JFrame_Chat_Room.java)
            connectButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String username = usernameField.getText().trim();
                    if (!username.isEmpty()) {
                        statusLabel.setText("Connecting to server...");
                        statusLabel.setForeground(new Color(230, 160, 30));

                        // Ambil instance client jaringan
                        ChatClient client = ChatClient.getInstance();

                        // Masukkan IP laptop teman Anda yang menyalakan program Server backend.
                        // Gunakan "localhost" jika Anda menguji coba Server & GUI di satu laptop yang sama.
                        String ipServer = "localhost";
                        int portServer = 12345; // Sesuaikan port dengan settingan di backend teman Anda

                        // Jalankan koneksi socket TCP
                        if (client.startConnection(ipServer, portServer, username)) {
                            // Jika sukses connect, pindah ke Layar 2 (Lobby)
                            JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(LoginCard.this);
                            currentFrame.dispose();

                            JFrame_Chat_Room_Lobby lobbyScreen = new JFrame_Chat_Room_Lobby(username);
                            lobbyScreen.setVisible(true);
                        } else {
                            statusLabel.setText("● Connection Failed! Server Offline.");
                            statusLabel.setForeground(Color.RED);
                            JOptionPane.showMessageDialog(null, "Gagal terhubung ke Server! Pastikan Server backend sudah dinyalakan.", "Error Koneksi", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Username tidak boleh kosong!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                    }
                }
            });
        }

        // Menggambar background kotak login dengan opacity dan rounded corner
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Background abu-abu gelap transparan (Glassmorphism effect semu)
            g2d.setColor(new Color(45, 52, 71, 180));
            g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 30, 30));

            // Border tipis di sekeliling kotak login
            g2d.setColor(new Color(255, 255, 255, 30));
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 30, 30));
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
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new JFrame_Chat_Room_Login().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
