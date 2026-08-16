package com.peak.mixin;

import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.DragonFireballEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;

@Mixin(DragonFireballEntity.class)
public class DragonFireballEntityMixin {
    @ModifyArg(method = "onCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/AreaEffectCloudEntity;setRadius(F)V"))
    private float reshapeAreaRadius(float original) {
        return original * 2F;
    }

    @ModifyArg(method = "onCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/AreaEffectCloudEntity;addEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)V"))
    private StatusEffectInstance reshapeAreaDamage(StatusEffectInstance original) {
        return new StatusEffectInstance(StatusEffects.INSTANT_DAMAGE, 2, 1);
    }
}
