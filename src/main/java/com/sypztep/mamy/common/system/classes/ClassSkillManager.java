package com.sypztep.mamy.common.system.classes;

import com.sypztep.mamy.client.payload.SendToastPayloadS2C;
import com.sypztep.mamy.common.system.skill.Skill;
import com.sypztep.mamy.common.system.skill.SkillRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.*;

public class ClassSkillManager {
    private final PlayerEntity player;
    private final Map<Identifier, Integer> skillLevels = new HashMap<>(); // Skill ID -> Level
    private final Identifier[] boundSkills = new Identifier[8]; // 8 skill slots

    public ClassSkillManager(PlayerEntity player) {
        this.player = player;
        initializeBasicSkills();
    }

    // ====================
    // INITIALIZATION
    // ====================

    private void initializeBasicSkills() {
        // Auto-learn basic attack for all players (free skill)
        learnSkill(SkillRegistry.FIRSTAID, true);
//        bindSkill(0, SkillRegistry.FIRSTAID);
    }

    // ====================
    // SKILL LEARNING & UPGRADING
    // ====================

    /**
     * Learn a skill at level 1 using class points
     */
    public boolean learnSkill(Identifier skillId, PlayerClassManager classManager) {
        return learnSkill(skillId, false, classManager);
    }

    /**
     * Learn a skill with option to make it free (for basic skills)
     */
    private boolean learnSkill(Identifier skillId, boolean free, PlayerClassManager classManager) {
        if (hasLearnedSkill(skillId)) return false;

        Skill skill = SkillRegistry.getSkill(skillId);
        if (skill == null) return false;

        if (!free) {
            if (classManager == null) return false;

            // Check if player's class can learn this skill
            if (!skill.isAvailableForClass(classManager.getCurrentClass())) return false;

            // Check if player has enough class points
            int cost = skill.getBaseClassPointCost();
            if (classManager.getClassStatPoints() < cost) return false;

            // Spend class points
            classManager.getClassLevelSystem().subtractStatPoints((short) cost);
        }

        skillLevels.put(skillId, 1);

        if (player instanceof ServerPlayerEntity serverPlayer && !free) {
            SendToastPayloadS2C.sendInfo(serverPlayer, "Learned: " + skill.getName());
        }

        return true;
    }

    public boolean unlearnSkill(Identifier skillId, PlayerClassManager classManager) {
        if (!hasLearnedSkill(skillId)) return false;

        Skill skill = SkillRegistry.getSkill(skillId);
        int currentLevel = getSkillLevel(skillId);

        if (skill == null) return false;

        if (skill.isDefaultSkill() && currentLevel <= 1) {
            return false; // Can't unlearn default skills completely
        }

        if (currentLevel <= 1) {
            // Remove skill completely if at level 1
            skillLevels.remove(skillId);

            // Unbind from all slots
            for (int i = 0; i < boundSkills.length; i++) {
                if (skillId.equals(boundSkills[i])) {
                    boundSkills[i] = null;
                }
            }

            // Return the base learning cost
            classManager.getClassLevelSystem().addStatPoints((short) skill.getBaseClassPointCost());
        } else {
            // Reduce skill level by 1
            skillLevels.put(skillId, currentLevel - 1);

            // Return the upgrade cost
            classManager.getClassLevelSystem().addStatPoints((short) skill.getUpgradeClassPointCost());
        }

        if (player instanceof ServerPlayerEntity serverPlayer) {
            if (currentLevel <= 1) {
                SendToastPayloadS2C.sendInfo(serverPlayer, "Unlearned: " + skill.getName());
            } else {
                SendToastPayloadS2C.sendInfo(serverPlayer,
                        skill.getName() + " downgraded to level " + (currentLevel - 1));
            }
        }

        return true;
    }

    /**
     * Free learning method for basic skills
     */
    private void learnSkill(Identifier skillId, boolean free) {
        if (hasLearnedSkill(skillId)) return;
        if (free) skillLevels.put(skillId, 1);
    }

    /**
     * Upgrade a skill to the next level using class points
     */
    public boolean upgradeSkill(Identifier skillId, PlayerClassManager classManager) {
        if (!hasLearnedSkill(skillId)) return false;

        Skill skill = SkillRegistry.getSkill(skillId);
        if (skill == null) return false;

        int currentLevel = getSkillLevel(skillId);
        if (currentLevel >= skill.getMaxSkillLevel()) return false; // Already at max level

        int cost = skill.getUpgradeClassPointCost();
        if (classManager.getClassStatPoints() < cost) return false;

        // Spend class points
        classManager.getClassLevelSystem().subtractStatPoints((short) cost);
        skillLevels.put(skillId, currentLevel + 1);

        if (player instanceof ServerPlayerEntity serverPlayer) {
            SendToastPayloadS2C.sendInfo(serverPlayer,
                    "Upgraded " + skill.getName() + " to level " + (currentLevel + 1));
        }

        return true;
    }

    /**
     * Check if a skill is learned
     */
    public boolean hasLearnedSkill(Identifier skillId) {
        return skillLevels.containsKey(skillId);
    }

    /**
     * Get the level of a learned skill
     */
    public int getSkillLevel(Identifier skillId) {
        return skillLevels.getOrDefault(skillId, 0);
    }

    /**
     * Get all learned skills
     */
    public Set<Identifier> getLearnedSkills() {
        return new HashSet<>(skillLevels.keySet());
    }

    // ====================
    // SKILL BINDING
    // ====================

    public boolean bindSkill(int slot, Identifier skillId) {
        if (slot < 0 || slot >= 8) return false;

        if (skillId == null) {
            boundSkills[slot] = null;
            return true;
        }

        if (!hasLearnedSkill(skillId)) return false;
        boundSkills[slot] = skillId;
        return true;
    }

    public Identifier getBoundSkill(int slot) {
        if (slot < 0 || slot >= 8) return null;
        return boundSkills[slot];
    }

    public Identifier[] getAllBoundSkills() {
        return Arrays.copyOf(boundSkills, boundSkills.length);
    }

    public void clearAllSkillSlots() {
        Arrays.fill(boundSkills, null);
    }

    // ====================
    // CLASS CHANGE HANDLING
    // ====================

    /**
     * Custom logic to add the class skill
     * @param newClassId
     * @param level
     */
    public void onClassChange(String newClassId, int level) {
        // No auto-learning on class change anymore
        // Players must manually learn skills with class points
    }

    public void onTranscendence() {
        clearAllSkillSlots();
        // Re-bind basic attack
        bindSkill(0, SkillRegistry.FIRSTAID);
    }

    // ====================
    // NBT SERIALIZATION
    // ====================

    public void writeToNbt(NbtCompound nbt) {
        // Save skill levels
        NbtCompound skillLevelsNbt = new NbtCompound();
        for (Map.Entry<Identifier, Integer> entry : skillLevels.entrySet()) {
            skillLevelsNbt.putInt(entry.getKey().toString(), entry.getValue());
        }
        nbt.put("SkillLevels", skillLevelsNbt);

        // Save bound skills
        NbtList boundSkillsNbt = new NbtList();
        for (Identifier skillId : boundSkills) {
            if (skillId != null) {
                boundSkillsNbt.add(NbtString.of(skillId.toString()));
            } else {
                boundSkillsNbt.add(NbtString.of(""));
            }
        }
        nbt.put("BoundSkills", boundSkillsNbt);
    }

    public void readFromNbt(NbtCompound nbt) {
        // Read skill levels
        skillLevels.clear();
        if (nbt.contains("SkillLevels")) {
            NbtCompound skillLevelsNbt = nbt.getCompound("SkillLevels");
            for (String key : skillLevelsNbt.getKeys()) {
                try {
                    Identifier skillId = Identifier.of(key);
                    int level = skillLevelsNbt.getInt(key);
                    skillLevels.put(skillId, level);
                } catch (Exception e) {
                    // Skip invalid entries
                }
            }
        }

        // Read bound skills
        Arrays.fill(boundSkills, null);
        if (nbt.contains("BoundSkills")) {
            NbtList boundSkillsNbt = nbt.getList("BoundSkills", 8);
            for (int i = 0; i < Math.min(boundSkillsNbt.size(), boundSkills.length); i++) {
                String skillIdStr = boundSkillsNbt.getString(i);
                if (!skillIdStr.isEmpty()) {
                    try {
                        boundSkills[i] = Identifier.of(skillIdStr);
                    } catch (Exception e) {
                        // Skip invalid entries
                    }
                }
            }
        }

        // Ensure basic attack is still learned
        if (!hasLearnedSkill(SkillRegistry.FIRSTAID)) {
            learnSkill(SkillRegistry.FIRSTAID, true);
        }
    }
}
