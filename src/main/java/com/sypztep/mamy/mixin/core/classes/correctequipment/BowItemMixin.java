package com.sypztep.mamy.mixin.core.classes.correctequipment;

import com.llamalad7.mixinextras.sugar.Local;
import com.sypztep.mamy.common.util.ClassEquipmentUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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

    /**
     * Prevent bow from shooting if player can't use it or if it's broken
     */
    @Inject(method = "onStoppedUsing",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getProjectileType(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;", shift = At.Shift.AFTER),
            cancellable = true)
    private void preventShootingIfRestricted(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci, @Local PlayerEntity playerEntity) {
        // Check if item is broken
        if (ClassEquipmentUtil.isBroken(stack)) {
            ci.cancel();
            if (!world.isClient) {
                playerEntity.sendMessage(Text.literal("This bow is broken and cannot be used!")
                        .formatted(Formatting.RED), true);
            }
            return;
        }

        // Check if player can use this bow
        if (!ClassEquipmentUtil.canPlayerUseItem(playerEntity, stack)) {
            ci.cancel();
            if (!world.isClient) {
                String className = ClassEquipmentUtil.getPlayerClassName(playerEntity);
                playerEntity.sendMessage(Text.literal(className + " cannot use this weapon!")
                        .formatted(Formatting.RED), true);
            }
        }
    }

    /**
     * Prevent bow usage (starting to draw) if player can't use it or if it's broken
     */
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void preventUseIfRestricted(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        ItemStack stack = user.getStackInHand(hand);

        // Check if item is broken
        if (ClassEquipmentUtil.isBroken(stack)) {
            if (!world.isClient) {
                user.sendMessage(Text.literal("This bow is broken and cannot be used!")
                        .formatted(Formatting.RED), true);
            }
            cir.setReturnValue(TypedActionResult.fail(stack));
            return;
        }

        // Check if player can use this bow
        if (!ClassEquipmentUtil.canPlayerUseItem(user, stack)) {
            if (!world.isClient) {
                String className = ClassEquipmentUtil.getPlayerClassName(user);
                user.sendMessage(Text.literal(className + " cannot use this weapon!")
                        .formatted(Formatting.RED), true);
            }
            cir.setReturnValue(TypedActionResult.fail(stack));
        }
    }
}