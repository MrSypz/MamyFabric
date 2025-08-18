package com.sypztep.mamy.mixin.core.classes.correctequipment;

import com.sypztep.mamy.common.util.ClassEquipmentUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {

    /**
     * Prevent crossbow usage if player can't use it or if it's broken
     */
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void preventUseIfRestricted(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        ItemStack stack = user.getStackInHand(hand);

        if (ClassEquipmentUtil.handleRestriction(user, stack, "use")) {
            cir.setReturnValue(TypedActionResult.fail(stack));
        }
    }
}