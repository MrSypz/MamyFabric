package com.sypztep.mamy.common.component.living;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.data.ItemWeightEntry;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.init.ModEntityComponents;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public class PlayerWeightComponent implements AutoSyncedComponent {
    private final PlayerEntity player;
    private double currentWeight = 0.0;
    private int currentTier = 0;
    private boolean modifierApplied = false;

    private static final String WEIGHT_PENALTY_ID = "weight_penalty";

    public PlayerWeightComponent(PlayerEntity player) {
        this.player = player;
    }

    public double getCurrentWeight() { return currentWeight; }

    public void recalculateWeight() {
        if (player.getWorld().isClient) return;

        double total = 0.0;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty()) total += ItemWeightEntry.getTotalWeight(stack);
        }

        if (Math.abs(this.currentWeight - total) > 0.01) {
            this.currentWeight = total;
            updateSpeedPenalty();
            ModEntityComponents.PLAYERWEIGHT.sync(player);
        }
    }

    public void updateWeightDelta(ItemStack oldStack, ItemStack newStack) {
        if (player.getWorld().isClient) return;

        double oldWeight = oldStack.isEmpty() ? 0.0 : ItemWeightEntry.getTotalWeight(oldStack);
        double newWeight = newStack.isEmpty() ? 0.0 : ItemWeightEntry.getTotalWeight(newStack);

        if (Math.abs(oldWeight - newWeight) < 0.01) return;

        this.currentWeight += (newWeight - oldWeight);
        updateSpeedPenalty();
        ModEntityComponents.PLAYERWEIGHT.sync(player);
    }

    public double getMaxWeight() { return player.getAttributeValue(ModEntityAttributes.MAX_WEIGHT); }

    public int getWeightTier() {
        double maxWeight = getMaxWeight();
        if (maxWeight <= 0) return 0;
        float percentage = (float) (currentWeight / maxWeight);
        if (percentage < 1.0f) return 0;
        if (percentage >= 2.0f) return 10;
        return (int) Math.floor((percentage - 1.0f) * 10) + 1;
    }

    private void updateSpeedPenalty() {
        if (player.getWorld().isClient) return;
        int newTier = getWeightTier();
        if (newTier != currentTier) {
            removeSpeedPenalty();
            if (newTier > 0) applySpeedPenalty(newTier);
            currentTier = newTier;
        }
    }

    private void applySpeedPenalty(int tier) {
        if (tier <= 0 || modifierApplied) return;
        double penaltyValue = -Math.min(tier * 0.09, 0.9);
        EntityAttributeModifier modifier = new EntityAttributeModifier(Mamy.id(WEIGHT_PENALTY_ID), penaltyValue, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        EntityAttributeInstance speedAttribute = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (speedAttribute != null && !speedAttribute.hasModifier(modifier.id())) {
            speedAttribute.addPersistentModifier(modifier);
            modifierApplied = true;
        }
    }

    private void removeSpeedPenalty() {
        if (!modifierApplied) return;
        EntityAttributeInstance speedAttribute = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (speedAttribute != null) {
            speedAttribute.removeModifier(Mamy.id(WEIGHT_PENALTY_ID));
            modifierApplied = false;
        }
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.currentWeight = tag.getFloat("CurrentWeight");
        this.currentTier = tag.getInt("CurrentTier");
        this.modifierApplied = tag.getBoolean("ModifierApplied");
        if (!player.getWorld().isClient && currentTier > 0) updateSpeedPenalty();
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        float rounded = Math.round(currentWeight * 10f) / 10f;
        tag.putFloat("CurrentWeight", rounded);
        tag.putInt("CurrentTier", currentTier);
        tag.putBoolean("ModifierApplied", modifierApplied);
    }
}