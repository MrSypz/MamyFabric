package com.sypztep.mamy.client.event.tooltip;

import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.init.ModTags;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ClassRestrictionTooltipHelper {
    private static final java.util.Map<String, TagKey<Item>> CLASS_TAGS = java.util.Map.of(
            ModClasses.NOVICE.getId(), ModTags.Items.NOVICE_EQUIPMENT,
            ModClasses.SWORDMAN.getId(), ModTags.Items.SWORDMAN_EQUIPMENT,
            ModClasses.MAGE.getId(), ModTags.Items.MAGE_EQUIPMENT,
            ModClasses.ARCHER.getId(), ModTags.Items.ARCHER_EQUIPMENT,
            ModClasses.ACOLYTE.getId(), ModTags.Items.ACOLYTE_EQUIPMENT,
            ModClasses.THIEF.getId(), ModTags.Items.THIEF_EQUIPMENT
    );

    public static void appendClassRestrictions(ItemStack itemStack, Consumer<Text> textConsumer, PlayerEntity player) {
        if (itemStack.isIn(ModTags.Items.ALL_CLASSES)) return; // Tools can be used by everyone

        List<String> allowedClasses = getClassesThatCanUse(itemStack);

        if (allowedClasses.isEmpty()) return; // No restrictions found

        // Check if player can use this item
        boolean playerCanUse = false;
        if (player != null) {
            PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);
            PlayerClass playerClass = classComponent.getClassManager().getCurrentClass();
            playerCanUse = allowedClasses.contains(playerClass.getId());
        }

        // Build class names using YOUR ModClasses.CLASSES map
        List<String> displayNames = allowedClasses.stream()
                .map(classId -> {
                    PlayerClass clazz = ModClasses.CLASSES.get(classId);
                    return clazz != null ? clazz.getDisplayName() : classId;
                })
                .toList();

        String classText = String.join(", ", displayNames);

        // Add empty line before restriction
        textConsumer.accept(Text.empty());

        // Simple tooltip as requested
        if (playerCanUse)
            textConsumer.accept(Text.literal(" - " + classText + " Exclusive").formatted(Formatting.GRAY));
         else
            textConsumer.accept(Text.literal(" - " + classText + " Exclusive").formatted(Formatting.RED));

    }

    private static List<String> getClassesThatCanUse(ItemStack itemStack) {
        List<String> allowedClasses = new ArrayList<>();

        for (java.util.Map.Entry<String, TagKey<Item>> entry : CLASS_TAGS.entrySet())
            if (itemStack.isIn(entry.getValue())) allowedClasses.add(entry.getKey());
        return allowedClasses;
    }
}