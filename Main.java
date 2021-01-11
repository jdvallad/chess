import processing.core.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class Main extends PApplet {

    float widthP, heightP; //used to resize window on different resolutions
    chessboard board; //handles graphics of board
    chess logic; //handles logic of chess
    String move; //keeps track of current move
    boolean threadRunning;
    dogThread dog;
    stockThread stock;
    Stockfish fish = new Stockfish();

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
        size(displayWidth, displayHeight);
        widthP = (float) width / 1920f;
        heightP = (float) height / 1080f;
        threadRunning = false;
        move = "";
        logic = new chess();//"K7/8/1q6/8/8/7k/8/8 b - - 0 1");
        board = new chessboard(this, Map.of(
                "data", "meme", //datapack to use for images and sounds
                "fen", logic.fen, //fen of board, should match up with logical board
                "perspective", "white", //from who's perspective to view the board
                "staticPerspective", true //whether or not perspective flips depending on who's turn it is
        ));
        board.settings();
    }

    public void draw() {
        board.run();
        if (!logic.gameOver) {
            move = getMove(move,"human","human");

            board.drawMove(move);

            board.drawLastMove(logic.lastMove());

            board.drawLegalMovesFromPiece(move.length() == 0 ? "" : move.substring(0, 2), logic.legalMoves);

            if (move.length() == 4) {
                if (logic.legalMoves.contains(move)) {
                    threadRunning = false;

                    String moveType = logic.moveType(move);

                    board.playSound(moveType);

                    logic.makeMove(move);

                    board.setFromFEN(logic.fen);

                    if (logic.inCheck())
                        board.showCheck(logic.lastMove());

                    if (logic.gameOver) {
                        chess.print("\r\n" + logic.result);
                        board.tintScreen();
                    }

                }
                move = ""; //reset move
            }
        }
    }

    public String getMove(String move, String white, String black) {
        String[] w = white.replaceAll("\\s","").split(",");
        String[] b = black.replaceAll("\\s","").split(",");
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

    public void keyPressed() {
        if (key == ESC) {
            key = 0;
            reset();
            return;
        }
        if (key == ' ') {
            board.flipBoard();
            board.drawScreen(logic.allMovesMade.size() != 0 ? logic.allMovesMade.get(logic.allMovesMade.size() - 1) : "");
            return;
        }
        if (keyCode == LEFT) {
            logic.rollback(2);
            board.setFromFEN(logic.fen);
        }
        if (keyCode == RIGHT) {
            logic.rollForward(2);
            board.setFromFEN(logic.fen);
        }
        if (key == '`')
            key = ESC;
    }

    public void mouseReleased() {
        if (move.length() == 2)
            move = board.finishMoveOnMouseRelease(move);
    }

    public String randomMove() {
        ArrayList<String> temp = new ArrayList<>(logic.legalMoves);
        Collections.shuffle(temp);
        if (temp.size() != 0) {
            return temp.get(0);
        }
        return move;
    }

    public void clear() {
        logic.clear();
        board.setFromFEN(logic.fen);
    }

    public void reset() {
        board.start.play();
        logic.reset();
        board.setFromFEN(logic.fen);
    }

    public void mousePressed() {
        if (move.length() == 0)
            move = board.startMoveOnMousePress(move);
    }

    public void rect(float a, float b, float c, float d) {
        super.rect(widthP * a, heightP * b, widthP * c, heightP * d);
    }

    public void circle(float a, float b, float c) {
        super.circle(widthP * a, heightP * b, widthP * c);
    }

    public void textSize(float s) {
        super.textSize(widthP * s);
    }

    public void text(String s, float a, float b) {
        super.text(s, widthP * a, heightP * b);
    }

    public void image(PImage img, float a, float b) {
        super.image(img, widthP * a, heightP * b, widthP * img.width, heightP * img.height);
    }

    public void image(PImage img, float a, float b, float x, float y) {
        super.image(img, widthP * a, heightP * b, widthP * x, heightP * y);
    }

}