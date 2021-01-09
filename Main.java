import processing.core.*;
import processing.sound.*;

import java.lang.reflect.Array;
import java.util.*;

public class Main extends PApplet {
    float widthP;
    float heightP;
    PImage[] images;
    PImage tempImage;
    String move = "";
    char[][] pieceBoard;
    boolean startupFlag = false;
    ArrayList<String> moves = new ArrayList<>();
    chessboard board;
    SoundFile start;
    SoundFile moveSound;
    SoundFile capture;
    SoundFile error;
    boolean flipped = false;
    boolean visualFlip = false;
    Stockfish stock;

    static public void main(String[] passedArgs) {
        com.sun.javafx.application.PlatformImpl.startup(() -> {
        });
        try {
            String[] appletArgs = new String[]{"--present", "--window-color=#666666", "--stop-color=#cccccc", "Main"};
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
        widthP = (float) displayWidth / 1920f;
        heightP = (float) displayHeight / 1080f;
        images = new PImage[20];
        start = new SoundFile(this, "./data/sounds/start.mp3");
        moveSound = new SoundFile(this, "./data/sounds/move.mp3");
        capture = new SoundFile(this, "./data/sounds/capture.mp3");
        error = new SoundFile(this, "./data/sounds/error.mp3");
        String imageString = "lichess/";
        images[0] = loadImage("./data/images/" + imageString + "wK.png");
        images[1] = loadImage("./data/images/" + imageString + "bK.png");
        images[2] = loadImage("./data/images/" + imageString + "wQ.png");
        images[3] = loadImage("./data/images/" + imageString + "bQ.png");
        images[4] = loadImage("./data/images/" + imageString + "wR.png");
        images[5] = loadImage("./data/images/" + imageString + "bR.png");
        images[6] = loadImage("./data/images/" + imageString + "wB.png");
        images[7] = loadImage("./data/images/" + imageString + "bB.png");
        images[8] = loadImage("./data/images/" + imageString + "wN.png");
        images[9] = loadImage("./data/images/" + imageString + "bN.png");
        images[10] = loadImage("./data/images/" + imageString + "wP.png");
        images[11] = loadImage("./data/images/" + imageString + "bP.png");
        images[12] = loadImage("./data/images/" + imageString + "board.png");
        images[13] = loadImage("./data/images/" + imageString + "filler.png");//rank_top.png");
        images[14] = loadImage("./data/images/" + imageString + "filler.png");//"rank_bottom.png");
        images[15] = loadImage("./data/images/" + imageString + "filler.png");//"file_left.png");
        images[16] = loadImage("./data/images/" + imageString + "filler.png");//"file_right.png");
        images[17] = loadImage("./data/images/" + imageString + "filler.png");//"default_board.png");
        images[18] = loadImage("./data/images/" + imageString + "light_square.png");
        images[19] = loadImage("./data/images/" + imageString + "dark_square.png");
        pieceBoard = new char[8][8];
    }

    public void initialStartup() {
        start.play();
        board = new chessboard();//"8/8/3BB2k/8/8/2K5/8/8 w - - 0 1");
        setFromFEN(board.fen);
        drawScreen();
        tempImage = get();
        startupFlag = true;
        stock = new Stockfish();
    }

    public void draw() {
        if (!startupFlag) {
            initialStartup();
            return;
        }
        if (!board.gameOver) {
            if (board.turn.equals("white"))
                move = getMove(); //stock.ponder(board.fen, 10, 7, 100);
            if (board.turn.equals("black"))
                move = stock.ponder(board.fen, 20, 7, 100);
            if (move.length() == 4) {
                if (board.legalMoves.contains(move)) {
                    boolean capture = board.isCapture(move);
                    board.makeMove(move);
                    playSound(capture);
                    setFromFEN(board.fen);
                    if (board.gameOver)
                        chessboard.print("\r\n" + board.result);
                } else {
                    background(tempImage);
                    error.play();
                }
                move = "";
            }
        }
    }

    public String randomMove(int a) {
        ArrayList<String> temp = new ArrayList<>();
        temp.addAll(board.legalMoves);
        Collections.shuffle(temp);
        if (temp.size() != 0) {
            delay(a);
            return temp.get(0);
        }
        return move;
    }

    public void playSound(boolean b) {
        if (board.legalMoves.size() == 0)
            start.play();
        else if (b)
            capture.play();
        else
            moveSound.play();
    }

    public String moveFromList(int a) {
        delay(a);
        return moves.remove(0);
    }

    public boolean moveReady() {
        return move.length() == 4;
    }

    public boolean moveInMoveList() {
        return moves.size() != 0;
    }

    public String getMove() {
        if (lookingForPickupPiece())
            drawSelectionHighlight();
        if (holdingPiece())
            drawFloatingPiece(move);
        return move;
    }

    public void clear() {
        board.clear();
        setFromFEN(board.fen);
    }

    public void reset() {
        start.play();
        board.reset();
        setFromFEN(board.fen);
    }

    public void setFromFEN(String f) {
        String[] fen = f.split("/");
        for (int i = 0; i < 7; i++) {
            int index = 0;
            for (char c : fen[i].toCharArray()) {
                if (c >= '1' && c <= '8') {
                    int t = Integer.parseInt("" + c);
                    for (int q = 0; q < t; q++) {
                        pieceBoard[i][index] = ' ';
                        index++;
                    }
                } else {
                    pieceBoard[i][index] = c;
                    index++;
                }
            }
        }
        String[] last = fen[7].split(" ");
        int index = 0;
        for (char c : last[0].toCharArray()) {
            if (c >= '1' && c <= '8') {
                int t = Integer.parseInt("" + c);
                for (int i = 0; i < t; i++) {
                    pieceBoard[7][index] = ' ';
                    index++;
                }
            } else {
                pieceBoard[7][index] = c;
                index++;
            }
        }
        if (visualFlip) {
            flipped = last[1].equals("b");
        }
        drawScreen();
        tempImage = get();
    }

    public boolean lookingForPickupPiece() {
        int x = (int) ((((mouseY)) - 28 * heightP) / (128 * heightP));
        int y = (int) ((((mouseX)) - 448 * widthP) / (128 * widthP));
        if (flipped)
            return mouseOnBoard() && ((!mousePressed) && move.length() == 0) && (pieceBoard[7 - x][7 - y] != ' ');
        else
            return mouseOnBoard() && ((!mousePressed) && move.length() == 0) && (pieceBoard[x][y] != ' ');
    }

    public boolean holdingPiece() {
        return mousePressed && mouseButton == LEFT && move.length() == 2;
    }

    public void drawSelectionHighlight() {
        background(tempImage);
        tempImage = get();
        int x = (int) ((((mouseX)) - 448 * widthP) / (128 * widthP));
        int y = (int) ((((mouseY)) - 28 * heightP) / (128 * heightP));
        if (flipped) {
            x = 7 - x;
            y = 7 - y;
        }
        String s = "" + ((char) (x + 97)) + (8 - y);

        showLegalMoves(s);
    }

    public boolean mouseOnBoard() {
        return 448 * widthP < ((mouseX)) && ((mouseX)) < 1472 * widthP && 28 * heightP < ((mouseY)) && ((mouseY)) < 1052 * heightP;
    }

    public void keyPressed() {
        if (key == ESC) {
            key = 0;
            reset();
            return;
        }
        if (keyCode == LEFT) {
            board.rollback();
            setFromFEN(board.fen);
        }
        if (keyCode == RIGHT) {
            board.rollForward();
            setFromFEN(board.fen);
        }
    }

    public void mouseReleased() {

        if (move.length() == 2) {
            if (mouseOnBoard()) {

                int x = (int) ((((mouseX)) - 448 * widthP) / (128 * widthP));
                int y = (int) ((((mouseY)) - 28 * heightP) / (128 * heightP));
                if (flipped) {
                    x = 7 - x;
                    y = 7 - y;
                }
                char xFile = (char) (97 + x);
                int yRank = 8 - y;
                if ((move.equals("" + xFile + yRank))) {
                    move = "";
                    background(tempImage);
                } else {
                    move += "" + xFile + yRank;
                }
            } else {
                move = "";
                background(tempImage);
            }
        }
    }

    public void mousePressed() {
        if (mouseOnBoard() && mouseButton == LEFT && move.length() == 0) {
            background(tempImage);
            tempImage = get();
            int x = (int) ((((mouseX)) - 448 * widthP) / (128 * widthP));
            int y = (int) ((((mouseY)) - 28 * heightP) / (128 * heightP));
            if (flipped) {
                x = 7 - x;
                y = 7 - y;
            }
            int yRank = 8 - y;
            char xFile = (char) (97 + x);
            if (getPiece("" + xFile + yRank) != ' ')
                move = "" + xFile + yRank;
        }
    }

    public void drawScreen() {
        tint(180);
        strokeWeight(0);
        background((float) (173 * .5), (float) (216 * .5), (float) (230 * .5));
        fill(board.turn.equals("white") ? 180 : 0);
        rect(448 - 20, 28 - 20, 1024 + 40, 1024 + 40);
        image(images[12], 448, 28, 1024, 1024);
        if (!flipped) {
            image(images[15], 448, 28);
            image(images[14], 448, 1052 - 38);
        } else {
            image(images[13], 448, 1052 - 1024);
            image(images[16], 448 - 35 + 1024, 28);
        }
        drawLastMove();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                drawPiece(r, c);
            }
        }
        tempImage = get();
    }

    public void drawLastMove() {
        String lastMove = board.allMovesMade.size() != 0 ? board.allMovesMade.get(board.allMovesMade.size() - 1) : "";
        if (lastMove.length() == 0)
            return;
        fill(250, 247, 39, 30);
        strokeWeight(0);
        int a = convertFileToInt(lastMove.charAt(0));
        int b = 8 - Integer.parseInt("" + lastMove.charAt(1));
        int c = convertFileToInt(lastMove.charAt(2));
        int d = 8 - Integer.parseInt("" + lastMove.charAt(3));
        if (flipped) {
            a = 7 - a;
            b = 7 - b;
            c = 7 - c;
            d = 7 - d;
        }
        rect(447 + 128 * a, 27 + 128 * b, 128, 128);
        rect(447 + 128 * c, 27 + 128 * d, 128, 128);
    }

    public int convertFileToInt(char c) {
        return ((int) c) - 97;
    }

    public void drawPiece(int r, int c) {
        String name = ("" + pieceBoard[r][c]);
        int temp = switch (name.toLowerCase()) {
            case "q" -> 2;
            case "r" -> 4;
            case "b" -> 6;
            case "n" -> 8;
            case "p" -> 10;
            case "k" -> 0;
            default -> -1;
        };
        if (temp == -1) {
            return;
        }
        int team;
        if (Character.isUpperCase(name.charAt(0))) {
            team = 0;
        } else {
            team = 1;
        }
        if (flipped)
            image(images[temp + (team == 0 ? 0 : 1)], 448 + 128 * (7 - c), 28 + 128 * (7 - r), 128, 128);
        else
            image(images[temp + (team == 0 ? 0 : 1)], 448 + 128 * c, 28 + 128 * r, 128, 128);
    }

    public void drawFloatingPiece(String location) {
        String lastMove = board.allMovesMade.size() != 0 ? board.allMovesMade.get(board.allMovesMade.size() - 1) : "";
        int r = 8 - Integer.parseInt("" + location.charAt(1));
        int c = convertFileToInt(location.charAt(0));
        String name = ("" + pieceBoard[r][c]);
        if (flipped) {
            r = 7 - r;
            c = 7 - c;
        }
        int temp = switch (name.toLowerCase()) {
            case "q" -> 2;
            case "r" -> 4;
            case "b" -> 6;
            case "n" -> 8;
            case "p" -> 10;
            case "k" -> 0;
            default -> -1;
        };
        if (temp == -1) {
            return;
        }
        int team;
        if (Character.isUpperCase(name.charAt(0))) {
            team = 0;
        } else {
            team = 1;
        }
        background(tempImage);
        image(images[(r + c) % 2 == 0 ? 18 : 19], 448 + 128 * c, 28 + 128 * r, 128, 128);
        if ((!lastMove.equals("")) && (location.equals(lastMove.substring(0, 2)) || location.equals(lastMove.substring(2)))) {
            fill(250, 247, 39, 30);
            strokeWeight(0);
            rect(447 + 128 * c, 27 + 128 * r, 129, 129);
        }

        int x = (int) ((((mouseX)) - 448 * widthP) / (128 * widthP));
        int y = (int) ((((mouseY)) - 28 * heightP) / (128 * heightP));
        showLegalMoves(location);
        image(images[temp + (team == 0 ? 0 : 1)], mouseX - 64, mouseY - 64, 128, 128);
    }

    public void showLegalMoves(String location) {
        for (String str : board.legalMoves) {
            if (str.substring(0, 2).equals(location)) {
                int r1 = 8 - Integer.parseInt("" + str.charAt(3));
                int c1 = convertFileToInt(str.charAt(2));
                if (flipped) {
                    r1 = 7 - r1;
                    c1 = 7 - c1;
                }
                fill(30, 80);
                strokeWeight(0);
                circle(64 + 447 + 128 * c1, 64 + 27 + 128 * r1, 50);
            }
        }
    }

    public void makeMove(String m) {
        setPiece(m.substring(2), getPiece(m.substring(0, 2)));
        setPiece(m.substring(0, 2), ' ');
        move = "";
        drawScreen();
    }

    public void setPiece(String s, char c) {
        int x = convertFileToInt(s.charAt(0));
        int y = 8 - Integer.parseInt("" + s.charAt(1));
        pieceBoard[y][x] = c;
    }

    public char getPiece(String s) {
        int x = convertFileToInt(s.charAt(0));
        int y = 8 - Integer.parseInt("" + s.charAt(1));
        return pieceBoard[y][x];
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

}