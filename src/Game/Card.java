package Game;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class Card implements Serializable {
    private int number;
    private Image frontImage;
    private Image backImage;
    private boolean isUsed = false;

    public Card(int number) {
        this.number = number;
        try {
            if (number % 2 == 0 && number <=8&& number >= 0) {
                frontImage = ImageIO.read(new File("src/images/BT_" + number + ".png"));
                backImage = ImageIO.read(new File("src/images/BT_back_" + number + ".png"));
            } else {
                frontImage = ImageIO.read(new File("src/images/WT_" + number + ".png"));
                backImage = ImageIO.read(new File("src/images/WT_back_" + number + ".png"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getNumber() {
        return number;
    }

    public boolean isUsed() {
        return isUsed;
    }

    // 카드를 사용한 상태로 설정
    public void submit() {
        isUsed = true;
    }

    // 카드 그리기 메서드
    public void draw(Graphics g, int x, int y, boolean faceUp) {
        if (!isUsed) {
            g.drawImage(faceUp ? frontImage : backImage, x, y, 60, 90, null);
        }
    }
}
