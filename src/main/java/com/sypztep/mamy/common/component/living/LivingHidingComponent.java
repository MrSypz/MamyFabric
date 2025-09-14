package com.sypztep.mamy.common.component.living;

import com.sypztep.mamy.common.init.ModEntityComponents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.CommonTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class LivingHidingComponent implements AutoSyncedComponent, ServerTickingComponent {
    private final LivingEntity obj;
    private BlockPos hiddingPos = null;
    public LivingHidingComponent(LivingEntity obj) {
        this.obj = obj;
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
            if (obj.getX() != hiddingPos.getX() + 0.5 || obj.getY() != hiddingPos.getY() + 1 || obj.getZ() != hiddingPos.getZ() + 0.5)
                obj.refreshPositionAfterTeleport(hiddingPos.getX() + 0.5, hiddingPos.getY(), hiddingPos.getZ() + 0.5);

            if (obj.getVelocity() != Vec3d.ZERO) {
                obj.setVelocity(Vec3d.ZERO);
                obj.velocityModified = true;
            }
            obj.setInvisible(true);
        }
    }

    public void sync() {
        ModEntityComponents.HIDING.sync(obj);
    }
    public BlockPos getHiddingPos() {
        return hiddingPos;
    }
    public void setHiddingPos(BlockPos hiddingPos) {
        this.hiddingPos = hiddingPos;
    }
    public void unbury() {
        setHiddingPos(null);
        sync();
    }
}