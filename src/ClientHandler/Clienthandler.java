package ClientHandler;

import Server.StartServer;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Clienthandler implements Runnable {
    public static List<Clienthandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;
    private StartServer server; // StartServer 참조

    public Clienthandler(Socket socket, StartServer server) {
        try {
            this.socket = socket;
            this.server = server;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));

            this.clientUsername = bufferedReader.readLine();
            clientHandlers.add(this);
            broadcastMessage("SERVER: " + clientUsername + " 님이 입장했습니다!");
            broadcastUserList(); // 유저리스트 브로드캐스트
        } catch (IOException e) {
            closeEverything();
        }
    }

    @Override
    public void run() {
        String messageFromClient;

        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();

                if (messageFromClient != null) {
                    System.out.println("Received: " + messageFromClient);

                    if (messageFromClient.startsWith("CREATE_ROOM:")) {
                        String roomTitle = messageFromClient.substring(12).trim();
                        System.out.println(clientUsername + " is creating room: " + roomTitle);

                        // StartServer의 createRoom 메서드 호출
                        server.createRoom(roomTitle, clientUsername);

                    } else if (messageFromClient.startsWith("JOIN_ROOM:")) {
                        String roomTitle = messageFromClient.substring(10).trim();
                        System.out.println(clientUsername + " is joining room: " + roomTitle);

                        // StartServer의 playerJoinRoom 메서드 호출
                        server.playerJoinRoom(roomTitle, clientUsername);

                    } else if (messageFromClient.equals("REQUEST_USERLIST")) {
                        System.out.println(clientUsername + " requested user list.");
                        broadcastUserList(); // 유저리스트 브로드캐스트

                    } else if (messageFromClient.equals("REQUEST_ROOMLIST")) {
                        System.out.println(clientUsername + " requested room list.");
                        server.broadcastRoomList(); // 방 목록 브로드캐스트

                    } else {
                        broadcastMessage(messageFromClient);
                    }
                }
            } catch (IOException e) {
                closeEverything();
                break;
            }
        }
    }


    public void broadcastMessage(String message) {
        for (Clienthandler clientHandler : clientHandlers) {
            try {
                clientHandler.bufferedWriter.write(message);
                clientHandler.bufferedWriter.newLine();
                clientHandler.bufferedWriter.flush();
            } catch (IOException e) {
                closeEverything();
            }
        }
    }

    public BufferedWriter getBufferedWriter() {
        return bufferedWriter;
    }

    // 유저 리스트 브로드캐스트 메서드
    public void broadcastUserList() {
        StringBuilder userListMessage = new StringBuilder("USERLIST:");
        for (Clienthandler clientHandler : clientHandlers) {
            userListMessage.append(clientHandler.clientUsername).append(",");
        }

        for (Clienthandler clientHandler : clientHandlers) {
            try {
                clientHandler.bufferedWriter.write(userListMessage.toString());
                clientHandler.bufferedWriter.newLine();
                clientHandler.bufferedWriter.flush();
            } catch (IOException e) {
                clientHandler.closeEverything();
            }
        }
    }

    public void closeEverything() {
        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + clientUsername + " 님이 나갔습니다!");
        broadcastUserList(); // 유저리스트 브로드캐스트
        try {
            if (bufferedReader != null) bufferedReader.close();
            if (bufferedWriter != null) bufferedWriter.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
