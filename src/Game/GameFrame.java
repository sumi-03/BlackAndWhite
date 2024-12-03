package Game;

import javax.swing.*;

public class GameFrame extends JFrame {

    public GameFrame() {
        setTitle("BnW");
        setSize(960, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // WAV 파일 목록
        String[] wavFiles = {
                "EvenFloor.wav",
                "Heyday.wav",
                "IDIOT.wav",
                "080509.wav",
                "Melodie.wav",
                "SunsetStrip.wav",
                "ToadSong.wav",
                "Wasted.wav"
        };

        // MusicPlayer 초기화 및 재생 시작
        MusicPlayer musicPlayer = new MusicPlayer(wavFiles);
        musicPlayer.playNextSong();

        // 초기 화면을 LoginPanel 설정
        setContentPane(new LoginPanel(this, musicPlayer));
        setVisible(true);
    }

}
