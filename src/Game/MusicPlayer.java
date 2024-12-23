package Game;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



public class MusicPlayer {
    private final List<String> playlist; // 재생 목록
    private int currentIndex = 0; // 현재 재생 중인 곡 인덱스
    private Clip clip; // 현재 재생 중인 클립
    private boolean isPlaying = false; // 현재 재생 상태 플래그

    private int volumeSliderValue = 100; // default
    private String currentSongName = "";
    private SongChangeListener songChangeListener = null;

    public MusicPlayer(String[] filePaths) {
        playlist = new ArrayList<>();
        Collections.addAll(playlist, filePaths);
        Collections.shuffle(playlist); // 시작 시 셔플
    }

    public void setListener(SongChangeListener listener) {
        this.songChangeListener = listener;
    }

    public synchronized void playNextSong() {
        stop(); // 기존 재생 중단
        try {
            // 현재 곡 가져오기
            String currentFile = "src/Musics/" + playlist.get(currentIndex);
            System.out.println("재생 중: " + currentFile);

            //곡 이름 분할
            String[] parts = currentFile.split("/");
            String fileNameWithExtension = parts[parts.length - 1];
            String songName = fileNameWithExtension.split("\\.")[0];

            //곡 이름 저장 & 리스너 동작
            this.currentSongName = "재생중인 곡 제목 : " + songName;
            if (songChangeListener != null) {
                this.songChangeListener.changeSongName(this.currentSongName);
            }

            // 오디오 파일 로드
            File audioFile = new File(currentFile);
            if (!audioFile.exists()) {
                System.err.println("파일을 찾을 수 없습니다: " + currentFile);
                moveToNextSong(); // 다음 곡으로 이동
                playNextSong(); // 다음 곡 재생
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);

            // 클립 객체 초기화
            clip = AudioSystem.getClip();
            clip.open(audioStream);

            // 클립 이벤트: 재생 종료 시 다음 곡으로 이동
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                    moveToNextSong();
                    playNextSong();
                }
            });

            // 재생 시작
            this.setVolume(volumeSliderValue); // 재생 시작 시에 볼륨 컨트롤
            isPlaying = true;
            clip.start();

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
            moveToNextSong();
            playNextSong(); // 오류 발생 시 다음 곡으로 이동
        }
    }

    public synchronized void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close(); // 리소스 해제
            isPlaying = false;
        }
    }

    private synchronized void moveToNextSong() {
        currentIndex++;
        if (currentIndex >= playlist.size()) {
            currentIndex = 0;
            Collections.shuffle(playlist); // 모든 곡 재생 후 셔플
        }
    }

    // 2024_12_19 설정창에서, 플레이어의 볼륨을 사용하도록
    public int getVolumeLevel() {
        return this.volumeSliderValue;
    }

    public void setVolumeLevel(int sliderValue) {
        this.volumeSliderValue = sliderValue;
        this.setVolume(sliderValue);
    }

    // 2024_12_19 private 로 변경
    private void setVolume(int sliderValue) {
        if (clip != null && clip.isOpen()) {

            float volume = sliderValue / 100f; // 0~100 값을 0.0~1.0로 변환

            try {
                FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

                // 볼륨 범위 가져오기
                float min = volumeControl.getMinimum(); // 대개 약 -80.0
                float max = volumeControl.getMaximum(); // 대개 6.0

                // 볼륨 값 계산: 로그 스케일로 설정
                if (volume <= 0.01f) {
                    volumeControl.setValue(min); // 완전히 무음
                } else {
                    // 로그 스케일을 사용하여 부드러운 감소 처리
                    float gain = (float) (Math.log10(volume) * 20); // 0.0~1.0을 dB로 변환
                    volumeControl.setValue(Math.max(min, Math.min(gain, max))); // min~max 범위 내 설정
                }
            } catch (IllegalArgumentException e) {
                System.err.println("이 시스템에서 볼륨 제어를 지원하지 않습니다.");
            }
        }
    }

    public String getCurrentSongName() {
        return this.currentSongName;
    }


    public boolean isPlaying() {
        return isPlaying;
    }
}
