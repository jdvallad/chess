import processing.sound.SoundFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CreationScene extends Scene {
    float widthP, heightP; //used to resize window on different resolutions
    String move;
    char piece;
    Chess logic; //handles logic of Chess
    Chessboard board; //handles graphics of board
    List<Button> buttons;
    String data;

    public CreationScene(SceneSwitcher app, String str, boolean bn) {
        super(app, str, bn);
    }

    public void settings() {
        widthP = (float) screen.width / 1920f;
        heightP = (float) screen.height / 1080f;
        move = "";
        piece = ' ';
        logic = new Chess();
        logic.clear();
        board = new Chessboard(screen, Map.of(
                "data", "lichess", //datapack to use for images and sounds
                "fen", logic.fen, //fen of board, should match up with logical board
                "perspective", "white", //from who's perspective to view the board
                "staticPerspective", true //whether or not perspective flips depending on who's turn it is
        ));
        data = board.data;
        buttons = new ArrayList<>();
        buttons.add(
                new Button(screen, "wP", true, 50f, 128 * 0 + 50f, 128, 128, screen.loadImage("./data/" + data + "/images/light_square.png"), screen.loadImage("./data/" + data + "/images/wP.png")) {
                    public void action() {
                        piece = 'P';
                    }
                });
        buttons.add(
                new Button(screen, "wR", true, 50f, 128 * 1 + 50f, 128, 128, screen.loadImage("./data/" + data + "/images/dark_square.png"), screen.loadImage("./data/" + data + "/images/wR.png")) {
                    public void action() {
                        piece = 'R';
                    }
                });
        buttons.add(
                new Button(screen, "wN", true, 50f, 128 * 2 + 50f, 128, 128, screen.loadImage("./data/" + data + "/images/light_square.png"), screen.loadImage("./data/" + data + "/images/wN.png")) {
                    public void action() {
                        piece = 'N';
                    }
                });
        buttons.add(
                new Button(screen, "wB", true, 50f, 128 * 3 + 50f, 128, 128, screen.loadImage("./data/" + data + "/images/dark_square.png"), screen.loadImage("./data/" + data + "/images/wB.png")) {
                    public void action() {
                        piece = 'B';
                    }
                });
        buttons.add(
                new Button(screen, "wQ", true, 50f, 128 * 4 + 50f, 128, 128, screen.loadImage("./data/" + data + "/images/light_square.png"), screen.loadImage("./data/" + data + "/images/wQ.png")) {
                    public void action() {
                        piece = 'Q';
                    }
                });
        buttons.add(
                new Button(screen, "wK", true, 50f, 128 * 5 + 50f, 128, 128, screen.loadImage("./data/" + data + "/images/dark_square.png"), screen.loadImage("./data/" + data + "/images/wK.png")) {

                    public void action() {
                        piece = 'K';
                    }
                });
        buttons.add(
                new Button(screen, "trash", true, 50f, 128 * 6 + 50f, 128, 128, screen.loadImage("./data/" + data + "/images/light_square.png"), screen.loadImage("./data/buttons/trash.png")) {

                    public void action() {
                        piece = ' ';
                    }
                });
        buttons.add(
                new Button(screen, "bP", true, -50f, 128 * 0 + 50f, 128, 128, screen.loadImage("./data/" + data + "/images/light_square.png"), screen.loadImage("./data/" + data + "/images/bP.png")) {
                    public void action() {
                        piece = 'p';
                    }
                });
        buttons.add(
                new Button(screen, "bR", true, -50f, 128 * 1 + 50f, 128, 128, screen.loadImage("./data/" + data + "/images/dark_square.png"), screen.loadImage("./data/" + data + "/images/bR.png")) {
                    public void action() {
                        piece = 'r';
                    }
                });
        buttons.add(
                new Button(screen, "bN", true, -50f, 128 * 2 + 50f, 128, 128, screen.loadImage("./data/" + data + "/images/light_square.png"), screen.loadImage("./data/" + data + "/images/bN.png")) {
                    public void action() {
                        piece = 'n';
                    }
                });
        buttons.add(
                new Button(screen, "bB", true, -50f, 128 * 3 + 50f, 128, 128, screen.loadImage("./data/" + data + "/images/dark_square.png"), screen.loadImage("./data/" + data + "/images/bB.png")) {
                    public void action() {
                        piece = 'b';
                    }
                });
        buttons.add(
                new Button(screen, "bQ", true, -50f, 128 * 4 + 50f, 128, 128, screen.loadImage("./data/" + data + "/images/light_square.png"), screen.loadImage("./data/" + data + "/images/bQ.png")) {
                    public void action() {
                        piece = 'q';
                    }
                });
        buttons.add(
                new Button(screen, "bK", true, -50f, 128 * 5 + 50f, 128, 128, screen.loadImage("./data/" + data + "/images/dark_square.png"), screen.loadImage("./data/" + data + "/images/bK.png")) {

                    public void action() {
                        piece = 'k';
                    }
                });
        buttons.add(
                new Button(screen, "trash", true, -50f, 128 * 6 + 50f, 128, 128, screen.loadImage("./data/" + data + "/images/light_square.png"), screen.loadImage("./data/buttons/trash.png")) {

                    public void action() {
                        piece = ' ';
                    }
                });
    }

    public void draw() {
        if (!isInitialized()) {
            board.setFromFEN(logic.fen);
            snap();
            initialize();
            return;
        }
        refresh();
        for (Button button : buttons)
            button.update();
        // board.drawFloatingPiece(piece);
        if (screen.mousePressed)
            move = board.startMoveOnMousePress(move, false);
        Chess.println(move + " " + piece);
        if (move.length() == 2) {
           // logic.putPiece(piece, move);
            board.setFromFEN(logic.fen);
            //piece = ' ';
            move = "";
            snap();
            for (Button button : buttons)
                button.update();
        }
    }


    public void keyPressed() {

    }

    public void keyReleased() {

    }

    public void mousePressed() {
        for (Button button : buttons)
            button.update();
    }

    public void mouseReleased() {

    }
}
