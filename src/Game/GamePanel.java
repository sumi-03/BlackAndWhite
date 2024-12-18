package Game;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GamePanel extends JPanel {
    private final MusicPlayer musicPlayer;
    private final String currentRoomTitle;

    private List<Card> playerCards;       // 내 카드 리스트
    private List<Card> opponentCards;     // 상대 카드 리스트
    private int playerWins;
    private int opponentWins;
    private int roundCount;               // 현재 라운드 수
    private Image backgroundImage; // 게임 배경 이미지
    private String playerName; // 현재 플레이어 이름
    private String redPlayerName; // RED 플레이어 이름
    private String bluePlayerName; // BLUE 플레이어 이름
    private GameFrame parentFrame; // 부모 프레임
    private boolean isInitialized = false;




    public GamePanel(GameFrame parentFrame, String playerName, MusicPlayer musicPlayer, boolean isHost, String opponentName, String currentRoomTitle)
            throws IOException {
        this.playerName = playerName;
        this.parentFrame = parentFrame;
        this.currentRoomTitle = currentRoomTitle;
        this.musicPlayer = musicPlayer;

        this.redPlayerName = isHost ? playerName : opponentName;
        this.bluePlayerName = isHost ? opponentName : playerName;

        System.out.println("RED: " + redPlayerName);
        System.out.println("BLUE: " + bluePlayerName);

        backgroundImage = ImageIO.read(new File(isHost ? "src/images/gameScreenRED.png" : "src/images/gameScreenBLUE.png"));

        initializeCards();
        setLayout(null);
        setPreferredSize(new Dimension(960, 600));

        this.isInitialized = true;

        roundCount = 0;
        playerWins = 0;
        opponentWins = 0;

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleCardSelection(e.getX(), e.getY());
            }
        });

        repaint();
    }

    // 카드 초기화
    private void initializeCards() {
        playerCards = new ArrayList<>();
        opponentCards = new ArrayList<>();

        for (int i = 0; i <= 8; i++) {
            playerCards.add(new Card(i));
            opponentCards.add(new Card(i));
        }

        // 카드 섞기 (랜덤으로 배치)
        Collections.shuffle(playerCards);
        Collections.shuffle(opponentCards);
    }


    // 카드 제출 및 서버로 전송
    private void submitCard(int cardIndex) {
        Card playerCard = playerCards.get(cardIndex);

        if (playerCard != null) {
            // 서버에 카드 제출 메시지 전송
            parentFrame.sendMessage("SUBMIT_CARD:" + playerCard.getNumber());
            playerCards.set(cardIndex, null);
            repaint();
        }
    }


    // 상대방이 제출한 카드 삭제
    public void updateOpponentCard(int cardNumber) {
        System.out.println("Attempting to remove opponent card: " + cardNumber);
        System.out.print("Current opponent cards: ");
        for (Card card : opponentCards) {
            if (card != null) {
                System.out.print(card.getNumber() + " ");
            } else {
                System.out.print("null ");
            }
        }
        System.out.println();

        boolean cardRemoved = false;

        for (int i = 0; i < opponentCards.size(); i++) {
            if (opponentCards.get(i) != null && opponentCards.get(i).getNumber() == cardNumber) {
                System.out.println("Removing opponent card: " + cardNumber);
                opponentCards.set(i, null);  // 제출된 카드 삭제
                cardRemoved = true;
                repaint();
                break;
            }
        }
    }

    // 라운드 결과를 처리하는 메서드
    public void handleRoundResult(String result) {
        JOptionPane.showMessageDialog(this, result, "라운드 결과", JOptionPane.INFORMATION_MESSAGE);

        if (result.contains("WINNER:" + playerName)) {
            playerWins++;
        } else if (result.contains("WINNER:")) {
            opponentWins++;
        }

        roundCount++;
        repaint();

        if (playerWins >= 5) {
            JOptionPane.showMessageDialog(this, "축하합니다! 당신이 승리했습니다!", "게임 종료", JOptionPane.INFORMATION_MESSAGE);
            resetGame();
        } else if (opponentWins >= 5) {
            JOptionPane.showMessageDialog(this, "상대방이 승리했습니다!", "게임 종료", JOptionPane.INFORMATION_MESSAGE);
            resetGame();
        } else if (roundCount >= 9) {
            JOptionPane.showMessageDialog(this, "게임 종료!", "알림", JOptionPane.INFORMATION_MESSAGE);
            resetGame();
        }
    }

    // 게임 리셋
    private void resetGame() {
        initializeCards();
        roundCount = 0;
        playerWins = 0;
        opponentWins = 0;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBackground(g);
        drawCards(g);

        // 항상 이름 그리기
        drawName(g);
    }

    private void drawBackground(Graphics g) {
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    // 카드 선택 처리
    private void handleCardSelection(int x, int y) {
        for (int i = 0; i < playerCards.size(); i++) {
            int cardX = 170 + i * 80; // drawCards와 동일하게 조정
            int cardY = 470;

            if (x >= cardX && x <= cardX + 60 && y >= cardY && y <= cardY + 90) {
                submitCard(i);
                break;
            }
        }
    }

    // 카드 그리기
    private void drawCards(Graphics g) {
        // 내 카드 그리기 (앞면)
        for (int i = 0; i < playerCards.size(); i++) {
            if (playerCards.get(i) != null) {
                playerCards.get(i).draw(g, 170 + i * 80, 460, true); // 앞면으로 그리기
            }
        }

        // 상대방 카드 그리기 (뒷면)
        for (int i = 0; i < opponentCards.size(); i++) {
            if (opponentCards.get(i) != null) {
                opponentCards.get(i).draw(g, 170 + i * 80, 80, false); // 뒷면으로 그리기
            }
        }
    }

    private void drawName(Graphics g) {

        g.setColor(Color.WHITE); // 텍스트 색상 설정
        g.setFont(new Font("Malgun Gothic", Font.BOLD, 20)); // 텍스트 폰트 설정
        g.drawString(redPlayerName, 150, 40); // 좌측 상단에 상대방 이름 표시

        FontMetrics metrics = g.getFontMetrics(g.getFont());
        int textWidth = metrics.stringWidth(bluePlayerName);

        // 오른쪽 정렬을 위해 시작 위치 계산
        int x = getWidth() - textWidth - 170; // 우측 여백 20

        g.drawString(bluePlayerName, x, 40); // 좌측 상단에 상대방 이름 표시
    }

}
