import java.util.*;

class Piece {
    public static final int none = 0;
    public static final int king = 1;
    public static final int pawn = 2;
    public static final int knight = 3;
    public static final int bishop = 4;
    public static final int rook = 5;
    public static final int queen = 6;
    public static final Map<Integer, Character> symbolFromPieceType = Map.of(
            Piece.king, 'k', Piece.pawn, 'p', Piece.knight, 'n',
            Piece.bishop, 'b', Piece.rook, 'r', Piece.queen, 'q',
            Piece.none, ' '
    );
    public static final int white = 8;
    public static final int black = 16;

    public static boolean isWhite(int piece) {
        return (piece >>> 4) == 1;
    }

    public static boolean isBlack(int piece) {
        return (piece >>> 5) == 1;
    }

    public static char pieceType(int piece) {
        return symbolFromPieceType.get(piece & 7);
    }
}

class Board {
    public static int[] squares = new int[64];

    public static final String startFEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    public static void loadPositionFromFen(String fen) {
        Map<Character, Integer> pieceTypeFromSymbol = Map.of(
                'k', Piece.king, 'p', Piece.pawn, 'n', Piece.knight,
                'b', Piece.bishop, 'r', Piece.rook, 'q', Piece.queen
        );
        String fenBoard = fen.split(" ")[0];
        int file = 0, rank = 7;
        for (char symbol : fenBoard.toCharArray()) {
            if (symbol == '/') {
                file = 0;
                rank--;
            } else {
                if (Character.isDigit(symbol)) {
                    file += Character.getNumericValue(symbol);
                } else {
                    int pieceColor = (Character.isUpperCase(symbol)) ? Piece.white : Piece.black;
                    int pieceType = pieceTypeFromSymbol.get(Character.toLowerCase(symbol));
                    squares[rank * 8 + file] = pieceType | pieceColor;
                    file++;
                }
            }
        }
    }
}

public class Chess{

    static final int[] directionOffsets = {8, -8, -1, 1, 7, -7, 9, -9};
    static final int[][] numSquaresToEdge;

    static {
        numSquaresToEdge = new int[64][];
        for (int i = 0; i < 64; i++) {
            int rank = rank(i);
            int file = file(i);
            int numNorth = 7 - rank;
            int numEast = 7 - file;
            numSquaresToEdge[i] = new int[]{
                    numNorth,
                    rank,
                    file,
                    numEast,
                    Math.min(numNorth, file),
                    Math.min(rank, numEast),
                    Math.min(numNorth, numEast),
                    Math.min(rank, file)
            };
        }
    }

    //Begin Move and FEN Lists
    Set<Short> legalMoves = new HashSet<>();
    Set<Short> psuedoLegalMoves = new HashSet<>();
    boolean turn; //keeps track current turn, "white" or "black"
    String fen; //stores fen of current board.


    //Begin Constructors
    public Chess() {
        this("");
    }

    public Chess(String f) {
        if (f.equals(""))
            Board.loadPositionFromFen(Board.startFEN);
        else
            Board.loadPositionFromFen(f);
    }

    //Begin FEN Methods

    public static int rank(int a) {
        return 8 - ((63 - a) >>> 3);
    }

    public static char file(int a) {
        return (char) (((63 - a) & 7) + 97);
    }


    //Begin Visualisation Methods
    public void drawBoard() {
        System.out.print("\r\n8 | ");
        for (int k = 0; k < 64; k++) {
            char piece = Piece.pieceType(Board.squares[k]) ;
            piece = (Piece.isWhite(Board.squares[k])) ? Character.toUpperCase(piece) : piece;
            println(piece != ' ' ? piece : '*');
            if ((k + 1) % 8 == 0 && (k != 63))
                print("\r\n" + (7 - (k >>> 3)) + " | ");
        }
        print("\r\n  -----------------");
        print("    a b c d e f g h");
    }


    public long Perft(int depth, boolean print, Map<Short, Long> map) {
        List<Short> moves = new ArrayList<>(legalMoves);
        int n_moves, i;
        int nodes = 0;
        if (depth == 0)
            return 1;
        n_moves = moves.size();
        for (i = 0; i < n_moves; i++) {
            makeMove(moves.get(i));
            long l = Perft(depth - 1, false, map);
            nodes += l;
            if (print)
                map.put(moves.get(i), l);
            undo();
        }
        if (print) {
            System.out.println("Moves: " + n_moves);
            System.out.println("Nodes: " + nodes);
        }
        return nodes;
    }

    public static void print(String s){
        System.out.print(s);
    }
    public static void println(String s){
        System.out.println(s);
    }
    public static void print(char s){
        System.out.print(s);
    }
    public static void println(char s){
        System.out.println(s);
    }
}
