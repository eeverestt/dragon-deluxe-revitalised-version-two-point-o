package com.peak.mixin.client;

import com.peak.manager.rendering.screenshake.ScreenshakeRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;update(Lnet/minecraft/world/BlockView;Lnet/minecraft/entity/Entity;ZZF)V"))
    private void onRenderWorld(RenderTickCounter tickCounter, CallbackInfo ci) {
        Camera camera = net.minecraft.client.MinecraftClient.getInstance().gameRenderer.getCamera();
        MatrixStack matrices = new MatrixStack();
        float tickDelta = tickCounter.getTickDelta(true);
        ScreenshakeRenderer.getInstance().applyShakeToCamera(camera, matrices, tickDelta);
    }
}