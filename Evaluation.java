public class Evaluation {
    static final float PAWN = 1;
    static final float KNIGHT = 3;
    static final float BISHOP = 3;
    static final float ROOK = 5;
    static final float QUEEN = 9;
    static final float KING = 0;
    static final float[][] PT = new float[][] {
            { 0, 0, 0, 0, 0, 0, 0, 0, },
            { 50, 50, 50, 50, 50, 50, 50, 50, },
            { 10, 10, 20, 30, 30, 20, 10, 10, },
            { 5, 5, 10, 27, 27, 10, 5, 5, },
            { 0, 0, 0, 25, 25, 0, 0, 0, },
            { 5, -5, -10, 5, 5, -10, -5, 5, },
            { 5, 10, 10, -40, -40, 10, 10, 5, },
            { 0, 0, 0, 0, 0, 0, 0, 0, } };
    static final float[][] NT = new float[][] {
            { -50, -40, -30, -30, -30, -30, -40, -50, },
            { -40, -20, 0, 0, 0, 0, -20, -40, },
            { -30, 0, 10, 15, 15, 10, 0, -30, },
            { -30, 5, 15, 20, 20, 15, 5, -30, },
            { -30, 0, 15, 20, 20, 15, 0, -30, },
            { -30, 5, 10, 15, 15, 10, 5, -30, },
            { -40, -20, 0, 5, 5, 0, -20, -40, },
            { -50, -40, -30, -30, -30, -30, -40, -50, } };
    static final float[][] BT = new float[][] {
            { -20, -10, -10, -10, -10, -10, -10, -20, },
            { -10, 0, 0, 0, 0, 0, 0, -10, },
            { -10, 0, 5, 10, 10, 5, 0, -10, },
            { -10, 5, 5, 10, 10, 5, 5, -10, },
            { -10, 0, 10, 10, 10, 10, 0, -10, },
            { -10, 10, 10, 10, 10, 10, 10, -10, },
            { -10, 5, 0, 0, 0, 0, 5, -10, },
            { -20, -10, -10, -10, -10, -10, -10, -20, } };
    static final float[][] RT = new float[][] {
            { 0, 0, 0, 0, 0, 0, 0, 0, },
            { 5, 10, 10, 10, 10, 10, 10, 5, },
            { -5, 0, 0, 0, 0, 0, 0, -5, },
            { -5, 0, 0, 0, 0, 0, 0, -5, },
            { -5, 0, 0, 0, 0, 0, 0, -5, },
            { -5, 0, 0, 0, 0, 0, 0, -5, },
            { -5, 0, 0, 0, 0, 0, 0, -5, },
            { 0, 0, 0, 5, 5, 0, 0, 0, } };
    static final float[][] QT = new float[][] {
            { -20, -10, -10, -5, -5, -10, -10, -20, },
            { -10, 0, 0, 0, 0, 0, 0, -10, },
            { -10, 0, 5, 5, 5, 5, 0, -10, },
            { -5, 0, 5, 5, 5, 5, 0, -5, },
            { 0, 0, 5, 5, 5, 5, 0, -5, },
            { -10, 5, 5, 5, 5, 5, 0, -10, },
            { -10, 0, 5, 0, 0, 0, 0, -10, },
            { -20, -10, -10, -5, -5, -10, -10, -20, } };
    static final float[][] KMT = new float[][] {
            { -30, -40, -40, -50, -50, -40, -40, -30, },
            { -30, -40, -40, -50, -50, -40, -40, -30, },
            { -30, -40, -40, -50, -50, -40, -40, -30, },
            { -30, -40, -40, -50, -50, -40, -40, -30, },
            { -20, -30, -30, -40, -40, -30, -30, -20, },
            { -10, -20, -20, -20, -20, -20, -20, -10, },
            { 20, 20, 0, 0, 0, 0, 20, 20, },
            { 20, 30, 10, 0, 0, 10, 30, 20, } };
    static final float[][] KET = new float[][] {
            { -50, -40, -30, -20, -20, -30, -40, -50, },
            { -30, -20, -10, 0, 0, -10, -20, -30, },
            { -30, -10, 20, 30, 30, 20, -10, -30, },
            { -30, -10, 30, 40, 40, 30, -10, -30, },
            { -30, -10, 30, 40, 40, 30, -10, -30, },
            { -30, -10, 20, 30, 30, 20, -10, -30, },
            { -30, -30, 0, 0, 0, 0, -30, -30, },
            { -50, -30, -30, -30, -30, -30, -30, -50, }
    };

    public static boolean endGame(Chess board) {
        boolean whiteQueen = Chess.longToBytes(board.pieces('Q')).size() == 1;
        boolean blackQueen = Chess.longToBytes(board.pieces('q')).size() == 1;
        int pawnCount = Chess.longToBytes(board.pieces('p')).size()
                + Chess.longToBytes(board.pieces('P')).size();
        if ((!(whiteQueen || blackQueen)) && pawnCount <= 4)
            return true;
        if (whiteQueen) {
            int minorPieceCount = Chess.longToBytes(board.pieces('B')).size()
                    + Chess.longToBytes(board.pieces('N')).size();
            int rookCount = Chess.longToBytes(board.pieces('R')).size();
            if (rookCount != 0)
                return false;
            if (minorPieceCount > 1)
                return false;
        }
        if (blackQueen) {
            int minorPieceCount = Chess.longToBytes(board.pieces('b')).size()
                    + Chess.longToBytes(board.pieces('n')).size();
            int rookCount = Chess.longToBytes(board.pieces('r')).size();
            if (rookCount != 0)
                return false;
            return minorPieceCount <= 1;
        }
        return true;
    }

    public static float evaluate(Chess board) {
        float res = 0;
        boolean endGame = endGame(board);
        for (int i = 0; i < 64; i++)
            switch (board.pieceAt((byte) i)) {
                case 'P':
                    res += PAWN + (PT[i / 8][i % 8] / 100f);
                    break;
                case 'R':
                    res += ROOK + (RT[i / 8][i % 8] / 100f);
                    break;
                case 'N':
                    res += KNIGHT + (NT[i / 8][i % 8] / 100f);
                    break;
                case 'B':
                    res += BISHOP + (BT[i / 8][i % 8] / 100f);
                    break;
                case 'Q':
                    res += QUEEN + (QT[i / 8][i % 8] / 100f);
                    break;
                case 'K':
                    res += KING + ((endGame ? KET[i / 8][i % 8] : KMT[i / 8][i % 8]) / 100f);
                    break;
                case 'p':
                    res += -PAWN - (PT[7 - (i / 8)][7 - (i % 8)] / 100f);
                    break;
                case 'r':
                    res += -ROOK - (RT[7 - (i / 8)][7 - (i % 8)] / 100f);
                    break;
                case 'n':
                    res += -KNIGHT - (NT[7 - (i / 8)][7 - (i % 8)] / 100f);
                    break;
                case 'b':
                    res += -BISHOP - (BT[7 - (i / 8)][7 - (i % 8)] / 100f);
                    break;
                case 'q':
                    res += -QUEEN - (QT[7 - (i / 8)][7 - (i % 8)] / 100f);
                    break;
                case 'k':
                    res += -KING - ((endGame ? KET[7 - (i / 8)][7 - (i % 8)] : KMT[7 - (i / 8)][7 - (i % 8)]) / 100f);
                    break;
                default:
                    res += 0;
                    break;
            }
        if (board.gameOver)
            switch (board.result.split(" ")[0]) {
                case "draw":
                    res = 0;
                    break;
                case "white":
                    res = 1000;
                    break;
                case "black":
                    res = -1000;
                    break;
            }
        return res;
    }
}
