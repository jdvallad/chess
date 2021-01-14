import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Chess {


    //Begin Static ROWS and FILES
    static final long AFILE = 72340172838076673L;
    static final long BFILE = 144680345676153346L;
    static final long CFILE = 289360691352306692L;
    static final long DFILE = 578721382704613384L;
    static final long EFILE = 1157442765409226768L;
    static final long FFILE = 2314885530818453536L;
    static final long GFILE = 4629771061636907072L;
    static final long HFILE = -9187201950435737472L;
    static final long ROW1 = -72057594037927936L;
    static final long ROW2 = 71776119061217280L;
    static final long ROW3 = 280375465082880L;
    static final long ROW4 = 1095216660480L;
    static final long ROW5 = 4278190080L;
    static final long ROW6 = 16711680L;
    static final long ROW7 = 65280L;
    static final long ROW8 = 255L;

    //Begin Piece Instances
    long wP = 71776119061217280L;
    long wR = -9151314442816847872L;
    long wN = 4755801206503243776L;
    long wB = 2594073385365405696L;
    long wQ = 576460752303423488L;
    long wK = 1152921504606846976L;
    long bP = 65280L;
    long bR = 129L;
    long bN = 66L;
    long bB = 36L;
    long bQ = 8L;
    long bK = 16L;


    //Begin Move and FEN Lists
    Set<Short> legalMoves = new HashSet<>();
    Set<Short> psuedoLegalMoves = new HashSet<>();
    List<Short> allMovesMade = new ArrayList<>();
    List<Short> extraAllMovesMade = new ArrayList<>();
    List<String> fenList = new ArrayList<>();
    List<String> extraFenList = new ArrayList<>();


    //Begin additional class variables
    Boolean[] castleRights = new Boolean[4];
    String turn; //keeps track current turn, "white" or "black"
    String enPassant = ""; //keeps track of current en passant square.
    String fen; //stores fen of current board.
    int halfMoveClock; //keeps track of moves since last capture or pawn push.
    int fullMoveNumber;
    boolean gameOver = false;
    String result = ""; //result to be shown when game ends.


    //Begin Constructors
    public Chess() {
        setFromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }

    public Chess(String f) {
        setFromFEN(f);
    }

    private Chess(String f, String shadow) {
        if (!shadow.equals("filler text"))
            shadowSetFromFEN(f);
    }

    public Chess(Chess temp) {
        extraAllMovesMade = new ArrayList<>(temp.extraAllMovesMade);
        psuedoLegalMoves = new HashSet<>(temp.psuedoLegalMoves);
        extraFenList = new ArrayList<>(temp.extraFenList);
        allMovesMade = new ArrayList<>(temp.allMovesMade);
        legalMoves = new HashSet<>(temp.legalMoves);
        castleRights = temp.castleRights.clone();
        fenList = new ArrayList<>(temp.fenList);
        fullMoveNumber = temp.fullMoveNumber;
        wP = temp.wP;
        wR = temp.wR;
        wN = temp.wN;
        wB = temp.wB;
        wQ = temp.wQ;
        wK = temp.wK;
        bP = temp.bP;
        bR = temp.bR;
        bN = temp.bN;
        bB = temp.bB;
        bQ = temp.bQ;
        bK = temp.bK;
        halfMoveClock = temp.halfMoveClock;
        enPassant = temp.enPassant;
        gameOver = temp.gameOver;
        result = temp.result;
        turn = temp.turn;
        fen = temp.fen;
    }


    //Begin nextBoard Methods
    public Chess nextBoard(String move) {
        Chess temp = new Chess(this);
        temp.makeMove(move);
        return temp;
    }

    private Chess simpleNextBoard(String move) {
        Chess temp = new Chess(fen, "shadow");
        temp.simpleMove(move);
        return temp;
    }


    //Begin Move Methods
    public void makeMove(String m) {
        simpleMove(m);
        allMovesMade.add(Chess.encodeMove(m)); //add move made to allMovesMade
        psuedoLegalMoves = updatePsuedoLegalMoves();
        legalMoves = updateLegalMoves(); //update list of legal Moves
        fenList.add(fen);
        extraAllMovesMade.clear();
        extraFenList.clear();
        checkForGameOver();
    }

    public void simpleMove(String m) {
        String[] move = new String[]{m.substring(0, 2), m.substring(2)}; //splits move into origin and destination squares
        char from = pieceAt(move[0]); //gets char for origin piece. e.g. 'P'
        char to = pieceAt(move[1]); //gets char for captured piece.
        // If no piece captured or capture is an en passant capture, will be ' '.

        if (to != ' ')
            andPiece(to, ~boardBuilder(move[1])); //if piece at destination square, removes it
        andPiece(from, ~boardBuilder(move[0])); //deletes moving piece from origin square
        orPiece(from, boardBuilder(move[1])); //puts moving piece on destination square

        setHalfMoveClock(to, from); //sets HalfMoveClock accordingly

        if (turn.equals("black"))
            fullMoveNumber++; //increment fullMoveNumber

        checkForCastleMove(m); //move rook to proper position if move is castling

        checkForEnPassant(m); //capture pawn for en passant move

        checkForPromotion(m); //promotes pawn if on first of eighth rank.

        turn = (turn.equals("white")) ? "black" : "white"; //switch turn
        fen = fen();
    }

    public Short lastMove() {
        return allMovesMade.size() == 0 ? 0 : allMovesMade.get(allMovesMade.size() - 1);
    }

    public String moveType(String move) {
        boolean capture = isCapture(move);
        boolean castle = false;
        boolean promotion = false;
        String res = "";
        if (pieceAt(move.substring(0, 2)) == 'P' && move.charAt(3) == '8')
            promotion = true;
        if (pieceAt(move.substring(0, 2)) == 'p' && move.charAt(3) == '1')
            promotion = true;
        if (castleRights[0] && move.equals("e1g1"))
            castle = true;
        if (castleRights[1] && move.equals("e1c1"))
            castle = true;
        if (castleRights[2] && move.equals("e8g8"))
            castle = true;
        if (castleRights[3] && move.equals("e8c8"))
            castle = true;
        Chess shadow = nextBoard(move);
        if (shadow.gameOver)
            res += "gameOver-";
        if (shadow.inCheck())
            res += "check-";
        if (promotion)
            res += "promotion-";
        if (capture)
            res += "capture-";
        if (castle)
            res += "castle-";
        res += "move-";
        return res.substring(0, res.length() - 1);
    }


    //Begin Piece Methods
    public long pieces(char s) {
        return switch (s) {
            case 'P' -> wP;
            case 'R' -> wR;
            case 'N' -> wN;
            case 'B' -> wB;
            case 'Q' -> wQ;
            case 'K' -> wK;
            case 'p' -> bP;
            case 'r' -> bR;
            case 'n' -> bN;
            case 'b' -> bB;
            case 'q' -> bQ;
            case 'k' -> bK;
            default -> 0L;
        };
    }

    public void putPiece(char c, long l) {
        switch (c) {
            case 'P' -> wP = l;
            case 'R' -> wR = l;
            case 'N' -> wN = l;
            case 'B' -> wB = l;
            case 'Q' -> wQ = l;
            case 'K' -> wK = l;
            case 'p' -> bP = l;
            case 'r' -> bR = l;
            case 'n' -> bN = l;
            case 'b' -> bB = l;
            case 'q' -> bQ = l;
            case 'k' -> bK = l;
        }
    }

    public void orPiece(char c, long l) {
        switch (c) {
            case 'P' -> wP |= l;
            case 'R' -> wR |= l;
            case 'N' -> wN |= l;
            case 'B' -> wB |= l;
            case 'Q' -> wQ |= l;
            case 'K' -> wK |= l;
            case 'p' -> bP |= l;
            case 'r' -> bR |= l;
            case 'n' -> bN |= l;
            case 'b' -> bB |= l;
            case 'q' -> bQ |= l;
            case 'k' -> bK |= l;
        }
    }

    public void andPiece(char c, long l) {
        switch (c) {
            case 'P' -> wP &= l;
            case 'R' -> wR &= l;
            case 'N' -> wN &= l;
            case 'B' -> wB &= l;
            case 'Q' -> wQ &= l;
            case 'K' -> wK &= l;
            case 'p' -> bP &= l;
            case 'r' -> bR &= l;
            case 'n' -> bN &= l;
            case 'b' -> bB &= l;
            case 'q' -> bQ &= l;
            case 'k' -> bK &= l;
        }
    }

    public void resetPieces() {
        wP = wR = wN = wB = wQ = wK = bP = bR = bN = bB = bQ = bK = 0L;
    }

    public char pieceAt(String str) {
        print(str);
        int one = (str.charAt(0) - 97) + 8 * (8 - Integer.parseInt("" + str.charAt(1)));
        for (char s : new char[]{'P', 'R', 'N', 'B', 'Q', 'K', 'p', 'r', 'n', 'b', 'q', 'k'}) {
            if (((pieces(s) >>> one & 1) == 1L)) {
                return s;
            }
        }
        return ' ';
    }

    private void pieces_initializer() {
        wP = 71776119061217280L;
        wR = -9151314442816847872L;
        wN = 4755801206503243776L;
        wB = 2594073385365405696L;
        wQ = 576460752303423488L;
        wK = 1152921504606846976L;
        bP = 65280L;
        bR = 129L;
        wN = 66L;
        wB = 36L;
        wQ = 8L;
        wK = 16L;
    }


    //Begin FEN Methods
    private String fen() {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int count = 0;
            for (int c = 0; c < 8; c++) {
                char start = ' ';
                for (char s : new char[]{'P', 'R', 'N', 'B', 'Q', 'K', 'p', 'r', 'n', 'b', 'q', 'k'}) {
                    if ((pieces(s) >>> (i * 8 + c) & 1) == 1L) {
                        start = s;
                    }
                }
                if (start == ' ') {
                    count += 1;
                } else {
                    if (count != 0) {
                        res.append(count);
                        count = 0;
                    }
                    res.append(start);
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

    public void setFromFEN(String f) {
        resetPieces();
        String[] fen = f.split("/");
        for (int i = 0; i < 7; i++) {
            int index = 0;
            for (char c : fen[i].toCharArray()) {
                if (c >= '1' && c <= '8')
                    index += Integer.parseInt("" + c);
                else {
                    orPiece(c, boardBuilder(((char) (97 + index)), 8 - i));
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
                orPiece(c, boardBuilder(((char) (97 + index)), 1));
                index++;
            }
        }
        if (last[1].equals("w"))
            turn = "white";
        else
            turn = "black";
        List<Character> temp = new ArrayList<>();
        for (char c : last[2].toCharArray())
            temp.add(c);
        castleRights[0] = temp.contains('K');
        castleRights[1] = temp.contains('Q');
        castleRights[2] = temp.contains('k');
        castleRights[3] = temp.contains('q');
        if (!last[3].equals("-"))
            enPassant = last[3];
        else
            enPassant = "";
        halfMoveClock = Integer.parseInt(last[4]);
        fullMoveNumber = Integer.parseInt(last[5]);
        allMovesMade.clear();
        fenList.clear();
        this.fen = f;
        fenList.add(this.fen);
        psuedoLegalMoves = updatePsuedoLegalMoves();
        legalMoves = updateLegalMoves();
        gameOver = false;
        result = "";
        checkForGameOver();
    }

    public void shadowSetFromFEN(String f) {
        resetPieces();
        String[] fen = f.split("/");
        for (int i = 0; i < 7; i++) {
            int index = 0;
            for (char c : fen[i].toCharArray()) {
                if (c >= '1' && c <= '8')
                    index += Integer.parseInt("" + c);
                else {
                    orPiece(c, boardBuilder(((char) (97 + index)), 8 - i));
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
                orPiece(c, boardBuilder(((char) (97 + index)), 1));
                index++;
            }
        }
        if (last[1].equals("w"))
            turn = "white";
        else
            turn = "black";
        List<Character> temp = new ArrayList<>();
        for (char c : last[2].toCharArray())
            temp.add(c);
        castleRights[0] = temp.contains('K');
        castleRights[1] = temp.contains('Q');
        castleRights[2] = temp.contains('k');
        castleRights[3] = temp.contains('q');
        if (!last[3].equals("-"))
            enPassant = last[3];
        enPassant = "";
        halfMoveClock = Integer.parseInt(last[4]);
        fullMoveNumber = Integer.parseInt(last[5]);
        allMovesMade.clear();
        fenList.clear();
        this.fen = f;
    }

    private List<String> shortenedFenList() {
        List<String> temp = new ArrayList<>();
        for (String s : fenList) {
            temp.add(shortenedFen(s));
        }
        return temp;
    }

    public String shortenedFen(String s) {
        String[] str = s.split(" ");
        return str[0] + " " + str[1] + " " + str[2] + " " + str[3];
    }


    //Begin Move List Update Methods
    private Set<Short> updatePsuedoLegalMoves() {
        Set<Short> res = new HashSet<>();
        res.addAll(legalPawnMoves(turn.equals("white") ? 'P' : 'p'));
        res.addAll(legalRookMoves(turn.equals("white") ? 'R' : 'r'));
        res.addAll(legalKnightMoves(turn.equals("white") ? 'N' : 'n'));
        res.addAll(legalBishopMoves(turn.equals("white") ? 'B' : 'b'));
        res.addAll(legalQueenMoves(turn.equals("white") ? 'Q' : 'q'));
        res.addAll(legalKingMoves(turn.equals("white") ? 'K' : 'k'));
        return res;
    }

    public Set<Short> updateLegalMoves() {
        Set<Short> res = new HashSet<>();
        for (short str : psuedoLegalMoves) {
            if (!simpleNextBoard(Chess.decodeMove(str)).kingCanBeCaptured()) {
                res.add(str);
            }
        }
        turn = turn.equals("white") ? "black" : "white";
        Set<Short> temp = updatePsuedoLegalMoves();
        turn = turn.equals("white") ? "black" : "white";
        for (Short sh : temp) {
            String str = decodeMove(sh);
            if (res.contains(Chess.encodeMove("e1g1")) && castleRights[0] && (str.substring(2).equals("g1") || str.substring(2).equals("f1") || inCheck())) {
                res.remove(Chess.encodeMove("e1g1"));
            }
            if (res.contains(encodeMove("e1c1")) && castleRights[1] && (str.substring(2).equals("d1") || str.substring(2).equals("c1") || inCheck())) {
                res.remove(encodeMove("e1c1"));
            }
            if (res.contains(encodeMove("e8g8")) && castleRights[2] && (str.substring(2).equals("g8") || str.substring(2).equals("f8") || inCheck())) {
                res.remove(encodeMove("e8g8"));
            }
            if (res.contains(encodeMove("e8c8")) && castleRights[3] && (str.substring(2).equals("d8") || str.substring(2).equals("c8") || inCheck())) {
                res.remove(encodeMove("e8c8"));
            }
        }
        return res;
    }


    //Begin MakeMove Helpers
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
        boolean whiteINS = false;
        boolean blackINS = false;
        int nw = Chess.longToStrings(pieces('N')).size();
        int bw = Chess.longToStrings(pieces('B')).size();
        int rw = Chess.longToStrings(pieces('R')).size();
        int qw = Chess.longToStrings(pieces('Q')).size();
        int pw = Chess.longToStrings(pieces('P')).size();
        int nb = Chess.longToStrings(pieces('n')).size();
        int bb = Chess.longToStrings(pieces('b')).size();
        int rb = Chess.longToStrings(pieces('r')).size();
        int qb = Chess.longToStrings(pieces('q')).size();
        int pb = Chess.longToStrings(pieces('p')).size();
        if (rw + qw + pw == 0)
            whiteINS = true;
        if (rb + qb + pb == 0)
            blackINS = true;
        if (whiteINS && blackINS) {
            if ((nw + bw <= 1) && (nb + bb <= 1)) {
                gameOver = true;
                result = "draw by insufficient material!";
                legalMoves.clear();
                return;
            }
            if ((nw == 2) && bw == 0 && (nb + bb == 0)) {
                gameOver = true;
                result = "draw by insufficient material!";
                legalMoves.clear();
                return;
            }
            if ((nb == 2) && bb == 0 && (nw + bw == 0)) {
                gameOver = true;
                result = "draw by insufficient material!";
                legalMoves.clear();
            }
        }
    }

    public void checkForCastleMove(String m) {
        if (castleRights[0] && m.equals("e1g1")) {
            andPiece('R', 9223372036854775807L); //deletes rook from origin square
            orPiece('R', 2305843009213693952L); //puts rook on destination square
        }
        if (castleRights[1] && m.equals("e1c1")) {
            andPiece('R', -72057594037927937L); //deletes rook from origin square
            orPiece('R', boardBuilder("d1")); //puts rook on destination square
        }
        if (castleRights[2] && m.equals("e8g8")) {
            andPiece('R', ~boardBuilder("h8")); //deletes rook from origin square
            orPiece('R', boardBuilder("f8")); //puts rook on destination square
        }
        if (castleRights[3] && m.equals("e8c8")) {
            andPiece('R', ~boardBuilder("a8")); //deletes rook from origin square
            orPiece('R', boardBuilder("d8")); //puts rook on destination square
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
                andPiece('p', ~boardBuilder(temp));
            }
            if (from == 'p') {
                String temp = "" + move[1].charAt(0) + '4';
                andPiece('P', ~boardBuilder(temp));
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
            andPiece('P', ~boardBuilder(move[1])); //deletes pawn from promotion square
            orPiece('Q', boardBuilder(move[1])); //puts queen on promotion square
        }
        if (from == 'p' && move[1].charAt(1) == '1') {
            andPiece('p', ~boardBuilder(move[1])); //deletes pawn from promotion square
            orPiece('q', boardBuilder(move[1])); //puts queen on promotion square
        }
    }

    public void setHalfMoveClock(char to, char from) {
        if (to != ' ')
            halfMoveClock = 0; //reset clock due to capture
        else if ((from == 'p') || (from == 'P'))
            halfMoveClock = 0; //increment clock due to non-capture/non-pawn-push
        else
            halfMoveClock++; //reset clock due to pawn push
    }


    //Begin Board Modifier Methods
    public void reset() {
        fen = fenList.get(0);
        setFromFEN(fen);
    }

    public void clear() {
        setFromFEN("8/8/8/8/8/8/8/8 w - - 0 1");
    }

    public void putPiece(char piece, String square) {
        char to = pieceAt(square);
        if (to != ' ')
            andPiece(to, ~boardBuilder(square)); //if piece at destination square, removes it
        if (piece != ' ')
            orPiece(piece, boardBuilder(square)); //puts moving piece on destination square
        fen = fen();
    }


    //Begin Occupied Methods
    public Long whiteOccupied() {
        return pieces('P') | pieces('R') | pieces('N') | pieces('B') | pieces('Q') | pieces('K');
    }

    public Long blackOccupied() {
        return pieces('p') | pieces('r') | pieces('n') | pieces('b') | pieces('q') | pieces('k');
    }

    public long occupied() {
        return whiteOccupied() | blackOccupied();
    }


    // Begin Knight Methods
    public List<Short> legalKnightMoves(char c) {
        List<Short> res = new ArrayList<>();
        long notSameColorOccupied = Character.isUpperCase(c) ? ~whiteOccupied() : ~blackOccupied();
        for (String str : longToStrings(
                nne(pieces(c)) & notSameColorOccupied
        )) {
            res.add(Chess.encodeMove(ssw(str) + str));
        }
        for (String str : longToStrings(
                nee(pieces(c)) & notSameColorOccupied)) {
            res.add(Chess.encodeMove(sww(str) + str));
        }
        for (String str : longToStrings(
                nnw(pieces(c)) & notSameColorOccupied)) {
            res.add(Chess.encodeMove(sse(str) + str));
        }
        for (String str : longToStrings(
                nww(pieces(c)) & notSameColorOccupied)) {
            res.add(Chess.encodeMove(see(str) + str));
        }
        for (String str : longToStrings(
                sse(pieces(c)) & notSameColorOccupied)) {
            res.add(Chess.encodeMove(nnw(str) + str));
        }
        for (String str : longToStrings(
                see(pieces(c)) & notSameColorOccupied)) {
            res.add(Chess.encodeMove(nww(str) + str));
        }
        for (String str : longToStrings(
                ssw(pieces(c)) & notSameColorOccupied)) {
            res.add(Chess.encodeMove(nne(str) + str));
        }
        for (String str : longToStrings(
                sww(pieces(c)) & notSameColorOccupied)) {
            res.add(Chess.encodeMove(nee(str) + str));
        }
        return res;
    }


    //Begin King Methods
    public List<Short> legalKingMoves(char c) {
        List<Short> res = new ArrayList<>();
        long king = pieces(c);
        if (king == 0L)
            return res;
        String kong = longToStrings(king).get(0);
        king |= n(king);
        king |= s(king);
        king |= e(king);
        king |= w(king);
        king &= isWhite(c) ? ~whiteOccupied() : ~blackOccupied();
        for (String str : longToStrings(king)) {
            res.add(Chess.encodeMove(kong + str));
        }
        res.addAll(legalKingCastleMoves(c));
        return res;
    }

    public List<Short> legalKingCastleMoves(char c) {
        List<Short> res = new ArrayList<>();
        long moves = 0L;
        long empty = ~occupied();
        if (isWhite(c)) {
            if (castleRights[0]) {
                moves |= boardBuilder("g1") & e(empty);
            }
            if (castleRights[1]) {
                moves |= boardBuilder("c1") & w(empty) & e(empty);
            }
        }
        if (isBlack(c)) {
            if (castleRights[2]) {
                moves |= boardBuilder("g8") & e(empty);
            }
            if (castleRights[3]) {
                moves |= boardBuilder("c8") & w(empty) & e(empty);
            }
        }
        for (String str : longToStrings(moves)) {
            res.add(Chess.encodeMove((str.charAt(1) == '1' ? "e1" : "e8") + str));
        }
        return res;
    }

    public boolean kingCanBeCaptured() {
        char c = turn.equals("white") ? 'k' : 'K';
        String king = longToStrings(pieces(c)).get(0);
        Set<Short> temp = updatePsuedoLegalMoves();
        for (short sh : temp) {
            String str = decodeMove(sh);
            if (str.substring(2).equals(king)) {
                return true;
            }
        }
        return false;
    }


    //Begin Pawn Methods
    public List<Short> legalPawnMoves(char c) {
        List<Short> res = new ArrayList<>();
        for (String str : longToStrings(
                pawnsThatCanDoublePush(c))) {
            res.add(
                    Chess.encodeMove(str + (isWhite(c) ? n(n(str)) : s(s(str))))
            );
        }
        for (String str : longToStrings(
                pawnsThatCanPush(c))) {
            res.add(Chess.encodeMove(str + (isWhite(c) ? n(str) : s(str))));
        }
        for (String str : longToStrings(
                pawnsThatCanCaptureEast(c))) {
            res.add(Chess.encodeMove(str + (isWhite(c) ? n(e(str)) : s(e(str)))));
        }
        for (String str : longToStrings(
                pawnsThatCanCaptureWest(c))) {
            res.add(Chess.encodeMove(str + (isWhite(c) ? n(w(str)) : s(w(str)))));
        }
        return res;
    }

    public long pawnsThatCanDoublePush(char c) {
        long empty = ~occupied();
        if (isWhite(c)) {
            return pieces(c) & s(empty) & s(s(empty)) & ROW2;
        }
        if (isBlack(c)) {
            return pieces(c) & n(empty) & n(n(empty)) & ROW7;
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
        long ep = !enPassant.equals("") ? boardBuilder(enPassant) : 0;
        if (isWhite(c)) {
            return pieces(c) & se(blackOccupied() | ep);
        }
        if (isBlack(c)) {
            return pieces(c) & ne(whiteOccupied() | ep);
        }
        return 0L;
    }

    public long pawnsThatCanCaptureEast(char c) {
        long ep = !enPassant.equals("") ? boardBuilder(enPassant) : 0;
        if (isWhite(c)) {
            return pieces(c) & sw(blackOccupied() | ep);
        }
        if (isBlack(c)) {
            return pieces(c) & nw(whiteOccupied() | ep);
        }
        return 0L;
    }


    //Begin Bishop Methods
    public List<Short> legalBishopMoves(char c) {
        List<Short> res = new ArrayList<>();
        for (String str : longToStrings(pieces(c))) {
            res.addAll(bishopSouthWestMoves(c, str));
            res.addAll(bishopSouthEastMoves(c, str));
            res.addAll(bishopNorthWestMoves(c, str));
            res.addAll(bishopNorthEastMoves(c, str));
        }
        return res;
    }

    public List<Short> bishopNorthEastMoves(char c, String str) {
        List<Short> res = new ArrayList<>();
        String temp = str;
        while ((!ne(temp).equals("-1")) && (pieceAt(ne(temp)) == ' ')) {
            temp = ne(temp);
            res.add(encodeMove(str + temp));
        }
        if ((!ne(temp).equals("-1")) &&
                (isWhite(c) ? isBlack(pieceAt(ne(temp))) : isWhite(pieceAt(ne(temp))))
        ) {
            temp = ne(temp);
            res.add(encodeMove(str + temp));
        }
        return res;
    }

    public List<Short> bishopNorthWestMoves(char c, String str) {
        List<Short> res = new ArrayList<>();
        String temp = str;
        while ((!nw(temp).equals("-1")) && (pieceAt(nw(temp)) == ' ')) {
            temp = nw(temp);
            res.add(encodeMove(str + temp));
        }
        if ((!nw(temp).equals("-1")) &&
                (isWhite(c) ? isBlack(pieceAt(nw(temp))) : isWhite(pieceAt(nw(temp))))
        ) {
            temp = nw(temp);
            res.add(encodeMove(str + temp));
        }
        return res;
    }

    public List<Short> bishopSouthEastMoves(char c, String str) {
        List<Short> res = new ArrayList<>();
        String temp = str;
        while ((!se(temp).equals("-1")) && (pieceAt(se(temp)) == ' ')) {
            temp = se(temp);
            res.add(encodeMove(str + temp));
        }
        if ((!se(temp).equals("-1")) &&
                (isWhite(c) ? isBlack(pieceAt(se(temp))) : isWhite(pieceAt(se(temp))))
        ) {
            temp = se(temp);
            res.add(encodeMove(str + temp));
        }
        return res;
    }

    public List<Short> bishopSouthWestMoves(char c, String str) {
        List<Short> res = new ArrayList<>();
        String temp = str;
        while ((!sw(temp).equals("-1")) && (pieceAt(sw(temp)) == ' ')) {
            temp = sw(temp);
            res.add(encodeMove(str + temp));
        }
        if ((!sw(temp).equals("-1")) &&
                (isWhite(c) ? isBlack(pieceAt(sw(temp))) : isWhite(pieceAt(sw(temp))))
        ) {
            temp = sw(temp);
            res.add(encodeMove(str + temp));
        }
        return res;
    }


    //Begin Rook Methods
    public List<Short> legalRookMoves(char c) {
        List<Short> res = new ArrayList<>();
        for (String str : longToStrings(pieces(c))) {
            res.addAll(rookNorthMoves(c, str));
            res.addAll(rookSouthMoves(c, str));
            res.addAll(rookEastMoves(c, str));
            res.addAll(rookWestMoves(c, str));
        }
        return res;
    }

    public List<Short> rookNorthMoves(char c, String str) {
        List<Short> res = new ArrayList<>();
        String temp = str;
        while ((!n(temp).equals("-1")) && (pieceAt(n(temp)) == ' ')) {
            temp = n(temp);
            res.add(encodeMove(str + temp));
        }
        if ((!n(temp).equals("-1")) &&
                (isWhite(c) ? isBlack(pieceAt(n(temp))) : isWhite(pieceAt(n(temp))))
        ) {
            temp = n(temp);
            res.add(encodeMove(str + temp));
        }
        return res;
    }

    public List<Short> rookSouthMoves(char c, String str) {
        List<Short> res = new ArrayList<>();
        String temp = str;
        while ((!s(temp).equals("-1")) && (pieceAt(s(temp)) == ' ')) {
            temp = s(temp);
            res.add(encodeMove(str + temp));
        }
        if ((!s(temp).equals("-1")) &&
                (isWhite(c) ? isBlack(pieceAt(s(temp))) : isWhite(pieceAt(s(temp))))
        ) {
            temp = s(temp);
            res.add(encodeMove(str + temp));
        }
        return res;
    }

    public List<Short> rookEastMoves(char c, String str) {
        List<Short> res = new ArrayList<>();
        String temp = str;
        while ((!e(temp).equals("-1")) && (pieceAt(e(temp)) == ' ')) {
            temp = e(temp);
            res.add(encodeMove(str + temp));
        }
        if ((!e(temp).equals("-1")) &&
                (isWhite(c) ? isBlack(pieceAt(e(temp))) : isWhite(pieceAt(e(temp))))
        ) {
            temp = e(temp);
            res.add(encodeMove(str + temp));
        }
        return res;
    }

    public List<Short> rookWestMoves(char c, String str) {
        List<Short> res = new ArrayList<>();
        String temp = str;
        while ((!w(temp).equals("-1")) && (pieceAt(w(temp)) == ' ')) {
            temp = w(temp);
            res.add(encodeMove(str + temp));
        }
        if ((!w(temp).equals("-1")) &&
                (isWhite(c) ? isBlack(pieceAt(w(temp))) : isWhite(pieceAt(w(temp))))
        ) {
            temp = w(temp);
            res.add(encodeMove(str + temp));
        }
        return res;
    }


    //Begin Queen Methods
    public List<Short> legalQueenMoves(char c) {
        List<Short> res = new ArrayList<>();
        res.addAll(legalRookMoves(c));
        res.addAll(legalBishopMoves(c));
        return res;
    }


    //Begin Boolean Methods
    public boolean inCheck() {
        char c = turn.equals("white") ? 'K' : 'k';
        long l = pieces(c);
        if (l == 0L)
            return false;
        String king = longToStrings(l).get(0);
        turn = turn.equals("white") ? "black" : "white";
        Set<Short> temp = updatePsuedoLegalMoves();
        turn = turn.equals("white") ? "black" : "white";
        for (short sh : temp) {
            String str = decodeMove(sh);
            if (str.substring(2).equals(king)) {
                return true;
            }
        }
        return false;
    }

    public boolean isWhite(char c) {
        return Character.isUpperCase(c);
    }

    public boolean isBlack(char c) {
        return Character.isLowerCase(c);
    }

    public boolean isCapture(String s) {
        return (pieceAt(s.substring(2)) != ' ')
                ||
                (s.substring(2).equals(enPassant)
                        &&
                        (Character.toLowerCase(pieceAt(s.substring(0, 2))) == 'p')
                );
    }


    //Begin Rolling Methods
    public String rollback() {
        if (fenList.size() == 1)
            return "";
        String temp = fenList.remove(fenList.size() - 1);
        extraFenList.add(0, temp);
        short sh = allMovesMade.remove(allMovesMade.size() - 1);
        extraAllMovesMade.add(0, sh);
        List<String> tempFENList = new ArrayList<>(fenList);
        List<Short> tempAllMovesMade = new ArrayList<>(allMovesMade);
        fen = fenList.get(fenList.size() - 1);
        setFromFEN(fen);
        fenList = tempFENList;
        allMovesMade = tempAllMovesMade;
        return moveType(decodeMove(sh));
    }

    public String rollForward() {
        if (extraFenList.size() == 0) {
            return "";
        }
        String temp = extraFenList.remove(0);
        fenList.add(temp);
        Short sh = extraAllMovesMade.remove(0);
        String res = moveType(decodeMove(sh));
        allMovesMade.add(sh);
        List<String> tempFENList = new ArrayList<>(fenList);
        List<Short> tempAllMovesMade = new ArrayList<>(allMovesMade);
        fen = fenList.get(fenList.size() - 1);
        setFromFEN(fen);
        fenList = tempFENList;
        allMovesMade = tempAllMovesMade;
        return res;
    }

    public String rollback(int n) {
        for (int i = 0; i < n; i++)
            rollback();
        return "";
    }

    public String rollForward(int n) {
        for (int i = 0; i < n; i++)
            rollForward();
        return "";
    }


    //Begin Print Methods
    public static void print(Object... args) {

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

    public static void println(Object... args) {
        System.out.print("\r\n");
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


    //Begin Directional String Methods
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


    //Begin Directional Long Methods
    public static long n(long a) {
        return a >>> 8 & ~ROW1;
    }

    public static long s(long a) {
        return a << 8 & ~ROW8;
    }

    public static long e(long a) {
        return (a << 1) & ~AFILE;
    }

    public static long w(long a) {
        return (a >>> 1) & ~HFILE;
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


    //Begin boardBuilder Methods
    public static long boardBuilder(String a) {
        return boardBuilder(stringToByte(a));
    }

    public static long boardBuilder(char a, int b) {
        return boardBuilder("" + a + b);
    }

    public static long boardBuilder(byte move) {
        return 1L << (move);
    }


    public static List<String> longToStrings(Long l) {
        List<String> res = new ArrayList<>();
        for (byte i = 0; i < 64; i++) {
            if (((l >>> i) & 1L) == 1L)
                res.add(byteToString(i));
        }
        return res;
    }


    //Begin Decoding Methods
    public static String decodeMove(short move) {
        String destination = Chess.byteToString((byte) (63 & (move >>> 10)));
        String origin = Chess.byteToString((byte) (63 & (move >>> 4)));
        return origin + destination;
    }

    public static byte[] decodeMoveYEYE(short move) {
        byte[] res = new byte[4];
        res[0] = (byte) (63 & (move >>> 10));
        res[1] = (byte) (63 & (move >>> 4));
        res[2] = (byte) (63 & (move >>> 2));
        res[3] = (byte) (63 & move);
        return res;
    }

    public static String byteToString(byte b) {
        return "" + ((char) (((b) & 7) + 97)) + (8 - (b >>> 3));
    }

    public static int rank(byte a) {
        return 8 - ((63 - a) >>> 3);
    }

    public static char file(byte a) {
        return (char) (((63 - a) & 7) + 97);
    }


    //Begin Encoding Methods
    public static short encodeMove(byte destination, byte origin, byte promotion, byte flag) {
        return (short) ((destination << 10) | (origin << 4) | (promotion << 2) | flag);
    }

    public static short encodeMove(String move) {
        byte origin = Chess.stringToByte(move.substring(0, 2));
        byte destination = Chess.stringToByte(move.substring(2));
        byte promotion = 0;
        byte flag = 0;
        return encodeMove(destination, origin, promotion, flag);
    }

    public static byte stringToByte(String str) {
        return (byte) (351 + str.charAt(0) - 8 * str.charAt(1));
    }


    //Begin Visualisation Methods
    public void drawBoard() {
        print("\r\n8 | ", "");
        for (int k = 0; k < 64; k++) {
            char temp = ' ';
            for (char s : new char[]{'P', 'R', 'N', 'B', 'Q', 'K', 'p', 'r', 'n', 'b', 'q', 'k'}) {
                if ((pieces(s) >>> k & 1) == 1L) {
                    temp = s;
                }
            }
            print(temp != ' ' ? temp : "*", " ");
            if ((k + 1) % 8 == 0 && (k != 63)) {
                print("\r\n" + (7 - (k >>> 3)) + " | ", "");
            }
        }
        print("\r\n  -----------------");
        print("    a b c d e f g h");
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

}
