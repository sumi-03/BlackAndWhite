package Server;

import ClientHandler.Clienthandler;
import Dto.CardSubmitDto;
import Dto.GameInfo;
import Dto.MessageType;
import Dto.ObjectDto;
import Game.RoomInfo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static ClientHandler.Clienthandler.clientHandlers;

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
                Clienthandler clientHandler = new Clienthandler(socket, this);
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void Log(String message) {
        System.out.println("Log Server StartServer >> " + message);
    }

    // 방 생성 메서드
    public void createRoom(RoomInfo room) {
        Log("createRoom() called ");
        synchronized (roomList) {
            roomList.add(room);
            Log("room ? " + roomList);

            broadcastRoomList(); // 방 목록 브로드캐스트
        }

        room.getGameInfo().setCurrentFirstPlayer(room.getHostName()); //19:50 추가

        for (Clienthandler client : clientHandlers) {
            if (client.getClientUsername().equals(room.getHostName())) {
                client.sendObjectMessage(new ObjectDto(MessageType.CREATE_ROOM_COMPLETED, room));
                break;
            }
        }
    }


    // 방 삭제 메서드
    public void deleteRoom(String roomTitle, String hostName) {
        synchronized (roomList) {
            roomList.removeIf(room -> room.getRoomTitle().equals(roomTitle) && room.getHostName().equals(hostName));
            broadcastRoomList();
        }
        Log("Room deleted: " + roomTitle + ", Host: " + hostName);

    }

    //    // 방 목록 브로드캐스트 메서드
    public void broadcastRoomList() {
        synchronized (roomList) { // roomList 동기화
            for (Clienthandler client : clientHandlers) {
                client.sendObjectMessage(new ObjectDto(MessageType.ROOMLIST, new ArrayList<>(roomList)) // 복사본을 보냄
                );
            }
        }
    }

    public void joinRoomByTitle(String roomTitle, String playerName) {
        Log("joinRoom: " + roomTitle + ", player: " + playerName);

        synchronized (roomList) {
            RoomInfo targetRoom = null;

            // 방 찾기
            for (RoomInfo room : roomList) {
                if (room.getRoomTitle().equals(roomTitle)) {
                    targetRoom = room;
                    break;
                }
            }

            if (targetRoom == null) {
                Log("Room not found: " + roomTitle);
                return;
            }

            // 방의 상태 확인 및 수정
            synchronized (targetRoom) {

                targetRoom.setStatus("게임중");

                Log("Before setting opponentName: " + targetRoom.getOpponentName());

                if (!targetRoom.getOpponentName().equals("???")) {
                    Log("Room already has an opponent.");
                    return;
                }

                targetRoom.setOpponentName(playerName);

                Log("After setting opponentName: " + targetRoom.getOpponentName());

                // 방장 및 참가자에게 알림
                notifyPlayers(targetRoom, playerName);
            }

            broadcastRoomList(); ///
        }
    }

    private void notifyPlayers(RoomInfo room, String playerName) {
        Clienthandler host = null;
        Clienthandler opponent = null;

        for (Clienthandler client : clientHandlers) {
            String clientName = client.getClientUsername();
            // 캐시 초기화
            client.resetCache();

            if (clientName.equals(room.getHostName())) {
                host = client;
                client.sendObjectMessage(new ObjectDto(MessageType.OPPONENT_JOINED, playerName));
            } else if (clientName.equals(playerName)) {
                opponent = client;
                client.sendObjectMessage(new ObjectDto(MessageType.JOIN_ROOM_SUCCESS, room));
            }
        }

        if (host != null && opponent != null) {
            sendCountdownToGame(host, opponent, room);
        }
    }

    public void sendCountdownToGame(Clienthandler host, Clienthandler opponent, RoomInfo room) {
        Log("sendCountdownToGame");
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            int countdown = 5;

            public void run() {
                if (countdown > 0) {
                    host.sendObjectMessage(new ObjectDto(MessageType.LOADING_TIMER, countdown + ""));
                    opponent.sendObjectMessage(new ObjectDto(MessageType.LOADING_TIMER, countdown + ""));

                    countdown--;
                } else {
                    startGame(host, opponent, room);
                    timer.cancel();
                }
            }
        };
        timer.scheduleAtFixedRate(task, 0, 1000);
    }

    public synchronized void submitTutorialClosed(String roomTitle, String playerName) {
        RoomInfo targetRoom = findRoomByTitle(roomTitle);
        if (targetRoom == null) {
            Log("TutorialClosed: Room not found: " + roomTitle);
            return;
        }

        GameInfo gameInfo = targetRoom.getGameInfo();
        // Host vs Opponent 구분
        if (playerName.equals(targetRoom.getHostName())) {
            gameInfo.setHostTutorialClosed(true);
            Log("Host tutorial closed in room " + roomTitle);
        } else if (playerName.equals(targetRoom.getOpponentName())) {
            gameInfo.setOpponentTutorialClosed(true);
            Log("Opponent tutorial closed in room " + roomTitle);
        }

        // 둘 다 닫았는지 체크
        if (gameInfo.isHostTutorialClosed() && gameInfo.isOpponentTutorialClosed()) {
            Log("Both tutorials closed in room " + roomTitle + "! Broadcasting TUTORIAL_BOTH_CLOSED...");
            broadcastTutorialBothClosed(targetRoom);
        }
    }

    private RoomInfo findRoomByTitle(String roomTitle) {
        synchronized (roomList) {
            for (RoomInfo room : roomList) {
                if (room.getRoomTitle().equals(roomTitle)) {
                    return room;
                }
            }
        }
        return null; // 찾지 못한 경우
    }

    private void broadcastTutorialBothClosed(RoomInfo targetRoom) {
        // 방에 속한 2명(Host, Opponent)에게 “둘 다 튜토리얼 닫았다” 메시지 전송
        broadcastMessageToRoom(targetRoom, MessageType.TUTORIAL_BOTH_CLOSED, "TutorialBothClosed");
    }

    private void startGame(Clienthandler host, Clienthandler opponent, RoomInfo room) {
        // 방 정보 관리하려고
        host.sendObjectMessage(new ObjectDto(MessageType.START_GAME, room));
        opponent.sendObjectMessage(new ObjectDto(MessageType.START_GAME, room));
    }

    // 카드 제출 처리 메서드에서 상대방 카드 제출 메시지 전송
    public synchronized void submitCard(CardSubmitDto cardSubmitted) {
        int cardNum = cardSubmitted.getCardNum();
        String submitted = cardSubmitted.getSubmitName();
        String roomTitle = cardSubmitted.getRoomTitle();

        synchronized (roomList) {
            RoomInfo targetRoom = null;

            // 방 찾기
            for (RoomInfo room : roomList) {
                if (room.getRoomTitle().equals(roomTitle)) {
                    targetRoom = room;
                    break;
                }
            }

            if (targetRoom == null) {
                Log("Room not found: " + roomTitle);
                return;
            }

            GameInfo gameInfo = targetRoom.getGameInfo();

            synchronized (targetRoom) {

                //  1) "이미 냈는지" 체크 먼저
                if (submitted.equals(targetRoom.getHostName())) {
                    if (gameInfo.getHostCard() != -1) {
                        Log("host 카드(" + submitted + ") 이미 제출하셨습니다.");
                        return;
                    }
                } else if (submitted.equals(targetRoom.getOpponentName())) {
                    if (gameInfo.getOpponentCard() != -1) {
                        Log("Opponent 카드(" + submitted + ") 이미 제출하셨습니다.");
                        return;
                    }
                }

                // 2) notifySubmitName 호출 (카드 제출 브로드캐스트)
                gameInfo.setLastSubmitted(submitted);
                notifySubmitName(targetRoom, submitted, cardNum);

                // 3) 카드 저장
                if (submitted.equals(targetRoom.getHostName())) {
                    gameInfo.setHostCard(cardNum);
                } else if (submitted.equals(targetRoom.getOpponentName())) {
                    gameInfo.setOpponentCard(cardNum);
                }

                // 두 명 모두 제출했는지 확인
                if (gameInfo.isCardsSubmitted()) {
                    determineRoundWinner(targetRoom);
                }
            }
        }
    }

    private synchronized void determineRoundWinner(RoomInfo targetRoom) {
        GameInfo gameInfo = targetRoom.getGameInfo();
        String currentFirst = gameInfo.getCurrentFirstPlayer();// 지금 라운드의 선공
        String hostName = targetRoom.getHostName();
        String oppoName = targetRoom.getOpponentName();


        int hostCard = gameInfo.getHostCard();
        int opponentCard = gameInfo.getOpponentCard();
        String winner;

        if (hostCard > opponentCard) {
            gameInfo.incrementHostPoint();
            winner = targetRoom.getHostName();
        } else if (hostCard < opponentCard) {
            gameInfo.incrementOpponentPoint();
            winner = targetRoom.getOpponentName();
        } else {
            winner = "DRAW";
        }

        // 라운드 결과 브로드캐스트
        broadcastRoundResult(targetRoom, winner);

        // 승리 조건 확인
        if (gameInfo.getHostPoint() >= 5 || gameInfo.getOpponentPoint() >= 5) {
            broadcastGameEnd(targetRoom);
        }
        decideNextFirstPlayer(targetRoom); //선공 교대
        broadcastEndOfRound(targetRoom); //선공 알림
        gameInfo.resetCards();

    }

    //다음 라운드 선공 교대 (host <-> opponent)
    private void decideNextFirstPlayer(RoomInfo targetRoom) {
        GameInfo gameInfo = targetRoom.getGameInfo();
        String currentFirst = gameInfo.getCurrentFirstPlayer();

        String hostName = targetRoom.getHostName();
        String oppoName = targetRoom.getOpponentName();

        if (currentFirst.equals(hostName)) {
            // 현재 선공이 호스트면, 다음 라운드는 상대
            gameInfo.setCurrentFirstPlayer(oppoName);
        } else {
            // 현재 선공이 상대면, 다음 라운드는 호스트
            gameInfo.setCurrentFirstPlayer(hostName);
        }
    }

    // 다음 라운드의 선공을 클라이언트에게 알리기
    private void broadcastEndOfRound(RoomInfo targetRoom) {
        GameInfo gameInfo = targetRoom.getGameInfo();

        String nextFirst = gameInfo.getCurrentFirstPlayer();

        String data = "NEXT_FIRST:" + nextFirst;

        // 방에 속한 두 클라이언트(호스트, 상대)에게만 전송
        broadcastMessageToRoom(targetRoom, MessageType.END_OF_ROUND, data);
    }


    private void broadcastRoundResult(RoomInfo targetRoom, String winner) {
        GameInfo gameInfo = targetRoom.getGameInfo();

        // 라운드 결과 메시지
        String resultMessage = winner.equals("DRAW") ?
                "draw!":
                "WINNER:" + winner;

        // 점수 메시지
        String scoreMessage = "Score - " + targetRoom.getHostName() + ": " + gameInfo.getHostPoint() +
                " | " + targetRoom.getOpponentName() + ": " + gameInfo.getOpponentPoint();

        // 결과와 점수 정보를 함께 전송
        broadcastMessageToRoom(targetRoom, MessageType.ROUND_RESULT, resultMessage + "\n" + scoreMessage);
    }

    private void broadcastGameEnd(RoomInfo targetRoom) {
        String winner =
                (targetRoom.getGameInfo().getHostPoint() >= 5)
                        ? targetRoom.getHostName()
                        : targetRoom.getOpponentName();

        String message = "Game Over! Winner: " + winner;

        roomList.remove(targetRoom);
        broadcastRoomList();

        // GAME_END 메시지로 방에 전송
        broadcastMessageToRoom(targetRoom, MessageType.GAME_END, message);

        // 게임 상태 리셋(점수, 카드 등 0으로)
        targetRoom.getGameInfo().resetGame();
    }


    private void broadcastMessageToRoom(RoomInfo targetRoom, MessageType messageType, Object data) {
        for (Clienthandler client : clientHandlers) {
            String clientName = client.getClientUsername();

            // 방의 호스트와 상대방에게 메시지를 전송
            if (clientName.equals(targetRoom.getHostName()) || clientName.equals(targetRoom.getOpponentName())) {
                client.sendObjectMessage(new ObjectDto(messageType, data));
            }
        }
    }


    private void notifySubmitName(RoomInfo room, String submitPlayerName, int cardNumber) {
        Log("notifySubmitName: Room: " + room.getRoomTitle()
                + ", submitPlayer: " + submitPlayerName
                + ", cardNumber: " + cardNumber);
        for (Clienthandler client : clientHandlers) {
            String clientName = client.getClientUsername();
            // 캐시 초기화
            // 통신보낼 때는 ,resetCache
            client.resetCache();

            if (clientName.equals(room.getHostName()) ||
                    clientName.equals(room.getOpponentName())) {

                // CardSubmitDto 를 만들어서,
                // 낸 사람 / 카드번호 / 방제목 등을 담아 보냄
                CardSubmitDto dto = new CardSubmitDto();
                dto.setSubmitName(submitPlayerName);
                dto.setCardNum(cardNumber);
                dto.setRoomTitle(room.getRoomTitle());

                // 이걸 CARD_SUBMITTED 로 송신
                client.sendObjectMessage(new ObjectDto(MessageType.CARD_SUBMITTED, dto));
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