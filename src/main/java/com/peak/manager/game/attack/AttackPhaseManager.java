package com.peak.manager.game.attack;

import com.peak.content.entity.DragonEntity;
import com.peak.manager.util.BossBarManager;
import com.peak.manager.game.DragonFightStateManager;
import com.peak.manager.game.attack.airborne.ChargePlayer;
import com.peak.manager.game.attack.airborne.DragonsBreathArea;
import com.peak.manager.game.attack.airborne.EndermanAggro;
import com.peak.manager.game.attack.airborne.HealingRoute;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Random;

public class AttackPhaseManager {
    public ArrayList<AttackPhase> phasesOne = new ArrayList<>();
    public ArrayList<AttackPhase> phasesTwo = new ArrayList<>();
    public ArrayList<AttackPhase> phasesThree = new ArrayList<>();

    DragonEntity dragon;
    World world;

    AttackPhase currentPhase;
    AttackPhase lastPhase;

    public AttackPhaseManager(DragonEntity dragon, World world) {
        this.dragon = dragon;
        this.world = world;
        currentPhase = new InitPhase(this, dragon, world);

        currentPhase.beginPhase();
        DragonFightStateManager.setState(DragonFightStateManager.State.ONE);

        phasesOne.add(new ChargePlayer(this, dragon, world));
        phasesOne.add(new DragonsBreathArea(this, dragon, world));
        phasesOne.add(new HealingRoute(this, dragon, world));
        phasesOne.add(new EndermanAggro(this, dragon, world));
    }

    public void nextPhase() {
        lastPhase = currentPhase;
        switch (DragonFightStateManager.getState()) {
            case DragonFightStateManager.State.ONE -> {
                currentPhase = phasesOne.get(new Random().nextInt(0, phasesOne.size()));
                break;
            }
            case DragonFightStateManager.State.TWO -> {
                //currentPhase = phasesTwo.get(new Random().nextInt(0, phasesTwo.size()));
                break;
            }
            case DragonFightStateManager.State.THREE -> {
                //currentPhase = phasesThree.get(new Random().nextInt(0, phasesThree.size()));
                break;
            }
        }

        currentPhase.beginPhase();
    }

    public void die() {
        currentPhase = new DyingPhase(this, dragon, world);
        BossBarManager.die();
    }

    public AttackPhase getCurrentPhase() {
        return currentPhase;
    }
}
