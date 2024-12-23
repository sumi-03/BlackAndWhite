package Game;

import Dto.MessageType;
import Dto.ObjectDto;

import javax.swing.*;
import java.awt.*;

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
        privateRoomCheckBox.setBounds(50, 150, 150, 30);
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
            AudioPlayer.playSound(AudioPlayer.SoundPaths.CREATE_ROOM);
            String roomTitle = roomTitleField.getText().trim();
            boolean isPrivateRoom = privateRoomCheckBox.isSelected();
            String password = new String(passwordField.getPassword());

            if (!roomTitle.isEmpty()) {
                String privacyStatus = isPrivateRoom ? "Y" : "N";
                RoomInfo roomInfo = new RoomInfo(roomTitle, parentFrame.getPlayerName(), "대기중", password, privacyStatus);

                // 서버로 방 생성 요청 전송
                parentFrame.getClient().sendObjectMessage(

                        new ObjectDto(MessageType.CREATE_ROOM, roomInfo)
                );
                parentFrame.getLayeredPane().remove(this);
                parentFrame.getLayeredPane().revalidate();
                parentFrame.getLayeredPane().repaint();
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
