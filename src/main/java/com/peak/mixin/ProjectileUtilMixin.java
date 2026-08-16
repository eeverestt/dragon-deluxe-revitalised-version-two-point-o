package com.peak.mixin;

import com.peak.manager.util.DragonCollisionUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(ProjectileUtil.class)
public class ProjectileUtilMixin {
    @Inject(
            method = "getEntityCollision(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;F)Lnet/minecraft/util/hit/EntityHitResult;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void onGetEntityCollision(World world, Entity entity, Vec3d min, Vec3d max, Box box, Predicate<Entity> predicate, float margin, CallbackInfoReturnable<EntityHitResult> cir) {
        if (entity instanceof ProjectileEntity projectile) {
            EntityHitResult dragonHit = DragonCollisionUtil.raycastDragonPartForProjectile(projectile, min, max);
            if (dragonHit != null) {
                cir.setReturnValue(dragonHit);
            }
        }
    }
}