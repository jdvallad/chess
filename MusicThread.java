import processing.core.PApplet;
import processing.sound.SoundFile;

public class MusicThread extends Thread {
    private final SoundFile music;
    private boolean running;

    public MusicThread(SoundFile file) {
        music = file;
    }

    public void run() {
        running = true;
        music.play();
    }

    public void cancel() {
        music.stop();
        running = false;
    }

    public boolean isRunning() {
        return running;

    }
}
