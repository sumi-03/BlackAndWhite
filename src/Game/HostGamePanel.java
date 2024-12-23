package Game;

import Dto.CardSubmitDto;
import Dto.MessageType;
import Dto.ObjectDto;
import Dto.TutorialClosedDto;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;

public class HostGamePanel extends JPanel implements CardSubmitListener {
    private final MusicPlayer musicPlayer;
    private final String currentRoomTitle;

    private List<Card> playerCards; // 내 카드 리스트
    private List<Card> opponentCards; // 상대 카드 리스트
    private int playerWins; // 내 점수
    private int opponentWins; // 상대 점수
    private int roundCount; // 현재 라운드 수
    private Image backgroundImage; // 게임 배경 이미지
    private String playerName; // 현재 플레이어 이름
    private String redPlayerName; // RED 플레이어 이름
    private String bluePlayerName; // BLUE 플레이어 이름
    private GameFrame parentFrame; // 부모 프레임
    private boolean isInitialized = false;
    private TutorialDialog tutorialDialog;
    private AudioPlayer audioPlayer;

    private boolean isCardClickable = true; //클릭 가능 여부


    // 제출된 카드(중앙)에 표시할 배열 (9라운드, 양쪽 플레이어)
    private Card[] submittedHostCards;
    private Card[] submittedOpponentCards;
    // 라운드별 위치
    private Point[] centerSlotsHost;       // 호스트 카드 좌표
    private Point[] centerSlotsOpponent;   // 상대 카드 좌표

    public HostGamePanel(GameFrame parentFrame, String playerName, MusicPlayer musicPlayer, RoomInfo room)
            throws IOException {
        this.playerName = playerName;
        this.parentFrame = parentFrame;
        this.currentRoomTitle = room.getRoomTitle();
        this.musicPlayer = musicPlayer;
        this.audioPlayer = new AudioPlayer();

        // 카드 제출 이벤트 등록
        parentFrame.setCardSubmitListener(this);

        // 방장 이름 및 상대방 이름 설정
        this.redPlayerName = room.getHostName();
        this.bluePlayerName = room.getOpponentName();

        // 배경 이미지 설정
        backgroundImage = ImageIO.read(new File("src/images/gameScreenRED.png"));

        // 음량 설정 버튼
        JButton settingsButton = new JButton("");
        settingsButton.setBounds(20, 13, 50, 50);
        settingsButton.setContentAreaFilled(false);
        settingsButton.setBorderPainted(false);
        settingsButton.setFocusPainted(false);
        settingsButton.setOpaque(false);

        // 음량 설정 버튼 클릭 이벤트
        settingsButton.addActionListener(e -> {
            audioPlayer.playSound("/Sounds/enter_lobby.wav");
            SettingsPanel settingsPanel = new SettingsPanel((JFrame) parentFrame, musicPlayer);
            parentFrame.getLayeredPane().add(settingsPanel, JLayeredPane.POPUP_LAYER);
            parentFrame.getLayeredPane().revalidate();
            parentFrame.getLayeredPane().repaint();
        });

        add(settingsButton);

        initializeCards(); // 카드 초기화
        setLayout(null);
        setPreferredSize(new Dimension(960, 600));

        this.isInitialized = true;

        roundCount = 1; // 초기 라운드 번호
        playerWins = 0; // 초기 점수
        opponentWins = 0; // 초기 점수


        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleCardSelection(e.getX(), e.getY());
            }
        });

        repaint();

        SwingUtilities.invokeLater(() -> showTutorialDialog()); // 룰설명
    }

    private void Log(String message) {
        System.out.println("Log Client HostGamePanel >> " + message);
    }

    private void showTutorialDialog() {
        tutorialDialog = new TutorialDialog((Frame) parentFrame, this);
        tutorialDialog.setLocationRelativeTo(parentFrame);
        tutorialDialog.setVisible(true);
    }

    void closeTutorialDialog() {
        // 서버로 TUTORIAL_CLOSED 전송
        TutorialClosedDto dto = new TutorialClosedDto();
        dto.setRoomTitle(this.currentRoomTitle);
        dto.setPlayerName(this.playerName);

        parentFrame.sendObjectMessage(
                new ObjectDto(MessageType.TUTORIAL_CLOSED, dto)
        );
    }

    public void startRealGame() {
        System.out.println("Game Start!");
        this.isCardClickable = true;
    }

    public class AudioPlayer {

        public List<String> getAllFilesInResourceDirectory(String resourceDir) {
            try {
                // 디렉토리 경로를 URL로 가져옴
                URL dirURL = getClass().getResource(resourceDir);

                if (dirURL == null) {
                    throw new FileNotFoundException("Resource directory not found: " + resourceDir);
                }

                if (dirURL.getProtocol().equals("jar")) {
                    // JAR 내부에서 파일 읽기
                    String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!"));
                    try (JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"))) {
                        Enumeration<JarEntry> entries = jar.entries();
                        List<String> files = new ArrayList<>();

                        while (entries.hasMoreElements()) {
                            String name = entries.nextElement().getName();
                            if (name.startsWith(resourceDir.substring(1)) && name.endsWith(".wav")) {
                                files.add("/" + name); // 리소스 경로 형식으로 추가
                            }
                        }
                        return files;
                    }
                } else {
                    // IDE 환경에서 디렉토리 파일 탐색
                    Path dirPath = Paths.get(dirURL.toURI());
                    try (Stream<Path> paths = Files.walk(dirPath, 1)) {
                        return paths
                                .filter(Files::isRegularFile)
                                .map(path -> resourceDir + "/" + path.getFileName().toString())
                                .collect(Collectors.toList());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        }

        public void playAllSoundsInDirectory(String resourceDir) {
            List<String> files = getAllFilesInResourceDirectory(resourceDir);

            for (String file : files) {
                System.out.println("Playing: " + file);
                playSound(file);
            }
        }

        public void playSound(String resourcePath) {
            try (InputStream resourceStream = getClass().getResourceAsStream(resourcePath)) {
                if (resourceStream == null) {
                    throw new FileNotFoundException("Resource not found: " + resourcePath);
                }

                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(resourceStream);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.start();

                // 오디오 재생 완료 대기
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });

                while (clip.isRunning()) {
                    Thread.sleep(100);
                }
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void main(String[] args) {
            AudioPlayer audioPlayer = new AudioPlayer();
            audioPlayer.playAllSoundsInDirectory("/Voices");
        }
    }


    // 카드 초기화
    private void initializeCards() {
        playerCards = new ArrayList<>();
        opponentCards = new ArrayList<>();

        for (int i = 0; i <= 8; i++) {
            playerCards.add(new Card(i));
            opponentCards.add(new Card(i));
        }
        initCenterSlots();
        // 카드 섞기
        Collections.shuffle(playerCards);
        Collections.shuffle(opponentCards);
    }

    private void initCenterSlots() {
        submittedHostCards = new Card[9];
        submittedOpponentCards = new Card[9];

        centerSlotsHost = new Point[9];
        centerSlotsOpponent = new Point[9];

        // 호스트 카드: 1줄, Y=240, X=200 + i*70
        // 상대 카드:   그 위나 아래에 1줄, Y=340, X=200 + i*70
        int baseX = 150, baseY_Host = 340, baseY_Oppo = 210;
        int gap = 80;

        for (int i = 0; i < 9; i++) {
            centerSlotsHost[i] = new Point(baseX + i * gap, baseY_Host);
            centerSlotsOpponent[i] = new Point(baseX + i * gap, baseY_Oppo);
        }
    }


    public void onRoundFinished(String nextFirstPlayer) {
        // 다음 라운드 선공'이면 내 패널에서 카드 클릭 가능
        if (this.playerName.equals(nextFirstPlayer)) {
            this.isCardClickable = true;
        } else {
            this.isCardClickable = false;
        }
    }


    // 카드 제출 및 서버로 전송
    private void submitCard(int cardIndex) {
        Card playerCard = playerCards.get(cardIndex);

        if (playerCard != null) {
            // 서버 전송
            CardSubmitDto dto = new CardSubmitDto();
            dto.setCardNum(playerCard.getNumber());
            dto.setRoomTitle(this.currentRoomTitle);
            dto.setSubmitName(playerName);
            parentFrame.sendObjectMessage(new ObjectDto(MessageType.SUBMIT_CARD, dto));

            // ★ 중앙 표시
            int roundIdx = roundCount - 1; // 혹은 currentRoundIndex
            submittedHostCards[roundIdx] = playerCard;

            // 손패에서 제거
            playerCards.set(cardIndex, null);
            repaint();
        }
    }

    public void updateOpponentCard(int cardNum, String submitName) {
        // 1) "누가 냈는지" 판별: Host=redPlayerName, Opponent=bluePlayerName
        boolean isHost = submitName.equals(redPlayerName);

        // 2) 라운드 인덱스
        int roundIdx = roundCount - 1;

        // 3) 만약 "상대(blue)가 낸 카드"라면 -> 내 화면에서 그 상대 손패(opponentCards)를 제거
        if (!isHost) {
            // 상대(blue) 낸 카드 => topRow(opponentCards)에서 제거
            removeCardFromOpponentCards(cardNum);
            submittedOpponentCards[roundIdx] = new Card(cardNum);
        } else {
            submittedHostCards[roundIdx] = new Card(cardNum);
        }

        repaint();
    }

    private void removeCardFromOpponentCards(int cardNum) {
        for (int i = 0; i < opponentCards.size(); i++) {
            Card c = opponentCards.get(i);
            if (c != null && c.getNumber() == cardNum) {
                opponentCards.set(i, null);
                break;
            }
        }
    }

    // 라운드 결과를 처리
    public void handleRoundResult(String result) {
        new Thread(() -> {
            try {
                Thread.sleep(500);

                if (result.startsWith("WINNER:")) {
                    String winnerName = result.substring("WINNER:".length()).trim();
                    SwingUtilities.invokeLater(() -> {
                        if (winnerName.equals(playerName)) {
                            audioPlayer.playSound(playerName.equals(redPlayerName) ? "/voices/red_win.wav" : "/voices/blue_win.wav");
                        } else {
                            audioPlayer.playSound(playerName.equals(redPlayerName) ? "/voices/blue_win.wav" : "/voices/red_win.wav");
                        }
                    });
                } else if (result.equals("draw!")) {
                    SwingUtilities.invokeLater(() -> {
                        audioPlayer.playSound("/voices/draw.wav");
                    });
                }
                checkFinalWinner();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // 최종승리자, 라운드 업데이트
    private void checkFinalWinner() throws InterruptedException {
        if (playerWins >= 5 || opponentWins >= 5) {
            Thread.sleep(5500); // 최종 승리 음성 재생 전 대기
            SwingUtilities.invokeLater(() -> {
                String finalWinSound = playerWins >= 5 ? "/voices/final_win_blue.wav" : "/voices/final_win_red.wav";
                audioPlayer.playSound(finalWinSound);
            });
        } else {
            Thread.sleep(5500); // 다음 라운드 음성 재생 전 대기
            roundCount++;
            SwingUtilities.invokeLater(() -> {
                repaint(); // UI 갱신
                playRoundStartSound(); // 라운드 시작 사운드 재생
            });
        }
    }

    // 라운드 시작 사운드 재생
    private void playRoundStartSound() {
        String soundPath = "/voices/" + roundCount + "round.wav";
        audioPlayer.playSound(soundPath);
    }

    public void updateScoresFromMessage(String scoreMessage) {
        Log("Updating scores: " + scoreMessage);

        String[] scores = scoreMessage.replace("Score - ", "").split(" \\| ");
        for (String score : scores) {
            String[] parts = score.split(": ");
            if (parts.length == 2) {
                if (parts[0].trim().equals(redPlayerName)) {
                    opponentWins = Integer.parseInt(parts[1].trim());
                } else if (parts[0].trim().equals(bluePlayerName)) {
                    playerWins = Integer.parseInt(parts[1].trim());
                }
            }
        }

        repaint(); // 화면 갱신
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 배경 그리기
        drawBackground(g);

        // 카드 그리기
        drawCards(g);

        // 이름 그리기
        drawName(g);

        // 라운드 및 점수 표시
        drawGameInfo(g);


        drawCenterCards(g);
    }

    private void drawBackground(Graphics g) {
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    private void drawCards(Graphics g) {
        // 내 카드 그리기 (앞면)
        for (int i = 0; i < playerCards.size(); i++) {
            if (playerCards.get(i) != null) {
                playerCards.get(i).draw(g, 150 + i * 80, 450, true); // 앞면으로 그리기
            }
        }

        // 상대방 카드 그리기 (뒷면)
        for (int i = 0; i < opponentCards.size(); i++) {
            if (opponentCards.get(i) != null) {
                opponentCards.get(i).draw(g, 150 + i * 80, 80, false); // 뒷면으로 그리기
            }
        }
    }

    private void drawCenterCards(Graphics g) {
        for (int i = 0; i < 9; i++) {
            // Host 카드
            if (submittedHostCards[i] != null) {
                Point p = centerSlotsHost[i];
                // "뒷면"으로 표시하고 싶다면 false
                submittedHostCards[i].draw(g, p.x, p.y, true);
            }

            // Opponent 카드
            if (submittedOpponentCards[i] != null) {
                Point p = centerSlotsOpponent[i];
                submittedOpponentCards[i].draw(g, p.x, p.y, false);
            }
        }
    }

    private void drawName(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Malgun Gothic", Font.BOLD, 20));

        // 1) Red 이름(왼쪽 고정)
        g.drawString(redPlayerName + " (RED)", 150, 40);
        int redTextWidth = g.getFontMetrics().stringWidth(redPlayerName + " (RED)");

        // 2) Blue 이름(오른쪽 고정)
        int blueTextWidth = g.getFontMetrics().stringWidth(bluePlayerName + " (BLUE)");
        int blueX = getWidth() - blueTextWidth - 170;
        g.drawString(bluePlayerName + " (BLUE)", blueX, 40);

        // 'isCardClickable=true' → "내 턴" (나는 Red)
        //                         → 빨간 이름 옆에 원
        // 'isCardClickable=false'→ "상대 턴" → 파란 이름 옆에 원

    }

    private void drawGameInfo(Graphics g) {
        g.setColor(Color.WHITE); // 텍스트 색상
        g.setFont(new Font("Arial", Font.BOLD, 16)); // 텍스트 폰트

        // 라운드 텍스트
        String roundText = "Round: " + roundCount;

        // 라운드 텍스트 너비 계산
        int roundTextWidth = g.getFontMetrics().stringWidth(roundText);

        // 중앙 정렬 계산 (화면의 정중앙에서 텍스트 시작 X 좌표를 구함)
        int centerX = getWidth() / 2; // 화면 정중앙
        int roundTextX = centerX - (roundTextWidth / 2); // 라운드 텍스트 시작 X 좌표
        int roundY = 30; // 라운드 텍스트 Y 좌표

        // 원의 X 좌표 계산 (라운드 텍스트의 양옆에 위치)
        int padding = 10; // 공백
        int circleDiameter = 15; // 원의 지름
        int leftCircleX = roundTextX - circleDiameter - padding; // 왼쪽 원 X 좌표
        int rightCircleX = roundTextX + roundTextWidth + padding; // 오른쪽 원 X 좌표
        int circleY = roundY - 12; // 원의 Y 좌표 (텍스트 높이에 맞춤)

        // 라운드 텍스트 그리기
        g.drawString(roundText, roundTextX, roundY);


        if (isCardClickable) {
            // 내 턴 ⇒ 원을 Red 이름 끝 옆에
            g.setColor(Color.RED);
            g.fillOval(leftCircleX, circleY, circleDiameter, circleDiameter);
        } else {
            // 상대 턴 ⇒ Blue 이름 끝 옆
            g.setColor(Color.BLUE);
            g.fillOval(rightCircleX, circleY, circleDiameter, circleDiameter);
        }

        // 점수 표시 (RED : BLUE)
        String scoreText = opponentWins + " : " + playerWins;
        int scoreTextWidth = g.getFontMetrics().stringWidth(scoreText);
        int scoreX = centerX - (scoreTextWidth / 2); // 점수를 중앙에 정렬
        g.setColor(Color.WHITE); // 텍스트 색상
        g.drawString(scoreText, scoreX, 55); // 점수 표시
    }

    // 카드 제출 처리
    private void handleCardSelection(int x, int y) {
        if (!isCardClickable) {
            Log("이미 제출함");
            return;
        }

        for (int i = 0; i < playerCards.size(); i++) {
            int cardX = 170 + i * 80;
            int cardY = 470;

            if (x >= cardX && x <= cardX + 60 && y >= cardY && y <= cardY + 90) {
                audioPlayer.playSound("/Sounds/cardclick.wav");
                submitCard(i);
                break;
            }
        }
    }

    @Override
    public String cardSubmitted(String submitName) {
        Log("HostPanel cardSubmitted >> submitName=" + submitName
                + ", myName=" + playerName);
        boolean wasClickable = this.isCardClickable;

        if (submitName.equals(playerName)) {
            this.isCardClickable = false; // 내가 냈음 -> 내 턴 종료
            // audioPlayer.playSound("/voices/blueturn.wav");
        } else {
            this.isCardClickable = true; // 상대가 냈음 -> 내 턴 시작
            //audioPlayer.playSound("/voices/redturn.wav");
        }
        return null;
    }

}
