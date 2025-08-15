package com.sypztep.mamy.mixin.core.weight;

import com.sypztep.mamy.common.init.ModEntityComponents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Inject(method = "sendPickup", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerEntity;sendPickup(Lnet/minecraft/entity/Entity;I)V"))
    private void onSendPickup(Entity item, int count, CallbackInfo ci) {
        if (item instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getStack();
            ItemStack newStack = stack.copy();
            newStack.setCount(count);

            ModEntityComponents.PLAYERWEIGHT.get(this).updateWeightDelta(ItemStack.EMPTY, newStack);
        }
    }
}
