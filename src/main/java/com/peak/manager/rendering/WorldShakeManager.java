package com.peak.manager.rendering;

public class WorldShakeManager {
    private static boolean worldShaking = true;
    private static int worldShakeAmplitude = 0;

    public static boolean isWorldShaking() {
        return worldShaking;
    }

    public static void setWorldShaking(boolean val) {
        worldShaking = val;
    }

    public static int getWorldShakeAmplitude() {
        return worldShakeAmplitude;
    }

    public static void setWorldShakeAmplitude(int amplitude) {
        worldShakeAmplitude = amplitude;
    }
}
