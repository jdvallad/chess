import java.util.HashMap;
import java.util.Map;

public class Driver2 {
    static String startpos = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    static String densepos = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1";

    public static void main(String[] args) {
        String testFen = startpos;
        int testDepth = 4;
        System.out.println("Timing with:");
        System.out.println("Fen: " + testFen);
        System.out.println("Depth: " + testDepth);
        Chess game = new Chess();
        game.setFromFEN(startpos);
        Map<Short, Long> map = new HashMap<>();
        long startTime = System.nanoTime();
        game.Perft(testDepth, false, map);
        long endTime = System.nanoTime();
        // Lets print the duration to console!
        long duration = (endTime - startTime);
        System.out.println("This took " + formatNanoIntoSeconds(duration) + " seconds.");
    }

    public static String formatNanoIntoSeconds(long duration) {
        StringBuilder seconds = new StringBuilder("000000000000" + duration);
        seconds.insert(seconds.length() - 9, ".");
        while (seconds.charAt(0) == '0') {
            seconds.deleteCharAt(0);
        }
        return seconds.toString();
    }
}
