package Game;

import java.awt.*;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public class LobbyPanel extends JPanel {
    private final GameFrame parentFrame;
    private Image backgroundImage;
    private MusicPlayer musicPlayer;
    private JPanel userListPanel;
    private JTextArea textArea;

    public LobbyPanel(GameFrame parentFrame, MusicPlayer musicPlayer, String playerName) {
        this.parentFrame = parentFrame;
        setPreferredSize(new Dimension(960, 600));
        setLayout(null);
        this.musicPlayer = musicPlayer;

        // 왼쪽 패널
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(null);
        leftPanel.setBounds(55, 10, 181, 580);
        leftPanel.setOpaque(false);
        add(leftPanel);

        JLabel profileLabel = new JLabel(playerName, SwingConstants.CENTER);
        profileLabel.setForeground(Color.WHITE);
        profileLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
        profileLabel.setBounds(34, 85, 128, 31);
        leftPanel.add(profileLabel);

        // 접속 유저 목록 패널
        userListPanel = new JPanel();
        userListPanel.setLayout(null);
        userListPanel.setBounds(38, 196, 128, 280);
        userListPanel.setOpaque(false);
        leftPanel.add(userListPanel);

        // 초기 유저 목록 설정
        updateUserList(new String[]{});

        // 오른쪽 패널
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(null);
        rightPanel.setBounds(750, 10, 181, 580);
        rightPanel.setOpaque(false);
        add(rightPanel);

        // 방 만들기 버튼
        JButton cRoomButton = new JButton("");
        cRoomButton.setBounds(35, 81, 108, 34);
        cRoomButton.setContentAreaFilled(false);
        cRoomButton.setBorderPainted(false);
        cRoomButton.setFocusPainted(false);
        cRoomButton.setOpaque(false);
        cRoomButton.addActionListener(e -> {
            CreateRoomPanel createRoomPanel = new CreateRoomPanel(parentFrame);
            parentFrame.getLayeredPane().add(createRoomPanel, JLayeredPane.POPUP_LAYER);
            parentFrame.getLayeredPane().revalidate();
            parentFrame.getLayeredPane().repaint();
        });
        rightPanel.add(cRoomButton);
        rightPanel.add(cRoomButton);

        // EXIT 버튼
        JButton backButton = new JButton("");
        backButton.setBounds(35, 28, 108, 34);
        backButton.setContentAreaFilled(false);
        backButton.setBorderPainted(false);
        backButton.setFocusPainted(false);
        backButton.setOpaque(false);
        backButton.addActionListener(e -> {
            try {
                // 클라이언트 연결 종료
                this.parentFrame.getClient().closeConnection();

                // 초기 화면으로 돌아가기
                parentFrame.setContentPane(new LoginPanel(parentFrame, musicPlayer));
                parentFrame.revalidate();
                parentFrame.repaint();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        rightPanel.add(backButton);

        // 설정 버튼
        JButton settingsButton = new JButton("");
        settingsButton.setEnabled(true); // 버튼 활성화
        settingsButton.setBounds(64, 456, 79, 68);
        settingsButton.setContentAreaFilled(false);
        settingsButton.setBorderPainted(false);
        settingsButton.setFocusPainted(false);
        settingsButton.setOpaque(false);
        rightPanel.add(settingsButton);

        // 클릭 이벤트 리스너
        settingsButton.addActionListener(e -> {
            SettingsPanel settingsPanel = new SettingsPanel(parentFrame, musicPlayer);
            parentFrame.getLayeredPane().add(settingsPanel, JLayeredPane.POPUP_LAYER);
            parentFrame.getLayeredPane().revalidate();
            parentFrame.getLayeredPane().repaint();
        });

        // 채팅 패널
        JPanel chatPanel = new JPanel();
        chatPanel.setBounds(248, 381, 490, 136);
        chatPanel.setOpaque(false);
        chatPanel.setLayout(null);
        add(chatPanel);

        // 채팅 표시용 JTextArea
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBounds(80, 18, 396, 77);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        chatPanel.add(scrollPane);

        // 채팅 입력 JTextField
        JTextField inputField = new JTextField();
        inputField.setBounds(91, 97, 300, 23);
        inputField.setOpaque(false);
        inputField.setBorder(BorderFactory.createEmptyBorder());
        chatPanel.add(inputField);

        // 전송 버튼
        JButton sendButton = new JButton("");
        sendButton.setBounds(404, 95, 68, 30);
        sendButton.setContentAreaFilled(false);
        sendButton.setBorderPainted(false);
        sendButton.setFocusPainted(false);
        sendButton.setOpaque(false);
        chatPanel.add(sendButton);

        // 전송 버튼 이벤트 리스너
        sendButton.addActionListener(e -> {

            String text = inputField.getText().trim();
            if (!text.isEmpty()) {
                
                // 서버로 메시지 전송
                this.parentFrame.getClient().sendMessage(text);

                // 입력 필드 초기화
                inputField.setText("");
                inputField.requestFocus();
            }
        });

        // 배경 이미지 로드
        try {
            backgroundImage = ImageIO.read(getClass().getResource("/Images/lobbyScreen.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void appendMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            textArea.append(message + "\n");
            textArea.setCaretPosition(textArea.getDocument().getLength()); // 자동 스크롤
        });
    }

    // 유저 목록을 업데이트하는 메서드
    public void updateUserList(String[] users) {
        SwingUtilities.invokeLater(() -> {
            userListPanel.removeAll(); // 기존 유저 목록 제거

            int panelHeight = userListPanel.getHeight() / 8;
            int panelWidth = userListPanel.getWidth();
            int verticalGap = 2;

            for (int i = 0; i < users.length; i++) {
                JPanel userPanel = new JPanel(null);
                userPanel.setOpaque(false);
                userPanel.setBounds(0, i * (panelHeight + verticalGap), panelWidth, panelHeight - verticalGap);

                JLabel userLabel = new JLabel(users[i]);
                userLabel.setForeground(Color.BLACK);
                userLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
                userLabel.setBounds(5, 5, panelWidth - 40, panelHeight - 15);
                userPanel.add(userLabel);

                userListPanel.add(userPanel);
            }

            userListPanel.revalidate();
            userListPanel.repaint();
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }


}
