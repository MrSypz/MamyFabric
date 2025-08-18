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
     * Prevent crossbow usage (starting to charge or shooting) if player can't use it or if it's broken
     */
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void preventUseIfRestricted(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        ItemStack stack = user.getStackInHand(hand);

        // Check if item is broken
        if (ClassEquipmentUtil.isBroken(stack)) {
            if (!world.isClient) {
                user.sendMessage(Text.literal("This crossbow is broken and cannot be used!")
                        .formatted(Formatting.RED), true);
            }
            cir.setReturnValue(TypedActionResult.fail(stack));
            return;
        }

        // Check if player can use this crossbow
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