package com.peak.mixin;

import net.minecraft.item.EnderEyeItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderEyeItem.class)
public class EnderEyeItemMixin {
    // TODO: I have no clue if this will work, we need it to not do anything when used on a block
    @Redirect(method = "useOnBlock", at = @At("HEAD"))
    private ActionResult useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        return ActionResult.PASS;
    }
}
