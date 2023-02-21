public class Driver {

    public static void main(String[] args) {
        Chess game = new FastChess();
        Scene gameScene = new ChessScene(game, "Chess Scene", true);
        SceneSwitcher.addScene(gameScene);
        SceneSwitcher.begin();
    }
}