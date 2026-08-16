package com.peak.manager.game.attack;

import com.peak.content.entity.DragonEntity;
import com.peak.manager.util.BossBarManager;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.EndPortalFeature;
import org.jetbrains.annotations.Nullable;

public class DyingPhase extends AttackPhase {
    @Nullable
    private Vec3d target;
    private int ticks;

    public DyingPhase(AttackPhaseManager manager, DragonEntity dragon, World world) {
        super(manager, dragon, world);
    }

    public void clientTick() {
        if (this.ticks++ % 10 == 0) {
            float f = (this.dragon.getRandom().nextFloat() - 0.5F) * 8.0F;
            float g = (this.dragon.getRandom().nextFloat() - 0.5F) * 4.0F;
            float h = (this.dragon.getRandom().nextFloat() - 0.5F) * 8.0F;
            this.dragon.getWorld().addParticle(ParticleTypes.EXPLOSION_EMITTER, this.dragon.getX() + (double)f, this.dragon.getY() + (double)2.0F + (double)g, this.dragon.getZ() + (double)h, (double)0.0F, (double)0.0F, (double)0.0F);
            BossBarManager.die();
        }
    }

    public void serverTick() {
        ++this.ticks;
        if (this.target == null) {
            BlockPos blockPos = this.dragon.getWorld().getTopPosition(Heightmap.Type.MOTION_BLOCKING, EndPortalFeature.offsetOrigin(this.dragon.getFightOrigin()));
            this.target = Vec3d.ofBottomCenter(blockPos);
        }

        double d = this.target.squaredDistanceTo(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
        if (!(d < (double)100.0F) && !(d > (double)22500.0F) && !this.dragon.horizontalCollision && !this.dragon.verticalCollision) {
            this.dragon.setHealth(1.0F);
        } else {
            this.dragon.setHealth(0.0F);
        }

        BossBarManager.die();
    }

    public void beginPhase() {
        this.target = null;
        this.ticks = 0;
    }

    public float getMaxYAcceleration() {
        return 3.0F;
    }

    @Nullable
    public Vec3d getPathTarget() {
        return this.target;
    }

    public PhaseType<net.minecraft.entity.boss.dragon.phase.DyingPhase> getType() {
        return PhaseType.DYING;
    }
}
