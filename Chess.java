import java.util.*;

public class Chess implements Comparable<Chess> {


    //Begin Static ROWS and FILES
    static final long AFILE = 72340172838076673L;
    static final long HFILE = -9187201950435737472L;
    static final long ROW1 = -72057594037927936L;
    static final long ROW2 = 71776119061217280L;
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
    boolean wKC = true;
    boolean wQC = true;
    boolean bKC = true;
    boolean bQC = true;
    boolean turn; //keeps track current turn, "white" or "black"
    byte enPassant = -1; //keeps track of current en passant square.
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
        if (f.equals(""))
            setFromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        else
            setFromFEN(f);
    }
/*
    private Chess(String f, String shadow) {
        if (!shadow.equals("filler text"))
            simpleSetFromFEN(f);
    }
*/
    public Chess(Chess temp) {
        extraAllMovesMade = new ArrayList<>(temp.extraAllMovesMade);
        psuedoLegalMoves = new HashSet<>(temp.psuedoLegalMoves);
        extraFenList = new ArrayList<>(temp.extraFenList);
        allMovesMade = new ArrayList<>(temp.allMovesMade);
        legalMoves = new HashSet<>(temp.legalMoves);
        wKC = temp.wKC;
        wQC = temp.wQC;
        bKC = temp.bKC;
        bQC = temp.bQC;
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
    public Chess nextBoard(short move) {
        Chess temp = new Chess(this);
        temp.makeMove(move);
        return temp;
    }

    public int compareTo(Chess anotherChess) {
        int runningTotal = 0;
        if (fenList.size() != anotherChess.fenList.size())
            return fenList.size() - anotherChess.fenList.size();
        for (int i = 0; i < fenList.size(); i++)
            runningTotal += fenList.get(i).compareTo(anotherChess.fenList.get(i));
        return runningTotal;
    }

    public boolean equals(Chess anotherChess) {
        return compareTo(anotherChess) == 0;
    }

    //Begin Move Methods
    public void makeMove(short m) {
        byte[] move = moveToByteArray(m);
        char from = pieceAt(move[0]); //gets char for origin piece. e.g. 'P'
        char to = pieceAt(move[1]); //gets char for captured piece.
        // If no piece captured or capture is an en passant capture, will be ' '.
        enPassant = -1;
        if (from == 'P' && 47 < move[0] && move[0] < 56 && 31 < move[1] && move[1] < 40)
            enPassant = (byte) (move[1] + 8);
        if (from == 'p' && 7 < move[0] && move[0] < 16 && 23 < move[1] && move[1] < 32)
            enPassant = (byte) (move[1] - 8);
        simpleMove(m);
        updateCastleRights(from, move[0]);
        setHalfMoveClock(to, from); //sets HalfMoveClock accordingly
        if (!turn)
            fullMoveNumber++; //increment fullMoveNumber
        switch (move[3]) {
            case 1 -> castleMove(move[1]); //move rook to proper position if move is castling
            case 2 -> enPassantMove(move[1]); //capture pawn for en passant move
            case 3 -> promotionMove(move[1], move[2]); //promotes pawn if on first of eighth rank.
        }
        fen = fen();
        allMovesMade.add(m); //add move made to allMovesMade
        psuedoLegalMoves = getPsuedoLegalMoves();
        legalMoves = getLegalMoves(); //update list of legal Moves
        fenList.add(fen);
        extraAllMovesMade.clear();
        extraFenList.clear();
        checkForGameOver();
    }

    public void undo() {
        fenList.remove(fenList.size() - 1);
        allMovesMade.remove(allMovesMade.size() - 1);
        simpleSetFromFEN(fenList.get(fenList.size() - 1));
        psuedoLegalMoves = getPsuedoLegalMoves();
        legalMoves = getLegalMoves(); //update list of legal Moves
        gameOver = false;
        result = "";
        checkForGameOver();
    }

    public static char pieceAt(String fenString, String location) {
        int x = location.charAt(0) - 97;
        int y = 8 - Character.getNumericValue(location.charAt(1));
        int stringIndex = 0;
        int rowIndex = 0;
        String row = (y != 7) ? fenString.split("/")[y] : fenString.split("/")[7].split(" ")[0];
        while (rowIndex < x && stringIndex < row.length() - 1) {
            if (Character.isDigit(row.charAt(stringIndex)))
                rowIndex += Character.getNumericValue(row.charAt(stringIndex));
            else
                rowIndex++;
            stringIndex++;
        }
        if (rowIndex == x && !Character.isDigit(row.charAt(stringIndex)))
            return row.charAt(stringIndex);
        return ' ';
    }

    public void simpleMove(short m) {
        turn = !turn; //switch turn
        byte[] move = moveToByteArray(m);
        char from = pieceAt(move[0]); //gets char for origin piece. e.g. 'P'
        char to = pieceAt(move[1]); //gets char for captured piece.
        if (to != ' ')
            andPiece(to, ~boardBuilder(move[1])); //if piece at destination square, removes it
        andPiece(from, ~boardBuilder(move[0])); //deletes moving piece from origin square
        orPiece(from, boardBuilder(move[1])); //puts moving piece on destination square
    }

    public void updateCastleRights(char piece, byte move) {
        if (piece == 'k') {
            bKC = bQC = false;
            return;
        }
        if (piece == 'K') {
            wKC = wQC = false;
            return;
        }
        if (piece == 'r' || piece == 'R') {
            if (bKC && move == 7) {
                bKC = false;
                return;
            }
            if (bQC && move == 0) {
                bQC = false;
                return;
            }
            if (wKC && move == 63) {
                wKC = false;
                return;
            }
            if (wQC && move == 56) {
                wQC = false;
            }
        }
    }

    public Short lastMove() {
        return allMovesMade.size() == 0 ? 0 : allMovesMade.get(allMovesMade.size() - 1);
    }

    public String moveType(short s) {
        byte[] move = moveToByteArray(s);
        boolean capture = isCapture(s);
        boolean castle = move[3] == 1;
        boolean promotion = move[3] == 3;
        String res = "";
        Chess shadow = nextBoard(s);
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

    public char pieceAt(byte str) {
        if (((wP >>> str) & 1L) == 1L)
            return 'P';
        if (((wR >>> str) & 1L) == 1L)
            return 'R';
        if (((wN >>> str) & 1L) == 1L)
            return 'N';
        if (((wB >>> str) & 1L) == 1L)
            return 'B';
        if (((wQ >>> str) & 1L) == 1L)
            return 'Q';
        if (((wK >>> str) & 1L) == 1L)
            return 'K';
        if (((bP >>> str) & 1L) == 1L)
            return 'p';
        if (((bR >>> str) & 1L) == 1L)
            return 'r';
        if (((bN >>> str) & 1L) == 1L)
            return 'n';
        if (((bB >>> str) & 1L) == 1L)
            return 'b';
        if (((bQ >>> str) & 1L) == 1L)
            return 'q';
        if (((bK >>> str) & 1L) == 1L)
            return 'k';
        return ' ';
    }
/*
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
    */


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
        res.append(turn ? " w " : " b ");
        int count = 0;
        if (wKC) {
            res.append("K");
            count++;
        }
        if (wQC) {
            res.append("Q");
            count++;
        }
        if (bKC) {
            res.append("k");
            count++;
        }
        if (bQC) {
            res.append("q");
            count++;
        }
        if (count == 0) {
            res.append("-");
        }
        if (enPassant == -1) {
            res.append(" -");
        } else {
            res.append(" ").append(byteToString(enPassant));
        }
        res.append(" ").append(halfMoveClock).append(" ").append(fullMoveNumber);
        return res.toString();
    }

    public void setFromFEN(String f) {
        simpleSetFromFEN(f);
        fenList.clear();
        fenList.add(this.fen);
        psuedoLegalMoves = getPsuedoLegalMoves();
        legalMoves = getLegalMoves();
        gameOver = false;
        result = "";
        checkForGameOver();
    }

    public void simpleSetFromFEN(String f) {
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
        turn = last[1].equals("w");
        List<Character> temp = new ArrayList<>();
        for (char c : last[2].toCharArray())
            temp.add(c);
        wKC = temp.contains('K');
        wQC = temp.contains('Q');
        bKC = temp.contains('k');
        bQC = temp.contains('q');
        if (!last[3].equals("-"))
            enPassant = stringToByte(last[3]);
        else
            enPassant = -1;
        halfMoveClock = Integer.parseInt(last[4]);
        fullMoveNumber = Integer.parseInt(last[5]);
        extraAllMovesMade.clear();
        extraFenList.clear();
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
    public Set<Short> getPsuedoLegalMoves() {
        Set<Short> res = new HashSet<>();
        res.addAll(legalPawnMoves(turn ? 'P' : 'p'));
        res.addAll(legalRookMoves(turn ? 'R' : 'r'));
        res.addAll(legalKnightMoves(turn ? 'N' : 'n'));
        res.addAll(legalBishopMoves(turn ? 'B' : 'b'));
        res.addAll(legalQueenMoves(turn ? 'Q' : 'q'));
        res.addAll(legalKingMoves(turn ? 'K' : 'k'));
        return res;
    }

    public Set<Short> getLegalMoves() {
        Set<Short> res = new HashSet<>();
        for (short str : psuedoLegalMoves) {
            simpleMove(str);
            if (!kingCanBeCaptured())
                res.add(str);
            simpleSetFromFEN(fen);
        }
        boolean f1 = false, d1 = false, f8 = false, d8 = false;
        turn = !turn;
        Set<Short> temp = getPsuedoLegalMoves();
        turn = !turn;
        for (Short sh : temp) {
            int destination = moveToByteArray(sh)[1];
            if (destination == 61)
                f1 = true;
            if (destination == 59)
                d1 = true;
            if (destination == 5)
                f8 = true;
            if (destination == 3)
                d8 = true;
        }
        if (wKC &&
                (f1 || pieceAt((byte) 54) == 'p' || pieceAt((byte) 52) == 'p')) {
            res.remove(encodeMove("e1g1q"));
            res.remove(encodeMove("e1g1n"));
            res.remove(encodeMove("e1g1r"));
            res.remove(encodeMove("e1g1b"));
        }
        if (wQC &&
                (d1 || pieceAt((byte) 52) == 'p' || pieceAt((byte) 50) == 'p')) {
            res.remove(encodeMove("e1c1q"));
            res.remove(encodeMove("e1c1n"));
            res.remove(encodeMove("e1c1r"));
            res.remove(encodeMove("e1c1b"));
        }
        if (bKC &&
                (f8 || pieceAt((byte) 14) == 'P' || pieceAt((byte) 12) == 'P')) {
            res.remove(encodeMove("e8g8q"));
            res.remove(encodeMove("e8g8n"));
            res.remove(encodeMove("e8g8r"));
            res.remove(encodeMove("e8g8b"));
        }
        if (bQC &&
                (d8 || pieceAt((byte) 12) == 'P' || pieceAt((byte) 10) == 'P')) {
            res.remove(encodeMove("e8c8q"));
            res.remove(encodeMove("e8c8n"));
            res.remove(encodeMove("e8c8r"));
            res.remove(encodeMove("e8c8b"));
        }
        return res;
    }


    //Begin MakeMove Helpers
    public void checkForGameOver() {
        if (legalMoves.size() == 0) {
            gameOver = true;
            if (inCheck()) {
                result = (turn) ? "black wins!" : "white wins!";
            } else {
                gameOver = true;
                result = "draw by stalemate!";
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
        int nw = Chess.longToBytes(pieces('N')).size();
        int bw = Chess.longToBytes(pieces('B')).size();
        int rw = Chess.longToBytes(pieces('R')).size();
        int qw = Chess.longToBytes(pieces('Q')).size();
        int pw = Chess.longToBytes(pieces('P')).size();
        int nb = Chess.longToBytes(pieces('n')).size();
        int bb = Chess.longToBytes(pieces('b')).size();
        int rb = Chess.longToBytes(pieces('r')).size();
        int qb = Chess.longToBytes(pieces('q')).size();
        int pb = Chess.longToBytes(pieces('p')).size();
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

    public void castleMove(byte destination) {
        if (destination == stringToByte("g1")) {
            andPiece('R', 9223372036854775807L); //deletes rook from origin square
            orPiece('R', 2305843009213693952L); //puts rook on destination square
            wKC = false;
        }
        if (destination == stringToByte("c1")) {
            andPiece('R', -72057594037927937L); //deletes rook from origin square
            orPiece('R', boardBuilder("d1")); //puts rook on destination square
            wQC = false;
        }
        if (destination == stringToByte("g8")) {
            andPiece('r', ~boardBuilder("h8")); //deletes rook from origin square
            orPiece('r', boardBuilder("f8")); //puts rook on destination square
            bKC = false;
        }
        if (destination == stringToByte("c8")) {
            andPiece('r', ~boardBuilder("a8")); //deletes rook from origin square
            orPiece('r', boardBuilder("d8")); //puts rook on destination square
            bQC = false;
        }
    }

    public void enPassantMove(byte destination) {
        if (destination < 32)
            andPiece('p', ~s(boardBuilder(destination)));
        else
            andPiece('P', ~n(boardBuilder(destination)));
    }

    public void promotionMove(byte destination, byte promote) {
        if (destination < 32) {
            andPiece('P', ~boardBuilder(destination));
            switch (promote) {
                case 0 -> orPiece('Q', boardBuilder(destination));
                case 1 -> orPiece('N', boardBuilder(destination));
                case 2 -> orPiece('R', boardBuilder(destination));
                case 3 -> orPiece('B', boardBuilder(destination));
            }
        } else {
            andPiece('p', ~boardBuilder(destination));
            switch (promote) {
                case 0 -> orPiece('q', boardBuilder(destination));
                case 1 -> orPiece('n', boardBuilder(destination));
                case 2 -> orPiece('r', boardBuilder(destination));
                case 3 -> orPiece('b', boardBuilder(destination));
            }
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
        for (byte str : longToBytes(nne(pieces(c)) & notSameColorOccupied))
            res.add(Chess.encodeMove(ssw(str), str, (byte) 0, (byte) 0));
        for (byte str : longToBytes(nee(pieces(c)) & notSameColorOccupied))
            res.add(Chess.encodeMove(sww(str), str, (byte) 0, (byte) 0));
        for (byte str : longToBytes(nnw(pieces(c)) & notSameColorOccupied))
            res.add(Chess.encodeMove(sse(str), str, (byte) 0, (byte) 0));
        for (byte str : longToBytes(nww(pieces(c)) & notSameColorOccupied))
            res.add(Chess.encodeMove(see(str), str, (byte) 0, (byte) 0));
        for (byte str : longToBytes(sse(pieces(c)) & notSameColorOccupied))
            res.add(Chess.encodeMove(nnw(str), str, (byte) 0, (byte) 0));
        for (byte str : longToBytes(see(pieces(c)) & notSameColorOccupied))
            res.add(Chess.encodeMove(nww(str), str, (byte) 0, (byte) 0));
        for (byte str : longToBytes(ssw(pieces(c)) & notSameColorOccupied))
            res.add(Chess.encodeMove(nne(str), str, (byte) 0, (byte) 0));
        for (byte str : longToBytes(sww(pieces(c)) & notSameColorOccupied))
            res.add(Chess.encodeMove(nee(str), str, (byte) 0, (byte) 0));
        return res;
    }


    //Begin King Methods
    public List<Short> legalKingMoves(char c) {
        List<Short> res = new ArrayList<>();
        long king = pieces(c);
        if (king == 0L)
            return res;
        byte kong = longToBytes(king).get(0);
        king |= n(king);
        king |= s(king);
        king |= e(king);
        king |= w(king);
        king &= isWhite(c) ? ~whiteOccupied() : ~blackOccupied();
        for (byte b : longToBytes(king))
            res.add(Chess.encodeMove(kong, b, (byte) 0, (byte) 0));
        res.addAll(legalKingCastleMoves(c));
        return res;
    }

    public List<Short> legalKingCastleMoves(char c) {
        List<Short> res = new ArrayList<>();
        long moves = 0L;
        long empty = ~occupied();
        boolean white = isWhite(c);
        if (white) {
            if (wKC)
                moves |= boardBuilder("g1") & e(empty) & empty;
            if (wQC)
                moves |= boardBuilder("c1") & w(empty) & e(empty) & empty;
        } else {
            if (bKC)
                moves |= boardBuilder("g8") & e(empty) & empty;
            if (bQC)
                moves |= boardBuilder("c8") & w(empty) & e(empty) & empty;
        }
        for (byte b : longToBytes(moves))
            res.add(
                    Chess.encodeMove((byte) (white ? 60 : 4), b, (byte) 0, (byte) 1));
        return res;
    }

    public boolean kingCanBeCaptured() {
        char c = turn ? 'k' : 'K';
        byte king = longToBytes(pieces(c)).get(0);
        Set<Short> temp = getPsuedoLegalMoves();
        for (short sh : temp)
            if (moveToByteArray(sh)[1] == king)
                return true;
        return false;
    }


    //Begin Pawn Methods
    public List<Short> legalPawnMoves(char c) {
        List<Short> res = new ArrayList<>();
        boolean white = isWhite(c);
        for (byte str : longToBytes(pawnsThatCanDoublePush(c)))
            res.add(Chess.encodeMove(str, white ? n(n(str)) : s(s(str)), (byte) 0, (byte) 0));
        for (byte str : longToBytes(pawnsThatCanPush(c)))
            if ((white && n(str) < 8) || (!white && 55 < s(str)))
                for (byte i = 0; i < 4; i++)
                    res.add(Chess.encodeMove(str, white ? n(str) : s(str), i, (byte) 3));
            else
                res.add(Chess.encodeMove(str, white ? n(str) : s(str), (byte) 0, (byte) 0));
        for (byte str : longToBytes(pawnsThatCanCaptureEast(c)))
            if ((white && n(str) < 8) || (!white && 55 < s(str)))
                for (byte i = 0; i < 4; i++)
                    res.add(Chess.encodeMove(str, white ? n(e(str)) : s(e(str)), i, (byte) 3));
            else
                res.add(Chess.encodeMove(str, white ? n(e(str)) : s(e(str)), (byte) 0, (byte) 0));
        for (byte str : longToBytes(pawnsThatCanCaptureWest(c)))
            if ((white && n(str) < 8) || (!white && 55 < s(str)))
                for (byte i = 0; i < 4; i++)
                    res.add(Chess.encodeMove(str, white ? n(w(str)) : s(w(str)), i, (byte) 3));
            else
                res.add(Chess.encodeMove(str, white ? n(w(str)) : s(w(str)), (byte) 0, (byte) 0));
        for (byte str : longToBytes(pawnsThatCanCaptureEastEnPassant(c)))
            res.add(Chess.encodeMove(str, white ? n(e(str)) : s(e(str)), (byte) 0, (byte) 2));
        for (byte str : longToBytes(pawnsThatCanCaptureWestEnPassant(c)))
            res.add(Chess.encodeMove(str, white ? n(w(str)) : s(w(str)), (byte) 0, (byte) 2));
        return res;
    }

    public long pawnsThatCanDoublePush(char c) {
        long empty = ~occupied();
        if (isWhite(c))
            return pieces(c) & s(empty) & s(s(empty)) & ROW2;
        if (isBlack(c))
            return pieces(c) & n(empty) & n(n(empty)) & ROW7;
        return 0L;
    }

    public long pawnsThatCanPush(char c) {
        long empty = ~occupied();
        if (isWhite(c))
            return pieces(c) & s(empty);
        if (isBlack(c))
            return pieces(c) & n(empty);
        return 0L;
    }

    public long pawnsThatCanCaptureWest(char c) {
        if (isWhite(c))
            return pieces(c) & se(blackOccupied());
        if (isBlack(c))
            return pieces(c) & ne(whiteOccupied());
        return 0L;
    }

    public long pawnsThatCanCaptureWestEnPassant(char c) {
        if (enPassant == -1)
            return 0;
        long ep = boardBuilder(enPassant);
        if (isWhite(c))
            return pieces(c) & se(ep);
        if (isBlack(c))
            return pieces(c) & ne(ep);
        return 0L;
    }

    public long pawnsThatCanCaptureEastEnPassant(char c) {
        if (enPassant == -1)
            return 0;
        long ep = boardBuilder(enPassant);
        if (isWhite(c))
            return pieces(c) & sw(ep);
        if (isBlack(c))
            return pieces(c) & nw(ep);
        return 0L;
    }

    public long pawnsThatCanCaptureEast(char c) {
        if (isWhite(c))
            return pieces(c) & sw(blackOccupied());
        if (isBlack(c))
            return pieces(c) & nw(whiteOccupied());
        return 0L;
    }


    //Begin Bishop Methods
    public List<Short> legalBishopMoves(char c) {
        List<Short> res = new ArrayList<>();
        for (byte str : longToBytes(pieces(c))) {
            res.addAll(bishopSouthWestMoves(c, str));
            res.addAll(bishopSouthEastMoves(c, str));
            res.addAll(bishopNorthWestMoves(c, str));
            res.addAll(bishopNorthEastMoves(c, str));
        }
        return res;
    }

    public List<Short> bishopNorthEastMoves(char c, byte str) {
        List<Short> res = new ArrayList<>();
        byte temp = ne(str);
        for (; temp != -1 && (pieceAt(temp) == ' '); temp = ne(temp))
            res.add(encodeMove(str, temp, (byte) 0, (byte) 0));
        if (temp != -1 && (isWhite(c) ? isBlack(pieceAt(temp)) : isWhite(pieceAt(temp))))
            res.add(encodeMove(str, temp, (byte) 0, (byte) 0));
        return res;
    }

    public List<Short> bishopNorthWestMoves(char c, byte str) {
        List<Short> res = new ArrayList<>();
        byte temp = nw(str);
        for (; temp != -1 && (pieceAt(temp) == ' '); temp = nw(temp))
            res.add(encodeMove(str, temp, (byte) 0, (byte) 0));
        if (temp != -1 && (isWhite(c) ? isBlack(pieceAt(temp)) : isWhite(pieceAt(temp))))
            res.add(encodeMove(str, temp, (byte) 0, (byte) 0));
        return res;
    }

    public List<Short> bishopSouthEastMoves(char c, byte str) {
        List<Short> res = new ArrayList<>();
        byte temp = se(str);
        for (; temp != -1 && (pieceAt(temp) == ' '); temp = se(temp))
            res.add(encodeMove(str, temp, (byte) 0, (byte) 0));
        if (temp != -1 && (isWhite(c) ? isBlack(pieceAt(temp)) : isWhite(pieceAt(temp))))
            res.add(encodeMove(str, temp, (byte) 0, (byte) 0));
        return res;
    }

    public List<Short> bishopSouthWestMoves(char c, byte str) {
        List<Short> res = new ArrayList<>();
        byte temp = sw(str);
        for (; temp != -1 && (pieceAt(temp) == ' '); temp = sw(temp))
            res.add(encodeMove(str, temp, (byte) 0, (byte) 0));
        if (temp != -1 && (isWhite(c) ? isBlack(pieceAt(temp)) : isWhite(pieceAt(temp))))
            res.add(encodeMove(str, temp, (byte) 0, (byte) 0));
        return res;
    }


    //Begin Rook Methods
    public List<Short> legalRookMoves(char c) {
        List<Short> res = new ArrayList<>();
        for (byte str : longToBytes(pieces(c))) {
            res.addAll(rookNorthMoves(c, str));
            res.addAll(rookSouthMoves(c, str));
            res.addAll(rookEastMoves(c, str));
            res.addAll(rookWestMoves(c, str));
        }
        return res;
    }

    public List<Short> rookNorthMoves(char c, byte str) {
        List<Short> res = new ArrayList<>();
        byte temp = n(str);
        for (; temp != -1 && (pieceAt(temp) == ' '); temp = n(temp))
            res.add(encodeMove(str, temp, (byte) 0, (byte) 0));
        if (temp != -1 && (isWhite(c) ? isBlack(pieceAt(temp)) : isWhite(pieceAt(temp))))
            res.add(encodeMove(str, temp, (byte) 0, (byte) 0));
        return res;
    }

    public List<Short> rookSouthMoves(char c, byte str) {
        List<Short> res = new ArrayList<>();
        byte temp = s(str);
        for (; temp != -1 && (pieceAt(temp) == ' '); temp = s(temp))
            res.add(encodeMove(str, temp, (byte) 0, (byte) 0));
        if (temp != -1 && (isWhite(c) ? isBlack(pieceAt(temp)) : isWhite(pieceAt(temp))))
            res.add(encodeMove(str, temp, (byte) 0, (byte) 0));
        return res;
    }

    public List<Short> rookEastMoves(char c, byte str) {
        List<Short> res = new ArrayList<>();
        byte temp = e(str);
        for (; temp != -1 && (pieceAt(temp) == ' '); temp = e(temp))
            res.add(encodeMove(str, temp, (byte) 0, (byte) 0));
        if (temp != -1 && (isWhite(c) ? isBlack(pieceAt(temp)) : isWhite(pieceAt(temp))))
            res.add(encodeMove(str, temp, (byte) 0, (byte) 0));
        return res;
    }

    public List<Short> rookWestMoves(char c, byte str) {
        List<Short> res = new ArrayList<>();
        byte temp = w(str);
        for (; temp != -1 && (pieceAt(temp) == ' '); temp = w(temp))
            res.add(encodeMove(str, temp, (byte) 0, (byte) 0));
        if (temp != -1 && (isWhite(c) ? isBlack(pieceAt(temp)) : isWhite(pieceAt(temp))))
            res.add(encodeMove(str, temp, (byte) 0, (byte) 0));
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
        char c = turn ? 'K' : 'k';
        long l = pieces(c);
        if (l == 0L)
            return false;
        byte king = longToBytes(l).get(0);
        turn = !turn;
        Set<Short> temp = getPsuedoLegalMoves();
        turn = !turn;
        for (short sh : temp)
            if (moveToByteArray(sh)[1] == king)
                return true;
        return false;
    }

    public boolean isWhite(char c) {
        return Character.isUpperCase(c);
    }

    public boolean isBlack(char c) {
        return Character.isLowerCase(c);
    }

    public boolean isCapture(short s) {
        byte[] move = moveToByteArray(s);
        return (pieceAt(move[1]) != ' ') || move[2] == 2;
    }


    //Begin Rolling Methods
    public void rollback() {
        if (fenList.size() == 1)
            return;
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
    }


    public void rollForward() {
        if (extraFenList.size() == 0)
            return;
        String temp = extraFenList.remove(0);
        fenList.add(temp);
        Short sh = extraAllMovesMade.remove(0);
        allMovesMade.add(sh);
        List<String> tempFENList = new ArrayList<>(fenList);
        List<Short> tempAllMovesMade = new ArrayList<>(allMovesMade);
        fen = fenList.get(fenList.size() - 1);
        setFromFEN(fen);
        fenList = tempFENList;
        allMovesMade = tempAllMovesMade;
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
            for (Object o : args)
                res.append(o);
            System.out.print(res);
        }
    }

    public static void println(Object... args) {
        System.out.print("\r\n");
        if (args.length == 1) {
            System.out.println(args[0]);
        } else {
            StringBuilder res = new StringBuilder();
            for (Object o : args)
                res.append(o);
            System.out.print(res);
        }
    }


    //Begin Directional String Methods
    public static byte n(byte s) {
        if (s < 8)
            return -1;
        return (byte) (s - 8);
    }

    public static byte s(byte s) {
        if (s == -1 || s > 55)
            return -1;
        return (byte) (s + 8);
    }

    public static byte e(byte s) {
        if (s == -1 || s % 8 == 7)
            return -1;
        return (byte) (s + 1);
    }

    public static byte w(byte s) {
        if (s == -1 || s % 8 == 0)
            return -1;
        return (byte) (s - 1);
    }

    public static byte nw(byte a) {
        return n(w(a));
    }

    public static byte ne(byte a) {
        return n(e(a));
    }

    public static byte se(byte a) {
        return s(e(a));
    }

    public static byte sw(byte a) {
        return s(w(a));
    }

    public static byte nne(byte a) {
        return n(ne(a));
    }

    public static byte nee(byte a) {
        return ne(e(a));
    }

    public static byte nnw(byte a) {
        return n(nw(a));
    }

    public static byte nww(byte a) {
        return nw(w(a));
    }

    public static byte sse(byte a) {
        return s(se(a));
    }

    public static byte see(byte a) {
        return se(e(a));
    }

    public static byte ssw(byte a) {
        return s(sw(a));
    }

    public static byte sww(byte a) {
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


    public static List<Byte> longToBytes(Long l) {
        List<Byte> res = new ArrayList<>();
        for (byte i = 0; i < 64; i++)
            if (((l >>> i) & 1L) == 1L)
                res.add(i);
        return res;
    }


    //Begin Decoding Methods
    public static String decodeMove(short move) {
        String origin = Chess.byteToString((byte) (63 & (move >>> 10)));
        String destination = Chess.byteToString((byte) (63 & (move >>> 4)));
        boolean isPromotion = (3 & (move)) == 3;
        char promotion = switch (3 & (move >>> 2)) {
            case 1 -> 'n';
            case 2 -> 'r';
            case 3 -> 'b';
            default -> 'q';
        };
        return origin + destination + (isPromotion ? promotion : "");
    }

    public static byte[] moveToByteArray(short move) {
        byte[] res = new byte[4];
        res[0] = (byte) (63 & (move >>> 10));
        res[1] = (byte) (63 & (move >>> 4));
        res[2] = (byte) (3 & (move >>> 2));
        res[3] = (byte) (3 & move);
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

    public static List<String> shortToStringArray(List<Short> shorts) {
        List<String> res = new ArrayList<>();
        for (short b : shorts)
            res.add(decodeMove(b));
        return res;
    }

    //Begin Encoding Methods
    public static short encodeMove(byte origin, byte destination, byte promotion, byte flag) {
        return (short) ((origin << 10) | (destination << 4) | (promotion << 2) | flag);
    }

    public short encodeMove(String move) {
        move += 'q';
        byte origin = Chess.stringToByte(move.substring(0, 2));
        byte destination = Chess.stringToByte(move.substring(2, 4));
        int promotion = 0;
        byte flag = 0;
        if (wKC && origin == 60 && destination == 62)
            flag = 1;
        if (wQC && origin == 60 && destination == 58)
            flag = 1;
        if (bKC && origin == 4 && destination == 6)
            flag = 1;
        if (bQC && origin == 4 && destination == 2)
            flag = 1;
        if (destination == enPassant && Character.toLowerCase(pieceAt(origin)) == 'p')
            flag = 2;
        if ((destination < 8 && pieceAt(origin) == 'P') || (55 < destination && pieceAt(origin) == 'p')) {
            flag = 3;
            promotion = switch (move.charAt(4)) {
                case 'n' -> 1;
                case 'r' -> 2;
                case 'b' -> 3;
                default -> 0;
            };
        }
        return encodeMove(origin, destination, (byte) promotion, flag);
    }

    public static byte stringToByte(String str) {
        if (str.equals(""))
            return -1;
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
            if ((k + 1) % 8 == 0 && (k != 63))
                print("\r\n" + (7 - (k >>> 3)) + " | ", "");
        }
        print("\r\n  -----------------");
        print("    a b c d e f g h");
    }

    public static void print_bitboard(long a) {
        System.out.print("8 | ");
        for (int i = 0; i < 64; i++) {
            long temp = (a) >>> i & 1;
            System.out.print((temp != 0 ? temp : "*") + " ");
            if ((i + 1) % 8 == 0 && i != 63)
                System.out.print("\r\n" + (7 - (i >>> 3)) + " | ");
        }
        System.out.println("\r\n  -----------------");
        System.out.println("    a b c d e f g h");
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
}
