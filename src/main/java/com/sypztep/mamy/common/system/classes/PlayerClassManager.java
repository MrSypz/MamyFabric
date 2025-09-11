package com.sypztep.mamy.common.system.classes;

import com.sypztep.mamy.ModConfig;
import com.sypztep.mamy.common.network.client.SendToastPayloadS2C;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.system.level.ClassLevelSystem;
import com.sypztep.mamy.common.system.skill.SkillRegistry;
import com.sypztep.mamy.common.system.stat.Stat;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

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
    private int passiveResourceRegenTick = 0;

    private final ClassSkillManager skillManager;

    public PlayerClassManager(PlayerEntity player) {
        this.player = player;
        this.currentClass = ClassRegistry.getDefaultClass(); // NOVICE
        this.classLevelSystem = new ClassLevelSystem(player, currentClass);
        this.currentResource = currentClass.getBaseMaxResource();
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

        if (newLevel > oldLevel) onClassLevelUp(newLevel);
    }

    public boolean canEvolveTo(PlayerClass targetClass) {
        if (!targetClass.canEvolveFrom(currentClass, getClassLevel())) {
            return false;
        }

        if (currentClass.getTier() == 0) {
            return getClassLevel() == currentClass.getMaxLevel() // Max lvl = 10
                    && getSkillLevel(SkillRegistry.BASICSKILL) >= 10;
        } else {
            return getClassLevel() >= 45
                    && getClassStatPoints() == 0;
        }
    }

    public List<PlayerClass> getAvailableEvolutions() {
        return ClassRegistry.getAvailableEvolutions(currentClass);
    }

    public List<PlayerClass> getAvailableTranscendence() {
        return ClassRegistry.getAvailableTranscendence(currentClass, getClassLevel());
    }

    public boolean canTranscend() {
        return !getAvailableTranscendence().isEmpty();
    }

    public void applyJobBonusesToStats(PlayerEntity player) {
        LivingLevelComponent statsComponent = ModEntityComponents.LIVINGLEVEL.getNullable(player);
        if (statsComponent == null) return;

        statsComponent.performBatchUpdate(() -> {
            if (currentClass != null) {
                PlayerClass.JobBonuses progressiveBonuses = currentClass.getProgressiveJobBonuses(getClassLevel());

                statsComponent.getStatByType(StatTypes.STRENGTH).setClassBonus(progressiveBonuses.str());
                statsComponent.getStatByType(StatTypes.AGILITY).setClassBonus(progressiveBonuses.agi());
                statsComponent.getStatByType(StatTypes.VITALITY).setClassBonus(progressiveBonuses.vit());
                statsComponent.getStatByType(StatTypes.INTELLIGENCE).setClassBonus(progressiveBonuses.intel());
                statsComponent.getStatByType(StatTypes.DEXTERITY).setClassBonus(progressiveBonuses.dex());
                statsComponent.getStatByType(StatTypes.LUCK).setClassBonus(progressiveBonuses.luk());

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
        currentClass.applyClassAttributeModifiers(player);
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
        classLevelSystem.resetForTranscendence();// for future me it's not going to reset class point
//        classLevelSystem.updateForClass(currentClass);  // it using resetFor

        // Apply new class modifiers
        currentClass.applyClassAttributeModifiers(player);

        // Reset resource to full
        currentResource = targetClass.getMaxResource(player);

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
        currentClass.applyClassAttributeModifiers(player);
        currentResource = currentClass.getMaxResource(player);
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
        applyJobBonusesToStats(player);

        if (player instanceof ServerPlayerEntity serverPlayer) {
            SendToastPayloadS2C.sendClassLevelUp(serverPlayer, newLevel, currentClass.getDisplayName());

            // Optional: Show job bonus increase notification
            if (newLevel > 1) {
                PlayerClass.JobBonuses oldBonuses = currentClass.getProgressiveJobBonuses(newLevel - 1);
                PlayerClass.JobBonuses newBonuses = currentClass.getProgressiveJobBonuses(newLevel);

                if (!oldBonuses.equals(newBonuses)) {
                    SendToastPayloadS2C.sendInfo(serverPlayer, "Job bonuses increased!");
                }
            }
        }
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
    public Set<Identifier> getLearnedSkills(boolean allowPassive) {
        return skillManager.getLearnedSkills(allowPassive);
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
        this.currentResource = Math.clamp(amount, 0, getMaxResource());
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
        return max > 0 ? (currentResource / max) : 0f;
    }

    /**
     * Handle resource regeneration - called from component tick
     */
    public void tickResourceRegeneration() {
        resourceRegenTick++;
        passiveResourceRegenTick++;

        double regenRateTick = player.getAttributeValue(ModEntityAttributes.RESOURCE_REGEN_RATE) * 20; // 8 sec regen rate
        float maxResource = getMaxResource();
        if (currentResource > getMaxResource()) currentResource = maxResource;

        if (resourceRegenTick >= regenRateTick) {
            resourceRegenTick = 0; // This resets to 0

            if (currentResource < maxResource) {
                double regenAmount = player.getAttributeValue(ModEntityAttributes.RESOURCE_REGEN);
                addResource((float) regenAmount);
            }
        }

        if (passiveResourceRegenTick >= 200) { // 200 ticks = 10 seconds
            passiveResourceRegenTick = 0; // same as before :)

            boolean isIdle = player.getVelocity().lengthSquared() < 0.01;
            if (isIdle && currentResource < maxResource) {
                double passiveRegen = player.getAttributeValue(ModEntityAttributes.PASSIVE_RESOURCE_REGEN);
                if (passiveRegen > 0) {
                    addResource((float) passiveRegen);
                }
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
            currentClass.applyClassAttributeModifiers(player);
            skillManager.reapplyPassiveSkills();
            currentResource = Math.min(currentResource, this.getMaxResource());
        }
    }
    public void respawn() {
        if (currentClass != null) {
            currentClass.applyClassAttributeModifiers(player);
            skillManager.reapplyPassiveSkills();
            currentResource = this.getMaxResource();
        }
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
        nbt.putInt("PassiveResourceRegenTick", passiveResourceRegenTick);

        // Save class skills
        NbtCompound skillsTag = new NbtCompound();
        skillManager.writeToNbt(skillsTag);
        nbt.put("ClassSkills", skillsTag);
    }

    public void readFromNbt(NbtCompound nbt) {
        String classId = nbt.getString("CurrentClass");
        PlayerClass loadedClass = ClassRegistry.getClass(classId);
        currentClass = loadedClass != null ? loadedClass : ClassRegistry.getDefaultClass();

        hasTranscended = nbt.getBoolean("HasTranscended");

        classLevelSystem.updateForClass(currentClass);
        classLevelSystem.readFromNbt(nbt);

        // Load resource state - DON'T clamp yet!
        currentResource = nbt.getFloat("CurrentResource");
        resourceRegenTick = nbt.getInt("ResourceRegenTick");
        passiveResourceRegenTick = nbt.getInt("PassiveResourceRegenTick");

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
        if (currentClass.getTier() == 0) {
            boolean levelReady = getClassLevel() >= 10;
            boolean basicSkillReady = getSkillLevel(SkillRegistry.BASICSKILL) >= 10;
            boolean hasEvolutions = !getAvailableEvolutions().isEmpty();

            return levelReady && basicSkillReady && hasEvolutions;
        } else {
            boolean levelReady = getClassLevel() >= 45;
            boolean skillPointsSpent = getClassStatPoints() == 0;
            boolean hasEvolutions = !getAvailableEvolutions().isEmpty();

            return levelReady && skillPointsSpent && hasEvolutions;
        }
    }

    public boolean isReadyForTranscendence() {
        if (currentClass.getTier() == 0) {
            return false;
        } else {
            boolean levelReady = getClassLevel() >= 45;
            boolean skillPointsSpent = getClassStatPoints() == 0;
            boolean hasTranscendence = !getAvailableTranscendence().isEmpty();

            return levelReady && skillPointsSpent && hasTranscendence;
        }
    }

    public int getEvolutionRequiredLevel() {
        if (currentClass.getTier() == 0) return currentClass.getMaxLevel(); // novice max lvl are 10
        return 45; // All other classes
    }


    public boolean reachCap() {
        return currentClass.getTier() != 0;
    }

    public ClassSkillManager getSkillManager() {
        return skillManager;
    }
}