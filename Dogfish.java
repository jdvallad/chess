import java.util.*;

public class Dogfish {
    dogThread dog = new dogThread(null, 2, null);

    public static float negamax(Chess board, int depth, float alpha, float beta, boolean color) {
        if (board.gameOver || depth == 0)
            return Evaluation.evaluate(board) * (color ? 1 : -1);
        List<Short> childMoves = new ArrayList<>(board.legalMoves);
        float value = -99999999;
        for (short move : childMoves) {
            board.makeMove(move);
            value = Math.max(value, -negamax(board, depth - 1, -beta, -alpha, !color));
            board.undo();
            alpha = Math.max(alpha, value);
            if (alpha >= beta)
                break;
        }
        return value;
    }

    public static MoveEval ponder(Chess temp, int depth, float alpha, float beta, List<Short> moveList) {
        Chess board = new Chess(temp);
        if (board.gameOver)
            return new MoveEval(0, "");
        if (depth == 0) {
            Collections.shuffle(moveList);
            board.makeMove(moveList.get(0));
            float eval = Evaluation.evaluate(board);
            board.undo();
            return new MoveEval(eval, moveList.get(0));
        }
        float value = -99999999;
        float eval;
        short bestMove = 0;
        for (short move : moveList) {
            board.makeMove(move);
            eval = -negamax(board, depth - 1, -beta, -alpha, board.turn);
            board.undo();
            if (eval > value) {
                value = eval;
                bestMove = move;
            }
            alpha = Math.max(alpha, value);
            if (alpha >= beta)
                break;
        }
        return new MoveEval(value, bestMove);
    }

    public MoveEval move(Chess logic, int depth, List<Short> moves) {
        if (!dog.running) {
            dog = new dogThread(logic, depth, moves);
            dog.start();
        }
        return dog.move();
    }
}