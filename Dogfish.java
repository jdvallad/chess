import java.util.*;

public class Dogfish {

    public static double minimax(Board b, int depth,double alpha, double beta, boolean maximizingPlayer) {
        double res=b.evaluate();
        if (depth==0||b.noMoves()||b.movesSinceLastCapture>=30) {
            return res;
        }
        if (maximizingPlayer) {
            double maxEval=-5000.;
            ArrayList<Board> moves = b.getChildrenBoards();
            for (Board temp : moves) {
                double eval= minimax(temp, depth-1,alpha,beta, false);
                maxEval=Math.max(maxEval, eval);
                alpha=Math.max(alpha,eval);
                if(alpha>=beta){
                    break;
                }
            }
            return maxEval;
        }
        else
        {
            double minEval=5000.;
            ArrayList<Board> moves = b.getChildrenBoards();
            for (Board temp : moves) {
                double eval= minimax(temp,depth-1,alpha,beta, true);
                minEval=Math.min(minEval, eval);
                beta=Math.min(beta,eval);
                if(beta<=alpha){
                    break;
                }
            }
            return minEval;
        }
    }

    public static int[] ponder(Board b, int depth, double alpha,double beta) {
        ArrayList<int[]> moves = b.getArrayMoves();
        if(moves.size()==0){
            return new int[]{-1,0,0,0};
        }
        if(depth==0||depth==-1){
            Collections.shuffle(moves);
            return moves.get(0);
        }
        if (b.turn) {
            double maxEval=-5000;
            int index=0;
            for (int i=0; i<moves.size(); i++) {
                int[] temp1 = moves.get(i);
                double eval= minimax(b.nextBoard(temp1), depth-1,alpha, beta,false);
                if (eval>=maxEval) {
                    maxEval=eval;
                    index=i;
                }
                alpha=Math.max(alpha,eval);
                if(alpha>=beta){
                    break;
                }
            }
            return moves.get(index);
        } 
        else {
            double minEval=5000;
            int index=0;
            for (int i=0; i<moves.size(); i++) {
                int[] temp1 = moves.get(i);
                double eval= minimax(b.nextBoard(temp1),depth-1,alpha,beta, true);
                if (eval<=minEval) {
                    minEval=eval;
                    index=i;
                }
                beta=Math.min(beta,eval);
                if(beta<=alpha){
                    break;
                }
            }
            return moves.get(index);
        }
    }
}