import org.jetbrains.annotations.NotNull;

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
        return 8 - ((63 - a) >>> 3);
    }

    public static char file(int a) {
        return (char) (((63 - a) & 7) + 97);
    }

    public static String file_and_rank(int a) {
        return "" + file(a) + rank(a);
    }


    public static String n(String s) {
        if (s.equals("-1") || s.charAt(1) >= '8') {
            return "-1";
        }
        return "" + s.charAt(0) + (Integer.parseInt("" + s.charAt(1)) + 1);
    }

    public static String s(String s) {
        if (s.equals("-1") || s.charAt(1) <= '1') {
            return "-1";
        }
        return "" + s.charAt(0) + (Integer.parseInt("" + s.charAt(1)) - 1);
    }

    public static String e(String s) {
        if (s.equals("-1") || s.charAt(0) == 'h') {
            return "-1";
        }
        return "" + ((char) (s.charAt(0) + 1)) + s.charAt(1);
    }

    public static String w(String s) {
        if (s.equals("-1") || s.charAt(0) == 'a') {
            return "-1";
        }
        return "" + ((char) (s.charAt(0) - 1)) + s.charAt(1);
    }

    public static String nw(String a) {
        return n(w(a));
    }

    public static String ne(String a) {
        return n(e(a));
    }

    public static String se(String a) {
        return s(e(a));
    }

    public static String sw(String a) {
        return s(w(a));
    }


    public static String nne(String a) {
        return n(ne(a));
    }

    public static String nee(String a) {
        return ne(e(a));
    }

    public static String nnw(String a) {
        return n(nw(a));
    }

    public static String nww(String a) {
        return nw(w(a));
    }

    public static String sse(String a) {
        return s(se(a));
    }

    public static String see(String a) {
        return se(e(a));
    }

    public static String ssw(String a) {
        return s(sw(a));
    }

    public static String sww(String a) {
        return sw(w(a));
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

    public static long board_builder(@NotNull String a) {
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

    public static ArrayList<String> longToStrings(Long l) {
        ArrayList<String> res = new ArrayList<>();
        for (int i = 63; i >= 0; i--) {
            if ((l >>> i & 1) == 1L)
                res.add(file_and_rank(63 - i));
        }
        return res;
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
    ArrayList<String> fenList = new ArrayList<>();
    ArrayList<String> extraAllMovesMade = new ArrayList<>();
    ArrayList<String> extraFenList = new ArrayList<>();

    HashSet<String> legalMoves = new HashSet<>();
    HashSet<String> psuedoLegalMoves = new HashSet<>();
    Boolean[] castleRights = new Boolean[4];
    String turn;
    String enPassant = "";
    String fen;
    int halfMoveClock;
    int fullMoveNumber;
    boolean gameOver = false;
    String result = "";

    public chessboard() {
        setFromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }

    public chessboard(String f) {
        setFromFEN(f);
    }

    public chessboard(String f, String strs) {
        setFromFEN(f, strs);
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
        halfMoveClock = Integer.parseInt(last[4]);
        fullMoveNumber = Integer.parseInt(last[5]);
        allMovesMade.clear();
        fenList.clear();
        this.fen = f;
        fenList.add(this.fen);
        psuedoLegalMoves = updatePsuedoLegalMoves();
        legalMoves = updateLegalMoves();
        gameOver = false;
        checkForGameOver();
    }

    public void checkForGameOver() {
        if (legalMoves.size() == 0) {
            gameOver = true;
            if (inCheck()) {
                result = (turn.equals("white")) ? "black wins!" : "white wins!";
            } else {
                gameOver = true;
                result = "draw by statemate!";
            }
            return;
        }
        if (halfMoveClock == 50) {
            gameOver = true;
            result = "draw by 50 move rule!";
            legalMoves.clear();
            return;
        }
        int occurrences = Collections.frequency(shortenedFenList(), shortenedFen(fen));
        if (occurrences >= 5) {
            gameOver = true;
            result = "draw by 5-fold repetition!";
            legalMoves.clear();
            return;
        }
    }

    public chessboard(chessboard tempBoard) {
        this(tempBoard.fen());
        tempBoard.fenList = new ArrayList<>(fenList);
        tempBoard.allMovesMade = new ArrayList<>(allMovesMade);
        tempBoard.gameOver = gameOver;
    }
    public chessboard nextBoardAfterMove(String move){
        chessboard temp = new chessboard(this);
        temp.makeMove(move);
        return temp;
    }
    public ArrayList<String> shortenedFenList() {
        ArrayList<String> temp = new ArrayList<>();
        for (String s : fenList) {
            temp.add(shortenedFen(s));
        }
        return temp;
    }

    public String shortenedFen(String s) {
        String[] str = s.split(" ");
        return str[0] + " " + str[1] + " " + str[2] + " " + str[3];
    }

    public void setFromFEN(String f, String strs) {
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
        halfMoveClock = Integer.parseInt(last[4]);
        fullMoveNumber = Integer.parseInt(last[5]);
        allMovesMade.clear();
        fenList.clear();
        this.fen = f;
    }

    public void clear() {
        setFromFEN("8/8/8/8/8/8/8/8 w - - 0 1");
    }

    public void drawBoard() {
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

    public HashSet<String> updatePsuedoLegalMoves() {
        HashSet<String> res = new HashSet<>();
        res.addAll(legalPawnMoves(turn.equals("white") ? 'P' : 'p'));
        res.addAll(legalRookMoves(turn.equals("white") ? 'R' : 'r'));
        res.addAll(legalKnightMoves(turn.equals("white") ? 'N' : 'n'));
        res.addAll(legalBishopMoves(turn.equals("white") ? 'B' : 'b'));
        res.addAll(legalQueenMoves(turn.equals("white") ? 'Q' : 'q'));
        res.addAll(legalKingMoves(turn.equals("white") ? 'K' : 'k'));
        return res;
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
            res.append(" -");
        } else {
            res.append(" ").append(enPassant);
        }
        res.append(" ").append(halfMoveClock).append(" ").append(fullMoveNumber);
        return res.toString();
    }

    public void rollback() {
        if (fenList.size() == 1)
            return;
        String temp = fenList.remove(fenList.size() - 1);
        extraFenList.add(0, temp);
        temp = allMovesMade.remove(allMovesMade.size() - 1);
        extraAllMovesMade.add(0, temp);
        ArrayList<String> tempFENList = new ArrayList<>(fenList);
        ArrayList<String> tempAllMovesMade = new ArrayList<>(allMovesMade);
        fen = fenList.get(fenList.size() - 1);
        setFromFEN(fen);
        fenList = tempFENList;
        allMovesMade = tempAllMovesMade;
    }

    public void rollForward() {
        if (extraFenList.size() == 0) {
            return;
        }
        String temp = extraFenList.remove(0);
        fenList.add(temp);
        temp = extraAllMovesMade.remove(0);
        allMovesMade.add(temp);
        ArrayList<String> tempFENList = new ArrayList<>(fenList);
        ArrayList<String> tempAllMovesMade = new ArrayList<>(allMovesMade);
        fen = fenList.get(fenList.size() - 1);
        setFromFEN(fen);
        fenList = tempFENList;
        allMovesMade = tempAllMovesMade;
    }

    public void rollback(int n) {
        for (int i = 0; i < n; i++)
            rollback();
    }

    public void rollForward(int n) {
        for (int i = 0; i < n; i++)
            rollForward();
    }

    public void checkForCastleMove(String m) {
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

    public void checkForPromotion(String m) {
        String[] move = new String[]{m.substring(0, 2), m.substring(2)}; //splits move into origin and destination squares
        char from = pieceAt(move[1]); //gets char for destination piece.
        if (from == 'P' && move[1].charAt(1) == '8') {
            pieces.put("P", pieces("P") & ~board_builder(move[1])); //deletes pawn from promotion square
            pieces.put("Q", pieces("Q") | board_builder(move[1])); //puts queen on promotion square
        }
        if (from == 'p' && move[1].charAt(1) == '1') {
            pieces.put("p", pieces("p") & ~board_builder(move[1])); //deletes pawn from promotion square
            pieces.put("q", pieces("q") | board_builder(move[1])); //puts queen on promotion square
        }
    }

    public void setHalfMoveClock(char to, char from, String move) {
        if (to != ' ')
            halfMoveClock = 0; //reset clock due to capture
        else if ((from == 'p') || (from == 'P'))
            halfMoveClock = 0; //increment clock due to non-capture/non-pawn-push
        else
            halfMoveClock++; //reset clock due to pawn push
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
            fullMoveNumber++; //increment fullMoveNumber

        checkForCastleMove(m); //move rook to proper position if move is castling

        checkForEnPassant(m); //capture pawn for en passant move

        checkForPromotion(m);

        turn = (turn.equals("white")) ? "black" : "white"; //switch turn

        allMovesMade.add(m); //add move made to allMovesMade
        psuedoLegalMoves = updatePsuedoLegalMoves();
        fen = fen();
        legalMoves = updateLegalMoves(); //update list of legal Moves
        fenList.add(fen);
        extraAllMovesMade.clear();
        extraFenList.clear();
        checkForGameOver();
    }

    public void makeMove(String m, String strs) {
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
            fullMoveNumber++; //increment fullMoveNumber

        checkForCastleMove(m); //move rook to proper position if move is castling

        checkForEnPassant(m); //capture pawn for en passant move

        checkForPromotion(m);

        turn = (turn.equals("white")) ? "black" : "white"; //switch turn

        allMovesMade.add(m); //add move made to allMovesMade
        fen = fen();
    }

    public Long whiteOccupied() {
        return pieces('P') | pieces('R') | pieces('N') | pieces('B') | pieces('Q') | pieces('K');
    }

    public Long blackOccupied() {
        return pieces('p') | pieces('r') | pieces('n') | pieces('b') | pieces('q') | pieces('k');
    }

    public long occupied() {
        return whiteOccupied() | blackOccupied();
    }

    public ArrayList<String> legalKnightMoves(char c) {
        ArrayList<String> res = new ArrayList<>();
        long notSameColorOccupied = Character.isUpperCase(c) ? ~whiteOccupied() : ~blackOccupied();
        for (String str : longToStrings(
                nne(pieces(c)) & notSameColorOccupied
        )) {
            res.add(ssw(str) + str);
        }
        for (String str : longToStrings(
                nee(pieces(c)) & notSameColorOccupied)) {
            res.add(sww(str) + str);
        }
        for (String str : longToStrings(
                nnw(pieces(c)) & notSameColorOccupied)) {
            res.add(sse(str) + str);
        }
        for (String str : longToStrings(
                nww(pieces(c)) & notSameColorOccupied)) {
            res.add(see(str) + str);
        }
        for (String str : longToStrings(
                sse(pieces(c)) & notSameColorOccupied)) {
            res.add(nnw(str) + str);
        }
        for (String str : longToStrings(
                see(pieces(c)) & notSameColorOccupied)) {
            res.add(nww(str) + str);
        }
        for (String str : longToStrings(
                ssw(pieces(c)) & notSameColorOccupied)) {
            res.add(nne(str) + str);
        }
        for (String str : longToStrings(
                sww(pieces(c)) & notSameColorOccupied)) {
            res.add(nee(str) + str);
        }
        return res;
    }

    public ArrayList<String> legalPawnMoves(char c) {
        ArrayList<String> res = new ArrayList<>();
        for (String str : longToStrings(
                pawnsThatCanDoublePush(c))) {
            res.add(str + (isWhite(c) ? n(n(str)) : s(s(str))));
        }
        for (String str : longToStrings(
                pawnsThatCanPush(c))) {
            res.add(str + (isWhite(c) ? n(str) : s(str)));
        }
        for (String str : longToStrings(
                pawnsThatCanCaptureEast(c))) {
            res.add(str + (isWhite(c) ? n(e(str)) : s(e(str))));
        }
        for (String str : longToStrings(
                pawnsThatCanCaptureWest(c))) {
            res.add(str + (isWhite(c) ? n(w(str)) : s(w(str))));
        }
        return res;
    }

    public ArrayList<String> legalKingMoves(char c) {
        ArrayList<String> res = new ArrayList<>();
        long king = pieces(c);
        String kong = longToStrings(king).get(0);
        king |= n(king);
        king |= s(king);
        king |= e(king);
        king |= w(king);
        king &= isWhite(c) ? ~whiteOccupied() : ~blackOccupied();
        for (String str : longToStrings(king)) {
            res.add(kong + str);
        }
        res.addAll(legalKingCastleMoves(c));
        return res;
    }

    public ArrayList<String> legalKingCastleMoves(char c) {
        ArrayList<String> res = new ArrayList<>();
        long moves = 0L;
        long empty = ~occupied();
        if (isWhite(c)) {
            if (castleRights[0]) {
                moves |= board_builder("g1") & e(empty);
            }
            if (castleRights[1]) {
                moves |= board_builder("c1") & w(empty) & e(empty);
            }
        }
        if (isBlack(c)) {
            if (castleRights[2]) {
                moves |= board_builder("g8") & e(empty);
            }
            if (castleRights[3]) {
                moves |= board_builder("c8") & w(empty) & e(empty);
            }
        }
        for (String str : longToStrings(moves)) {
            res.add((str.charAt(1) == '1' ? "e1" : "e8") + str);
        }
        return res;
    }

    public ArrayList<String> legalBishopMoves(char c) {
        ArrayList<String> res = new ArrayList<>();
        for (String str : longToStrings(pieces(c))) {
            res.addAll(bishopSouthWestMoves(c, str));
            res.addAll(bishopSouthEastMoves(c, str));
            res.addAll(bishopNorthWestMoves(c, str));
            res.addAll(bishopNorthEastMoves(c, str));
        }
        return res;
    }

    public HashSet<String> updateLegalMoves() {
        HashSet<String> res = new HashSet<>();
        for (String str : psuedoLegalMoves) {
            if (!nextBoard(str).kingCanBeCaptured()) {
                res.add(str);
            }
        }
        turn = turn.equals("white") ? "black" : "white";
        HashSet<String> temp = updatePsuedoLegalMoves();
        turn = turn.equals("white") ? "black" : "white";
        for (String str : temp) {
            if (res.contains("e1g1") && str.substring(2).equals("g1") || str.substring(2).equals("f1") || inCheck()) {
                res.remove("e1g1");
            }
            if (res.contains("e1c1") && str.substring(2).equals("d1") || str.substring(2).equals("c1") || inCheck()) {
                res.remove("e1c1");
            }
            if (res.contains("e8g8") && str.substring(2).equals("g8") || str.substring(2).equals("f8") || inCheck()) {
                res.remove("e8g8");
            }
            if (res.contains("e8c8") && str.substring(2).equals("d8") || str.substring(2).equals("c8") || inCheck()) {
                res.remove("e8c8");
            }
        }
        return res;
    }

    public boolean inCheck() {
        char c = turn.equals("white") ? 'K' : 'k';
        String king = longToStrings(pieces(c)).get(0);
        turn = turn.equals("white") ? "black" : "white";
        HashSet<String> temp = updatePsuedoLegalMoves();
        turn = turn.equals("white") ? "black" : "white";
        for (String str : temp) {
            if (str.substring(2).equals(king)) {
                return true;
            }
        }
        return false;
    }

    public chessboard nextBoard(String move) {
        chessboard temp = new chessboard(fen, "shallow");
        temp.makeMove(move, "shallow");
        return temp;
    }

    public boolean squareWillBeAttacked(String square) {
        turn = turn.equals("white") ? "black" : "white";
        HashSet<String> temp = updatePsuedoLegalMoves();
        turn = turn.equals("white") ? "black" : "white";
        for (String str : temp) {
            if (str.substring(2).equals(square)) {
                return true;
            }
        }
        return false;
    }

    public boolean kingCanBeCaptured() {
        char c = turn.equals("white") ? 'k' : 'K';
        String king = longToStrings(pieces(c)).get(0);
        HashSet<String> temp = updatePsuedoLegalMoves();
        for (String str : temp) {
            if (str.substring(2).equals(king)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<String> legalRookMoves(char c) {
        ArrayList<String> res = new ArrayList<>();
        for (String str : longToStrings(pieces(c))) {
            res.addAll(rookNorthMoves(c, str));
            res.addAll(rookSouthMoves(c, str));
            res.addAll(rookEastMoves(c, str));
            res.addAll(rookWestMoves(c, str));
        }
        return res;
    }

    public ArrayList<String> legalQueenMoves(char c) {
        ArrayList<String> res = new ArrayList<>();
        res.addAll(legalRookMoves(c));
        res.addAll(legalBishopMoves(c));
        return res;
    }

    public ArrayList<String> bishopNorthEastMoves(char c, String str) {
        ArrayList<String> res = new ArrayList<>();
        String temp = str;
        while ((!ne(temp).equals("-1")) && pieceAt(ne(temp)) == ' ') {
            temp = ne(temp);
            res.add(str + temp);
        }
        if ((!ne(temp).equals("-1")) &&
                isWhite(c) ? isBlack(pieceAt(ne(temp))) : isWhite(pieceAt(ne(temp)))
        ) {
            temp = ne(temp);
            res.add(str + temp);
        }
        return res;
    }

    public ArrayList<String> bishopNorthWestMoves(char c, String str) {
        ArrayList<String> res = new ArrayList<>();
        String temp = str;
        while ((!nw(temp).equals("-1")) && pieceAt(nw(temp)) == ' ') {
            temp = nw(temp);
            res.add(str + temp);
        }
        if ((!nw(temp).equals("-1")) &&
                isWhite(c) ? isBlack(pieceAt(nw(temp))) : isWhite(pieceAt(nw(temp)))
        ) {
            temp = nw(temp);
            res.add(str + temp);
        }
        return res;
    }

    public ArrayList<String> bishopSouthEastMoves(char c, String str) {
        ArrayList<String> res = new ArrayList<>();
        String temp = str;
        while ((!se(temp).equals("-1")) && pieceAt(se(temp)) == ' ') {
            temp = se(temp);
            res.add(str + temp);
        }
        if ((!se(temp).equals("-1")) &&
                isWhite(c) ? isBlack(pieceAt(se(temp))) : isWhite(pieceAt(se(temp)))
        ) {
            temp = se(temp);
            res.add(str + temp);
        }
        return res;
    }

    public ArrayList<String> bishopSouthWestMoves(char c, String str) {
        ArrayList<String> res = new ArrayList<>();
        String temp = str;
        while ((!sw(temp).equals("-1")) && pieceAt(sw(temp)) == ' ') {
            temp = sw(temp);
            res.add(str + temp);
        }
        if ((!sw(temp).equals("-1")) &&
                isWhite(c) ? isBlack(pieceAt(sw(temp))) : isWhite(pieceAt(sw(temp)))
        ) {
            temp = sw(temp);
            res.add(str + temp);
        }
        return res;
    }

    public ArrayList<String> rookNorthMoves(char c, String str) {
        ArrayList<String> res = new ArrayList<>();
        String temp = str;
        while ((!n(temp).equals("-1")) && pieceAt(n(temp)) == ' ') {
            temp = n(temp);
            res.add(str + temp);
        }
        if ((!n(temp).equals("-1")) &&
                isWhite(c) ? isBlack(pieceAt(n(temp))) : isWhite(pieceAt(n(temp)))
        ) {
            temp = n(temp);
            res.add(str + temp);
        }
        return res;
    }

    public ArrayList<String> rookSouthMoves(char c, String str) {
        ArrayList<String> res = new ArrayList<>();
        String temp = str;
        while ((!s(temp).equals("-1")) && pieceAt(s(temp)) == ' ') {
            temp = s(temp);
            res.add(str + temp);
        }
        if ((!s(temp).equals("-1")) &&
                isWhite(c) ? isBlack(pieceAt(s(temp))) : isWhite(pieceAt(s(temp)))
        ) {
            temp = s(temp);
            res.add(str + temp);
        }
        return res;
    }

    public ArrayList<String> rookEastMoves(char c, String str) {
        ArrayList<String> res = new ArrayList<>();
        String temp = str;
        while ((!e(temp).equals("-1")) && pieceAt(e(temp)) == ' ') {
            temp = e(temp);
            res.add(str + temp);
        }
        if ((!e(temp).equals("-1")) &&
                isWhite(c) ? isBlack(pieceAt(e(temp))) : isWhite(pieceAt(e(temp)))
        ) {
            temp = e(temp);
            res.add(str + temp);
        }
        return res;
    }

    public ArrayList<String> rookWestMoves(char c, String str) {
        ArrayList<String> res = new ArrayList<>();
        String temp = str;
        while ((!w(temp).equals("-1")) && pieceAt(w(temp)) == ' ') {
            temp = w(temp);
            res.add(str + temp);
        }
        if ((!w(temp).equals("-1")) &&
                isWhite(c) ? isBlack(pieceAt(w(temp))) : isWhite(pieceAt(w(temp)))
        ) {
            temp = w(temp);
            res.add(str + temp);
        }
        return res;
    }

    public boolean isWhite(char c) {
        return Character.isUpperCase(c);
    }

    public boolean isBlack(char c) {
        return Character.isLowerCase(c);
    }

    public long pawnsThatCanDoublePush(char c) {
        long empty = ~occupied();
        if (isWhite(c)) {
            return pieces(c) & s(empty) & s(s(empty)) & fr(2);
        }
        if (isBlack(c)) {
            return pieces(c) & n(empty) & n(n(empty)) & fr(7);
        }
        return 0L;
    }

    public long pawnsThatCanPush(char c) {
        long empty = ~occupied();
        if (isWhite(c)) {
            return pieces(c) & s(empty);
        }
        if (isBlack(c)) {
            return pieces(c) & n(empty);
        }
        return 0L;
    }

    public long pawnsThatCanCaptureWest(char c) {
        long empty = ~occupied();
        long ep = !enPassant.equals("") ? board_builder(enPassant) : 0;
        if (isWhite(c)) {
            return pieces(c) & se(blackOccupied() | ep);
        }
        if (isBlack(c)) {
            return pieces(c) & ne(whiteOccupied() | ep);
        }
        return 0L;
    }

    public long pawnsThatCanCaptureEast(char c) {
        long empty = ~occupied();
        long ep = !enPassant.equals("") ? board_builder(enPassant) : 0;
        if (isWhite(c)) {
            return pieces(c) & sw(blackOccupied() | ep);
        }
        if (isBlack(c)) {
            return pieces(c) & nw(whiteOccupied() | ep);
        }
        return 0L;
    }

    public boolean isCapture(String s) {
        return (pieceAt(s.substring(2)) != ' ') || (s.substring(2).equals(enPassant) && (Character.toLowerCase(pieceAt(s.substring(0, 2))) == 'p'));
    }
}
