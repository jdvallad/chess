import processing.core.PApplet;
import processing.core.PImage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Button {
    private final PApplet screen;
    private final List<PImage> images;
    private final String id;
    private boolean active;
    private final float xPosition;
    private final float yPosition;
    private final float width;
    private final float height;
    public int pressCount = -1;
    private final int delay = 2;

    public Button(PApplet app, String str, boolean bn, float x, float y, float w, float h, PImage... img) {
        screen = app;
        images = new ArrayList<PImage>(Arrays.asList(img));
        id = str;
        active = bn;
        width = w;
        height = h;
        if (x < 0)
            xPosition = 1920f + x - width;
        else
            xPosition = x;
        if (y < 0)
            yPosition = 1080f + y - height;
        else
            yPosition = y;
    }

    public Button(PApplet app, String str, boolean bn, float x, float y, float w, float h, PImage img) {
        this(app, str, bn, x, y, w, h, new PImage[]{img});
    }

    public Button(PApplet app, String str, boolean bn, float x, float y, float m, PImage img) {
        this(app, str, bn, x, y, img.width * m, img.height * m, img);
    }

    public Button(PApplet app, String str, boolean bn, float x, float y, PImage img) {
        this(app, str, bn, x, y, img.width, img.height, img);
    }

    public String getId() {
        return id;
    }

    public boolean isActive() {
        return active;
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

    public boolean pressed() {
        if (active) {
            if (screen.mousePressed && mouseOverButton()) {
                pressCount = delay;
                return true;
            }
            return pressCount != -1;
        }
        return false;
    }

    public boolean justPressed() {
        if (active)
            return pressCount == delay - 1;
        return false;
    }

    public boolean mouseOverButton() {
        if (active) {
            float widthP = (float) screen.width / 1920f;
            float heightP = (float) screen.height / 1080f;
            float xShift = (screen.mouseX / widthP) - xPosition;
            float yShift = (screen.mouseY / heightP) - yPosition;
            return (0 < xShift) && (xShift < width) && (0 < yShift) && (yShift < height);
        }
        return false;
    }


    public void update() {
        if (isActive()) {
            if (pressed()) {
                if (justPressed()) {
                    action();
                }
            }
            screen.tint(255);
            if (pressCount > 0) {
                screen.tint(120);
            }
            if (pressCount == 0)
                screen.tint(255);
            for (PImage image : images)
                screen.image(image, xPosition, yPosition, width, height);
            if (pressCount != -1)
                pressCount--;
        }
    }

    public abstract void action();
}
