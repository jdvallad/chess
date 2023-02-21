import processing.core.PConstants;
import processing.sound.SoundFile;

import java.util.*;

public class ChessGUI extends Scene {

    Chessboard board; // handles graphics of board
    Chess logic; // handles logic of Chess
    String move; // keeps track of current move
    Stockfish fish = new Stockfish();
    Dogfish dog = new Dogfish();
    List<Button> buttons;
    String game = "";

    public ChessGUI(SceneSwitcher app, String str, boolean bn) {
        super(app, str, bn);
    }

    public void settings() {
        move = "";
        screen.setSize(1920, 1080);
        logic = new Chess();
        board = new Chessboard(screen, Map.of(
                "data", "lichess", // datapack to use for images and sounds
                "fen", logic.fen, // fen of board, should match up with logical board
                "perspective", "white", // from who's perspective to view the board
                "staticPerspective", true // whether or not perspective flips depending on who's turn it is
        ));
        addButtons();
    }

    public void addButtons() {
        buttons = new ArrayList<>();
        buttons.add(
                new Button(screen, "flip", true, 50f, 50f, .8f, screen.loadImage("./data/buttons/flip.png")) {
                    public void action() {
                        board.flipBoard();
                        snap();
                    }
                });
        buttons.add(
                new Button(screen, "reset", true, -50f, 50f, .8f, screen.loadImage("./data/buttons/reset.png")) {
                    public void action() {
                        board.start.play();
                        dog.dog = new dogThread(null, 2, null);
                        logic.reset();
                        board.setFromFEN(logic.fen);
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
                        board.playSound(logic.rollback(2));
                        board.setFromFEN(logic.fen);
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
                new Button(screen, "forward", true, -50f, -50f, .8f, screen.loadImage("./data/buttons/forward.png")) {
                    public void action() {
                        board.playSound(logic.rollForward(2));
                        board.setFromFEN(logic.fen);
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
            board.start.play();
            board.setFromFEN(logic.fen);
            snap();
            initialize();
            return;
        }
        refresh();
        move = getMove(move, "human", "dog,1");
        board.drawMove(move);
        board.drawLegalMovesFromPiece(move.length() == 0 ? "" : move.substring(0, 2), logic.legalMoves);
        board.drawLastMove(Chess.decodeMove(logic.lastMove()));
        if (move.length() > 2 && logic.legalMoves.contains(logic.encodeMove(move))) {
            String moveType = logic.moveType(logic.encodeMove(move));
            board.playSound(moveType);
            logic.makeMove(logic.encodeMove(move));
            move = "";
            board.setFromFEN(logic.fen);
            if (logic.inCheck())
                board.showCheck();
            if (logic.gameOver) {
                buttons.add(
                        new Button(screen, "result", true, 755f, 435f, .8f,
                                screen.loadImage("./data/results/" + logic.result + ".png")) {
                            public void action() {
                            }
                        });
                board.tintScreen();
                for (Button button : buttons)
                    button.update();
            }
            snap();
        }
        if (move.length() > 2)
            if (board.legalMovesFromPiece(move.substring(2), logic.legalMoves).size() != 0)
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
            move = board.startMoveOnMousePress(move, true);
        if (move.length() == 4 && board.mouseOnBoard())
            move = board.startMoveOnMousePress(move, true);
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
        if (screen.key == PConstants.ESC)
            screen.key = 0;
    }

    public void keyReleased() {
    }
}
