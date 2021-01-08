import java.io.*;
import java.util.*;

import processing.core.*;

public class MainOld extends PApplet {
    float widthP;
    float heightP;
    boolean setupFlag = false;
    boolean gotMove, gameRunning, takenTurn, gameStarted, paused, squareSelected, fishMove;
    ArrayList<String> moveList;
    int moveLength, depth1, depth2, gameMode;
    PImage[] images;
    String[] sounds;
    int[] currentMove;
    Board board;
    String boardString;
    ArrayList<String> game;
    ArrayList<Board> rollback;
    int index;
    boolean rotation; //determines which side of the board is being shown to player
    int counter;
    boolean pressed;
    final String FINAL_BOARD_STRING = "";//PJAAAUqgAAIqkAABK5AAAT6QAAFqkAABKqAAAiyQAAFAAA8=";
    boolean menu;
    boolean focus;
    int level;
    String playerOne; //Human,Stockfish, or Dogfish
    String playerTwo;//Human,Stockfish, or Dogfish
    String chessVariant;
    int displaySave;
    SoundPlayer soundPlayer;

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
        widthP = (float) ((float) displayWidth) / 1920f;
        heightP = (float) ((float) displayHeight) / 1080f;
        depth1 = depth2 = 1;
        playerOne = playerTwo = "Human";
        counter = 0;
        chessVariant = "Normal";
        displaySave = 0;
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

    public void initialSetup() {

        pressed = true;
        focus = false;
        level = 0;
        rotation = menu = true;
        fishMove = gotMove = gameRunning = takenTurn = gameStarted = paused = squareSelected = false;
        moveList = new ArrayList<>();
        currentMove = new int[4];
        gameMode = 1;
        moveLength = 0;
        index = 1;
        background(0);
        boardString = FINAL_BOARD_STRING;
        board = new Board(boardString);
        drawSimpleScreen();
        drawStartScreen();
        setupFlag = true;
    }

    public void draw() {
        pressed = true;
        if (!setupFlag) {
            initialSetup();
        }
        if (!paused) {
            if (gameRunning) {
                switch (gameMode) {
                    case 0 -> playChess();
                    case 1 -> developerMode();
                    case 2 -> replayMode();
                }
            }
        }
    }

    public void developerMode() {
        if (!gotMove) {
            getDeveloperMove();
        }
        if (gotMove) {
            board.developerUpdate(currentMove);
            drawDeveloperScreen();
            gotMove = false;
            if (squareSelected) {
                fill(13, 213, 252, 80);
                strokeWeight(0);
                rect(447 + 128 * currentMove[0], 27 + 128 * currentMove[1], 129, 129);
            }
        }
    }

    public void drawDeveloperScreen() {
        strokeWeight(0);
        background(173, 216, 230);
        fill(board.turn ? 255 : 0);
        rect(448 - 20, 28 - 20, 1024 + 40, 1024 + 40);
        image(images[12], 448, 28, 1024, 1024);
        image(images[15], 448, 28);
        image(images[14], 448, 1052 - 38);
        for (int r = 0; r < board.pieces.length; r++) {
            for (int c = 0; c < board.pieces[r].length; c++) {
                Piece temp = board.pieces[r][c];
                drawPiece(temp.name, temp.team, r, c);
            }
        }
        drawPiece("king", 0, -2, 1);
        drawPiece("queen", 0, -2, 2);
        drawPiece("rook", 0, -2, 3);
        drawPiece("bishop", 0, -2, 4);
        drawPiece("knight", 0, -2, 5);
        drawPiece("pawn", 0, -2, 6);
        drawPiece("king", 1, -3, 1);
        drawPiece("queen", 1, -3, 2);
        drawPiece("rook", 1, -3, 3);
        drawPiece("bishop", 1, -3, 4);
        drawPiece("knight", 1, -3, 5);
        drawPiece("pawn", 1, -3, 6);
        textSize(26);
        fill(0);
        text(
                "LEFT CLICK\r\n" +
                        "     select/place piece.\r\n" +
                        "RIGHT CLICK\r\n" +
                        "     deselect/delete piece.\r\n" +
                        "SPACE\r\n" +
                        "     switch starting side.\r\n"
                        + "ENTER\r\n" +
                        "   save current board.\r\n" +
                        "\r\n" +
                        "Play saved board under  \r\n" +
                        "Custom variant in Play Mode."
                , 1500, 50);
        if (displaySave > 0) {
            textSize(28);
            text("Current Board saved!", 1640, 1065);
            displaySave--;
        }
    }

    public void getDeveloperMove() {
        int x = -3 + (int) (+(((int) (mouseX)) - (int) 64 * widthP) / ((int) 128 * widthP));
        int y = (int) ((((int) (mouseY)) - 28 * heightP) / (128 * heightP));
        //System.out.println(x+", "+y);
        if (mousePressed && (mouseOnBoard() || mouseOnSideBoard()) && mouseButton == LEFT) {
            if (squareSelected && (x == currentMove[0] && y == currentMove[1]) && (x > 0)) {
                drawDeveloperScreen();
                currentMove[2] = -5;
                currentMove[3] = -5;
                squareSelected = false;
                return;
            }
            if ((!squareSelected)) {
                if (x < 0 || !board.pieces[x][y].isSquare()) {
                    drawDeveloperScreen();
                    fill(13, 213, 252, 80);
                    strokeWeight(0);
                    currentMove[0] = x;
                    currentMove[1] = y;
                    rect(447 + 128 * currentMove[0], 27 + 128 * currentMove[1], 129, 129);
                    squareSelected = true;
                    return;
                }
            }
            if (squareSelected && x < 0) {
                drawDeveloperScreen();
                currentMove[0] = x;
                currentMove[1] = y;
                fill(13, 213, 252, 80);
                strokeWeight(0);
                rect(447 + 128 * currentMove[0], 27 + 128 * currentMove[1], 129, 129);
                return;
            }
            if (squareSelected && !(x == currentMove[0] && y == currentMove[1])) {
                currentMove[2] = x;
                currentMove[3] = y;
                gotMove = true;
                return;
            }
        }
        if (mousePressed && (mouseOnBoard() || mouseOnSideBoard()) && mouseButton == RIGHT) {
            if (!squareSelected) {
                currentMove[0] = x;
                currentMove[1] = y;
            }
            if (currentMove[0] == x && currentMove[1] == y) {
                currentMove[2] = -3;
                currentMove[3] = 1;
                gotMove = true;
            } else {
                currentMove[2] = -5;
                currentMove[3] = -5;
                drawDeveloperScreen();
            }
            squareSelected = false;
        }
    }

    public boolean mouseOnBoard() {
        return widthP * 448 < ((int) (mouseX)) && ((int) (mouseX)) < 1472 * widthP && 28 * heightP < ((int) (mouseY)) && ((int) (mouseY)) < 1052 * heightP;
    }

    public boolean mouseOnSideBoard() {
        return widthP * 73 < ((int) (mouseX)) && ((int) (mouseX)) < widthP * 329 && 156 * heightP < ((int) (mouseY)) && ((int) (mouseY)) < 924 * heightP;
    }

    public void replayMode() {
        pressed = true;
    }

    public void playChess() {
        if (playerOne.equals("Human") && playerTwo.equals("Human")) {
            normalChess();
            return;
        }
        if ((!playerOne.equals("Human")) && (!playerTwo.equals("Human"))) {
            autoChess();
            return;
        }
        fightAI();
    }

    public void autoChess() {
        if (!gotMove) {
            if (playerOne.equals("Stockfish")) {
                executeMove(Stockfish.ponder(board, depth1, 50, 100));
            }
            if (playerOne.equals("Dogfish")) {
                executeMove(Dogfish.ponder(board, depth1, -5000, 5000));
            }
            gotMove = true;
            return;
        }
        if (playerTwo.equals("Stockfish")) {
            executeMove(Stockfish.ponder(board, depth2, 50, 100));
        }
        if (playerTwo.equals("Dogfish")) {
            executeMove(Dogfish.ponder(board, depth2, -5000, 5000));
        }
        gotMove = false;
    }

    public void fightAI() {
        if (!takenTurn) {
            rotation = false;
            if (playerOne.equals("Stockfish")) {
                executeMove(Stockfish.ponder(board, depth1, 50, 100));
            }
            if (playerOne.equals("Dogfish")) {
                executeMove(Dogfish.ponder(board, depth1, -5000, 5000));
            }
            takenTurn = true;
        }
        gotMove = false;
        getMove();
        if (fishMove) {
            int[] tempMove = null;
            if (playerOne.equals("Stockfish")) {
                tempMove = Stockfish.ponder(board, depth1, 50, 100);
            }
            if (playerTwo.equals("Stockfish")) {
                tempMove = Stockfish.ponder(board, 5 * depth2, 50, 100);
            }
            if (playerOne.equals("Dogfish")) {
                tempMove = Dogfish.ponder(board, depth1, -5000, 5000);
            }
            if (playerTwo.equals("Dogfish")) {
                tempMove = Dogfish.ponder(board, depth2, -5000, 5000);
            }
            assert tempMove != null;
            if (tempMove[0] != -1) {
                executeMove(tempMove);
                fishMove = false;
            }
        }
        if (gotMove) {
            executeMove(currentMove);
            fishMove = true;
        } else {
            fishMove = false;
        }
    }

    public void normalChess() {
        if (!gotMove) {
            getMove();
        } else {
            rotation = !rotation;
            executeMove(currentMove);
        }
    }

    public void getMove() {
        if (displaySave > 0) {
            displaySave--;
            int[] temp = board.kingPosition();
            if (!rotation) {
                temp[0] = 7 - temp[0];
                temp[1] = 7 - temp[1];
            }
            if ((displaySave % 3) == 0) {
                fill(251, 43, 17, 100);
                strokeWeight(0);
                if (displaySave == 0) {
                    fill(13, 213, 252, 80);
                }
                rect(447 + 128 * temp[0], 27 + 128 * temp[1], 129, 129);
            } else {
                drawScreen();
            }
        }
        int x_real = (int) ((((int) (mouseX)) - 448 * widthP) / (128 * widthP));
        int x = x_real;
        if (!rotation) {
            x = 7 - x;
        }
        int y_real = (int) ((((int) (mouseY)) - 28 * heightP) / (128 * heightP));
        int y = y_real;
        if (!rotation) {
            y = 7 - y;
        }
        if (mousePressed && 448 * widthP < ((int) (mouseX)) && ((int) (mouseX)) < 1472 * widthP && 28 * heightP < ((int) (mouseY)) && ((int) (mouseY)) < 1052 * heightP && mouseButton == LEFT) {
            if (((board.pieces[x][y].team == 0 && board.turn) || (board.pieces[x][y].team == 1 && !board.turn))) {
                drawScreen();
                fill(13, 213, 252, 80);
                strokeWeight(0);
                rect(447 + 128 * x_real, 27 + 128 * y_real, 129, 129);
                currentMove[0] = x;
                currentMove[1] = y;
                squareSelected = true;
            }
            if (squareSelected) {
                if (((board.pieces[x][y].team != 0 && board.turn) || (board.pieces[x][y].team != 1 && !board.turn))) {
                    int temp1 = currentMove[2];
                    int temp2 = currentMove[3];
                    currentMove[2] = x;
                    currentMove[3] = y;
                    if (legalMove(currentMove)) {
                        gotMove = true;
                        squareSelected = false;
                        return;
                    }
                    if ((!board.pieces[currentMove[2]][currentMove[3]].isLegalMove(board, currentMove[0], currentMove[1])) && (!board.nextBoard(currentMove).futureInCheck())) {
                        currentMove[2] = temp1;
                        currentMove[3] = temp2;
                        squareSelected = false;
                        return;
                    }
                    currentMove[2] = x;
                    currentMove[3] = y;
                    if (displaySave == 0 && board.pieces[currentMove[0]][currentMove[1]].isLegalMove(board, currentMove[2], currentMove[3]) && board.nextBoard(currentMove).futureInCheck()) {
                        int[] temp = board.kingPosition();
                        if (!rotation) {
                            temp[0] = 7 - temp[0];
                            temp[1] = 7 - temp[1];
                        }
                        displaySave = 8;
                        fill(251, 43, 17, 100);
                        strokeWeight(0);
                        rect(447 + 128 * temp[0], 27 + 128 * temp[1], 129, 129);
                        soundPlayer.play(sounds[8]);
                        currentMove[2] = temp1;
                        currentMove[3] = temp2;
                        return;
                    }
                }
            }
        }
    }

    public void playSound(String str) {
        if (str.charAt(str.length() - 1) == '#') {
            soundPlayer.play(sounds[1]);
            return;
        }
        if (str.charAt(str.length() - 1) == '+') {
            soundPlayer.play(sounds[5]);
            return;
        }
        if (str.contains("=")) {
            soundPlayer.play(sounds[7]);
            return;
        }
        if (str.contains("x")) {
            soundPlayer.play(sounds[4]);
            return;
        }
        if (str.contains("O")) {
            soundPlayer.play(sounds[6]);
            return;
        }
        if (rotation && board.turn || ((!rotation) && (!board.turn))) {
            soundPlayer.play(sounds[2]);
        } else {
            soundPlayer.play(sounds[3]);
        }
    }
    private boolean legalMove(int[] move) {
        String s = "" + move[0] + " " + move[1] + " " + move[2] + " " + move[3] + " " + board.pieces[move[0]][move[1]].name + " " + board.pieces[move[2]][move[3]].name;
        return board.legalMoves.contains(s);
    }
    public void executeMove(int[] move) {
        String str = board.getMoveName(move);
        if (board.turn) {
            moveLength = str.length();
            str = "\r\n   " + str;
        } else {
            str = "              ".substring(moveLength) + str;
        }
        moveList.add(str);
        board.makeMove(move);
        playSound(str);
        if (gameMode == 0) {
            game.add(board.getName(move));
        }
        drawScreen();
        if (board.gameResult != -1 && gameMode != -1) {
            endGame();
        }
        gotMove = false;
    }

    public void saveGame() {
        if (game.size() > 0) {
            try {
                FileOutputStream fileOut =
                        new FileOutputStream("./data/games/game.ser");
                ObjectOutputStream out = new ObjectOutputStream(fileOut);
                out.writeObject(game);
                out.close();
                fileOut.close();
                // System.out.println("Game saved to game.ser");
            } catch (IOException i) {
                i.printStackTrace();
            }
        }
    }

    public void loadGame() {
        try {
            FileInputStream fis = new FileInputStream("./data/games/game.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            game = (ArrayList<String>) ois.readObject();
            ois.close();
            fis.close();
            rollback = new ArrayList<>();
            rollback.add(new Board(game.get(0)));
            moveList = new ArrayList<>();
            for (int i = 1; i < game.size(); i++) {
                Board b = new Board(rollback.get(rollback.size() - 1));
                String str = b.getMoveName(b.getMove(game.get(i)));
                if (b.turn) {
                    moveLength = str.length();
                    str = "\r\n   " + str;
                } else {
                    str = "              ".substring(moveLength) + str;
                }
                moveList.add(str);
                b.makeMove(b.getMove(game.get(i)));
                rollback.add(b);
            }
            String result = "";
            (rollback.get(rollback.size() - 1)).evaluate();
            switch (rollback.get(rollback.size() - 1).gameResult) {
                case -1:
                    break;
                case 0:
                    result = ((rollback.get(rollback.size() - 1)).turn ? "Black" : "White") + " wins!";
                    break;
                case 1:
                    result = "Stalemate!";
                    break;
                case 2:
                    result = "Draw by 50 move rule!";
                    break;
                case 3:
                    result = "Draw by 5-fold repetition!";
                    break;
            }
            if (!result.equals("")) {
                moveList.add("\r\n");
                moveList.add(result);
            }
            //   System.out.println("Game from game.ser loaded!");
            boardString = game.get(0);
        } catch (Exception e) {
            //  System.out.println("Class not found");
            e.printStackTrace();
        }
    }

    public void endGame() {
        String result = switch (board.gameResult) {
            case 0 -> (board.turn ? "Black" : "White") + " wins!";
            case 1 -> "Stalemate!";
            case 2 -> "Draw by 50 move rule!";
            case 3 -> "Draw by 5-fold repetition!";
            default -> "";
        };
        moveList.add("\r\n");
        moveList.add(result);
        drawScreen();
        if (gameMode == 0) {
            saveGame();
        }
        gameRunning = false;
    }


    public void drawSimpleScreen() {
        strokeWeight(0);
        background(173, 216, 230);
        fill(board.turn ? 255 : 0);
        rect(448 - 20, 28 - 20, 1024 + 40, 1024 + 40);
        image(images[17], 448, 28, 1024, 1024);
    }

    public void drawScreen() {
        strokeWeight(0);
        background(173, 216, 230);
        fill(board.turn ? 255 : 0);
        rect(448 - 20, 28 - 20, 1024 + 40, 1024 + 40);
        image(images[12], 448, 28, 1024, 1024);
        if (rotation) {
            image(images[15], 448, 28);
            image(images[14], 448, 1052 - 38);
        } else {
            image(images[13], 448, 1052 - 1024);
            image(images[16], 448 - 35 + 1024, 28);
        }
        for (int r = 0; r < board.pieces.length; r++) {
            for (int c = 0; c < board.pieces[r].length; c++) {
                Piece temp = board.pieces[r][c];
                drawPiece(temp.name, temp.team, r, c);
            }
        }

        drawEvaluation();
        drawMoveList();
        drawPlayers();
        drawLastMove();
    }

    public void drawLastMove() {
        int[] tempMove = board.lastMove;
        if (tempMove[0] == -1 && tempMove[1] == -1 && tempMove[2] == -1 && tempMove[3] == -1) {
            return;
        }
        fill(250, 247, 39, 80);
        strokeWeight(0);
        if (rotation) {
            rect(447 + 128 * tempMove[0], 27 + 128 * tempMove[1], 129, 129);
            rect(447 + 128 * tempMove[2], 27 + 128 * tempMove[3], 129, 129);
        } else {
            rect(447 + 128 * (7 - tempMove[0]), 27 + 128 * (7 - tempMove[1]), 129, 129);
            rect(447 + 128 * (7 - tempMove[2]), 27 + 128 * (7 - tempMove[3]), 129, 129);
        }
    }

    public void drawPlayers() {
        String white = "";
        String black = "";
        switch (gameMode) {
            case 0 -> {
                white = playerOne + (playerOne.equals("Stockfish") ? "(Difficulty = " + depth1 + ")" : "") + (playerOne.equals("Dogfish") ? "(Difficulty = " + depth1 + ")" : "");
                black = playerTwo + (playerTwo.equals("Stockfish") ? "(Difficulty = " + depth2 + ")" : "") + (playerTwo.equals("Dogfish") ? "(Diffculty = " + depth2 + ")" : "");
            }
            case 1 -> {
                white = "";
                black = "";
            }
            case 2 -> {
                black = "";
                white = "";
            }
        }
        textSize(30);
        fill(255);
        text("White: " + white, 20, 105);
        fill(0);
        text("Black: " + black, 20, 145);
    }

    public void drawMoveList() {
        StringBuilder white = new StringBuilder();
        StringBuilder black = new StringBuilder();
        boolean flag = false;
        if (gameMode == 2 && index == rollback.size()) {
            index += 2;
            flag = true;
        }

        if (gameMode == 2) {
            int t = index - 44;
            t /= 2;
            t *= 2;
            for (int i = Math.max(0, t); i < Math.min(index - 1, moveList.size()); i++) {
                if (moveList.get(i).charAt(0) == ' ') {
                    black.append("\r\n").append(moveList.get(i).trim());
                } else {
                    white.append(moveList.get(i));
                }
            }
        } else {
            int t = moveList.size() - 44;
            t /= 2;
            t *= 2;
            for (int i = Math.max(0, t); i < moveList.size(); i++) {
                if (moveList.get(i).charAt(0) == ' ') {
                    black.append("\r\n").append(moveList.get(i).trim());
                } else {
                    white.append(moveList.get(i));
                }
            }
        }
        fill(0);
        textSize(30);
        text("Moves", 1670, 70);
        textSize(25);
        text(white.toString(), 1600, 70);
        text(black.toString(), 1750, 68);
        noFill();
        strokeWeight(4);
        rect(1550, 80, 330, 950);
        if (flag) {
            index -= 2;
        }
    }

    public void drawEvaluation() {
        fill(255, 237, 39);
        textSize(60);
        double eval = board.evaluate();
        int temp = (int) (100. * eval);
        eval = ((double) temp) / 100.;
        text(((eval > 0 ? "+" : "")) + "" + eval, 20, 70);
    }

    public void drawPiece(String name, int team, int r, int c) {
        if (team == -1) {
            return;
        }
        int temp = switch (name) {
            case "queen" -> 2;
            case "rook" -> 4;
            case "bishop" -> 6;
            case "knight" -> 8;
            case "pawn" -> 10;
            default -> 0;
        };
        if (rotation) {
            image(images[temp + (team == 0 ? 0 : 1)], 448 + 128 * r, 28 + 128 * c, 128, 128);
        } else {
            image(images[temp + (team == 0 ? 0 : 1)], 448 + 128 * (7 - r), 28 + 128 * (7 - c), 128, 128);
        }
    }

    public void drawPauseScreen() {
        strokeWeight(10);
        fill(0, 0, 0, 150);
        rect(0, 0, 1920, 1080);
        fill(60, 110, 180, 220);
        rect(468 + 50, 340 + 50, 246 * 4 - 100, 400 - 100);
        textSize(40);
        fill(0);
        text("Press escape to resume", 468 + 280, 500);
        text("Press enter to restart", 468 + 300, 570);
        text("Press space to exit", 508 + 280, 640);
    }

    public void drawStartScreen() {
        fill(0, 180, 180);
        strokeWeight(10);
        int k = 3;
        rect(468 + 246, 10, 246 * 2, 100);
        int rectWidth = 340;
        int rectLength = 500;
        fill(0, 109, 109);
        for (int i = 0; i < k; i++) {
            rect(((1920 - k * rectWidth) / 2) + (rectWidth) * i, 340, rectWidth, rectLength);
        }
        textSize(40);
        fill(0);
        String str1 = "" + (depth1 == 0 ? "Easy" : "") + (depth1 == 1 ? "Intermediate" : "") + (depth1 == 2 ? "Difficult" : "") + (depth1 == 3 ? "Challenging" : "") + (depth1 == 4 ? "Impossible" : "");
        String str2 = "" + (depth2 == 0 ? "Easy" : "") + (depth2 == 1 ? "Intermediate" : "") + (depth2 == 2 ? "Difficult" : "") + (depth2 == 3 ? "Challenging" : "") + (depth2 == 4 ? "Impossible" : "");
        text("Press escape for options", 468 + 246 + 26, 70);
        text("Play Mode", 80 + ((1920 - k * rectWidth) / 2), 400);
        text("Developer Mode", 23 + ((1920 - k * rectWidth) / 2) + (rectWidth), 400);
        text("Replay Mode", 56 + ((1920 - k * rectWidth) / 2) + (rectWidth) * 2, 400);

        textSize(26);
        text("       Player One: " + playerOne, 10 + ((1920 - k * rectWidth) / 2), 400 + 70);
        text("      Difficulty: " + str1, 10 + ((1920 - k * rectWidth) / 2), 400 + 70 * 2);
        text("       Player Two: " + playerTwo, 10 + ((1920 - k * rectWidth) / 2), 400 + 70 * 3);
        text("      Difficulty: " + str2, 10 + ((1920 - k * rectWidth) / 2), 400 + 70 * 4);
        text("          Variant: " + chessVariant, 10 + ((1920 - k * rectWidth) / 2), 400 + 70 * 5);
        text("          Create Custom " + "\r\n" +
                        "         Chess Positions",
                10 + ((1920 - k * rectWidth) / 2) + (rectWidth), 400 + 90);
        text("          Replay the Last" + "\r\n" +
                        "            Played Game",
                10 + ((1920 - k * rectWidth) / 2) + (rectWidth) * 2, 400 + 90);
        if (focus) {
            strokeWeight(10);
            k = 3;
            fill(0, 109, 109);
            rect(468 + 246, 10, 246 * 2, 100);
            for (int i = 0; i < k; i++) {
                rect(((1920 - k * rectWidth) / 2) + (rectWidth) * i, 340, rectWidth, rectLength);
            }
            fill(0, 180, 180);
            rect(((1920 - k * rectWidth) / 2), 340, rectWidth, rectLength);
            fill(0);
            textSize(40);
            fill(0);
            text("Press escape for options", 468 + 246 + 26, 70);
            text("Play Mode", 80 + ((1920 - k * rectWidth) / 2), 400);
            text("Developer Mode", 23 + ((1920 - k * rectWidth) / 2) + (rectWidth), 400);
            text("Replay Mode", 56 + ((1920 - k * rectWidth) / 2) + (rectWidth) * 2, 400);
            textSize(26);
            fill(0);
            if (level == 0) {
                fill(123, 237, 60);
            }
            text("       Player One: " + playerOne, 10 + ((1920 - k * rectWidth) / 2), 400 + 70);
            fill(0);
            if (level == 1) {
                fill(123, 237, 60);
            }
            text("      Difficulty: " + str1, 10 + ((1920 - k * rectWidth) / 2), 400 + 70 * 2);
            fill(0);
            if (level == 2) {
                fill(123, 237, 60);
            }
            text("       Player Two: " + playerTwo, 10 + ((1920 - k * rectWidth) / 2), 400 + 70 * 3);
            fill(0);
            if (level == 3) {
                fill(123, 237, 60);
            }
            text("      Difficulty: " + str2, 10 + ((1920 - k * rectWidth) / 2), 400 + 70 * 4);
            fill(0);
            if (level == 4) {
                fill(123, 237, 60);
            }
            text("          Variant: " + chessVariant, 10 + ((1920 - k * rectWidth) / 2), 400 + 70 * 5);
            fill(0);
        } else {
            fill(0, 180, 180);
            rect(((1920 - k * rectWidth) / 2) + (rectWidth) * counter, 340, rectWidth, rectLength);
            textSize(40);
            fill(0);
            text("Press escape for options", 468 + 246 + 26, 70);
            text("Play Mode", 80 + ((1920 - k * rectWidth) / 2), 400);
            text("Developer Mode", 23 + ((1920 - k * rectWidth) / 2) + (rectWidth), 400);
            text("Replay Mode", 56 + ((1920 - k * rectWidth) / 2) + (rectWidth) * 2, 400);

            textSize(26);
            text("       Player One: " + playerOne, 10 + ((1920 - k * rectWidth) / 2), 400 + 70);
            text("      Difficulty: " + str1, 10 + ((1920 - k * rectWidth) / 2), 400 + 70 * 2);
            text("       Player Two: " + playerTwo, 10 + ((1920 - k * rectWidth) / 2), 400 + 70 * 3);
            text("      Difficulty: " + str2, 10 + ((1920 - k * rectWidth) / 2), 400 + 70 * 4);
            text("          Variant: " + chessVariant, 10 + ((1920 - k * rectWidth) / 2), 400 + 70 * 5);
        }
        text("          Create Custom " + "\r\n" +
                        "         Chess Positions",
                10 + ((1920 - k * rectWidth) / 2) + (rectWidth), 400 + 90);
        text("          Replay the Last" + "\r\n" +
                        "            Played Game",
                10 + ((1920 - k * rectWidth) / 2) + (rectWidth) * 2, 400 + 90);
    }

    public void pauseFunctionality() {
        if (key == ESC) {
            if (menu) {
                drawSimpleScreen();
            } else if (gameMode == 1) {
                drawDeveloperScreen();
            } else {
                drawScreen();
            }
            if (!gameStarted) {
                drawStartScreen();
            }
            if (squareSelected) {
                fill(13, 213, 252, 80);
                strokeWeight(0);
                if (rotation) {
                    rect(447 + 128 * currentMove[0], 27 + 128 * currentMove[1], 129, 129);
                } else {
                    rect(447 + 128 * (7 - currentMove[0]), 27 + 128 * (7 - currentMove[1]), 129, 129);
                }
            }
            paused = !paused;
            key = 0;
            return;
        }
        if (key == ENTER) {
            if (gameRunning && (gameMode == 0)) {
                saveGame();
            }
            initialSetup();
            return;
        }
        //
        if (key == ' ') {
            if (gameRunning && (gameMode == 0)) {
                saveGame();
            }
            gameStarted = true;
            gameRunning = false;
            exit();
            return;
        }
        if (keyCode == UP) {
            //
            return;
        }
        if (keyCode == DOWN) {
            //
            return;
        }
        if (keyCode == LEFT) {
            //
            return;
        }
        if (keyCode == RIGHT) {
            //
        }
    }

    public void focusFunctionality() {
        if (key == ENTER) {
            gameMode = 0;
            soundPlayer.play(sounds[0]);
            boardString = FINAL_BOARD_STRING;
            if (chessVariant.equals("Chess960")) {
                boardString = board.get960();
            }
            if (chessVariant.equals("Custom")) {
                String[] lines = loadStrings("./data/games/gameCode.txt");
                boardString = lines[0];
            }
            if (chessVariant.equals("Panic Attack")) {
                boardString = "MJAMoACQu2AAkAygAJAAAAAAAAEAAkABAA4zAQACQAEAAAA=";
            }
            board = new Board(boardString);
            gameRunning = true;
            gameStarted = true;
            menu = false;
            focus = false;
            takenTurn = playerOne.equals("Human");
            fishMove = false;
            game = new ArrayList<>();
            game.add(boardString);
            drawScreen();
            return;
        }
        if (keyCode == UP) {
            level--;
            if (level == -1) {
                level = 4;
            }
            drawStartScreen();
            return;
        }
        if (keyCode == DOWN) {
            level++;
            if (level == 5) {
                level = 0;
            }
            drawStartScreen();
            return;
        }
        if (keyCode == LEFT) {
            switch (level) {
                case 0 -> {
                    if (playerOne.equals("Human")) {
                        playerOne = ("Dogfish");
                        drawStartScreen();
                        return;
                    }
                    if (playerOne.equals("Dogfish")) {
                        playerOne = ("Stockfish");
                        drawStartScreen();
                        return;
                    }
                    if (playerOne.equals("Stockfish")) {
                        playerOne = ("Human");
                        drawStartScreen();
                        return;
                    }
                    return;
                }
                case 1 -> {
                    depth1 = Math.max(depth1 - 1, 0);
                    drawStartScreen();
                    return;
                }
                case 2 -> {
                    if (playerTwo.equals("Human")) {
                        playerTwo = ("Dogfish");
                        drawStartScreen();
                        return;
                    }
                    if (playerTwo.equals("Dogfish")) {
                        playerTwo = ("Stockfish");
                        drawStartScreen();
                        return;
                    }
                    if (playerTwo.equals("Stockfish")) {
                        playerTwo = ("Human");
                        drawStartScreen();
                        return;
                    }
                    return;
                }
                case 3 -> {
                    depth2 = Math.max(depth2 - 1, 0);
                    drawStartScreen();
                    return;
                }
                case 4 -> {
                    if (chessVariant.equals("Normal")) {
                        chessVariant = "Custom";
                        drawStartScreen();
                        return;
                    }
                    if (chessVariant.equals("Custom")) {
                        chessVariant = "Panic Attack";
                        drawStartScreen();
                        return;
                    }
                    if (chessVariant.equals("Panic Attack")) {
                        chessVariant = "Chess960";
                        drawStartScreen();
                        return;
                    }
                    if (chessVariant.equals("Chess960")) {
                        chessVariant = "Normal";
                        drawStartScreen();
                        return;
                    }
                    return;
                }
            }
            return;
        }
        if (keyCode == RIGHT) {
            switch (level) {
                case 0 -> {
                    if (playerOne.equals("Human")) {
                        playerOne = ("Stockfish");
                        drawStartScreen();
                        return;
                    }
                    if (playerOne.equals("Stockfish")) {
                        playerOne = ("Dogfish");
                        drawStartScreen();
                        return;
                    }
                    if (playerOne.equals("Dogfish")) {
                        playerOne = ("Human");
                        drawStartScreen();
                        return;
                    }
                }
                case 1 -> {
                    depth1 = Math.min(depth1 + 1, 4);
                    drawStartScreen();
                }
                case 2 -> {
                    if (playerTwo.equals("Human")) {
                        playerTwo = ("Stockfish");
                        drawStartScreen();
                        return;
                    }
                    if (playerTwo.equals("Stockfish")) {
                        playerTwo = ("Dogfish");
                        drawStartScreen();
                        return;
                    }
                    if (playerTwo.equals("Dogfish")) {
                        playerTwo = ("Human");
                        drawStartScreen();
                        return;
                    }
                }
                case 3 -> {
                    depth2 = Math.min(depth2 + 1, 4);
                    drawStartScreen();
                }
                case 4 -> {
                    if (chessVariant.equals("Normal")) {
                        chessVariant = "Chess960";
                        drawStartScreen();
                        return;
                    }
                    if (chessVariant.equals("Chess960")) {
                        chessVariant = "Panic Attack";
                        drawStartScreen();
                        return;
                    }
                    if (chessVariant.equals("Panic Attack")) {
                        chessVariant = "Custom";
                        drawStartScreen();
                        return;
                    }
                    if (chessVariant.equals("Custom")) {
                        chessVariant = "Normal";
                        drawStartScreen();
                        return;
                    }
                }
            }
        }
    }

    public void menuFunctionality() {
        if (key == ENTER) {
            if (counter == 0) {
                focus = true;
                strokeWeight(10);
                fill(0, 0, 0, 150);
                rect(0, 0, 1920, 1080);
                drawStartScreen();
                return;
            }
            switch (counter) {
                case 1 -> gameMode = 1; // Computer Starts (old)
                case 2 -> gameMode = 2; //Human Starts (old)
            }

            if (gameMode == 0) {
                soundPlayer.play(sounds[0]);
                boardString = FINAL_BOARD_STRING;
                if (chessVariant.equals("Chess960")) {
                    boardString = board.get960();
                }
                if (chessVariant.equals("Custom")) {
                    String[] lines = loadStrings("./data/games/gameCode.txt");
                    boardString = lines[0];
                }
                board = new Board(boardString);
                gameRunning = true;
                gameStarted = true;
                menu = false;
                focus = false;
                if (!playerOne.equals("Human")) {
                    takenTurn = false;
                    fishMove = false;
                } else {
                    takenTurn = true;
                }
                game = new ArrayList<>();
                game.add(boardString);
                drawScreen();
                return;
            }
            if (gameMode == 1) {
                board = new Board("MAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=");
                drawDeveloperScreen();
                gameRunning = true;
                gameStarted = true;
                menu = false;
                game = new ArrayList<>();
                game.add(boardString);
                return;
            }
            if (gameMode == 2) {
                menu = false;
                gameRunning = true;
                gameStarted = true;
                loadGame();
                board = new Board(boardString);
                drawScreen();
                return;
            }
        }
        //
        if (keyCode == LEFT) {
            counter--;
            if (counter == -1) {
                counter = 2;
            }
            drawStartScreen();
            return;
        }
        if (keyCode == RIGHT) {
            counter++;
            if (counter == 3) {
                counter = 0;
            }
            drawStartScreen();
        }
    }

    public void gameFunctionality() {
        if (gameMode == 0) {
            if (keyCode == LEFT) {
                if (!gameRunning) {
                    gameMode = 2;
                    gameRunning = true;
                    gameStarted = true;
                    takenTurn = true;
                    loadGame();
                    index = rollback.size() - 1;
                    board = rollback.get(index - 1);
                    board.lastMove = board.getMove(game.get(index - 1));
                    drawScreen();
                    pressed = false;
                    return;
                }
            }
        }
        if (gameMode == 1) {
            if (key == ' ') {
                board.turn = !board.turn;
                drawDeveloperScreen();
                return;
            }
            if (displaySave == 0 && pressed && key == ENTER) {
                try {
                    PrintWriter output = new PrintWriter("./data/games/gameCode.txt");
                    output.println(board.hash());
                    output.close();
                    displaySave = 1;
                    drawDeveloperScreen();
                    pressed = false;
                } catch (Exception ignored) {
                }
            }
        }
        if (gameMode == 2) {
            if (keyCode == LEFT) {
                if (pressed && rollback.size() > 0 && index > 1) {
                    index--;
                    playSound(moveList.get(index - 1).trim());
                    board = rollback.get(index - 1);
                    board.lastMove = board.getMove(game.get(index));
                    drawScreen();
                    pressed = false;
                    return;
                }
            }
            if (keyCode == RIGHT) {
                if (pressed && index < rollback.size()) {
                    index++;
                    if (index > 1) {
                        playSound(moveList.get(index - 2).trim());
                    }
                    board = rollback.get(index - 1);
                    board.lastMove = board.getMove(game.get(index - 1));
                    drawScreen();
                    pressed = false;
                }
            }
        }
    }

    public void keyPressed() {
        if (paused) {
            pauseFunctionality();
            return;
        }
        if (key == ESC) {
            if (menu && focus) {
                focus = false;
                drawSimpleScreen();
                drawStartScreen();
                key = 0;
                return;
            }
            drawPauseScreen();
            paused = !paused;
            key = 0;
            return;
        }
        if (menu) {
            if (focus) {
                focusFunctionality();
                return;
            }
            menuFunctionality();
            return;
        }
        if (gameStarted) {
            gameFunctionality();
        }
    }
}