package com.peak;

import com.peak.manager.rendering.screenshake.ScreenshakeRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;

public class Client implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        initEvents();
    }

    public void initEvents() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            ScreenshakeRenderer manager = ScreenshakeRenderer.getInstance();
            Camera camera = context.camera();
            MatrixStack matrices = context.matrixStack();
            float tickDelta = context.tickCounter().getTickDelta(true);

            manager.applyShakeToCamera(camera, matrices, tickDelta);
        });
    }
}
