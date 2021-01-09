
import java.io.File;
 import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
/**
 * This is an example program that demonstrates how to play back an audio file
 * using the SourceDataLine in Java Sound API.
 * @author www.codejava.net
 *
 */
public class SoundPlayer {

// --Commented out by Inspection START (9/20/2020 10:06 AM):
//    // size of the byte buffer used to read/write the audio stream
//    private static final int BUFFER_SIZE = 4096;
// --Commented out by Inspection STOP (9/20/2020 10:06 AM)

    /**
     * Play a given audio file.
     */
    void play(String bip) {
        Media hit = new Media(new File(bip).toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(hit);
        mediaPlayer.play();
        try{
            Thread.sleep(100);
        }catch(Exception ignored){}
    }

}