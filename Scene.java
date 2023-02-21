import processing.core.PApplet;
import processing.core.PImage;

public abstract class Scene {
    protected PApplet screen;
    private final String id;
    private boolean active;
    private PImage image;
    private boolean initialized;

    public Scene(String sceneID, boolean isActive) {
        screen = null;
        id = sceneID;
        active = isActive;
        initialized = false;
    }

    public void setScreen(PApplet screen){
        this.screen = screen;
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

    public void snap() {
        image = screen.get();
    }

    public void refresh() {
        screen.background(image);
    }

    public abstract void draw();

    public abstract void settings();

    public abstract void keyPressed();

    public abstract void keyReleased();

    public abstract void mousePressed();

    public abstract void mouseReleased();

    public boolean isInitialized() {
        return initialized;
    }

    public void initialize() {
        initialized = true;
    }
}
