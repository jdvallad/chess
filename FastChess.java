import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FastChess extends Chess{
    private Set<Short> legalShortMoves;
    private long[][] pieceBoards;
    private long[] combinedBoards;
    private boolean[][] castleRights;
    private long enPassantSquare;
    private int halfMoveCount;
    public int fullMoveCount;
    private List<Set<Short>> pseudoLegalMoves;
    private List<Long> reversibleMoves;
    public List<String> hashList;
    public char[] hash;
    // Super Class methods

    public FastChess(String fen) {
        this();
        setFromFen(fen);
    }

    public FastChess() {
        gameResult = "";
        hash = new char[HASH_SIZE];
        turn = WHITE;
        gameOver = false;
        enPassantSquare = 0l;
        halfMoveCount = 0;
        fullMoveCount = 1;
        reversibleMoves = new ArrayList<>();
        legalShortMoves = new HashSet<>();
        legalMoves = new HashSet<>();
        hashList = new ArrayList<>();
        pseudoLegalMoves = new ArrayList<>();
        pieceBoards = new long[COLORS.length][PIECES.length];
        combinedBoards = new long[COLORS.length];
        castleRights = new boolean[COLORS.length][SIDES.length];
        for (int color : COLORS) {
            pseudoLegalMoves.add(new HashSet<>());
            for (int piece : PIECES) {
                pieceBoards[color][piece] = DEFAULT_PIECEBOARDS[color][piece];
                combinedBoards[color] |= pieceBoards[color][piece];
            }
            for (int side : SIDES) {
                castleRights[color][side] = true;
            }
        }
        setHash();
        hashList.add(new String(hash));
        getLegalShortMoves(legalShortMoves);
    }

    public FastChess clone() {
        FastChess output = new FastChess(false);
        output.turn = this.turn;
        output.pieceBoards = new long[2][];
        output.pieceBoards[0] = this.pieceBoards[0].clone();
        output.pieceBoards[1] = this.pieceBoards[1].clone();
        output.combinedBoards = this.combinedBoards.clone();
        output.castleRights = new boolean[2][];
        output.castleRights[0] = this.castleRights[0].clone();
        output.castleRights[1] = this.castleRights[1].clone();
        output.enPassantSquare = this.enPassantSquare;
        output.halfMoveCount = this.halfMoveCount;
        output.fullMoveCount = this.fullMoveCount;
        output.legalMoves = new HashSet<>(this.legalMoves);
        output.legalShortMoves = new HashSet<>(this.legalShortMoves);
        output.pseudoLegalMoves = new ArrayList<>();
        output.pseudoLegalMoves.add(new HashSet<>(this.pseudoLegalMoves.get(0)));
        output.pseudoLegalMoves.add(new HashSet<>(this.pseudoLegalMoves.get(1)));
        output.reversibleMoves = new ArrayList<>(this.reversibleMoves);
        output.hashList = new ArrayList<>(this.hashList);
        output.gameOver = this.gameOver;
        output.hash = this.hash.clone();
        return output;
    }

    public FastChess setFromFen(String fen) {
        gameResult = "";
        fen = fen.trim();
        int fileIndex = 0;
        int stringIndex = 0;
        char pointer = fen.charAt(stringIndex);
        for (long rank : RANKS_REVERSED) {
            fileIndex = 0;
            while (pointer != '/' && pointer != ' ') {
                if ('0' <= pointer && pointer <= '9') {
                    for (int i = 0; i < pointer - '0'; i++) {
                        add(FILES[fileIndex + i] & rank, WHITE, NONE_PIECE);
                    }
                    fileIndex += pointer - '0';
                    stringIndex++;
                    pointer = fen.charAt(stringIndex);
                    continue;
                }
                int piece = getPieceFromChar(pointer);
                int color = getColorFromChar(pointer);
                add(FILES[fileIndex] & rank, color, piece);
                fileIndex++;
                stringIndex++;
                pointer = fen.charAt(stringIndex);
            }
            stringIndex++;
            pointer = fen.charAt(stringIndex);
        }
        turn = (pointer == 'w') ? WHITE : BLACK;
        stringIndex += 2;
        pointer = fen.charAt(stringIndex);
        for (int color : COLORS) {
            for (int side : SIDES) {
                castleRights[color][side] = false;
            }
        }
        if (pointer == '-') {
            stringIndex++;
            pointer = fen.charAt(stringIndex);
        } else {
            while (pointer != ' ') {
                int color = getColorFromChar(pointer);
                int side = (Character.toUpperCase(pointer) == 'Q') ? QUEENSIDE : KINGSIDE;
                castleRights[color][side] = true;
                stringIndex++;
                pointer = fen.charAt(stringIndex);
            }
        }
        stringIndex++;
        pointer = fen.charAt(stringIndex);
        if (pointer == '-') {
            enPassantSquare = 0l;
        } else {
            long file = FILES[pointer - 'a'];
            stringIndex++;
            pointer = fen.charAt(stringIndex);
            long rank = RANKS[pointer - '1'];
            enPassantSquare = file & rank;
        }
        if (stringIndex == fen.length() - 1) {
            halfMoveCount = 0;
            fullMoveCount = 1;
        } else {
            stringIndex += 2;
            pointer = fen.charAt(stringIndex);
            String temp = "";
            while (pointer != ' ') {
                temp += pointer;
                stringIndex++;
                pointer = fen.charAt(stringIndex);
            }
            halfMoveCount = Integer.parseInt(temp);
            stringIndex++;
            temp = fen.substring(stringIndex);
            fullMoveCount = Integer.parseInt(temp);
        }
        reversibleMoves.clear();
        hashList.clear();
        setHash();
        hashList.add(new String(hash));
        getLegalShortMoves(legalShortMoves);
        if (isValidBoardState()) {
            return this;
        } else {
            return null;
        }
    }

    public String getFen() {
        StringBuilder build = new StringBuilder("");
        int count = 0;
        for (long rank : RANKS_REVERSED) {
            for (long file : FILES) {
                int piece = getPiece(rank & file);
                int color = getColor(rank & file);
                if (piece == NONE_PIECE) {
                    count++;
                    if ((file & H_FILE) != 0) {
                        build.append(count);
                        count = 0;
                    }
                    continue;
                }
                if (count > 0) {
                    build.append(count);
                    count = 0;
                }
                build.append(PIECE_CHARS_ASCII[color][piece]);
            }
            build.append("/");
        }
        build.setLength(build.length() - 1);
        build.append(" ");
        if (turn == WHITE) {
            build.append("w ");
        } else {
            build.append("b ");
        }
        boolean anyCastle = false;
        for (int color : COLORS) {
            for (int side : SIDES_REVERSED) {
                if (castleRights[color][side]) {
                    anyCastle = true;
                    char temp = side == KINGSIDE ? 'k' : 'q';
                    if (color == WHITE) {
                        temp = Character.toUpperCase(temp);
                    }
                    build.append(temp);
                }
            }
        }
        if (!anyCastle) {
            build.append("-");
        }
        build.append(" ");
        if (enPassantSquare == 0l) {
            build.append("-");
        } else {
            build.append((char) (getFileIndex(enPassantSquare) + 'a'));
            build.append((char) (getRankIndex(enPassantSquare) + '1'));
        }
        build.append(" ");
        build.append(halfMoveCount);
        build.append(" ");
        build.append(fullMoveCount);
        return build.toString();
    }

    public FastChess move(String moveString) {
        for (short move : legalShortMoves) {
            if (getMoveString(move).equals(moveString)) {
                move(move);
                return this;
            }
        }
        return this;
    }

    public FastChess move(String... moves) {
        for (String move : moves) {
            move(move);
        }
        return this;
    }

    public FastChess undo() {
        if (this.reversibleMoves.size() == 0) {
            return this;
        }
        undoNoUpdate();
        getLegalShortMoves(legalShortMoves);
        return this;
    }

    public Set<String> getLegalMoves() {
        return getLegalMoves(new HashSet<String>());
    }

    public Set<String> getLegalMoves(Set<String> output) {
        getLegalShortMoves(legalShortMoves);
        for (short move : legalShortMoves) {
            output.add(getMoveString(move));
        }
        return output;
    }

    public Map<String, Integer> perftMap(int depth) {
        Map<String, Integer> map = new HashMap<>();
        int[] nodes = { 0 };
        Set<Short> moves = getLegalShortMoves();
        if (depth == 1) {
            for (short move : moves) {
                map.put(getMoveString(move), 1);
            }
            map.put("total", moves.size());
            return map;
        }
        List<Thread> threads = new ArrayList<>();
        for (short move : moves) {
            Thread temp = new Thread() {
                public void run() {
                    int divide = FastChess.this.clone().moveNoUpdate(move).perft(depth - 1);
                    nodes[0] += divide;
                    map.put(getMoveString(move), divide);
                }
            };
            temp.start();
            threads.add(temp);
        }
        while (true) {
            boolean allDead = true;
            for (Thread p : threads) {
                if (p.isAlive()) {
                    allDead = false;
                }
            }
            if (allDead) {
                break;
            }
        }
        map.put("total", nodes[0]);
        return map;
    }

    public int perft(int depth, boolean verbose) {
        Map<String, Integer> map = perftMap(depth);
        if (verbose) {
            for (String key : map.keySet()) {
                System.out.println(key + ": " + map.get(key));
            }
        }
        return map.get("total");
    }

    public int perft(int depth) {
        Set<Short> moves = getLegalShortMoves();
        if (depth == 1) {
            return moves.size();
        }
        int nodes = 0;
        for (short move : moves) {
            nodes += this.moveNoUpdate(move).perft(depth - 1);
            this.undoNoUpdate();
        }
        return nodes;
    }

    public void printBoard(int perspective) {
        boolean flag = false;
        System.out.print("                                ");
        if (perspective == WHITE) {
            System.out.println("  a   b   c   d   e   f   g   h");
        } else {
            System.out.println("  h   g   f   e   d   c   b   a");

        }
        System.out.print("                                ");
        System.out.println("╻⎯⎯⎯╻⎯⎯⎯╻⎯⎯⎯╻⎯⎯⎯╻⎯⎯⎯╻⎯⎯⎯╻⎯⎯⎯╻⎯⎯⎯╻");
        System.out.print("                              ");
        if (perspective == WHITE) {
            System.out.print("8 ");
        } else {
            System.out.print("1 ");

        }
        System.out.print("│");
        for (long rank : (perspective == BLACK ? RANKS : RANKS_REVERSED)) {
            if (flag) {
                System.out.print("\r\n                                ");
                System.out.println("│⎯⎯⎯│⎯⎯⎯│⎯⎯⎯│⎯⎯⎯│⎯⎯⎯│⎯⎯⎯│⎯⎯⎯│⎯⎯⎯│");
                System.out.print("                              ");
                System.out.print("" + (1 + getRankIndex(rank & A_FILE)) + " ");
                System.out.print("│");
            }
            flag = true;
            foundPiece: for (long file : (perspective == WHITE ? FILES : FILES_REVERSED)) {
                long square = rank & file;
                for (int color : COLORS) {
                    for (int piece : PIECES) {
                        if ((pieceBoards[color][piece] & square) == square) {
                            System.out.print(" " + PIECE_CHARS_UNICODE[color][piece] + " │");
                            if (file == (perspective == BLACK ? A_FILE : H_FILE)) {
                                System.out.print(" " + (1 + getRankIndex(rank & file)));
                            }
                            continue foundPiece;
                        }
                    }
                }
                System.out.print("   │");
                if (file == (perspective == BLACK ? A_FILE : H_FILE)) {
                    System.out.print(" " + (1 + getRankIndex(rank & file)));
                }
            }
        }
        System.out.print("\r\n                                ");
        System.out.println("╹⎯⎯⎯╹⎯⎯⎯╹⎯⎯⎯╹⎯⎯⎯╹⎯⎯⎯╹⎯⎯⎯╹⎯⎯⎯╹⎯⎯⎯╹");
        System.out.print("                                ");
        System.out.println("  a   b   c   d   e   f   g   h\r\n");
    }

    public void printEntireGame() {
        System.out.print("[");
        boolean flag = false;
        for (long move : reversibleMoves) {
            if (flag) {
                System.out.print(", ");
            }
            flag = true;
            System.out.print(getMoveStringReversible(move) + "");
        }
        System.out.println("]");
        this.printBoard(WHITE);
        System.out.println(gameResult);
    }

    public void printLegalMoves() {
        getLegalShortMoves(legalShortMoves);
        System.out.print("[");
        boolean flag = false;
        for (String move : legalMoves) {
            if (flag) {
                System.out.print(", ");
            }
            flag = true;
            System.out.print(move + "");
        }

        System.out.println("]");
    }

    public void printPseudoLegalMoves() {
        getPseudoLegalMoves(pseudoLegalMoves.get(turn));
        System.out.print("[");
        boolean flag = false;
        for (short move : pseudoLegalMoves.get(turn)) {
            if (flag) {
                System.out.print(", ");
            }
            flag = true;
            System.out.print(getMoveString(move) + "");
        }

        System.out.println("]");
    }

    public void reset(){
        while(reversibleMoves.size() > 0){
            undo();
        }
    }

    public String getLastMove(){
        if(reversibleMoves.size() == 0){
            return "";
        }
        return getMoveStringReversible(reversibleMoves.get(reversibleMoves.size() - 1));
    }
    public static void print(long input) {
        for (long i = 63; i >= 0; i--) {
            if (i % 8 == 7) {
                System.out.println();
            }
            long index = 8 * (i / 8) + (7 - (i % 8));
            System.out.print(" " + ((input >>> index) & 1l));
        }
        System.out.println();
        System.out.println();

    }

    // Helper Methods

    private FastChess(boolean empty) {

    }

    private FastChess move(short move) {
        moveNoUpdate(move);
        getLegalShortMoves(legalShortMoves);
        return this;
    }

    private FastChess moveNoUpdate(short move) {
        reversibleMoves.add(encodeReversibleMove(move));
        enPassantSquare = 0;
        switch (getFlag(move)) {
            case FLAG_PROMOTION:
                makePromotionMove(move);
                break;
            case FLAG_CASTLE:
                makeCastleMove(move);
                break;
            case FLAG_EN_PASSANT:
                makeEnPassantMove(move);
                break;
            case FLAG_STANDARD:
                makeStandardMove(move);
                break;
            default:

        }
        if (turn == BLACK) {
            fullMoveCount += 1;
        }
        halfMoveCount += 1;
        turn ^= 1;
        updateHash(move);
        hashList.add(new String(hash));
        return this;
    }

    private void makePromotionMove(short move) {
        long start = getStartingSquare(move);
        long end = getEndingSquare(move);
        byte promotion = getPromotion(move);
        int startColor = getColor(start);
        int endColor = getColor(end);
        add(start, startColor, promotion);
        move(start, end);
        for (int side : SIDES) {
            if (castleRights[endColor][side]
                    && (ROOK_STARTING_FILES[side] & getBackRank(endColor)) == end) {
                castleRights[endColor][side] = false;
            }
        }
        halfMoveCount = -1;
        return;
    }

    private void makeCastleMove(short move) {
        long start = getStartingSquare(move);
        long end = getEndingSquare(move);
        int startColor = getColor(start);
        int endColor = getColor(end);
        long mainStartSquare = start;
        long helperStartSquare = end;
        long mainRank = getBackRank(startColor);
        long helperRank = getBackRank(endColor);
        long mainEndSquare = 0;
        long helperEndSquare = 0;
        if (getFileIndex(end) - getFileIndex(start) > 0) {// kingside castle if castling to the right
            mainEndSquare = mainRank & G_FILE;
            helperEndSquare = helperRank & F_FILE;
        } else {
            mainEndSquare = mainRank & C_FILE;
            helperEndSquare = helperRank & D_FILE;
        }
        move(mainStartSquare, mainEndSquare);
        move(helperStartSquare, helperEndSquare);
        for (int side : SIDES) {
            castleRights[startColor][side] = false;
        }
        return;
    }

    private void makeEnPassantMove(short move) {
        long start = getStartingSquare(move);
        long end = getEndingSquare(move);
        long captureSquare = FastChess.compassLong(end, end - start > 0 ? SOUTH : NORTH, 0);
        remove(captureSquare);
        move(start, end);
        halfMoveCount = -1;
        return;
    }

    private void makeStandardMove(Short move) {
        long start = getStartingSquare(move);
        long end = getEndingSquare(move);
        int startPiece = getPiece(start);
        int endPiece = getPiece(end);
        int startColor = getColor(start);
        int endColor = getColor(end);
        move(start, end);
        if (startPiece == PAWN || endPiece != NONE_PIECE) { // pawn move or capture, reset halfMoveCount
            halfMoveCount = -1;
        }
        if (startPiece == PAWN) { // update enPassantSquare
            int rankDifference = getRankIndex(end) - getRankIndex(start);
            if (Math.abs(rankDifference) == 2) {
                enPassantSquare = compassLong(start, rankDifference / 2, 0);
            }
        }
        for (int side : SIDES) { // update castling rights
            if (castleRights[startColor][side] // update if moving king
                    && startPiece == KING) {
                castleRights[startColor][side] = false;
            }
            if (castleRights[startColor][side] // update if moving an unmoved rook
                    && (ROOK_STARTING_FILES[side] & getBackRank(startColor)) == start) {
                castleRights[startColor][side] = false;
            }
            if (castleRights[endColor][side] // update if capturing an unmoved rook
                    && (ROOK_STARTING_FILES[side] & getBackRank(endColor)) == end) {
                castleRights[endColor][side] = false;
            }
        }
    }

    private FastChess undoNoUpdate() {
        if (reversibleMoves.size() == 0) {
            return this;
        }
        // castleRights (4 bits)
        halfMoveCount = 0; // (32 bits)
        int flag = 0; // (2 bits)
        int enPassantSquareOffset = 0; // 6 bits
        int endingColor = 0; // 1 bit
        int endingPiece = 0; // 3 bits
        int endingSquareOffset = 0; // 6 bits
        int startingPiece = 0; // 3 bits
        int startingSquareOffset = 0; // 6 bits
        long endingSquare = 0l;
        long startingSquare = 0l;
        long input = reversibleMoves.remove(reversibleMoves.size() - 1);
        long push = 0;
        for (int color : COLORS_REVERSED) {
            for (int side : SIDES_REVERSED) {
                push = 1l;
                while (push != 0) {
                    castleRights[color][side] = (push & input) != 0;
                    push = push >>> 1;
                }
                input = input >>> 1;
            }
        }
        push = 1l << 31;
        while (push != 0) {
            halfMoveCount |= push & input;
            push = push >>> 1;
        }
        input = input >>> 32;

        push = 1l << 1;
        while (push != 0) {
            flag |= push & input;
            push = push >>> 1;
        }
        input = input >>> 2;

        push = 1l << 5;
        while (push != 0) {
            enPassantSquareOffset |= push & input;
            push = push >>> 1;
        }
        if (enPassantSquareOffset != 0) { // EnPassantOffset gets encoded as 0 if their is not enpassant square
            enPassantSquare = 1L << enPassantSquareOffset;
        } else {
            enPassantSquare = 0;
        }
        input = input >>> 6;

        push = 1l;
        while (push != 0) {
            endingColor |= push & input;
            push = push >>> 1;
        }
        input = input >>> 1;

        push = 1l << 2;
        while (push != 0) {
            endingPiece |= push & input;
            push = push >>> 1;
        }
        input = input >>> 3;

        push = 1l << 5;
        while (push != 0) {
            endingSquareOffset |= push & input;
            push = push >>> 1;
        }
        endingSquare = 1l << endingSquareOffset;
        input = input >>> 6;

        push = 1l << 2;
        while (push != 0) {
            startingPiece |= push & input;
            push = push >>> 1;
        }
        input = input >>> 3;

        push = 1l << 5;
        while (push != 0) {
            startingSquareOffset |= push & input;
            push = push >>> 1;
        }
        startingSquare = 1l << startingSquareOffset;
        input = input >>> 6;
        int startingColor = turn ^ 1;
        switch (flag) {
            case FLAG_CASTLE:
                long back = getBackRank(startingColor);
                if ((ROOK_STARTING_FILES[QUEENSIDE] & back) == endingSquare) {
                    swap(back & C_FILE, startingSquare);
                    swap(back & D_FILE, endingSquare);
                } else {
                    swap(back & G_FILE, startingSquare);
                    swap(back & F_FILE, endingSquare);
                }
                break;
            case FLAG_PROMOTION:
                add(startingSquare, startingColor, startingPiece);
                replace(endingSquare, endingColor, endingPiece);
                break;
            case FLAG_EN_PASSANT:
                long addPawnSquare = RANKS[getRankIndex(startingSquare)] & FILES[getFileIndex(endingSquare)];
                replace(addPawnSquare, endingColor, endingPiece);
                remove(endingSquare);
                add(startingSquare, startingColor, startingPiece);
                break;
            case FLAG_STANDARD:
                replace(startingSquare, startingColor, startingPiece);
                replace(endingSquare, endingColor, endingPiece);
                break;
            default:

        }
        turn ^= 1;
        if(turn == BLACK){
            fullMoveCount--;
        }
        this.gameOver = false;
        this.gameResult = "";
        hashList.remove(hashList.size() - 1);
        hash = hashList.get(hashList.size() - 1).toCharArray();
        return this;
    }

    private Set<Short> getLegalShortMoves() {
        return getLegalShortMoves(new HashSet<Short>());
    }

    private Set<Short> getLegalShortMoves(Set<Short> output) {
        output.clear();
        legalMoves.clear();
        if (gameOver) {
            return output;
        }
        if (Collections.frequency(hashList, hashList.get(hashList.size() - 1)) == 5) {
            gameOver = true; // 5 fold repetition
            gameResult = "draw by 5-fold repetition!";
            return output;
        }
        if (halfMoveCount == 75) {
            gameOver = true;
            gameResult = "draw by 50 move rule!";
            return output;
        }
        getPseudoLegalMoves(pseudoLegalMoves.get(turn));
        for (short move : pseudoLegalMoves.get(turn)) {
            this.moveNoUpdate(move);
            getPseudoLegalMoves(pseudoLegalMoves.get(turn));
            if (!enemyInCheck()) {
                output.add(move);
                legalMoves.add(getMoveString(move));
            }
            undoNoUpdate();
        }
        if (output.size() == 0) {
            gameOver = true;
            if (inCheck()) {
                gameResult = (turn == WHITE ? "black" : "white") + " wins!";
            } else {
                gameResult = "draw by stalemate!";
            }
        }
        return output;
    }

    private Set<Short> getPseudoLegalMoves(Set<Short> moveSet) {
        moveSet.clear();
        addPseudoLegalPawnDoublePushes(moveSet);
        addPseudoLegalPawnSinglePushes(moveSet);
        addPseudoLegalPawnCaptures(moveSet, EAST);
        addPseudoLegalPawnCaptures(moveSet, WEST);
        addPseudoLegalKnightMoves(moveSet);
        addPseudoLegalSlidingMoves(moveSet, pieceBoards[turn][BISHOP] | pieceBoards[turn][QUEEN], DIAGONAL_RAYS);
        addPseudoLegalSlidingMoves(moveSet, pieceBoards[turn][ROOK] | pieceBoards[turn][QUEEN], ORTHOGONAL_RAYS);
        addPseudoLegalKingMoves(moveSet);
        addPseudoLegalCastleMoves(moveSet);
        return moveSet;
    }

    private void addPseudoLegalPawnDoublePushes(Set<Short> moveSet) {
        final long NONE_PIECESquares = getEmpty();
        int forward = (turn == BLACK) ? SOUTH : NORTH;
        long pawnBirthSquares = ((turn == BLACK) ? RANK_7 : RANK_2);
        long doublePawns = pieceBoards[turn][PAWN] & pawnBirthSquares; // all friendly pawns who haven't moved
        doublePawns = compassLong(doublePawns, forward, 0) & NONE_PIECESquares;
        doublePawns = compassLong(doublePawns, forward, 0) & NONE_PIECESquares;
        short move = 0;
        for (byte end : serializeBitboard(doublePawns)) {
            byte start = compassOffset(end, -2 * forward, 0);
            move = encodeMove(start, end, 0, FLAG_STANDARD);
            moveSet.add(move);
        }
    }

    private void addPseudoLegalPawnSinglePushes(Set<Short> moveSet) {
        final long NONE_PIECESquares = getEmpty();
        int forward = (turn == BLACK) ? SOUTH : NORTH;
        long doublePawns = pieceBoards[turn][PAWN]; // all friendly pawns
        doublePawns = compassLong(doublePawns, forward, 0) & NONE_PIECESquares;
        short move = 0;
        for (byte end : serializeBitboard(doublePawns)) {
            byte start = compassOffset(end, -forward, 0);
            if (end > 55 || end < 8) {
                for (int piece : PROMOTION_PIECES) {
                    move = encodeMove(start, end, piece, FLAG_PROMOTION);
                    moveSet.add(move);
                }
            } else {
                move = encodeMove(start, end, 0, FLAG_STANDARD);
                moveSet.add(move);
            }
        }
    }

    private void addPseudoLegalPawnCaptures(Set<Short> moveSet, int direction) {
        // DON'T FORGET PROMOTION AND EN PASSANT!!!
        final long captureSquares = combinedBoards[turn ^ 1] | enPassantSquare;
        int forward = (turn == BLACK) ? SOUTH : NORTH;
        long doublePawns = pieceBoards[turn][PAWN]; // all friendly pawns
        doublePawns = compassLong(doublePawns, forward, direction) & captureSquares;
        short move = 0;
        byte enPassantSquareOffset = (byte) getIndex(enPassantSquare);
        for (byte end : serializeBitboard(doublePawns)) {
            byte start = compassOffset(end, -forward, -direction);
            if (end > 55 || end < 8) {
                for (int piece : PROMOTION_PIECES) {
                    move = encodeMove(start, end, piece, FLAG_PROMOTION);
                    moveSet.add(move);
                }
            } else if (end == enPassantSquareOffset) {
                move = encodeMove(start, end, 0, FLAG_EN_PASSANT);
                moveSet.add(move);
            } else {
                move = encodeMove(start, end, 0, FLAG_STANDARD);
                moveSet.add(move);
            }
        }
    }

    private void addPseudoLegalSlidingMoves(Set<Short> moveSet, long startingPieces, byte[][][] serializedRays) {
        for (byte start : serializeBitboard(startingPieces)) {
            for (byte[][] direction : serializedRays) {
                for (byte end : direction[start]) {
                    long endBoard = getBitboard(end);
                    if ((endBoard & combinedBoards[turn]) != 0) {
                        break;
                    }
                    moveSet.add(encodeMove(start, end, 0, FLAG_STANDARD));
                    if ((endBoard & combinedBoards[turn ^ 1]) != 0) {
                        break;
                    }
                }
            }
        }
    }

    private void addPseudoLegalKnightMoves(Set<Short> moveSet) {
        long knights = pieceBoards[turn][KNIGHT];
        long destinationSquares = combinedBoards[turn ^ 1] | getEmpty();
        List<Byte> serializedStart = serializeBitboard(knights);
        List<Byte> serializedEnd;
        for (byte start : serializedStart) {
            serializedEnd = serializeBitboard(KNIGHT_MOVE_SQUARES[start] & destinationSquares);
            for (byte end : serializedEnd) {
                moveSet.add(encodeMove(start, end, 0, FLAG_STANDARD));
            }
        }
    }

    private void addPseudoLegalKingMoves(Set<Short> moveSet) {
        long king = pieceBoards[turn][KING];
        long destinationSquares = combinedBoards[turn ^ 1] | getEmpty();
        List<Byte> serializedStart = serializeBitboard(king);
        List<Byte> serializedEnd;
        for (byte start : serializedStart) {
            serializedEnd = serializeBitboard(KING_MOVE_SQUARES[start] & destinationSquares);
            for (byte end : serializedEnd) {
                moveSet.add(encodeMove(start, end, 0, FLAG_STANDARD));
            }
        }
    }

    private void addPseudoLegalCastleMoves(Set<Short> moveSet) {
        long king = pieceBoards[turn][KING];
        byte kingOffset = (byte) getIndex(king);
        if (castleRights[turn][KINGSIDE]
                & (isEmpty(compassLong(king, NONE_DIR, EAST) | compassLong(king, NONE_DIR, 2 * EAST)))) {
            moveSet.add(encodeMove(kingOffset, compassOffset(kingOffset, NONE_DIR, 3 * EAST), 0, FLAG_CASTLE));
        }
        if (castleRights[turn][QUEENSIDE]
                & (isEmpty(
                        compassLong(king, NONE_DIR, WEST) | compassLong(king, NONE_DIR, 2 * WEST)
                                | compassLong(king, NONE_DIR, 3 * WEST)))) {
            moveSet.add(encodeMove(kingOffset, compassOffset(kingOffset, NONE_DIR, 4 * WEST), 0, FLAG_CASTLE));
        }
    }

    private List<Byte> serializeBitboard(long board) {
        List<Byte> output = new ArrayList<>();
        byte index = 0;
        while (board != 0) {
            index = (byte) getIndex(board);
            board = board & (~(1l << index));
            output.add(index);
        }
        return output;
    }

    private boolean isEmpty(long squares) {
        return (squares & (combinedBoards[WHITE] | combinedBoards[BLACK])) == 0;
    }

    private boolean enemyInCheck() {
        return enemySquareAttacked(pieceBoards[turn ^ 1][KING]);
    }

    public boolean inCheck() {
        turn ^= 1;
        getPseudoLegalMoves(pseudoLegalMoves.get(turn));
        boolean result = enemySquareAttacked(pieceBoards[turn ^ 1][KING]);
        turn ^= 1;
        return result;
    }

    private boolean enemySquareAttacked(long square) { // Returns true if one of your pieces is attacking a square
        // In the case of pawns, only considers diagonal attacks.
        // also if a king just castled and a square it castled through is attacked,
        // including starting square, and
        // square = where the king is now, this will return true

        if (((compassLong(pieceBoards[turn][PAWN], (turn == BLACK) ? SOUTH : NORTH, EAST)

                | compassLong(pieceBoards[turn][PAWN], (turn == BLACK) ? SOUTH : NORTH, WEST)) & getEmpty()
                & square) != 0) {
            return true;
        }
        long enemyKing = pieceBoards[turn ^ 1][KING];
        if (square == enemyKing) {
            if (reversibleMoves.size() > 0
                    && ((reversibleMoves.get(reversibleMoves.size() - 1) >>> 36) & 3) == FLAG_CASTLE) {
                if ((pieceBoards[turn ^ 1][KING] & G_FILE) != 0) {
                    if (enemySquareAttacked(compassLong(enemyKing, NONE_DIR, WEST))) {
                        return true;
                    }
                    if (enemySquareAttacked(compassLong(enemyKing, NONE_DIR, 2 * WEST))) {
                        return true;
                    }
                } else {
                    if (enemySquareAttacked(compassLong(enemyKing, NONE_DIR, EAST))) {
                        return true;
                    }
                    if (enemySquareAttacked(compassLong(enemyKing, NONE_DIR, 2 * EAST))) {
                        return true;
                    }
                }
            }
        }
        for (short move : pseudoLegalMoves.get(turn)) {
            if (getEndingSquare(move) == square) {
                long startingSquare = getStartingSquare(move);
                int attackingPiece = getPiece(startingSquare);
                if (attackingPiece == PAWN && getFileIndex(square) == getFileIndex(startingSquare)) {
                    continue;
                }
                return true;
            }
        }
        return false;
    }

    private long encodeReversibleMove(short move) {
        // Info needed to encode reversible move:
        // starting and ending square, starting and ending piece,
        // starotting and ending piece color, enPassantSquare
        // castleRights, move flag, and half move count.
        // starting piece and starting color can be inferred from board state
        // so they do not need to be encoded. But they will just for ease.

        long startingSquareLong = getStartingSquare(move);
        int startingSquareInt = Long.numberOfTrailingZeros(startingSquareLong); // 6 bits
        int startingPiece = getPiece(startingSquareLong); // 3 bits
        long endingSquareLong = getEndingSquare(move);
        int endingSquareInt = Long.numberOfTrailingZeros(endingSquareLong); // 6 bits
        int endingPiece = getPiece(endingSquareLong); // 3 bits
        int endingColor = getColor(endingSquareLong); // 1 bit
        int enPassantSquareOffset = Long.numberOfTrailingZeros(enPassantSquare); // 6 bits
        int flag = getFlag(move); // (2 bits)
        // halfMoveCount (32 bits)
        // castleRights (4 bits)
        // 64 bits, a long! (niceee)
        if (flag == FLAG_EN_PASSANT) {
            long captureSquare = RANKS[getRankIndex(startingSquareLong)] & FILES[getFileIndex(endingSquareLong)];
            endingPiece = getPiece(captureSquare);
            endingColor = getColor(captureSquare);
        }
        long output = 0;
        long push = 1l << 5;
        while (push != 0) {
            output |= push & startingSquareInt;
            push = (push >>> 1);
        }

        output = output << 3;
        push = 1l << 2;
        while (push != 0) {
            output |= push & startingPiece;
            push = (push >>> 1);
        }

        output = output << 6;
        push = 1l << 6 - 1;
        while (push != 0) {
            output |= push & endingSquareInt;
            push = (push >>> 1);
        }

        output = output << 3;
        push = 1l << 2;
        while (push != 0) {
            output |= push & endingPiece;
            push = (push >>> 1);
        }

        output = output << 1;
        push = 1l;
        while (push != 0) {
            output |= push & endingColor;
            push = (push >>> 1);
        }

        output = output << 6;
        push = 1l << 5;
        while (push != 0) {
            output |= push & enPassantSquareOffset; // if enPassantSquare is 0, then enPassantSquareOffset gets encoded
                                                    // as 64, but only 5 bits, so get set as 0 in encodedMove
            push = (push >>> 1);
        }

        output = output << 2;
        push = 1l << 1;
        while (push != 0) {
            output |= push & flag;
            push = (push >>> 1);
        }

        output = output << 32;
        push = 1l << 31;
        while (push != 0) {
            output |= push & halfMoveCount;
            push = (push >>> 1);
        }

        for (int color : COLORS) {
            for (int side : SIDES) {
                output = output << 1;
                push = 1l;
                while (push != 0) {
                    output |= push & (castleRights[color][side] ? 1l : 0l);
                    push = (push >>> 1);
                }
            }
        }

        return output;
    }

    private void setHash() {
        int index = 0;
        while (index < 64) {
            long square = FILES[index % 8] & RANKS[index / 8];
            hash[index] = PIECE_CHARS_ASCII[getColor(square)][getPiece(square)]; // 0-63
            index++;
        }
        hash[index] = (turn == WHITE) ? 'w' : 'b'; // 64
        index++;
        for (int color : COLORS) {
            for (int side : SIDES) {
                hash[index] = castleRights[color][side] ? 'T' : 'F'; // 65-69
                index++;
            }
        }
        if (enPassantSquare == 0) {
            hash[index] = '-'; // 70
            index++;
            hash[index] = '-'; // 71
            index++;
        } else {
            hash[index] = (char) ('a' + getFileIndex(enPassantSquare)); // 70
            index++;
            hash[index] = (char) ('1' + getRankIndex(enPassantSquare)); // 71
            index++;
        }
    }

    private void updateHash(short move) {
        long start = getStartingSquare(move);
        long end = getEndingSquare(move);
        byte promotion = getPromotion(move);
        int startColor = getColor(start);
        int endColor = getColor(end);
        switch (getFlag(move)) {
            case FLAG_PROMOTION:
                hash[getIndex(start)] = ' ';
                hash[getIndex(end)] = PIECE_CHARS_ASCII[startColor][promotion];
                break;
            case FLAG_EN_PASSANT:
                long captureSquare = FastChess.compassLong(end, end - start > 0 ? SOUTH : NORTH, 0);
                hash[getIndex(start)] = ' ';
                hash[getIndex(end)] = PIECE_CHARS_ASCII[turn][PAWN];
                hash[getIndex(captureSquare)] = ' ';
                break;
            case FLAG_CASTLE:
                long mainRank = getBackRank(startColor);
                long helperRank = getBackRank(endColor);
                long mainEndSquare = 0;
                long helperEndSquare = 0;
                if (getFileIndex(end) - getFileIndex(start) > 0) {// kingside castle if castling to the right
                    mainEndSquare = mainRank & G_FILE;
                    helperEndSquare = helperRank & F_FILE;
                } else {
                    mainEndSquare = mainRank & C_FILE;
                    helperEndSquare = helperRank & D_FILE;
                }
                hash[getIndex(start)] = ' ';
                hash[getIndex(end)] = ' ';
                hash[getIndex(mainEndSquare)] = PIECE_CHARS_ASCII[startColor][KING];
                hash[getIndex(helperEndSquare)] = PIECE_CHARS_ASCII[startColor][ROOK];
                break;
            case FLAG_STANDARD:
                int startPiece = getPiece(end);
                hash[getIndex(start)] = ' ';
                hash[getIndex(end)] = PIECE_CHARS_ASCII[startColor][startPiece];
                break;
            default:
                break;
        }
        int index = 64;
        hash[index] = (turn == WHITE) ? 'w' : 'b'; // 64
        index++;
        for (int color : COLORS) {
            for (int side : SIDES) {
                hash[index] = castleRights[color][side] ? 'T' : 'F'; // 65-69
                index++;
            }
        }
        if (enPassantSquare == 0) {
            hash[index] = '-'; // 70
            index++;
            hash[index] = '-'; // 71
            index++;
        } else {
            hash[index] = (char) ('a' + getFileIndex(enPassantSquare)); // 70
            index++;
            hash[index] = (char) ('1' + getRankIndex(enPassantSquare)); // 71
            index++;
        }
    }

    private String getMoveString(short move) {
        long startingSquare = getStartingSquare(move);
        long endingSquare = getEndingSquare(move);
        int flag = getFlag(move);
        if (flag == FLAG_CASTLE) {
            if (FILES[getFileIndex(endingSquare)] == ROOK_STARTING_FILES[KINGSIDE]) {
                endingSquare = compassLong(startingSquare, NONE_DIR, 2 * EAST);
            } else {
                endingSquare = compassLong(startingSquare, NONE_DIR, 2 * WEST);
            }
        }
        char startingFile = (char) ('a' + getFileIndex(startingSquare));
        char startingRank = (char) ('1' + getRankIndex(startingSquare));
        char endingFile = (char) ('a' + getFileIndex(endingSquare));
        char endingRank = (char) ('1' + getRankIndex(endingSquare));
        String output = "" + startingFile + startingRank + endingFile + endingRank;
        if (flag == FLAG_PROMOTION) {
            output += PIECE_CHARS_ASCII[BLACK][getPromotion(move)];
        }
        return output;
    }

    private long getBackRank(int color) {
        return (color == BLACK) ? RANK_8 : RANK_1;
    }

    private void move(long start, long end) {
        int startPiece = getPiece(start);
        int startColor = getColor(start);
        remove(start);
        add(end, startColor, startPiece);
    }

    private void swap(long start, long end) {
        int startPiece = getPiece(start);
        int endPiece = getPiece(end);
        int startColor = getColor(start);
        int endColor = getColor(end);
        add(start, endColor, endPiece);
        add(end, startColor, startPiece);
    }

    private void replace(long square, int color, int piece) {
        add(square, color, piece);
    }

    private void add(long square, int color, int piece) {
        remove(square);
        if (piece != NONE_PIECE) {
            combinedBoards[color] |= square; // add piece to combinedBoard

            pieceBoards[color][piece] |= square; // add piece to board
        }
    }

    private void remove(long square) {
        int priorPiece = getPiece(square);
        int priorColor = getColor(square);
        if (priorPiece != NONE_PIECE) {
            combinedBoards[priorColor] &= ~square; // remove prior piece from combinedBoard if its not NONE_PIECE

            pieceBoards[priorColor][priorPiece] &= ~square; // remove prior piece from board
        }
    }

    private int getPiece(long square) {
        int color = getColor(square);
        for (int piece : PIECES) {
            if ((pieceBoards[color][piece] & square) == square) {
                return piece;
            }
        }
        return NONE_PIECE;
    }

    private int getColor(long square) {
        return (combinedBoards[BLACK] & square) == square ? BLACK : WHITE;
    }

    private long getStartingSquare(short move) {
        byte start = (byte) (63 & (move >>> 10));
        return 1l << start;
    }

    private long getEndingSquare(short move) {
        byte end = (byte) (63 & (move >>> 4));
        return 1l << end;
    }

    private byte getPromotion(short move) {
        return (byte) (3 & (move >>> 2));
    }

    private byte getFlag(short move) {
        return (byte) (3 & move);
    }

    private long getEmpty() {
        return ~(combinedBoards[WHITE] | combinedBoards[BLACK]);
    }

    private boolean isValidBoardState() {
        if (Long.bitCount(pieceBoards[WHITE][KING]) != 1) { // exactly one white king
            return false;
        }
        if (Long.bitCount(pieceBoards[BLACK][KING]) != 1) { // exactly one black king
            return false;
        }
        if (((RANK_1 | RANK_8) & (pieceBoards[WHITE][PAWN] | pieceBoards[WHITE][PAWN])) != 0) { // no pawns on back rank
            return false;
        }
        for (int color : COLORS) {
            long backRank = getBackRank(color);
            long kingStartingSquare = backRank & E_FILE;
            for (int side : SIDES) {
                if (castleRights[color][side]) {
                    if (pieceBoards[color][KING] != kingStartingSquare) {
                        return false;
                    }
                    if ((pieceBoards[color][ROOK] & (backRank & ROOK_STARTING_FILES[side])) == 0) {
                        return false;
                    }
                }
            }
        }
        if (enemyInCheck()) {
            return false;
        }
        return true;
    }

    private int getPieceFromChar(char c) {
        if (c == ' ') {
            return NONE_PIECE;
        }
        switch (Character.toUpperCase(c)) {
            case 'Q':
                return QUEEN;
            case 'R':
                return ROOK;
            case 'B':
                return BISHOP;
            case 'N':
                return KNIGHT;
            case 'K':
                return KING;
            case 'P':
                return PAWN;
            default:
                return -1;
        }
    }

    private int getColorFromChar(char c) {
        if (c == ' ') {
            return WHITE;
        }
        return Character.isUpperCase(c) ? WHITE : BLACK;
    }

    private String getMoveStringReversible(long temp) {
        int endingSquareOffset = 0;
        int startingSquareOffset = 0;
        long endingSquare = 0l;
        long startingSquare = 0l;
        long push = 0;
        long input = temp;
        input = (input >>> 48);
        push = 1l << 5;
        while (push != 0) {
            endingSquareOffset |= push & input;
            push = (push >>> 1);
        }
        endingSquare = 1L << endingSquareOffset;
        input = (temp >>> 57);
        push = 1l << 5;
        while (push != 0) {
            startingSquareOffset |= push & input;
            push = (push >>> 1);
        }
        startingSquare = 1l << startingSquareOffset;
        char startingFile = (char) ('a' + getFileIndex(startingSquare));
        char startingRank = (char) ('1' + getRankIndex(startingSquare));
        char endingFile = (char) ('a' + getFileIndex(endingSquare));
        char endingRank = (char) ('1' + getRankIndex(endingSquare));
        return "" + startingFile + startingRank + endingFile + endingRank;
    }

    // Static Data
    private static short encodeMove(byte startOffset, byte endOffset, int promotion, int flag) {
        return (short) (((63 & startOffset) << 10) | ((63 & endOffset) << 4) | ((3 & promotion) << 2) | (3 & flag));
    }

    private static int getIndex(long square) {
        return Long.numberOfTrailingZeros(square);
    }

    private static int getRankIndex(long square) {
        int zeroes = Long.numberOfTrailingZeros(square);
        return (zeroes / 8);
    }

    private static int getFileIndex(long square) {
        int zeroes = Long.numberOfTrailingZeros(square);
        return (zeroes % 8);
    }

    private static long getBitboard(int index) {
        return 1l << index;
    }

    private static long compassLong(long input, int north, int east) {
        input = input & VERTICAL_OFFSETS[north + 8] & HORIZONTAL_OFFSETS[east + 8];
        input = north >= 0 ? input << (north * 8) : input >>> -(north * 8);
        input = east >= 0 ? input << east : input >>> -east;
        return input;
    }

    private static byte compassOffset(byte input, int north, int east) {
        return (byte) (input + east + 8 * north);
    }

    public static final int NORTH = 1;
    public static final int SOUTH = -1;
    public static final int EAST = 1;
    public static final int WEST = -1;
    public static final int NONE_DIR = 0;
    public static final int QUEEN = 0;
    public static final int KNIGHT = 1;
    public static final int BISHOP = 2;
    public static final int ROOK = 3;
    public static final int KING = 4;
    public static final int PAWN = 5;
    public static final int NONE_PIECE = 6;
    public static final int QUEENSIDE = 0;
    public static final int KINGSIDE = 1;
    public static final long A_FILE = 72340172838076673l;
    public static final long B_FILE = 144680345676153346l;
    public static final long C_FILE = 289360691352306692l;
    public static final long D_FILE = 578721382704613384l;
    public static final long E_FILE = 1157442765409226768l;
    public static final long F_FILE = 2314885530818453536l;
    public static final long G_FILE = 4629771061636907072l;
    public static final long H_FILE = -9187201950435737472l;
    public static final long RANK_1 = 255l;
    public static final long RANK_2 = 65280l;
    public static final long RANK_3 = 16711680l;
    public static final long RANK_4 = 4278190080l;
    public static final long RANK_5 = 1095216660480l;
    public static final long RANK_6 = 280375465082880l;
    public static final long RANK_7 = 71776119061217280l;
    public static final long RANK_8 = -72057594037927936l;
    public static final byte FLAG_STANDARD = 0;
    public static final byte FLAG_CASTLE = 1;
    public static final byte FLAG_EN_PASSANT = 2;
    public static final byte FLAG_PROMOTION = 3;
    public static final int[] PROMOTION_PIECES = { QUEEN, KNIGHT, BISHOP, ROOK };
    public static final byte[] FLAGS = new byte[] { FLAG_STANDARD, FLAG_CASTLE, FLAG_EN_PASSANT, FLAG_PROMOTION };
    public static final int[] PIECES = new int[] { QUEEN, KNIGHT, BISHOP, ROOK, KING, PAWN };
    public static final long[] FILES = new long[] { A_FILE, B_FILE, C_FILE, D_FILE, E_FILE, F_FILE, G_FILE, H_FILE };
    public static final long[] FILES_REVERSED = new long[] { H_FILE, G_FILE, F_FILE, E_FILE, D_FILE, C_FILE, B_FILE,
            A_FILE };
    public static final long[] RANKS = new long[] { RANK_1, RANK_2, RANK_3, RANK_4, RANK_5, RANK_6, RANK_7, RANK_8 };
    public static final long[] RANKS_REVERSED = new long[] { RANK_8, RANK_7, RANK_6, RANK_5, RANK_4, RANK_3, RANK_2,
            RANK_1 };
    public static final char[][] PIECE_CHARS_UNICODE = new char[][] { { '♛', '♞', '♝', '♜', '♚', '♟', ' ' },
            { '♕', '♘', '♗', '♖', '♔', '♙', ' ' } }; // All NONE_PIECE squares will be white, ⚠ should never print
    public static final char[][] PIECE_CHARS_ASCII = new char[][] { { 'Q', 'N', 'B', 'R', 'K', 'P', ' ' },
            { 'q', 'n', 'b', 'r', 'k', 'p', ' ' } }; // All NONE_PIECE squares will be white, ⚠ should never print
    public static final int[] COLORS = new int[] { WHITE, BLACK };
    public static final int[] COLORS_REVERSED = new int[] { BLACK, WHITE };
    public static final int[] SIDES = new int[] { QUEENSIDE, KINGSIDE };
    public static final int[] SIDES_REVERSED = new int[] { KINGSIDE, QUEENSIDE };
    public static final long[][] DEFAULT_PIECEBOARDS = new long[][] {
            { 8l, 66l, 36l, 129l, 16l, 65280l },
            { 576460752303423488l, 4755801206503243776l, 2594073385365405696l,
                    -9151314442816847872l, 1152921504606846976l,
                    71776119061217280l }
    };
    public static final long[] ROOK_STARTING_FILES = new long[] { A_FILE, H_FILE };

    public static final long[] KNIGHT_MOVE_SQUARES = { 0x00020400L, 0x00050800L, 0x000A1100L, 0x00142200L, 0x00284400L,
            0x00508800L, 0x00A01000L, 0x00402000L, 0x02040004L, 0x05080008L, 0x0A110011L, 0x14220022L, 0x28440044L,
            0x50880088L, 0xA0100010L, 0x40200020L, 0x204000402L, 0x508000805L, 0xA1100110AL, 0x1422002214L,
            0x2844004428L, 0x5088008850L, 0xA0100010A0L, 0x4020002040L, 0x20400040200L, 0x50800080500L, 0xA1100110A00L,
            0x142200221400L, 0x284400442800L, 0x508800885000L, 0xA0100010A000L, 0x402000204000L, 0x2040004020000L,
            0x5080008050000L, 0xA1100110A0000L, 0x14220022140000L, 0x28440044280000L, 0x50880088500000L,
            0xA0100010A00000L, 0x40200020400000L, 0x204000402000000L, 0x508000805000000L, 0xA1100110A000000L,
            0x1422002214000000L, 0x2844004428000000L, 0x5088008850000000L, 0xA0100010A0000000L, 0x4020002040000000L,
            0x400040200000000L, 0x800080500000000L, 0x1100110A00000000L, 0x2200221400000000L, 0x4400442800000000L,
            0x8800885000000000L, 0x100010A000000000L, 0x2000204000000000L, 0x4020000000000L, 0x8050000000000L,
            0x110A0000000000L, 0x22140000000000L, 0x44280000000000L, 0x88500000000000L, 0x10A00000000000L,
            0x20400000000000L };

    public static final long[] KING_MOVE_SQUARES = { 0x00000302L, 0x00000705L, 0x00000E0AL, 0x00001C14L, 0x00003828L,
            0x00007050L,
            0x0000E0A0L, 0x0000C040L, 0x00030203L, 0x00070507L, 0x000E0A0EL, 0x001C141CL, 0x00382838L, 0x00705070L,
            0x00E0A0E0L, 0x00C040C0L, 0x03020300L, 0x07050700L, 0x0E0A0E00L, 0x1C141C00L, 0x38283800L, 0x70507000L,
            0xE0A0E000L, 0xC040C000L, 0x302030000L, 0x705070000L, 0xE0A0E0000L, 0x1C141C0000L, 0x3828380000L,
            0x7050700000L, 0xE0A0E00000L, 0xC040C00000L, 0x30203000000L, 0x70507000000L, 0xE0A0E000000L,
            0x1C141C000000L, 0x382838000000L, 0x705070000000L, 0xE0A0E0000000L, 0xC040C0000000L, 0x3020300000000L,
            0x7050700000000L, 0xE0A0E00000000L, 0x1C141C00000000L, 0x38283800000000L, 0x70507000000000L,
            0xE0A0E000000000L, 0xC040C000000000L, 0x302030000000000L, 0x705070000000000L, 0xE0A0E0000000000L,
            0x1C141C0000000000L, 0x3828380000000000L, 0x7050700000000000L, 0xE0A0E00000000000L, 0xC040C00000000000L,
            0x203000000000000L, 0x507000000000000L, 0xA0E000000000000L, 0x141C000000000000L, 0x2838000000000000L,
            0x5070000000000000L, 0xA0E0000000000000L, 0x40C0000000000000L };

    public static final long[] VERTICAL_OFFSETS = { 0l, -72057594037927936l, -281474976710656l, -1099511627776l,
            -4294967296l, -16777216l, -65536l, -256l, -1l, 72057594037927935l, 281474976710655l, 1099511627775l,
            4294967295l, 16777215l, 65535l, 255l, 0l };
    public static final long[] HORIZONTAL_OFFSETS = { 0l, -9187201950435737472l, -4557430888798830400l,
            -2242545357980376864l, -1085102592571150096l, -506381209866536712l, -217020518514230020l,
            -72340172838076674l, -1l, 9187201950435737471l, 4557430888798830399l, 2242545357980376863l,
            1085102592571150095l, 506381209866536711l, 217020518514230019l, 72340172838076673l, 0l };
    public static final byte[][] NORTH_EAST_RAYS = { { 9, 18, 27, 36, 45, 54, 63 }, { 10, 19, 28, 37, 46, 55 },
            { 11, 20, 29, 38, 47 }, { 12, 21, 30, 39 }, { 13, 22, 31 }, { 14, 23 }, { 15 }, {},
            { 17, 26, 35, 44, 53, 62 }, { 18, 27, 36, 45, 54, 63 }, { 19, 28, 37, 46, 55 }, { 20, 29, 38, 47 },
            { 21, 30, 39 }, { 22, 31 }, { 23 }, {}, { 25, 34, 43, 52, 61 }, { 26, 35, 44, 53, 62 },
            { 27, 36, 45, 54, 63 }, { 28, 37, 46, 55 }, { 29, 38, 47 }, { 30, 39 }, { 31 }, {}, { 33, 42, 51, 60 },
            { 34, 43, 52, 61 }, { 35, 44, 53, 62 }, { 36, 45, 54, 63 }, { 37, 46, 55 }, { 38, 47 }, { 39 }, {},
            { 41, 50, 59 }, { 42, 51, 60 }, { 43, 52, 61 }, { 44, 53, 62 }, { 45, 54, 63 }, { 46, 55 }, { 47 }, {},
            { 49, 58 }, { 50, 59 }, { 51, 60 }, { 52, 61 }, { 53, 62 }, { 54, 63 }, { 55 }, {}, { 57 }, { 58 }, { 59 },
            { 60 }, { 61 }, { 62 }, { 63 }, {}, {}, {}, {}, {}, {}, {}, {}, {} };
    public static final byte[][] NORTH_WEST_RAYS = { {}, { 8 }, { 9, 16 }, { 10, 17, 24 }, { 11, 18, 25, 32 },
            { 12, 19, 26, 33, 40 }, { 13, 20, 27, 34, 41, 48 }, { 14, 21, 28, 35, 42, 49, 56 }, {}, { 16 }, { 17, 24 },
            { 18, 25, 32 }, { 19, 26, 33, 40 }, { 20, 27, 34, 41, 48 }, { 21, 28, 35, 42, 49, 56 },
            { 22, 29, 36, 43, 50, 57 }, {}, { 24 }, { 25, 32 }, { 26, 33, 40 }, { 27, 34, 41, 48 },
            { 28, 35, 42, 49, 56 }, { 29, 36, 43, 50, 57 }, { 30, 37, 44, 51, 58 }, {}, { 32 }, { 33, 40 },
            { 34, 41, 48 }, { 35, 42, 49, 56 }, { 36, 43, 50, 57 }, { 37, 44, 51, 58 }, { 38, 45, 52, 59 }, {}, { 40 },
            { 41, 48 }, { 42, 49, 56 }, { 43, 50, 57 }, { 44, 51, 58 }, { 45, 52, 59 }, { 46, 53, 60 }, {}, { 48 },
            { 49, 56 }, { 50, 57 }, { 51, 58 }, { 52, 59 }, { 53, 60 }, { 54, 61 }, {}, { 56 }, { 57 }, { 58 }, { 59 },
            { 60 }, { 61 }, { 62 }, {}, {}, {}, {}, {}, {}, {}, {} };
    public static final byte[][] SOUTH_EAST_RAYS = { {}, {}, {}, {}, {}, {}, {}, {}, { 1 }, { 2 }, { 3 }, { 4 }, { 5 },
            { 6 }, { 7 }, {}, { 9, 2 }, { 10, 3 }, { 11, 4 }, { 12, 5 }, { 13, 6 }, { 14, 7 }, { 15 }, {},
            { 17, 10, 3 }, { 18, 11, 4 }, { 19, 12, 5 }, { 20, 13, 6 }, { 21, 14, 7 }, { 22, 15 }, { 23 }, {},
            { 25, 18, 11, 4 }, { 26, 19, 12, 5 }, { 27, 20, 13, 6 }, { 28, 21, 14, 7 }, { 29, 22, 15 }, { 30, 23 },
            { 31 }, {}, { 33, 26, 19, 12, 5 }, { 34, 27, 20, 13, 6 }, { 35, 28, 21, 14, 7 }, { 36, 29, 22, 15 },
            { 37, 30, 23 }, { 38, 31 }, { 39 }, {}, { 41, 34, 27, 20, 13, 6 }, { 42, 35, 28, 21, 14, 7 },
            { 43, 36, 29, 22, 15 }, { 44, 37, 30, 23 }, { 45, 38, 31 }, { 46, 39 }, { 47 }, {},
            { 49, 42, 35, 28, 21, 14, 7 }, { 50, 43, 36, 29, 22, 15 }, { 51, 44, 37, 30, 23 }, { 52, 45, 38, 31 },
            { 53, 46, 39 }, { 54, 47 }, { 55 }, {} };
    public static final byte[][] SOUTH_WEST_RAYS = { {}, {}, {}, {}, {}, {}, {}, {}, {}, { 0 }, { 1 }, { 2 }, { 3 },
            { 4 }, { 5 }, { 6 }, {}, { 8 }, { 9, 0 }, { 10, 1 }, { 11, 2 }, { 12, 3 }, { 13, 4 }, { 14, 5 }, {}, { 16 },
            { 17, 8 }, { 18, 9, 0 }, { 19, 10, 1 }, { 20, 11, 2 }, { 21, 12, 3 }, { 22, 13, 4 }, {}, { 24 }, { 25, 16 },
            { 26, 17, 8 }, { 27, 18, 9, 0 }, { 28, 19, 10, 1 }, { 29, 20, 11, 2 }, { 30, 21, 12, 3 }, {}, { 32 },
            { 33, 24 }, { 34, 25, 16 }, { 35, 26, 17, 8 }, { 36, 27, 18, 9, 0 }, { 37, 28, 19, 10, 1 },
            { 38, 29, 20, 11, 2 }, {}, { 40 }, { 41, 32 }, { 42, 33, 24 }, { 43, 34, 25, 16 }, { 44, 35, 26, 17, 8 },
            { 45, 36, 27, 18, 9, 0 }, { 46, 37, 28, 19, 10, 1 }, {}, { 48 }, { 49, 40 }, { 50, 41, 32 },
            { 51, 42, 33, 24 }, { 52, 43, 34, 25, 16 }, { 53, 44, 35, 26, 17, 8 }, { 54, 45, 36, 27, 18, 9, 0 } };
    public static final byte[][] NORTH_RAYS = { { 8, 16, 24, 32, 40, 48, 56 }, { 9, 17, 25, 33, 41, 49, 57 },
            { 10, 18, 26, 34, 42, 50, 58 }, { 11, 19, 27, 35, 43, 51, 59 }, { 12, 20, 28, 36, 44, 52, 60 },
            { 13, 21, 29, 37, 45, 53, 61 }, { 14, 22, 30, 38, 46, 54, 62 }, { 15, 23, 31, 39, 47, 55, 63 },
            { 16, 24, 32, 40, 48, 56 }, { 17, 25, 33, 41, 49, 57 }, { 18, 26, 34, 42, 50, 58 },
            { 19, 27, 35, 43, 51, 59 }, { 20, 28, 36, 44, 52, 60 }, { 21, 29, 37, 45, 53, 61 },
            { 22, 30, 38, 46, 54, 62 }, { 23, 31, 39, 47, 55, 63 }, { 24, 32, 40, 48, 56 }, { 25, 33, 41, 49, 57 },
            { 26, 34, 42, 50, 58 }, { 27, 35, 43, 51, 59 }, { 28, 36, 44, 52, 60 }, { 29, 37, 45, 53, 61 },
            { 30, 38, 46, 54, 62 }, { 31, 39, 47, 55, 63 }, { 32, 40, 48, 56 }, { 33, 41, 49, 57 }, { 34, 42, 50, 58 },
            { 35, 43, 51, 59 }, { 36, 44, 52, 60 }, { 37, 45, 53, 61 }, { 38, 46, 54, 62 }, { 39, 47, 55, 63 },
            { 40, 48, 56 }, { 41, 49, 57 }, { 42, 50, 58 }, { 43, 51, 59 }, { 44, 52, 60 }, { 45, 53, 61 },
            { 46, 54, 62 }, { 47, 55, 63 }, { 48, 56 }, { 49, 57 }, { 50, 58 }, { 51, 59 }, { 52, 60 }, { 53, 61 },
            { 54, 62 }, { 55, 63 }, { 56 }, { 57 }, { 58 }, { 59 }, { 60 }, { 61 }, { 62 }, { 63 }, {}, {}, {}, {}, {},
            {}, {}, {} };
    public static final byte[][] WEST_RAYS = { {}, { 0 }, { 1, 0 }, { 2, 1, 0 }, { 3, 2, 1, 0 }, { 4, 3, 2, 1, 0 },
            { 5, 4, 3, 2, 1, 0 }, { 6, 5, 4, 3, 2, 1, 0 }, {}, { 8 }, { 9, 8 }, { 10, 9, 8 }, { 11, 10, 9, 8 },
            { 12, 11, 10, 9, 8 }, { 13, 12, 11, 10, 9, 8 }, { 14, 13, 12, 11, 10, 9, 8 }, {}, { 16 }, { 17, 16 },
            { 18, 17, 16 }, { 19, 18, 17, 16 }, { 20, 19, 18, 17, 16 }, { 21, 20, 19, 18, 17, 16 },
            { 22, 21, 20, 19, 18, 17, 16 }, {}, { 24 }, { 25, 24 }, { 26, 25, 24 }, { 27, 26, 25, 24 },
            { 28, 27, 26, 25, 24 }, { 29, 28, 27, 26, 25, 24 }, { 30, 29, 28, 27, 26, 25, 24 }, {}, { 32 }, { 33, 32 },
            { 34, 33, 32 }, { 35, 34, 33, 32 }, { 36, 35, 34, 33, 32 }, { 37, 36, 35, 34, 33, 32 },
            { 38, 37, 36, 35, 34, 33, 32 }, {}, { 40 }, { 41, 40 }, { 42, 41, 40 }, { 43, 42, 41, 40 },
            { 44, 43, 42, 41, 40 }, { 45, 44, 43, 42, 41, 40 }, { 46, 45, 44, 43, 42, 41, 40 }, {}, { 48 }, { 49, 48 },
            { 50, 49, 48 }, { 51, 50, 49, 48 }, { 52, 51, 50, 49, 48 }, { 53, 52, 51, 50, 49, 48 },
            { 54, 53, 52, 51, 50, 49, 48 }, {}, { 56 }, { 57, 56 }, { 58, 57, 56 }, { 59, 58, 57, 56 },
            { 60, 59, 58, 57, 56 }, { 61, 60, 59, 58, 57, 56 }, { 62, 61, 60, 59, 58, 57, 56 } };
    public static final byte[][] EAST_RAYS = { { 1, 2, 3, 4, 5, 6, 7 }, { 2, 3, 4, 5, 6, 7 }, { 3, 4, 5, 6, 7 },
            { 4, 5, 6, 7 }, { 5, 6, 7 }, { 6, 7 }, { 7 }, {}, { 9, 10, 11, 12, 13, 14, 15 }, { 10, 11, 12, 13, 14, 15 },
            { 11, 12, 13, 14, 15 }, { 12, 13, 14, 15 }, { 13, 14, 15 }, { 14, 15 }, { 15 }, {},
            { 17, 18, 19, 20, 21, 22, 23 }, { 18, 19, 20, 21, 22, 23 }, { 19, 20, 21, 22, 23 }, { 20, 21, 22, 23 },
            { 21, 22, 23 }, { 22, 23 }, { 23 }, {}, { 25, 26, 27, 28, 29, 30, 31 }, { 26, 27, 28, 29, 30, 31 },
            { 27, 28, 29, 30, 31 }, { 28, 29, 30, 31 }, { 29, 30, 31 }, { 30, 31 }, { 31 }, {},
            { 33, 34, 35, 36, 37, 38, 39 }, { 34, 35, 36, 37, 38, 39 }, { 35, 36, 37, 38, 39 }, { 36, 37, 38, 39 },
            { 37, 38, 39 }, { 38, 39 }, { 39 }, {}, { 41, 42, 43, 44, 45, 46, 47 }, { 42, 43, 44, 45, 46, 47 },
            { 43, 44, 45, 46, 47 }, { 44, 45, 46, 47 }, { 45, 46, 47 }, { 46, 47 }, { 47 }, {},
            { 49, 50, 51, 52, 53, 54, 55 }, { 50, 51, 52, 53, 54, 55 }, { 51, 52, 53, 54, 55 }, { 52, 53, 54, 55 },
            { 53, 54, 55 }, { 54, 55 }, { 55 }, {}, { 57, 58, 59, 60, 61, 62, 63 }, { 58, 59, 60, 61, 62, 63 },
            { 59, 60, 61, 62, 63 }, { 60, 61, 62, 63 }, { 61, 62, 63 }, { 62, 63 }, { 63 }, {} };
    public static final byte[][] SOUTH_RAYS = { {}, {}, {}, {}, {}, {}, {}, {}, { 0 }, { 1 }, { 2 }, { 3 }, { 4 },
            { 5 }, { 6 }, { 7 }, { 8, 0 }, { 9, 1 }, { 10, 2 }, { 11, 3 }, { 12, 4 }, { 13, 5 }, { 14, 6 }, { 15, 7 },
            { 16, 8, 0 }, { 17, 9, 1 }, { 18, 10, 2 }, { 19, 11, 3 }, { 20, 12, 4 }, { 21, 13, 5 }, { 22, 14, 6 },
            { 23, 15, 7 }, { 24, 16, 8, 0 }, { 25, 17, 9, 1 }, { 26, 18, 10, 2 }, { 27, 19, 11, 3 }, { 28, 20, 12, 4 },
            { 29, 21, 13, 5 }, { 30, 22, 14, 6 }, { 31, 23, 15, 7 }, { 32, 24, 16, 8, 0 }, { 33, 25, 17, 9, 1 },
            { 34, 26, 18, 10, 2 }, { 35, 27, 19, 11, 3 }, { 36, 28, 20, 12, 4 }, { 37, 29, 21, 13, 5 },
            { 38, 30, 22, 14, 6 }, { 39, 31, 23, 15, 7 }, { 40, 32, 24, 16, 8, 0 }, { 41, 33, 25, 17, 9, 1 },
            { 42, 34, 26, 18, 10, 2 }, { 43, 35, 27, 19, 11, 3 }, { 44, 36, 28, 20, 12, 4 }, { 45, 37, 29, 21, 13, 5 },
            { 46, 38, 30, 22, 14, 6 }, { 47, 39, 31, 23, 15, 7 }, { 48, 40, 32, 24, 16, 8, 0 },
            { 49, 41, 33, 25, 17, 9, 1 }, { 50, 42, 34, 26, 18, 10, 2 }, { 51, 43, 35, 27, 19, 11, 3 },
            { 52, 44, 36, 28, 20, 12, 4 }, { 53, 45, 37, 29, 21, 13, 5 }, { 54, 46, 38, 30, 22, 14, 6 },
            { 55, 47, 39, 31, 23, 15, 7 } };

    public static final byte[][][] DIAGONAL_RAYS = { NORTH_EAST_RAYS, NORTH_WEST_RAYS, SOUTH_EAST_RAYS,
            SOUTH_WEST_RAYS };
    public static final byte[][][] ORTHOGONAL_RAYS = { NORTH_RAYS, SOUTH_RAYS, EAST_RAYS, WEST_RAYS };

    public static final int HASH_SIZE = 71;
}