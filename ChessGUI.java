import processing.core.PConstants;
import processing.core.PImage;
import processing.sound.SoundFile;

import java.util.*;

public class ChessGUI extends Scene {
    PImage[] images;
    char[][] pieceBoard = new char[8][8];
    SoundFile start;
    SoundFile end;
    SoundFile moveSound;
    SoundFile capture;
    SoundFile error;
    SoundFile check;
    SoundFile castle;
    SoundFile promotion;
    String perspective; // changes the orientation of the board
    boolean staticPerspective; // determines whether perspective will be changed when a move is made
    String data;
    String turn;
    String fen;
    Chess logic; // handles logic of Chess
    String move; // keeps track of current move
    Stockfish fish = new Stockfish();
    Dogfish dog = new Dogfish();
    List<Button> buttons;
    String game = "";
    Map<String, Object> parameters;

    public ChessGUI(SceneSwitcher app, String str, boolean bn, Map<String, Object> parameters) {
        super(app, str, bn);

        this.parameters = parameters;
    }

    public void settings() {
        move = "";
        screen.setSize(1920, 1080);
        logic = new Chess();
        data = (String) parameters.get("data");
        fen = (String) parameters.get("fen");
        if (fen.equals(""))
            fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
        perspective = (String) parameters.get("perspective");
        staticPerspective = (boolean) parameters.get("staticPerspective");
        images = new PImage[19];
        start = new SoundFile(screen, "./data/" + data + "/sounds/start.mp3");
        end = new SoundFile(screen, "./data/" + data + "/sounds/end.mp3");
        moveSound = new SoundFile(screen, "./data/" + data + "/sounds/move.mp3");
        capture = new SoundFile(screen, "./data/" + data + "/sounds/capture.mp3");
        error = new SoundFile(screen, "./data/" + data + "/sounds/error.mp3");
        check = new SoundFile(screen, "./data/" + data + "/sounds/check.mp3");
        castle = new SoundFile(screen, "./data/" + data + "/sounds/castle.mp3");
        promotion = new SoundFile(screen, "./data/" + data + "/sounds/promotion.mp3");
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
        addButtons();
    }

 
    public void addButtons() {
        buttons = new ArrayList<>();
        buttons.add(
                new Button(screen, "flip", true, 50f, 50f, .8f, screen.loadImage("./data/buttons/flip.png")) {
                    public void action() {
                        flipBoard();
                        snap();
                    }
                });
        buttons.add(
                new Button(screen, "reset", true, -50f, 50f, .8f, screen.loadImage("./data/buttons/reset.png")) {
                    public void action() {
                        start.play();
                        dog.dog = new dogThread(null, 2, null);
                        logic.reset();
                        setFromFEN(logic.fen);
                        for (Button button : buttons) {
                            if (button.getId().equals("result")) {
                                button.deactivate();
                                button.update();
                            }
                        }
                        snap();
                    }
                });
        buttons.add(
                new Button(screen, "back", true, 50f, -50f, .8f, screen.loadImage("./data/buttons/back.png")) {
                    public void action() {
                        dog.dog = new dogThread(null, 2, null);
                        playSound(logic.rollback(2));
                        setFromFEN(logic.fen);
                        for (Button button : buttons) {
                            if (button.getId().equals("result")) {
                                button.deactivate();
                                button.update();
                            }
                        }
                        snap();
                    }
                });
        buttons.add(
                new Button(screen, "Music", true, 50f, 550f, .8f, screen.loadImage("./data/buttons/tetris.png")) {
                    final SoundFile theme = new SoundFile(screen, "./data/music/Tetris.mp3");

                    public void action() {
                        if (theme.isPlaying())
                            theme.stop();
                        else
                            theme.loop(1, .1f);
                    }
                });
        buttons.add(
                new Button(screen, "exit", true, -50f, 550f, .8f, screen.loadImage("./data/buttons/exit.png")) {

                    public void action() {
                        screen.exit();
                    }
                });
    }
   
    public void draw() {
        if (!isInitialized()) {
            start.play();
            setFromFEN(logic.fen);
            snap();
            initialize();
            return;
        }
        refresh();
        move = getMove(move, "human", "dog,1");
        drawMove(move);
        drawLegalMovesFromPiece(move.length() == 0 ? "" : move.substring(0, 2), logic.legalMoves);
        drawLastMove(Chess.decodeMove(logic.lastMove()));
        if (move.length() > 2 && logic.legalMoves.contains(logic.encodeMove(move))) {
            String moveType = logic.moveType(logic.encodeMove(move));
            playSound(moveType);
            logic.makeMove(logic.encodeMove(move));
            move = "";
            setFromFEN(logic.fen);
            if (logic.inCheck())
                showCheck();
            if (logic.gameOver) {
                buttons.add(
                        new Button(screen, "result", true, 755f, 435f, .8f,
                                screen.loadImage("./data/results/" + logic.result + ".png")) {
                            public void action() {
                            }
                        });
                tintScreen();
                for (Button button : buttons)
                    button.update();
            }
            snap();
        }
        if (move.length() > 2)
            if (legalMovesFromPiece(move.substring(2), logic.legalMoves).size() != 0)
                move = move.substring(2);
            else
                move = "";
        for (Button button : buttons)
            button.update();
    }

    public String getMove(String move, String white, String black) {
        String[] w = white.replaceAll("\\s", "").split(",");
        String[] b = black.replaceAll("\\s", "").split(",");
        if (logic.turn) {
            if (w[0].equals("stock"))
                return stockMove(Integer.parseInt(w[1]), Integer.parseInt(w[2]));
            if (w[0].equals("dog"))
                return dogMove(Integer.parseInt(w[1]));
            if (w[0].equals("human"))
                return move;
        } else {
            if (b[0].equals("stock"))
                return stockMove(Integer.parseInt(b[1]), Integer.parseInt(b[2]));
            if (b[0].equals("dog"))
                return dogMove(Integer.parseInt(b[1]));
            if (b[0].equals("human"))
                return move;
        }
        return move;
    }

    public String dogMove(int depth) {
        return dog.move(logic, depth, new ArrayList<>(logic.legalMoves)).getMove();
    }

    public String stockMove(int depth, int diff) {
        String res = fish.move(logic, depth, diff, move);
        if (res.equals(""))
            return "";
        return res;
    }

    public String randomMove() {
        List<Short> temp = new ArrayList<>(logic.legalMoves);
        Collections.shuffle(temp);
        if (temp.size() != 0) {
            return Chess.decodeMove(temp.get(0));
        }
        return move;
    }

    public void clear() {
        logic.clear();
        setFromFEN(logic.fen);
        snap();
    }

    public void mousePressed() {
        for (Button button : buttons)
            button.update();
        if (move.length() == 2) {
            if (legalMovesFromPiece(move, logic.legalMoves).size() != 0)
                move = finishMoveOnMouseRelease(move);
            else
                move = "";
        }
        if (move.length() == 0)
            move = startMoveOnMousePress(move, true);
        if (move.length() == 4 && mouseOnBoard())
            move = startMoveOnMousePress(move, true);
    }

    public void mouseReleased() {
        if (move.length() == 2) {
            if (legalMovesFromPiece(move, logic.legalMoves).size() != 0)
                move = finishMoveOnMouseRelease(move);
            else
                move = "";
        }
    }

    public void keyPressed() {
        if (screen.key == PConstants.ESC)
            screen.key = 0;
    }

    public void keyReleased() {
    }

    
    public void setFromParameters(Map<String, Object> parameters) {
        data = (String) parameters.get("data");
        fen = (String) parameters.get("fen");
        if (fen.equals(""))
            fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
        perspective = (String) parameters.get("perspective");
        staticPerspective = (boolean) parameters.get("staticPerspective");
        setFromFEN(fen);
    }

    public void flipBoard() {
        perspective = perspective.equals("white") ? "black" : "white";
        drawScreen();
    }

    public void tintScreen() {
        screen.fill(30, 80);
        screen.rect(0, 0, 1920, 1080);
    }

    public void playSound(String moveType) {
        switch (moveType.split("-")[0]) {
            case "gameOver":
                end.play();
                return;
            case "check":
                check.play();
                return;
            case "capture":
                capture.play();
                return;
            case "castle":
                castle.play();
                return;
            case "promotion":
                promotion.play();
                return;
            case "move":
                moveSound.play();
                return;
            default:
        }
    }

    public void drawMove(String move) {
        if (move.length() == 0) {
            return;
        }
        if (holdingPiece(move))
            drawFloatingPiece(move); // This will render piece held as floating
    }

    public void setFromFEN(String f) {
        fen = f;
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
        if (last[1].equals("b"))
            turn = "black";
        else
            turn = "white";
        if (!staticPerspective) {
            perspective = last[1].equals("b") ? "black" : "white";
        }
        drawScreen();
    }

    public boolean lookingForPickupPiece(String move) {
        int x = (int) ((((screen.mouseY)) - 28) / (128));
        int y = (int) ((((screen.mouseX)) - 448) / (128));
        if (perspective.equals("black"))
            return mouseOnBoard() && ((!screen.mousePressed) && move.length() == 0)
                    && (pieceBoard[7 - x][7 - y] != ' ');
        else
            return mouseOnBoard() && ((!screen.mousePressed) && move.length() == 0) && (pieceBoard[x][y] != ' ');
    }

    public boolean holdingPiece(String move) {
        return screen.mousePressed && screen.mouseButton == PConstants.LEFT && move.length() == 2;
    }

    public boolean mouseOnBoard() {
        return 448 < ((screen.mouseX)) && ((screen.mouseX)) < 1472 && 28 < ((screen.mouseY))
                && ((screen.mouseY)) < 1052;
    }

    public void showCheck() {
        screen.tint(180);
        screen.strokeWeight(0);
        screen.background((float) (173 * .5), (float) (216 * .5), (float) (230 * .5));
        screen.fill(turn.equals("white") ? 180 : 0);
        screen.rect(448 - 20, 28 - 20, 1024 + 40, 1024 + 40);
        screen.image(images[12], 448, 28, 1024, 1024);
        if (!perspective.equals("black")) {
            screen.image(images[15], 448, 28);
            screen.image(images[14], 448, 1052 - 38);
        } else {
            screen.image(images[13], 448, 1052 - 1024);
            screen.image(images[16], 448 - 35 + 1024, 28);
        }
        char king = turn.equals("white") ? 'K' : 'k';
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (pieceBoard[r][c] == king)
                    screen.tint(240, 128, 128);
                else
                    screen.tint(180);
                drawPiece(r, c);
            }
        }

    }

    public String finishMoveOnMouseRelease(String m) {
        String move = m;
        if (mouseOnBoard()) {
            int x = (int) ((((screen.mouseX)) - 448) / (128));
            int y = (int) ((((screen.mouseY)) - 28) / (128));
            if (perspective.equals("black")) {
                x = 7 - x;
                y = 7 - y;
            }
            char xFile = (char) (97 + x);
            int yRank = 8 - y;
            if ((move.equals("" + xFile + yRank))) {
                // move = "";
            } else {
                move += "" + xFile + yRank;
            }
        } else {
            move = "";
        }
        return move;
    }

    public void highlightHoverSpace(String m) {
        int x = (int) ((((screen.mouseX)) - 448) / (128));
        int y = (int) ((((screen.mouseY)) - 28) / (128));
        if (perspective.equals("black")) {
            x = 7 - x;
            y = 7 - y;
        }
        screen.fill(153, 255, 255, 30);
        screen.strokeWeight(0);
        screen.rect(447 + 128 * x, 27 + 128 * y, 128, 128);
    }

    public String startMoveOnMousePress(String m, boolean check) {
        if (mouseOnBoard() && screen.mouseButton == PConstants.LEFT) {
            String move = m;
            int x = (int) ((((screen.mouseX)) - 448) / (128));
            int y = (int) ((((screen.mouseY)) - 28) / (128));
            if (perspective.equals("black")) {
                x = 7 - x;
                y = 7 - y;
            }
            int yRank = 8 - y;
            char xFile = (char) (97 + x);
            if ((!check) || getPiece("" + xFile + yRank) != ' ')
                move = "" + xFile + yRank;
            return move;
        }
        return m;
    }

    public void drawScreen() {
        screen.tint(180);
        screen.strokeWeight(0);
        screen.background((float) (173 * .5), (float) (216 * .5), (float) (230 * .5));
        screen.fill(turn.equals("white") ? 180 : 0);
        screen.rect(448 - 20, 28 - 20, 1024 + 40, 1024 + 40);
        screen.image(images[12], 448, 28, 1024, 1024);
        if (!perspective.equals("black")) {
            screen.image(images[15], 448, 28);
            screen.image(images[14], 448, 1052 - 38);
        } else {
            screen.image(images[13], 448, 1052 - 1024);
            screen.image(images[16], 448 - 35 + 1024, 28);
        }
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                drawPiece(r, c);
            }
        }
    }

    public void drawLastMove(String lastMove) {
        if (lastMove.length() == 0)
            return;
        if (lastMove.substring(0, 2).equals(lastMove.substring(2)))
            return;
        screen.fill(250, 247, 39, 30);
        screen.strokeWeight(0);
        int a = convertFileToInt(lastMove.charAt(0));
        int b = 8 - Integer.parseInt("" + lastMove.charAt(1));
        int c = convertFileToInt(lastMove.charAt(2));
        int d = 8 - Integer.parseInt("" + lastMove.charAt(3));
        if (perspective.equals("black")) {
            a = 7 - a;
            b = 7 - b;
            c = 7 - c;
            d = 7 - d;
        }
        screen.rect(447 + 128 * a, 27 + 128 * b, 128, 128);
        screen.rect(447 + 128 * c, 27 + 128 * d, 128, 128);
    }

    private int convertFileToInt(char c) {
        return ((int) c) - 97;
    }

    public void drawPiece(int r, int c) {
        String name = ("" + pieceBoard[r][c]);
        int temp;
        switch (name.toLowerCase()) {
            case "q":
                temp = 2;
                break;
            case "r":
                temp = 4;
                break;
            case "b":
                temp = 6;
                break;
            case "n":
                temp = 8;
                break;
            case "p":
                temp = 10;
                break;
            case "k":
                temp = 0;
                break;
            default:
                temp = -1;
                break;
        }
        if (temp == -1) {
            return;
        }
        int team;
        if (Character.isUpperCase(name.charAt(0))) {
            team = 0;
        } else {
            team = 1;
        }
        if (perspective.equals("black"))
            screen.image(images[temp + (team == 0 ? 0 : 1)], 448 + 128 * (7 - c), 28 + 128 * (7 - r), 128, 128);
        else
            screen.image(images[temp + (team == 0 ? 0 : 1)], 448 + 128 * c, 28 + 128 * r, 128, 128);
    }

    public void drawFloatingPiece(String location) {
        screen.tint(180);
        int r = 8 - Integer.parseInt("" + location.charAt(1));
        int c = convertFileToInt(location.charAt(0));
        String name = ("" + pieceBoard[r][c]);
        if (perspective.equals("black")) {
            r = 7 - r;
            c = 7 - c;
        }
        int temp;
        switch (name.toLowerCase()) {
            case "q":
                temp = 2;
                break;
            case "r":
                temp = 4;
                break;
            case "b":
                temp = 6;
                break;
            case "n":
                temp = 8;
                break;
            case "p":
                temp = 10;
                break;
            case "k":
                temp = 0;
                break;
            default:
                temp = -1;
                break;
        }
        if (temp == -1) {
            return;
        }
        int team;
        if (Character.isUpperCase(name.charAt(0))) {
            team = 0;
        } else {
            team = 1;
        }
        screen.image(images[(r + c) % 2 == 0 ? 17 : 18], 448 + 128 * c, 28 + 128 * r, 128, 128);
        screen.image(images[temp + (team == 0 ? 0 : 1)], (screen.mouseX - 64), (screen.mouseY - 64), 128, 128);
    }

    public void drawFloatingPiece(char piece) {
        if (piece == ' ')
            return;
        screen.tint(180);
        String name = "" + piece;
        int temp;
        switch (name.toLowerCase()) {
            case "q":
                temp = 2;
                break;
            case "r":
                temp = 4;
                break;
            case "b":
                temp = 6;
                break;
            case "n":
                temp = 8;
                break;
            case "p":
                temp = 10;
                break;
            case "k":
                temp = 0;
                break;
            default:
                temp = -1;
                break;
        }
        if (temp == -1) {
            return;
        }
        int team;
        if (Character.isUpperCase(name.charAt(0))) {
            team = 0;
        } else {
            team = 1;
        }
        screen.image(images[temp + (team == 0 ? 0 : 1)], (screen.mouseX - 64), (screen.mouseY - 64), 128, 128);
    }

    public Set<Short> legalMovesFromPiece(String location, Set<Short> legalMoves) {
        Set<Short> res = new HashSet<>();
        for (short sh : legalMoves) {
            String str = Chess.decodeMove(sh);
            if (str.substring(0, 2).equals(location))
                res.add(sh);
        }
        return res;
    }

    public void drawLegalMovesFromPiece(String location, Set<Short> legalMoves) {
        if (location.equals("")) {
            int x = (int) ((((screen.mouseX)) - 448) / 128);
            int y = (int) ((((screen.mouseY)) - 28) / 128);
            if (perspective.equals("black")) {
                x = 7 - x;
                y = 7 - y;
            }
            int yRank = 8 - y;
            char xFile = (char) (97 + x);
            drawLegalMovesFromPiece("" + xFile + yRank, legalMoves);
            return;
        }
        for (short sh : legalMovesFromPiece(location, legalMoves)) {
            String str = Chess.decodeMove(sh);
            int r1 = 8 - Integer.parseInt("" + str.charAt(3));
            int c1 = convertFileToInt(str.charAt(2));
            int x = (int) ((((screen.mouseX)) - 448) / 128);
            int y = (int) ((((screen.mouseY)) - 28) / 128);
            if (perspective.equals("black")) {
                r1 = 7 - r1;
                c1 = 7 - c1;
                x = 7 - x;
                y = 7 - y;
            }
            if (x == c1 && y == r1) {
                screen.fill(153, 255, 255, 30);
                screen.strokeWeight(0);
                screen.rect(447 + 128 * x, 27 + 128 * y, 128, 128);
            }
            screen.fill(30, 80);
            screen.strokeWeight(0);
            screen.circle(64 + 447 + 128 * c1, 64 + 27 + 128 * r1, 50);
        }
    }

    public char getPiece(String s) {
        int x = convertFileToInt(s.charAt(0));
        int y = 8 - Integer.parseInt("" + s.charAt(1));
        return pieceBoard[y][x];
    }

}
