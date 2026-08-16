package com.peak.manager.game.attack.airborne;

import com.peak.content.entity.DragonEntity;
import com.peak.manager.game.attack.AttackPhase;
import com.peak.manager.game.attack.AttackPhaseManager;
import net.minecraft.entity.boss.dragon.phase.Phase;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.DragonFireballEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class DragonsBreathArea extends AttackPhase {
    private PlayerEntity target;
    private int ticks;
    private int shot;

    public DragonsBreathArea(AttackPhaseManager manager, DragonEntity dragon, World world) {
        super(manager, dragon, world);
    }

    @Override
    public void beginPhase() {
        ticks = 0;
        shot = 0;
        target = null;

        List<PlayerEntity> candidates = world.getPlayers().stream()
                .filter(p -> p.getPos().isInRange(dragon.getPos(), 200.0))
                .collect(Collectors.toUnmodifiableList());

        if (candidates.isEmpty()) return;

        target = candidates.get(dragon.getRandom().nextInt(candidates.size()));
    }

    @Override
    public void tick() {
        ticks++;

        if (target == null || !target.isAlive()) {
            this.manager.nextPhase();
            return;
        }

        if (shot == 0 && ticks > 40) {
            shootBreath();
            shot++;
        }

        if (shot == 1 && ticks > 80) {
            shootBreath();
            shot++;
        }

        if (shot == 2 && ticks > 120) {
            shootBreath();
            this.manager.nextPhase();
        }
    }

    private void shootBreath() {
        if (world.isClient()) return;

        Vec3d from = dragon.getPos().add(0.0, 2.0, 0.0);
        Vec3d to = target.getPos().add(0.0, 0.5, 0.0);
        Vec3d dir = to.subtract(from).normalize();

        DragonFireballEntity fireball = new DragonFireballEntity(world, dragon, new Vec3d(dir.x, dir.y, dir.z));
        fireball.refreshPositionAndAngles(from.x, from.y, from.z, 0.0F, 0.0F);

        world.spawnEntity(fireball);
        world.playSound(
                null,
                dragon.getX(),
                dragon.getY(),
                dragon.getZ(),
                SoundEvents.ENTITY_ENDER_DRAGON_SHOOT,
                dragon.getSoundCategory(),
                2.5F,
                0.9F + dragon.getRandom().nextFloat() * 0.2F
        );
    }

    @Override
    public float getMaxYAcceleration() {
        return 8.0F;
    }

    @Override
    public @Nullable Vec3d getPathTarget() {
        if (target == null) return null;

        double hoverHeight = 18.0D;
        return new Vec3d(
                target.getX(),
                target.getY() + hoverHeight,
                target.getZ()
        );
    }

    @Override
    public PhaseType<? extends Phase> getType() {
        return null;
    }
}
