import processing.core.PImage;

public abstract class Scene {
    public SceneSwitcher screen;
    private final String id;
    private boolean active;
    private PImage image;
    public Scene(SceneSwitcher app, String str, boolean bn) {
        screen = app;
        id = str;
        active = bn;
    }

    public String getId() {
        return id;
    }

    public void activate() {
        active = true;
    }

    public void deactivate() {
        active = false;
    }

    public void switchState() {
        active = !active;
    }

    public boolean isActive() {
        return active;
    }
    public void snap(){
        image = screen.get();
    }
    public void refresh(){
        screen.background(image);
    }
    public abstract void draw();

    public abstract void settings();

    public abstract void keyPressed();

    public abstract void keyReleased();

    public abstract void mousePressed();

    public abstract void mouseReleased();
}
