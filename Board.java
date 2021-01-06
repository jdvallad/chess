import java.util.*;
import java.math.*;
import java.io.Serializable;

@SuppressWarnings("SpellCheckingInspection")
public class Board implements Serializable{
    public double[][] pawnTable,rookTable,knightTable,bishopTable,queenTable,kingMiddleTable,kingEndTable;
    final  String[] PIECES = {"rook", "knight", "bishop", "queen", "king", "bishop", "knight", "rook"};
    public int repeatedPosition,gameResult,movesSinceLastCapture;
    public int[] lastMove;
    public final int[] lastLastMove;
    final HashSet<String> legalMoves = new HashSet<>();
    public final Piece[][] pieces;
    public boolean turn;
    public int turnCount=1;
    public Board(String str){
        if(str.length()==0){
            initializePieceSquareTables();
            lastLastMove = new int[4];
            pieces = new Piece[8][8];
            movesSinceLastCapture=0;
            lastMove=new int[4];
            repeatedPosition=1;
            initializeBoard();
            gameResult=-1;
            turn=true;
            lastMove[0]=lastMove[1]=lastMove[2]=lastMove[3]=-1;
            lastLastMove[0]=lastLastMove[1]=lastLastMove[2]=lastLastMove[3]=-1;
        }else{
            gameResult=-1;
            initializePieceSquareTables();
            lastLastMove = new int[4];
            pieces = new Piece[8][8];
            movesSinceLastCapture=0;
            lastMove=new int[4];
            repeatedPosition=1;
            lastMove[0]=lastMove[1]=lastMove[2]=lastMove[3]=-1;
            lastLastMove[0]=lastLastMove[1]=lastLastMove[2]=lastLastMove[3]=-1;

            String bits = new BigInteger(1, Base64.getDecoder().decode(str)).toString(2);
            int i=1;
            int bit = bits.charAt(i)=='1'?1:0;   
            turn=(bit == 1);
            i++;
            for(int r=0;r<8;r++){
                for(int c=0;c<8;c++){
                    String tempo=""+bits.charAt(i)+bits.charAt(i+1)+bits.charAt(i+2)+bits.charAt(i+3);
                    i+=4;
                    int temp=Integer.parseInt(tempo,2);
                    String name="";
                    int team = temp<9?0:1;
                    if(temp==0){
                        team=-1;
                    }
                    switch (temp % 8) {
                        case 0 -> name = "square";
                        case 1 -> name = "pawn";
                        case 2 -> name = "knight";
                        case 3 -> name = "bishop";
                        case 4 -> name = "rook";
                        case 5 -> name = "queen";
                        case 6 -> name = "king";
                    }
                    pieces[r][c]=new Piece(name,team,r,c);
                }
            }
            for(int r=0;r<8;r++){
                bit = bits.charAt(i)=='1'?1:0; 
                i++;
                if(bit==1){
                    pieces[r][4].hasMoved=1;
                }

            }
            for(int r=0;r<8;r++){
                bit = bits.charAt(i)=='1'?1:0; 
                i++;
                if(bit==1){
                    pieces[r][5].hasMoved=1;
                }
            }
            bit = bits.charAt(i)=='1'?1:0; 
            i++;
            if(bit==1){
                pieces[0][7].hasMoved=2;
            }
            bit = bits.charAt(i)=='1'?1:0; 
            i++;
            if(bit==1){
                pieces[7][7].hasMoved=2;
            }
            bit = bits.charAt(i)=='1'?1:0; 
            i++;
            if(bit==1){
                pieces[0][0].hasMoved=2;
            }
            bit = bits.charAt(i)=='1'?1:0;
            if(bit==1){
                pieces[7][0].hasMoved=2;
            }
            evaluate();
        }
    }

    public String get960(){
        Board temp = new Board("MJAAAQCQAAEAkAABAJAAAQCQAAEAkAABAJAAAQCQAAEAAAA=");
        var nums = new ArrayList<String>();
        for(int i=0;i<8;i++){
            nums.add(""+i);   
        }
        Collections.shuffle(nums);
        int lB = 2*((int)(4.*Math.random()));
        int dB = 2*((int)(4.*Math.random()))+1;
        nums.remove("" + lB);
        nums.remove("" + dB);
        int q = Integer.parseInt(nums.remove(0));
        int n = Integer.parseInt(nums.remove(0));
        temp.pieces[lB][0]=new Piece("bishop",1,lB,0,2);
        temp.pieces[lB][7]=new Piece("bishop",0,lB,7,2);
        temp.pieces[dB][0]=new Piece("bishop",1,dB,0,2);
        temp.pieces[dB][7]=new Piece("bishop",0,dB,7,2);
        temp.pieces[q][0]=new Piece("queen",1,q,0,2);
        temp.pieces[q][7]=new Piece("queen",0,q,7,2);
        temp.pieces[n][0]=new Piece("knight",1,n,0,2);
        temp.pieces[n][7]=new Piece("knight",0,n,7,2);
        n = Integer.parseInt(nums.remove(0));
        temp.pieces[n][0]=new Piece("knight",1,n,0,2);
        temp.pieces[n][7]=new Piece("knight",0,n,7,2);
        Collections.sort(nums);
        int t = Integer.parseInt(nums.remove(0));
        temp.pieces[t][0]=new Piece("rook",1,t,0,2);
        temp.pieces[t][7]=new Piece("rook",0,t,7,2);
        t = Integer.parseInt(nums.remove(0));
        temp.pieces[t][0]=new Piece("king",1,t,0,2);
        temp.pieces[t][7]=new Piece("king",0,t,7,2);
        t = Integer.parseInt(nums.remove(0));
        temp.pieces[t][0]=new Piece("rook",1,t,0,2);
        temp.pieces[t][7]=new Piece("rook",0,t,7,2);
        return temp.hash();
    }

    public Board(Board temp) {
        lastMove=new int[4];
        lastLastMove = new int[4];
        initializePieceSquareTables();
        movesSinceLastCapture=temp.movesSinceLastCapture;
        turn=temp.turn;
        repeatedPosition=temp.repeatedPosition;
        gameResult=temp.gameResult;
        pieces = new Piece[8][8];
        for (int r=0; r<8; r++) {
            for (int c=0; c<8; c++) {
                pieces[r][c]=new Piece(temp.pieces[r][c].name, temp.pieces[r][c].team, temp.pieces[r][c].rank, temp.pieces[r][c].file, temp.pieces[r][c].hasMoved);
            }
        }
        int[] in = temp.lastMove;
        for(int i=0;i<4;i++){
            lastMove[i]=in[i];
            lastLastMove[i]= temp.lastLastMove[0];
        }
        turnCount=temp.turnCount;
    }

    public String hash(){
        StringBuilder str= new StringBuilder("1" + (turn ? "1" : "0"));
        for(int r=0;r<8;r++){
            for(int c=0;c<8;c++){
                if(pieces[r][c].isSquare()){
                    str.append("0000");
                }
                else
                {
                    if(pieces[r][c].isWhite()){
                        switch (pieces[r][c].name) {
                            case "pawn" -> str.append("0001");
                            case "knight" -> str.append("0010");
                            case "bishop" -> str.append("0011");
                            case "rook" -> str.append("0100");
                            case "queen" -> str.append("0101");
                            case "king" -> str.append("0110");
                        }
                    }
                    else
                    {
                        switch (pieces[r][c].name) {
                            case "pawn" -> str.append("1001");
                            case "knight" -> str.append("1010");
                            case "bishop" -> str.append("1011");
                            case "rook" -> str.append("1100");
                            case "queen" -> str.append("1101");
                            case "king" -> str.append("1110");
                        }
                    }
                }
            }
        }
        Piece temp;
        for(int r=0;r<8;r++){
            temp = pieces[r][3];
            if(temp.isPawn()&&temp.hasMoved==1){
                str.append("1");
            }
            else
            {
                str.append("0");
            }
        }
        for(int r=0;r<8;r++){
            temp = pieces[r][4];
            if(temp.isPawn()&&temp.hasMoved==1){
                str.append("1");
            }
            else
            {
                str.append("0");
            }
        }
        temp=pieces[4][7];
        if(temp.isKing()&&temp.hasMoved==2){
            if(pieces[0][7].isRook()&&pieces[0][7].hasMoved==2){
                str.append("1");
            }
            else
            {
                str.append("0");
            }
            if(pieces[7][7].isRook()&&pieces[7][7].hasMoved==2){
                str.append("1");
            }
            else
            {
                str.append("0");
            }
        }
        else
        {
            str.append("00");
        }
        temp=pieces[4][0];
        if(temp.isKing()&&temp.hasMoved==2){
            if(pieces[0][0].isRook()&&pieces[0][0].hasMoved==2){
                str.append("1");
            }
            else
            {
                str.append("0");
            }
            if(pieces[7][0].isRook()&&pieces[7][0].hasMoved==2){
                str.append("1");
            }
            else
            {
                str.append("0");
            }
        }
        else
        {
            str.append("00");
        }
        return Base64.getEncoder().encodeToString( new BigInteger(str.toString(), 2).toByteArray());
    }

    void initializeBoard(){
        for (int r=0; r<8; r++) {
            pieces[r][0]=new Piece(PIECES[r], 1, r, 0, 2); 
            pieces[r][1]=new Piece("pawn", 1, r, 1, 2); 
            pieces[r][2]=new Piece("square",-1, r, 2, 2); 
            pieces[r][3]=new Piece("square", -1, r, 3, 2); 
            pieces[r][4]=new Piece("square", -1, r, 4, 2); 
            pieces[r][5]=new Piece("square", -1, r, 5, 2); 
            pieces[r][6]=new Piece("pawn", 0, r, 6, 2); 
            pieces[r][7]=new Piece(PIECES[r], 0, r, 7, 2);
        } 
    }

    public void initializePieceSquareTables(){
        pawnTable=new double[8][];
        pawnTable[0]=new double[]{0,  0,  0,  0,  0,  0,  0,  0};
        pawnTable[1]=new double[]{50, 50, 50, 50, 50, 50, 50, 50};
        pawnTable[2]=new double[]{10, 10, 20, 30, 30, 20, 10, 10};
        pawnTable[3]=new double[]{5,  5, 10, 25, 25, 10,  5,  5};
        pawnTable[4]=new double[]{0,  0,  0, 20, 20,  0,  0,  0};
        pawnTable[5]=new double[]{5, -5,-10,  0,  0,-10, -5,  5};
        pawnTable[6]=new double[]{5, 10, 10,-20,-20, 10, 10,  5};
        pawnTable[7]=new double[]{0,  0,  0,  0,  0,  0,  0,  0};

        knightTable=new double[8][];
        knightTable[0]=new double[]{-50,-40,-30,-30,-30,-30,-40,-50};
        knightTable[1]=new double[]{-40,-20,  0,  0,  0,  0,-20,-40};
        knightTable[2]=new double[]{-30,  0, 10, 15, 15, 10,  0,-30};
        knightTable[3]=new double[]{-30,  5, 15, 20, 20, 15,  5,-30};
        knightTable[4]=new double[]{-30,  0, 15, 20, 20, 15,  0,-30};
        knightTable[5]=new double[]{-30,  5, 10, 15, 15, 10,  5,-30};
        knightTable[6]=new double[]{-40,-20,  0,  5,  5,  0,-20,-40};
        knightTable[7]=new double[]{-50,-40,-30,-30,-30,-30,-40,-50};

        bishopTable=new double[8][];
        bishopTable[0]=new double[]{-20,-10,-10,-10,-10,-10,-10,-20};
        bishopTable[1]=new double[]{-10,  0,  0,  0,  0,  0,  0,-10};
        bishopTable[2]=new double[]{-10,  0,  5, 10, 10,  5,  0,-10};
        bishopTable[3]=new double[]{-10,  5,  5, 10, 10,  5,  5,-10};
        bishopTable[4]=new double[]{-10,  0, 10, 10, 10, 10,  0,-10};
        bishopTable[5]=new double[]{-10, 10, 10, 10, 10, 10, 10,-10};
        bishopTable[6]=new double[]{-10,  5,  0,  0,  0,  0,  5,-10};
        bishopTable[7]=new double[]{-20,-10,-10,-10,-10,-10,-10,-20};

        rookTable=new double[8][];
        rookTable[0]=new double[]{0,  0,  0,  0,  0,  0,  0,  0};
        rookTable[1]=new double[]{5, 10, 10, 10, 10, 10, 10,  5};
        rookTable[2]=new double[]{-5,  0,  0,  0,  0,  0,  0, -5};
        rookTable[3]=new double[]{-5,  0,  0,  0,  0,  0,  0, -5};
        rookTable[4]=new double[]{-5,  0,  0,  0,  0,  0,  0, -5};
        rookTable[5]=new double[]{ -5,  0,  0,  0,  0,  0,  0, -5};
        rookTable[6]=new double[]{ -5,  0,  0,  0,  0,  0,  0, -5};
        rookTable[7]=new double[]{  0,  0,  0,  5,  5,  0,  0,  0};

        queenTable=new double[8][];
        queenTable[0]=new double[]{-20,-10,-10, -5, -5,-10,-10,-20};
        queenTable[1]=new double[]{-10,  0,  0,  0,  0,  0,  0,-10};
        queenTable[2]=new double[]{-10,  0,  5,  5,  5,  5,  0,-10};
        queenTable[3]=new double[]{ -5,  0,  5,  5,  5,  5,  0, -5};
        queenTable[4]=new double[]{ 0,  0,  5,  5,  5,  5,  0, -5};
        queenTable[5]=new double[]{-10,  5,  5,  5,  5,  5,  0,-10};
        queenTable[6]=new double[]{-10,  0,  5,  0,  0,  0,  0,-10};
        queenTable[7]=new double[]{-20,-10,-10, -5, -5,-10,-10,-20};

        kingMiddleTable=new double[8][];
        kingMiddleTable[0]=new double[]{-30,-40,-40,-50,-50,-40,-40,-30};
        kingMiddleTable[1]=new double[]{-30,-40,-40,-50,-50,-40,-40,-30};
        kingMiddleTable[2]=new double[]{-30,-40,-40,-50,-50,-40,-40,-30};
        kingMiddleTable[3]=new double[]{-30,-40,-40,-50,-50,-40,-40,-30};
        kingMiddleTable[4]=new double[]{-20,-30,-30,-40,-40,-30,-30,-20};
        kingMiddleTable[5]=new double[]{-10,-20,-20,-20,-20,-20,-20,-10};
        kingMiddleTable[6]=new double[]{20, 20,  0,  0,  0,  0, 20, 20};
        kingMiddleTable[7]=new double[]{20, 30, 10,  0,  0, 10, 30, 20};

        kingEndTable=new double[8][];
        kingEndTable[0]=new double[]{-50,-40,-30,-20,-20,-30,-40,-50};
        kingEndTable[1]=new double[]{-30,-20,-10,  0,  0,-10,-20,-30};
        kingEndTable[2]=new double[]{-30,-10, 20, 30, 30, 20,-10,-30};
        kingEndTable[3]=new double[]{-30,-10, 30, 40, 40, 30,-10,-30};
        kingEndTable[4]=new double[]{-30,-10, 30, 40, 40, 30,-10,-30};
        kingEndTable[5]=new double[]{-30,-10, 20, 30, 30, 20,-10,-30};
        kingEndTable[6]=new double[]{-30,-30,  0,  0,  0,  0,-30,-30};
        kingEndTable[7]=new double[]{-50,-30,-30,-30,-30,-30,-30,-50};
    }

    public void updateLegalMoves(){
        legalMoves.clear();
        for (int r=0; r<8; r++) {
            for (int c=0; c<8; c++) {
                if ((turn&&pieces[r][c].isWhite())||(!turn&&pieces[r][c].isBlack())) {
                    for (int i=0; i<8; i++) {
                        for (int j=0; j<8; j++) {
                            int[] temp = new int[]{r,c,i,j};
                            if (isLegal(temp)&&!nextBoard(temp).futureInCheck()) {
                                String s=""+r+" "+c+" "+i+" "+j+" "+pieces[r][c].name+" "+pieces[i][j].name;
                                legalMoves.add(s);
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean ambiguousMoveName(int[] move){
        boolean res=true;
        boolean doubleCounted=false;
        updateLegalMoves();
        if(pieces[move[0]][move[1]].isPawn()){
            return false;
        }
        for(String str:legalMoves){
            String name = pieces[move[0]][move[1]].name;
            if(str.contains(name)&&(move[3]==Integer.parseInt(""+str.charAt(4)))&&(move[2]==Integer.parseInt(""+str.charAt(6)))){
                if(!doubleCounted){
                    res=false;
                }
                doubleCounted=true;
            }
            else
            {
                res=false;
            }
            if(res){
                return true;
            }
            res=true;
        }
        return false;
    }

    public boolean endGame(){
        boolean whiteQueen=false;
        boolean blackQueen=false;
        int whitePieces=0;
        int whiteMinorPieces=0;
        int blackPieces=0;
        int blackMinorPieces=0;
        for(int r=0;r<8;r++){
            for(int c=0;c<8;c++){
                Piece temp = pieces[r][c];
                if(temp.isWhite()){
                    if(temp.isQueen()){
                        whiteQueen=true;
                    }
                    if((!temp.isKing()&&(!temp.isPawn()))){
                        whitePieces++;
                        if(temp.isBishop()||temp.isKnight()){
                            whiteMinorPieces++;
                        }
                    }
                }
                if(temp.isBlack()){
                    if(temp.isQueen()){
                        blackQueen=true;
                    }
                    if((!temp.isKing()&&(!temp.isPawn()))){
                        blackPieces++;
                        if(temp.isBishop()||temp.isKnight()){
                            blackMinorPieces++;
                        }
                    }
                }
            }
        }
        if((!whiteQueen)&&(!blackQueen)){
            return false;
        }
        if(whiteQueen){
            if(!blackQueen){
                return (whitePieces != 1) && (whitePieces != 2 || whiteMinorPieces != 1);
            }
            else
            {
                return ((whitePieces != 1) && (whitePieces != 2 || whiteMinorPieces != 1))
                        ||
                        ((blackPieces != 1) && (blackPieces != 2 || blackMinorPieces != 1));
            }
        }
        else
        {
            return ((blackPieces != 1) && (blackPieces != 2 || blackMinorPieces != 1));
        }
    }

    public char convertToRank(int s){
        return switch (s) {
            case 0 -> 'a';
            case 1 -> 'b';
            case 2 -> 'c';
            case 3 -> 'd';
            case 4 -> 'e';
            case 5 -> 'f';
            case 6 -> 'g';
            case 7 -> 'h';
            default -> ' ';
        };
    }

    public String getMoveName(int[] move){
        Board tempBoard=nextBoard(move);
        Piece tempPiece = pieces[move[0]][move[1]];
        boolean capture = !pieces[move[2]][move[3]].isSquare();
        boolean ambiguous=ambiguousMoveName(move);
        String res="";
        if(ambiguous){
            res+=convertToRank(move[0]);
            res+=(8-move[1]);
        }
        switch(tempPiece.name){
            case "square" : 
            break; 
            case "pawn" : 
            if(capture||enPassant(move)){
                res+=convertToRank(tempPiece.rank);
            }
            break; 
            case "rook" :
            res+="R";
            break; 
            case "knight" :
            res+="N";
            break; 
            case "bishop" :
            res+="B";
            break; 
            case "queen" :
            res+="Q";
            break; 
            case "king" :
            if(castled(move)){
                if(move[2]==2){
                    res+="O-O-O";
                }
                else
                {
                    res+="O-O";
                }
                if(tempBoard.inCheck()){
                    if(tempBoard.noMoves()){
                        res+="#";
                    }
                    else{
                        res+="+";
                    }
                }
                return res;
            }
            else{
                res+="K";
            }
            break; 
        }
        if(capture||enPassant(move)){
            res+="x";
        }
        res+= convertToRank(move[2]);
        res+=(8-move[3]);
        if(tempPiece.isPawn()&&move[3]==(7*tempPiece.team)){
            res+="=Q";  
        }
        if(tempBoard.inCheck()){
            if(tempBoard.noMoves()){
                res+="#";
            }
            else{
                res+="+";   
            }
        }
        return res;
    }

    public double evaluate() {
        double eval=0.;
        double absEval=0;
        double temp1;
        for (int r=0; r<pieces.length; r++) {
            for (int c=0; c<pieces[r].length; c++) {
                Piece temp = pieces[r][c];
                int i = temp.team == 0 ? c : 7 - c;
                switch(temp.name) {
                    case "square" : 
                    break; 
                    case "king" :
                    if(endGame()){
                        temp1=(100.+(kingMiddleTable[r][i]/100.));
                    }
                    else
                    {
                        temp1=(100.+(kingEndTable[r][i]/100.));
                    }
                        eval+=temp1*((temp.team==0) ? 1. : -1.);
                        absEval+=temp1;
                        break;
                    case "pawn" : 
                    temp1=(1.+(pawnTable[r][i]/100.));
                    eval+=temp1*((temp.team==0) ? 1. : -1.);
                    absEval+=temp1;
                    break; 
                    case "knight" : 
                    temp1=(3.2+(knightTable[r][i]/100.));
                    eval+=temp1*((temp.team==0) ? 1. : -1.);
                    absEval+=temp1;
                    break; 
                    case "bishop" : 
                    temp1=(3.3+(bishopTable[r][i]/100.));
                    eval+=temp1*((temp.team==0) ? 1. : -1.);
                    absEval+=temp1;
                    break; 
                    case "rook" : 
                    temp1=(5.+(rookTable[r][i]/100.));
                    eval+=temp1*((temp.team==0) ? 1. : -1.);
                    absEval+=temp1;
                    break; 
                    case "queen" : 
                    temp1=(9.+(queenTable[r][i]/100.));
                    eval+=temp1*((temp.team==0) ? 1. : -1.);
                    absEval+=temp1;
                    break;
                }
            }
        }
        if(eval>0){
            eval+=50./absEval;
        }
        if(eval<0)
        {
            eval-=50./absEval;
        }
        if(noMoves()){
            if(inCheck()){
                gameResult=0;
                return turn?-1000:1000;  
            }
            else
            {
                gameResult=1;
                return 0; 
            }
        }
        if(movesSinceLastCapture>=50){
            gameResult=2;
            return 0;  
        }
        if(repeatedPosition>=15){
            gameResult=3;
            return 0;  
        }
        return eval;
    }

    public boolean enPassant(int[] move){
        Piece p = pieces[move[0]][move[1]];
        int x = move[2];
        int y = move[3];
        return (y+(p.isWhite()?1:-1)<=7)
        &&
        (0<=y+(p.isWhite()?1:-1))
        &&
        (pieces[x][y].isSquare())
        &&
        ((y==5&&p.isBlack())||(y==2&&p.isWhite()))
        &&
        (pieces[x][y+(p.team==0?1:-1)].isPawn())
        &&
        (pieces[x][y+(p.team==0?1:-1)].hasMoved==1);

    }

    public boolean castled(int[] move){
        Piece p = pieces[move[0]][move[1]];
        int x = move[2];
        return p.isKing()&&(Math.abs(p.rank-x)==2);
    }

    public String castledStr(){
        String result="";
        Piece temp=pieces[4][7];
        if(temp.isKing()&&temp.hasMoved==2){
            if(pieces[7][7].isRook()&&pieces[7][7].hasMoved==2){
                result+="K";
            }
            if(pieces[0][7].isRook()&&pieces[0][7].hasMoved==2){
                result+="Q";
            }
        }
        temp=pieces[4][0];
        if(temp.isKing()&&temp.hasMoved==2){
            if(pieces[7][0].isRook()&&pieces[7][0].hasMoved==2){
                result+="k";
            }
            if(pieces[0][0].isRook()&&pieces[0][0].hasMoved==2){
                result+="q";
            }
        }
        if(result.equals("")){
            return "-";
        }
        return result;
    }

    public void developerUpdate(int[] move){
        int x=move[0];
        int y=move[1];
        int a=move[2];
        int b=move[3];
        int team;
        if(x<0){
            if(x==-3){
                team=1;   
            }else{
                team=0;   
            }
        }   
        else
        {
            team=pieces[x][y].team; 
        }
        if(a==-3&&x>=0){
            pieces[x][y]=new Piece("square",-1,a,b,2);
            return;
        }
        if(a<0){
            return;   
        }
        System.arraycopy(move, 0, lastMove, 0, 4);
        if(x<0){
            switch (y) {
                case 1 -> {
                    if (a == 4) {
                        if (team == 0 && b == 7) {
                            pieces[a][b] = new Piece("king", team, a, b, 2);
                            return;
                        }
                        if (team == 1 && b == 0) {
                            pieces[a][b] = new Piece("king", team, a, b, 2);
                            return;
                        }
                        pieces[a][b] = new Piece("king", team, a, b, 0);
                    } else {
                        pieces[a][b] = new Piece("king", x == -2 ? 0 : 1, a, b, 0);
                    }
                }
                case 2 -> pieces[a][b] = new Piece("queen", x == -2 ? 0 : 1, a, b, 2);
                case 3 -> {
                    if (team == 0) {
                        if (a == 0 && b == 7) {
                            pieces[a][b] = new Piece("rook", team, a, b, 2);
                            return;
                        }
                        if (a == 7 && b == 7) {
                            pieces[a][b] = new Piece("rook", team, a, b, 2);
                            return;
                        }
                    } else {
                        if (a == 0 && b == 0) {
                            pieces[a][b] = new Piece("rook", team, a, b, 2);
                            return;
                        }
                        if (a == 7 && b == 0) {
                            pieces[a][b] = new Piece("rook", team, a, b, 2);
                            return;
                        }
                    }
                    pieces[a][b] = new Piece("rook", team, a, b, 0);
                }
                case 4 -> pieces[a][b] = new Piece("bishop", x == -2 ? 0 : 1, a, b, 2);
                case 5 -> pieces[a][b] = new Piece("knight", x == -2 ? 0 : 1, a, b, 2);
                case 6 -> {
                    if (team == 0) {
                        if (b == 6) {
                            pieces[a][b] = new Piece("pawn", team, a, b, 2);
                            return;
                        }
                    } else {
                        if (b == 1) {
                            pieces[a][b] = new Piece("pawn", team, a, b, 2);
                            return;
                        }
                    }
                    pieces[a][b] = new Piece("pawn", team, a, b, 0);
                }
            }
        }
        else
        {
            if(pieces[x][y].name.equals("king")){
                if(x==4){
                    if(team==0&&b==7){
                        pieces[a][b]=new Piece("king",team,a,b,2);
                        return;
                    }
                    if(team==1&&b==0){
                        pieces[a][b]=new Piece("king",team,a,b,2);
                        return;   
                    }
                    pieces[a][b]=new Piece("king",team,a,b,0);
                    return;
                }   
            }
            if(pieces[x][y].name.equals("rook")){
                if(team==0){
                    if(a==0&&b==7){
                        pieces[a][b]=new Piece("rook",team,a,b,2);
                        return;
                    }
                    if(a==7&&b==7){
                        pieces[a][b]=new Piece("rook",team,a,b,2);
                        return;
                    }
                    pieces[a][b]=new Piece("rook",team,a,b,0);
                }
                else
                {
                    if(a==0&&b==0){
                        pieces[a][b]=new Piece("rook",team,a,b,2);
                        return;
                    }
                    if(a==7&&b==0){
                        pieces[a][b]=new Piece("rook",team,a,b,2);
                        return;
                    }
                    pieces[a][b]=new Piece("rook",team,a,b,0); 
                    return;
                }
            }
            if(pieces[x][y].name.equals("pawn")){
                if(team==0){
                    if(b==6){
                        pieces[a][b]=new Piece("pawn",team,a,b,2);
                        return;
                    }
                }else{
                    if(b==1){
                        pieces[a][b]=new Piece("pawn",team,a,b,2);
                        return;
                    }
                }
                pieces[a][b]=new Piece("pawn",team,a,b,0);
                return;
            }
            pieces[a][b]=new Piece(pieces[x][y].name,pieces[x][y].team,a,b,2);
        }
    }

    public String fenBuilder(){
        StringBuilder result = new StringBuilder();
        for(int c=0;c<8;c++){
            int count=0;
            for(int r=0;r<8;r++){
                if(pieces[r][c].isSquare()){
                    count++;   
                }else{
                    if(count!=0){
                        result.append(count);
                        count=0; 
                    }  
                    String temp =""+pieces[r][c].name.charAt(0);
                    if(pieces[r][c].isKnight()){
                        temp="n";   
                    }
                    if(pieces[r][c].isWhite()){
                        temp=temp.toUpperCase();
                    }
                    result.append(temp);
                }
            }
            if(count!=0){
                result.append(count);
            }
            if(c!=7){
                result.append("/");
            }
        }
        result.append(turn ? " w " : " b ");
        result.append(castledStr());
        result.append(" ").append(lastEP()).append(" ");
        result.append(movesSinceLastCapture).append(" ");
        result.append(turnCount);
        return result.toString();
    }

    public String lastEP(){
        if(lastMove[0]==-1){
            return "-";   
        }
        Piece temp = pieces[lastMove[2]][lastMove[3]];
        if(temp.isPawn()&&Math.abs(lastMove[3]-lastMove[1])!=1){
            switch(lastMove[0]){
                case 0:
                return "a"+(temp.isWhite()?"3":"6");
                case 1:
                return "b"+(temp.isWhite()?"3":"6");
                case 2:
                return "c"+(temp.isWhite()?"3":"6");
                case 3:
                return "d"+(temp.isWhite()?"3":"6");
                case 4:
                return "e"+(temp.isWhite()?"3":"6");
                case 5:
                return "f"+(temp.isWhite()?"3":"6");
                case 6:
                return "g"+(temp.isWhite()?"3":"6");
                case 7:
                return "h"+(temp.isWhite()?"3":"6");
            }
        }
        return "-";
    }

    public void update(int[] move) {
        if(!turn){
            turnCount++;   
        }
        if((lastLastMove[0]==move[2]&&lastLastMove[1]==move[3]&&lastLastMove[2]==move[0]&&lastLastMove[3]==move[1])){
            repeatedPosition++;
        }
        else
        {
            repeatedPosition=1;
        }
        for(int i=0;i<4;i++){
            lastLastMove[i]=lastMove[i];
            lastMove[i]=move[i];
        }
        Piece temp = pieces[move[0]][move[1]];
        int x = move[2];
        int y = move[3];
        if (enPassant(move)){
            pieces[x][y+(temp.team==0?1:-1)]=new Piece("square", -1, x, y+(temp.team==0?1:-1), 2);
            movesSinceLastCapture=0;
        }
        if (castled(move)) {
            temp.hasMoved=0;
            if (x==6) {
                pieces[5][y]=new Piece("rook", temp.team, 5, y, 0); 
                pieces[7][y]=new Piece("square", -1, 7, y, 2);
            }
            if (x==2) {
                pieces[3][y]=new Piece("rook", temp.team, 3, y, 0); 
                pieces[0][y]=new Piece("square", -1, 0, y, 2);
            }
        }
        if((!temp.isPawn())&&pieces[x][y].isSquare()){
            movesSinceLastCapture++;   
        }
        else{
            movesSinceLastCapture=0;
        }
        pieces[x][y]=temp;
        if (temp.isPawn()&&y==(temp.team==0?0:7)) {
            pieces[x][y]=new Piece("queen", temp.team, x, y, 0);
        }
        pieces[move[0]][move[1]]=new Piece("square", -1, move[0], move[1], 2);
        temp.rank=x; 
        temp.file=y; 
        for (int r=0; r<8; r++) {
            for (int c=0; c<8; c++) {
                Piece temp1 = pieces[r][c];
                if (temp1.hasMoved==1&&temp1.sameColor(temp)) {
                    temp1.hasMoved--;
                }
            }
        } 
        if (pieces[move[2]][move[3]].hasMoved==2) {
            pieces[move[2]][move[3]].hasMoved--;
        }
        turn=!turn;
    }

    public Board nextBoard(int[] move){
        Board res = new Board(this);
        res.update(move);
        return res;
    }

    public boolean noMoves() {
        updateLegalMoves();
        return legalMoves.size()==0;
    }

    public boolean inCheck() {
        String king="";
        for (int r=0; r<8; r++) {
            for (int c=0; c<8; c++) {
                if ((!turn&&pieces[r][c].team==1&&pieces[r][c].name.equals("king"))
                ||(turn&&pieces[r][c].team==0&&pieces[r][c].name.equals("king"))) {
                    king=""+r+","+c;
                }
            }
        }
        for (int r=0; r<8; r++) {
            for (int c=0; c<8; c++) {
                if ((turn&&pieces[r][c].team==1)||(!turn&&pieces[r][c].team==0)) {
                    for (int i=0; i<8; i++) {
                        for (int j=0; j<8; j++) {
                            if (futureIsLegal(new int[]{r,c,i,j})) {
                                if ((""+i+","+j).equals(king)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public int[] kingPosition(){
        for(int r=0;r<8;r++){
            for(int c=0;c<8;c++){
                if(turn&&pieces[r][c].isWhite()&&pieces[r][c].isKing()){
                    return new int[]{r,c};
                }
                if((!turn)&&pieces[r][c].isBlack()&&pieces[r][c].isKing()){
                    return new int[]{r,c};
                }
            }
        }
        return new int[]{};
    }

    public boolean futureInCheck() {
        turn=!turn;
        boolean res = inCheck();
        turn=!turn;
        return res;
    }

    public boolean futureIsLegal(int[] move) {
        turn=!turn;
        boolean res = isLegal(move);
        turn=!turn;
        return res;
    }

    public ArrayList<int[]> getArrayMoves() {
        ArrayList<int[]> capturesPositive = new ArrayList<>();
        ArrayList<int[]> capturesNegative = new ArrayList<>();
        ArrayList<int[]> nonCapturesNegative = new ArrayList<>();
        ArrayList<int[]> nonCapturesPositive = new ArrayList<>();
        ArrayList<int[]> pawnCapturesNegative = new ArrayList<>();
        ArrayList<int[]> pawnCapturesPositive = new ArrayList<>();
        ArrayList<int[]> pawnNonCapturesNegative = new ArrayList<>();
        ArrayList<int[]> pawnNonCapturesPositive = new ArrayList<>();
        for (int r=0; r<8; r++) {
            for (int c=0;c<8;c++) {
                if ((turn&&pieces[r][c].isWhite())||(!turn&&pieces[r][c].isBlack())) {
                    for (int i=0; i<8; i++) {
                        for (int j=0;j<8;j++) {
                            int[] temp = new int[]{r,c,i,j};
                            Board tempBoard=nextBoard(temp);
                            if (isLegal(temp)&&!tempBoard.futureInCheck()) {
                                double eval=0;
                                switch(pieces[r][c].name) {
                                    case "square" : 
                                    break; 
                                    case "king" : 
                                    if(endGame()){
                                        eval=kingMiddleTable[r][turn?c:7-c]-kingMiddleTable[i][turn?j:7-j];
                                    }
                                    else
                                    {
                                        eval=kingEndTable[r][turn?c:7-c]-kingEndTable[i][turn?j:7-j];
                                    }

                                    break;
                                    case "pawn" : 
                                    eval=pawnTable[r][turn?c:7-c]-pawnTable[i][turn?j:7-j];
                                    break; 
                                    case "knight" : 
                                    eval=knightTable[r][turn?c:7-c]-knightTable[i][turn?j:7-j];
                                    break; 
                                    case "bishop" : 
                                    eval=bishopTable[r][turn?c:7-c]-bishopTable[i][turn?j:7-j];
                                    break; 
                                    case "rook" : 
                                    eval=rookTable[r][turn?c:7-c]-rookTable[i][turn?j:7-j];
                                    break; 
                                    case "queen" : 
                                    eval=queenTable[r][turn?c:7-c]-queenTable[i][turn?j:7-j];
                                    break;
                                }

                                if(pieces[i][j].team==-1){
                                    if((turn&&eval<=0)||(!turn&&eval>=0)){
                                        if(pieces[r][c].isPawn()){
                                            pawnNonCapturesNegative.add(temp);
                                        }
                                        else
                                        {
                                            nonCapturesNegative.add(temp);
                                        }
                                    }
                                    else
                                    {
                                        if(pieces[r][c].isPawn()){
                                            pawnNonCapturesPositive.add(temp);
                                        }
                                        else
                                        {
                                            nonCapturesPositive.add(temp);
                                        }
                                    }
                                }
                                else
                                {
                                    if((turn&&eval<=0)||(!turn&&eval>=0)){
                                        if(pieces[r][c].isPawn()){
                                            pawnCapturesNegative.add(temp);
                                        }
                                        else
                                        {
                                            capturesNegative.add(temp);
                                        }
                                    }
                                    else
                                    {
                                        if(pieces[r][c].isPawn()){
                                            pawnCapturesPositive.add(temp);
                                        }
                                        else
                                        {
                                            capturesPositive.add(temp);
                                        }
                                    }
                                }
                            }
                        }  
                    }
                }
            }
        }
        Collections.shuffle(capturesPositive);
        Collections.shuffle(capturesNegative);
        Collections.shuffle(nonCapturesPositive);
        Collections.shuffle(nonCapturesNegative);
        Collections.shuffle(pawnNonCapturesPositive);
        Collections.shuffle(pawnNonCapturesNegative);
        Collections.shuffle(pawnCapturesPositive);
        Collections.shuffle(pawnCapturesNegative);

        pawnCapturesPositive.addAll(capturesPositive);
        pawnCapturesPositive.addAll(pawnCapturesNegative);
        pawnCapturesPositive.addAll(capturesNegative);
        pawnCapturesPositive.addAll(pawnNonCapturesPositive);
        pawnCapturesPositive.addAll(pawnNonCapturesNegative);
        pawnCapturesPositive.addAll(nonCapturesPositive);
        pawnCapturesPositive.addAll(nonCapturesNegative);
        return pawnCapturesPositive;
    }

    public ArrayList<Board> getChildrenBoards() {
        ArrayList<int[]> moves = getArrayMoves();
        ArrayList<Board> res = new ArrayList<>();
        for (int[] arr : moves) {
            res.add(nextBoard(arr));
        }
        return res;
    }

    public boolean legalMove(int[] move) {
        String s=""+move[0]+" "+move[1]+" "+move[2]+" "+move[3]+" "+pieces[move[0]][move[1]].name+" "+pieces[move[2]][move[3]].name;
        updateLegalMoves();
        return legalMoves.contains(s);
    }

    public int[] getMove(String str){
        int[] res = new int[4];
        for(int i=0;i<4;i++){
            res[i]=Integer.parseInt(""+str.charAt(2*i));
        }
        return res;
    }

    public String getName(int[] move){
        return ""+move[0]+" "+move[1]+" "+move[2]+" "+move[3]+" "+pieces[move[0]][move[1]].name+" "+pieces[move[2]][move[3]].name;
    }

    public boolean isLegal(int[] move) {
        return pieces[move[0]][move[1]].isLegalMove(this, move[2], move[3]);
    }

}