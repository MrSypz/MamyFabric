package com.sypztep.mamy.mixin.vanilla.bowdrawspeed;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BowItem.class)
public class BowItemMixin {
    @WrapOperation(method = "onStoppedUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/BowItem;getPullProgress(I)F"))
    private float wrapPullProgress(int useTicks, Operation<Float> original, ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        float base = original.call(useTicks);

        if (user instanceof PlayerEntity player) {
            if (player.isSneaking()) return base + 1f;
        }

        return base;
    }
}
