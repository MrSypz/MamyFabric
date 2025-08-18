package com.sypztep.mamy.client.event.tooltip;

import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.init.ModDataComponents;
import com.sypztep.mamy.common.init.ModTags;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.util.ClassEquipmentUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Helper class for adding class restriction tooltips to items
 */
public class ClassRestrictionTooltipHelper {

    /**
     * Append class restriction information to item tooltips
     * @param itemStack The item being examined
     * @param textConsumer Consumer to add tooltip lines
     * @param player The player viewing the tooltip
     */
    public static void appendClassRestrictions(ItemStack itemStack, Consumer<Text> textConsumer, PlayerEntity player) {
        List<String> allowedClasses = getClassesThatCanUse(itemStack);

        if (allowedClasses.isEmpty()) return; // No restrictions found

        // Check if player can use this item
        boolean playerCanUse = false;
        if (player != null) {
            playerCanUse = ClassEquipmentUtil.canPlayerUseItem(player, itemStack);
        }
        boolean everyOne = itemStack.isIn(ModTags.Items.ALL_CLASSES);

        // Build class names using ModClasses.CLASSES map
        List<String> displayNames = allowedClasses.stream()
                .map(classId -> {
                    PlayerClass clazz = ModClasses.CLASSES.get(classId);
                    return clazz != null ? clazz.getDisplayName() : classId;
                })
                .toList();

        String classText = String.join(", ", displayNames);

        // Add empty line before restriction
        textConsumer.accept(Text.empty());

        if (playerCanUse)
            textConsumer.accept(Text.literal(" - " + classText + " Exclusive").formatted(Formatting.GRAY));
        else if (everyOne)
            textConsumer.accept(Text.literal(" - EveryOne").formatted(Formatting.GRAY));
        else
            textConsumer.accept(Text.literal(" - " + classText + " Exclusive").formatted(Formatting.RED));

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
     * Get the list of class IDs that can use this item
     */
    private static List<String> getClassesThatCanUse(ItemStack itemStack) {
        List<String> allowedClasses = new ArrayList<>();

        for (Map.Entry<String, TagKey<Item>> entry : ClassEquipmentUtil.getClassEquipmentMap().entrySet())
            if (itemStack.isIn(entry.getValue())) allowedClasses.add(entry.getKey());

        return allowedClasses;
    }
}