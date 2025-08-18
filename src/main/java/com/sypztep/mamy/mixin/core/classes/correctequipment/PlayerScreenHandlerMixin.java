package com.sypztep.mamy.mixin.core.classes.correctequipment;

import com.sypztep.mamy.common.util.ClassEquipmentUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerScreenHandler.class)
public abstract class PlayerScreenHandlerMixin extends ScreenHandler {

    protected PlayerScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    /**
     * Prevent invalid items from being placed in equipment slots via shift-click
     */
    @Inject(method = "quickMove", at = @At("HEAD"), cancellable = true)
    private void preventInvalidQuickMove(PlayerEntity player, int slotIndex, CallbackInfoReturnable<ItemStack> cir) {
        Slot slot = this.slots.get(slotIndex);
        ItemStack stack = slot.getStack();

        if (stack.isEmpty()) return;

        // Check if this item would go to an armor slot when shift-clicked
        if (isArmorItem(stack) || isShieldItem(stack)) {
            if (ClassEquipmentUtil.isBroken(stack)) {
                player.sendMessage(Text.literal("Cannot equip broken items!")
                        .formatted(Formatting.RED), true);
                cir.setReturnValue(ItemStack.EMPTY);
                return;
            }

            if (!ClassEquipmentUtil.canPlayerUseItem(player, stack)) {
                String className = ClassEquipmentUtil.getPlayerClassName(player);
                player.sendMessage(Text.literal(className + " cannot use this equipment!")
                        .formatted(Formatting.RED), true);
                cir.setReturnValue(ItemStack.EMPTY);
            }
        }
    }
    private boolean isArmorItem(ItemStack stack) {
        return stack.getItem() instanceof net.minecraft.item.ArmorItem;
    }

    private boolean isShieldItem(ItemStack stack) {
        return stack.getItem() instanceof net.minecraft.item.ShieldItem;
    }
}