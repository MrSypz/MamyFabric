package com.sypztep.mamy.common.system.classes;

import com.sypztep.mamy.client.payload.SendToastPayloadS2C;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * Manages all class-related data for a player
 * Similar to LivingStats but for class system
 */
public class PlayerClassManager {
    private final PlayerEntity player;
    private final ClassLevelSystem classLevelSystem;
    private PlayerClass currentClass;

    // Resource system
    private float currentResource;
    private int resourceRegenTick = 0;
    private static final int RESOURCE_REGEN_INTERVAL = 100; // 5 second 20 = 1 Sec

    // Future: Class skills
//    private ClassSkillManager skillManager; // For future class skills

    public PlayerClassManager(PlayerEntity player) {
        this.player = player;
        this.classLevelSystem = new ClassLevelSystem(player);
        this.currentClass = ClassRegistry.getDefaultClass(); // NOVICE
        this.currentResource = currentClass.getMaxResource();
        // Future: Initialize skill manager
        // this.skillManager = new ClassSkillManager(player);
    }

    // ====================
    // CLASS MANAGEMENT
    // ====================

    public PlayerClass getCurrentClass() {
        return currentClass;
    }

    public ClassLevelSystem getClassLevelSystem() {
        return classLevelSystem;
    }

    public int getClassLevel() {
        return classLevelSystem.getLevel();
    }

    public long getClassExperience() {
        return classLevelSystem.getExperience();
    }

    public void addClassExperience(long amount) {
        if (amount <= 0) return;

        int oldLevel = classLevelSystem.getLevel();
        classLevelSystem.addExperience(amount);
        int newLevel = classLevelSystem.getLevel();

        if (newLevel > oldLevel) {
            onClassLevelUp(newLevel);
        }
    }

    public boolean canEvolveTo(PlayerClass targetClass) {
        return targetClass.canEvolveFrom(currentClass, getClassLevel());
    }

    public List<PlayerClass> getAvailableEvolutions() {
        return ClassRegistry.getAvailableEvolutions(currentClass, getClassLevel());
    }

    public boolean evolveToClass(PlayerClass newClass) {
        if (!canEvolveTo(newClass)) return false;

        // Remove old class attributes
        currentClass.removeAttributeModifiers(player);

        PlayerClass oldClass = currentClass;
        currentClass = newClass;

        // Apply new class attributes
        currentClass.applyAttributeModifiers(player);

        // Reset class level and experience (configurable)
        classLevelSystem.setLevel((short) 1);
        classLevelSystem.setExperience(0);

        // Reset resource to full for new class
        currentResource = currentClass.getMaxResource();

        // Notify player
        if (player instanceof ServerPlayerEntity serverPlayer) {
            Text evolveMessage = Text.literal("Class evolved from ")
                    .formatted(Formatting.GOLD)
                    .append(oldClass.getFormattedName())
                    .append(Text.literal(" to ").formatted(Formatting.GOLD))
                    .append(newClass.getFormattedName())
                    .append(Text.literal("!").formatted(Formatting.GOLD));

            serverPlayer.sendMessage(evolveMessage, false);
        }

        return true;
    }

    public void setClass(PlayerClass newClass) {
        if (currentClass != null) {
            currentClass.removeAttributeModifiers(player);
        }

        currentClass = newClass;
        currentClass.applyAttributeModifiers(player);
        currentResource = currentClass.getMaxResource();
    }

    private void onClassLevelUp(int newLevel) {
        if (player instanceof ServerPlayerEntity serverPlayer)
            SendToastPayloadS2C.sendClassLevelUp(serverPlayer, newLevel, currentClass.getDisplayName());
    }

    // ====================
    // RESOURCE MANAGEMENT
    // ====================

    public ResourceType getResourceType() {
        return currentClass.getPrimaryResource();
    }

    public float getMaxResource() {
        return currentClass.getMaxResource();
    }

    public float getCurrentResource() {
        return currentResource;
    }

    public void setCurrentResource(float amount) {
        this.currentResource = Math.max(0, Math.min(getMaxResource(), amount));
    }

    public boolean useResource(float amount) {
        if (currentResource >= amount) {
            currentResource -= amount;
            return true;
        }
        return false;
    }

    public void addResource(float amount) {
        currentResource = Math.min(getMaxResource(), currentResource + amount);
    }

    public float getResourcePercentage() {
        float max = getMaxResource();
        return max > 0 ? (currentResource / max) * 100f : 0f;
    }

    /**
     * Handle resource regeneration - called from component tick
     */
    public void tickResourceRegeneration() {
        resourceRegenTick++;

        if (resourceRegenTick >= RESOURCE_REGEN_INTERVAL) {
            resourceRegenTick = 0;

            float maxResource = getMaxResource();
            if (currentResource < maxResource) {
                float regenAmount = currentClass.getPrimaryResource().getBaseRegenRate();
                float levelBonus = getClassLevel() * 0.5f; // Bonus regen based on level

                addResource(regenAmount + levelBonus);
            }
        }
    }

    // ====================
    // LIFECYCLE
    // ====================

    /**
     * Initialize class effects - called from event, not tick
     */
    public void initialize() {
        if (currentClass != null) {
            currentClass.applyAttributeModifiers(player);
            currentResource = Math.min(currentResource, currentClass.getMaxResource());
        }
    }

    /**
     * Handle respawn - reapply class effects
     */
    public void handleRespawn() {
        initialize();
        currentResource = getMaxResource() * 0.1f; // 10% resource on respawn
    }

    // ====================
    // NBT
    // ====================

    public void writeToNbt(NbtCompound nbt) {
        nbt.putString("CurrentClass", currentClass.getId());

        classLevelSystem.writeToNbt(nbt);

        nbt.putFloat("CurrentResource", currentResource);
        nbt.putInt("ResourceRegenTick", resourceRegenTick);

        // Future: Save class skills
        // if (skillManager != null) {
        //     NbtCompound skillsTag = new NbtCompound();
        //     skillManager.writeToNbt(skillsTag);
        //     nbt.put("ClassSkills", skillsTag);
        // }
    }

    public void readFromNbt(NbtCompound nbt) {
        String classId = nbt.getString("CurrentClass");
        PlayerClass loadedClass = ClassRegistry.getClass(classId);
        if (loadedClass != null) {
            currentClass = loadedClass;
        } else {
            currentClass = ClassRegistry.getDefaultClass();
        }

        classLevelSystem.readFromNbt(nbt);

        // Load resource state
        currentResource = nbt.getFloat("CurrentResource");
        resourceRegenTick = nbt.getInt("ResourceRegenTick");

        // Ensure resource is within valid bounds
        currentResource = Math.min(currentResource, currentClass.getMaxResource());

        // Future: Load class skills
        // if (skillManager != null && nbt.contains("ClassSkills")) {
        //     skillManager.readFromNbt(nbt.getCompound("ClassSkills"));
        // }
    }

    // ====================
    // UTILITY
    // ====================

    public Text getClassInfo() {
        return Text.literal("[" + currentClass.getClassCode() + "] ")
                .formatted(Formatting.GRAY)
                .append(currentClass.getFormattedName())
                .append(Text.literal(" Lv." + getClassLevel()).formatted(Formatting.WHITE));
    }

    public boolean isReadyForEvolution() {
        return getClassLevel() >= 45 && !getAvailableEvolutions().isEmpty();
    }

    public float getClassProgressPercentage() {
        return classLevelSystem.getExperiencePercentage();
    }

    // Future getter for class skills
//    public ClassSkillManager getSkillManager() {
//        return skillManager;
//    }
}