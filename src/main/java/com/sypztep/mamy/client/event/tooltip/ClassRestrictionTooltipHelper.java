package com.sypztep.mamy.client.event.tooltip;

import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.init.ModDataComponents;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.util.ClassEquipmentUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Helper class for adding class restriction tooltips to items
 */
public final class ClassRestrictionTooltipHelper {

    /**
     * Append class restriction information to item tooltips
     * @param itemStack The item being examined
     * @param textConsumer Consumer to add tooltip lines
     * @param player The player viewing the tooltip
     */
    public static void appendClassRestrictions(ItemStack itemStack, Consumer<Text> textConsumer, PlayerEntity player) {
        boolean playerCanUse = false;
        if (player != null) {
            playerCanUse = ClassEquipmentUtil.canPlayerUseItem(player, itemStack);
        }

        ClassEquipmentUtil.EquipmentCategory itemCategory = ClassEquipmentUtil.getItemCategory(itemStack);

        // If it's a universal tool, show that
        if (itemCategory == ClassEquipmentUtil.EquipmentCategory.UNIVERSAL_TOOL) {
            textConsumer.accept(Text.empty());
            textConsumer.accept(Text.literal(" - Universal Tool").formatted(Formatting.GRAY));
        } else {
            List<String> allowedClasses = getClassesThatCanUseCategory(itemCategory);

            if (allowedClasses.isEmpty()) {
                textConsumer.accept(Text.empty());
                textConsumer.accept(Text.literal(" - No Class Can Use").formatted(Formatting.DARK_RED));
            } else if (allowedClasses.size() == ModClasses.CLASSES.size()) {
                textConsumer.accept(Text.empty());
                textConsumer.accept(Text.literal(" - All Classes").formatted(Formatting.GRAY));
            } else {
                List<String> displayNames = allowedClasses.stream()
                        .map(classId -> {
                            PlayerClass clazz = ModClasses.CLASSES.get(classId);
                            return clazz != null ? clazz.getDisplayName() : classId;
                        })
                        .toList();

                String classText = String.join(", ", displayNames);

                textConsumer.accept(Text.empty());

                // Show in different colors based on whether player can use it
                if (playerCanUse)
                    textConsumer.accept(Text.literal(" - " + classText + " Exclusive").formatted(Formatting.GREEN));
                 else
                    textConsumer.accept(Text.literal(" - " + classText + " Exclusive").formatted(Formatting.RED));
            }
        }

        // Show broken status if item is broken
        if (ClassEquipmentUtil.isBroken(itemStack)) {
            textConsumer.accept(Text.empty());
            textConsumer.accept(Text.literal(" - BROKEN (Cannot be used)")
                    .formatted(Formatting.DARK_RED));
        }

        if (itemStack.contains(ModDataComponents.CRAFT_BY)) {
            textConsumer.accept(Text.literal(" Craft By: ").formatted(Formatting.GRAY).append(Text.literal(itemStack.get(ModDataComponents.CRAFT_BY)).formatted(Formatting.WHITE)));
        }
    }

    /**
     * Get the list of class IDs that can use the given equipment category
     */
    private static List<String> getClassesThatCanUseCategory(ClassEquipmentUtil.EquipmentCategory category) {
        List<String> allowedClasses = new ArrayList<>();

        for (String classId : ModClasses.CLASSES.keySet()) {
            Set<ClassEquipmentUtil.EquipmentCategory> classPermissions = ClassEquipmentUtil.getClassPermissions(classId);
            if (classPermissions.contains(category)) {
                allowedClasses.add(classId);
            }
        }

        return allowedClasses;
    }
}