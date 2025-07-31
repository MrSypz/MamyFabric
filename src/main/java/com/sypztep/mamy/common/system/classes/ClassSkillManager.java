package com.sypztep.mamy.common.system.classes;

import com.sypztep.mamy.client.payload.SendToastPayloadS2C;
import com.sypztep.mamy.common.system.skill.Skill;
import com.sypztep.mamy.common.system.skill.SkillRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ClassSkillManager {
    private final PlayerEntity player;
    private final List<Identifier> learnedSkills = new ArrayList<>();
    private final Identifier[] boundSkills = new Identifier[8]; // 8 skill slots

    public ClassSkillManager(PlayerEntity player) {
        this.player = player;
        initializeBasicSkills();
    }

    // ====================
    // INITIALIZATION
    // ====================

    private void initializeBasicSkills() {
        // Auto-learn basic attack for all players
        learnSkill(SkillRegistry.BASIC_ATTACK);

        // Bind basic attack to slot 0 (Z key)
        bindSkill(0, SkillRegistry.BASIC_ATTACK);
    }

    // ====================
    // SKILL LEARNING
    // ====================

    public boolean learnSkill(Identifier skillId) {
        if (learnedSkills.contains(skillId)) return false;

        learnedSkills.add(skillId);
        return true;
    }

    public boolean hasLearnedSkill(Identifier skillId) {
        return learnedSkills.contains(skillId);
    }

    public List<Identifier> getLearnedSkills() {
        return new ArrayList<>(learnedSkills);
    }

    public void autoLearnClassSkills(String classId, int level) {
        List<Skill> availableSkills = SkillRegistry.getSkillsForLevel(classId, level);

        for (Skill skill : availableSkills) {
            if (!hasLearnedSkill(skill.getId())) {
                learnSkill(skill.getId());
            }
        }
    }

    // ====================
    // SKILL BINDING
    // ====================

    public boolean bindSkill(int slot, Identifier skillId) {
        if (slot < 0 || slot >= 8) return false;
        if (skillId != null && !hasLearnedSkill(skillId)) return false;

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

    public void clearSkillSlot(int slot) {
        bindSkill(slot, null);
    }

    public void clearAllSkillSlots() {
        Arrays.fill(boundSkills, null);
    }

    // ====================
    // UTILITY
    // ====================

    public int getLearnedSkillCount() {
        return learnedSkills.size();
    }

    public int getBoundSkillCount() {
        return (int) Arrays.stream(boundSkills).filter(Objects::nonNull).count();
    }

    public boolean hasAnyBoundSkills() {
        return getBoundSkillCount() > 0;
    }

    public List<Identifier> getUnboundSkills() {
        List<Identifier> unbound = new ArrayList<>(learnedSkills);
        for (Identifier bound : boundSkills) {
            unbound.remove(bound);
        }
        return unbound;
    }

    // ====================
    // CLASS CHANGE HANDLING
    // ====================

    public void onClassChange(String newClassId, int level) {
        autoLearnClassSkills(newClassId, level);
    }

    public void onLevelUp(String classId, int newLevel) {
        List<Skill> newSkills = SkillRegistry.getSkillsUnlockedAtLevel(classId, newLevel);

        for (Skill skill : newSkills) {
            if (learnSkill(skill.getId())) {
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    SendToastPayloadS2C.sendInfo(serverPlayer, "Learned new skill: " + skill.getName());
                }
            }
        }
    }

    public void onTranscendence() {
        clearAllSkillSlots();

        // Re-bind basic attack
        bindSkill(0, SkillRegistry.BASIC_ATTACK);
    }

    // ====================
    // NBT SERIALIZATION
    // ====================

    public void writeToNbt(NbtCompound nbt) {
        NbtList learnedSkillsNbt = new NbtList();
        for (Identifier skillId : learnedSkills) {
            learnedSkillsNbt.add(NbtString.of(skillId.toString()));
        }
        nbt.put("LearnedSkills", learnedSkillsNbt);

        // Bound skills
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
        learnedSkills.clear();
        Arrays.fill(boundSkills, null);

        // Learned skills
        if (nbt.contains("LearnedSkills")) {
            NbtList learnedSkillsNbt = nbt.getList("LearnedSkills", NbtElement.STRING_TYPE);
            for (int i = 0; i < learnedSkillsNbt.size(); i++) {
                String skillIdStr = learnedSkillsNbt.getString(i);
                try {
                    learnedSkills.add(Identifier.of(skillIdStr));
                } catch (Exception e) {
                    // Skip invalid skill IDs
                }
            }
        }

        // Bound skills
        if (nbt.contains("BoundSkills")) {
            NbtList boundSkillsNbt = nbt.getList("BoundSkills", NbtElement.STRING_TYPE);
            for (int i = 0; i < Math.min(boundSkillsNbt.size(), 8); i++) {
                String skillIdStr = boundSkillsNbt.getString(i);
                if (!skillIdStr.isEmpty()) {
                    try {
                        boundSkills[i] = Identifier.of(skillIdStr);
                    } catch (Exception e) {
                        // Skip invalid skill IDs
                    }
                }
            }
        }

        // Ensure basic skills are always available
        if (!hasLearnedSkill(SkillRegistry.BASIC_ATTACK)) {
            initializeBasicSkills();
        }
    }
}