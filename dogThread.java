import java.util.List;

public class dogThread extends Thread {
    Chess board;
    private MoveEval move;
    private final int depth;
    boolean running;
    List<Short> moves;

    public dogThread(Chess board, int depth, List<Short> moves) {
        running = false;
        this.board = board;
        this.depth = 2 * depth;
        move = new MoveEval(0, "");
        this.moves = moves;
    }

    public void run() {
        try {
            running = true;
            int i = 1;
            MoveEval eval = null;
            while (i <= depth) {
                eval = Dogfish.ponder(board, i, -999999, 999999, moves);
                if (board.turn && eval.getEvaluation() == 1000)
                    break;
                if ((!board.turn) && eval.getEvaluation() == -1000)
                    break;
                i++;
            }
            move = eval;
        } catch (Exception e) {
            running = false;
            move = new MoveEval(0, "");
            System.out.println(e);
        }
    }

    public MoveEval move() {
        if (!move.getMove().equals(""))
            running = false;
        return move;
    }
}
