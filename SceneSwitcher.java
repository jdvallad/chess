import processing.core.*;
import java.util.*;

public class SceneSwitcher extends PApplet {

    static List<Scene> sceneList = new ArrayList<>();

    static public void begin() {
        PApplet.main(new String[] { "--present", "--window-color=#666666", "--stop-color=#cccccc",
                "SceneSwitcher" });
    }

    public static void addScene(Scene scene) {
        sceneList.add(scene);
    }

    public void settings() {
        for (Scene scn : sceneList) {
            scn.screen = this;
            scn.settings();
        }
    }

    public void draw() {
        for (Scene scn : sceneList)
            if (scn.isActive())
                scn.draw();
    }

    public void mousePressed() {
        for (Scene scn : sceneList)
            if (scn.isActive())
                scn.mousePressed();
    }

    public void mouseReleased() {
        for (Scene scn : sceneList)
            if (scn.isActive())
                scn.mouseReleased();
    }

    public void keyPressed() {
        for (Scene scn : sceneList)
            if (scn.isActive())
                scn.keyPressed();
    }

    public void keyReleased() {
        for (Scene scn : sceneList)
            if (scn.isActive())
                scn.keyReleased();
    }

    public Scene getScene(String id) {
        for (Scene scn : sceneList)
            if (scn.getId().equals(id))
                return scn;
        return null;
    }

    public void activateScene(String id) {
        for (Scene scn : sceneList)
            if (scn.getId().equals(id))
                scn.activate();
    }

    public void deactivateScene(String id) {
        for (Scene scn : sceneList)
            if (scn.getId().equals(id))
                scn.deactivate();
    }
}