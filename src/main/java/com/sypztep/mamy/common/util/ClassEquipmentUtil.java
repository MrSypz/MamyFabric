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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Matrix-style equipment system using equipment categories and class permissions
 */
public final class ClassEquipmentUtil {

    // Equipment categories
    public enum EquipmentCategory {
        ONE_HAND_SWORD,
        TWO_HAND_SWORD,
        SPEAR,
        DAGGER,
        STAFF,
        MACE,
        KNUCKLE,
        BOW,
        SHORT_BOW,
        SHIELD,
        LIGHT_ARMOR,
        MEDIUM_ARMOR,
        HEAVY_ARMOR,
        MAGIC_ITEM,
        HOLY_ITEM,
        UNIVERSAL_TOOL
    }

    // Class permissions matrix
    private static final Map<String, Set<EquipmentCategory>> CLASS_PERMISSIONS = new HashMap<>();

    static {
        // NOVICE: One Hand Sword, Shield, Light Armor, Medium Armor
        CLASS_PERMISSIONS.put("novice", EnumSet.of(
                EquipmentCategory.ONE_HAND_SWORD,
                EquipmentCategory.SHIELD,
                EquipmentCategory.LIGHT_ARMOR,
                EquipmentCategory.MEDIUM_ARMOR,
                EquipmentCategory.UNIVERSAL_TOOL
        ));

        // SWORDMAN: Spear, Shield, Two Hand Sword, One Hand Sword, Medium Armor, Heavy Armor
        CLASS_PERMISSIONS.put("swordman", EnumSet.of(
                EquipmentCategory.SPEAR,
                EquipmentCategory.SHIELD,
                EquipmentCategory.TWO_HAND_SWORD,
                EquipmentCategory.ONE_HAND_SWORD,
                EquipmentCategory.MEDIUM_ARMOR,
                EquipmentCategory.HEAVY_ARMOR,
                EquipmentCategory.UNIVERSAL_TOOL
        ));

        // MAGE: Dagger, Staff, Light Armor
        CLASS_PERMISSIONS.put("mage", EnumSet.of(
                EquipmentCategory.DAGGER,
                EquipmentCategory.STAFF,
                EquipmentCategory.LIGHT_ARMOR,
                EquipmentCategory.MAGIC_ITEM,
                EquipmentCategory.UNIVERSAL_TOOL
        ));

        // ARCHER: Bow, Dagger, Light Armor
        CLASS_PERMISSIONS.put("archer", EnumSet.of(
                EquipmentCategory.BOW,
                EquipmentCategory.DAGGER,
                EquipmentCategory.LIGHT_ARMOR,
                EquipmentCategory.UNIVERSAL_TOOL
        ));

        // ACOLYTE: Mace, Shield, Knuckle, Light Armor, Medium Armor
        CLASS_PERMISSIONS.put("acolyte", EnumSet.of(
                EquipmentCategory.MACE,
                EquipmentCategory.SHIELD,
                EquipmentCategory.KNUCKLE,
                EquipmentCategory.LIGHT_ARMOR,
                EquipmentCategory.MEDIUM_ARMOR,
                EquipmentCategory.HOLY_ITEM,
                EquipmentCategory.UNIVERSAL_TOOL
        ));

        // THIEF: Dagger, Short Bow, Light Armor
        CLASS_PERMISSIONS.put("thief", EnumSet.of(
                EquipmentCategory.DAGGER,
                EquipmentCategory.SHORT_BOW,
                EquipmentCategory.LIGHT_ARMOR,
                EquipmentCategory.UNIVERSAL_TOOL
        ));
    }

    // Equipment category detection map
    private static final Map<TagKey<Item>, EquipmentCategory> TAG_TO_CATEGORY = new HashMap<>();

    static {
        TAG_TO_CATEGORY.put(ModTags.Items.ONE_HAND_SWORDS, EquipmentCategory.ONE_HAND_SWORD);
        TAG_TO_CATEGORY.put(ModTags.Items.TWO_HAND_SWORDS, EquipmentCategory.TWO_HAND_SWORD);
        TAG_TO_CATEGORY.put(ModTags.Items.SPEARS, EquipmentCategory.SPEAR);
        TAG_TO_CATEGORY.put(ModTags.Items.DAGGERS, EquipmentCategory.DAGGER);
        TAG_TO_CATEGORY.put(ModTags.Items.STAFFS, EquipmentCategory.STAFF);
        TAG_TO_CATEGORY.put(ModTags.Items.MACES, EquipmentCategory.MACE);
        TAG_TO_CATEGORY.put(ModTags.Items.KNUCKLES, EquipmentCategory.KNUCKLE);
        TAG_TO_CATEGORY.put(ModTags.Items.BOWS, EquipmentCategory.BOW);
        TAG_TO_CATEGORY.put(ModTags.Items.SHORT_BOWS, EquipmentCategory.SHORT_BOW);
        TAG_TO_CATEGORY.put(ModTags.Items.SHIELDS, EquipmentCategory.SHIELD);
        TAG_TO_CATEGORY.put(ModTags.Items.LIGHT_ARMOR, EquipmentCategory.LIGHT_ARMOR);
        TAG_TO_CATEGORY.put(ModTags.Items.MEDIUM_ARMOR, EquipmentCategory.MEDIUM_ARMOR);
        TAG_TO_CATEGORY.put(ModTags.Items.HEAVY_ARMOR, EquipmentCategory.HEAVY_ARMOR);
        TAG_TO_CATEGORY.put(ModTags.Items.MAGIC_ITEMS, EquipmentCategory.MAGIC_ITEM);
        TAG_TO_CATEGORY.put(ModTags.Items.HOLY_ITEMS, EquipmentCategory.HOLY_ITEM);
    }

    // Restriction reasons
    public enum RestrictionReason {
        NONE,           // Can use item
        BROKEN,         // Item is broken
        CLASS_RESTRICTED // Class cannot use item
    }

    /**
     * Get equipment category for an item
     */
    private static EquipmentCategory getEquipmentCategory(ItemStack itemStack) {
        // Check universal tools first (tools, pickaxes, etc.)
        if (itemStack.getItem() instanceof ShovelItem ||
                itemStack.getItem() instanceof PickaxeItem ||
                itemStack.getItem() instanceof AxeItem ||
                itemStack.getItem() instanceof HoeItem) {
            return EquipmentCategory.UNIVERSAL_TOOL;
        }

        // Check tagged categories
        for (Map.Entry<TagKey<Item>, EquipmentCategory> entry : TAG_TO_CATEGORY.entrySet()) {
            if (itemStack.isIn(entry.getKey())) {
                return entry.getValue();
            }
        }

        // If no specific category found, treat as universal (allow by default)
        return EquipmentCategory.UNIVERSAL_TOOL;
    }

    /**
     * Check if a player can use an item and get the reason if they can't
     */
    public static RestrictionReason getRestrictionReason(PlayerEntity player, ItemStack itemStack) {
        if (player == null || itemStack.isEmpty()) {
            return RestrictionReason.NONE;
        }

        // Check if item is broken first
        if (isBroken(itemStack)) {
            return RestrictionReason.BROKEN;
        }

        // Get equipment category
        EquipmentCategory category = getEquipmentCategory(itemStack);

        // Universal tools can always be used
        if (category == EquipmentCategory.UNIVERSAL_TOOL) {
            return RestrictionReason.NONE;
        }

        // Get player's current class
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);
        PlayerClass playerClass = classComponent.getClassManager().getCurrentClass();

        // Check if class can use this category
        Set<EquipmentCategory> allowedCategories = CLASS_PERMISSIONS.get(playerClass.getId());
        if (allowedCategories == null || !allowedCategories.contains(category)) {
            return RestrictionReason.CLASS_RESTRICTED;
        }

        return RestrictionReason.NONE;
    }

    /**
     * Simple boolean check if player can use item
     */
    public static boolean canPlayerUseItem(PlayerEntity player, ItemStack itemStack) {
        RestrictionReason reason = getRestrictionReason(player, itemStack);
        return reason == RestrictionReason.NONE;
    }

    /**
     * Centralized method to handle restriction enforcement and messaging
     */
    public static boolean handleRestriction(PlayerEntity player, ItemStack itemStack, String actionName) {
        RestrictionReason reason = getRestrictionReason(player, itemStack);

        switch (reason) {
            case NONE:
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

        if (itemStack.getItem() instanceof ToolItem && itemStack.contains(DataComponentTypes.TOOL)) {
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

        if (itemStack.getDamage() + damageAmount >= itemStack.getMaxDamage()) {
            setBroken(itemStack);
            return true;
        }
        return false;
    }

    public static Set<EquipmentCategory> getClassPermissions(String classId) {
        return CLASS_PERMISSIONS.getOrDefault(classId, EnumSet.noneOf(EquipmentCategory.class));
    }

    public static EquipmentCategory getItemCategory(ItemStack itemStack) {
        return getEquipmentCategory(itemStack);
    }
}