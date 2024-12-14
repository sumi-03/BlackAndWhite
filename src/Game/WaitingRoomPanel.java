package Game;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class WaitingRoomPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private JLabel hostNameLabel;
    private JLabel opponentNameLabel;
    private JLabel roomTitleLabel;
    private JLabel privacyLabel;
    private Image backgroundImage;
    private GameFrame parentFrame;

    public WaitingRoomPanel(GameFrame parentFrame, String hostName, String roomTitle, boolean isPrivate, String password) {
        this.parentFrame = parentFrame;
        setLayout(null);
        setPreferredSize(new Dimension(960, 600));

        // 배경 이미지 로드
        try {
            backgroundImage = ImageIO.read(new File("src/images/waitingScreen.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 방 제목 라벨 (상단 중앙)
        roomTitleLabel = new JLabel("방 제목: " + roomTitle, SwingConstants.CENTER);
        roomTitleLabel.setForeground(Color.WHITE);
        roomTitleLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 20));
        roomTitleLabel.setBounds(0, 30, 960, 30);
        add(roomTitleLabel);

        // 비밀방 여부 라벨 (상단 중앙)
        String privacyText = isPrivate ? "비밀방 (비밀번호: " + password + ")" : "공개방";
        privacyLabel = new JLabel(privacyText, SwingConstants.CENTER);
        privacyLabel.setForeground(Color.WHITE);
        privacyLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
        privacyLabel.setBounds(0, 70, 960, 30);
        add(privacyLabel);

        // 방장 이름 라벨 (왼쪽 하단)
        hostNameLabel = new JLabel("RED 플레이어: " + hostName, SwingConstants.LEFT);
        hostNameLabel.setForeground(Color.WHITE);
        hostNameLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
        hostNameLabel.setBounds(30, 520, 300, 30);
        add(hostNameLabel);

        // 상대방 이름 라벨 (오른쪽 하단) - 초기에는 ???로 설정
        opponentNameLabel = new JLabel("BLUE 플레이어: ???", SwingConstants.RIGHT);
        opponentNameLabel.setForeground(Color.WHITE);
        opponentNameLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
        opponentNameLabel.setBounds(630, 520, 300, 30);
        add(opponentNameLabel);

        // 뒤로 가기 버튼
        JButton backButton = new JButton("뒤로가기");
        backButton.setBounds(820, 30, 100, 40);
        backButton.setFocusPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setBorderPainted(true);
        backButton.setForeground(Color.WHITE);
        backButton.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
        backButton.addActionListener(e -> {
            parentFrame.showLobbyPanel(parentFrame.getPlayerName());
            parentFrame.requestUserList();
        });
        add(backButton);
    }

    // 상대방 이름 업데이트 메서드
    public void updateOpponentName(String opponentName) {
        opponentNameLabel.setText("BLUE 플레이어: " + opponentName);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
