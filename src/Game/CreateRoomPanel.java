package Game;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class CreateRoomPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private JTextField roomTitleField;
    private JCheckBox privateRoomCheckBox;
    private JPasswordField passwordField;
    private JButton createButton;
    private JButton cancelButton;

    public CreateRoomPanel(GameFrame parentFrame) {
        setLayout(null);
        setBounds(200, 100, 560, 400);
        setBackground(new Color(0, 0, 0, 230));
        setOpaque(true);

        // 방 제목 라벨
        JLabel roomTitleLabel = new JLabel("방 제목");
        roomTitleLabel.setForeground(Color.WHITE);
        roomTitleLabel.setBounds(50, 50, 150, 30);
        add(roomTitleLabel);

        // 방 제목 입력 필드
        roomTitleField = new JTextField();
        roomTitleField.setBounds(155, 50, 300, 30);
        add(roomTitleField);

        // 비밀방 체크박스
        privateRoomCheckBox = new JCheckBox("비밀방 설정");
        privateRoomCheckBox.setForeground(Color.WHITE);
        privateRoomCheckBox.setOpaque(false);
        privateRoomCheckBox.setBounds(50, 100, 150, 30);
        add(privateRoomCheckBox);

        // 비밀번호 입력 필드
        passwordField = new JPasswordField();
        passwordField.setBounds(155, 150, 150, 30);
        passwordField.setEnabled(false);
        add(passwordField);

        // 비밀방 체크박스 이벤트 리스너
        privateRoomCheckBox.addActionListener(e -> {
            passwordField.setEnabled(privateRoomCheckBox.isSelected());
        });

        // 입장 버튼
        createButton = new JButton("입장");
        createButton.setBounds(155, 300, 100, 40);
        createButton.addActionListener(e -> {
            String roomTitle = roomTitleField.getText().trim();

            if (!roomTitle.isEmpty()) {
                // 방 생성 요청 전송
                parentFrame.getClient().sendMessage("CREATE_ROOM:" + roomTitle);

                // 서버 응답 대기 및 처리
                new Thread(() -> {
                    try {
                        String msgFromServer;
                        while (true) {
                            msgFromServer = parentFrame.getClient().getBufferedReader().readLine();
                            if (msgFromServer != null && msgFromServer.startsWith("CREATE_ROOM_COMPLETED:")) {
                                String receivedRoomTitle = msgFromServer.substring(22).trim();

                                if (receivedRoomTitle.equals(roomTitle)) {
                                    SwingUtilities.invokeLater(() -> {
                                        RoomInfo roomInfo = new RoomInfo(roomTitle, parentFrame.getPlayerName(), "대기중...");
                                        parentFrame.showWaitingRoomPanel(
                                                roomInfo,
                                                privateRoomCheckBox.isSelected(),
                                                true,
                                                new String(passwordField.getPassword())
                                        );
                                        parentFrame.getLayeredPane().remove(this);
                                        parentFrame.getLayeredPane().revalidate();
                                        parentFrame.getLayeredPane().repaint();
                                    });
                                    break; // 메시지 처리가 완료되면 루프 종료
                                }
                            }
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }).start();
            } else {
                JOptionPane.showMessageDialog(this, "방 제목을 입력해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
            }
        });



        add(createButton);

        // 취소 버튼
        cancelButton = new JButton("취소");
        cancelButton.setBounds(305, 300, 100, 40);
        cancelButton.addActionListener(e -> {
            parentFrame.getLayeredPane().remove(this);
            parentFrame.getLayeredPane().revalidate();
            parentFrame.getLayeredPane().repaint();
        });
        add(cancelButton);
    }
}
