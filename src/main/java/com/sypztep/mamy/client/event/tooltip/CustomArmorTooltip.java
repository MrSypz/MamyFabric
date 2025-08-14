package com.sypztep.mamy.client.event.tooltip;

import com.sypztep.mamy.common.data.ItemDataEntry;
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
import org.apache.commons.lang3.mutable.MutableBoolean;

import java.util.function.Consumer;

public class CustomArmorTooltip extends AttributeTooltipHelper {

    // Armor-specific icons
    private static final String ARMOR_ICON = "\u0011";
    private static final String SHIELD_ICON = "\u0012";
    private static final String KNOCKBACK_ICON = "\u0013";

    public static void appendCustomArmorTooltip(ItemStack stack, Consumer<Text> textConsumer, PlayerEntity player) {
        new CustomArmorTooltip().appendCustomTooltip(stack, textConsumer, player);
    }

    @Override
    public void appendCustomTooltip(ItemStack stack, Consumer<Text> textConsumer, PlayerEntity player) {
        AttributeModifiersComponent attributeModifiers = stack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);

        if (!attributeModifiers.showInTooltip()) {
            return;
        }

        for (AttributeModifierSlot slot : AttributeModifierSlot.values()) {
            if (slot != AttributeModifierSlot.HEAD && slot != AttributeModifierSlot.CHEST &&
                    slot != AttributeModifierSlot.LEGS && slot != AttributeModifierSlot.FEET) {
                continue;
            }

            MutableBoolean hasShownHeader = new MutableBoolean(true);

            stack.applyAttributeModifier(slot, (attribute, modifier) -> {
                if (hasShownHeader.isTrue()) {
                    textConsumer.accept(ScreenTexts.EMPTY);
                    textConsumer.accept(Text.translatable("item.modifiers." + slot.asString()).formatted(Formatting.GRAY));
                    hasShownHeader.setFalse();
                }

                // Handle each attribute type
                if (attribute.equals(EntityAttributes.GENERIC_ARMOR)) {
                    appendCustomArmorValue(textConsumer, player, attribute, modifier);
                } else if (attribute.equals(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE)) {
                    appendCustomKnockbackResistance(textConsumer, player, attribute, modifier);
                } else {
                    // Use vanilla formatting for other attributes
                    appendVanillaAttributeModifier(textConsumer, player, attribute, modifier);
                }
            });

            // Add elemental resistances section if this slot had modifiers and item has elemental data
            if (!hasShownHeader.isTrue() && ItemDataEntry.hasEntry(stack.getItem())) {
                appendElementalResistances(stack, textConsumer, player);
            }
        }
    }

    private void appendCustomArmorValue(Consumer<Text> textConsumer, PlayerEntity player,
                                        net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attribute,
                                        net.minecraft.entity.attribute.EntityAttributeModifier modifier) {
        double armorValue = modifier.value();

        MutableText comp = Text.empty();
        comp.append(createIconText(ARMOR_ICON));
        comp.append(Text.translatable("attribute.name.generic.armor").formatted(Formatting.WHITE));
        comp.append(Text.literal(": ").formatted(Formatting.GRAY));
        comp.append(Text.literal(AttributeModifiersComponent.DECIMAL_FORMAT.format(armorValue)).formatted(Formatting.BLUE));

        textConsumer.accept(ScreenTexts.space().append(comp));
    }

    private void appendCustomKnockbackResistance(Consumer<Text> textConsumer, PlayerEntity player,
                                                 net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attribute,
                                                 net.minecraft.entity.attribute.EntityAttributeModifier modifier) {
        double knockbackValue = modifier.value() * 10; // Convert to percentage

        if (knockbackValue <= 0) return;

        MutableText comp = Text.empty();
        comp.append(createIconText(KNOCKBACK_ICON));
        comp.append(Text.translatable("attribute.name.generic.knockback_resistance").formatted(Formatting.WHITE));
        comp.append(Text.literal(": ").formatted(Formatting.GRAY));
        comp.append(Text.literal(AttributeModifiersComponent.DECIMAL_FORMAT.format(knockbackValue)).formatted(Formatting.BLUE));

        textConsumer.accept(ScreenTexts.space().append(comp));
    }

    private void appendElementalResistances(ItemStack stack, Consumer<Text> textConsumer, PlayerEntity player) {
        ItemDataEntry itemData = ItemDataEntry.getEntry(stack.getItem());

        // Check if has any resistances (positive or negative)
        boolean hasElementalResistances = itemData.damageRatios().entrySet().stream()
                .anyMatch(entry -> entry.getValue() != 0.0);

        if (!hasElementalResistances) {
            return;
        }

        // Resistances section header
        textConsumer.accept(ScreenTexts.EMPTY);
        MutableText comp = Text.empty();
        comp.append(createIconText(SHIELD_ICON));
        comp.append(Text.translatable("tooltip.mamy.elemental_resistances").formatted(Formatting.WHITE));
        textConsumer.accept(comp);

        // Use the new resistance-specific method
        appendResistanceSection(stack, textConsumer, "tooltip.mamy.resistance_budget");
    }
}