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

public class LivingHidingComponent implements AutoSyncedComponent, CommonTickingComponent {
    private final LivingEntity obj;
    private BlockPos buryPos = null;
    public LivingHidingComponent(LivingEntity obj) {
        this.obj = obj;
    }
    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        if (tag.contains("BuryPos")) {
            buryPos = BlockPos.fromLong(tag.getLong("BuryPos"));
        } else {
            buryPos = null;
        }
    }
    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        if (buryPos != null) {
            tag.putLong("BuryPos", buryPos.asLong());
        }
    }

    @Override
    public void tick() {
        if (buryPos != null) {
            if (obj.getX() != buryPos.getX() + 0.5 || obj.getY() != buryPos.getY() + 1 || obj.getZ() != buryPos.getZ() + 0.5) {
                obj.refreshPositionAfterTeleport(buryPos.getX() + 0.5, buryPos.getY(), buryPos.getZ() + 0.5);
            }
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
    public BlockPos getBuryPos() {
        return buryPos;
    }
    public void setBuryPos(BlockPos buryPos) {
        this.buryPos = buryPos;
    }
    public void unbury() {
        setBuryPos(null);
        sync();
    }
}