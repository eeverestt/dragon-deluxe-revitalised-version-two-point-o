package com.peak.mixin.client;

import com.peak.manager.rendering.WorldShakeManager;
import com.peak.manager.rendering.screenshake.ScreenshakeRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraMixin {
    @Unique
    private static final PerlinNoiseSampler sampler = new PerlinNoiseSampler(Random.create());

    @Shadow
    private Vec3d pos;

    @Shadow
    private float pitch;

    @Shadow
    private float yaw;

    @Unique
    private static float randomiseOffset(int offset) {
        float intensity = 0.2f;
        float min = -intensity * 2;
        float max = intensity * 2;
        float sampled = (float) sampler.sample((MinecraftClient.getInstance().world.getTime() % 24000L + MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false)) / intensity, offset, 0) * 1.5f;
        return min >= max ? min : sampled * max;
    }

    @Inject(method = "update", at = @At("RETURN"))
    private void dragon$doScreenshake(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        Vec3d shakeOffset = ScreenshakeRenderer.getInstance().getShakeOffset();
        if (!shakeOffset.equals(Vec3d.ZERO)) {
            this.pos = this.pos.add(shakeOffset);
        }

        if (WorldShakeManager.isWorldShaking()) {
            Camera camera = (Camera) (Object) this;

            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            int age = player.age;
            float amplitude = .0025f;
            float strength = 0.5f;

            float yawOffset = 0;
            float pitchOffset = 0;

            amplitude *= WorldShakeManager.getWorldShakeAmplitude();

            camera.setRotation(camera.getYaw() + yawOffset, camera.getPitch() + pitchOffset);
            camera.setPos(camera.getPos().add(0, Math.sin((age + tickDelta) * strength) / 2f * amplitude, Math.cos((age + tickDelta) * strength) * amplitude));
        }
    }
}
