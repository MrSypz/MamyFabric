package com.sypztep.mamy.common.system.passive;

import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.init.ModPassiveAbilities;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.*;

/**
 * Manages passive abilities for players - tracks which are unlocked and applies effects
 */
public class PassiveAbilityManager {
    private final PlayerEntity player;
    private final Set<String> unlockedAbilities;
    private final Set<String> activeAbilities;

    public PassiveAbilityManager(PlayerEntity player) {
        this.player = player;
        this.unlockedAbilities = new HashSet<>();
        this.activeAbilities = new HashSet<>();
    }

    /**
     * Check if player has unlocked a specific ability
     */
    public static boolean isUnlocked(PlayerEntity player, PassiveAbility ability) {
        return getManager(player).isUnlocked(ability);
    }

    /**
     * Check if player has a specific ability active
     */
    public static boolean isActive(PlayerEntity player, PassiveAbility ability) {
        return getManager(player).isActive(ability);
    }

    /**
     * Get the manager instance for a player (stored in level component)
     */
    private static PassiveAbilityManager getManager(PlayerEntity player) {
        LivingLevelComponent component = ModEntityComponents.LIVINGLEVEL.get(player);
        return component.getPassiveAbilityManager();
    }

    /**
     * Check if this manager has unlocked the ability
     */
    public boolean isUnlocked(PassiveAbility ability) {
        return unlockedAbilities.contains(ability.getId());
    }

    /**
     * Check if this manager has the ability active
     */
    public boolean isActive(PassiveAbility ability) {
        return activeAbilities.contains(ability.getId());
    }

    /**
     * Update all passive abilities - check for new unlocks and apply/remove effects
     */
    public void updatePassiveAbilities() {
        for (PassiveAbility ability : ModPassiveAbilities.getAllAbilities()) updateAbility(ability);
    }
    /**
     * Update all passive abilities by Stats - check for new unlocks and apply/remove effects
     */
    public void updatePassiveAbilitiesForStat(StatTypes changedStatType) {
        for (PassiveAbility ability : ModPassiveAbilities.getAllAbilities())
            if (ability.getRequirements().containsKey(changedStatType)) updateAbility(ability);
    }

    /**
     * Update a specific ability
     */
    private void updateAbility(PassiveAbility ability) {
        boolean meetsRequirements = ability.meetsRequirements(player);
        boolean wasUnlocked = unlockedAbilities.contains(ability.getId());
        boolean wasActive = activeAbilities.contains(ability.getId());

        if (meetsRequirements && !wasUnlocked) {
            // Newly unlocked!
            unlockedAbilities.add(ability.getId());
            activeAbilities.add(ability.getId());
            ability.applyEffects(player);

            // Send notification (you can customize this)
            sendUnlockNotification(ability);

        } else if (!meetsRequirements && wasActive) {
            // No longer meets requirements, deactivate
            activeAbilities.remove(ability.getId());
            ability.removeEffects(player);

        } else if (meetsRequirements && wasUnlocked && !wasActive) {
            // Re-activate if requirements are met again
            activeAbilities.add(ability.getId());
            ability.applyEffects(player);
        }
    }

    /**
     * Force refresh all active abilities (useful after respawn)
     */
    public void refreshActiveAbilities() {
        for (String abilityId : new HashSet<>(activeAbilities)) {
            PassiveAbility ability = ModPassiveAbilities.getAbility(abilityId);
            if (ability != null) {
                ability.removeEffects(player);
                if (ability.meetsRequirements(player)) {
                    ability.applyEffects(player);
                } else {
                    activeAbilities.remove(abilityId);
                }
            }
        }
    }

    /**
     * Get all unlocked abilities
     */
    public List<PassiveAbility> getUnlockedAbilities() {
        return unlockedAbilities.stream()
                .map(ModPassiveAbilities::getAbility)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Get all active abilities
     */
    public List<PassiveAbility> getActiveAbilities() {
        return activeAbilities.stream()
                .map(ModPassiveAbilities::getAbility)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Send unlock notification to player
     */
    private void sendUnlockNotification(PassiveAbility ability) {
        // Using your toast system
        if (player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
            com.sypztep.mamy.client.payload.SendToastPayloadS2C.sendInfo(
                    serverPlayer,
                    "Passive Ability Unlocked: " + ability.getDisplayName().getString()
            );
        }
    }

    /**
     * Manual unlock for admin commands
     */
    public void forceUnlock(PassiveAbility ability) {
        if (!unlockedAbilities.contains(ability.getId())) {
            unlockedAbilities.add(ability.getId());
            if (ability.meetsRequirements(player)) {
                activeAbilities.add(ability.getId());
                ability.applyEffects(player);
            }
        }
    }

    /**
     * Manual lock for admin commands
     */
    public void forceLock(PassiveAbility ability) {
        if (activeAbilities.contains(ability.getId())) {
            ability.removeEffects(player);
            activeAbilities.remove(ability.getId());
        }
        unlockedAbilities.remove(ability.getId());
    }

    /**
     * Reset all abilities
     */
    public void resetAll() {
        for (String abilityId : new HashSet<>(activeAbilities)) {
            PassiveAbility ability = ModPassiveAbilities.getAbility(abilityId);
            if (ability != null) {
                ability.removeEffects(player);
            }
        }
        unlockedAbilities.clear();
        activeAbilities.clear();
    }

    // NBT serialization
    public void writeToNbt(NbtCompound tag) {
        NbtList unlockedList = new NbtList();
        for (String abilityId : unlockedAbilities) {
            unlockedList.add(NbtString.of(abilityId));
        }
        tag.put("UnlockedAbilities", unlockedList);

        NbtList activeList = new NbtList();
        for (String abilityId : activeAbilities) {
            activeList.add(NbtString.of(abilityId));
        }
        tag.put("ActiveAbilities", activeList);
    }

    public void readFromNbt(NbtCompound tag) {
        unlockedAbilities.clear();
        activeAbilities.clear();

        if (tag.contains("UnlockedAbilities")) {
            NbtList unlockedList = tag.getList("UnlockedAbilities", 8); // STRING type
            for (int i = 0; i < unlockedList.size(); i++) {
                unlockedAbilities.add(unlockedList.getString(i));
            }
        }

        if (tag.contains("ActiveAbilities")) {
            NbtList activeList = tag.getList("ActiveAbilities", 8); // STRING type
            for (int i = 0; i < activeList.size(); i++) {
                activeAbilities.add(activeList.getString(i));
            }
        }
    }
}