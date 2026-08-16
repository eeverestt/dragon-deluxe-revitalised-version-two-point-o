package com.peak.manager.game.attack;

import com.peak.content.entity.DragonEntity;
import net.minecraft.entity.boss.dragon.phase.Phase;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public class InitPhase extends AttackPhase {
    static int initCounter;

    public InitPhase(AttackPhaseManager manager, DragonEntity dragon, World world) {
        super(manager, dragon, world);

    }

    @Override
    public void tick() {
        initCounter++;

        if (initCounter >= 60) {
            this.manager.nextPhase();
            initCounter = 0;
        }
    }

    @Override
    public boolean isSittingOrHovering() {
        return true;
    }

    @Override
    public PhaseType<? extends Phase> getType() {
        return null;
    }
}
