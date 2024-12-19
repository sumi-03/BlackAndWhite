package Server;

import ClientHandler.Clienthandler;
import Game.RoomInfo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static ClientHandler.Clienthandler.clientHandlers;

public class StartServer {
    private ServerSocket serverSocket;
    private static List<RoomInfo> roomList = new ArrayList<>();
    private int[] submittedCards = new int[2];           // 제출된 카드 번호 저장
    private String[] submittingPlayers = new String[2];  // 카드 제출한 플레이어 이름 저장
    private int submittedCount = 0;                      // 현재 제출된 카드 수

    public StartServer(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer() {
        try {
            System.out.println("서버가 시작되었습니다. 클라이언트 접속을 기다립니다...");
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("새로운 유저가 접속했습니다!");
                Clienthandler clientHandler = new Clienthandler(socket, this);
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 방 생성 메서드
    public void createRoom(String roomTitle, String hostName) {
        synchronized (roomList) {
            RoomInfo newRoom = new RoomInfo(roomTitle, hostName, "대기중...");
            roomList.add(newRoom);
            System.out.println("Room created: " + roomTitle + ", Host: " + hostName);
            broadcastRoomList(); // 방 목록 브로드캐스트
        }

        // 방 생성 후 호스트를 자동으로 입장시킴
        playerJoinRoom(roomTitle, hostName, false); // false로 전달하여 카운트다운 시작 방지
    }

    // 방 삭제 메서드
    public void deleteRoom(String roomTitle, String hostName) {
        synchronized (roomList) {
            roomList.removeIf(room -> room.getRoomTitle().equals(roomTitle) && room.getHostName().equals(hostName));
        }
        System.out.println("Room deleted: " + roomTitle + ", Host: " + hostName);
        broadcastRoomList();
    }

    // 방 목록 브로드캐스트 메서드
    public void broadcastRoomList() {
        StringBuilder roomListMessage = new StringBuilder("ROOMLIST:");
        synchronized (roomList) {
            for (RoomInfo room : roomList) {
                roomListMessage.append(room.getRoomTitle()).append(",")
                        .append(room.getHostName()).append(",")
                        .append(room.getStatus()).append(";");
            }
        }

        for (Clienthandler client : clientHandlers) {
            try {
                client.getBufferedWriter().write(roomListMessage.toString());
                client.getBufferedWriter().newLine();
                client.getBufferedWriter().flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 플레이어가 방에 입장할 때 호출되는 메서드
    public void playerJoinRoom(String roomTitle, String playerName, boolean isHost) {
        synchronized (roomList) {
            for (RoomInfo room : roomList) {
                if (room.getRoomTitle().equals(roomTitle)) {
                    if (room.getHostName().equals(playerName)) {
                        System.out.println(playerName + " (host) joined the room: " + roomTitle);
                        room.setStatus("대기중...");
                    } else if (room.getOpponentName().equals("???")) {
                        room.setOpponentName(playerName);
                        room.setStatus("게임중");
                        broadcastRoomList();

                        // 호스트에게 START_COUNTDOWN 신호 전송
                        sendCountdownSignal(room.getHostName());
                        // 상대방에게 START_GAME 신호 전송
                        sendStartGameSignal(playerName, "OPPONENT");

                        // 호스트에게 상대방 입장 알림
                        for (Clienthandler client : clientHandlers) {
                            if (client.getClientUsername().equals(room.getHostName())) {
                                client.sendMessage("OPPONENT_JOINED:" + room.getRoomTitle() + "," + playerName);
                            }
                        }
                    }
                }
            }
        }
    }

    private void sendStartGameSignal(String playerName, String role) {
        // 먼저 카운트다운 신호를 보냄
        sendCountdownSignal(playerName);

        // 5초 후에 START_GAME 신호를 보냄
        new Thread(() -> {
            try {
                Thread.sleep(5000); // 5초 대기
                for (Clienthandler client : clientHandlers) {
                    if (client.getClientUsername().equals(playerName)) {
                        client.getBufferedWriter().write("START_GAME:" + role);
                        client.getBufferedWriter().newLine();
                        client.getBufferedWriter().flush();
                        break;
                    }
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }).start();
    }


    private void sendCountdownSignal(String playerName) {
        for (Clienthandler client : clientHandlers) {
            if (client.getClientUsername().equals(playerName)) {
                try {
                    client.getBufferedWriter().write("START_COUNTDOWN");
                    client.getBufferedWriter().newLine();
                    client.getBufferedWriter().flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    // 카드 제출 처리 메서드에서 상대방 카드 제출 메시지 전송
    public void submitCard(String playerName, int cardNumber, String roomTitle) {
        synchronized (roomList) {
            for (RoomInfo room : roomList) {
                if (room.getRoomTitle().equals(roomTitle)) {
                    if (room.submitCard(playerName, cardNumber)) {
                        System.out.println(playerName + "님이 방 '" + roomTitle + "'에 카드 " + cardNumber + "를 제출했습니다.");

                        // 상대방에게 카드 제출 알림
                        for (Clienthandler client : clientHandlers) {
                            if (!client.getClientUsername().equals(playerName) &&
                                    (client.getClientUsername().equals(room.getHostName()) || client.getClientUsername().equals(room.getOpponentName()))) {
                                client.sendMessage("OPPONENT_CARD_SUBMITTED:" + cardNumber);
                            }
                        }

                        // 두 장이 제출되면 라운드 결과를 결정하고 알림
                        String result = room.determineRoundWinner();
                        if (result != null) {
                            broadcastMessage(result);
                        }
                    }
                    break;
                }
            }
        }



        if (submittedCount == 2) {
            determineRoundWinner();
            resetRound();
        }
    }

    // 상대방 이름 반환 메서드
    public static String getOpponentName(boolean isHost, String playerName) {
        int i = 0;

        System.out.println("### roomList size: " + roomList.size());
        System.out.flush(); // 버퍼 비우기: roomList 크기를 즉시 확인

        for (RoomInfo room : roomList) {
            System.out.println("출력:::: " + i++ + room.getHostName() + room.getOpponentName());
            System.out.flush(); // 버퍼 비우기: 방 정보 즉시 확인

            if (isHost && room.getHostName().equals(playerName)) {
                return room.getOpponentName();
            } else if (!isHost && room.getOpponentName().equals(playerName)) {
                return room.getHostName();
            }
        }
        return "빵";
    }


    // 라운드 승자 결정 메서드
    private void determineRoundWinner() {
        String result;
        if (submittedCards[0] > submittedCards[1]) {
            result = "ROUND_RESULT:WINNER:" + submittingPlayers[0];
        } else if (submittedCards[0] < submittedCards[1]) {
            result = "ROUND_RESULT:WINNER:" + submittingPlayers[1];
        } else {
            result = "ROUND_RESULT:DRAW";
        }

        broadcastMessage(result);
    }

    // 라운드 상태 초기화
    private void resetRound() {
        submittedCount = 0;
        submittingPlayers = new String[2];
        submittedCards = new int[2];
    }

    // 메시지 브로드캐스트 메서드
    public void broadcastMessage(String message) {
        for (Clienthandler client : clientHandlers) {
            try {
                client.getBufferedWriter().write(message);
                client.getBufferedWriter().newLine();
                client.getBufferedWriter().flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 서버 시작 메서드
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress("127.0.0.1", 8888));
        StartServer server = new StartServer(serverSocket);
        server.startServer();
    }
}
