package Dto;


//메세지 타입 지정
public enum MessageType {

    //그냥 메세지 보낼 때
    STRING_MESSAGE("STRING_MESSAGE"),
    USER_NAME("USER_NAME"),
    USER_EXIT("USER_EXIT"), // 사람 나갈 때
    END_OF_ROUND("END_OF_ROUND"),
    GAME_END("GAME_END"), //게임 끝

    LOADING_TIMER("loading_timer"), // 게임 시작 전에

    //ClientHander 에서 사용하는 메세지
    EXIT("EXIT"),
    CREATE_ROOM("create_room"),
    DELETE_ROOM("delete_room"),
    JOIN_ROOM("join_room"),
    REQUEST_USERLIST("request_user_list"),
    REQUEST_ROOMLIST("REQUEST_ROOMLIST"),
    START_GAME_SIGNAL("START_GAME_SIGNAL"),
    SUBMIT_CARD("SUBMIT_CARD"),

    //ClientStart 에서 사용하는 메세지
    USERLIST("USERLIST"),
    ROOMLIST("ROOMLIST"),
    CREATE_ROOM_COMPLETED("CREATE_ROOM_COMPLETED"),
    JOIN_ROOM_SUCCESS("JOIN_ROOM_SUCCESS"),
    OPPONENT_JOINED("OPPONENT_JOINED"),
    //    START_COUNTDOWN("START_COUNTDOWN"),
    START_GAME("START_GAME"),
    OPPONENT_CARD_SUBMITTED("OPPONENT_CARD_SUBMITTED"),
    ROUND_RESULT("ROUND_RESULT"),


    TUTORIAL_CLOSED("TUTORIAL_CLOSED"),
    TUTORIAL_BOTH_CLOSED("TUTORIAL_BOTH_CLOSED"),
    CARD_SUBMITTED("card_submitted"); // 누군가의 카드가 제출 되었을 때

    private final String value;

    MessageType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
