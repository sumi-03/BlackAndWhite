package Game;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;

public class SettingsPanel extends JPanel implements SongChangeListener {
    private static final long serialVersionUID = 1L;

    private JLabel songNameLabel = new JLabel("노래 제목이 여기 표시됩니다. ");

    public SettingsPanel(JFrame parentFrame, MusicPlayer musicPlayer) {
        setLayout(null);
        setBounds(200, 100, 560, 400); // 설정 패널 크기
        setBackground(new Color(0, 0, 0, 230)); // 반투명 배경
        setOpaque(true);

        JLabel masterVolumeLabel = new JLabel("마스터 볼륨");
        masterVolumeLabel.setForeground(Color.WHITE);
        masterVolumeLabel.setBounds(50, 50, 150, 30);
        add(masterVolumeLabel);

        JSlider masterVolumeSlider = new JSlider(0, 100, 100); // 초기값 100
        masterVolumeSlider.setBounds(155, 50, 300, 30);
        add(masterVolumeSlider);

        JLabel musicVolumeLabel = new JLabel("음악 볼륨");
        musicVolumeLabel.setForeground(Color.WHITE);
        musicVolumeLabel.setBounds(50, 100, 150, 30);
        add(musicVolumeLabel);

        JSlider musicVolumeSlider = new JSlider(0, 100, 100); // 초기값 100
        musicVolumeSlider.setBounds(155, 100, 300, 30);
        add(musicVolumeSlider);


        //제목달기 편의상 맨밑에 담
        String songName = musicPlayer.getCurrentSongName();
        songNameLabel.setText(songName);
        songNameLabel.setBounds(155,250, 300, 30);
        songNameLabel.setForeground(Color.WHITE);
        add(songNameLabel);

        //제목 리스너 설정
        musicPlayer.setListener(this);

        //2024_12_19 Volume Slider Value
        musicVolumeSlider.setValue(musicPlayer.getVolumeLevel());

        // 음악 볼륨 슬라이더 이벤트
        musicVolumeSlider.addChangeListener((ChangeEvent e) -> {
            int sliderValue = musicVolumeSlider.getValue();
            musicPlayer.setVolumeLevel(sliderValue); // 플레어 안에서 쓰도록 함
        });


        JLabel effectVolumeLabel = new JLabel("효과음 볼륨");
        effectVolumeLabel.setForeground(Color.WHITE);
        effectVolumeLabel.setBounds(50, 150, 150, 30);
        add(effectVolumeLabel);

        JSlider effectVolumeSlider = new JSlider(0, 100, 100); // 초기값 100
        effectVolumeSlider.setBounds(155, 150, 300, 30);
        add(effectVolumeSlider);

        JLabel voiceVolumeLabel = new JLabel("음성 볼륨");
        voiceVolumeLabel.setForeground(Color.WHITE);
        voiceVolumeLabel.setBounds(50, 200, 150, 30);
        add(voiceVolumeLabel);

        JSlider voiceVolumeSlider = new JSlider(0, 100, 100); // 초기값 100
        voiceVolumeSlider.setBounds(155, 200, 300, 30);
        add(voiceVolumeSlider);

        // 닫기 버튼
        JButton closeButton = new JButton("닫기");
        closeButton.setBounds(230, 300, 100, 40);
        closeButton.addActionListener(e -> {
            // 설정 패널을 부모의 레이어드 팬에서 제거
            AudioPlayer.playSound(AudioPlayer.SoundPaths.ENTER_LOBBY);
            parentFrame.getLayeredPane().remove(this);
            parentFrame.getLayeredPane().revalidate();
            parentFrame.getLayeredPane().repaint();
        });
        add(closeButton);

    }

    //플레이어로부터 이벤트 받아서 설정
    @Override
    public String changeSongName(String songName) {

        this.songNameLabel.setText(songName);

        return null;
    }
}
