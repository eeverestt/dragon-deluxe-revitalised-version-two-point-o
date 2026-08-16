package com.peak.manager.util;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class DragonCollisionUtil {
    public static int damageTicks = -1;

    /**
     * Raycasts along a projectile's flight path to see if it intersects with a Dragon Part.
     *
     * @param projectile The projectile entity (e.g., Arrow, Snowball, Trident)
     * @param start      The projectile's position at the start of the tick
     * @param end        The projectile's target position at the end of the tick (start + velocity)
     * @return An EntityHitResult containing the hit part and exact hit position, or null if nothing was hit.
     */
    @Nullable
    public static EntityHitResult raycastDragonPartForProjectile(ProjectileEntity projectile, Vec3d start, Vec3d end) {
        //if (damageTicks > 0) return null;

        World world = projectile.getWorld();

        Box searchBox = projectile.getBoundingBox().stretch(projectile.getVelocity()).expand(1.0D);

        List<EnderDragonEntity> dragons = world.getEntitiesByClass(
                EnderDragonEntity.class,
                searchBox,
                dragon -> dragon.isAlive() && !dragon.isSpectator()
        );

        double bestDistanceSq = Double.MAX_VALUE;
        EnderDragonPart bestPart = null;
        Vec3d bestHitPos = null;

        for (EnderDragonEntity dragon : dragons) {
            for (EnderDragonPart part : dragon.getBodyParts()) {
                if (part == null || !part.isAlive()) continue;

                float margin = projectile.getTargetingMargin();
                Box partBox = part.getBoundingBox().expand(margin + 0.3D);

                Optional<Vec3d> hit = partBox.raycast(start, end);

                if (hit.isPresent()) {
                    double distSq = start.squaredDistanceTo(hit.get());
                    if (distSq < bestDistanceSq) {
                        bestDistanceSq = distSq;
                        bestPart = part;
                        bestHitPos = hit.get();
                    }
                }
            }
        }

        if (bestPart != null) {
            //damageTicks = 5;
            return new EntityHitResult(bestPart, bestHitPos);
        }

        return null;
    }

    public static void tick() {
        //damageTicks--;
    }
}