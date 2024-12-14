package ClientHandler;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Clienthandler implements Runnable {

    public static ArrayList<Clienthandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;

    public Clienthandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername = bufferedReader.readLine();
            clientHandlers.add(this);
            broadcastMessage("SERVER: " + clientUsername + " 님이 입장했습니다!");
            broadcastUserList(); // 유저 목록 브로드캐스트 호출 추가
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        String messageFromClient;
        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                if (messageFromClient != null) {
                    if (messageFromClient.equals("EXIT")) {
                        closeEverything(socket, bufferedReader, bufferedWriter);
                        break; // 연결 종료 후 루프 종료
                    } else {
                        // 일반 메시지 처리
                        String formattedMessage = clientUsername + ": " + messageFromClient;
                        System.out.println(formattedMessage);
                        broadcastMessage(formattedMessage);
                    }
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public void broadcastMessage(String messageToSend) {
        for (Clienthandler clientHandler : clientHandlers) {
            try {
                // if (!clientHandler.clientUsername.equals(clientUsername)) {
                clientHandler.bufferedWriter.write(messageToSend);
                clientHandler.bufferedWriter.newLine();
                clientHandler.bufferedWriter.flush();
                //   }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    // 유저 목록을 모든 클라이언트에게 전송
    public void broadcastUserList() {
        StringBuilder userList = new StringBuilder("USERLIST:");
        for (Clienthandler clientHandler : clientHandlers) {
            userList.append(clientHandler.clientUsername).append(",");
        }

        for (Clienthandler clientHandler : clientHandlers) {
            try {
                clientHandler.bufferedWriter.write(userList.toString());
                clientHandler.bufferedWriter.newLine();
                clientHandler.bufferedWriter.flush();
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    public void removeClientHandler() {
        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + clientUsername + " 님이 나갔습니다!");
        broadcastUserList();
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
        try {
            if (bufferedReader != null) bufferedReader.close();
            if (bufferedWriter != null) bufferedWriter.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
