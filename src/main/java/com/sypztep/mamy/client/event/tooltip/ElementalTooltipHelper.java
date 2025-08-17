package com.sypztep.mamy.client.event.tooltip;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.data.ItemElementDataEntry;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.damage.ElementType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class ElementalTooltipHelper {

    // Custom font style and icons
    public static final Style ICONS = Style.EMPTY.withFont(Mamy.id("icons"));
    private static final String SWORD_ICON = "\u0001";
    private static final String SPEED_ICON = "\u0002";
    private static final String ARMOR_ICON = "\u0011";
    private static final String SHIELD_ICON = "\u0012";
    private static final String KNOCKBACK_ICON = "\u0013";
    private static final String POWER_ICON = "\u0010";

    // Main entry points for different item types
    public static void appendWeaponTooltip(ItemStack stack, Consumer<Text> textConsumer, PlayerEntity player) {
        var modifiers = stack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
        if (!modifiers.showInTooltip()) return;

        boolean hasShownHeader = false;
        hasShownHeader = appendMeleeDamage(stack, textConsumer, player, hasShownHeader);
        hasShownHeader = appendAttackSpeed(stack, textConsumer, player, hasShownHeader);

        if (ItemElementDataEntry.hasEntry(stack.getItem())) {
            appendElementalDamage(stack, textConsumer, player, hasShownHeader);
        }

        appendOtherAttributes(stack, textConsumer);
    }

    public static void appendArmorTooltip(ItemStack stack, Consumer<Text> textConsumer) {
        var modifiers = stack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
        if (!modifiers.showInTooltip()) return;

        for (AttributeModifierSlot slot : AttributeModifierSlot.values()) {
            if (!isArmorSlot(slot)) continue;

            AtomicBoolean hasShownHeader = new AtomicBoolean(true);

            stack.applyAttributeModifier(slot, (attribute, modifier) -> {
                if (hasShownHeader.get()) {
                    textConsumer.accept(ScreenTexts.EMPTY);
                    textConsumer.accept(Text.translatable("item.modifiers." + slot.asString()).formatted(Formatting.GRAY));
                    hasShownHeader.set(false);
                }

                if (attribute.equals(EntityAttributes.GENERIC_ARMOR)) {
                    appendCustomArmorValue(textConsumer, modifier.value());
                } else if (attribute.equals(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE)) {
                    appendCustomKnockbackResistance(textConsumer, modifier.value());
                } else {
                    appendVanillaAttributeModifier(textConsumer, attribute, modifier);
                }
            });

            if (!hasShownHeader.get() && ItemElementDataEntry.hasEntry(stack.getItem())) {
                appendElementalResistances(stack, textConsumer);
            }
        }
    }

    // Private helper methods
    private static boolean appendMeleeDamage(ItemStack stack, Consumer<Text> textConsumer, PlayerEntity player, boolean hasShownHeader) {
        var attackDamageModifier = getAttributeModifier(stack, EntityAttributes.GENERIC_ATTACK_DAMAGE);
        if (attackDamageModifier == null || player == null) return hasShownHeader;

        if (!hasShownHeader) {
            textConsumer.accept(ScreenTexts.EMPTY);
            textConsumer.accept(Text.translatable("item.modifiers.mainhand").formatted(Formatting.GRAY));
            hasShownHeader = true;
        }

        double baseDamage = attackDamageModifier.value();
        double playerAttackDamage = player.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        double meleeDamageFlat = player.getAttributeValue(ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT);
        double totalDamage = baseDamage + playerAttackDamage + meleeDamageFlat;

        MutableText comp = Text.empty();
        comp.append(createIconText(SWORD_ICON));
        comp.append(Text.translatable("attribute.name.generic.attack_damage").formatted(Formatting.WHITE));
        comp.append(Text.literal(": ").formatted(Formatting.GRAY));
        comp.append(Text.literal(format(totalDamage)).formatted(Formatting.YELLOW));

        textConsumer.accept(ScreenTexts.space().append(comp));

        if (ItemElementDataEntry.hasEntry(stack.getItem()) && Screen.hasShiftDown()) {
            textConsumer.accept(createIndentedText("├─ ", "tooltip.mamy.base_weapon", baseDamage + playerAttackDamage, Formatting.WHITE));
            textConsumer.accept(createIndentedText("└─ ", "tooltip.mamy.melee_bonus", meleeDamageFlat, meleeDamageFlat > 0 ? Formatting.GREEN : Formatting.DARK_GRAY));
        }

        return hasShownHeader;
    }

    private static boolean appendAttackSpeed(ItemStack stack, Consumer<Text> textConsumer, PlayerEntity player, boolean hasShownHeader) {
        var attackSpeedModifier = getAttributeModifier(stack, EntityAttributes.GENERIC_ATTACK_SPEED);
        if (attackSpeedModifier == null || player == null) return hasShownHeader;

        if (!hasShownHeader) {
            textConsumer.accept(ScreenTexts.EMPTY);
            textConsumer.accept(Text.translatable("item.modifiers.mainhand").formatted(Formatting.GRAY));
            hasShownHeader = true;
        }

        double speedValue = attackSpeedModifier.value() + player.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_SPEED);

        MutableText comp = Text.empty();
        comp.append(createIconText(SPEED_ICON));
        comp.append(Text.translatable("attribute.name.generic.attack_speed").formatted(Formatting.WHITE));
        comp.append(Text.literal(": ").formatted(Formatting.GRAY));
        comp.append(Text.literal(format(speedValue)).formatted(Formatting.YELLOW));

        textConsumer.accept(ScreenTexts.space().append(comp));
        return hasShownHeader;
    }

    private static void appendElementalDamage(ItemStack stack, Consumer<Text> textConsumer, PlayerEntity player, boolean hasShownHeader) {
        if (!hasShownHeader) {
            textConsumer.accept(ScreenTexts.EMPTY);
            textConsumer.accept(Text.translatable("item.modifiers.mainhand").formatted(Formatting.GRAY));
        }

        var attackDamageModifier = getAttributeModifier(stack, EntityAttributes.GENERIC_ATTACK_DAMAGE);
        double baseDamage = 0;
        if (attackDamageModifier != null && player != null) {
            baseDamage = attackDamageModifier.value() + player.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        }

        appendElementalSection(stack, textConsumer,
                baseDamage);
    }

    private static void appendCustomArmorValue(Consumer<Text> textConsumer, double armorValue) {
        MutableText comp = Text.empty();
        comp.append(createIconText(ARMOR_ICON));
        comp.append(Text.translatable("attribute.name.generic.armor").formatted(Formatting.WHITE));
        comp.append(Text.literal(": ").formatted(Formatting.GRAY));
        comp.append(Text.literal(format(armorValue)).formatted(Formatting.BLUE));

        textConsumer.accept(ScreenTexts.space().append(comp));
    }

    private static void appendCustomKnockbackResistance(Consumer<Text> textConsumer, double knockbackValue) {
        double knockbackPercentage = knockbackValue * 10;
        if (knockbackPercentage <= 0) return;

        MutableText comp = Text.empty();
        comp.append(createIconText(KNOCKBACK_ICON));
        comp.append(Text.translatable("attribute.name.generic.knockback_resistance").formatted(Formatting.WHITE));
        comp.append(Text.literal(": ").formatted(Formatting.GRAY));
        comp.append(Text.literal(format(knockbackPercentage)).formatted(Formatting.BLUE));

        textConsumer.accept(ScreenTexts.space().append(comp));
    }

    private static void appendElementalResistances(ItemStack stack, Consumer<Text> textConsumer) {
        ItemElementDataEntry itemData = ItemElementDataEntry.getEntry(stack.getItem());

        boolean hasElementalResistances = itemData.damageRatios().entrySet().stream()
                .anyMatch(entry -> entry.getValue() != 0.0);

        if (!hasElementalResistances) return;

        textConsumer.accept(ScreenTexts.EMPTY);
        MutableText comp = Text.empty();
        comp.append(createIconText(SHIELD_ICON));
        comp.append(Text.translatable("tooltip.mamy.elemental_resistances").formatted(Formatting.WHITE));
        textConsumer.accept(comp);

        appendResistanceSection(stack, textConsumer);
    }

    private static void appendElementalSection(ItemStack stack, Consumer<Text> textConsumer,
                                               double baseDamage) {
        ItemElementDataEntry itemData = ItemElementDataEntry.getEntry(stack.getItem());

        if (Math.abs(itemData.powerBudget() - 1.0) > 0.01) {
            textConsumer.accept(ScreenTexts.space()
                    .append(Text.literal("  "))
                    .append(createIconText(POWER_ICON).formatted(Formatting.GOLD))
                    .append(Text.translatable("tooltip.mamy.power_budget", String.format("%.1f%%", itemData.powerBudget() * 100)))
                    .formatted(Formatting.YELLOW));
        }

        double powerRatio = itemData.powerBudget() / itemData.damageRatios().values().stream().mapToDouble(Double::doubleValue).sum();

        itemData.damageRatios().entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .forEach(entry -> {
                    ElementType element = ElementType.fromDamageAttribute(entry.getKey());
                    double ratio = entry.getValue();
                    String percentage = String.format("%.1f%%", ratio * 100 / itemData.damageRatios().values().stream().mapToDouble(Double::doubleValue).sum());

                    MutableText elementComp = Text.literal("  ")
                            .append(createIconText(element.icon))
                            .append(Text.translatable("damage_element." + element.name).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(element.color))))
                            .append(Text.literal(": " + percentage).formatted(Formatting.GRAY));

                    if (baseDamage > 0) {
                        double amount = ratio * powerRatio * baseDamage;
                        elementComp.append(Text.literal(" (").formatted(Formatting.DARK_GRAY))
                                .append(Text.literal(format(amount)).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(element.color))))
                                .append(Text.literal(")").formatted(Formatting.DARK_GRAY));
                    }

                    textConsumer.accept(ScreenTexts.space().append(elementComp));
                });
    }

    private static void appendResistanceSection(ItemStack stack, Consumer<Text> textConsumer) {
        ItemElementDataEntry itemData = ItemElementDataEntry.getEntry(stack.getItem());

        if (Math.abs(itemData.powerBudget() - 1.0) > 0.01) {
            textConsumer.accept(ScreenTexts.space()
                    .append(createIconText(POWER_ICON))
                    .append(Text.translatable("tooltip.mamy.resistance_budget", String.format("%.1f%%", itemData.powerBudget() * 100)))
                    .formatted(Formatting.YELLOW));
        }

        itemData.damageRatios().entrySet().stream()
                .filter(entry -> entry.getValue() != 0.0)
                .sorted((a, b) -> Double.compare(Math.abs(b.getValue()), Math.abs(a.getValue())))
                .forEach(entry -> {
                    ElementType element = ElementType.fromResistanceAttribute(entry.getKey());
                    double resistance = entry.getValue() * itemData.powerBudget();
                    String percentage = String.format("%.1f%%", Math.abs(resistance * 100));

                    MutableText elementComp = Text.literal("  ")
                            .append(createIconText(element.icon))
                            .append(Text.translatable("resistance_element." + element.name).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(element.color))))
                            .append(Text.literal(": ").formatted(Formatting.GRAY));

                    if (resistance >= 0) {
                        elementComp.append(Text.literal("+" + percentage).formatted(Formatting.BLUE));
                    } else {
                        elementComp.append(Text.literal("-" + percentage).formatted(Formatting.RED));
                    }

                    textConsumer.accept(ScreenTexts.space().append(elementComp));
                });
    }

    private static void appendOtherAttributes(ItemStack stack, Consumer<Text> textConsumer) {
        AtomicBoolean hasShownOtherHeader = new AtomicBoolean(false);

        stack.applyAttributeModifier(AttributeModifierSlot.MAINHAND, (attribute, modifier) -> {
            if (attribute.equals(EntityAttributes.GENERIC_ATTACK_DAMAGE) || attribute.equals(EntityAttributes.GENERIC_ATTACK_SPEED)) {
                return;
            }

            if (!hasShownOtherHeader.get()) {
                textConsumer.accept(ScreenTexts.EMPTY);
                textConsumer.accept(Text.translatable("tooltip.mamy.other_attributes").formatted(Formatting.GRAY));
                hasShownOtherHeader.set(true);
            }

            appendVanillaAttributeModifier(textConsumer, attribute, modifier);
        });
    }

    // Utility methods
    public static MutableText createIconText(String iconChar) {
        return Text.literal(iconChar).setStyle(ICONS);
    }

    private static MutableText createIndentedText(String indent, String translationKey, double value, Formatting color) {
        return ScreenTexts.space()
                .append(Text.literal("  " + indent).formatted(Formatting.DARK_GRAY))
                .append(Text.translatable(translationKey).formatted(Formatting.GRAY))
                .append(Text.literal(": ").formatted(Formatting.WHITE))
                .append(Text.literal(format(value)).formatted(color));
    }

    private static String format(double value) {
        return AttributeModifiersComponent.DECIMAL_FORMAT.format(value);
    }

    private static boolean isArmorSlot(AttributeModifierSlot slot) {
        return slot == AttributeModifierSlot.HEAD || slot == AttributeModifierSlot.CHEST ||
                slot == AttributeModifierSlot.LEGS || slot == AttributeModifierSlot.FEET;
    }

    private static net.minecraft.entity.attribute.EntityAttributeModifier getAttributeModifier(
            ItemStack stack, RegistryEntry<EntityAttribute> attribute) {
        AttributeModifiersComponent modifiers = stack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);

        for (var entry : modifiers.modifiers()) {
            if (entry.attribute().equals(attribute) && entry.slot() == AttributeModifierSlot.MAINHAND) {
                return entry.modifier();
            }
        }
        return null;
    }

    private static void appendVanillaAttributeModifier(Consumer<Text> textConsumer,
                                                       net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attribute,
                                                       net.minecraft.entity.attribute.EntityAttributeModifier modifier) {
        double value = modifier.value();
        double displayValue = switch (modifier.operation()) {
            case ADD_MULTIPLIED_BASE, ADD_MULTIPLIED_TOTAL -> value * 100.0;
            default -> value;
        };

        if (value > 0.0) {
            textConsumer.accept(ScreenTexts.space()
                    .append(Text.literal("  + ").formatted(Formatting.GREEN))
                    .append(Text.translatable("attribute.modifier.plus." + modifier.operation().getId(),
                                    format(displayValue),
                                    Text.translatable(attribute.value().getTranslationKey()))
                            .formatted(attribute.value().getFormatting(true))));
        } else if (value < 0.0) {
            textConsumer.accept(ScreenTexts.space()
                    .append(Text.literal("  - ").formatted(Formatting.RED))
                    .append(Text.translatable("attribute.modifier.take." + modifier.operation().getId(),
                                    format(-displayValue),
                                    Text.translatable(attribute.value().getTranslationKey()))
                            .formatted(attribute.value().getFormatting(false))));
        }
    }
}