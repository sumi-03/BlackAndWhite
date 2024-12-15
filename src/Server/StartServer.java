package Server;

import ClientHandler.Clienthandler;
import static ClientHandler.Clienthandler.clientHandlers;
import Game.RoomInfo;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class StartServer {

    private ServerSocket serverSocket;
    private static List<RoomInfo> roomList = new ArrayList<>();

    public StartServer(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer() {
        try {
            System.out.println("서버가 시작되었습니다. 클라이언트 접속을 기다립니다...");
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("새로운 유저가 접속했습니다!");
                Clienthandler clientHandler = new Clienthandler(socket, this); // StartServer 참조 전달

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createRoom(String roomTitle, String hostName) {
        synchronized (roomList) {
            RoomInfo newRoom = new RoomInfo(roomTitle, hostName, "대기중...");
            roomList.add(newRoom);
            System.out.println("Room created: " + roomTitle + ", Host: " + hostName);
            broadcastRoomList(); // 방 목록 브로드캐스트
        }

        // 방 생성 완료 메시지 전송
        for (Clienthandler client : Clienthandler.clientHandlers) {
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

    public void deleteRoom(String roomTitle, String hostName) {

        // roomlist에서 방 제목과 host가 일치하는 방을 찾아 삭제

        for (RoomInfo room : roomList) {

            if (room.getRoomTitle().equals(roomTitle) && room.getHostName().equals(hostName)) {

                roomList.remove(room);
            }

            break;
        }

        System.out.println("Room deleted: " + roomTitle + ", Host: " + hostName);
        broadcastRoomList(); // 방 생성 후 방 목록 브로드캐스트
    }

    public void broadcastRoomList() {
        StringBuilder roomListMessage = new StringBuilder("ROOMLIST:");
        for (RoomInfo room : roomList) {
            roomListMessage.append(room.getRoomTitle()).append(",")
                    .append(room.getHostName()).append(",")
                    .append(room.getStatus()).append(";");
        }

        System.out.println("Broadcasting Room List: " + roomListMessage); // 디버그 출력

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

    public void playerJoinRoom(String roomTitle, String playerName) {
        synchronized (roomList) {
            for (RoomInfo room : roomList) {
                if (room.getRoomTitle().equals(roomTitle)) {
                    if (room.getOpponentName().equals("???")) {
                        room.setOpponentName(playerName); // 상대방 이름 설정
                        room.setStatus("게임중"); // 상태를 '게임중'으로 변경
                        broadcastRoomList(); // 방 목록 갱신을 모든 클라이언트에 전송

                        // Blue Player 갱신 메시지 전송 (방장에게)
                        for (Clienthandler client : Clienthandler.clientHandlers) {
                            if (client.getClientUsername().equals(room.getHostName())) {
                                try {
                                    String message = "OPPONENT_JOINED:" + roomTitle + "," + playerName;
                                    client.getBufferedWriter().write(message);
                                    client.getBufferedWriter().newLine();
                                    client.getBufferedWriter().flush();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        }
    }


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


    public static List<RoomInfo> getRoomList() {
        return roomList;
    }


    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress("127.0.0.1", 8888)); // localhost 전용 바인딩
        StartServer server = new StartServer(serverSocket);
        server.startServer();
    }
}
