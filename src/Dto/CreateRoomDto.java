package Dto;

import java.io.Serializable;

public class CreateRoomDto implements Serializable {
    private String roomTitle;    // 방 제목
    private String userName;     // 사용자 이름

    // 기본 생성자
    public CreateRoomDto() {}

    // 생성자
    public CreateRoomDto(String roomTitle, String userName) {
        this.roomTitle = roomTitle;
        this.userName = userName;
    }

    // Getter와 Setter
    public String getRoomTitle() {
        return roomTitle;
    }

    public void setRoomTitle(String roomTitle) {
        this.roomTitle = roomTitle;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "CreateRoomDto{" +
                "roomTitle='" + roomTitle + '\'' +
                ", userName='" + userName + '\'' +
                '}';
    }
}
