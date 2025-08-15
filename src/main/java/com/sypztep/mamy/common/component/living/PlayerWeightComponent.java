package com.sypztep.mamy.common.component.living;

import com.sypztep.mamy.common.data.ItemWeightEntry;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.init.ModEntityComponents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public class PlayerWeightComponent implements AutoSyncedComponent {
    private final PlayerEntity player;
    private double currentWeight = 0.0;

    public PlayerWeightComponent(PlayerEntity player) {
        this.player = player;
    }

    public double getCurrentWeight() {
        return currentWeight;
    }

    public void recalculateWeight() {
        double total = 0.0;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty()) {
                total += ItemWeightEntry.getTotalWeight(stack);
            }
        }
        this.currentWeight = total;
        ModEntityComponents.PLAYERWEIGHT.sync(player);
    }
    public void updateWeightDelta(ItemStack oldStack, ItemStack newStack) {
        double oldWeight = oldStack.isEmpty() ? 0.0 : ItemWeightEntry.getTotalWeight(oldStack);
        double newWeight = newStack.isEmpty() ? 0.0 : ItemWeightEntry.getTotalWeight(newStack);

        this.currentWeight += (newWeight - oldWeight);

        ModEntityComponents.PLAYERWEIGHT.sync(player);
    }


    public double getMaxWeight() {
        return player.getAttributeValue(ModEntityAttributes.MAX_WEIGHT);
    }

    public boolean isOverburdened() {
        return currentWeight > getMaxWeight();
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.currentWeight = tag.getFloat("CurrentWeight");
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        float rounded = Math.round(currentWeight * 10f) / 10f; // keep 1 decimal
        tag.putFloat("CurrentWeight", rounded);
    }
}
