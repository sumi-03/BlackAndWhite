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
        int panelHeight = userListPanel.getHeight() /8;
        int panelWidth = userListPanel.getWidth();
        int verticalGap = 2; // 패널 간의 간격
        // 유저 추가 메서드
        for (int i = 0; i < 3; i++) {
            // 개별 유저 패널 생성
            JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            userPanel.setOpaque(false);
            userPanel.setBounds(0, i * (panelHeight + verticalGap), panelWidth, panelHeight - verticalGap);

            // 유저 이름 라벨 추가
            JLabel userLabel = new JLabel("유저 " + (i + 1));
            userLabel.setForeground(Color.BLACK);

            // 쪽지 버튼 추가
            JButton messageButton = new JButton("쪽지");
            messageButton.setFont(new Font("Malgun Gothic", Font.PLAIN, 11)); // 버튼 크기 조정

            userPanel.add(userLabel);
            userPanel.add(messageButton);

            // userListPanel에 추가
            userListPanel.add(userPanel);
        }
        // 유저 목록 패널을 부모 패널에 추가
        leftPanel.add(userListPanel);

        JLabel label = new JLabel("접속유저", SwingConstants.CENTER);
        label.setBounds(32, 85, 128, 30);
        label.setForeground(Color.BLACK);
        label.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        leftPanel.add(label);

        // 배경 이미지 로드
        try {
            backgroundImage = ImageIO.read(getClass().getResource("/Images/lobby_ntext.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        //EXIT 버튼
        JButton backButton = new JButton("EXIT");
        backButton.setBounds(701, 92, 136, 45);
        backButton.setContentAreaFilled(false); // 배경 비활성화
        backButton.setBorderPainted(false);    // 테두리 비활성화
        backButton.setFocusPainted(false);     // 포커스 외형 비활성화
        backButton.setOpaque(false);           // 투명한 버튼
        backButton.setFont(new Font("Arial", Font.BOLD, 20));
        backButton.addActionListener(e -> {
            parentFrame.setContentPane(new LoginPanel(parentFrame, musicPlayer));
            parentFrame.revalidate();
            parentFrame.repaint();
        });
        add(backButton);
        
        //방만들기 버튼
        JButton CRoomButton = new JButton("방만들기");
        CRoomButton.setBounds(701, 160, 136, 45);
        CRoomButton.setContentAreaFilled(false); // 배경 비활성화
        CRoomButton.setBorderPainted(false);    // 테두리 비활성화
        CRoomButton.setFocusPainted(false);     // 포커스 외형 비활성화
        CRoomButton.setOpaque(false);           // 투명한 버튼
        CRoomButton.setFont(new Font("Malgun Gothic", Font.BOLD, 20));
        /*CRoomButton.addActionListener(e -> {
            parentFrame.setContentPane(new CRoomPanel(parentFrame, musicPlayer));
            parentFrame.revalidate();
            parentFrame.repaint();
          }); */
        add(CRoomButton);
        
        // 설정 버튼
        JButton settingsButton = new JButton("");
        settingsButton.setBounds(806, 455, 55, 55); // 좌하단에 50x50 크기로 설정
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
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
