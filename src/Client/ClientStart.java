package Client;

import Game.GameFrame;

import java.io.*;
import java.net.Socket;

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
                    System.out.println("Received from Server: " + msgFromServer); // 디버그 출력

                    if (msgFromServer != null && msgFromServer.startsWith("USERLIST:")) {
                        String[] users = msgFromServer.substring(9).split(",");
                        System.out.println("Parsed User List: " + String.join(", ", users)); // 디버그 출력
                        gameFrame.updateUserList(users);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
