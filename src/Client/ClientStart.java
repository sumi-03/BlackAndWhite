package Client;

import Game.*;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientStart {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String userName;
    private GameFrame gameFrame;
    private String currentRoomTitle;

    public ClientStart(String serverAddress, int port, String username, GameFrame gameFrame) throws IOException {
        this.gameFrame = gameFrame;
        this.userName = username;
        socket = new Socket(serverAddress, port);
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));

        // 서버에 사용자 이름 전송
        bufferedWriter.write(username);
        bufferedWriter.newLine();
        bufferedWriter.flush();

        // 서버로부터 메시지 수신 시작
        listenForMessages();
    }

    // 서버로 메시지 전송
    public void sendMessage(String message) {
        try {
            bufferedWriter.write(message);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
            closeEverything();
        }
    }

    public void setCurrentRoomTitle(String roomTitle) {
        this.currentRoomTitle = roomTitle;
    }

    public String getCurrentRoomTitle() {
        return currentRoomTitle;
    }


    // 서버로부터 메시지를 수신하는 메서드
    public void listenForMessages() {
        new Thread(() -> {
            while (socket.isConnected()) {
                try {
                    String msgFromServer = bufferedReader.readLine();
                    System.out.println("Received from Server: " + msgFromServer);

                    if (msgFromServer != null) {
                        if (msgFromServer.startsWith("USERLIST:")) {
                            String[] users = msgFromServer.substring(9).split(",");
                            gameFrame.updateUserList(users);

                        } else if (msgFromServer.startsWith("ROOMLIST:")) {
                            String[] roomData = msgFromServer.substring(9).split(";");
                            List<RoomInfo> rooms = new ArrayList<>();

                            for (String data : roomData) {
                                String[] details = data.split(",");
                                if (details.length == 3) {
                                    rooms.add(new RoomInfo(details[0], details[1], details[2]));
                                }
                            }

                            gameFrame.updateRoomList(rooms);

                        }else if (msgFromServer.startsWith("CREATE_ROOM_COMPLETED:")) {
                            String roomTitle = msgFromServer.substring(20).trim();
                            System.out.println("Room creation completed. Joining room: " + roomTitle);
                            sendMessage("JOIN_ROOM:" + roomTitle); // 방 생성 후 자동으로 입장 요청
                        }
                        else if (msgFromServer.startsWith("OPPONENT_JOINED:")) {
                            String[] parts = msgFromServer.substring(16).split(",");
                            String roomTitle = parts[0];
                            String opponentName = parts[1];
                            SwingUtilities.invokeLater(() -> gameFrame.updateBluePlayer(opponentName));

                        } else if (msgFromServer.startsWith("JOIN_ROOM_SUCCESS:")) {
                            String roomTitle = msgFromServer.substring(17).trim();
                            setCurrentRoomTitle(roomTitle); // 현재 방 제목 설정
                        }
                        else if (msgFromServer.equals("START_COUNTDOWN")) {
                            SwingUtilities.invokeLater(() -> {
                                if (gameFrame.getContentPane() instanceof WaitingRoomPanel waitingRoomPanel) {
                                    waitingRoomPanel.startCountdownToGame();
                                }
                            });
                        } else if (msgFromServer.startsWith("START_GAME:")) {
                            boolean isHost = msgFromServer.substring(11).equals("HOST");
                            SwingUtilities.invokeLater(() -> {
                                gameFrame.showGamePanel(isHost, getCurrentRoomTitle());
                            });
                        }
                        else if (msgFromServer.startsWith("OPPONENT_CARD_SUBMITTED:")) {
                            try {
                                // 콜론(:) 이후의 값을 추출
                                String cardNumberStr = msgFromServer.split(":")[1].trim();
                                System.out.println("Received card number string: \"" + cardNumberStr + "\"");

                                int cardNumber = Integer.parseInt(cardNumberStr);
                                System.out.println("Parsed card number: " + cardNumber);

                                SwingUtilities.invokeLater(() -> {
                                    if (gameFrame.getContentPane() instanceof GamePanel gamePanel) {
                                        gamePanel.updateOpponentCard(cardNumber);
                                    }
                                });
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                                JOptionPane.showMessageDialog(null, "서버에서 잘못된 카드 번호를 수신했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                            }
                        } else if (msgFromServer.startsWith("ROUND_RESULT:")) {
                            String result = msgFromServer.substring(13).trim();
                            SwingUtilities.invokeLater(() -> gameFrame.updateRoundResult(result));

                        } else {
                            gameFrame.appendMessageToLobby(msgFromServer);
                        }
                    }
                } catch (IOException e) {
                    closeEverything();
                    break;
                }
            }
        }).start();
    }

    // 리소스 정리 메서드
    public void closeEverything() {
        try {
            if (bufferedReader != null) bufferedReader.close();
            if (bufferedWriter != null) bufferedWriter.close();
            if (socket != null) socket.close();
            System.out.println("Connection closed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() throws IOException {
        if (socket != null) {
            bufferedWriter.write("EXIT");
            bufferedWriter.newLine();
            bufferedWriter.flush();
            closeEverything();
        }
    }

    public BufferedReader getBufferedReader() {
        return bufferedReader;
    }

    public String getUserName() {
        return userName;
    }
}
