import java.io.*;
import java.util.*;

import processing.core.*;

public class chessboard {

    PImage[] images;
    String[] sounds;
    SoundPlayer soundPlayer;
    boolean turn;
    Main screen;
    float widthP;
    float heightP;
    public chessboard(Main m,PImage[] imgs, String[] snds, float w,float h) {
        screen = m;
        images = imgs;
        sounds = snds;
        turn = true; //true for white, false otherwise
        widthP = w;
        heightP = h;
    }


    public void show() {
        screen.strokeWeight(0);
        screen.background(173, 216, 230);
        screen.fill(turn ? 255 : 0);
        screen.rect(448 - 20, 28 - 20, 1024 + 40, 1024 + 40);
        screen.image(images[17], 448, 28, 1024, 1024);
    }
    public void getMove() {

    }
}
