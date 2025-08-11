package com.sypztep.mamy.mixin.vanilla.passive.shadowstep;

import com.sypztep.mamy.common.init.ModPassiveAbilities;
import com.sypztep.mamy.common.system.passive.PassiveAbilityManager;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Inject(method = "playStepSound", at = @At("HEAD"), cancellable = true)
    private void cancelFootstepSound(BlockPos pos, BlockState state, CallbackInfo ci) {
        if (PassiveAbilityManager.isActive((PlayerEntity) (Object) this, ModPassiveAbilities.SHADOW_STEP)) ci.cancel(); // Cancel the footstep sound completely TODO: make warden not hear player walk
    }
}
