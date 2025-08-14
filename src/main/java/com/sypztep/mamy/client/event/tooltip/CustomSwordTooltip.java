package com.sypztep.mamy.client.event.tooltip;

import com.sypztep.mamy.common.data.ItemElementDataEntry;
import com.sypztep.mamy.common.init.ModEntityAttributes;
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

public final class CustomSwordTooltip extends AttributeTooltipHelper {

    // Weapon-specific icons
    private static final String SWORD_ICON = "\u0001";
    private static final String SPEED_ICON = "\u0002";

    public static void appendCustomAttributeTooltip(ItemStack stack, Consumer<Text> textConsumer, PlayerEntity player) {
        new CustomSwordTooltip().appendCustomTooltip(stack, textConsumer, player);
    }

    @Override
    public void appendCustomTooltip(ItemStack stack, Consumer<Text> textConsumer, PlayerEntity player) {
        AttributeModifiersComponent attributeModifiers = stack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);

        if (!attributeModifiers.showInTooltip()) {
            return;
        }

        boolean hasShownHeader = false;

        // 1. MELEE DAMAGE (always show if present)
        hasShownHeader = appendMeleeDamage(stack, textConsumer, player, hasShownHeader);

        // 2. ATTACK SPEED (always show if present)
        hasShownHeader = appendAttackSpeed(stack, textConsumer, player, hasShownHeader);

        // 3. ELEMENTAL DAMAGE (only if has elemental data)
        if (ItemElementDataEntry.hasEntry(stack.getItem())) {
            hasShownHeader = appendElementalDamage(stack, textConsumer, player, hasShownHeader);
        }

        // 4. OTHER ATTRIBUTES (always show if present)
        appendOtherAttributes(stack, textConsumer, player, hasShownHeader);
    }

    private boolean appendMeleeDamage(ItemStack stack, Consumer<Text> textConsumer, PlayerEntity player, boolean hasShownHeader) {
        var attackDamageModifier = getAttributeModifier(stack, EntityAttributes.GENERIC_ATTACK_DAMAGE, AttributeModifierSlot.MAINHAND);
        if (attackDamageModifier == null || player == null) {
            return hasShownHeader;
        }

        if (!hasShownHeader) {
            textConsumer.accept(ScreenTexts.EMPTY);
            textConsumer.accept(Text.translatable("item.modifiers.mainhand").formatted(Formatting.GRAY));
            hasShownHeader = true;
        }

        // Calculate total melee damage
        double baseDamage = attackDamageModifier.value();
        double playerAttackDamage = player.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        double meleeDamageFlat = player.getAttributeValue(ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT);
        double totalDamage = baseDamage + playerAttackDamage + meleeDamageFlat;

        // Main damage line
        MutableText comp = Text.empty();
        comp.append(createIconText(SWORD_ICON));
        comp.append(Text.translatable("attribute.name.generic.attack_damage").formatted(Formatting.WHITE));
        comp.append(Text.literal(": ").formatted(Formatting.GRAY));
        comp.append(Text.literal(AttributeModifiersComponent.DECIMAL_FORMAT.format(totalDamage)).formatted(Formatting.YELLOW));

        textConsumer.accept(ScreenTexts.space().append(comp));

        // Breakdown on shift (only if has elemental data)
        if (ItemElementDataEntry.hasEntry(stack.getItem()) && Screen.hasShiftDown()) {
            textConsumer.accept(
                    ScreenTexts.space()
                            .append(Text.literal("  ├─ ").formatted(Formatting.DARK_GRAY))
                            .append(Text.translatable("tooltip.mamy.base_weapon").formatted(Formatting.GRAY))
                            .append(Text.literal(": " + AttributeModifiersComponent.DECIMAL_FORMAT.format(baseDamage + playerAttackDamage)))
                            .formatted(Formatting.WHITE)
            );

            textConsumer.accept(
                    ScreenTexts.space()
                            .append(Text.literal("  └─ ").formatted(Formatting.DARK_GRAY))
                            .append(Text.translatable("tooltip.mamy.melee_bonus").formatted(Formatting.GRAY))
                            .append(Text.literal(": " + (meleeDamageFlat > 0 ? "+" : "") + AttributeModifiersComponent.DECIMAL_FORMAT.format(meleeDamageFlat)))
                            .formatted(meleeDamageFlat > 0 ? Formatting.GREEN : Formatting.DARK_GRAY)
            );
        }

        return hasShownHeader;
    }

    private boolean appendAttackSpeed(ItemStack stack, Consumer<Text> textConsumer, PlayerEntity player, boolean hasShownHeader) {
        var attackSpeedModifier = getAttributeModifier(stack, EntityAttributes.GENERIC_ATTACK_SPEED, AttributeModifierSlot.MAINHAND);
        if (attackSpeedModifier == null || player == null) {
            return hasShownHeader;
        }

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
        comp.append(Text.literal(AttributeModifiersComponent.DECIMAL_FORMAT.format(speedValue)).formatted(Formatting.YELLOW));

        textConsumer.accept(ScreenTexts.space().append(comp));

        return hasShownHeader;
    }

    private boolean appendElementalDamage(ItemStack stack, Consumer<Text> textConsumer, PlayerEntity player, boolean hasShownHeader) {
        if (!hasShownHeader) {
            textConsumer.accept(ScreenTexts.EMPTY);
            textConsumer.accept(Text.translatable("item.modifiers.mainhand").formatted(Formatting.GRAY));
            hasShownHeader = true;
        }

        var attackDamageModifier = getAttributeModifier(stack, EntityAttributes.GENERIC_ATTACK_DAMAGE, AttributeModifierSlot.MAINHAND);
        double baseDamage = 0;
        if (attackDamageModifier != null && player != null) {
            baseDamage = attackDamageModifier.value() + player.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        }

        appendElementalSection(stack, textConsumer, "tooltip.mamy.elemental_damage", "tooltip.mamy.power_budget", "damage_element.", baseDamage, true);

        return hasShownHeader;
    }

    private void appendOtherAttributes(ItemStack stack, Consumer<Text> textConsumer, PlayerEntity player, boolean hasShownHeader) {
        AtomicBoolean hasShownOtherHeader = new AtomicBoolean(false);

        stack.applyAttributeModifier(AttributeModifierSlot.MAINHAND, (attribute, modifier) -> {
            // Skip attributes we've already handled
            if (attribute.equals(EntityAttributes.GENERIC_ATTACK_DAMAGE) || attribute.equals(EntityAttributes.GENERIC_ATTACK_SPEED)) {
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
}