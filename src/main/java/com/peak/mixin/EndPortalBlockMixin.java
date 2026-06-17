package com.peak.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.peak.manager.game.DragonFightStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.EndPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EndPortalBlock.class)
public class EndPortalBlockMixin {
    @WrapMethod(method = "onEntityCollision")
    private void dragon$preventLeave(BlockState state, World world, BlockPos pos, Entity entity, Operation<Void> original) {
        if (!DragonFightStateManager.getState().equals(DragonFightStateManager.State.TWO)) {
            original.call(state, world, pos, entity);
        }

        if (world.isClient) return;

        if (world instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.PORTAL, entity.getX(), entity.getY() + 1.0, entity.getZ(), 30, 0.5, 0.5, 0.5, 0.2);
        }

        world.playSound(
                null,
                entity.getBlockPos(),
                SoundEvents.ENTITY_ENDERMAN_TELEPORT,
                SoundCategory.PLAYERS,
                1.0f,
                0.8f + world.random.nextFloat() * 0.4f
        );

        double dx = entity.getX() - (pos.getX() + 0.5);
        double dz = entity.getZ() - (pos.getZ() + 0.5);

        double distance = Math.max(0.001, Math.sqrt(dx * dx + dz * dz));

        dx /= distance;
        dz /= distance;

        double strength = 1.2;

        double vx = dx * strength;
        double vz = dz * strength;
        double vy = 0.9;

        entity.setVelocity(vx, vy, vz);
        entity.velocityModified = true;
    }
}
