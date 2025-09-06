package com.sypztep.mamy.common.system.classes;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.ModConfig;
import com.sypztep.mamy.client.payload.SendToastPayloadS2C;
import com.sypztep.mamy.common.system.skill.PassiveSkill;
import com.sypztep.mamy.common.system.skill.Skill;
import com.sypztep.mamy.common.system.skill.SkillRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.stream.Collectors;

public class ClassSkillManager {
    private final PlayerEntity player;
    private final Map<Identifier, Integer> skillLevels = new HashMap<>(); // Skill ID, Skill Level
    private final Identifier[] boundSkills = new Identifier[8]; // 8 skill slots

    public ClassSkillManager(PlayerEntity player) {
        this.player = player;
    }

    // ====================
    // SKILL LEARNING & UPGRADING
    // ====================

    /**
     * Learn a skill at level 1 using class points
     */
    public void learnSkill(Identifier skillId, PlayerClassManager classManager) {
        learnSkill(skillId, false, classManager);
    }

    /**
     * Learn a skill with option to make it free (for basic skills)
     */
    private void learnSkill(Identifier skillId, boolean free, PlayerClassManager classManager) {
        if (hasLearnedSkill(skillId)) return;

        Skill skill = SkillRegistry.getSkill(skillId);
        if (skill == null) return;

        if (!free) {
            if (classManager == null) return;

            if (!skill.isAvailableForClass(classManager.getCurrentClass())) return;

            if (!skill.arePrerequisitesMet(player)) {
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    List<Skill.SkillRequirement> missing = skill.getMissingPrerequisites(player);
                    StringBuilder message = new StringBuilder("Missing prerequisites: ");

                    for (int i = 0; i < missing.size(); i++) {
                        Skill.SkillRequirement req = missing.get(i);
                        Skill prerequisiteSkill = SkillRegistry.getSkill(req.skillId());

                        if (prerequisiteSkill != null) {
                            message.append(prerequisiteSkill.getName()).append(" Lv.").append(req.minLevel());
                            if (i < missing.size() - 1) message.append(", ");
                        }
                    }

                    SendToastPayloadS2C.sendError(serverPlayer, message.toString());
                }
                return;
            }

            int cost = skill.getBaseClassPointCost();
            if (classManager.getClassStatPoints() < cost) return;

            classManager.getClassLevelSystem().subtractStatPoints((short) cost);
        }

        skillLevels.put(skillId, 1);

        if (skill instanceof PassiveSkill passiveSkill) passiveSkill.applyPassiveEffects(player, 1);

        if (player instanceof ServerPlayerEntity serverPlayer && !free)
            SendToastPayloadS2C.sendInfo(serverPlayer, "Learned: " + skill.getName());
    }

    public void unlearnSkill(Identifier skillId, PlayerClassManager classManager) {
        if (!hasLearnedSkill(skillId)) return;

        if (!ModConfig.unlearnskill) {
            if (player instanceof ServerPlayerEntity serverPlayer) SendToastPayloadS2C.sendError(serverPlayer, "Skill unlearning is disabled!");
            return;
        }

        Skill skill = SkillRegistry.getSkill(skillId);
        int currentLevel = getSkillLevel(skillId);

        if (skill == null) return;

        if (skillId.equals(SkillRegistry.BASICSKILL)) {
            if (player instanceof ServerPlayerEntity serverPlayer) SendToastPayloadS2C.sendInfo(serverPlayer, "Basic Skill cannot be unlearned!");
            return;
        }

        if (skill.isDefaultSkill() && currentLevel <= 1)
            return; // Can't unlearn default skills completely

        List<Skill> dependentSkills = findDependentSkills(skillId, currentLevel);
        if (!dependentSkills.isEmpty()) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
                StringBuilder message = new StringBuilder("Cannot unlearn! Required by: ");
                for (int i = 0; i < dependentSkills.size(); i++) {
                    message.append(dependentSkills.get(i).getName());
                    if (i < dependentSkills.size() - 1) message.append(", ");
                }
                SendToastPayloadS2C.sendError(serverPlayer, message.toString());
            }
            return;
        }

        if (currentLevel <= 1) {
            if (skill instanceof PassiveSkill passiveSkill)
                passiveSkill.removePassiveEffects(player);

            // Remove skill completely if at level 1
            skillLevels.remove(skillId);

            // Unbind from all slots
            for (int i = 0; i < boundSkills.length; i++)
                if (skillId.equals(boundSkills[i])) boundSkills[i] = null;

            // Return the base learning cost
            classManager.getClassLevelSystem().addStatPoints((short) skill.getBaseClassPointCost());
        } else {
            // Reduce skill level by 1 and reapply passive effects with new level
            int newLevel = currentLevel - 1;
            skillLevels.put(skillId, newLevel);

            if (skill instanceof PassiveSkill passiveSkill) passiveSkill.applyPassiveEffects(player, newLevel);

            // Return the upgrade cost
            classManager.getClassLevelSystem().addStatPoints((short) skill.getUpgradeClassPointCost());
        }

        if (player instanceof ServerPlayerEntity serverPlayer) {
            if (currentLevel <= 1) SendToastPayloadS2C.sendInfo(serverPlayer, "Unlearned: " + skill.getName());
             else SendToastPayloadS2C.sendInfo(serverPlayer, skill.getName() + " downgraded to level " + (currentLevel - 1));
        }
    }

    private List<Skill> findDependentSkills(Identifier skillId, int currentLevel) {
        List<Skill> dependentSkills = new ArrayList<>();

        Set<Identifier> learnedSkills = getLearnedSkills(true);

        for (Identifier learnedSkillId : learnedSkills) {
            if (learnedSkillId.equals(skillId)) continue;

            Skill learnedSkill = SkillRegistry.getSkill(learnedSkillId);
            if (learnedSkill == null) continue;

            for (Skill.SkillRequirement req : learnedSkill.getPrerequisites()) {
                if (req.skillId().equals(skillId)) {
                    int newLevel = (currentLevel <= 1) ? 0 : currentLevel - 1;
                    if (newLevel < req.minLevel()) {
                        dependentSkills.add(learnedSkill);
                        break; // Found dependency, no need to check other requirements
                    }
                }
            }
        }

        return dependentSkills;
    }
    /**
     * Upgrade a skill to the next level using class points
     */
    public void upgradeSkill(Identifier skillId, PlayerClassManager classManager) {
        if (!hasLearnedSkill(skillId)) return;

        Skill skill = SkillRegistry.getSkill(skillId);
        if (skill == null) return;

        int currentLevel = getSkillLevel(skillId);
        if (currentLevel >= skill.getMaxSkillLevel()) return; // Already at max level

        int cost = skill.getUpgradeClassPointCost();
        if (classManager.getClassStatPoints() < cost) return;

        // Spend class points
        classManager.getClassLevelSystem().subtractStatPoints((short) cost);
        int newLevel = currentLevel + 1;
        skillLevels.put(skillId, newLevel);

        // Reapply passive effects with new level
        if (skill instanceof PassiveSkill passiveSkill) passiveSkill.applyPassiveEffects(player, newLevel);

        if (player instanceof ServerPlayerEntity serverPlayer) {
            SendToastPayloadS2C.sendInfo(serverPlayer, "Upgraded " + skill.getName() + " to level " + newLevel);
        }
    }
    @Deprecated
    public void reapplyPassiveSkills() {
        for (Map.Entry<Identifier, Integer> entry : skillLevels.entrySet()) {
            Skill skill = SkillRegistry.getSkill(entry.getKey());
            if (skill instanceof PassiveSkill passiveSkill)
                passiveSkill.applyPassiveEffects(player, entry.getValue());
        }
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
    public Set<Identifier> getLearnedSkills(boolean allowPassive) {
        if (allowPassive) {
            return new HashSet<>(skillLevels.keySet());
        } else {
            return skillLevels.keySet().stream().filter(skillId -> {
                Skill skill = SkillRegistry.getSkill(skillId);
                return skill != null && !(skill instanceof PassiveSkill);
            }).collect(Collectors.toSet());
        }
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
     *
     * @param newClassId
     * @param level
     */
    @Deprecated
    public void onClassChange(String newClassId, int level) {
        // No auto-learning on class change anymore
        // Players must manually learn skills with class points
    }

    public void onTranscendence() {
        clearAllSkillSlots();
        // No auto-rebinding
    }

    // ====================
    // NBT SERIALIZATION
    // ====================

    public void writeToNbt(NbtCompound nbt) {
        // Save skill levels
        NbtCompound skillLevelsNbt = new NbtCompound();
        for (Map.Entry<Identifier, Integer> entry : skillLevels.entrySet())
            skillLevelsNbt.putInt(entry.getKey().toString(), entry.getValue());

        nbt.put("SkillLevels", skillLevelsNbt);

        // Save bound skills
        NbtList boundSkillsNbt = new NbtList();
        for (Identifier skillId : boundSkills) {
            if (skillId != null)
                boundSkillsNbt.add(NbtString.of(skillId.toString()));
             else
                boundSkillsNbt.add(NbtString.of(""));
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

                    if (SkillRegistry.getSkill(skillId) != null)
                        skillLevels.put(skillId, level);
                     else
                        Mamy.LOGGER.info("[ClassSkillManager] Skipped unknown skill: {}", skillId);

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
                        Identifier skillId = Identifier.of(skillIdStr);

                        if (SkillRegistry.getSkill(skillId) != null && hasLearnedSkill(skillId))
                            boundSkills[i] = skillId;
                         else
                            Mamy.LOGGER.info("[ClassSkillManager] Removed invalid bound skill: {}", skillId);

                    } catch (Exception e) {
                        // Skip invalid entries
                    }
                }
            }
        }
    }
}