package com.sypztep.mamy.common.component.living;

import com.sypztep.mamy.common.init.ModEntityComponents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public final class HeadShotEntityComponent implements AutoSyncedComponent {
    private final LivingEntity living;
    private boolean headShot;

    public HeadShotEntityComponent(LivingEntity entity) {
        this.living = entity;
        this.headShot = false;
    }
    public void setHeadShot(boolean headShot) {
        this.headShot = headShot;
        sync();
    }

    public boolean isHeadShot() {
        return headShot;
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.setHeadShot(tag.getBoolean("headShot"));
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putBoolean("headShot", this.headShot);
    }
    private void sync() {
        ModEntityComponents.HEADSHOT.sync(living);
    }
}
