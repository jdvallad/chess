import java.io.*;
import java.util.*;

import processing.core.*;

public class chessboard extends PApplet {
    float widthP;
    float heightP;
    PImage[] images;
    String[] sounds;
    SoundPlayer soundPlayer;

    static public void main(String[] passedArgs) {
        com.sun.javafx.application.PlatformImpl.startup(() -> {
        });
        try {
            String[] appletArgs = new String[]{"--present", "--window-color=#666666", "--stop-color=#cccccc", "chessboard"};
            if (passedArgs != null) {
                PApplet.main(concat(appletArgs, passedArgs));
            } else {
                PApplet.main(appletArgs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void settings() {
        fullScreen();
        widthP = (float) ((float) displayWidth) / 1920f;
        heightP = (float) ((float) displayHeight) / 1080f;
        soundPlayer = new SoundPlayer();
        images = new PImage[18];
        sounds = new String[9];
        sounds[0] = ("./data/sounds/game_start.mp3");
        soundPlayer.play(sounds[0]);
        sounds[1] = ("./data/sounds/game_end.mp3");
        sounds[2] = ("./data/sounds/move_self.mp3");
        sounds[3] = ("./data/sounds/move_opponent.mp3");
        sounds[4] = ("./data/sounds/move_capture.mp3");
        sounds[5] = ("./data/sounds/move_check.mp3");
        sounds[6] = ("./data/sounds/move_castle.mp3");
        sounds[7] = ("./data/sounds/move_promote.mp3");
        sounds[8] = ("./data/sounds/move_illegal.mp3");
        images[0] = loadImage("./data/images/king_white.png");
        images[1] = loadImage("./data/images/king_black.png");
        images[2] = loadImage("./data/images/queen_white.png");
        images[3] = loadImage("./data/images/queen_black.png");
        images[4] = loadImage("./data/images/rook_white.png");
        images[5] = loadImage("./data/images/rook_black.png");
        images[6] = loadImage("./data/images/bishop_white.png");
        images[7] = loadImage("./data/images/bishop_black.png");
        images[8] = loadImage("./data/images/knight_white.png");
        images[9] = loadImage("./data/images/knight_black.png");
        images[10] = loadImage("./data/images/pawn_white.png");
        images[11] = loadImage("./data/images/pawn_black.png");
        images[12] = loadImage("./data/images/board.png");
        images[13] = loadImage("./data/images/rank_top.png");
        images[14] = loadImage("./data/images/rank_bottom.png");
        images[15] = loadImage("./data/images/file_left.png");
        images[16] = loadImage("./data/images/file_right.png");
        images[17] = loadImage("./data/images/default_board.png");
    }

    public void rect(float a, float b, float c, float d) {
        super.rect(widthP * a, heightP * b, widthP * c, heightP * d);
    }

    public void textSize(float s) {
        super.textSize(widthP * s);
    }

    public void text(String s, float a, float b) {
        super.text(s, widthP * a, heightP * b);
    }

    public void image(PImage img, float a, float b) {
        super.image(img, widthP * a, heightP * b, widthP * img.width, widthP * img.height);
    }

    public void image(PImage img, float a, float b, float x, float y) {
        super.image(img, widthP * a, heightP * b, widthP * x, widthP * y);
    }
    public void draw
}
