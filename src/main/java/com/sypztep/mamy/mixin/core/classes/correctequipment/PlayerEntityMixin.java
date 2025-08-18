package com.sypztep.mamy.mixin.core.classes.correctequipment;

import com.sypztep.mamy.common.init.ModTags;
import com.sypztep.mamy.common.util.ClassEquipmentUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
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

        // Skip universal tools
        if (mainHandStack.isIn(ModTags.Items.ALL_CLASSES)) {
            return;
        }

        if (ClassEquipmentUtil.handleRestriction(player, mainHandStack, "attack with")) {
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

        if (ClassEquipmentUtil.handleRestriction(player, stack, "equip")) {
            ci.cancel();
        }
    }
}