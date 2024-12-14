package Client;

import Game.GameFrame;
import Game.RoomInfo;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientStart {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;
    private GameFrame gameFrame;

    public ClientStart(String serverAddress, int port, String username, GameFrame gameFrame) throws IOException {
        this.gameFrame = gameFrame;
        this.username = username;
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

    // 서버로부터 메시지를 수신하는 메서드
    public void listenForMessages() {
        new Thread(() -> {
            String msgFromServer;
            while (socket.isConnected()) {
                try {
                    msgFromServer = bufferedReader.readLine();
                    System.out.println("Received from Server: " + msgFromServer);

                    if (msgFromServer.startsWith("USERLIST:")) {
                        String[] users = msgFromServer.substring(9).split(",");
                        gameFrame.updateUserList(users); // 유저리스트 업데이트

                    } else if (msgFromServer.startsWith("ROOMLIST:")) {
                        String[] roomData = msgFromServer.substring(9).split(";");
                        List<RoomInfo> rooms = new ArrayList<>();

                        for (String data : roomData) {
                            String[] details = data.split(",");
                            if (details.length == 3) {
                                rooms.add(new RoomInfo(details[0], details[1], details[2]));
                            }
                        }

                        gameFrame.updateRoomList(rooms); // 방 리스트 업데이트

                    } else if (msgFromServer.startsWith("OPPONENT_JOINED:")) {
                        String[] parts = msgFromServer.substring(16).split(",");
                        String roomTitle = parts[0];
                        String opponentName = parts[1];
                        gameFrame.updateOpponentName(opponentName); // 상대방 이름 업데이트
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
}
