package Dto;

import java.io.Serializable;

//카드 제출할 때 사용
public class CardSubmitDto implements Serializable {
    private static final long serialVersionUID = 1L; // 자동 생성된 직렬화 UID

    private String submitName = "";
    private int cardNum = 99;
    private String roomTitle = "roomTitle";
    private boolean isHost = false; // 호스트 여부

    // 기본 생성자
    public CardSubmitDto() {}

    // 통합된 생성자
    public CardSubmitDto(int cardNum, String roomTitle, String submitName, boolean isHost) {
        this.cardNum = cardNum;
        this.roomTitle = roomTitle;
        this.submitName = submitName;
        this.isHost = isHost;
    }


    // Getter와 Setter
    public int getCardNum() {
        return cardNum;
    }

    public void setCardNum(int cardNum) {
        this.cardNum = cardNum;
    }

    public String getRoomTitle() {
        return roomTitle;
    }

    public void setRoomTitle(String roomTitle) {
        this.roomTitle = roomTitle;
    }

    public void setSubmitName(String name) {
        this.submitName = name;
    }

    public String getSubmitName() {
        return this.submitName;
    }

    public boolean isHost() {
        return isHost;
    }

    public void setHost(boolean isHost) {
        this.isHost = isHost;
    }


    // toString 메서드
    @Override
    public String toString() {
        return "CardSubmitDto{" +
                "cardNum=" + cardNum +
                ", roomTitle='" + roomTitle + '\'' +
                ", submitName='" + submitName + '\'' +
                ", isHost=" + isHost +
                '}';
    }
}
