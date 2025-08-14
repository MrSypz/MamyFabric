package com.sypztep.mamy.client.event.tooltip;

import com.sypztep.mamy.common.data.ItemDataEntry;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class CustomArmorTooltip extends AttributeTooltipHelper {

    // Armor-specific icons
    private static final String ARMOR_ICON = "\u0011";
    private static final String SHIELD_ICON = "\u0012";
    private static final String KNOCKBACK_ICON = "\u0013";

    public static void appendCustomArmorTooltip(ItemStack stack, Consumer<Text> textConsumer, PlayerEntity player) {
        new CustomArmorTooltip().appendCustomTooltip(stack, textConsumer, player);
    }

    @Override
    public void appendCustomTooltip(ItemStack stack, Consumer<Text> textConsumer, PlayerEntity player) {
        // ONLY show for items that have datapack entries
        if (!ItemDataEntry.hasEntry(stack.getItem())) {
            return;
        }

        AttributeModifiersComponent attributeModifiers = stack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);

        if (!attributeModifiers.showInTooltip()) {
            return;
        }

        AttributeModifierSlot slot = getArmorSlot(stack);
        if (slot == null) return;

        boolean hasShownHeader = false;

        // 1. ARMOR VALUE
        hasShownHeader = appendArmorValue(stack, textConsumer, player, hasShownHeader, slot);

        // 2. KNOCKBACK RESISTANCE
        hasShownHeader = appendKnockbackResistance(stack, textConsumer, player, hasShownHeader, slot);

        // 3. ELEMENTAL RESISTANCES
        hasShownHeader = appendElementalResistances(stack, textConsumer, player, hasShownHeader);

        // 4. OTHER ATTRIBUTES
        appendOtherArmorAttributes(stack, textConsumer, player, hasShownHeader, slot);
    }

    private boolean appendArmorValue(ItemStack stack, Consumer<Text> textConsumer, PlayerEntity player, boolean hasShownHeader, AttributeModifierSlot slot) {
        var armorModifier = getAttributeModifier(stack, EntityAttributes.GENERIC_ARMOR, slot);
        if (armorModifier == null) {
            return hasShownHeader;
        }

        if (!hasShownHeader) {
            textConsumer.accept(ScreenTexts.EMPTY);
            textConsumer.accept(Text.translatable("item.modifiers." + slot.asString()).formatted(Formatting.GRAY));
            hasShownHeader = true;
        }

        double armorValue = armorModifier.value();

        MutableText comp = Text.empty();
        comp.append(createIconText(ARMOR_ICON));
        comp.append(Text.translatable("attribute.name.generic.armor").formatted(Formatting.WHITE));
        comp.append(Text.literal(": ").formatted(Formatting.GRAY));
        comp.append(Text.literal("+" + AttributeModifiersComponent.DECIMAL_FORMAT.format(armorValue)).formatted(Formatting.BLUE));

        textConsumer.accept(ScreenTexts.space().append(comp));

        return hasShownHeader;
    }

    private boolean appendKnockbackResistance(ItemStack stack, Consumer<Text> textConsumer, PlayerEntity player, boolean hasShownHeader, AttributeModifierSlot slot) {
        var knockbackModifier = getAttributeModifier(stack, EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, slot);
        if (knockbackModifier == null || knockbackModifier.value() <= 0) {
            return hasShownHeader;
        }

        if (!hasShownHeader) {
            textConsumer.accept(ScreenTexts.EMPTY);
            textConsumer.accept(Text.translatable("item.modifiers." + slot.asString()).formatted(Formatting.GRAY));
            hasShownHeader = true;
        }

        double knockbackValue = knockbackModifier.value() * 10; // Convert to percentage

        MutableText comp = Text.empty();
        comp.append(createIconText(KNOCKBACK_ICON));
        comp.append(Text.translatable("attribute.name.generic.knockback_resistance").formatted(Formatting.WHITE));
        comp.append(Text.literal(": ").formatted(Formatting.GRAY));
        comp.append(Text.literal("+" + AttributeModifiersComponent.DECIMAL_FORMAT.format(knockbackValue)).formatted(Formatting.BLUE));

        textConsumer.accept(ScreenTexts.space().append(comp));

        return hasShownHeader;
    }

    private boolean appendElementalResistances(ItemStack stack, Consumer<Text> textConsumer, PlayerEntity player, boolean hasShownHeader) {
        ItemDataEntry itemData = ItemDataEntry.getEntry(stack.getItem());

        // Check if has elemental resistances
        boolean hasElementalResistances = itemData.damageRatios().entrySet().stream()
                .anyMatch(entry -> entry.getValue() > 0);

        if (!hasElementalResistances) {
            return hasShownHeader;
        }

        if (!hasShownHeader) {
            textConsumer.accept(ScreenTexts.EMPTY);
            textConsumer.accept(Text.translatable("item.modifiers.armor").formatted(Formatting.GRAY));
            hasShownHeader = true;
        }

        // Resistances section header
        textConsumer.accept(ScreenTexts.EMPTY);
        MutableText comp = Text.empty();
        comp.append(createIconText(SHIELD_ICON));
        comp.append(Text.translatable("tooltip.mamy.elemental_resistances").formatted(Formatting.WHITE));
        textConsumer.accept(comp);

        if (Screen.hasShiftDown()) {
            // Use helper method for elemental breakdown
            appendElementalSection(stack, textConsumer, "tooltip.mamy.elemental_resistances", "tooltip.mamy.resistance_budget", "resistance_element.");
        } else {
            textConsumer.accept(
                    ScreenTexts.space()
                            .append(Text.literal("    "))
                            .append(Text.translatable("tooltip.mamy.hold_shift").formatted(Formatting.DARK_GRAY, Formatting.ITALIC))
            );
        }

        return hasShownHeader;
    }

    private void appendOtherArmorAttributes(ItemStack stack, Consumer<Text> textConsumer, PlayerEntity player, boolean hasShownHeader, AttributeModifierSlot slot) {
        AtomicBoolean hasShownOtherHeader = new AtomicBoolean(false);

        stack.applyAttributeModifier(slot, (attribute, modifier) -> {
            // Skip attributes we've already handled
            if (attribute.equals(EntityAttributes.GENERIC_ARMOR) || attribute.equals(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE)) {
                return;
            }

            if (!hasShownOtherHeader.get()) {
                textConsumer.accept(ScreenTexts.EMPTY);
                textConsumer.accept(Text.translatable("tooltip.mamy.other_attributes").formatted(Formatting.GRAY));
                hasShownOtherHeader.set(true);
            }

            appendVanillaAttributeModifier(textConsumer, player, attribute, modifier);
        });
    }

    private AttributeModifierSlot getArmorSlot(ItemStack stack) {
        // Try to determine armor slot from the item
        String itemName = stack.getItem().toString().toLowerCase();
        if (itemName.contains("helmet") || itemName.contains("cap") || itemName.contains("hood")) {
            return AttributeModifierSlot.HEAD;
        } else if (itemName.contains("chestplate") || itemName.contains("tunic") || itemName.contains("shirt")) {
            return AttributeModifierSlot.CHEST;
        } else if (itemName.contains("leggings") || itemName.contains("pants") || itemName.contains("legs")) {
            return AttributeModifierSlot.LEGS;
        } else if (itemName.contains("boots") || itemName.contains("shoes") || itemName.contains("feet")) {
            return AttributeModifierSlot.FEET;
        }

        // Fallback: check which slot has modifiers
        AttributeModifiersComponent modifiers = stack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
        for (var entry : modifiers.modifiers()) {
            if (entry.slot() != AttributeModifierSlot.MAINHAND && entry.slot() != AttributeModifierSlot.OFFHAND) {
                return entry.slot();
            }
        }

        return null;
    }
}