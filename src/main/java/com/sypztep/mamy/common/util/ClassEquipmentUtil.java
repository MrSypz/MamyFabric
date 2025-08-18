package com.sypztep.mamy.common.util;

import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.init.ModDataComponents;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.init.ModTags;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.item.Item;

import java.util.Map;

/**
 * Centralized utility for handling class-based equipment restrictions
 */
public class ClassEquipmentUtil {

    // Mapping of class IDs to their allowed equipment tags
    private static final Map<String, TagKey<Item>> CLASS_EQUIPMENT_MAP = Map.of(
            "novice", ModTags.Items.NOVICE_EQUIPMENT,
            "swordman", ModTags.Items.SWORDMAN_EQUIPMENT,
            "mage", ModTags.Items.MAGE_EQUIPMENT,
            "archer", ModTags.Items.ARCHER_EQUIPMENT,
            "acolyte", ModTags.Items.ACOLYTE_EQUIPMENT,
            "thief", ModTags.Items.THIEF_EQUIPMENT
    );

    /**
     * Check if a player can use a specific item based on their class
     * @param player The player to check
     * @param itemStack The item to check
     * @return true if the player can use the item, false otherwise
     */
    public static boolean canPlayerUseItem(PlayerEntity player, ItemStack itemStack) {
        if (player == null || itemStack.isEmpty()) {
            return true;
        }

        // All players can use tools (items in ALL_CLASSES tag)
        if (itemStack.isIn(ModTags.Items.ALL_CLASSES)) {
            return true;
        }

        // Check if item is broken - broken items cannot be used
        if (isBroken(itemStack)) {
            return false;
        }

        // Get player's current class
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);
        PlayerClass playerClass = classComponent.getClassManager().getCurrentClass();

        // Get the equipment tag for this class
        TagKey<Item> allowedEquipment = CLASS_EQUIPMENT_MAP.get(playerClass.getId());
        if (allowedEquipment == null) {
            return false; // Unknown class
        }

        // Check if the item is in the allowed equipment list for this class
        return itemStack.isIn(allowedEquipment);
    }
    /**
     * Get the display name of the player's current class
     */
    public static String getPlayerClassName(PlayerEntity player) {
        if (player == null) return "Unknown";

        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);
        PlayerClass playerClass = classComponent.getClassManager().getCurrentClass();
        return playerClass.getDisplayName();
    }

    /**
     * Mark an item as broken (cannot be used but won't disappear)
     */
    public static void setBroken(ItemStack itemStack) {
        itemStack.set(ModDataComponents.BROKEN_FLAG, true);
    }

    /**
     * Check if an item is marked as broken
     */
    public static boolean isBroken(ItemStack itemStack) {
        Boolean broken = itemStack.get(ModDataComponents.BROKEN_FLAG);
        return broken != null && broken;
    }

    /**
     * Repair a broken item (remove broken flag)
     */
    public static void repair(ItemStack itemStack) {
        itemStack.remove(ModDataComponents.BROKEN_FLAG);
    }

    /**
     * Prevent item from being damaged if it would break
     * @param itemStack The item being damaged
     * @param damageAmount The amount of damage
     * @return true if damage should be prevented, false if normal damage should occur
     */
    public static boolean shouldPreventDamage(ItemStack itemStack, int damageAmount) {
        if (itemStack.isEmpty() || !itemStack.isDamageable()) {
            return false;
        }

        // If the damage would break the item, mark it as broken instead
        if (itemStack.getDamage() + damageAmount >= itemStack.getMaxDamage()) {
            setBroken(itemStack);
            return true; // Prevent the damage
        }

        return false; // Allow normal damage
    }

    public static Map<String, TagKey<Item>> getClassEquipmentMap() {
        return CLASS_EQUIPMENT_MAP;
    }
}