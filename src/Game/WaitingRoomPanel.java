package Game;

import Dto.MessageType;
import Dto.ObjectDto;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public class WaitingRoomPanel extends JPanel implements TimerListener{
    private static final long serialVersionUID = 1L;
    private JLabel hostNameLabel;
    private JLabel opponentNameLabel;
    private JLabel roomTitleLabel;
    private JLabel countdownLabel;
    private JLabel privacyLabel;
    private Image backgroundImage;
    private GameFrame parentFrame;
    private String host = "";
    private String opponent= "";
    private boolean countdownStarted = false;
    private boolean isHost;
    private JButton backButton;

    private boolean isPrivate = false;
    private String roomTitle = "";
    private String password = "";

    //room 객체에서 정보 가져오도록 수정
    public WaitingRoomPanel(RoomInfo room,	GameFrame parentFrame, String playerName) {
        this.isHost = room.getHostName().equals(playerName);
        this.parentFrame = parentFrame;

        this.roomTitle = room.getRoomTitle();

        this.isPrivate = room.getIsPrivate().equals("Y");  //
        if (isPrivate) {
            this.password = room.getPassword();
        }
        this.host = room.getHostName();
        this.opponent = (room.getOpponentName() == null) ? "???" : room.getOpponentName();

        setLayout(null);
        setPreferredSize(new Dimension(960, 600));

        loadFrame();
        //타이머 리스너
        this.parentFrame.setTimerListener(this);
    }

    private void loadFrame() {

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

        // 게임 시작까지 남은 시간 라벨
        countdownLabel = new JLabel("상대방이 아직 입장하지 않았습니다", SwingConstants.CENTER);
        countdownLabel.setForeground(Color.WHITE);
        countdownLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 13));
        countdownLabel.setBounds(0, 60, 960, 30);
        add(countdownLabel);

        // 비밀방 여부 라벨 (상단 중앙)
        String privacyText = isPrivate ? "비밀방 (비밀번호: " + password + ")" : "공개방";
        privacyLabel = new JLabel(privacyText, SwingConstants.CENTER);
        privacyLabel.setForeground(Color.WHITE);
        privacyLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
        privacyLabel.setBounds(0, 90, 960, 30);
        add(privacyLabel);

        // 방장 이름 라벨 (왼쪽 하단)
        hostNameLabel = new JLabel("RED 플레이어: " + host, SwingConstants.LEFT);
        hostNameLabel.setForeground(Color.WHITE);
        hostNameLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
        hostNameLabel.setBounds(30, 520, 300, 30);
        add(hostNameLabel);

        // 상대방 이름 라벨 (오른쪽 하단) - 초기에는 ???로 설정
        opponentNameLabel = new JLabel("BLUE 플레이어: " + opponent, SwingConstants.RIGHT);
        opponentNameLabel.setForeground(Color.WHITE);
        opponentNameLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
        opponentNameLabel.setBounds(630, 520, 300, 30);
        add(opponentNameLabel);

        // 뒤로 가기 버튼
        backButton = new JButton("뒤로가기");
        backButton.setBounds(820, 30, 100, 40);
        backButton.setFocusPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setBorderPainted(true);
        backButton.setForeground(Color.WHITE);
        backButton.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
        backButton.addActionListener(e -> {
            parentFrame.sendObjectMessage(new ObjectDto(MessageType.DELETE_ROOM, roomTitle)); // 방 제거 요청
            parentFrame.showLobbyPanel(parentFrame.getPlayerName());
            parentFrame.requestUserList();
        });
        add(backButton);
    }

    // 상대방 이름 업데이트 메서드
    public void updateBluePlayer(String opponentName) {

        // 만약 backButton이 붙어있다면 제거
        if (backButton.getParent() != null) {
            this.remove(backButton);
        }

        Log("opponentName : " + opponentName);
        this.opponent = opponentName;
        opponentNameLabel.setText("BLUE 플레이어: " + opponentName);

        this.revalidate();
        this.repaint();
    }

    private void Log(String message) {
        System.out.println("Log Client WaitingRoomPanel >> " + message);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    @Override
    public String changeTimer(String timerString) {

        // 만약 backButton이 붙어있다면 제거
        if (backButton.getParent() != null) {
            this.remove(backButton);
        }

        int timeLeft = Integer.parseInt(timerString);

        if (timeLeft >= 1 && timeLeft <= 5) {
        
            countdownLabel.setText("게임 시작까지: " + timeLeft + "초");
    
        } else if (timeLeft == 0) {
        
            this.remove(countdownLabel); 
        }

    
        return null;
    }
}
