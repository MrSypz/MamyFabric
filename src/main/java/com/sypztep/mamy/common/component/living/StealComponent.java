package com.sypztep.mamy.common.component.living;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import org.ladysnake.cca.api.v3.component.Component;

public final class StealComponent implements Component {

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