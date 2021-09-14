import xyz.niflheim.stockfish.engine.enums.Option;
import xyz.niflheim.stockfish.engine.enums.Query;
import xyz.niflheim.stockfish.engine.enums.QueryType;
import xyz.niflheim.stockfish.engine.enums.Variant;

public class Stockfish {
    StockfishClient client;
    stockThread stock = new stockThread(null,null,0,0);
    public Stockfish() {
        try {
            client = new StockfishClient.Builder()
                    .setInstances(1)
                    .setOption(Option.Threads, 4) // Number of threads that Stockfish will use
                    .setOption(Option.Minimum_Thinking_Time, 1000) // Minimum thinking time Stockfish will take
                    .setOption(Option.Skill_Level, 10) // Stockfish skill level 0-20
                    .setVariant(Variant.BMI2) // Stockfish Variant
                    .build();
        } catch (Exception ignored) {
        }
    }

    public String ponder(String fen, int difficulty, int depth, int mintime) {
        try {
            String s = client.submit(new Query.Builder(QueryType.Best_Move)
                    .setFen(fen)
                    .setDifficulty(difficulty) //Setting this overrides Skill Level option
                    .setDepth(depth)//Setting this makes Stockfish search deeper
                    .setMovetime(mintime) //Setting this overrides the minimum thinking time
                    .build()); // This is handling the result of the query
            return s;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public String move(Chess logic,int depth,int difficulty,String old){
        if(!stock.running){
            stock = new stockThread(logic,this,depth,difficulty);
            stock.start();
        }
        String res = stock.move();
        if (!res.equals("")) {
            return res;
        } else {
            return new String[]{old, "", ""}[0];
        }
    }
}
