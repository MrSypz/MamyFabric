package com.sypztep.mamy.common.system.classes;

import com.sypztep.mamy.ModConfig;
import com.sypztep.mamy.client.payload.SendToastPayloadS2C;
import com.sypztep.mamy.common.init.ModEntityAttributes;
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
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Set;

public class PlayerClassManager {
    private final PlayerEntity player;
    private final ClassLevelSystem classLevelSystem;
    private PlayerClass currentClass;
    private boolean hasTranscended; // Track if player has ever transcended

    // Resource system
    private float currentResource;
    private int resourceRegenTick = 0;

    // NEW: Idle detection for faster regen
    private Vec3d lastPosition;
    private float lastYaw, lastPitch;
    private int idleTicks = 0;
    private static final int IDLE_THRESHOLD = 100; // 5 seconds
    private boolean isIdle = false;

    private final ClassSkillManager skillManager;

    public PlayerClassManager(PlayerEntity player) {
        this.player = player;
        this.currentClass = ClassRegistry.getDefaultClass(); // NOVICE
        this.classLevelSystem = new ClassLevelSystem(player, currentClass);
        this.currentResource = currentClass.getMaxResource();
        this.hasTranscended = false;
        this.skillManager = new ClassSkillManager(player);

        // Initialize idle tracking
        this.lastPosition = player.getPos();
        this.lastYaw = player.getYaw();
        this.lastPitch = player.getPitch();
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

    // UPDATED: Removed auto skill learning
    public void addClassExperience(long amount) {
        if (amount <= 0) return;

        int oldLevel = classLevelSystem.getLevel();
        classLevelSystem.addExperience(amount);
        int newLevel = classLevelSystem.getLevel();

        if (newLevel > oldLevel) {
            onClassLevelUp(newLevel);
            // REMOVED: skillManager.onLevelUp(currentClass.getId(), newLevel);
        }
    }

    public boolean canEvolveTo(PlayerClass targetClass) {
        return targetClass.canEvolveFrom(currentClass, getClassLevel());
    }

    public List<PlayerClass> getAvailableEvolutions() {
//        return ClassRegistry.getAvailableEvolutions(currentClass, getClassLevel());
        return ClassRegistry.getAvailableEvolutions(currentClass, 10);
    }

    public List<PlayerClass> getAvailableTranscendence() {
        return ClassRegistry.getAvailableTranscendence(currentClass, getClassLevel());
    }

    public boolean canTranscend() {
        return !getAvailableTranscendence().isEmpty();
    }

    private void applyJobBonusesToStats(PlayerEntity player) {
        LivingLevelComponent statsComponent = ModEntityComponents.LIVINGLEVEL.getNullable(player);
        if (statsComponent == null) return;
        statsComponent.performBatchUpdate(()-> {
            if (currentClass != null) {
                statsComponent.getStatByType(StatTypes.STRENGTH).setClassBonus(currentClass.getJobBonuses().str());
                statsComponent.getStatByType(StatTypes.AGILITY).setClassBonus(currentClass.getJobBonuses().agi());
                statsComponent.getStatByType(StatTypes.VITALITY).setClassBonus(currentClass.getJobBonuses().vit());
                statsComponent.getStatByType(StatTypes.INTELLIGENCE).setClassBonus(currentClass.getJobBonuses().intel());
                statsComponent.getStatByType(StatTypes.DEXTERITY).setClassBonus(currentClass.getJobBonuses().dex());
                statsComponent.getStatByType(StatTypes.LUCK).setClassBonus(currentClass.getJobBonuses().luk());
                statsComponent.refreshAllStatEffectsInternal();
            }
        });
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
        this.applyJobBonusesToStats(player);

        // Reset resource to full for new class
        currentResource = getMaxResource();

        // Reset stats when changing class (but keep class level)
        resetStatsForClassChange();

        // Notify skill manager about class change NOTE: Custom logic on the class
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
        applyJobBonusesToStats(player);
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
                levelComponent.getLevelSystem().setStatPoints((short) (ModConfig.startStatpoints * 4)); // Transcendence start with 4 time point
            }
        }
    }

    private void onClassLevelUp(int newLevel) {
        if (player instanceof ServerPlayerEntity serverPlayer)
            SendToastPayloadS2C.sendClassLevelUp(serverPlayer, newLevel, currentClass.getDisplayName());
    }

    // ====================
    // UPDATED SKILL SYSTEM DELEGATION
    // ====================

    public void learnSkill(Identifier skillId) {
        skillManager.learnSkill(skillId, this);
    }
    public void upgradeSkill(Identifier skillId) {
        skillManager.upgradeSkill(skillId, this);
    }
    public void unlearnSkill(Identifier skillId) {
        skillManager.unlearnSkill(skillId, this);
    }

    public boolean hasLearnedSkill(Identifier skillId) {
        return skillManager.hasLearnedSkill(skillId);
    }

    // UPDATED: Returns Set instead of List and uses getLearnedSkills()
    public Set<Identifier> getLearnedSkills() {
        return skillManager.getLearnedSkills();
    }

    // NEW: Get skill level
    public int getSkillLevel(Identifier skillId) {
        return skillManager.getSkillLevel(skillId);
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
        return max > 0 ? (currentResource / max): 0f;
    }

    /**
     * Handle resource regeneration - called from component tick
     */
    public void tickResourceRegeneration() {
        // Update idle detection
        updateIdleDetection();

        resourceRegenTick++;

        // Get regen rate from attributes (in seconds)
        double baseRegenRateSeconds = player.getAttributeValue(ModEntityAttributes.RESOURCE_REGEN_RATE);

        // Apply idle bonus (faster regen when idle)
        double effectiveRegenRateSeconds = isIdle ? baseRegenRateSeconds * 0.5 : baseRegenRateSeconds; // 50% faster when idle

        // Convert to ticks
        int regenIntervalTicks = (int)(effectiveRegenRateSeconds * 20);

        if (resourceRegenTick >= regenIntervalTicks) {
            resourceRegenTick = 0;

            float maxResource = getMaxResource();
            if (currentResource < maxResource) {
                double regenAmount = player.getAttributeValue(ModEntityAttributes.RESOURCE_REGEN);

                addResource((float)(regenAmount));

                // Debug message (remove in production)
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    String idleText = isIdle ? " (Idle Bonus)" : "";
                    serverPlayer.sendMessage(Text.literal(String.format("Regenerated %.1f %s%s",
                            regenAmount,
                            currentClass.getPrimaryResource().getDisplayName(),
                            idleText)).formatted(Formatting.GREEN), true);
                }
            }
        }
    }

    /**
     * Track player movement and actions to detect idle state
     */
    private void updateIdleDetection() {
        Vec3d currentPos = player.getPos();
        float currentYaw = player.getYaw();
        float currentPitch = player.getPitch();

        // Check if player moved or rotated
        boolean hasMoved = !currentPos.equals(lastPosition);
        boolean hasRotated = Math.abs(currentYaw - lastYaw) > 1.0f || Math.abs(currentPitch - lastPitch) > 1.0f;

        if (hasMoved || hasRotated) {
            // Player is active
            idleTicks = 0;
            isIdle = false;
        } else {
            idleTicks++;
            if (idleTicks >= IDLE_THRESHOLD && !isIdle)
                isIdle = true;

        }

        // Update tracking variables
        lastPosition = currentPos;
        lastYaw = currentYaw;
        lastPitch = currentPitch;
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
//            this.applyJobBonusesToStats(player);
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

        nbt.putInt("IdleTicks", idleTicks);
        nbt.putBoolean("IsIdle", isIdle);

        // Save class skills
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

        idleTicks = nbt.getInt("IdleTicks");
        isIdle = nbt.getBoolean("IsIdle");

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
        int requiredLevel = getEvolutionRequiredLevel();
        return getClassLevel() >= requiredLevel && !getAvailableEvolutions().isEmpty();
    }

    public boolean isReadyForTranscendence() {
        int requiredLevel = getEvolutionRequiredLevel();
        return getClassLevel() >= requiredLevel && !getAvailableTranscendence().isEmpty();
    }

    public int getEvolutionRequiredLevel() {
        if (currentClass.getTier() == 0) { // Tier 0 = Novice
            return currentClass.getMaxLevel();
        }
        return 45;
    }
    public boolean reachCap() {
        return currentClass.getTier() != 0;
    }

    public float getClassProgressPercentage() {
        return classLevelSystem.getExperiencePercentage();
    }

    // Future getter for class skills
    public ClassSkillManager getSkillManager() {
        return skillManager;
    }
}