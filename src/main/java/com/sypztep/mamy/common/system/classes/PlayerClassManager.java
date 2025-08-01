package com.sypztep.mamy.common.system.classes;

import com.sypztep.mamy.ModConfig;
import com.sypztep.mamy.client.payload.SendToastPayloadS2C;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.system.stat.Stat;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public class PlayerClassManager {
    private final PlayerEntity player;
    private final ClassLevelSystem classLevelSystem;
    private PlayerClass currentClass;
    private boolean hasTranscended; // Track if player has ever transcended

    // Resource system
    private float currentResource;
    private int resourceRegenTick = 0;
    private static final int RESOURCE_REGEN_INTERVAL = 100; // 5 second 20 = 1 Sec

    // Future: Class skills
    private final ClassSkillManager skillManager; // For future class skills

    public PlayerClassManager(PlayerEntity player) {
        this.player = player;
        this.classLevelSystem = new ClassLevelSystem(player, currentClass);
        this.currentClass = ClassRegistry.getDefaultClass(); // NOVICE
        this.currentResource = currentClass.getMaxResource();
        this.hasTranscended = false;
        this.skillManager = new ClassSkillManager(player);
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

    public long getClassExperienceToNext() {
        return classLevelSystem.getExperienceToNextLevel();
    }

    public boolean isClassMaxLevel() {
        return classLevelSystem.isMaxLevel();
    }

    public short getClassStatPoints() {
        return classLevelSystem.getStatPoints();
    }

    public boolean hasTranscended() {
        return hasTranscended;
    }

    public void addClassExperience(long amount) {
        if (amount <= 0) return;

        int oldLevel = classLevelSystem.getLevel();
        classLevelSystem.addExperience(amount);
        int newLevel = classLevelSystem.getLevel();

        if (newLevel > oldLevel) {
            onClassLevelUp(newLevel);
            skillManager.onLevelUp(currentClass.getId(), newLevel);
        }
    }

    public boolean canEvolveTo(PlayerClass targetClass) {
        return targetClass.canEvolveFrom(currentClass, getClassLevel());
    }

    public List<PlayerClass> getAvailableEvolutions() {
        return ClassRegistry.getAvailableEvolutions(currentClass, getClassLevel());
    }

    public List<PlayerClass> getAvailableTranscendence() {
        return ClassRegistry.getAvailableTranscendence(currentClass, getClassLevel());
    }

    public boolean canTranscend() {
        return !getAvailableTranscendence().isEmpty();
    }

    public boolean evolveToClass(PlayerClass newClass) {
        if (!canEvolveTo(newClass)) return false;

        if (newClass.isTranscendent() && newClass.canTranscendFrom(currentClass, getClassLevel()))
            return transcendToClass(newClass);


        // Normal evolution
        return normalEvolveToClass(newClass);
    }

    private boolean normalEvolveToClass(PlayerClass newClass) {
        // Remove old class attributes
        currentClass.removeAttributeModifiers(player);

        PlayerClass oldClass = currentClass;
        currentClass = newClass;
        classLevelSystem.updateForClass(currentClass);

        // Apply new class attributes
        currentClass.applyAttributeModifiers(player);

        // Reset resource to full for new class
        currentResource = getMaxResource();

        // Reset stats when changing class (but keep class level)
        resetStatsForClassChange();

        // Notify skill manager about class change
        skillManager.onClassChange(newClass.getId(), getClassLevel());

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

    private boolean transcendToClass(PlayerClass targetClass) {
        if (!targetClass.isTranscendent()) return false;

        // Remove current class modifiers
        if (currentClass != null) currentClass.removeAttributeModifiers(player);

        PlayerClass oldClass = currentClass;
        currentClass = targetClass;
        hasTranscended = true;

        // Reset level and experience (like RO transcendence)
        classLevelSystem.resetForTranscendence();
        classLevelSystem.updateForClass(currentClass);

        // Apply new class modifiers
        currentClass.applyAttributeModifiers(player);

        // Reset resource to full
        currentResource = targetClass.getMaxResource();

        // Reset all stats to base values (transcendence resets everything)
        resetAllStatsForTranscendence();
        // Notify skill manager about transcendence
        skillManager.onTranscendence();

        // Send transcendence message
        if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.sendMessage(Text.literal("✨ TRANSCENDENCE ✨").formatted(Formatting.GOLD, Formatting.BOLD), false);
            Text transcendMessage = Text.literal("You have transcended from ")
                    .append(oldClass.getFormattedName())
                    .append(Text.literal(" to "))
                    .append(targetClass.getFormattedName())
                    .append(Text.literal("! You are reborn with greater potential!"));
            serverPlayer.sendMessage(transcendMessage, false);
        }

        return true;
    }

    public void setClass(PlayerClass newClass) {
        if (currentClass != null) {
            currentClass.removeAttributeModifiers(player);
        }

        currentClass = newClass;
        classLevelSystem.updateForClass(currentClass);
        currentClass.applyAttributeModifiers(player);
        currentResource = currentClass.getMaxResource();
    }

    // Reset stats when changing class (not transcending)
    private void resetStatsForClassChange() {
        LivingLevelComponent levelComponent = ModEntityComponents.LIVINGLEVEL.getNullable(player);
        if (levelComponent == null) return;

        // Return stat points and reset stats (but don't reset main level stats)
        for (StatTypes statType : StatTypes.values()) {
            Stat stat = levelComponent.getStatByType(statType);
            if (stat != null) {
                // Return the points used for class-specific stats
                short pointsUsed = stat.getTotalPointsUsed();
                classLevelSystem.addStatPoints(pointsUsed);

                // Reset to base value
                stat.reset(player, classLevelSystem, false);
            }
        }
    }

    // Reset everything for transcendence
    private void resetAllStatsForTranscendence() {
        LivingLevelComponent levelComponent = ModEntityComponents.LIVINGLEVEL.getNullable(player);
        if (levelComponent == null) return;

        // Reset all stats to base values (no point return for transcendence)
        for (StatTypes statType : StatTypes.values()) {
            Stat stat = levelComponent.getStatByType(statType);
            if (stat != null) {
                stat.reset(player, classLevelSystem, false);
                levelComponent.getLevelSystem().setStatPoints((short) (ModConfig.startStatpoints * 4)); // Trancendence start with 4 time point
            }
        }
    }

    private void onClassLevelUp(int newLevel) {
        if (player instanceof ServerPlayerEntity serverPlayer)
            SendToastPayloadS2C.sendClassLevelUp(serverPlayer, newLevel, currentClass.getDisplayName());
    }
    // ====================
    // SKILL SYSTEM DELEGATION - เพิ่มใหม่
    // ====================

    // Convenience methods - delegate to skill manager
    public boolean learnSkill(Identifier skillId) {
        return skillManager.learnSkill(skillId);
    }

    public boolean hasLearnedSkill(Identifier skillId) {
        return skillManager.hasLearnedSkill(skillId);
    }

    public List<Identifier> getLearnedSkills() {
        return skillManager.getLearnedSkills();
    }

    public boolean bindSkill(int slot, Identifier skillId) {
        return skillManager.bindSkill(slot, skillId);
    }

    public Identifier getBoundSkill(int slot) {
        return skillManager.getBoundSkill(slot);
    }

    public Identifier[] getAllBoundSkills() {
        return skillManager.getAllBoundSkills();
    }

    // ====================
    // RESOURCE MANAGEMENT
    // ====================

    public ResourceType getResourceType() {
        return currentClass.getPrimaryResource();
    }

    public float getMaxResource() {
        return currentClass.getMaxResource(this.player);
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

    public float getCurrentResourcePercentage() {
        return currentResource / getMaxResource();
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
            currentResource = Math.min(currentResource, getMaxResource());
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
        nbt.putBoolean("HasTranscended", hasTranscended);

        classLevelSystem.writeToNbt(nbt);

        nbt.putFloat("CurrentResource", currentResource);
        nbt.putInt("ResourceRegenTick", resourceRegenTick);

        // Future: Save class skills
        NbtCompound skillsTag = new NbtCompound();
        skillManager.writeToNbt(skillsTag);
        nbt.put("ClassSkills", skillsTag);
    }

    public void readFromNbt(NbtCompound nbt) {
        String classId = nbt.getString("CurrentClass");
        PlayerClass loadedClass = ClassRegistry.getClass(classId);
        if (loadedClass != null) {
            currentClass = loadedClass;
        } else {
            currentClass = ClassRegistry.getDefaultClass();
        }

        hasTranscended = nbt.getBoolean("HasTranscended");

        classLevelSystem.updateForClass(currentClass);
        classLevelSystem.readFromNbt(nbt);

        // Load resource state
        currentResource = nbt.getFloat("CurrentResource");
        resourceRegenTick = nbt.getInt("ResourceRegenTick");

        // Ensure resource is within valid bounds
        currentResource = Math.min(currentResource, getMaxResource());

        if (nbt.contains("ClassSkills")) {
            skillManager.readFromNbt(nbt.getCompound("ClassSkills"));
        }
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

    public boolean isReadyForTranscendence() {
        return getClassLevel() >= 45 && !getAvailableTranscendence().isEmpty();
    }

    public float getClassProgressPercentage() {
        return classLevelSystem.getExperiencePercentage();
    }

    // Future getter for class skills
    public ClassSkillManager getSkillManager() {
        return skillManager;
    }
}