package com.sypztep.mamy.common.system.damage;

import com.sypztep.mamy.client.payload.ElementalDamagePayloadS2C;
import com.sypztep.mamy.common.data.ItemElementDataEntry;
import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModTags;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.DamageTypeTags;

import java.util.HashMap;
import java.util.Map;

public final class ElementalDamageSystem {
    private static final boolean DEBUG = true;

    private static void debugLog(String message, Object... args) {
        if (DEBUG) Mamy.LOGGER.info("[ElementalDamage] {}", String.format(message, args));
    }

    public record ElementalBreakdown(Map<ElementType, Float> elementalDamage, float totalDamage, DamageSource originalSource) {
        public ElementalBreakdown(Map<ElementType, Float> elementalDamage, DamageSource originalSource) {
            this(elementalDamage, elementalDamage.values().stream().reduce(0.0f, Float::sum), originalSource);
        }
    }

    public static float calculateElementalModifier(LivingEntity defender, float incomingDamage, DamageSource source) {
        debugLog("====ELEMENTAL MODIFIER START====");
        debugLog("Original damage: %.2f, Source: %s", incomingDamage, source.getType());

        if (!(source.getAttacker() instanceof LivingEntity attacker)) {
            debugLog("Non-living attacker, applying damage source element check");
            return applyEnvironmentalDamage(defender, incomingDamage, source);
        }

        ElementalBreakdown breakdown = splitDamageIntoElements(attacker, source, incomingDamage);
        float finalDamage = applyElementalResistances(defender, breakdown);

        debugLog("Final damage after elemental calculation: %.2f", finalDamage);
        debugLog("====ELEMENTAL MODIFIER END====");

        return finalDamage;
    }

    private static float applyEnvironmentalDamage(LivingEntity defender, float damage, DamageSource source) {
        ElementType sourceElement = getElementTypeFromDamageSource(source);
        float resistance = (float) defender.getAttributeValue(sourceElement.resistance);
        float finalDamage = Math.max(0.1f, damage * (1.0f - resistance));

        debugLog("Environmental damage: %s, Resistance: %.2f, Final: %.2f", sourceElement.name(), resistance, finalDamage);

        // Send particles for environmental damage
        sendDamageNumbers(defender, new ElementalBreakdown(Map.of(sourceElement, finalDamage), source));
        return finalDamage;
    }

    private static ElementalBreakdown splitDamageIntoElements(LivingEntity attacker, DamageSource source, float totalDamage) {
        debugLog("=== SPLITTING DAMAGE INTO ELEMENTS ===");

        ItemStack weapon = attacker.getMainHandStack();
        Map<ElementType, Double> ratios = getElementalRatios(weapon, source);
        double powerBudget = ItemElementDataEntry.hasEntry(weapon.getItem())
                ? ItemElementDataEntry.getEntry(weapon.getItem()).powerBudget()
                : 1.0;

        Map<ElementType, Float> elementalDamage = new HashMap<>();

        for (var entry : ratios.entrySet()) {
            ElementType element = entry.getKey();
            double ratio = entry.getValue();

            float baseDamage = (float) (totalDamage * ratio * powerBudget);
            float elementalBonus = (float) attacker.getAttributeValue(element.damageFlat);
            float affinity = (float) attacker.getAttributeValue(element.damageMult);
            float finalElementDamage = (baseDamage + elementalBonus) * (1.0f + affinity);

            if (finalElementDamage > 0) {
                elementalDamage.put(element, finalElementDamage);
                debugLog("%s: %.2f base + %.2f bonus × %.2f affinity = %.2f",
                        element.name(), baseDamage, elementalBonus, 1.0f + affinity, finalElementDamage);
            }
        }

        return new ElementalBreakdown(elementalDamage, source);
    }

    private static float applyElementalResistances(LivingEntity defender, ElementalBreakdown breakdown) {
        debugLog("=== APPLYING ELEMENTAL RESISTANCES ===");

        Map<ElementType, Float> armorResistances = calculateArmorResistances(defender);
        float totalFinalDamage = 0.0f;

        for (var entry : breakdown.elementalDamage.entrySet()) {
            ElementType element = entry.getKey();
            float elementDamage = entry.getValue();

            float playerResistance = (float) defender.getAttributeValue(element.resistance);
            float armorResistance = armorResistances.getOrDefault(element, 0.0f);
            float totalResistance = Math.min(0.95f, playerResistance + armorResistance);

            float afterResistance = elementDamage * (1.0f - totalResistance);
            float flatReduction = (float) defender.getAttributeValue(element.flatReduction);
            float finalElementDamage = Math.max(0.05f, afterResistance - flatReduction);

            totalFinalDamage += finalElementDamage;

            debugLog("%s: %.2f × (1 - %.3f total) = %.2f - %.2f flat = %.2f",
                    element.name(), elementDamage, totalResistance, afterResistance, flatReduction, finalElementDamage);
        }

        sendDamageNumbers(defender, breakdown);
        return Math.max(0.1f, totalFinalDamage);
    }

    private static Map<ElementType, Float> calculateArmorResistances(LivingEntity entity) {
        Map<ElementType, Float> totalResistances = new HashMap<>();

        for (ItemStack armorPiece : entity.getArmorItems()) {
            if (armorPiece.isEmpty() || !ItemElementDataEntry.hasEntry(armorPiece.getItem())) continue;

            ItemElementDataEntry entry = ItemElementDataEntry.getEntry(armorPiece.getItem());
            double powerBudget = entry.powerBudget();

            for (var ratioEntry : entry.damageRatios().entrySet()) {
                ElementType elementType = ElementType.fromResistanceAttribute(ratioEntry.getKey());
                if (elementType != null) {
                    float resistance = (float) (ratioEntry.getValue() * powerBudget);
                    totalResistances.merge(elementType, resistance, Float::sum);
                }
            }
        }

        return totalResistances;
    }

    private static ElementType getElementTypeFromDamageSource(DamageSource source) {
        if (source.isIn(ModTags.DamageTags.FIRE_DAMAGE) || source.isIn(DamageTypeTags.IS_FIRE)) {
            return ElementType.FIRE;
        } else if (source.isIn(ModTags.DamageTags.COLD_DAMAGE)) {
            return ElementType.COLD;
        } else if (source.isIn(ModTags.DamageTags.ELECTRIC_DAMAGE) || source.isIn(DamageTypeTags.IS_LIGHTNING)) {
            return ElementType.ELECTRIC;
        } else if (source.isIn(ModTags.DamageTags.WATER_DAMAGE)) {
            return ElementType.WATER;
        } else if (source.isIn(ModTags.DamageTags.WIND_DAMAGE)) {
            return ElementType.WIND;
        } else if (source.isIn(ModTags.DamageTags.HOLY_DAMAGE)) {
            return ElementType.HOLY;
        } else if (source.isIn(ModTags.DamageTags.MELEE_DAMAGE)) {
            return ElementType.PHYSICAL;
        } else {
            return ElementType.PHYSICAL;
        }
    }

    private static Map<ElementType, Double> getElementalRatios(ItemStack weapon, DamageSource source) {
        // Check for environmental damage first
        ElementType sourceElement = getElementTypeFromDamageSource(source);
        if (sourceElement != ElementType.PHYSICAL && !isWeaponBasedDamage(source)) {
            return Map.of(sourceElement, 1.0);
        }

        // Get weapon ratios
        if (!ItemElementDataEntry.hasEntry(weapon.getItem())) {
            return Map.of(ElementType.PHYSICAL, 1.0); // Default to 100% physical
        }

        Map<ElementType, Double> ratios = new HashMap<>();
        ItemElementDataEntry itemData = ItemElementDataEntry.getEntry(weapon.getItem());

        for (var entry : itemData.damageRatios().entrySet()) {
            ElementType elementType = ElementType.fromDamageAttribute(entry.getKey());
            if (elementType != null && entry.getValue() > 0) {
                ratios.put(elementType, entry.getValue());
            }
        }

        return ratios.isEmpty() ? Map.of(ElementType.PHYSICAL, 1.0) : ratios;
    }

    private static boolean isWeaponBasedDamage(DamageSource source) {
        return source.isIn(DamageTypeTags.IS_PLAYER_ATTACK) ||
                source.isIn(DamageTypeTags.IS_PROJECTILE) ||
                source.isIn(ModTags.DamageTags.MELEE_DAMAGE) ||
                source.getAttacker() instanceof LivingEntity;
    }

    public static void sendDamageNumbers(LivingEntity target, ElementalBreakdown breakdown) {
        if (target.getWorld().isClient()) return;
        PlayerLookup.tracking(target).forEach(player ->
                ElementalDamagePayloadS2C.send(player, target.getId(), breakdown.elementalDamage, breakdown.elementalDamage.size() > 1)
        );
    }
}