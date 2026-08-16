package com.peak.manager.game.attack;

import com.peak.content.entity.DragonEntity;
import net.minecraft.entity.boss.dragon.phase.Phase;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class AttackPhase implements Phase {
    protected final AttackPhaseManager manager;
    protected final DragonEntity dragon;
    protected final World world;

    public AttackPhase(AttackPhaseManager manager, DragonEntity dragon, World world) {
        this.manager = manager;
        this.dragon = dragon;
        this.world = world;
    }

    public boolean isSittingOrHovering() {
        return false;
    }

    public void clientTick() {
    }

    public void serverTick() {
    }

    public void tick() {
    }

    public void crystalDestroyed(EndCrystalEntity crystal, BlockPos pos, DamageSource source, @Nullable PlayerEntity player) {
    }

    public void beginPhase() {
    }

    public void endPhase() {
    }

    public float getMaxYAcceleration() {
        return 0.6F;
    }

    @Nullable
    public Vec3d getPathTarget() {
        return null;
    }

    public float modifyDamageTaken(DamageSource damageSource, float damage) {
        return damage;
    }

    public float getYawAcceleration() {
        float f = (float)this.dragon.getVelocity().horizontalLength() + 1.0F;
        float g = Math.min(f, 40.0F);
        return 2F / g / f;
    }
}
