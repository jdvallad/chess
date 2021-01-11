import processing.core.*;

public class Main extends PApplet {

    float widthP, heightP; //used to resize window on different resolutions
    chessboard board = new chessboard(this);
    chess logic = new chess(); //handles logic of chess
    static public void main(String[] passedArgs) {
        com.sun.javafx.application.PlatformImpl.startup(() -> {
        });
        try {
            String[] appletArgs = new String[]{"--present", "--window-color=#666666", "--stop-color=#cccccc", "Main"};
            if (passedArgs != null) {
                PApplet.main(concat(appletArgs, passedArgs));
            } else {
                PApplet.main(appletArgs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void settings() {
        widthP = (float) displayWidth / 1920f;
        heightP = (float) displayHeight / 1080f;
        board.settings();
    }

    public void draw() {
      board.draw();
    }

    public void keyPressed() {
     board.keyPressed();
    }

    public void mouseReleased() {
        board.mouseReleased();
    }

    public void mousePressed() {
        board.mousePressed();
    }

    public void rect(float a, float b, float c, float d) {
        super.rect(widthP * a, heightP * b, widthP * c, heightP * d);
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
        super.image(img, widthP * a, heightP * b, widthP * x, widthP * y);
    }

}