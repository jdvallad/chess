public class stockThread extends Thread {
    chessboard board;
    private String move = "";
    private final int depth;
    private final int difficulty;
    private final Stockfish stock;

    public stockThread(chessboard b,Stockfish s, int d,int diff) {
        board = b;
        depth = d;
        stock = s;
        difficulty = diff;
    }

    public void run() {
        try {
            move = stock.ponder(board.fen, difficulty,depth,100);
        } catch (Exception e) {
            move = "";
        }
    }

    public String move() {
        String temp = move;
        move = "";
        return temp;
    }
}
