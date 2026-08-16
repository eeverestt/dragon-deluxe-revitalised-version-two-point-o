package com.peak.manager.game.attack.airborne;

import com.peak.content.entity.DragonEntity;
import com.peak.manager.game.attack.AttackPhase;
import net.minecraft.entity.boss.dragon.phase.Phase;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ChargePlayer extends AttackPhase {
    PlayerEntity target;

    public ChargePlayer(DragonEntity dragon, MinecraftServer server) {
        super(dragon, server);
    }

    @Override
    public void beginPhase() {
        target = server.getWorld(World.END).getRandomAlivePlayer();
        if (!target.getPos().isInRange(dragon.getPos(), 100))
            beginPhase();
    }

    @Override
    public @Nullable Vec3d getPathTarget() {
        Vec3d dir = target.getPos().add(dragon.getPos().multiply(-1));
        float behindAmountTarget = 3f;

        Vec3d offset = dir.normalize().multiply(behindAmountTarget);
        return target.getPos().add(offset.multiply(-1));
    }

    @Override
    public PhaseType<? extends Phase> getType() {
        return null;
    }
}
