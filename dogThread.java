public class dogThread extends Thread {
    Chess board;
    private String[] move = new String[3];
    private final int depth;
    public dogThread(Chess b, int d) {
        board = b;
        depth = d;
        move[0] = "";
    }

    public void run() {
        try {
          //  System.out.println("Move being calculated...");
            move = Dogfish.ponder(board, depth, -5000, 5000);
        } catch (Exception e) {
            move[0] = "";
        }
    }

    public String[] move() {
        String[] temp = move.clone();
        move = new String[3];
        move[0] = "";
        return temp;
    }
}
