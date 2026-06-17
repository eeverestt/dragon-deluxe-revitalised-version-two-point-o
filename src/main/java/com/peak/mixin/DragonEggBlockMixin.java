package com.peak.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.peak.manager.game.DragonFightStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.DragonEggBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DragonEggBlock.class)
public class DragonEggBlockMixin {
    @WrapMethod(method = "onUse")
    private ActionResult dragon$preventUseUntilDead(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit, Operation<ActionResult> original) {
        if (!DragonFightStateManager.getState().equals(DragonFightStateManager.State.UNSPAWNED)) {
            return ActionResult.FAIL;
        }

        return original.call(state, world, pos, player, hit);
    }
}
