package com.peak;

import com.peak.client.DragonRenderers;
import com.peak.manager.rendering.screenshake.ScreenshakeRenderer;
import com.peak.packet.C2S.DragonAttackC2S;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

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

        ClientPreAttackCallback.EVENT.register(this::onPreAttack);

        DragonRenderers.registerModelLayers();
        DragonRenderers.registerRenderers();
    }

    private boolean onPreAttack(MinecraftClient client, ClientPlayerEntity player, int clickCount) {
        if (client.world == null || player == null) return false;
        if (player.isSpectator()) return false;
        if (clickCount == 0) return false;

        EnderDragonPart hitPart = raycastDragonPart(player, client.world);
        if (hitPart == null) return false;

        ClientPlayNetworking.send(new DragonAttackC2S(hitPart.getId()));
        return false;
    }

    private EnderDragonPart raycastDragonPart(ClientPlayerEntity player, World world) {
        double reach = player.isCreative() ? 5.0D : 3.0D;

        Vec3d start = player.getEyePos();
        Vec3d direction = player.getRotationVec(1.0F).normalize();
        Vec3d end = start.add(direction.multiply(reach));

        Box searchBox = new Box(start, end).expand(16.0D);

        List<EnderDragonEntity> dragons = world.getEntitiesByClass(
                EnderDragonEntity.class,
                searchBox,
                dragon -> dragon.isAlive() && !dragon.isSpectator()
        );

        double bestDistanceSq = Double.MAX_VALUE;
        EnderDragonPart bestPart = null;

        for (EnderDragonEntity dragon : dragons) {
            for (EnderDragonPart part : dragon.getBodyParts()) {
                if (part == null || !part.isAlive()) continue;

                Box box = part.getBoundingBox().expand(0.3D);
                Optional<Vec3d> hit = box.raycast(start, end);

                if (hit.isEmpty()) continue;

                double distSq = start.squaredDistanceTo(hit.get());
                if (distSq < bestDistanceSq) {
                    bestDistanceSq = distSq;
                    bestPart = part;
                }
            }
        }

        return bestPart;
    }
}
