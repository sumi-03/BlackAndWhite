package Game;

import Client.ClientStart;

import javax.swing.*;
import java.util.List;

public class GameFrame extends JFrame {
    private LobbyPanel lobbyPanel;
    private ClientStart client;
    private MusicPlayer musicPlayer;
    private String[] pendingUserList;
    private String playerName;

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
        musicPlayer = new MusicPlayer(wavFiles);
        musicPlayer.playNextSong();

        // 초기 화면을 LoginPanel 설정
        setContentPane(new LoginPanel(this, musicPlayer));
        setVisible(true);
    }

    // 클라이언트 초기화 메서드
    public void initializeClient(String username) {
        this.playerName = username; // 사용자 이름 저장
        try {
            client = new ClientStart("127.0.0.1", 8888, username, this);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "서버에 연결할 수 없습니다.", "연결 실패", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void showLobbyPanel(String playerName) {
        System.out.println("Initializing LobbyPanel for player: " + playerName);
        lobbyPanel = new LobbyPanel(this, musicPlayer, playerName);
        setContentPane(lobbyPanel);
        revalidate();
        repaint();
        System.out.println("Lobby Panel shown for player: " + playerName);

        // 대기 중인 유저 목록이 있으면 업데이트
        if (pendingUserList != null) {
            updateUserList(pendingUserList);
            pendingUserList = null; // 업데이트 후 대기 목록 초기화
        }
        // 서버에 유저리스트와 룸리스트 재요청
        requestUserList();
        requestRoomList();
    }

    public void requestUserList() {
        if (client != null) {
            client.sendMessage("REQUEST_USERLIST"); // 서버에 유저 리스트 요청
        }
    }
    public void requestRoomList() {
        if (client != null) {
            client.sendMessage("REQUEST_ROOMLIST"); // 서버에 룸리스트 요청
        }
    }

    // 유저 목록 업데이트
    public void updateUserList(String[] users) {
        System.out.println("updateUserList called with: " + String.join(", ", users));
        if (lobbyPanel != null) {
            System.out.println("Updating LobbyPanel with users");
            lobbyPanel.updateUserList(users);
        } else {
            System.out.println("LobbyPanel is null, storing user list for later update");
            pendingUserList = users;
        }
    }

     // 서버에서 받은 메시지를 로비 채팅창에 추가
    public void appendMessageToLobby(String message) {
        if (lobbyPanel != null) {
            lobbyPanel.appendMessage(message);
        }
    }

    // GameFrame 클래스에 추가
    public ClientStart getClient() {
        return client;
    }


    public String getPlayerName() {
        return playerName;
    }

    public void updateRoomList(List<RoomInfo> rooms) {
        if (lobbyPanel != null) {
            lobbyPanel.updateRoomList(rooms);
        } else {
            System.out.println("LobbyPanel is null, cannot update room list.");
        }
    }
    //sendMessage
    public void sendMessage(String message) {
        if (client != null) {
            client.sendMessage(message);
        } else {
            System.out.println("Client is not initialized.");
        }
    }


    // WaitingRoomPanel로 전환
    public void showWaitingRoomPanel(RoomInfo room, boolean isPrivate, boolean isHost, String password) {
        
        WaitingRoomPanel waitingRoomPanel = new WaitingRoomPanel( room, this,isHost, playerName, room.getRoomTitle(), isPrivate, password);
        setContentPane(waitingRoomPanel);
        revalidate();
        repaint();
    }

    // 대기방 상대방 이름
    public void updateBluePlayer(String opponentName) {
        if (getContentPane() instanceof WaitingRoomPanel waitingRoomPanel) {
            waitingRoomPanel.updateBluePlayer(opponentName);
        }
    }


    // 메시지 전송 
    // public void sendMessage(String message) {}
    
    // 서버로부터 받은 메시지를 Lobby 채팅창에 전달
    // public void appendMessage(String message) {}
    
    
}

