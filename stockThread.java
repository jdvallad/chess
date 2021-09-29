public class stockThread extends Thread {
    Chess board;
    private String move = "";
    private final int depth;
    private final int difficulty;
    private final Stockfish stock;
    public boolean running;

    public stockThread(Chess b, Stockfish s, int d, int diff) {
        board = b;
        depth = d;
        stock = s;
        difficulty = diff;
        running = false;
    }

    public void run() {
        try {
            running = true;
            move = stock.ponder(board.fen, difficulty, depth, 100);
        } catch (Exception e) {
            running = false;
            move = "";
        }
    }

    public String move() {
        if (!move.equals(""))
            running = false;
        return move;
    }
}
