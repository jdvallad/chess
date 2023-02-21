public class Driver{

    public static void main(String[] args) {
        Scene gui = new ChessGUI(null, "Chess GUI", true);
        SceneSwitcher.addScene(gui);
        SceneSwitcher.begin();
    }
}