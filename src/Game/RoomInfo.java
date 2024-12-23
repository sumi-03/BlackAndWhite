package Game;

import Dto.GameInfo;

import java.io.Serializable;

public class RoomInfo implements Serializable {
    private String roomTitle;
    private String hostName;
    private String opponentName; // 상대방 이름
    private String status;
    private String isPrivate = "Y"; //"N"
    private String password = "";
    private GameInfo gameInfo = new GameInfo();


    public RoomInfo(String roomTitle, String hostName, String status, String password, String isPrivate) {
        this.roomTitle = roomTitle;
        this.hostName = hostName;
        this.status = status;
        this.password = password;
        this.isPrivate = isPrivate;
        this.opponentName = "???";
    }

    public String getRoomTitle() { return roomTitle; }
    public String getHostName() { return hostName; }
    public String getOpponentName() { return opponentName; }
    public String getStatus() { return status; }
    public String getIsPrivate() { return isPrivate; }
    public String getPassword() { return password; }
    public GameInfo getGameInfo() { return gameInfo; }


    //방 제목 방장한테 변경한 사안을 알려주기 위함
    public synchronized void setOpponentName(String opponentName) {
        this.opponentName = opponentName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setStatus(String status) { this.status = status; }

    public void setIsPrivate(String isPrivate) {
        this.isPrivate = isPrivate;
    }

    @Override
    public String toString() {
        return "RoomInfo{" +
                "roomTitle='" + roomTitle + '\'' +
                ", hostName='" + hostName + '\'' +
                ", opponentName='" + opponentName + '\'' +
                ", status='" + status + '\'' +
                ", isPrivate='" + isPrivate + '\'' +
                ", password='" + password + '\'' +
                ", gameInfo=" + gameInfo.toString() +
                '}';
    }


}