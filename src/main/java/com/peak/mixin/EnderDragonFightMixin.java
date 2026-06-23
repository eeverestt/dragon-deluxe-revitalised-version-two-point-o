package com.peak.mixin;

import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderDragonFight.class)
public class EnderDragonFightMixin {
    @Shadow
    @Final
    private ServerBossBar bossBar;

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/boss/ServerBossBar;setVisible(Z)V"))
    private void neverShowDragonEvent(ServerBossBar bar, boolean visible) {
        bar.setPercent(0.0f);
        bar.setVisible(false);
        bar.clearPlayers();
    }

    @Inject(method = "tick", at = @At("TAIL"), cancellable = true)
    private void stopDragonFightTick(CallbackInfo ci) {
        this.bossBar.setPercent(0.0f);
        this.bossBar.setVisible(false);
        this.bossBar.clearPlayers();
    }
}
