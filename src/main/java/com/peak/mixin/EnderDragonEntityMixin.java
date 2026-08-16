package com.peak.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EnderDragonEntity.class)
public class EnderDragonEntityMixin {
    @ModifyArg(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/mob/MobEntity;<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;)V"
            ),
            index = 0
    )
    private static EntityType<?> passCorrectTypeToSuper(EntityType<?> originalType, @Local(argsOnly = true) EntityType<?> passedType) {
        return passedType;
    }

    @Overwrite
    public static DefaultAttributeContainer.Builder createEnderDragonAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, (double)600.0F);
    }
}
