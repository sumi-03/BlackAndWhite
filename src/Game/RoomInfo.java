package Game;

public class RoomInfo {
    private String roomTitle;
    private String hostName;
    private String opponentName; // 상대방 이름
    private String status;

    public RoomInfo(String roomTitle, String hostName, String status) {
        this.roomTitle = roomTitle;
        this.hostName = hostName;
        this.status = status;
        this.opponentName = "???";
    }

    public String getRoomTitle() { return roomTitle; }
    public String getHostName() { return hostName; }
    public String getOpponentName() { return opponentName; }
    public String getStatus() { return status; }


    public void setOpponentName(String opponentName) { this.opponentName = opponentName; }
    public void setStatus(String status) { this.status = status; }
}
