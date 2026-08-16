package com.peak.manager.game.attack;

import com.peak.content.entity.DragonEntity;
import com.peak.manager.game.DragonFightStateManager;
import com.peak.manager.game.attack.airborne.ChargePlayer;
import net.minecraft.server.MinecraftServer;

import java.util.List;
import java.util.Random;

public class AttackPhaseManager {
    public List<AttackPhase> phasesOne;
    public List<AttackPhase> phasesTwo;
    public List<AttackPhase> phasesThree;

    AttackPhase currentPhase;
    AttackPhase lastPhase;

    public AttackPhaseManager(DragonEntity dragon, MinecraftServer server) {
        phasesOne.add(new ChargePlayer(dragon, server));
    }

    public void nextPhase() {
        lastPhase = currentPhase;
        switch (DragonFightStateManager.getState()) {
            case DragonFightStateManager.State.ONE -> {
                currentPhase = phasesOne.get(new Random().nextInt(0, phasesOne.size() + 1));
                break;
            }
            case DragonFightStateManager.State.TWO -> {
                currentPhase = phasesTwo.get(new Random().nextInt(0, phasesTwo.size() + 1));
                break;
            }
            case DragonFightStateManager.State.THREE -> {
                currentPhase = phasesThree.get(new Random().nextInt(0, phasesThree.size() + 1));
                break;
            }
        }
    }

    public AttackPhase getCurrentPhase() {
        return currentPhase;
    }
}
