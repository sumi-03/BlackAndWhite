package Game;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class AudioPlayer {
    public static void playSound(String filePath) {
        try {
            File soundFile = new File(filePath);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();

            // 오디오 종료 이벤트 처리
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
            System.out.println("오디오 파일을 재생할 수 없습니다: " + filePath);
        }

    }
    public class SoundPaths {
        public static final String CARD_CLICK = "src/Sounds/cardclick.wav";
        public static final String CHAT = "src/Sounds/chat.wav";
        public static final String CREATE_ROOM = "src/Sounds/createroom.wav";
        public static final String ENTER_LOBBY = "src/Sounds/ibjang.wav";
    }
}