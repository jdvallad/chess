import java.util.*;

public class chessboard {
    //static methods
    public static void print(Object... args) {
        //Sensible print method so I don't go insane.
        //If one element, prints it with a linebreak.
        //Otherwise, prints all elements with spaces in between and NO linebreak.
        if (args.length == 1) {
            System.out.println(args[0]);
        } else {
            StringBuilder res = new StringBuilder();
            for (Object o : args) {
                res.append(o);
            }
            System.out.print(res);
        }
    }

    private static final HashMap<String, Long> fr_temp = fr_initializer();

    private static HashMap<String, Long> fr_initializer() {
        HashMap<String, Long> a = new HashMap<>();
        for (char r : new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'}) {
            a.put("" + r, board_builder(r));
            a.put("~" + r, ~board_builder(r));
        }
        for (int i = 1; i < 9; i++) {
            a.put("" + i, board_builder(i));
            a.put("-" + i, ~board_builder(i));
        }
        return a;
    }

    public static long fr(Object a) {
        return fr_temp.get("" + a);
    }

    public static long fr_or(Object... args) {
        long res = 0;
        for (Object o : args) {
            res |= fr(o);
        }
        return res;
    }

    public static long fr_and(Object... args) {
        long res = fr(args[0]);
        for (Object o : args) {
            res &= fr(o);
        }
        return res;
    }

    public static int rank(int a) {
        return 8 - ((63 - a) >> 3);
    }

    public static char file(int a) {
        return (char) (((63 - a) & 7) + 97);
    }

    public static String file_and_rank(int a) {
        return "" + file(a) + rank(a);
    }

    public static long n(long a) {
        return a >>> 8 & fr(-1);
    }

    public static long s(long a) {
        return a << 8 & fr(-8);
    }

    public static long e(long a) {
        return (a << 1) & fr("~a");
    }

    public static long w(long a) {
        return (a >>> 1) & fr("~h");
    }

    public static long nw(long a) {
        return n(w(a));
    }

    public static long ne(long a) {
        return n(e(a));
    }

    public static long se(long a) {
        return s(e(a));
    }

    public static long sw(long a) {
        return s(w(a));
    }


    public static long nne(long a) {
        return n(ne(a));
    }

    public static long nee(long a) {
        return ne(e(a));
    }

    public static long nnw(long a) {
        return n(nw(a));
    }

    public static long nww(long a) {
        return nw(w(a));
    }

    public static long sse(long a) {
        return s(se(a));
    }

    public static long see(long a) {
        return se(e(a));
    }

    public static long ssw(long a) {
        return s(sw(a));
    }

    public static long sww(long a) {
        return sw(w(a));
    }

    public static ArrayList<String> piece_instances(long k) {
        ArrayList<String> res = new ArrayList<>();
        for (int i = 0; i < 64; i++) {
            if ((k >>> i & 1) == 1) {
                res.add("" + file(i) + rank(i));
            }
        }
        return res;
    }

    public static long board_builder(char a) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < 64; i++) {
            if (file(i) == a) {
                str.append('1');
            } else {
                str.append('0');
            }
        }
        return Long.parseUnsignedLong(str.toString(), 2);
    }

    public static long board_builder(String a) {
        if (a.length() == 1) {
            StringBuilder str = new StringBuilder();
            for (int i = 0; i < 64; i++) {
                if (("" + file(i)).equals(a)) {
                    str.append('1');
                } else {
                    str.append('0');
                }
            }
            return Long.parseUnsignedLong(str.toString(), 2);
        }
        assert (a.length() == 2);
        char f = a.charAt(0);
        int r = Integer.parseInt("" + a.charAt(1));
        return board_builder(f, r);
    }

    public static long board_builder(int b) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < 64; i++) {
            if (rank(i) == b) {
                str.append('1');
            } else {
                str.append('0');
            }
        }
        return Long.parseUnsignedLong(str.toString(), 2);
    }

    public static long board_builder(char a, int b) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < 64; i++) {
            if (file(i) == a && rank(i) == b) {
                str.append('1');
            } else {
                str.append('0');
            }
        }
        return Long.parseUnsignedLong(str.toString(), 2);
    }

    public static long board_builder(String a, int b) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < 64; i++) {
            if (("" + file(i)).equals(a) && rank(i) == b) {
                str.append('1');
            } else {
                str.append('0');
            }
        }
        return Long.parseUnsignedLong(str.toString(), 2);
    }

    public static void print_bitboard(long a) {
        System.out.print("8 | ");
        for (int i = 0; i < 64; i++) {
            long temp = (a) >>> i & 1;
            System.out.print((temp != 0 ? temp : "*") + " ");
            if ((i + 1) % 8 == 0 && i != 63) {
                System.out.print("\r\n" + (7 - (i >>> 3)) + " | ");
            }
        }
        System.out.println("\r\n  -----------------");
        System.out.println("    a b c d e f g h");
    }

    //class instance methods
    HashMap<String, Long> pieces;
    ArrayList<String> allMovesMade = new ArrayList<>();
    HashSet<String> legalMoves = new HashSet<>();
    ArrayList<String> fenList = new ArrayList<>();
    Boolean[] castleRights = new Boolean[4];
    String turn;
    String enPassant;
    String fen;
    int halfmoveClock;
    int fullmoveNumber;

    public chessboard() {
        setFromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }

    public chessboard(String f) {
        setFromFEN(f);
    }

    public long pieces(String s) {
        return pieces.get(s);
    }

    public long pieces(char s) {
        return pieces.get("" + s);
    }

    public void setFromFEN(String f) {
        pieces = pieces_initializer();
        pieces.replaceAll((k, v) -> 0L);
        String[] fen = f.split("/");
        for (int i = 0; i < 7; i++) {
            int index = 0;
            for (char c : fen[i].toCharArray()) {
                if (c >= '1' && c <= '8')
                    index += Integer.parseInt("" + c);
                else {
                    pieces.put("" + c, pieces.get("" + c) | board_builder(((char) (97 + index)), 8 - i));
                    index++;
                }
            }
        }
        String[] last = fen[7].split(" ");
        int index = 0;
        for (char c : last[0].toCharArray()) {
            if (c >= '1' && c <= '8')
                index += Integer.parseInt("" + c);
            else {
                pieces.put("" + c, pieces.get("" + c) | board_builder(((char) (97 + index)), 1));
                index++;
            }
        }
        if (last[1].equals("w"))
            turn = "white";
        else
            turn = "black";
        ArrayList<Character> temp = new ArrayList<>();
        for (char c : last[2].toCharArray())
            temp.add(c);
        castleRights[0] = temp.contains('K');
        castleRights[1] = temp.contains('Q');
        castleRights[2] = temp.contains('k');
        castleRights[3] = temp.contains('q');
        if (!last[3].equals("-"))
            enPassant = last[3];
        halfmoveClock = Integer.parseInt(last[4]);
        fullmoveNumber = Integer.parseInt(last[5]);
        allMovesMade.clear();
        fenList.clear();
        legalMoves = updateLegalMoves();
        this.fen = f;
        fenList.add(this.fen);
    }

    public void clear() {
        setFromFEN("8/8/8/8/8/8/8/8 w - - 0 1");
    }

    public void draw_board() {
        print("\r\n8 | ", "");
        for (int k = 0; k < 64; k++) {
            ArrayList<String> temp = new ArrayList<>();
            for (String s : pieces.keySet()) {
                if ((pieces(s) >>> k & 1) == 1L) {
                    temp.add(s);
                }
            }
            print(temp.size() == 1 ? temp.get(0) : "*", " ");
            if ((k + 1) % 8 == 0 && (k != 63)) {
                print("\r\n" + (7 - (k >>> 3)) + " | ", "");
            }
        }
        print("\r\n  -----------------");
        print("    a b c d e f g h");
    }

    public char pieceAt(String str) {
        int one = (str.charAt(0) - 97) + 8 * (8 - Integer.parseInt("" + str.charAt(1)));
        for (String key : pieces.keySet()) {
            if (((pieces.get(key) >>> one & 1) == 1L)) {
                return key.charAt(0);
            }
        }
        return ' ';
    }

    private static HashMap<String, Long> pieces_initializer() {
        HashMap<String, Long> a = new HashMap<>();
        a.put("P", 71776119061217280L);
        a.put("R", -9151314442816847872L);
        a.put("N", 4755801206503243776L);
        a.put("B", 2594073385365405696L);
        a.put("Q", 576460752303423488L);
        a.put("K", 1152921504606846976L);
        a.put("p", 65280L);
        a.put("r", 129L);
        a.put("n", 66L);
        a.put("b", 36L);
        a.put("q", 8L);
        a.put("k", 16L);
        return a;
    }

    public HashSet<String> updateLegalMoves() {
        return new HashSet<String>();
    }

    public void reset() {
        fen = fenList.get(0);
        setFromFEN(fen);
    }

    public void updateCastleRights(String m) {
        String[] move = new String[]{m.substring(0, 2), m.substring(2)};
        char from = pieceAt(move[1]);
        if (from == 'K') { //white king move
            castleRights[0] = castleRights[1] = false;
        }
        if (from == 'k') { //black king move
            castleRights[2] = castleRights[3] = false;
        }
        if (castleRights[0] && from == 'R' && move[0].equals("h1")) { //white kingside rook move
            castleRights[0] = false;
        }
        if (castleRights[1] && from == 'R' && move[0].equals("a1")) { //white queenside rook move
            castleRights[1] = false;
        }
        if (castleRights[2] && from == 'r' && move[0].equals("h8")) { //black kingside rook move
            castleRights[2] = false;
        }
        if (castleRights[3] && from == 'r' && move[0].equals("a8")) { //black queenside rook move
            castleRights[3] = false;
        }
    }

    private String fen() {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int count = 0;
            for (int c = 0; c < 8; c++) {
                ArrayList<String> start = new ArrayList<>();
                for (String p : pieces.keySet()) {
                    if ((pieces(p) >>> (i * 8 + c) & 1) == 1L) {
                        start.add(p);
                    }
                }
                if (start.size() == 0) {
                    count += 1;
                } else {
                    if (count != 0) {
                        res.append(count);
                        count = 0;
                    }
                    res.append(start.get(0));
                }
            }
            if (count != 0) {
                res.append(count);
            }
            if (i != 7) {
                res.append("/");
            }
        }
        res.append(turn.equals("white") ? " w " : " b ");
        int count = 0;
        if (castleRights[0]) {
            res.append("K");
            count++;
        }
        if (castleRights[1]) {
            res.append("Q");
            count++;
        }
        if (castleRights[2]) {
            res.append("k");
            count++;
        }
        if (castleRights[3]) {
            res.append("q");
            count++;
        }
        if (count == 0) {
            res.append("-");
        }
        if (enPassant.equals("")) {
            res.append(" - ");
        } else {
            res.append(" ").append(enPassant);
        }
        res.append(" ").append(halfmoveClock).append(" ").append(fullmoveNumber);
        return res.toString();
    }

    public void checkforCastleMove(String m) {
        if (castleRights[0] && m.equals("e1g1")) {
            pieces.put("R", pieces("R") & ~board_builder("h1")); //deletes rook from origin square
            pieces.put("R", pieces("R") | board_builder("f1")); //puts rook on destination square
        }
        if (castleRights[1] && m.equals("e1c1")) {
            pieces.put("R", pieces("R") & ~board_builder("a1")); //deletes rook from origin square
            pieces.put("R", pieces("R") | board_builder("d1")); //puts rook on destination square
        }
        if (castleRights[2] && m.equals("e8g8")) {
            pieces.put("r", pieces("r") & ~board_builder("h8")); //deletes rook from origin square
            pieces.put("r", pieces("r") | board_builder("f8")); //puts rook on destination square
        }
        if (castleRights[3] && m.equals("e8c8")) {
            pieces.put("r", pieces("r") & ~board_builder("a8")); //deletes rook from origin square
            pieces.put("r", pieces("r") | board_builder("d8")); //puts rook on destination square
        }
        if (castleRights[0] || castleRights[1] || castleRights[2] || castleRights[3]) {
            updateCastleRights(m); //if any castling available, check for loss of castling rights
        }
    }

    public void checkForEnPassant(String m) {
        String[] move = new String[]{m.substring(0, 2), m.substring(2)};
        char from = pieceAt(move[1]);
        if (move[1].equals(enPassant)) {
            if (from == 'P') {
                String temp = "" + move[1].charAt(0) + '5';
                pieces.put("p", pieces('p') & ~board_builder(temp));
            }
            if (from == 'p') {
                String temp = "" + move[1].charAt(0) + '4';
                pieces.put("P", pieces('P') & ~board_builder(temp));
            }
        }
        enPassant = ""; //resets enPassant
        if (from == 'p' && move[0].charAt(1) == '7' && move[1].charAt(1) == '5') {
            enPassant = "" + move[1].charAt(0) + '6'; //sets last enPassant if black pawn
        }
        if (from == 'P' && move[0].charAt(1) == '2' && move[1].charAt(1) == '4') {
            enPassant = "" + move[1].charAt(0) + '3';  //sets last enPassant if white pawn
        }
    }

    public void setHalfMoveClock(char to, char from, String move) {
        if (to != ' ')
            halfmoveClock = 0; //reset clock due to capture
        else if ((from == 'p') || (from == 'P'))
            halfmoveClock = 0; //increment clock due to non-capture/non-pawn-push
        else
            halfmoveClock++; //reset clock due to pawn push
    }

    public void makeMove(String m) {
        String[] move = new String[]{m.substring(0, 2), m.substring(2)}; //splits move into origin and destination squares
        char from = pieceAt(move[0]); //gets char for origin piece. e.g. 'P'
        char to = pieceAt(move[1]); //gets char for captured piece.
        // If no piece captured or capture is an en passant capture, will be ' '.

        if (to != ' ')
            pieces.put("" + to, pieces(to) & ~board_builder(move[1])); //if piece at destination square, removes it

        pieces.put("" + from, pieces(from) & ~board_builder(move[0])); //deletes moving piece from origin square
        pieces.put("" + from, pieces(from) | board_builder(move[1])); //puts moving piece on destination square

        setHalfMoveClock(to, from, move[1]); //sets HalfMoveClock accordingly

        if (turn.equals("black"))
            fullmoveNumber++; //increment fullMoveNumber

        checkforCastleMove(m); //move rook to proper position if move is castling

        checkForEnPassant(m); //capture pawn for en passant move

        turn = (turn.equals("white")) ? "black" : "white"; //switch turn

        allMovesMade.add(m); //add move made to allMovesMade

        legalMoves = updateLegalMoves(); //update list of legal Moves
        fen = fen();
    }
}
