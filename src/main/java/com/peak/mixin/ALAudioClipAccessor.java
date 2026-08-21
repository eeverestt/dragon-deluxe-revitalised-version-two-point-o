package com.peak.mixin;

import de.keksuccino.melody.resources.audio.openal.ALAudioClip;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ALAudioClip.class)
public interface ALAudioClipAccessor {

    @Accessor("source")
    int getSource();
}