package com.sypztep.mamy.mixin.core.weight;

import com.sypztep.mamy.common.component.living.PlayerWeightComponent;
import com.sypztep.mamy.common.data.ItemWeightEntry;
import com.sypztep.mamy.common.init.ModEntityComponents;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
    @Shadow private int pickupDelay;

    @Inject(method = "onPlayerCollision", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerInventory;insertStack(Lnet/minecraft/item/ItemStack;)Z"),
            cancellable = true)
    private void preventOverweightPickup(PlayerEntity player, CallbackInfo ci) {
        if (player.getWorld().isClient || this.pickupDelay != 0) return;

        ItemEntity itemEntity = (ItemEntity) (Object) this;
        ItemStack itemStack = itemEntity.getStack();
        PlayerWeightComponent weightComponent = ModEntityComponents.PLAYERWEIGHT.get(player);

        double totalWeight = ItemWeightEntry.getTotalWeight(itemStack);
        double availableWeight = weightComponent.getMaxWeight() * 1.1 -  weightComponent.getCurrentWeight();

        if (totalWeight <= availableWeight) return; // Full pickup is fine

        int maxPickupCount = (int) Math.floor(availableWeight / (totalWeight / itemStack.getCount()));

        if (maxPickupCount <= 0) {
            ci.cancel(); // Block pickup entirely
            return;
        }

        // Partial pickup
        ItemStack pickupStack = itemStack.copy();
        pickupStack.setCount(maxPickupCount);

        if (player.getInventory().insertStack(pickupStack)) {
            int remainingCount = itemStack.getCount() - maxPickupCount;
            if (remainingCount > 0)
                itemStack.setCount(remainingCount);
             else
                itemEntity.discard();

            // Trigger pickup effects
            player.sendPickup(itemEntity, maxPickupCount);
            player.increaseStat(Stats.PICKED_UP.getOrCreateStat(itemStack.getItem()), maxPickupCount);
            player.triggerItemPickedUpByEntityCriteria(itemEntity);

            // Update weight
            pickupStack.setCount(maxPickupCount);
            weightComponent.updateWeightDelta(ItemStack.EMPTY, pickupStack);
        }
        ci.cancel();
    }
}