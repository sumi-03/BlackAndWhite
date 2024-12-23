package ClientHandler;

import Dto.CardSubmitDto;
import Dto.MessageType;
import Dto.ObjectDto;
import Dto.TutorialClosedDto;
import Game.RoomInfo;
import Server.StartServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Clienthandler implements Runnable {
    public static List<Clienthandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private String clientUsername;
    private StartServer server; // StartServer 참조

    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;


    public Clienthandler(Socket socket, StartServer server) {
        try {
            this.socket = socket;
            this.server = server;

            // object 를 읽을 수 있도록 추가
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            this.objectInputStream = new ObjectInputStream(socket.getInputStream());

            clientHandlers.add(this);
            broadcastUserList(); // 유저리스트 브로드캐스트

        } catch (IOException e) {
            closeEverything();
        }
    }

    private void Log(String message) {
        System.out.println("Log Server ClientHandler >> " + message);
    }

    public void resetCache() {
        try {
            this.objectOutputStream.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Object 로 받을 수 있도록 변경 2024_12_20
    @Override
    public void run() {
        while (socket.isConnected()) {
            try {
                Object messageFromClient = objectInputStream.readObject();

                if (messageFromClient instanceof ObjectDto) {
                    ObjectDto objectDto = (ObjectDto) messageFromClient;
                    MessageType messageType = objectDto.getMessageType();
                    Object data = objectDto.getData();

                    System.out.println("Log >> Server Received messageType: " + messageType);

                    switch (messageType) {
                        case USER_NAME:
                            if (data instanceof String) {
                                this.clientUsername = (String) data;
                            } else {
                                System.out.println("Error");
                            }
                            broadcastMessage(MessageType.USER_NAME, "SERVER: " + clientUsername + " 님이 입장했습니다!");

                            break;

                        case CREATE_ROOM:
                            Log("create room");
                            if (data instanceof RoomInfo) { // 이후에도 모든 Object에 대해 2중 체크 혹은 try
                                RoomInfo room = (RoomInfo) data;
                                String roomTitle = room.getRoomTitle();
                                String hostName = room.getHostName();
                                System.out.println(hostName + " is creating room: " + roomTitle);

                                server.createRoom(room);
                            } else {
                                System.out.println("Invalid data for CREATE_ROOM");
                            }

                            break;

                        case JOIN_ROOM:
                            Log("join room");
                            if (data instanceof String) {
                                server.joinRoomByTitle(data.toString(), clientUsername);
                            } else {
                                System.out.println("Invalid data for JOIN_ROOM");
                            }
                            break;

                        case DELETE_ROOM:
                            Log("delete_room");

                            if (data instanceof String) {
                                String roomTitle = (String) data;
                                server.deleteRoom(roomTitle, clientUsername);
                            }

                            break;

                        case TUTORIAL_CLOSED:
                            if (data instanceof TutorialClosedDto) {
                                TutorialClosedDto closedDto = (TutorialClosedDto) data;
                                server.submitTutorialClosed(closedDto.getRoomTitle(), closedDto.getPlayerName());
                            }
                            break;

                        case SUBMIT_CARD:
                            CardSubmitDto cardSubmit = (CardSubmitDto) data;
                            Log(clientUsername + " submitted card: " + cardSubmit.toString());
                            server.submitCard(cardSubmit); //카드를 제출
                            break;

                        case EXIT:
                            removeClientHandler();
                            closeEverything();
                            return;
//
                        case REQUEST_USERLIST:
                            System.out.println(clientUsername + " requested user list.");

                            broadcastUserList();
                            break;
//
                        case REQUEST_ROOMLIST:
                            System.out.println(clientUsername + " requested room list.");
                            server.broadcastRoomList();
                            break;

                        case STRING_MESSAGE:
                            String message = (String) data;
                            broadcastMessage(MessageType.STRING_MESSAGE, clientUsername + ": " + message);
                            break;

                        default:
                            System.out.println("Unhandled messageType: " + messageType);
                            break;
                    }
                } else {
                    System.out.println("Invalid message received: " + messageFromClient);
                }
            } catch (IOException | ClassNotFoundException e) {
                closeEverything();
                break;
            }
        }
    }

    private void broadcastMessage(MessageType messageType, String message) {
        for (Clienthandler clientHandler : clientHandlers) {
            clientHandler.sendObjectMessage(new ObjectDto(messageType, message));
        }
    }

    // 유저 리스트 브로드캐스트 메서드
    public void broadcastUserList() {
        StringBuilder userListMessage = new StringBuilder("");
        for (Clienthandler clientHandler : clientHandlers) {

            userListMessage.append(clientHandler.clientUsername).append(",");
        }

        for (Clienthandler clientHandler : clientHandlers) {
            clientHandler.sendObjectMessage(new ObjectDto(MessageType.USERLIST, userListMessage.toString()));
        }
    }

    private void removeClientHandler() {
        Log("remove client " + clientUsername);
        clientHandlers.remove(this);

        broadcastMessage(MessageType.USER_EXIT, "SERVER: " + clientUsername + " 님이 나갔습니다!");
        broadcastUserList();
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

    private void closeEverything() {
        try {
            if (objectInputStream != null) objectInputStream.close();
            if (objectOutputStream != null) objectOutputStream.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 요청받는 클라이언트 이름 리턴
    public String getClientUsername() {
        return clientUsername;
    }
}
