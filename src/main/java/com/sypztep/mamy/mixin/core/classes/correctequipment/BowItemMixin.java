package com.sypztep.mamy.mixin.core.classes.correctequipment;

import com.llamalad7.mixinextras.sugar.Local;
import com.sypztep.mamy.common.util.ClassEquipmentUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BowItem.class)
public class BowItemMixin {

    @Inject(method = "onStoppedUsing",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getProjectileType(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;", shift = At.Shift.AFTER),
            cancellable = true)
    private void preventShootingIfRestricted(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci, @Local PlayerEntity playerEntity) {
        if (ClassEquipmentUtil.handleRestriction(playerEntity, stack, "shoot with")) {
            ci.cancel();
        }
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void preventUseIfRestricted(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        ItemStack stack = user.getStackInHand(hand);

        if (ClassEquipmentUtil.handleRestriction(user, stack, "use")) {
            cir.setReturnValue(TypedActionResult.fail(stack));
        }
    }
}