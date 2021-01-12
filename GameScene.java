import processing.sound.SoundFile;

import javax.swing.*;
import java.util.*;

public class GameScene extends Scene {

    float widthP, heightP; //used to resize window on different resolutions
    Chessboard board; //handles graphics of board
    Chess logic; //handles logic of Chess
    String move; //keeps track of current move
    boolean threadRunning;
    dogThread dog;
    stockThread stock;
    Stockfish fish = new Stockfish();
    List<Button> buttons;

    public GameScene(SceneSwitcher app, String str, boolean bn) {
        super(app, str, bn);
    }

    public void settings() {
        widthP = (float) screen.width / 1920f;
        heightP = (float) screen.height / 1080f;
        threadRunning = false;
        move = "";
        logic = new Chess();//"K7/8/1q6/8/8/7k/8/8 b - - 0 1");
        board = new Chessboard(screen, Map.of(
                "data", "meme", //datapack to use for images and sounds
                "fen", logic.fen, //fen of board, should match up with logical board
                "perspective", "white", //from who's perspective to view the board
                "staticPerspective", true //whether or not perspective flips depending on who's turn it is
        ));
        board.settings();
        buttons = new ArrayList<>();
        buttons.add(
                new Button(screen, screen.loadImage("./data/buttons/flip.png"), "flip", true, 50f, 50f, .8f) {
                    public void action() {
                        board.flipBoard();
                        snap();
                    }
                });
        buttons.add(
                new Button(screen, screen.loadImage("./data/buttons/reset.png"), "reset", true, -50f, 50f, .8f) {
                    public void action() {
                        reset();
                    }
                });
        buttons.add(
                new Button(screen, screen.loadImage("./data/buttons/back.png"), "back", true, 50f, -50f, .8f) {
                    public void action() {
                        board.playSound(logic.rollback());
                        board.setFromFEN(logic.fen);
                        snap();
                    }
                });
        buttons.add(
                new Button(screen, screen.loadImage("./data/buttons/forward.png"), "forward", true, -50f, -50f, .8f) {
                    public void action() {
                        String temp = logic.rollForward();
                        board.playSound(temp);
                        board.setFromFEN(logic.fen);
                        if (temp.split("-")[0].equals("gameOver")) {
                            board.tintScreen();
                            snap();
                        }
                        snap();
                    }
                });
        buttons.add(
                new Button(screen, screen.loadImage("./data/buttons/tetris.png"), "tetris", true, 50f, 550f, .8f) {
                    final SoundFile theme = new SoundFile(screen, "./data/Tetris.mp3");

                    public void action() {
                        if (theme.isPlaying())
                            theme.pause();
                        else
                            theme.play(1, .1f);
                    }
                });
        buttons.add(
                new Button(screen, screen.loadImage("./data/buttons/exit.png"), "exit", true, -50f, 550f, .8f) {

                    public void action() {
                        screen.exit();
                    }
                });
    }

    public void draw() {
        if (!board.initialized) {
            board.start.play();
            board.setFromFEN(logic.fen);
            snap();
            board.initialized = true;
            return;
        }
        refresh();
        move = getMove(move, "human", "dog,1");
        board.drawMove(move);
        board.drawLegalMovesFromPiece(move.length() == 0 ? "" : move.substring(0, 2), logic.legalMoves);
        board.drawLastMove(logic.lastMove());

        if (move.length() == 4 && logic.legalMoves.contains(move)) {
            threadRunning = false;
            String moveType = logic.moveType(move);
            board.playSound(moveType);
            logic.makeMove(move);
            move = "";
            board.setFromFEN(logic.fen);
            if (logic.inCheck())
                board.showCheck();
            snap();
            if (logic.gameOver) {
                Chess.print("\r\n" + logic.result);
                board.tintScreen();
                snap();
            }
        }
        if (move.length() == 4) {
            String to = move.substring(2);
            if (board.legalMovesFromPiece(to, logic.legalMoves).size() != 0)
                move = to;
            else
                move = "";
        }

        for (Button button : buttons)
            button.update();
    }

    public String getMove(String move, String white, String black) {
        String[] w = white.replaceAll("\\s", "").split(",");
        String[] b = black.replaceAll("\\s", "").split(",");
        if (logic.turn.equals("white")) {
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
        if (!threadRunning) {
            dog = new dogThread(logic, depth);
            dog.start();
            threadRunning = true;
        }
        String[] res = dog.move();
        if (!res[0].equals("")) {
            return res[0];
        } else
            return new String[]{move, "", ""}[0];
    }

    public String stockMove(int depth, int diff) {
        if (!threadRunning) {
            stock = new stockThread(logic, fish, depth, diff);
            stock.start();
            threadRunning = true;
        }
        String res = stock.move();
        if (!res.equals(""))
            return res;
        else
            return move;
    }


    public String randomMove() {
        List<String> temp = new ArrayList<>(logic.legalMoves);
        Collections.shuffle(temp);
        if (temp.size() != 0) {
            return temp.get(0);
        }
        return move;
    }

    public void clear() {
        logic.clear();
        board.setFromFEN(logic.fen);
        snap();
    }

    public void reset() {
        threadRunning = false;
        board.start.play();
        logic.reset();
        board.setFromFEN(logic.fen);
        snap();
    }

    public void mousePressed() {
        for (Button button : buttons)
            button.update();
        if (move.length() == 2) {
            if (board.legalMovesFromPiece(move, logic.legalMoves).size() != 0)
                move = board.finishMoveOnMouseRelease(move);
            else
                move = "";
        }
        if (move.length() == 0)
            move = board.startMoveOnMousePress(move);
    }

    public void mouseReleased() {
        if (move.length() == 2) {
            if (board.legalMovesFromPiece(move, logic.legalMoves).size() != 0)
                move = board.finishMoveOnMouseRelease(move);
            else
                move = "";
        }
    }

    public void keyPressed() {
    }

    public void keyReleased() {
    }
}
