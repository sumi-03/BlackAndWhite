package Dto;

import java.io.Serializable;

public class ObjectDto implements Serializable {
    private MessageType messageType; // 메시지 타입 (Enum)
    private Object data;             // 메시지에 포함된 데이터

    public ObjectDto() {}


    //MessageType은 서버와 클라이언트 간의 정해진 메세지. 데이터 타입이아니다.
    public ObjectDto(MessageType messageType, Object data) {
        this.messageType = messageType;
        this.data = data;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    //Serialize
    @Override
    public String toString() {
        return "ObjectDto{" +
                "messageType=" + messageType +
                ", data=" + data +
                '}';
    }
}