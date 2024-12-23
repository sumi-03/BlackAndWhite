package Game;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


public class TutorialDialog extends JDialog {
    private Clip clip;
    private HostGamePanel hostPanel;      // 만약 Host 전용이라면
    private OpponentGamePanel oppPanel;   // Opponent 전용이라면

    private void playSound(String resourcePath) {
        try {
            // 리소스 경로에서 오디오 파일 읽기
            InputStream resourceStream = getClass().getResourceAsStream("/Voices/rule.wav");
            if (resourceStream == null) {
                throw new FileNotFoundException("Resource not found: " + "/Voices/rule.wav");
            }

            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(resourceStream);
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start(); // 오디오 재생

            // 오디오 종료까지 대기
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "오디오 파일을 재생할 수 없습니다: " + resourcePath, "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 음성 중지 메서드
    private void stopSound() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
        }
    }

    // (1) HostGamePanel 전용 생성자
    public TutorialDialog(Frame owner, HostGamePanel panel) {
        super(owner, "게임 룰", true);
        this.hostPanel = panel;
        initUI();

    }

    // (2) OpponentGamePanel 전용 생성자
    public TutorialDialog(Frame owner, OpponentGamePanel panel) {
        super(owner, "게임 룰", true);
        this.oppPanel = panel;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        JLabel ruleLabel = new JLabel("<html><div style='text-align: center;'>"+
                "플레이어 두명은 0~8까지 9장의 숫자카드를 지급 받는다.<br/><br/>" +
                "9장의 숫자카드는 흑색, 백색으로 나뉘며 0, 2, 4, 6, 8은 흑색, 1, 3, 5, 7은 백색 타일로 구성되어 있다.<br/><br/>" +
                "선플레이어가 0~8까지의 숫자카드 중 한개를 클릭하여 제출한 뒤, 후 플레이어가 카드를 제출한다.<br/><br/>" +
                "둘 중 더 높은 숫자카드를 제시한 플레이어가 승리, 승점을 획득한다.<br/><br/>" +
                "상대가 어떤 숫자카드를 냈는지는 승패가 결정된 후에도 공개되지 않는다.<br/><br/>" +
                "플레이어들은 자신이 낸 카드와 흑, 백으로 나뉜 카드로 상대방의 남은 카드를 유추해 게임을 해야한다.<br/><br/>" +
                "총 9번의 라운드 중, 5번의 승리를 먼저 하는 플레이어가 최종 승리한다.<br/><br/><br/>" +
                "<strong>***두 플레이어 모두 [닫기]버튼 누르면 바로 게임시작***</strong>" +
                "</div></html>");
        add(ruleLabel, BorderLayout.CENTER);
           playSound("/Voices/rule.wav");

        JButton closeButton = new JButton("닫기");
        closeButton.addActionListener(e -> {
            // 음성 중지
            stopSound();
            // (A) Host면 hostPanel.closeTutorialDialog()
            if (hostPanel != null) {
                hostPanel.closeTutorialDialog();
            }
            // (B) Opponent면 oppPanel.closeTutorialDialog()
            if (oppPanel != null) {
                oppPanel.closeTutorialDialog();
            }
            dispose();
        });
        add(closeButton, BorderLayout.SOUTH);

        setSize(560, 400);
    }
}
