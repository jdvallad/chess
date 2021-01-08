import java.io.*;
import java.util.*;

import processing.core.*;

public class chessboard {
    PImage[] images;
    String[] sounds;
    public void settings(){
        soundPlayer = new SoundPlayer();
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
        return;
    }
}
