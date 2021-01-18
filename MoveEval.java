public class MoveEval {
    private final float evaluation;
    private final String move;

    public MoveEval(float evaluation, String move) {
        this.evaluation = evaluation;
        this.move = move;
    }
    public MoveEval(float evaluation, short move) {
        this.evaluation = evaluation;
        this.move = Chess.decodeMove(move);
    }
    public float getEvaluation() {
        return evaluation;
    }

    public String getMove() {
        return move;
    }
}
