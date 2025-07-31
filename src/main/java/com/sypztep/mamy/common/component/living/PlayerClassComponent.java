package com.sypztep.mamy.common.component.living;

import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.classes.ClassRegistry;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.classes.PlayerClassManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.CommonTickingComponent;

public class PlayerClassComponent implements AutoSyncedComponent, CommonTickingComponent {
    private final PlayerEntity player;
    private final PlayerClassManager classManager;

    public PlayerClassComponent(PlayerEntity player) {
        this.player = player;
        this.classManager = new PlayerClassManager(player);
    }

    // ====================
    // CORE GETTERS
    // ====================

    public PlayerClassManager getClassManager() {
        return classManager;
    }

    // ====================
    // BATCH OPERATIONS (DOMINATUS STYLE)
    // ====================

    public void performBatchUpdate(Runnable updates) {
        updates.run();
        sync();
    }

    public void addClassExperience(long amount) {
        performBatchUpdate(() -> classManager.addClassExperience(amount));
    }

    public void setLevel(short level) {
        performBatchUpdate(() -> {
            classManager.getClassLevelSystem().setLevel(level);
            classManager.getClassLevelSystem().setExperience(0);
        });
    }

    public boolean evolveToClass(String classId) {
        PlayerClass targetClass = ClassRegistry.getClass(classId);
        if (targetClass == null) return false;

        boolean[] result = {false};
        performBatchUpdate(() -> result[0] = classManager.evolveToClass(targetClass));
        return result[0];
    }

    public boolean useResource(float amount) {
        boolean[] result = {false};
        performBatchUpdate(() -> result[0] = classManager.useResource(amount));
        return result[0];
    }

    public void addResource(float amount) {
        performBatchUpdate(() -> classManager.addResource(amount));
    }

    public void setCurrentResource(float amount) {
        performBatchUpdate(() -> classManager.setCurrentResource(amount));
    }

    // ====================
    // LIFECYCLE
    // ====================

    @Override
    public void tick() {
        if (!player.getWorld().isClient()) {
            boolean needsSync = false;

            float oldResource = classManager.getCurrentResource();
            classManager.tickResourceRegeneration();
            float newResource = classManager.getCurrentResource();

            if (Math.abs(newResource - oldResource) > 1.0f) needsSync = true;

            if (needsSync) sync();
        }
    }

    public void initialize() {
        classManager.initialize();
        sync();
    }

    public void handleRespawn() {
        performBatchUpdate(classManager::handleRespawn);
    }

    // ====================
    // NBT & SYNC
    // ====================

    @Override
    public void writeToNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        classManager.writeToNbt(nbt);
    }

    @Override
    public void readFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        classManager.readFromNbt(nbt);
    }

    private void sync() {
        ModEntityComponents.PLAYERCLASS.sync(this.player);
    }
}