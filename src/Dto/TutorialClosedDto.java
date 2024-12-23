package Dto;

import java.io.Serializable;

public class TutorialClosedDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String roomTitle;
    private String playerName;

    // getters / setters
    public String getRoomTitle() { return roomTitle; }
    public void setRoomTitle(String rt) { this.roomTitle = rt; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String pn) { this.playerName = pn; }
}
