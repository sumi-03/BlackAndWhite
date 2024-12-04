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

    // 생성자
    public LoginPanel(GameFrame parentFrame, MusicPlayer musicPlayer) {
        this.parentFrame = parentFrame;
        this.musicPlayer = musicPlayer;
        setLayout(null);
        setPreferredSize(new Dimension(960, 600));

        // 배경 이미지 로드
        try {
            backgroundImage = ImageIO.read(new File("src/images/Login_img.png"));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("배경 이미지를 로드할 수 없습니다.");
        }

        // 이름 입력 필드
        JTextField nameField = new JTextField("이름을 입력하세요");
        nameField.setBounds(368, 280, 229, 33);
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

        // 로그인 버튼
        JButton loginButton = new JButton("");
        loginButton.setBounds(380, 407, 210, 55);
        loginButton.setContentAreaFilled(false); // 배경 채우기 비활성화
        loginButton.setBorderPainted(false);    // 테두리 비활성화
        loginButton.setFocusPainted(false);     // 포커스 외형 비활성화
        loginButton.setOpaque(false);           // 완전히 투명하게 설정
        loginButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty() || name.equals("이름을 입력하세요")) {
                // 경고 다이얼로그
                JOptionPane.showMessageDialog(
                        this,
                        "이름을 입력해주세요.",
                        "경고!",
                        JOptionPane.WARNING_MESSAGE
                );
            } else {
                if (parentFrame != null && musicPlayer != null) {
                    // 로비 패널로 이동
                    parentFrame.setContentPane(new LobbyPanel(parentFrame, musicPlayer, name));
                    parentFrame.revalidate();
                    parentFrame.repaint();
                } else {
                    System.out.println("parentFrame 또는 musicPlayer가 없습니다.");
                }
            }
        });
        add(loginButton);

        // 설정 버튼
        JButton settingsButton = new JButton("");
        settingsButton.setBounds(835, 475, 55, 55); // 좌하단에 50x50 크기로 설정
        settingsButton.setContentAreaFilled(false); // 배경 비활성화
        settingsButton.setBorderPainted(false);    // 테두리 비활성화
        settingsButton.setFocusPainted(false);     // 포커스 외형 비활성화
        settingsButton.setOpaque(false);           // 투명한 버튼
        settingsButton.setIcon(new ImageIcon("src/images/settings_icon.png")); // 아이콘 설정
        settingsButton.addActionListener(e -> {
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

        // 배경 이미지 그리기
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            System.out.println("배경 이미지가 로드되지 않았습니다.");
        }
    }
}
