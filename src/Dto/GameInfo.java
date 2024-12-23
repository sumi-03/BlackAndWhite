package Dto;

import java.io.Serializable;

public class GameInfo implements Serializable {

    private static final long serialVersionUID = 1L; //자동생성

    private int hostPoint = 0; // 호스트 플레이어 점수
    private int opponentPoint = 0; // 상대 플레이어 점수
    private int currentGameSet = 0; // 현재 라운드 번호
    private String winner = ""; // 승자 이름
    private String lastSubmitted = ""; // 마지막으로 카드 제출한 플레이어 이름
    private int hostCard = -1; // 호스트가 제출한 카드 (-1: 미제출)
    private int opponentCard = -1; // 상대방이 제출한 카드 (-1: 미제출)
    private String currentFirstPlayer = ""; //이번 라운드에 선공인 플레이어 이름
    private boolean hostTutorialClosed = false; //host가 튜토리얼 창을 닫았나
    private boolean opponentTutorialClosed = false; //opponent가 튜토리얼 창을 닫았나

    // 마지막으로 카드를 제출한 플레이어 이름 가져오기
    public String getLastSubmitted() { return this.lastSubmitted; }
    public void setLastSubmitted(String submitted) { this.lastSubmitted = submitted; }
    public void setHostCard(int card) { this.hostCard = card; }
    public void setOpponentCard(int card) { this.opponentCard = card; }
    public int getHostCard() { return hostCard; }
    public int getOpponentCard() { return opponentCard; }
    public boolean isCardsSubmitted() {
        return hostCard != -1 && opponentCard != -1;
    }
    public void resetCards() {
        hostCard = -1;
        opponentCard = -1;
    }
    public void resetGame() {
        hostPoint = 0;
        opponentPoint = 0;
        resetCards();
        currentGameSet = 0;
        winner = "";
        currentFirstPlayer = "";
    }

    // 호스트 플레이어의 점수 가져오기
    public int getHostPoint() {
        return hostPoint;
    }

    // 호스트 플레이어의 점수 설정
    public void setHostPoint(int hostPoint) {
        this.hostPoint = hostPoint;
    }

    // 상대방 플레이어의 점수 가져오기
    public int getOpponentPoint() {
        return opponentPoint;
    }

    // 상대방 플레이어의 점수 설정
    public void setOpponentPoint(int opponentPoint) {
        this.opponentPoint = opponentPoint;
    }

    // 현재 게임 라운드 번호 가져오기
    public int getCurrentGameSet() {
        return currentGameSet;
    }

    // 현재 게임 라운드 번호 설정
    public void setCurrentGameSet(int currentGameSet) {
        this.currentGameSet = currentGameSet;
    }

    // 현재 게임 승자 이름 가져오기
    public String getWinner() {
        return winner;
    }

    // 현재 게임 승자 이름 설정
    public void setWinner(String winner) {
        this.winner = winner;
    }

    // 호스트의 점수 1점 증가
    public void incrementHostPoint() {
        hostPoint++;
    }

    // 상대방의 점수 1점 증가
    public void incrementOpponentPoint() {
        opponentPoint++;
    }

    // 다음 라운드로 이동
    public void nextGameSet() {
        currentGameSet++;
    }

    // GameInfo 객체의 현재 상태를 문자열로 반환 (디버깅용)
    @Override
    public String toString() {
        return "GameInfo{" +
                "hostPoint=" + hostPoint +
                ", opponentPoint=" + opponentPoint +
                ", currentGameSet=" + currentGameSet +
                ", winner='" + winner + '\'' +
                ", currentFirstPlayer='" + currentFirstPlayer + '\'' + // 디버깅용
                '}';
    }

    public String getCurrentFirstPlayer() {
        return currentFirstPlayer;
    }

    public void setCurrentFirstPlayer(String currentFirstPlayer) {
        this.currentFirstPlayer = currentFirstPlayer;
    }

    public void setHostTutorialClosed(boolean closed) { this.hostTutorialClosed = closed; }
    public boolean isHostTutorialClosed() { return this.hostTutorialClosed; }

    public void setOpponentTutorialClosed(boolean closed) { this.opponentTutorialClosed = closed; }
    public boolean isOpponentTutorialClosed() { return this.opponentTutorialClosed; }
}
