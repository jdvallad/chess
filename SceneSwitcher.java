import processing.core.*;

import java.util.*;

public class SceneSwitcher extends PApplet {

    static public void mains(String[] passedArgs) {
        Chess logic = new Chess("rnbqkbnr/1ppppppp/8/p7/4P3/8/PPPP1PPP/RNBQKBNR w KQkq a6 0 2");
        logic.drawBoard();
        System.out.println(Chess.pieceAt(logic.fen,"e2"));
    }
    public static void doIt(Chess board,int i){
        HashMap<Short, Long> map = new HashMap<>();
        board.Perft(i, true, map);
        ArrayList<Short> list = new ArrayList<>(map.keySet());
        list.sort(
                Comparator.comparingLong(map::get)

        );
        for (short sh : list)
            System.out.println(Chess.decodeMove(sh) + ": " + map.get(sh));
    }
    static public void main(String[] passedArgs) {
        com.sun.javafx.application.PlatformImpl.startup(() -> {
        });
        try {
            String[] appletArgs = new String[]{"--present", "--window-color=#666666", "--stop-color=#cccccc", "SceneSwitcher"};
            if (passedArgs != null) {
                PApplet.main(concat(appletArgs, passedArgs));
            } else {
                PApplet.main(appletArgs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    float widthP, heightP;
    List<Scene> sceneList;

    public void settings() {
        size(displayWidth, displayHeight);
        widthP = (float) width / 1920f;
        heightP = (float) height / 1080f;
        sceneList = new ArrayList<>();
        sceneList.add(new GameScene(this, "game", true));
        sceneList.add(new TestScene(this, "testScene", false));
        sceneList.add(new CreationScene(this, "creationScene", false));
        for (Scene scn : sceneList)
            scn.settings();
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

    /*
Override methods to ensure that board is properly rendered on other resolutions.
 */
    public void rect(float a, float b, float c, float d) {
        super.rect(widthP * a, heightP * b, widthP * c, heightP * d);
    }

    public void circle(float a, float b, float c) {
        super.circle(widthP * a, heightP * b, widthP * c);
    }

    public void textSize(float s) {
        super.textSize(widthP * s);
    }

    public void text(String s, float a, float b) {
        super.text(s, widthP * a, heightP * b);
    }

    public void image(PImage img, float a, float b) {
        super.image(img, widthP * a, heightP * b, widthP * img.width, widthP * img.height);
    }

    public void image(PImage img, float a, float b, float x, float y) {
        super.image(img, widthP * a, heightP * b, widthP * x, heightP * y);
    }

}