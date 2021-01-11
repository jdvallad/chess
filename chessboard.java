import processing.core.PApplet;
import processing.core.PImage;
import processing.sound.SoundFile;

import java.util.ArrayList;
import java.util.Collections;

public class chessboard {
    float widthP, heightP;
    PApplet screen;
    PImage[] images;
    PImage tempImage;
    String move = "";
    char[][] pieceBoard = new char[8][8];
    boolean firstDraw = true; //used to run code once on first pass of draw
    chess board = new chess(); //handles logic of chess
    SoundFile start;
    SoundFile end;
    SoundFile moveSound;
    SoundFile capture;
    SoundFile error;
    boolean flipped = false; //changes the orientation of the board
    boolean visualFlip = false; //determines whether flipped will be changed when a move is made
    Stockfish fish = new Stockfish(); //used to get moves from stockfish
    boolean threadRunning = false;
    dogThread dog;
    stockThread stock;
    String data = "default";

    public chessboard(PApplet p) {
        screen = p;
    }

    public void settings() {
        screen.fullScreen();
        widthP = (float) screen.displayWidth / 1920f;
        heightP = (float) screen.displayHeight / 1080f;
        images = new PImage[19];
        start = new SoundFile(screen, "./data/" + data + "/sounds/start.mp3");
        end = new SoundFile(screen, "./data/" + data + "/sounds/end.mp3");
        moveSound = new SoundFile(screen, "./data/" + data + "/sounds/move.mp3");
        capture = new SoundFile(screen, "./data/" + data + "/sounds/capture.mp3");
        error = new SoundFile(screen, "./data/" + data + "/sounds/error.mp3");
        images[0] = screen.loadImage("./data/" + data + "/images/wK.png");
        images[1] = screen.loadImage("./data/" + data + "/images/bK.png");
        images[2] = screen.loadImage("./data/" + data + "/images/wQ.png");
        images[3] = screen.loadImage("./data/" + data + "/images/bQ.png");
        images[4] = screen.loadImage("./data/" + data + "/images/wR.png");
        images[5] = screen.loadImage("./data/" + data + "/images/bR.png");
        images[6] = screen.loadImage("./data/" + data + "/images/wB.png");
        images[7] = screen.loadImage("./data/" + data + "/images/bB.png");
        images[8] = screen.loadImage("./data/" + data + "/images/wN.png");
        images[9] = screen.loadImage("./data/" + data + "/images/bN.png");
        images[10] = screen.loadImage("./data/" + data + "/images/wP.png");
        images[11] = screen.loadImage("./data/" + data + "/images/bP.png");
        images[12] = screen.loadImage("./data/" + data + "/images/board.png");
        images[13] = screen.loadImage("./data/" + data + "/images/rank_top.png");
        images[14] = screen.loadImage("./data/" + data + "/images/rank_bottom.png");
        images[15] = screen.loadImage("./data/" + data + "/images/file_left.png");
        images[16] = screen.loadImage("./data/" + data + "/images/file_right.png");
        images[17] = screen.loadImage("./data/" + data + "/images/light_square.png");
        images[18] = screen.loadImage("./data/" + data + "/images/dark_square.png");
    }
    public void draw() {
        if (firstDraw) {
            start.play();
            setFromFEN(board.fen);
            tempImage = screen.get();
            //chess.println(Evaluation.evaluate(board));
            firstDraw = false;
            return;
        }
        if (!board.gameOver) { // game is still running
            move = getMove(); // move is created, either from user input or from bot
            if (move.length() == 4) { // ensures move is complete. (When user is holding piece, move is of form 'e2'
                if (board.legalMoves.contains(move)) { // ensures move is a legal move
                    boolean capture = board.isCapture(move); //used to play correct sound with move
                    board.makeMove(move); //make the move on the logical board
                    threadRunning = false;
                    playSound(capture); //play the correct sound
                    setFromFEN(board.fen); //update GUI board from logical board fen.
                    chess.println(Evaluation.evaluate(board));
                    //  chess.println(Evaluation.endGame(board));
                    if (board.gameOver) {// move made ends the game
                        chess.print("\r\n" + board.result); //show game results (want to move out of console)
                        tintScreen(); //darkens the screen to show game over.
                        tempImage = screen.get(); //updates tempImage
                    }
                    move = ""; //reset move
                    return;
                }
                //This is only reached if move attempted wasn't a legal move
                screen.background(tempImage); //This ensures piece being held snaps back to position
                error.play(); //play illegal move sound
                move = ""; //reset move
            }
        }
    }
    public void tintScreen() {
        screen.fill(30, 80);
        screen.rect(0, 0, 1920, 1080);
    }

    public String randomMove(int a) {
        ArrayList<String> temp = new ArrayList<>(board.legalMoves);
        Collections.shuffle(temp);
        if (temp.size() != 0) {
            screen.delay(a);
            return temp.get(0);
        }
        return move;
    }

    public void playSound(boolean b) {
        if (board.legalMoves.size() == 0)
            end.play();
        else if (b)
            capture.play();
        else
            moveSound.play();
    }

    public String getMove() {
        if (lookingForPickupPiece()) //This will show legal moves for piece currently hovered over
            drawSelectionHighlight();

        if (holdingPiece())
            drawFloatingPiece(move); //This will show legal moves for held piece as well as render it as floating

        if (board.turn.equals("white"))
            return move;
        if (board.turn.equals("black"))
            return dogMove(1);
        return "";
    }

    public String dogMove(int depth) {
        if (!threadRunning) {
            dog = new dogThread(board, depth);
            dog.start();
            threadRunning = true;
        }
        String[] res = dog.move();
        if (!res[0].equals("")) {
            // print(res);
            return res[0];
        } else
            return new String[]{move, "", ""}[0];
    }

    public String stockMove(int depth, int diff) {
        if (!threadRunning) {
            stock = new stockThread(board, fish, depth, diff);
            stock.start();
            threadRunning = true;
        }
        String res = stock.move();
        if (!res.equals(""))
            return res;
        else
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
        tempImage = screen.get();
    }

    public boolean lookingForPickupPiece() {
        int x = (int) ((((screen.mouseY)) - 28 * heightP) / (128 * heightP));
        int y = (int) ((((screen.mouseX)) - 448 * widthP) / (128 * widthP));
        if (flipped)
            return mouseOnBoard() && ((!screen.mousePressed) && move.length() == 0) && (pieceBoard[7 - x][7 - y] != ' ');
        else
            return mouseOnBoard() && ((!screen.mousePressed) && move.length() == 0) && (pieceBoard[x][y] != ' ');
    }

    public boolean holdingPiece() {
        return screen.mousePressed && screen.mouseButton == screen.LEFT && move.length() == 2;
    }

    public void drawSelectionHighlight() {
        screen.background(tempImage);
        tempImage = screen.get();
        int x = (int) ((((screen.mouseX)) - 448 * widthP) / (128 * widthP));
        int y = (int) ((((screen.mouseY)) - 28 * heightP) / (128 * heightP));
        if (flipped) {
            x = 7 - x;
            y = 7 - y;
        }
        String s = "" + ((char) (x + 97)) + (8 - y);

        showLegalMoves(s);
    }

    public boolean mouseOnBoard() {
        return 448 * widthP < ((screen.mouseX)) && ((screen.mouseX)) < 1472 * widthP && 28 * heightP < ((screen.mouseY)) && ((screen.mouseY)) < 1052 * heightP;
    }

    public void keyPressed() {
        if (screen.key == screen.ESC) {
            screen.key = 0;
            reset();
            return;
        }
        if (screen.key == ' ') {
            flipped = !flipped;
            drawScreen();
            tempImage = screen.get();
            return;
        }
        if (screen.keyCode == screen.LEFT) {
            board.rollback(2);
            setFromFEN(board.fen);
        }
        if (screen.keyCode == screen.RIGHT) {
            board.rollForward(2);
            setFromFEN(board.fen);
        }
    }

    public void mouseReleased() {

        if (move.length() == 2) {
            if (mouseOnBoard()) {

                int x = (int) ((((screen.mouseX)) - 448 * widthP) / (128 * widthP));
                int y = (int) ((((screen.mouseY)) - 28 * heightP) / (128 * heightP));
                if (flipped) {
                    x = 7 - x;
                    y = 7 - y;
                }
                char xFile = (char) (97 + x);
                int yRank = 8 - y;
                if ((move.equals("" + xFile + yRank))) {
                    move = "";
                    screen.background(tempImage);
                } else {
                    move += "" + xFile + yRank;
                }
            } else {
                move = "";
                screen.background(tempImage);
            }
        }
    }

    public void mousePressed() {
        if (mouseOnBoard() && screen.mouseButton == screen.LEFT && move.length() == 0) {
            screen.background(tempImage);
            tempImage = screen.get();
            int x = (int) ((((screen.mouseX)) - 448 * widthP) / (128 * widthP));
            int y = (int) ((((screen.mouseY)) - 28 * heightP) / (128 * heightP));
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
        screen.tint(180);
        screen.strokeWeight(0);
        screen.background((float) (173 * .5), (float) (216 * .5), (float) (230 * .5));
        screen.fill(board.turn.equals("white") ? 180 : 0);
        screen.rect(448 - 20, 28 - 20, 1024 + 40, 1024 + 40);
        screen.image(images[12], 448, 28, 1024, 1024);
        if (!flipped) {
            screen.image(images[15], 448, 28);
            screen.image(images[14], 448, 1052 - 38);
        } else {
            screen.image(images[13], 448, 1052 - 1024);
            screen.image(images[16], 448 - 35 + 1024, 28);
        }
        drawLastMove();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                drawPiece(r, c);
            }
        }
        tempImage = screen.get();
    }

    public void drawLastMove() {
        String lastMove = board.allMovesMade.size() != 0 ? board.allMovesMade.get(board.allMovesMade.size() - 1) : "";
        if (lastMove.length() == 0)
            return;
        screen.fill(250, 247, 39, 30);
        screen.strokeWeight(0);
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
        screen.rect(447 + 128 * a, 27 + 128 * b, 128, 128);
        screen.rect(447 + 128 * c, 27 + 128 * d, 128, 128);
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
            screen.image(images[temp + (team == 0 ? 0 : 1)], 448 + 128 * (7 - c), 28 + 128 * (7 - r), 128, 128);
        else
            screen.image(images[temp + (team == 0 ? 0 : 1)], 448 + 128 * c, 28 + 128 * r, 128, 128);
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
        screen.background(tempImage);
        screen.image(images[(r + c) % 2 == 0 ? 17 : 18], 448 + 128 * c, 28 + 128 * r, 128, 128);
        if ((!lastMove.equals("")) && (location.equals(lastMove.substring(0, 2)) || location.equals(lastMove.substring(2)))) {
            screen.fill(250, 247, 39, 30);
            screen.strokeWeight(0);
            screen.rect(447 + 128 * c, 27 + 128 * r, 129, 129);
        }

        showLegalMoves(location);
        screen.image(images[temp + (team == 0 ? 0 : 1)], screen.mouseX - 64, screen.mouseY - 64, 128, 128);
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
                screen.fill(30, 80);
                screen.strokeWeight(0);
                screen.circle(64 + 447 + 128 * c1, 64 + 27 + 128 * r1, 50);
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

}
