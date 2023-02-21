import java.util.Map;

public class Driver {

    public static void main(String[] args) {
        Map<String,Object> map = Map.of(
                "data", "lichess", // datapack to use for images and sounds
                "fen", "", 
                "perspective", "white", 
                "staticPerspective", true // whether or not perspective flips depending on who's turn it is
        );
        Scene gui = new ChessGUI(null, "Chess GUI", true, map);
        SceneSwitcher.addScene(gui);
        SceneSwitcher.begin();
    }
}