package Game;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class LoginPanel extends JPanel {
    private final GameFrame parentFrame;
    private final MusicPlayer musicPlayer;
    private Image backgroundImage;

    public LoginPanel(GameFrame parentFrame, MusicPlayer musicPlayer) {
        this.parentFrame = parentFrame;
        this.musicPlayer = musicPlayer;
        setLayout(null);
        setPreferredSize(new Dimension(960, 600));

        // 배경 이미지 로드
        try {
            backgroundImage = ImageIO.read(new File("src/images/loginScreen.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 이름 입력 필드
        JTextField nameField = new JTextField("이름을 입력하세요");
        nameField.setBounds(423, 330, 200, 24);
        nameField.setBorder(BorderFactory.createEmptyBorder());
        nameField.setHorizontalAlignment(SwingConstants.CENTER);
        nameField.setForeground(Color.GRAY);
        nameField.setFont(new Font("Malgun Gothic", Font.BOLD, 16));
        nameField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (nameField.getText().equals("이름을 입력하세요")) {
                    nameField.setText("");
                    nameField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (nameField.getText().isEmpty()) {
                    nameField.setText("이름을 입력하세요");
                    nameField.setForeground(Color.GRAY);
                }
            }
        });
        add(nameField);

        setFocusable(true);
        requestFocusInWindow();

        // 로그인 버튼
        JButton loginButton = new JButton("");
        loginButton.setBounds(380, 407, 210, 55);
        loginButton.setContentAreaFilled(false);
        loginButton.setBorderPainted(false);
        loginButton.setFocusPainted(false);
        loginButton.setOpaque(false);
        loginButton.addActionListener(e -> {
            String playerName = nameField.getText().trim();
            if (!playerName.isEmpty() && !playerName.equals("이름을 입력하세요")) {
                parentFrame.initializeClient(playerName);
                parentFrame.showLobbyPanel(playerName);
            } else {
                JOptionPane.showMessageDialog(this, "이름을 입력해주세요.", "경고!", JOptionPane.WARNING_MESSAGE);
            }
        });

        // 엔터 키 이벤트 추가
        nameField.addActionListener(e -> {
            AudioPlayer.playSound(AudioPlayer.SoundPaths.ENTER_LOBBY);
            loginButton.doClick(); // 로그인 버튼 클릭 동작 실행
        });
        add(loginButton);

        // 설정 버튼
        JButton settingsButton = new JButton("");
        settingsButton.setBounds(835, 475, 55, 55); // 좌하단에 50x50 크기로 설정
        settingsButton.setContentAreaFilled(false); // 배경 비활성화
        settingsButton.setBorderPainted(false);    // 테두리 비활성화
        settingsButton.setFocusPainted(false);     // 포커스 외형 비활성화
        settingsButton.setOpaque(false);           // 투명한 버튼
        settingsButton.addActionListener(e -> {
            AudioPlayer.playSound(AudioPlayer.SoundPaths.ENTER_LOBBY);
            // 설정 패널 호출
            if (parentFrame != null && musicPlayer != null) {
                SettingsPanel settingsPanel = new SettingsPanel(parentFrame, musicPlayer);
                parentFrame.getLayeredPane().add(settingsPanel, JLayeredPane.POPUP_LAYER);
                parentFrame.getLayeredPane().revalidate();
                parentFrame.getLayeredPane().repaint();
            }
        });
        add(settingsButton);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
