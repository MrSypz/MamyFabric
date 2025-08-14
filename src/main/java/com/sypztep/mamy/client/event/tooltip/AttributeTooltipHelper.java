package com.sypztep.mamy.client.event.tooltip;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.data.ItemDataEntry;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;

public abstract class AttributeTooltipHelper {

    // Custom font style
    protected static final Style ICONS = Style.EMPTY.withFont(Mamy.id("icons"));

    // Common Unicode characters for elements
    protected static final String PHYSICAL_ICON = "\u0003";
    protected static final String FIRE_ICON = "\u0004";
    protected static final String COLD_ICON = "\u0005";
    protected static final String ELECTRIC_ICON = "\u0006";
    protected static final String WATER_ICON = "\u0007";
    protected static final String WIND_ICON = "\u0008";
    protected static final String HOLY_ICON = "\u0009";

    // Element data holder with custom color support
    protected static class ElementInfo {
        public final String name;
        public final TextColor color;
        public final String icon;
        public final RegistryEntry<EntityAttribute> attribute;

        public ElementInfo(String name, TextColor color, String icon, RegistryEntry<EntityAttribute> attribute) {
            this.name = name;
            this.color = color;
            this.icon = icon;
            this.attribute = attribute;
        }

        // Constructor with hex color
        public ElementInfo(String name, int hexColor, String icon, RegistryEntry<EntityAttribute> attribute) {
            this.name = name;
            this.color = TextColor.fromRgb(hexColor);
            this.icon = icon;
            this.attribute = attribute;
        }

        // Constructor with Formatting (for compatibility)
        public ElementInfo(String name, Formatting formatting, String icon, RegistryEntry<EntityAttribute> attribute) {
            this.name = name;
            this.color = TextColor.fromFormatting(formatting);
            this.icon = icon;
            this.attribute = attribute;
        }
    }

    // Element registry with custom colors
    protected static final ElementInfo[] ELEMENTS = {
            new ElementInfo("physical", 0x9C9393, PHYSICAL_ICON, ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT), // Light gray
            new ElementInfo("heat", 0xFF4500, FIRE_ICON, ModEntityAttributes.FIRE_ATTACK_DAMAGE_FLAT),         // Orange red
            new ElementInfo("cold", 0x70C1E3, COLD_ICON, ModEntityAttributes.COLD_DAMAGE_FLAT),               // Sky blue
            new ElementInfo("electric", 0xFFD700, ELECTRIC_ICON, ModEntityAttributes.ELECTRIC_ATTACK_DAMAGE_FLAT), // Gold
            new ElementInfo("water", 0x4169E1, WATER_ICON, ModEntityAttributes.WATER_ATTACK_DAMAGE_FLAT),     // Royal blue
            new ElementInfo("wind", 0x98FB98, WIND_ICON, ModEntityAttributes.WIND_ATTACK_DAMAGE_FLAT),        // Pale green
            new ElementInfo("holy", 0xDDA0DD, HOLY_ICON, ModEntityAttributes.HOLY_ATTACK_DAMAGE_FLAT)         // Plum
    };

    // Helper methods
    protected static MutableText createIconText(String iconChar) {
        return Text.literal(iconChar).setStyle(ICONS);
    }

    protected static ElementInfo getElementInfo(RegistryEntry<EntityAttribute> attribute) {
        for (ElementInfo element : ELEMENTS) {
            if (element.attribute.equals(attribute)) {
                return element;
            }
        }
        return ELEMENTS[0]; // Default to physical
    }

    protected static EntityAttributeModifier getAttributeModifier(ItemStack stack, RegistryEntry<EntityAttribute> attribute, AttributeModifierSlot slot) {
        AttributeModifiersComponent modifiers = stack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);

        for (var entry : modifiers.modifiers()) {
            if (entry.attribute().equals(attribute) && entry.slot() == slot) {
                return entry.modifier();
            }
        }
        return null;
    }

    protected static void appendVanillaAttributeModifier(Consumer<Text> textConsumer, PlayerEntity player,
                                                         RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier modifier) {
        double value = modifier.value();

        double displayValue;
        if (modifier.operation() == EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                || modifier.operation() == EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
            displayValue = value * 100.0;
        } else {
            displayValue = value;
        }

        if (value > 0.0) {
            textConsumer.accept(
                    ScreenTexts.space()
                            .append(Text.literal("  + ").formatted(Formatting.GREEN))
                            .append(Text.translatable(
                                    "attribute.modifier.plus." + modifier.operation().getId(),
                                    AttributeModifiersComponent.DECIMAL_FORMAT.format(displayValue),
                                    Text.translatable(attribute.value().getTranslationKey())
                            ).formatted(attribute.value().getFormatting(true)))
            );
        } else if (value < 0.0) {
            textConsumer.accept(
                    ScreenTexts.space()
                            .append(Text.literal("  - ").formatted(Formatting.RED))
                            .append(Text.translatable(
                                    "attribute.modifier.take." + modifier.operation().getId(),
                                    AttributeModifiersComponent.DECIMAL_FORMAT.format(-displayValue),
                                    Text.translatable(attribute.value().getTranslationKey())
                            ).formatted(attribute.value().getFormatting(false)))
            );
        }
    }

    // Updated elemental section with percentage and amount display
    protected static void appendElementalSection(ItemStack stack, Consumer<Text> textConsumer, String sectionKey,
                                                 String budgetKey, String elementKeyPrefix, double baseDamage, boolean showAmount) {
        ItemDataEntry itemData = ItemDataEntry.getEntry(stack.getItem());

        // Show power budget if not default
        if (Math.abs(itemData.powerBudget() - 1.0) > 0.01) {
            textConsumer.accept(
                    ScreenTexts.space()
                            .append(Text.literal("  ● ").formatted(Formatting.GOLD))
                            .append(Text.translatable(budgetKey,
                                    String.format("%.1f%%", itemData.powerBudget() * 100)))
                            .formatted(Formatting.YELLOW)
            );
        }

        // Calculate power ratio for damage amounts
        double powerRatio = itemData.powerBudget() / itemData.damageRatios().values().stream().mapToDouble(Double::doubleValue).sum();

        // Show elemental ratios with percentage and amount
        itemData.damageRatios().entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .forEach(entry -> {
                    RegistryEntry<EntityAttribute> attribute = entry.getKey();
                    double ratio = entry.getValue();
                    ElementInfo element = getElementInfo(attribute);

                    String percentage = String.format("%.1f%%", ratio * 100 / itemData.damageRatios().values().stream().mapToDouble(Double::doubleValue).sum());

                    MutableText elementComp = Text.empty();
                    elementComp.append(Text.literal("  "));
                    elementComp.append(createIconText(element.icon));
                    elementComp.append(Text.translatable(elementKeyPrefix + element.name).setStyle(Style.EMPTY.withColor(element.color)));
                    elementComp.append(Text.literal(": " + percentage).formatted(Formatting.GRAY));

                    // Add amount in parentheses if requested
                    if (showAmount && baseDamage > 0) {
                        double amount = ratio * powerRatio * baseDamage;
                        elementComp.append(Text.literal(" (").formatted(Formatting.DARK_GRAY));
                        elementComp.append(Text.literal(AttributeModifiersComponent.DECIMAL_FORMAT.format(amount)).setStyle(Style.EMPTY.withColor(element.color)));
                        elementComp.append(Text.literal(")").formatted(Formatting.DARK_GRAY));
                    }

                    textConsumer.accept(ScreenTexts.space().append(elementComp));
                });
    }

    // Overloaded method for compatibility (no amount display)
    protected static void appendElementalSection(ItemStack stack, Consumer<Text> textConsumer, String sectionKey, String budgetKey, String elementKeyPrefix) {
        appendElementalSection(stack, textConsumer, sectionKey, budgetKey, elementKeyPrefix, 0, false);
    }

    // Abstract method to be implemented by subclasses
    public abstract void appendCustomTooltip(ItemStack stack, Consumer<Text> textConsumer, PlayerEntity player);
}