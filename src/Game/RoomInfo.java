package Game;

public class RoomInfo {
    private String roomTitle;
    private String hostName;
    private String opponentName; // 상대방 이름
    private String status;

    // 게임 상태 관련 변수
    private int[] submittedCards = new int[2];           // 제출된 카드 번호 저장
    private String[] submittingPlayers = new String[2];  // 카드 제출한 플레이어 이름 저장
    private int submittedCount = 0;                      // 현재 제출된 카드 수

    public RoomInfo(String roomTitle, String hostName, String status) {
        this.roomTitle = roomTitle;
        this.hostName = hostName;
        this.status = status;
        this.opponentName = "???";
    }

    // Getter 메서드
    public String getRoomTitle() { return roomTitle; }
    public String getHostName() { return hostName; }
    public String getOpponentName() { return opponentName; }
    public String getStatus() { return status; }

    // Setter 메서드
    public void setOpponentName(String opponentName) { this.opponentName = opponentName; }
    public void setStatus(String status) { this.status = status; }

    // 카드 제출 메서드
    public boolean submitCard(String playerName, int cardNumber) {
        if (submittedCount < 2) {
            submittingPlayers[submittedCount] = playerName;
            submittedCards[submittedCount] = cardNumber;
            submittedCount++;
            return true; // 카드 제출 성공
        }
        return false; // 카드 제출 실패 (이미 두 장 제출됨)
    }

    // 라운드 승자 결정 메서드
    public String determineRoundWinner() {
        if (submittedCount == 2) {
            String result;
            if (submittedCards[0] > submittedCards[1]) {
                result = "ROUND_RESULT:WINNER:" + submittingPlayers[0];
            } else if (submittedCards[0] < submittedCards[1]) {
                result = "ROUND_RESULT:WINNER:" + submittingPlayers[1];
            } else {
                result = "ROUND_RESULT:DRAW";
            }
            resetRound();
            return result;
        }
        return null; // 아직 두 장이 제출되지 않음
    }

    // 라운드 상태 초기화
    private void resetRound() {
        submittedCount = 0;
        submittingPlayers = new String[2];
        submittedCards = new int[2];
    }
}