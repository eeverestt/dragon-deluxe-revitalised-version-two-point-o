package com.peak.manager.game.attack.airborne;

import com.peak.content.entity.DragonEntity;
import com.peak.manager.game.attack.AttackPhase;
import com.peak.manager.game.attack.AttackPhaseManager;
import net.minecraft.entity.boss.dragon.phase.Phase;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class HealingRoute extends AttackPhase {
    private static final double HIGH_RADIUS = 45.0;
    private static final double LOW_RADIUS = 62.0;
    private static final Vec3d CENTER = new Vec3d(0.5, 0.0, 0.5);
    
    @Nullable
    private Vec3d target;
    private int ticks;
    private double currentAngle;
    private double radius;
    private boolean completedFullLoop;
    private int loopType;

    public HealingRoute(AttackPhaseManager manager, DragonEntity dragon, World world) {
        super(manager, dragon, world);
        this.ticks = 0;
        this.completedFullLoop = false;
        this.currentAngle = 0.0;
    }

    @Override
    public void beginPhase() {
        ticks = 0;
        completedFullLoop = false;
        loopType = dragon.getRandom().nextInt(2); // 0 = half loop, 1 = quarter loop

        Vec3d relativePos = dragon.getPos().subtract(CENTER);
        currentAngle = Math.atan2(relativePos.z, relativePos.x);

        int radType = dragon.getRandom().nextInt(2);
        radius = radType == 0 ? HIGH_RADIUS : LOW_RADIUS;
    }

    @Override
    public void tick() {
        ticks++;
        
        double angleIncrement = 0.02;
        currentAngle += angleIncrement;
        
        double targetAngle = loopType == 0 ? Math.PI / 1.2 : Math.PI / 1.8; // half loop = 2π, quarter loop = π
        
        if (currentAngle >= targetAngle) {
            completedFullLoop = true;
            this.manager.nextPhase();
        }
    }

    @Override
    public @Nullable Vec3d getPathTarget() {
        if (completedFullLoop) {
            return target;
        }
        
        double x = CENTER.x + radius * Math.cos(currentAngle);
        double z = CENTER.z + radius * Math.sin(currentAngle);
        double y = 105.0;
        
        target = new Vec3d(x, y, z);
        return target;
    }

    @Override
    public float getMaxYAcceleration() {
        return 0.6F;
    }

    @Override
    public PhaseType<? extends Phase> getType() {
        return null;
    }
}
