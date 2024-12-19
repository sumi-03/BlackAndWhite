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
                    System.out.println("Received: " + messageFromClient);

                    if (messageFromClient.equals("EXIT")) {
                        removeClientHandler();
                        closeEverything(socket, bufferedReader, bufferedWriter);

                    } else if (messageFromClient.startsWith("CREATE_ROOM:")) {
                        String roomTitle = messageFromClient.substring(12).trim();
                        System.out.println(clientUsername + " is creating room: " + roomTitle);
                        server.createRoom(roomTitle, clientUsername);
                        // 방 생성 후 클라이언트에게 생성 완료 메시지 전송
                        sendMessage("CREATE_ROOM_COMPLETED:" + roomTitle);
                    }
                    else if (messageFromClient.startsWith("DELETE_ROOM:")) {
                        String roomTitle = messageFromClient.substring(12).trim();
                        System.out.println(clientUsername + " is deleting the room: " + roomTitle);
                        server.deleteRoom(roomTitle, clientUsername);

                    } else if (messageFromClient.startsWith("JOIN_ROOM:")) {
                        String roomTitle = messageFromClient.substring(10).trim();
                        System.out.println(clientUsername + " is joining room: " + roomTitle);
                        server.playerJoinRoom(roomTitle, clientUsername, false); // 상대방은 false 전달
                    } else if (messageFromClient.equals("REQUEST_USERLIST")) {
                        System.out.println(clientUsername + " requested user list.");
                        broadcastUserList();

                    } else if (messageFromClient.equals("REQUEST_ROOMLIST")) {
                        System.out.println(clientUsername + " requested room list.");
                        server.broadcastRoomList();

                    } else if (messageFromClient.startsWith("SUBMIT_CARD:")) {
                        String[] parts = messageFromClient.substring(12).trim().split(":");
                        int cardNumber = Integer.parseInt(parts[0]);
                        String roomTitle = parts[1]; // 클라이언트에서 방 제목을 함께 보냄

                        System.out.println(clientUsername + " submitted card: " + cardNumber + " in room: " + roomTitle);
                        server.submitCard(clientUsername, cardNumber, roomTitle);


                        // 상대방에게 카드 제출 정보 전송
                        for (Clienthandler clientHandler : clientHandlers) {
                            if (!clientHandler.clientUsername.equals(clientUsername)) {
                                clientHandler.sendMessage("OPPONENT_CARD_SUBMITTED:" + cardNumber);
                            }
                        }

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

    public void sendMessage(String message) {
        try {
            bufferedWriter.write(message);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void broadcastMessage(String message) {
        for (Clienthandler clientHandler : clientHandlers) {
            try {
                clientHandler.bufferedWriter.write(message);
                clientHandler.bufferedWriter.newLine();
                clientHandler.bufferedWriter.flush();
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
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

        try {
            if (bufferedReader != null)
                bufferedReader.close();
            if (bufferedWriter != null)
                bufferedWriter.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // 요청받는 클라이언트 이름 리턴
    public String getClientUsername() {
        return clientUsername;
    }
}
