package com.sypztep.mamy.mixin.vanilla.disablefoodregen;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.sypztep.mamy.ModConfig;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @ModifyReturnValue(method = "canFoodHeal", at = @At("RETURN"))
    private boolean conditionalFoodHealing(boolean original) {
        return ModConfig.allowVanillaFoodHealing && original;
    }
}
