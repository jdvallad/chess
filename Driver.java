public class Driver{

    public static void main(String[] args) {
        Scene chessGame = new GameScene(null, "Chess Game", true);
        SceneSwitcher.addScene(chessGame);
        SceneSwitcher.begin();
    }
}