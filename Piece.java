import java.io.Serializable;
public class Piece implements Serializable
{
    public final String name;
    public final int team;//0 = white, 1 = black, -1 = no team (square)
    public int rank;
    public int file;
    public int hasMoved;
    public Piece(String n, int t, int r, int f, int b) {
        name=n;
        team=t;
        rank=r;
        file=f;
        hasMoved=b;
    }

    public Piece(String n, int t, int r, int f) {
        name=n;
        team=t;
        rank=r;
        file=f;
        hasMoved=2;
    }

    public boolean isPawn(){return name.equals("pawn");}

    public boolean isRook(){return name.equals("rook");}

    public boolean isKnight(){return name.equals("knight");}

    public boolean isBishop(){return name.equals("bishop");}

    public boolean isQueen(){return name.equals("queen");}

    public boolean isKing(){return name.equals("king");}

    public boolean isSquare(){return name.equals("square");}

    public boolean isWhite(){return team==0;}

    public boolean isBlack(){return team==1;}

    // --Commented out by Inspection (9/20/2020 10:04 AM):public boolean sameName(Piece p){return p.name.equals(name);}

    public boolean sameColor(Piece p){return p.team==team;}

    public boolean isLegalMove(Board board, int x, int y) {
        if (board.pieces[x][y].team==team) {
            return false;
        }
        return switch (name) {
            case "pawn" -> pawn(board, x, y);
            case "rook" -> rook(board, x, y);
            case "knight" -> knight(board, x, y);
            case "bishop" -> bishop(board, x, y);
            case "queen" -> queen(board, x, y);
            case "king" -> king(board, x, y);
            default -> false;
        };
    }

    public boolean queen(Board board, int x, int y) {
        return bishop(board, x, y)||rook(board, x, y);
    }

    public boolean king(Board board, int x, int y) {
        if(team==0){
            if(hasMoved==2){
                if(rank==4&&file==7&&x==6&&y==7){
                    if(!board.pieces[5][7].name.equals("square")){
                        return false;   
                    }
                    if(!board.pieces[6][7].name.equals("square")){
                        return false;   
                    }
                    if(!board.pieces[7][7].name.equals("rook")){
                        return false;   
                    }
                    if(board.pieces[7][7].hasMoved!=2){
                        return false;   
                    }
                    int temp1=hasMoved;
                    hasMoved=0;
                    if(board.inCheck()){
                        hasMoved=temp1;
                        return false;   
                    }
                    Board temp = board.nextBoard(new int[]{rank,file,5,7});
                    temp.turn=!temp.turn;
                    if(temp.inCheck()){
                        hasMoved=temp1;
                        return false;
                    }
                    temp = temp.nextBoard(new int[]{5,7,6,7});
                    temp.turn=!temp.turn;
                    if(temp.inCheck()){
                        hasMoved=temp1;
                        return false;
                    }
                    hasMoved=temp1;
                    return true;
                }
                if(rank==4&&file==7&&x==2&&y==7){
                    if(!board.pieces[3][7].name.equals("square")){
                        return false;   
                    }
                    if(!board.pieces[2][7].name.equals("square")){
                        return false;   
                    }
                    if(!board.pieces[1][7].name.equals("square")){
                        return false;   
                    }
                    if(!board.pieces[0][7].name.equals("rook")){
                        return false;   
                    }
                    if(board.pieces[0][7].hasMoved!=2){
                        return false;   
                    }
                    int temp1=hasMoved;
                    hasMoved=0;
                    if(board.inCheck()){
                        hasMoved=temp1;
                        return false;   
                    }
                    Board temp = board.nextBoard(new int[]{rank,file,3,7});
                    temp.turn=!temp.turn;
                    if(temp.inCheck()){
                        hasMoved=temp1;
                        return false;
                    }
                    temp = temp.nextBoard(new int[]{3,7,2,7});
                    temp.turn=!temp.turn;
                    if(temp.inCheck()){
                        hasMoved=temp1;
                        return false;
                    }
                    temp = temp.nextBoard(new int[]{2,7,1,7});
                    temp.turn=!temp.turn;
                    if(temp.inCheck()){
                        hasMoved=temp1;
                        return false;
                    }
                    hasMoved=temp1;
                    return true;
                }
            }
            int a =Math.abs(rank-x);
            int b =Math.abs(file-y);
            return (a<2&&b<2);
        }
        else
        {
            if(hasMoved==2){
                if(rank==4&&file==0&&x==6&&y==0){
                    if(!board.pieces[5][0].name.equals("square")){
                        return false;   
                    }
                    if(!board.pieces[6][0].name.equals("square")){
                        return false;   
                    }
                    if(!board.pieces[7][0].name.equals("rook")){
                        return false;   
                    }
                    if(board.pieces[7][0].hasMoved!=2){
                        return false;   
                    }
                    int temp1=hasMoved;
                    hasMoved=0;
                    if(board.inCheck()){
                        hasMoved=temp1;
                        return false;   
                    }
                    Board temp = board.nextBoard(new int[]{rank,file,5,0});
                    temp.turn=!temp.turn;
                    if(temp.inCheck()){
                        hasMoved=temp1;
                        return false;
                    }
                    temp = temp.nextBoard(new int[]{5,0,6,0});
                    temp.turn=!temp.turn;
                    if(temp.inCheck()){
                        hasMoved=temp1;
                        return false;
                    }
                    hasMoved=temp1;
                    return true;
                }
                if(rank==4&&file==0&&x==2&&y==0){
                    if(!board.pieces[3][0].name.equals("square")){
                        return false;   
                    }
                    if(!board.pieces[2][0].name.equals("square")){
                        return false;   
                    }
                    if(!board.pieces[1][0].name.equals("square")){
                        return false;   
                    }
                    if(!board.pieces[0][0].name.equals("rook")){
                        return false;   
                    }
                    if(board.pieces[0][0].hasMoved!=2){
                        return false;   
                    }
                    int temp1=hasMoved;
                    hasMoved=0;
                    if(board.inCheck()){
                        hasMoved=temp1;
                        return false;   
                    }
                    Board temp = board.nextBoard(new int[]{rank,file,3,0});
                    temp.turn=!temp.turn;
                    if(temp.inCheck()){
                        hasMoved=temp1;
                        return false;
                    }
                    temp = temp.nextBoard(new int[]{3,0,2,0});
                    temp.turn=!temp.turn;
                    if(temp.inCheck()){
                        hasMoved=temp1;
                        return false;
                    }
                    temp = temp.nextBoard(new int[]{2,0,1,0});
                    temp.turn=!temp.turn;
                    if(temp.inCheck()){
                        hasMoved=temp1;
                        return false;
                    }
                    hasMoved=temp1;
                    return true;
                }
            }
            int a =Math.abs(rank-x);
            int b =Math.abs(file-y);
            return (a<2&&b<2);
        }
    }

    public boolean bishop(Board board, int x, int y) {
        if (Math.abs(rank-x)!=Math.abs(file-y)) {
            return false;
        }
        if (rank==x||file==y) {
            return false;
        }
        int slope1 = (rank>x?1:-1);
        int slope2 =  (file>y?1:-1);
        for (int i=0; i<Math.abs(rank-x); i++) {
            if (i!=0&&!board.pieces[rank-i*slope1][file-i*slope2].name.equals("square")) {
                return false;
            }
        }
        return true;
    }

    public boolean rook(Board board, int x, int y) {
        if ((rank!=x&&file!=y)||(rank==x&&file==y)) {
            return false;
        }
        if (rank==x) {
            for (int i=0; i<Math.abs(file-y); i++) {
                if (i!=0&&!board.pieces[rank][file-i*(file>y?1:-1)].name.equals("square")) {
                    return false;
                }
            }
        }
        if (file==y) {
            for (int i=0; i<Math.abs(rank-x); i++) {
                if (i!=0&&!board.pieces[rank-i*(rank>x?1:-1)][file].name.equals("square")) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean knight(Board board, int x, int y) {
        if ((rank-x)*(file-y)==0) {
            return false;
        }
        return Math.abs(rank - x) + Math.abs(file - y) == 3;
    }

    public boolean pawn(Board board, int x, int y) {
        if (file==y) {
            return false;
        }
        if ((file-y)!=(team==0?1:-1)) {
            if (hasMoved!=2) {
                return false;
            }
            if (file - y != (team == 0 ? 2 : -2) || rank - x != 0) {
                return false;
            }
            if(team==0&&file!=6){
                return false;
            }
            if(!(team==0)&&file!=1){
                return false;
            }
            if(!board.pieces[rank][y+(team==0?1:-1)].name.equals("square")){
                return false;   
            }
        }
        if (rank==x) {
            if (!board.pieces[x][y].name.equals("square")) {
                return false;
            }
        }
        if (rank!=x) {
            if (Math.abs(rank-x)!=1) {
                return false;
            }
            if (board.pieces[x][y].team==team) {
                return false;
            }
            if (board.pieces[x][y].name.equals("square")) {
                if ((!(board.pieces[x][y+(team==0?1:-1)].name.equals("pawn")))) {
                    return false;
                }
                return board.pieces[x][y + (team == 0 ? 1 : -1)].hasMoved == 1;
            }
        }
        return true;
    }
}