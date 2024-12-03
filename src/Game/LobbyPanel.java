package Game;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class LobbyPanel extends JPanel {
    private Image backgroundImage;
    private MusicPlayer musicPlayer;

    public LobbyPanel(GameFrame parentFrame, MusicPlayer musicPlayer, String playerName) {
        setPreferredSize(new Dimension(960, 600));
        setLayout(null);
        this.musicPlayer = musicPlayer;

        // 왼쪽 패널
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(null);
        leftPanel.setBounds(55, 90, 181, 448);
        leftPanel.setOpaque(false);
        add(leftPanel);

        JLabel profileLabel = new JLabel(playerName, SwingConstants.CENTER);
        profileLabel.setForeground(Color.WHITE);
        profileLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 16));
        profileLabel.setBounds(17, 10, 159, 79);
        leftPanel.add(profileLabel);

        // 접속 유저 목록 패널
        JPanel userListPanel = new JPanel();
        userListPanel.setLayout(null); // 절대 위치로 설정
        userListPanel.setBounds(34, 125, 128, 280); // 위치 및 크기 설정
        userListPanel.setOpaque(false);

        // 각 유저 패널의 높이 계산
        int panelHeight = userListPanel.getHeight() / 8;
        int panelWidth = userListPanel.getWidth();
        int verticalGap = 2; // 패널 간의 간격

        for (int i = 0; i < 5; i++) {
            JPanel userPanel = new JPanel(null); // 절대 위치로 설정
            userPanel.setOpaque(false);
            userPanel.setBounds(0, i * (panelHeight + verticalGap), panelWidth, panelHeight - verticalGap);

            JLabel userLabel = new JLabel("유저 " + (i + 1));
            userLabel.setForeground(Color.BLACK);
            userLabel.setBounds(5, 5, panelWidth - 40, panelHeight - 15); // 왼쪽에 배치
            userPanel.add(userLabel);

            JButton messageButton = new JButton();
            messageButton.setIcon(new ImageIcon("src/images/mail_icon.png")); // 아이콘 설정
            messageButton.setBounds(panelWidth - 50, 5, 35, panelHeight - 12); // 오른쪽에 배치
            userPanel.add(messageButton);

            userListPanel.add(userPanel);
        }
        leftPanel.add(userListPanel);

        JLabel label = new JLabel("접속유저", SwingConstants.CENTER);
        label.setBounds(32, 85, 128, 30);
        label.setForeground(Color.BLACK);
        label.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
        leftPanel.add(label);

        // 오른쪽 패널
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(null);
        rightPanel.setBounds(684, 90, 181, 448);
        rightPanel.setOpaque(false);
        add(rightPanel);

        // EXIT 버튼
        JButton backButton = new JButton("EXIT");
        backButton.setBounds(16, 2, 135, 43); // 위치는 RightPanel 내부 기준
        backButton.setContentAreaFilled(false);
        backButton.setFont(new Font("Arial", Font.BOLD, 20));
        backButton.addActionListener(e -> {
            parentFrame.setContentPane(new LoginPanel(parentFrame, musicPlayer));
            parentFrame.revalidate();
            parentFrame.repaint();
        });
        rightPanel.add(backButton);

        // 방만들기 버튼
        JButton cRoomButton = new JButton("방만들기");
        cRoomButton.setBounds(16, 73, 135, 43); // 위치는 RightPanel 내부 기준
        cRoomButton.setContentAreaFilled(false);
        cRoomButton.setFont(new Font("Malgun Gothic", Font.BOLD, 20));
        rightPanel.add(cRoomButton);

        // 설정 버튼
        JButton settingsButton = new JButton("");
        settingsButton.setBounds(23, 150, 55, 55); // 위치는 RightPanel 내부 기준
        settingsButton.setContentAreaFilled(false);
        settingsButton.setBorderPainted(false);
        settingsButton.setFocusPainted(false);
        settingsButton.setOpaque(false);
        settingsButton.setIcon(new ImageIcon("src/images/settings_icon.png"));
        settingsButton.addActionListener(e -> {
            if (parentFrame != null && musicPlayer != null) {
                SettingsPanel settingsPanel = new SettingsPanel(parentFrame, musicPlayer);
                parentFrame.getLayeredPane().add(settingsPanel, JLayeredPane.POPUP_LAYER);
                parentFrame.getLayeredPane().revalidate();
                parentFrame.getLayeredPane().repaint();
            }
        });
        rightPanel.add(settingsButton);
        
        JPanel CenterPanel = new JPanel();
        CenterPanel.setBounds(275, 90, 377, 275);
        CenterPanel.setLayout(null);
        CenterPanel.setOpaque(false);
        add(CenterPanel);
        
        JPanel Chatpanel = new JPanel();
        Chatpanel.setBounds(248, 369, 424, 150);
        Chatpanel.setLayout(null);
        Chatpanel.setOpaque(false);
        add(Chatpanel);

        // 배경 이미지 로드
        try {
            backgroundImage = ImageIO.read(getClass().getResource("/Images/lobby_ntext.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
