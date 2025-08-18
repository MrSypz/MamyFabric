package com.sypztep.mamy.common.util;

import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.init.ModDataComponents;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.init.ModTags;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ToolComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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

    // Restriction reasons
    public enum RestrictionReason {
        NONE,           // Can use item
        BROKEN,         // Item is broken
        CLASS_RESTRICTED, // Class cannot use item
        UNIVERSAL_TOOL  // Universal tool (can always use)
    }

    /**
     * Check if a player can use an item and get the reason if they can't
     * @param player The player to check
     * @param itemStack The item to check
     * @return RestrictionReason indicating why the item can't be used
     */
    public static RestrictionReason getRestrictionReason(PlayerEntity player, ItemStack itemStack) {
        if (player == null || itemStack.isEmpty()) {
            return RestrictionReason.NONE;
        }

        // All players can use tools (items in ALL_CLASSES tag)
        if (itemStack.isIn(ModTags.Items.ALL_CLASSES)) {
            return RestrictionReason.UNIVERSAL_TOOL;
        }

        // Check if item is broken first
        if (isBroken(itemStack)) {
            return RestrictionReason.BROKEN;
        }

        // Get player's current class
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);
        PlayerClass playerClass = classComponent.getClassManager().getCurrentClass();

        // Get the equipment tag for this class
        TagKey<Item> allowedEquipment = CLASS_EQUIPMENT_MAP.get(playerClass.getId());
        if (allowedEquipment == null) {
            return RestrictionReason.CLASS_RESTRICTED; // Unknown class
        }

        // Check if the item is in the allowed equipment list for this class
        if (!itemStack.isIn(allowedEquipment)) {
            return RestrictionReason.CLASS_RESTRICTED;
        }

        return RestrictionReason.NONE;
    }

    /**
     * Simple boolean check if player can use item
     * @param player The player to check
     * @param itemStack The item to check
     * @return true if the player can use the item, false otherwise
     */
    public static boolean canPlayerUseItem(PlayerEntity player, ItemStack itemStack) {
        RestrictionReason reason = getRestrictionReason(player, itemStack);
        return reason == RestrictionReason.NONE || reason == RestrictionReason.UNIVERSAL_TOOL;
    }

    /**
     * Centralized method to handle restriction enforcement and messaging
     * @param player The player
     * @param itemStack The item
     * @param actionName The action being attempted (e.g., "use", "equip", "attack with")
     * @return true if action should be prevented, false if allowed
     */
    public static boolean handleRestriction(PlayerEntity player, ItemStack itemStack, String actionName) {
        RestrictionReason reason = getRestrictionReason(player, itemStack);

        switch (reason) {
            case NONE:
            case UNIVERSAL_TOOL:
                return false; // Allow action

            case BROKEN:
                if (!player.getWorld().isClient) {
                    sendBrokenItemMessage(player, itemStack, actionName);
                }
                return true; // Prevent action

            case CLASS_RESTRICTED:
                if (!player.getWorld().isClient) {
                    sendClassRestrictionMessage(player, itemStack, actionName);
                }
                return true; // Prevent action
        }

        return false;
    }

    private static void sendBrokenItemMessage(PlayerEntity player, ItemStack itemStack, String action) {
        String itemName = getItemTypeName(itemStack);
        player.sendMessage(Text.literal("This " + itemName + " is broken and cannot be " + action + "!")
                .formatted(Formatting.RED), true);
    }

    private static void sendClassRestrictionMessage(PlayerEntity player, ItemStack itemStack, String action) {
        String className = getPlayerClassName(player);
        String itemName = getItemTypeName(itemStack);
        player.sendMessage(Text.literal(className + " cannot " + action + " this " + itemName + "!")
                .formatted(Formatting.RED), true);
    }

    private static String getItemTypeName(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ArmorItem) {
            return "armor";
        } else if (itemStack.getItem() instanceof ToolItem) {
            return "tool";
        } else if (itemStack.getItem() instanceof SwordItem) {
            return "weapon";
        } else if (itemStack.getItem() instanceof BowItem) {
            return "bow";
        } else if (itemStack.getItem() instanceof CrossbowItem) {
            return "crossbow";
        } else {
            return "item";
        }
    }

    public static String getPlayerClassName(PlayerEntity player) {
        if (player == null) return "Unknown";

        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);
        PlayerClass playerClass = classComponent.getClassManager().getCurrentClass();
        return playerClass.getDisplayName();
    }

    public static void setBroken(ItemStack itemStack) {
        itemStack.set(ModDataComponents.BROKEN_FLAG, true);

        // Remove tool component to disable mining
        if (itemStack.getItem() instanceof ToolItem && itemStack.contains(DataComponentTypes.TOOL)) {
            // Store the original tool component before removing it
            ToolComponent originalTool = itemStack.get(DataComponentTypes.TOOL);
            if (originalTool != null) {
                itemStack.set(ModDataComponents.ORIGINAL_TOOL, originalTool);
            }
            itemStack.remove(DataComponentTypes.TOOL);
        }
    }

    public static boolean isBroken(ItemStack itemStack) {
        Boolean broken = itemStack.get(ModDataComponents.BROKEN_FLAG);
        return broken != null && broken;
    }

    public static void repair(ItemStack itemStack) {
        itemStack.remove(ModDataComponents.BROKEN_FLAG);

        // Restore tool component if it was a tool
        if (itemStack.getItem() instanceof ToolItem) {
            ToolComponent originalTool = itemStack.get(ModDataComponents.ORIGINAL_TOOL);
            if (originalTool != null) {
                itemStack.set(DataComponentTypes.TOOL, originalTool);
                itemStack.remove(ModDataComponents.ORIGINAL_TOOL);
            }
        }
    }

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