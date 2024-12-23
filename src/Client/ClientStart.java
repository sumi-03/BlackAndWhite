package Client;

import Dto.CardSubmitDto;
import Dto.MessageType;
import Dto.ObjectDto;
import Game.GameFrame;
import Game.HostGamePanel;
import Game.OpponentGamePanel;
import Game.RoomInfo;
import java.io.*;
import java.net.Socket;
import java.util.List;
import javax.sound.sampled.*;
import javax.swing.*;

public class ClientStart {
    private Socket socket;
    private String userName;
    private GameFrame gameFrame;
    private Clip clip;
    private HostGamePanel.AudioPlayer audioPlayer;

    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;

    public ClientStart(String serverAddress, int port, String username, GameFrame gameFrame) throws IOException {
        this.gameFrame = gameFrame;
        this.userName = username;
        socket = new Socket(serverAddress, port);

        this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        this.objectInputStream = new ObjectInputStream(socket.getInputStream());

        // 서버에 사용자 이름 전송
        sendObjectMessage(new ObjectDto(MessageType.USER_NAME, username));

        // 서버로부터 메시지 수신 시작
        listenForMessages();
    }

    //2024_12_20 object 전달할 수 있도록 변경
    public void sendObjectMessage(Object message) {
        try {
            objectOutputStream.writeObject(message);
            objectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.toString());
            closeEverything();
        }
    }


    private void Log(String message) {
        System.out.println("Log Client ClientStart >> " + message);
    }

    private void playSound(String resourcePath) {
        try {
            // 리소스 경로에서 오디오 파일 읽기
            InputStream resourceStream1 = getClass().getResourceAsStream("/Voices/gamestart.wav");
            if (resourceStream1 == null) {
                throw new FileNotFoundException("Resource not found: " + "/Voices/gamestart.wav");
            }

            AudioInputStream audioInputStream1 = AudioSystem.getAudioInputStream(resourceStream1);
            clip = AudioSystem.getClip();
            clip.open(audioInputStream1);
            clip.start();

            // 오디오 종료까지 대기
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();

        }
    }




    // 서버로부터 메시지를 수신하는 메서드
    public void listenForMessages() {
        new Thread(() -> {
            while (socket.isConnected()) {
                try {
                    // ObjectInputStream으로 ObjectDto 읽기
                    Object messageFromServer = objectInputStream.readObject();

                    if (messageFromServer instanceof ObjectDto) {
                        ObjectDto objectDto = (ObjectDto) messageFromServer;
                        MessageType messageType = objectDto.getMessageType();
                        Object data = objectDto.getData();

                        Log("Client Received messageType: " + messageType);

                        switch (messageType) {
                            case USERLIST:
                                if (data instanceof String) {
                                    String orginString = (String) data;

                                    String[] users = (String[]) orginString.split(",");
                                    gameFrame.updateUserList(users);
                                }
                                break;

                            case ROOMLIST:
                                Log("RoomList : " + data);
                                if (data instanceof List) {
                                    Log("is Updaiting ? ");
                                    @SuppressWarnings("unchecked")
                                    List<RoomInfo> rooms = (List<RoomInfo>) data;
                                    gameFrame.updateRoomList(rooms);
                                }
                                break;

                            case CREATE_ROOM_COMPLETED:
                                Log("CREATE_ROOM_COMPLETED");

                                if (data instanceof RoomInfo) {
                                    RoomInfo room = (RoomInfo) data;
                                    String roomTitle = room.getRoomTitle();

                                    //방 입장 어차피 정보는 서버에 들고 있고, 호스트는 고정이므로 화면 이동 처리
                                    SwingUtilities.invokeLater(() -> {
                                        gameFrame.showWaitingRoomPanel(
                                                room
                                        );
                                    });

                                    break;

                                } 

                            case JOIN_ROOM_SUCCESS:
                                Log("JOIN_ROOM_SUCCESS");
                                if (data instanceof RoomInfo) {
                                    RoomInfo room = (RoomInfo) data;
                                    Log(room.toString());
                                    SwingUtilities.invokeLater(() -> {
                                        gameFrame.showWaitingRoomPanel(
                                                room
                                        );
                                    });
                                }
                                break;
//
                            case OPPONENT_JOINED:
                                Log("OPPNENT_JOINED");
                                if (data instanceof String) {
                                    String opponentName = (String) data;
                                    SwingUtilities.invokeLater(() -> gameFrame.updateBluePlayer(opponentName));

                                }
                                break;

                            case LOADING_TIMER :
                                if (data instanceof String) {
                                    String timerString = (String) data;
                                    SwingUtilities.invokeLater(() -> gameFrame.updateTimer(timerString));
                                }
                                break;

                            case TUTORIAL_BOTH_CLOSED:
                                Log("TUTORIAL_BOTH_CLOSED received");
                                SwingUtilities.invokeLater(() -> {
                                    playSound("/Voices/gamestart.wav");
                                    //playSound("/voices/1round.wav");
                                    //  지금 어떤 패널이 표시 중인지 확인
                                    if (gameFrame.getContentPane() instanceof HostGamePanel hostPanel) {
                                        hostPanel.startRealGame();
                                    } else if (gameFrame.getContentPane() instanceof OpponentGamePanel oppPanel) {
                                        oppPanel.startRealGame();
                                    }
                                });
                                break;

                            case START_GAME:
                                Log("  START_GAME");
                                if (data instanceof RoomInfo) {
                                    RoomInfo room = (RoomInfo) data;

                                    SwingUtilities.invokeLater(() -> gameFrame.showGamePanel(room));
                                }
                                break;


                            case CARD_SUBMITTED:
                                if (data instanceof CardSubmitDto cardData) {
                                    SwingUtilities.invokeLater(() -> {
                                        // 1) 중앙 배치
                                        gameFrame.updateOpponentCard(cardData.getCardNum(), cardData.getSubmitName());
                                        // 2) 턴 교대
                                        if (gameFrame.getContentPane() instanceof HostGamePanel hostPanel) {
                                            hostPanel.cardSubmitted(cardData.getSubmitName());
                                        } else if (gameFrame.getContentPane() instanceof OpponentGamePanel oppPanel) {
                                            oppPanel.cardSubmitted(cardData.getSubmitName());
                                        }
                                    });
                                }
                                break;

                            case ROUND_RESULT:
                                Log("ROUND_RESULT received");
                                if (data instanceof String) {
                                    String[] parts = ((String) data).split("\n");
                                    String resultMessage = parts[0]; // 라운드 결과 메시지
                                    String scoreMessage = parts.length > 1 ? parts[1] : ""; // 점수 메시지

                                    SwingUtilities.invokeLater(() -> {
                                        if (gameFrame.getContentPane() instanceof HostGamePanel hostPanel) {
                                            hostPanel.handleRoundResult(resultMessage);
                                            if (!scoreMessage.isEmpty()) {
                                                hostPanel.updateScoresFromMessage(scoreMessage);
                                            }
                                        } else if (gameFrame.getContentPane() instanceof OpponentGamePanel opponentPanel) {
                                            opponentPanel.handleRoundResult(resultMessage);
                                            if (!scoreMessage.isEmpty()) {
                                                opponentPanel.updateScoresFromMessage(scoreMessage);
                                            }
                                        }
                                    });
                                }
                                break;

                            case END_OF_ROUND:
                                Log("END_OF_ROUND received");
                                if (data instanceof String) {
                                    String msg = (String) data;
                                    // 간단히 파싱
                                    String nextFirstPlayer = parseNextFirst(msg);

                                    // 이제 HostGamePanel 또는 OpponentGamePanel 중
                                    SwingUtilities.invokeLater(() -> {
                                        if (gameFrame.getContentPane() instanceof HostGamePanel hostPanel) {
                                            hostPanel.onRoundFinished(nextFirstPlayer);
                                        } else if (gameFrame.getContentPane() instanceof OpponentGamePanel opponentPanel) {
                                            opponentPanel.onRoundFinished(nextFirstPlayer);
                                        }
                                    });
                                }
                                break;


                            case GAME_END:
                                if (data instanceof String) {
                                    String finalWinner = (String) data;
                                    SwingUtilities.invokeLater(() -> {
                                        new Thread(() -> {
                                            try {
                                                Thread.sleep(9200);
                                                // 딜레이 후에 다이얼로그
                                                SwingUtilities.invokeLater(() -> {
                                                    int choice = JOptionPane.showConfirmDialog(gameFrame,
                                                            "최종 승리자: " + finalWinner + "\n로비로 돌아가시겠습니까?",
                                                            "게임 종료",
                                                            JOptionPane.YES_NO_OPTION);

                                                    if (choice == JOptionPane.YES_OPTION) {
                                                        gameFrame.returnToLobby();
                                                    } else {
                                                        // 로비로 돌아가지 않을 경우의 동작
                                                    }
                                                });
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }).start();
                                    });
                                }
                                break;

                            default:
                                if (data instanceof String) {
                                    String message = (String) data;
                                    gameFrame.appendMessageToLobby(message);
                                }
                                break;
                        }
                    } else {
                        System.out.println("Invalid message received: " + messageFromServer);
                    }
                } catch (IOException | ClassNotFoundException e) {
                    closeEverything();
                    break;
                }
            }
        }).start();
    }
    // 리소스 정리 메서드
    private void closeEverything() {
        try {
            if (objectInputStream != null) objectInputStream.close();
            if (objectOutputStream != null) objectOutputStream.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //파싱 메서드
    private String parseNextFirst(String message) {
        // "NEXT_FIRST:홍길동" -> "홍길동"만 추출
        if (message.startsWith("NEXT_FIRST:")) {
            return message.substring("NEXT_FIRST:".length()).trim();
        }
        return "";
    }


    public void closeConnection() throws IOException {
        if (socket != null) {
            sendObjectMessage(new ObjectDto(MessageType.EXIT, "UserExit"));
            closeEverything();
        }
    }

    public String getUserName() {
        return userName;
    }
}