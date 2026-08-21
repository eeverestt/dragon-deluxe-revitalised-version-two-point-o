package com.peak.openal;

import com.peak.Client;
import com.peak.toast.ToastHelper;
import de.keksuccino.melody.resources.audio.openal.ALException;
import net.minecraft.util.Identifier;

import static com.peak.Main.id;

public class MusicPlayer {
    private static boolean battle = false;

    public static void obstructedVision(boolean type) {
        Identifier oldId = battle
                ? id("audio/music/obstructed-vision-battle.ogg")
                : id("audio/music/obstructed-vision-chill.ogg");

        DynamicTrack oldTrack = Client.AUDIO_ENGINE.get(oldId);

        float position = 0f;

        if (oldTrack != null) {
            position = oldTrack.getPosition();
            oldTrack.stop(3f);
        }

        battle = type;

        Identifier newId = battle
                ? id("audio/music/obstructed-vision-battle.ogg")
                : id("audio/music/obstructed-vision-chill.ogg");

        DynamicTrack newTrack = Client.AUDIO_ENGINE.get(newId);

        if (newTrack != null) {
            try {
                newTrack.play(3f, true);
                newTrack.setPosition(position);
            } catch (ALException e) {
                e.printStackTrace();
            }
        }

        ToastHelper.showMusicToast("Now Playing", battle ? "Obstructed Vision (Battle) | Bashful" : "Obstructed Vision (Chill) | Bashful");
    }
    
    public static boolean getBattle() {
        return battle;
    }
}