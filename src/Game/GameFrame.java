package Game;

import Client.ClientStart;
import Dto.MessageType;
import Dto.ObjectDto;

import javax.swing.*;
import java.io.IOException;
import java.util.List;

public class GameFrame extends JFrame {
    private LobbyPanel lobbyPanel;
    private ClientStart client;
    private MusicPlayer musicPlayer;
    private String[] pendingUserList;
    private String playerName;
    private String currentRoomTitle;

    private TimerListener timerListener = null;
    private CardSubmitListener submitListener = null;


    public GameFrame() {
        setTitle("BnW");
        setSize(960, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        addWindowListener(new java.awt.event.WindowAdapter() {

            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (client != null) {
//                    client.sendMessage("EXIT");
                    try {
                        client.closeConnection();
                    } catch (IOException e1) {

                        e1.printStackTrace();
                    }

                }
                System.exit(0); // 프로그램 종료
            }
        });

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


    public void setTimerListener(TimerListener listener) {
        this.timerListener = listener;
    }

    public void setCardSubmitListener(CardSubmitListener listener) {
        this.submitListener = listener;
    }

    public void showLobbyPanel(String playerName) {
        System.out.println("Initializing LobbyPanel for player: " + playerName);
        lobbyPanel = new LobbyPanel(this, musicPlayer, playerName); // 수정된 생성자 사용
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
//            client.sendMessage("REQUEST_USERLIST"); // 서버에 유저 리스트 요청
            client.sendObjectMessage(new ObjectDto(MessageType.REQUEST_USERLIST, "ASD"));
        }
    }
    public void requestRoomList() {
        if (client != null) {
//            client.sendMessage("REQUEST_ROOMLIST"); // 서버에 룸리스트 요청
            client.sendObjectMessage(new ObjectDto(MessageType.REQUEST_ROOMLIST, null));
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
    public void sendObjectMessage(Object message) {
        if (client != null) {
            client.sendObjectMessage(message);
        } else {
            System.out.println("Client is not initialized.");
        }
    }

    public void updateTimer(String timerString) {
        if (this.timerListener != null) {
            timerListener.changeTimer(timerString);
        }
    }

    // WaitingRoomPanel로 전환
    public void showWaitingRoomPanel(RoomInfo room) { //, boolean isPrivate, boolean isHost, String password) {

//        WaitingRoomPanel waitingRoomPanel = new WaitingRoomPanel( room, this,isHost, playerName, room.getRoomTitle(), isPrivate, password);
        WaitingRoomPanel waitingRoomPanel = new WaitingRoomPanel(room, this, playerName);
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

    //게임 패널 전환
    public void showGamePanel(RoomInfo room) {
        try {
            boolean isHost = room.getHostName().equals(this.playerName);

            Log(playerName + "is Host ? " + isHost);
            Log(room.toString()); //방 정보 보기 위함
            //TODO 패널 분리
//          GamePanel gamePanel = new GamePanel(this, playerName, musicPlayer, isHost, room.getOpponentName(), room.getRoomTitle());
//          setContentPane(gamePanel);

            if (isHost) {
                HostGamePanel hostGamePanel = new HostGamePanel(this, playerName, musicPlayer, room);
                setContentPane(hostGamePanel);
            } else {
                OpponentGamePanel opponentGamePanel = new OpponentGamePanel(this, playerName, musicPlayer, room);
                setContentPane(opponentGamePanel);
            }
            revalidate();
            repaint();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "게임 화면 전환 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void updateSubmitName(String submitName) {
        if (this.submitListener != null) {
            submitListener.cardSubmitted(submitName);
        }
    }

    // 게임 종료 후 로비로 전환
    public void returnToLobby() {
        SwingUtilities.invokeLater(() -> {
            if (lobbyPanel == null) {
                lobbyPanel = new LobbyPanel(this, musicPlayer, playerName); // 수정된 생성자 사용
            }
            setContentPane(lobbyPanel); // 로비 패널로 전환
            revalidate();
            repaint();
        });
    }

    //  제출 카드 정보 전달
    public void updateOpponentCard(int cardNum, String submitName) {
        if (getContentPane() instanceof HostGamePanel hostPanel) {
            hostPanel.updateOpponentCard(cardNum, submitName);
        } else if (getContentPane() instanceof OpponentGamePanel oppPanel) {
            oppPanel.updateOpponentCard(cardNum, submitName);
        } else {
            System.out.println("No valid game panel is active. Cannot update opponent card.");
        }
    }

    private void Log(String message) {
        System.out.println("Log Client GameFrame >> " + message);
    }

}
