import xyz.niflheim.stockfish.engine.enums.Option;
import xyz.niflheim.stockfish.engine.enums.Query;
import xyz.niflheim.stockfish.engine.enums.QueryType;
import xyz.niflheim.stockfish.engine.enums.Variant;

public class Stockfish
{
    StockfishClient client;
    public Stockfish(){
        try{
            client = new StockfishClient.Builder()
            .setInstances(4)
            .setOption(Option.Threads, 4) // Number of threads that Stockfish will use
            .setOption(Option.Minimum_Thinking_Time, 1000) // Minimum thinking time Stockfish will take
            .setOption(Option.Skill_Level, 10) // Stockfish skill level 0-20
            .setVariant(Variant.BMI2) // Stockfish Variant
            .build();   
        }
        catch(Exception ignored){}
    }

    public static int[] ponder(Board b,int difficulty,int depth,int mintime){
        int[] res = new int[4];
        try{
            StockfishClient client = new StockfishClient.Builder()
                .setInstances(4)
                .setOption(Option.Threads, 4) // Number of threads that Stockfish will use
                .setOption(Option.Minimum_Thinking_Time, 1000) // Minimum thinking time Stockfish will take
                .setOption(Option.Skill_Level, 10) // Stockfish skill level 0-20
                .setVariant(Variant.BMI2) // Stockfish Variant
                .build();

            String s= client.submit(new Query.Builder(QueryType.Best_Move)
                    .setFen(b.fenBuilder())
                    .setDifficulty(5*difficulty) //Setting this overrides Skill Level option
                    .setDepth(depth)//Setting this makes Stockfish search deeper
                    .setMovetime(mintime) //Setting this overrides the minimum thinking time
                    .build()); // This is handling the result of the query
            switch (s.charAt(0)) {
                case 'a' -> res[0] = 0;
                case 'b' -> res[0] = 1;
                case 'c' -> res[0] = 2;
                case 'd' -> res[0] = 3;
                case 'e' -> res[0] = 4;
                case 'f' -> res[0] = 5;
                case 'g' -> res[0] = 6;
                case 'h' -> res[0] = 7;
            }

            switch (s.charAt(2)) {
                case 'a' -> res[2] = 0;
                case 'b' -> res[2] = 1;
                case 'c' -> res[2] = 2;
                case 'd' -> res[2] = 3;
                case 'e' -> res[2] = 4;
                case 'f' -> res[2] = 5;
                case 'g' -> res[2] = 6;
                case 'h' -> res[2] = 7;
            }
            res[1]=8-Integer.parseInt(""+s.charAt(1));
            res[3]=8-Integer.parseInt(""+s.charAt(3));
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return res;
    }
}
