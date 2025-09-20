package com.sypztep.mamy.common.component.living;

import com.sypztep.mamy.common.init.ModEntityComponents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public final class LivingHidingComponent implements AutoSyncedComponent, ServerTickingComponent {
    private final LivingEntity living;
    private BlockPos hiddingPos = null;
    public LivingHidingComponent(LivingEntity obj) {
        this.living = obj;
    }
    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        if (tag.contains("HiddingPos")) {
            hiddingPos = BlockPos.fromLong(tag.getLong("HiddingPos"));
        } else {
            hiddingPos = null;
        }
    }
    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        if (hiddingPos != null) {
            tag.putLong("HiddingPos", hiddingPos.asLong());
        }
    }

    @Override
    public void serverTick() {
        if (hiddingPos != null) {
            if (living.getVelocity() != Vec3d.ZERO) {
                living.setVelocity(Vec3d.ZERO);
                living.velocityModified = true;
            }
            living.setInvisible(true);
        }
    }

    public void sync() {
        ModEntityComponents.HIDING.sync(living);
    }
    public BlockPos getHiddingPos() {
        return hiddingPos;
    }
    public void setHiddingPos(BlockPos hiddingPos) {
        this.hiddingPos = hiddingPos;
    }
    public void unHidden() {
        setHiddingPos(null);
        sync();
    }
}