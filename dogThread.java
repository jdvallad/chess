public class dogThread extends Thread {
    Chess board;
    private String[] move = new String[3];
    private final int depth;
    boolean running;

    public dogThread(Chess b, int d) {
        board = b;
        depth = d;
        move[0] = "";
        running = false;
    }

    public void run() {
        try {
            running = true;
            move = Dogfish.ponder(board, depth, -5000, 5000);
        } catch (Exception e) {
            running = false;
            move[0] = "";
        }
    }

    public String[] move() {
        if(!move[0].equals(""))
            running = false;
        return move;
    }
}
