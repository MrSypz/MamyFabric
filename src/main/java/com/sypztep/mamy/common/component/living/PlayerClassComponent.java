package com.sypztep.mamy.common.component.living;

import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.classes.ClassLevelSystem;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public class PlayerClassComponent implements AutoSyncedComponent {
    private final PlayerEntity player;
    private final ClassLevelSystem classLevelSystem;
    private PlayerClass currentClass = PlayerClass.NOVICE;


    public PlayerClassComponent(PlayerEntity player) {
        this.player = player;
        this.classLevelSystem = new ClassLevelSystem(player);
    }
    // ====================
    // NBT & SYNC
    // ====================
    @Override
    public void readFromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {
        classLevelSystem.readFromNbt(nbtCompound);
    }

    @Override
    public void writeToNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {
        classLevelSystem.writeToNbt(nbtCompound);
    }
    private void sync() {
        ModEntityComponents.PLAYERCLASS.sync(this.player);
    }
}
