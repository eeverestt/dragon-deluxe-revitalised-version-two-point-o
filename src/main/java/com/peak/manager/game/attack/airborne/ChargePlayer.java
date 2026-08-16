package com.peak.manager.game.attack.airborne;

import com.peak.content.entity.DragonEntity;
import com.peak.manager.game.attack.AttackPhase;
import com.peak.manager.game.attack.AttackPhaseManager;
import net.minecraft.entity.boss.dragon.phase.Phase;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ChargePlayer extends AttackPhase {
    PlayerEntity target;
    static int initCounter;

    public ChargePlayer(AttackPhaseManager manager, DragonEntity dragon, World world) {
        super(manager, dragon, world);
    }

    @Override
    public void beginPhase() {
        if (world.getServer() == null) return;

        List<PlayerEntity> candidates = world.getPlayers().stream()
                .filter(p -> p.getPos().isInRange(dragon.getPos(), 200)).collect(Collectors.toUnmodifiableList());

        if (candidates.isEmpty()) return;

        Random random = new Random();
        target = candidates.get(random.nextInt(candidates.size()));
    }

    @Override
    public void tick() {
        initCounter++;

        if (initCounter >= 10 * 20) {
            this.manager.nextPhase();
            initCounter = 0;
        }
    }

    @Override
    public float getMaxYAcceleration() {
        return 8.0F;
    }

    @Override
    public float getYawAcceleration() {
        return super.getYawAcceleration();
    }

    @Override
    public @Nullable Vec3d getPathTarget() {
        if (target == null) return null;
        Vec3d dir = new Vec3d(target.getX(), target.getY() - 0.5, target.getZ()).add(dragon.getPos().multiply(-1));
        float behindAmountTarget = 3f;

        Vec3d offset = dir.normalize().multiply(behindAmountTarget);
        return new Vec3d(target.getX(), target.getY() - 0.5, target.getZ()).add(offset.multiply(-1));
    }

    @Override
    public PhaseType<? extends Phase> getType() {
        return null;
    }
}
