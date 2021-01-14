public class TestScene extends Scene {
    Button button;
    int count = 3;
    public TestScene(SceneSwitcher app, String str, boolean bn) {
        super(app, str, bn);
    }

    public void draw() {
        button.update();
    }

    public void settings() {
        button = new Button(screen, "testButton", true, 800, 800, screen.loadImage("./data/buttons/click.png")) {
            public void action() {
                count--;
                Chess.println("Starting in  " + count + "...");
                if (count == 0) {
                    screen.activateScene("game");
                    deactivate();
                }
            }
        };
    }

    public void mousePressed() {
        button.update();
    }

    public void mouseReleased() {

    }

    public void keyPressed() {
    }

    public void keyReleased() {

    }

}
