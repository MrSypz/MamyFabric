package com.sypztep.mamy.common.component.living;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import org.ladysnake.cca.api.v3.component.Component;

public class StealComponent implements Component {
    private LivingEntity livingEntity;

    public StealComponent(LivingEntity livingEntity) {
        this.livingEntity = livingEntity;
    }

    private boolean hasBeenStolenFrom = false;

    public boolean hasBeenStolenFrom() {
        return hasBeenStolenFrom;
    }

    public void markAsStolen() {
        this.hasBeenStolenFrom = true;
    }

    public void reset() {
        this.hasBeenStolenFrom = false;
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        hasBeenStolenFrom = tag.getBoolean("hasBeenStolenFrom");
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putBoolean("hasBeenStolenFrom", hasBeenStolenFrom);
    }
}