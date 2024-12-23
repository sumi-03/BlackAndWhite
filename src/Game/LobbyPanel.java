package Game;

import Dto.MessageType;
import Dto.ObjectDto;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class LobbyPanel extends JPanel {
    private Image backgroundImage;
    private MusicPlayer musicPlayer;
    private JPanel userListPanel;
    private JTextArea textArea;
    private JPanel roomListPanel;
    private GameFrame parentFrame;
    private JPanel[] roomPanels;

    public LobbyPanel(GameFrame parentFrame, MusicPlayer musicPlayer, String playerName) {
        this.parentFrame = parentFrame; // parentFrame 초기화
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
        userListPanel.setBounds(40, 196, 128, 280); // 이 크기를 확인해 보세요
        userListPanel.setOpaque(false);
        userListPanel.setBackground(new Color(255, 0, 0, 50)); // 배경색을 임시로 설정해 위치를 확인
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
            AudioPlayer.playSound(AudioPlayer.SoundPaths.ENTER_LOBBY);
            CreateRoomPanel createRoomPanel = new CreateRoomPanel(parentFrame);
            parentFrame.getLayeredPane().add(createRoomPanel, JLayeredPane.POPUP_LAYER);
            parentFrame.getLayeredPane().revalidate();
            parentFrame.getLayeredPane().repaint();
        });
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
                AudioPlayer.playSound(AudioPlayer.SoundPaths.ENTER_LOBBY);
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
            AudioPlayer.playSound(AudioPlayer.SoundPaths.ENTER_LOBBY);
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
        scrollPane.setBounds(80, 5, 396, 88);
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
        inputField.addActionListener(e -> {
            AudioPlayer.playSound(AudioPlayer.SoundPaths.CHAT);
            sendButton.doClick();
        });

        // 전송 버튼 이벤트 리스너
        sendButton.addActionListener(e -> {

            String text = inputField.getText().trim();
            if (!text.isEmpty()) {
                AudioPlayer.playSound(AudioPlayer.SoundPaths.CHAT);
                // 메시지 전송
                this.parentFrame.getClient().sendObjectMessage(new ObjectDto(MessageType.STRING_MESSAGE, text));
                // 입력 필드 초기화
                inputField.setText("");
                inputField.requestFocus();
            }
        });

        chatPanel.add(sendButton);


        // 방 목록 패널
        roomListPanel = new JPanel();
        roomListPanel.setBounds(375, 55, 309, 299);
        roomListPanel.setOpaque(false);
        roomListPanel.setLayout(null);
        add(roomListPanel);

        // 6개의 방 패널을 미리 생성하고 배열에 넣기
        roomPanels = new JPanel[6];

        for (int i = 0; i < roomPanels.length; i++) {
            roomPanels[i] = new JPanel();
            roomPanels[i].setLayout(null);
            roomPanels[i].setOpaque(false);
            roomPanels[i].setVisible(false);
            roomListPanel.add(roomPanels[i]);
        }

        // 위치 수동 설정
        roomPanels[0].setBounds(11, 8, 123, 71);
        roomPanels[1].setBounds(172, 8, 123, 71);
        roomPanels[2].setBounds(11, 114, 123, 71);
        roomPanels[3].setBounds(172, 114, 123, 71);
        roomPanels[4].setBounds(11, 218, 123, 71);
        roomPanels[5].setBounds(172, 218, 123, 71);

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
                if (!users[i].isEmpty()) {
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
            }

            userListPanel.revalidate();
            userListPanel.repaint();
        });
    }

    // 방 목록을 업데이트하는 메서드
    public void updateRoomList(List<RoomInfo> rooms) {
        SwingUtilities.invokeLater(() -> {
            if (roomPanels == null) {
                System.out.println("Error: roomPanels is not initialized.");
                return;
            }

            // 모든 방 패널 초기화
            for (JPanel panel : roomPanels) {
                panel.removeAll();
                panel.setVisible(false);
            }

            // 최대 6개의 방을 순서대로 표시
            for (int i = 0; i < Math.min(rooms.size(), roomPanels.length); i++) {
                RoomInfo room = rooms.get(i);
                JPanel panel = roomPanels[i];

                // 방 제목 라벨
                JLabel titleLabel = new JLabel("<< " + room.getRoomTitle() + " >>");
                titleLabel.setForeground(Color.WHITE);
                titleLabel.setBounds(10, 0, 200, 20);
                panel.add(titleLabel);

                // 방장 라벨
                JLabel hostLabel = new JLabel("방장: " + room.getHostName());
                hostLabel.setForeground(Color.WHITE);
                hostLabel.setBounds(10, 20, 200, 20);
                panel.add(hostLabel);

                // 상태 라벨
                JLabel statusLabel = new JLabel("상태: " + room.getStatus());
                statusLabel.setForeground(Color.WHITE);
                statusLabel.setBounds(10, 40, 200, 20);
                panel.add(statusLabel);

                // 비밀방 여부 라벨
                String privacyText = room.getIsPrivate().equals("Y") ? "비밀방" : "공개방";
                JLabel privacyLabel = new JLabel("공개여부: " + privacyText);
                privacyLabel.setForeground(Color.WHITE);
                privacyLabel.setBounds(10, 55, 200, 20);
                panel.add(privacyLabel);


                // 방 패널 클릭 시 입장 기능
                panel.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        if (room.getIsPrivate().equals("Y")) { //만약 비밀방이면
                            // 비밀번호 입력 패널
                            JPasswordField pwdField = new JPasswordField(10);
                            JOptionPane optionPane = new JOptionPane(pwdField, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);

                            // 다이얼로그 생성
                            JDialog dialog = optionPane.createDialog(parentFrame, "비밀번호 입력:");
                            dialog.setLocationRelativeTo(parentFrame);
                            dialog.setVisible(true);

                            Object selectedValue = optionPane.getValue();
                            if (selectedValue != null && (int) selectedValue == JOptionPane.OK_OPTION) {
                                String passwordEntered = new String(pwdField.getPassword());
                                // 비밀번호 검사
                                if (passwordEntered.equals(room.getPassword())) {
                                    // 비밀번호 맞을 경우 입장
                                    parentFrame.sendObjectMessage(
                                            new ObjectDto(MessageType.JOIN_ROOM, room.getRoomTitle()));
                                } else {
                                    // 비밀번호 틀릴 경우 오류 출력
                                    JOptionPane.showMessageDialog(parentFrame, "비밀번호가 틀렸습니다.", "Error", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        } else {
                            parentFrame.sendObjectMessage(
                                    new ObjectDto(MessageType.JOIN_ROOM, room.getRoomTitle()));
                        }
                    }
                });

                panel.setVisible(true);
            }

            roomListPanel.revalidate();
            roomListPanel.repaint();
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
