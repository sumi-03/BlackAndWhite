package Game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoomInfo {
    private String roomTitle;
    private String hostName;
    private String opponentName; // 상대방 이름
    private String status;
    private boolean isFull = false;

    // 동기화 리스트로 관리
    private static final List<RoomInfo> roomList = Collections.synchronizedList(new ArrayList<>());

    public RoomInfo(String roomTitle, String hostName, String status) {
        this.roomTitle = roomTitle;
        this.hostName = hostName;
        this.status = status;
        this.opponentName = "???";
    }

    public String getRoomTitle() {
        return roomTitle;
    }

    public String getHostName() {
        return hostName;
    }

    public String getOpponentName() {
        return opponentName;
    }

    public String getStatus() {
        return status;
    }

    public void setOpponentName(String opponentName) {
        this.opponentName = opponentName;
    }

    public void setRoomFullStatus(boolean isFull) {
        this.isFull = isFull;
    }

    public boolean getRoomFullStatus() {
        return isFull;
    }

    public static String getOpponentName(boolean isHost, String playerName) {
        synchronized (roomList) {
            for (RoomInfo room : roomList) {
                if (isHost && room.getHostName().equals(playerName)) {
                    return room.getOpponentName();
                } else if (!isHost && room.getOpponentName().equals(playerName)) {
                    return room.getHostName();
                }
            }
        }
        return "빵";
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static void addRoomList(RoomInfo room) {
        synchronized (roomList) {
            roomList.add(room);
        }
    }

    public static void removeRoomList(String roomTitle, String hostName) {
        synchronized (roomList) {
            roomList.removeIf(room -> room.getRoomTitle().equals(roomTitle) && room.getHostName().equals(hostName));
        }
    }

    public static List<RoomInfo> getRoomList() {
        // synchronizedList 자체를 반환하여 외부에서 수정 가능하도록 함
        return roomList;
    }
}
