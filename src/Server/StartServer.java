package Server;

import ClientHandler.Clienthandler;
import Game.Card;
import Game.RoomInfo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static ClientHandler.Clienthandler.clientHandlers;

public class StartServer {
    private ServerSocket serverSocket;
    //  private static List<RoomInfo> roomList = new ArrayList<>();
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

        RoomInfo newRoom = new RoomInfo(roomTitle, hostName, "대기중...");
        RoomInfo.addRoomList(newRoom);
        System.out.println("방 생성 완료. roomList size: " + RoomInfo.getRoomList().size());
        System.out.println("Room created: " + roomTitle + ", Host: " + hostName);

        for (RoomInfo room : RoomInfo.getRoomList()) {
            System.out.println("RoomTitle: " + room.getRoomTitle() + ", Host: " + room.getHostName());
        }


        broadcastRoomList();


        for (Clienthandler client : clientHandlers) {
            if (client.getClientUsername().equals(hostName)) {
                try {
                    client.getBufferedWriter().write("CREATE_ROOM_COMPLETED:" + roomTitle);
                    client.getBufferedWriter().newLine();
                    client.getBufferedWriter().flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    // 방 삭제 메서드
    public void deleteRoom(String roomTitle, String hostName) {

        RoomInfo.removeRoomList(roomTitle, hostName);
        System.out.println("Room deleted: " + roomTitle + ", Host: " + hostName);
        broadcastRoomList();
    }

    // 방 목록 브로드캐스트 메서드
    public void broadcastRoomList() {
        StringBuilder roomListMessage = new StringBuilder("ROOMLIST:");

            for (RoomInfo room : RoomInfo.getRoomList()) {
                roomListMessage.append(room.getRoomTitle()).append(",")
                        .append(room.getHostName()).append(",")
                        .append(room.getStatus()).append(";");
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
    public void playerJoinRoom(String roomTitle, String playerName) {
        for (RoomInfo room : RoomInfo.getRoomList()) {
            if (room.getRoomTitle().equals(roomTitle)) {
                if (room.getRoomFullStatus()) {
                    // Notify the player that the room is full
                    for (Clienthandler client : clientHandlers) {
                        if (client.getClientUsername().equals(playerName)) {
                            client.sendMessage("ERROR: 방이 가득 찼습니다.");
                        }
                    }
                    return;
                }

                // Add the player to the room if not full
                room.setOpponentName(playerName);
                room.setStatus("게임중");
                room.setRoomFullStatus(true);
                broadcastRoomList();

                // Notify host and opponent
                sendCountdownSignal(room.getHostName());
                sendCountdownSignal(playerName);

                for (Clienthandler client : clientHandlers) {
                    if (client.getClientUsername().equals(room.getHostName())) {
                        client.sendMessage("OPPONENT_JOINED:" + room.getRoomTitle() + "," + playerName);
                    }
                }
                return;
            }
        }
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
    public synchronized void submitCard(String playerName, int cardNumber) {
        if (submittedCount < 2) {
            submittingPlayers[submittedCount] = playerName;
            submittedCards[submittedCount] = new Card(cardNumber).getNumber(); // Card 객체 생성 후 저장
            submittedCount++;
            System.out.println(playerName + "님이 카드 " + cardNumber + "를 제출했습니다.");

            // 상대방에게 카드 제출 알림 (카드 번호 전송) - 중복 방지
            if (submittedCount == 1) {
                for (Clienthandler client : clientHandlers) {
                    if (!client.getClientUsername().equals(playerName)) {
                        try {
                            client.getBufferedWriter().write("OPPONENT_CARD_SUBMITTED:" + cardNumber);
                            client.getBufferedWriter().newLine();
                            client.getBufferedWriter().flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        if (submittedCount == 2) {
            determineRoundWinner();
            resetRound();
        }
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
