import java.util.*;

public class Dogfish {
    dogThread dog = new dogThread(null, 2, null);

    public static float negamax(Chess board, int depth, float alpha, float beta, boolean color) {
        if (board.gameOver)
            return Evaluation.evaluate(board) * (color ? 1 : -1);
        if (depth == 0) {
            //String lastMove = Chess.decodeMove(board.allMovesMade.get(board.allMovesMade.size() - 1));
            //String lastFen = board.fenList.get(board.fenList.size() - 2);
            //boolean wasCapture = Chess.pieceAt(lastFen, lastMove.substring(2, 4)) != ' ';
            if (false) {
                List<Short> childMoves = new ArrayList<>(board.legalMoves);
                float value = -99999999;
                for (short move : childMoves) {
                    board.makeMove(move);
                    value = Math.max(value, -negamax(board, 0, -beta, -alpha, !color));
                    board.undo();
                    alpha = Math.max(alpha, value);
                    if (alpha >= beta)
                        break;
                }
                // Chess temp = new Chess(lastFen);
                // temp.drawBoard();
                // Chess.println("Quiet board avoided. :" + lastMove);
                return value;
            } else
                return Evaluation.evaluate(board) * (color ? 1 : -1);
        }
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
     //   Map<Short, Float> moveEvalPairs = new HashMap<>();
        float value = -99999999;
        float eval;
     //   int index = 0;
        short bestMove = 0;
        for (short move : moveList) {
            board.makeMove(move);
            eval = -negamax(board, depth - 1, -beta, -alpha, board.turn);
            board.undo();
         //   moveEvalPairs.put(move, eval);
         //   index++;
            if (eval > value) {
                value = eval;
                bestMove = move;
            }
            alpha = Math.max(alpha, value);
            if (alpha >= beta)
                break;
        }
        //   ArrayList<Short> sortedMoves = new ArrayList<>(moveEvalPairs.keySet());
        //  sortedMoves.sort(
        //          Comparator.comparing(moveEvalPairs::get)
        //  );
        // List<Short> bestMoves = new ArrayList<>();
        //  for (short sh : moveEvalPairs.keySet())
        //     if (Math.abs(value - moveEvalPairs.get(sh)) < .1)
        //        bestMoves.add(sh);
        //  Collections.shuffle(bestMoves);
        //   bestMove = bestMoves.get(0);
        //   value = moveEvalPairs.get(bestMove);
        //   moveList.subList(0, index).clear();
        //   System.out.print("\r\n[ ");
        //   for (short sh : sortedMoves)
        //       moveList.add(0, sh);
       // for (short sh : moveList)
       //     System.out.print("(" + Chess.decodeMove(sh) + ", " + moveEvalPairs.get(sh) + "),");
       //  System.out.print("]");
          Chess.println(" " + Chess.decodeMove(bestMove) + " " + value + " " + depth);
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