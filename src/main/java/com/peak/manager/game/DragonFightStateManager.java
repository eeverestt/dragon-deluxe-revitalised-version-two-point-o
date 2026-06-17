package com.peak.manager.game;

import net.minecraft.util.StringIdentifiable;

public class DragonFightStateManager {
    private static State state = State.TWO;

    public static void setState(State state) {
        DragonFightStateManager.state = state;
    }

    public static State getState() {
        return state;
    }

    public enum State {
        UNSPAWNED,
        ONE,
        TWO,
        THREE
    }
}
