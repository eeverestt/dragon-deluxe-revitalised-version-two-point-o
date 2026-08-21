package com.peak.api.event;

import com.peak.Client;
import com.peak.Main;
import com.peak.openal.DragonAudioEngine;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;

public class AudioEngineEvents {
    public static class Join implements ClientPlayConnectionEvents.Join {
        @Override
        public void onPlayReady(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client) {
            Client.AUDIO_ENGINE.scanAndLoad(Main.MODID, "audio")
                    .thenRun(() ->
                            System.out.println("[Ender dragon deluxe revitalised version two point o] Audio Engine successfully loaded")
                    );
        }
    }

    public static class Disconnect implements ClientPlayConnectionEvents.Disconnect {
        @Override
        public void onPlayDisconnect(ClientPlayNetworkHandler handler, MinecraftClient client) {
            Client.AUDIO_ENGINE.stopAndUnloadAll()
                    .thenRun(() -> System.out.println("[Ender dragon deluxe revitalised version two point o] Audio Engine unloaded"));
        }
    }

    public static class Tick implements ClientTickEvents.EndTick {
        @Override
        public void onEndTick(MinecraftClient client) {
            Client.AUDIO_ENGINE.tick();
        }
    }
}
