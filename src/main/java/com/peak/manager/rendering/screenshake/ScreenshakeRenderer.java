package com.peak.manager.rendering.screenshake;

import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class ScreenshakeRenderer {
    private static ScreenshakeRenderer instance;
    private final List<ScreenShakeEffect> activeShakes = new ArrayList<>();
    private final Random random = new Random();
    private boolean enabled = true;
    private float currentAmplitudeX = 0;
    private float currentAmplitudeY = 0;
    private float currentAmplitudeZ = 0;
    private float currentRotationAmplitude = 0;
    private float previousAmplitudeX = 0;
    private float previousAmplitudeY = 0;
    private float previousAmplitudeZ = 0;
    private float previousRotationAmplitude = 0;
    private Vec3d shakeOffset = Vec3d.ZERO;

    private ScreenshakeRenderer() {}

    public static ScreenshakeRenderer getInstance() {
        if (instance == null) {
            instance = new ScreenshakeRenderer();
        }
        return instance;
    }

    public void addShake(float amplitude, int duration, ShakeType type) {
        addShake(amplitude, amplitude, amplitude, 0, duration, type, ShakeFalloff.LINEAR);
    }

    public void addShake(float amplitudeX, float amplitudeY, float amplitudeZ,
                         float rotationAmplitude, int duration, ShakeType type, ShakeFalloff falloff) {
        activeShakes.add(new ScreenShakeEffect(
                amplitudeX, amplitudeY, amplitudeZ,
                rotationAmplitude, duration, type, falloff
        ));
    }

    public void clearAllShakes() {
        activeShakes.clear();
        resetCurrentValues();
    }

    public void stopLastShake() {
        if (!activeShakes.isEmpty()) {
            activeShakes.remove(activeShakes.size() - 1);
            if (activeShakes.isEmpty()) {
                resetCurrentValues();
            }
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            resetCurrentValues();
        }
    }

    public void tick() {
        if (!enabled || activeShakes.isEmpty()) {
            return;
        }

        Iterator<ScreenShakeEffect> iterator = activeShakes.iterator();
        while (iterator.hasNext()) {
            ScreenShakeEffect shake = iterator.next();
            shake.tick();

            if (shake.isFinished()) {
                iterator.remove();
            }
        }

        if (activeShakes.isEmpty()) {
            resetCurrentValues();
        } else {
            calculateCombinedShake();
        }
    }

    private void calculateCombinedShake() {
        float totalAmplitudeX = 0;
        float totalAmplitudeY = 0;
        float totalAmplitudeZ = 0;
        float totalRotation = 0;

        for (ScreenShakeEffect shake : activeShakes) {
            float currentAmplitude = shake.getCurrentAmplitude();
            totalAmplitudeX += shake.amplitudeX * currentAmplitude;
            totalAmplitudeY += shake.amplitudeY * currentAmplitude;
            totalAmplitudeZ += shake.amplitudeZ * currentAmplitude;
            totalRotation += shake.rotationAmplitude * currentAmplitude;
        }

        previousAmplitudeX = currentAmplitudeX;
        previousAmplitudeY = currentAmplitudeY;
        previousAmplitudeZ = currentAmplitudeZ;
        previousRotationAmplitude = currentRotationAmplitude;

        this.currentAmplitudeX = (random.nextFloat() - 0.5f) * totalAmplitudeX * 2;
        this.currentAmplitudeY = (random.nextFloat() - 0.5f) * totalAmplitudeY * 2;
        this.currentAmplitudeZ = (random.nextFloat() - 0.5f) * totalAmplitudeZ * 2;
        this.currentRotationAmplitude = (random.nextFloat() - 0.5f) * totalRotation * 0.1f;
    }

    private void resetCurrentValues() {
        previousAmplitudeX = 0;
        previousAmplitudeY = 0;
        previousAmplitudeZ = 0;
        previousRotationAmplitude = 0;
        currentAmplitudeX = 0;
        currentAmplitudeY = 0;
        currentAmplitudeZ = 0;
        currentRotationAmplitude = 0;
        shakeOffset = Vec3d.ZERO;
    }

    public void applyShakeToCamera(Camera camera, MatrixStack matrices, float tickDelta) {
        if (!enabled || (currentAmplitudeX == 0 && currentAmplitudeY == 0 &&
                currentAmplitudeZ == 0 && currentRotationAmplitude == 0)) {
            return;
        }

        float smoothX = MathHelper.lerp(tickDelta, previousAmplitudeX, currentAmplitudeX);
        float smoothY = MathHelper.lerp(tickDelta, previousAmplitudeY, currentAmplitudeY);
        float smoothZ = MathHelper.lerp(tickDelta, previousAmplitudeZ, currentAmplitudeZ);
        float smoothRot = MathHelper.lerp(tickDelta, previousRotationAmplitude, currentRotationAmplitude);

        shakeOffset = new Vec3d(smoothX, smoothY, smoothZ);

        matrices.multiply(new Quaternionf().rotateZ(smoothRot * 0.01f));
        matrices.multiply(new Quaternionf().rotateX(smoothRot * 0.005f));
        matrices.multiply(new Quaternionf().rotateY(smoothRot * 0.005f));
    }

    public Vec3d getShakeOffset() {
        return shakeOffset;
    }

    public float getCurrentAmplitudeX() { return currentAmplitudeX; }
    public float getCurrentAmplitudeY() { return currentAmplitudeY; }
    public float getCurrentAmplitudeZ() { return currentAmplitudeZ; }
    public float getCurrentRotationAmplitude() { return currentRotationAmplitude; }
    public int getActiveShakeCount() { return activeShakes.size(); }
    public boolean isEnabled() { return enabled; }

    public enum ShakeType {
        RANDOM,
        SINE_WAVE,
        ROUGH,
        DECAYING
    }

    public enum ShakeFalloff {
        LINEAR,
        EXPONENTIAL,
        SMOOTH
    }

    private class ScreenShakeEffect {
        private final float amplitudeX;
        private final float amplitudeY;
        private final float amplitudeZ;
        private final float rotationAmplitude;
        private final int maxDuration;
        private final ShakeType type;
        private final ShakeFalloff falloff;
        private int time = 0;

        public ScreenShakeEffect(float amplitudeX, float amplitudeY, float amplitudeZ,
                                 float rotationAmplitude, int duration, ShakeType type, ShakeFalloff falloff) {
            this.amplitudeX = amplitudeX;
            this.amplitudeY = amplitudeY;
            this.amplitudeZ = amplitudeZ;
            this.rotationAmplitude = rotationAmplitude;
            this.maxDuration = Math.max(duration, 1);
            this.type = type;
            this.falloff = falloff;
        }

        public void tick() {
            time++;
        }

        public boolean isFinished() {
            return time >= maxDuration;
        }

        public float getCurrentAmplitude() {
            if (isFinished()) return 0;

            float progress = (float) time / maxDuration;
            float falloffMultiplier = getFalloffMultiplier(progress);
            float patternMultiplier = getPatternMultiplier(progress);

            return MathHelper.clamp(falloffMultiplier * patternMultiplier, 0, 1);
        }

        private float getFalloffMultiplier(float progress) {
            return switch (falloff) {
                case LINEAR -> 1 - progress;
                case EXPONENTIAL -> (float) Math.exp(-3 * progress);
                case SMOOTH -> (float) (1 - progress * progress * (3 - 2 * progress));
            };
        }

        private float getPatternMultiplier(float progress) {
            return switch (type) {
                case RANDOM -> 0.5f + 0.5f * random.nextFloat();
                case SINE_WAVE -> (float) (0.5 + 0.5 * Math.sin(progress * 20 * Math.PI));
                case ROUGH -> (random.nextFloat() > 0.7f) ? 1.0f : 0.3f;
                case DECAYING -> (float) Math.exp(-5 * progress) *
                        (0.5f + 0.5f * (float) Math.sin(progress * 30 * Math.PI));
            };
        }
    }
}