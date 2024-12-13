package Game;

import javax.swing.*;
import java.awt.*;

public class CreateRoomPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private JTextField roomTitleField;
    private JCheckBox privateRoomCheckBox;
    private JPasswordField passwordField;
    private JButton createButton;
    private JButton cancelButton;

    public CreateRoomPanel(JFrame parentFrame) {
        setLayout(null);
        setBounds(200, 100, 560, 400); // 패널 크기
        setBackground(new Color(0, 0, 0, 230)); // 반투명 배경
        setOpaque(true);

        // 방 제목 라벨
        JLabel roomTitleLabel = new JLabel("방 제목");
        roomTitleLabel.setForeground(Color.WHITE);
        roomTitleLabel.setBounds(80, 50, 90, 30);
        add(roomTitleLabel);

        // 방 제목 입력 필드
        roomTitleField = new JTextField();
        roomTitleField.setBounds(179, 51, 200, 30);
        add(roomTitleField);

        // 비밀방 체크박스
        privateRoomCheckBox = new JCheckBox("비밀방");
        privateRoomCheckBox.setForeground(Color.WHITE);
        privateRoomCheckBox.setOpaque(false);
        privateRoomCheckBox.setBounds(68, 149, 90, 30);
        add(privateRoomCheckBox);

        // 비밀번호 입력 필드
        passwordField = new JPasswordField();
        passwordField.setBounds(179, 150, 200, 30);
        passwordField.setEnabled(false); // 초기에는 비활성화
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
            boolean isPrivate = privateRoomCheckBox.isSelected();
            String password = new String(passwordField.getPassword()).trim();

            if (roomTitle.isEmpty()) {
                JOptionPane.showMessageDialog(this, "방 제목을 입력해주세요.", "경고!", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (isPrivate && (password.length() != 4 || !password.matches("\\d{4}"))) {
                JOptionPane.showMessageDialog(this, "비밀번호는 숫자 4자리여야 합니다.", "경고!", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 방 생성 로직 추가
            System.out.println("방 제목: " + roomTitle);
            if (isPrivate) {
                System.out.println("비밀방 설정됨, 비밀번호: " + password);
            } else {
                System.out.println("공개방 생성됨");
            }

            // 패널 닫기
            parentFrame.getLayeredPane().remove(this);
            parentFrame.getLayeredPane().revalidate();
            parentFrame.getLayeredPane().repaint();
        });
        add(createButton);

        // 취소 버튼
        cancelButton = new JButton("취소");
        cancelButton.setBounds(305, 300, 100, 40);
        cancelButton.addActionListener(e -> {
            // 패널을 부모 레이어드 팬에서 제거
            parentFrame.getLayeredPane().remove(this);
            parentFrame.getLayeredPane().revalidate();
            parentFrame.getLayeredPane().repaint();
        });
        add(cancelButton);
    }
}
