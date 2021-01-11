import java.util.*;

public class Dogfish {
    public static float[] minimax(chess board, int depth, float alpha, float beta, boolean maximizingPlayer) {
       // chess.println(depth);
        float[] result = new float[2];
        result[1] = depth;
        float res = Evaluation.evaluate(board);
        if (depth == -1 || depth == 0 || board.legalMoves.size() == 0) {
            result[0] = res;
            result[1] = 0;
            return result;
        }
        if (maximizingPlayer) {
            float maxEval = -5000;
            ArrayList<chess> moves = new ArrayList<>();
            for (String str : board.legalMoves)
                moves.add(board.nextBoard(str));
            for (chess temp : moves) {
                float eval = minimax(temp, depth - 1, alpha, beta, false)[0];
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (alpha >= beta) {
                    break;
                }
            }
            result[0] = maxEval;
            return result;
        } else {
            float minEval = 5000;
            ArrayList<chess> moves = new ArrayList<>();
            for (String str : board.legalMoves)
                moves.add(board.nextBoard(str));
            for (chess temp : moves) {
                float eval = minimax(temp, depth - 1, alpha, beta, true)[0];
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) {
                    break;
                }
            }
            result[0] = minEval;
            return result;
        }
    }

    public static String[] ponder(chess board, int dw, float alpha, float beta) {
        int depth = dw + 1;
        ArrayList<String> moves = new ArrayList<>(board.legalMoves);
        String[] result = new String[3];
        if (moves.size() == 0) {
            result[0] = "game";
            result[1] = "is";
            result[2] = "over!";
            return result;
        }
        if (depth == 1) {
            Collections.shuffle(moves);
            result[0] = moves.get(0);
            result[1] = "" + Evaluation.evaluate(board.nextBoard(result[0]));
            result[2] = "0";
            return result;
        }
        if (board.turn.equals("white")) {
            float maxEval = -5000;
            int index = 0;
            float cDepth = 0;
            for (int i = 0; i < moves.size(); i++) {
                String temp1 = moves.get(i);
                float[] eval = minimax(board.nextBoard(temp1), depth - 1, alpha, beta, false);
                if (eval[0] >= maxEval) {
                    maxEval = eval[0];
                    index = i;
                    cDepth = eval[1];
                }
                alpha = Math.max(alpha, eval[0]);
                if (alpha >= beta) {
                    break;
                }
            }
            result[0] = moves.get(index);
            result[1] = "" + maxEval;
            result[2] = "" + cDepth;
            return result;
        } else {
            float minEval = 5000;
            int index = 0;
            float cDepth = 0;
            for (int i = 0; i < moves.size(); i++) {
                String temp1 = moves.get(i);
                float[] eval = minimax(board.nextBoard(temp1), depth - 1, alpha, beta, true);
                if (eval[0] <= minEval) {
                    minEval = eval[0];
                    index = i;
                    cDepth = eval[1];
                }
                beta = Math.min(beta, eval[0]);
                if (beta <= alpha) {
                    break;
                }
            }
            result[0] = moves.get(index);
            result[1] = "" + minEval;
            result[2] = "" + cDepth;
            return result;
        }
    }
}