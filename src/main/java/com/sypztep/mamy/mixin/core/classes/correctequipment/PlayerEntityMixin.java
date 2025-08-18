package com.sypztep.mamy.mixin.core.classes.correctequipment;

import com.sypztep.mamy.common.init.ModTags;
import com.sypztep.mamy.common.util.ClassEquipmentUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    /**
     * Prevent players from attacking with weapons they cannot use or that are broken
     */
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void preventAttackWithInvalidWeapon(Entity target, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        ItemStack mainHandStack = player.getMainHandStack();

        if (mainHandStack.isEmpty()) {
            return;
        }

        if (mainHandStack.isIn(ModTags.Items.ALL_CLASSES)) {
            return;
        }

        if (ClassEquipmentUtil.isBroken(mainHandStack)) {
            if (!player.getWorld().isClient) {
                player.sendMessage(Text.literal("This weapon is broken and cannot be used to attack!")
                        .formatted(Formatting.RED), true);
            }
            ci.cancel();
            return;
        }

        if (!ClassEquipmentUtil.canPlayerUseItem(player, mainHandStack)) {
            if (!player.getWorld().isClient) {
                String className = ClassEquipmentUtil.getPlayerClassName(player);
                player.sendMessage(Text.literal(className + " cannot attack with this weapon!")
                        .formatted(Formatting.RED), true);
            }
            ci.cancel();
        }
    }
    @Inject(method = "equipStack", at = @At("HEAD"), cancellable = true)
    private void preventEquipStack(EquipmentSlot slot, ItemStack stack, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        // Don't restrict if the stack is empty
        if (stack.isEmpty()) {
            return;
        }

        // Check if item is broken
        if (ClassEquipmentUtil.isBroken(stack)) {
            if (!player.getWorld().isClient)
                player.sendMessage(Text.literal("Cannot equip broken items!").formatted(Formatting.RED), true);
            ci.cancel();
            return;
        }

        // Check if player can use this item
        if (!ClassEquipmentUtil.canPlayerUseItem(player, stack)) {
            if (!player.getWorld().isClient) {
                String className = ClassEquipmentUtil.getPlayerClassName(player);
                player.sendMessage(Text.literal(className + " cannot use this equipment!").formatted(Formatting.RED), true);
            }
            ci.cancel();
        }
    }
}