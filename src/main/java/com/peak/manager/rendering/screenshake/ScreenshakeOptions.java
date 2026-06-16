package com.peak.manager.rendering.screenshake;

public class ScreenshakeOptions {
    private static ScreenshakeOptions instance;
    private boolean enabled = true;
    private float globalMultiplier = 1.0f;
    private float maxAmplitude = 10.0f;
    
    private ScreenshakeOptions() {}
    
    public static ScreenshakeOptions getInstance() {
        if (instance == null) {
            instance = new ScreenshakeOptions();
        }
        return instance;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        ScreenshakeRenderer.getInstance().setEnabled(enabled);
    }
    
    public float getGlobalMultiplier() {
        return globalMultiplier;
    }
    
    public void setGlobalMultiplier(float multiplier) {
        this.globalMultiplier = Math.max(0.0f, Math.min(2.0f, multiplier));
    }
    
    public float getMaxAmplitude() {
        return maxAmplitude;
    }
    
    public void setMaxAmplitude(float maxAmplitude) {
        this.maxAmplitude = Math.max(0.1f, Math.min(20.0f, maxAmplitude));
    }
    
    public float applyOptions(float amplitude) {
        if (!enabled) return 0;
        return Math.min(amplitude * globalMultiplier, maxAmplitude);
    }
}