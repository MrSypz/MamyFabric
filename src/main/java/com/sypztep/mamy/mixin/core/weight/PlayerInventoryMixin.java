package com.sypztep.mamy.mixin.core.weight;

import com.sypztep.mamy.common.init.ModEntityComponents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {

    @Shadow @Final public PlayerEntity player;
    private ItemStack oldStack = ItemStack.EMPTY;

    @Unique
    private void recalcWeight() {
        if (!player.getWorld().isClient) {
            ModEntityComponents.PLAYERWEIGHT.get(player).recalculateWeight();
        }
    }

    @Inject(method = "setStack", at = @At("HEAD"))
    private void onSetStackPre(int slot, ItemStack stack, CallbackInfo ci) {
        if (!player.getWorld().isClient) {
            this.oldStack = player.getInventory().getStack(slot).copy();
        }
    }
    /**
     * Called whenever a slot is set.
     */
    @Inject(method = "setStack", at = @At("RETURN"))
    private void onSetStackPost(int slot, ItemStack stack, CallbackInfo ci) {
        if (!player.getWorld().isClient) {
            ModEntityComponents.PLAYERWEIGHT.get(player).updateWeightDelta(oldStack, stack);
            this.oldStack = ItemStack.EMPTY; // cleanup
        }
    }

    /**
     * Called when a stack is removed.
     */
    @Inject(method = "removeStack(II)Lnet/minecraft/item/ItemStack;", at = @At("RETURN"))
    private void onRemoveStackWithCount(int slot, int amount, CallbackInfoReturnable<ItemStack> cir) {
        recalcWeight();
    }

    @Inject(method = "removeStack(I)Lnet/minecraft/item/ItemStack;", at = @At("RETURN"))
    private void onRemoveStack(int slot, CallbackInfoReturnable<ItemStack> cir) {
        recalcWeight();
    }

    /**
     * Called when contents change (used internally).
     */
    @Inject(method = "markDirty", at = @At("RETURN"))
    private void onMarkDirty(CallbackInfo ci) {
        recalcWeight();
    }
}