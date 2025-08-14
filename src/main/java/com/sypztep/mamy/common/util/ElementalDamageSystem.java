package com.sypztep.mamy.common.util;

import com.sypztep.mamy.client.payload.ElementalDamagePayloadS2C;
import com.sypztep.mamy.common.data.ItemElementDataEntry;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModTags;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.entity.attribute.EntityAttribute;

import java.util.HashMap;
import java.util.Map;

public final class ElementalDamageSystem {
    private static final boolean DEBUG = true;

    private static void debugLog(String message, Object... args) {
        if (DEBUG) Mamy.LOGGER.info("[ElementalDamage] {}", String.format(message, args));
    }

    // Elemental types matching your attributes
    public enum ElementType {
        PHYSICAL("physical", null, null, null),
        HEAT("heat", ModEntityAttributes.FIRE_ATTACK_DAMAGE_FLAT, ModEntityAttributes.FIRE_ATTACK_DAMAGE_MULT, ModEntityAttributes.FIRE_RESISTANCE),
        ELECTRIC("electric", ModEntityAttributes.ELECTRIC_ATTACK_DAMAGE_FLAT, ModEntityAttributes.ELECTRIC_ATTACK_DAMAGE_MULT, ModEntityAttributes.ELECTRIC_RESISTANCE),
        WATER("water", ModEntityAttributes.WATER_ATTACK_DAMAGE_FLAT, ModEntityAttributes.WATER_ATTACK_DAMAGE_MULT, ModEntityAttributes.WATER_RESISTANCE),
        WIND("wind", ModEntityAttributes.WIND_ATTACK_DAMAGE_FLAT, ModEntityAttributes.WIND_ATTACK_DAMAGE_MULT, ModEntityAttributes.WIND_RESISTANCE),
        HOLY("holy", ModEntityAttributes.HOLY_ATTACK_DAMAGE_FLAT, ModEntityAttributes.HOLY_ATTACK_DAMAGE_MULT, ModEntityAttributes.HOLY_RESISTANCE);

        public final String name;
        public final RegistryEntry<EntityAttribute> baseAttribute;
        public final RegistryEntry<EntityAttribute> affinityAttribute;
        public final RegistryEntry<EntityAttribute> resistAttribute;

        ElementType(String name, RegistryEntry<EntityAttribute> baseAttribute,
                    RegistryEntry<EntityAttribute> affinityAttribute,
                    RegistryEntry<EntityAttribute> resistAttribute) {
            this.name = name;
            this.baseAttribute = baseAttribute;
            this.affinityAttribute = affinityAttribute;
            this.resistAttribute = resistAttribute;
        }
    }

    public static class ElementalBreakdown {
        public final Map<ElementType, Float> elementalDamage;
        public final float totalDamage;
        public final DamageSource originalSource;

        public ElementalBreakdown(Map<ElementType, Float> elementalDamage, DamageSource originalSource) {
            this.elementalDamage = elementalDamage;
            this.totalDamage = elementalDamage.values().stream().reduce(0.0f, Float::sum);
            this.originalSource = originalSource;
        }
    }

    /**
     * Main method: Apply elemental damage calculation as a modifier
     */
    public static float calculateElementalModifier(LivingEntity defender, float incomingDamage, DamageSource source) {
        debugLog("====ELEMENTAL MODIFIER START====");
        debugLog("Original damage: %.2f, Source: %s", incomingDamage, source.getType());

        if (!(source.getAttacker() instanceof LivingEntity attacker)) {
            debugLog("Non-living attacker, applying damage source element check");
            return applySourceElementalDamage(defender, incomingDamage, source);
        }

        // Step 1: Split damage into elemental components based on weapon
        ElementalBreakdown breakdown = splitDamageIntoElements(attacker, source, incomingDamage);

        // Step 2: Apply defender's elemental resistances
        float finalDamage = applyElementalResistances(defender, breakdown);

        debugLog("Final damage after elemental calculation: %.2f", finalDamage);
        debugLog("====ELEMENTAL MODIFIER END====");

        return finalDamage;
    }

    /**
     * Handle damage sources without living attackers (like fire, magic effects, etc.)
     */
    private static float applySourceElementalDamage(LivingEntity defender, float damage, DamageSource source) {
        ElementType sourceElement = getElementTypeFromDamageSource(source);
        float resistance = getElementalResistance(defender, sourceElement);
        float finalDamage = damage * (1.0f - resistance);

        debugLog("Source element: %s, Resistance: %.2f, Final: %.2f",
                sourceElement.name, resistance, finalDamage);

        return Math.max(0.1f, finalDamage);
    }

    /**
     * Split incoming damage into elemental components based on attacker's weapon
     */
    private static ElementalBreakdown splitDamageIntoElements(LivingEntity attacker, DamageSource source, float totalDamage) {
        debugLog("=== SPLITTING DAMAGE INTO ELEMENTS ===");

        Map<ElementType, Float> elementalDamage = new HashMap<>();

        // Get weapon elemental ratios
        ItemStack weapon = attacker.getMainHandStack();
        Map<ElementType, Double> ratios = getElementalRatios(weapon, source);

        double powerBudget = getPowerBudget(weapon);

        debugLog("Weapon: %s, Ratios: %s, PowerBudget: %.2f", weapon.getItem().toString(), ratios, powerBudget);

        // Split base damage according to ratios, then apply attacker bonuses
        for (Map.Entry<ElementType, Double> entry : ratios.entrySet()) {
            ElementType element = entry.getKey();
            double ratio = entry.getValue();

            float baseDamage = (float) (totalDamage * ratio * powerBudget);

            // Add attacker's base elemental damage
            float elementalBonus = getElementalBonus(attacker, element);

            // Apply attacker's elemental affinity (multiplier)
            float affinity = getElementalAffinity(attacker, element);

            float finalElementDamage = (baseDamage + elementalBonus) * (1.0f + affinity);

            if (finalElementDamage > 0) {
                elementalDamage.put(element, finalElementDamage);
                debugLog("%s: %.2f base + %.2f bonus × %.2f affinity = %.2f",
                        element.name, baseDamage, elementalBonus, 1.0f + affinity, finalElementDamage);
            }
        }

        return new ElementalBreakdown(elementalDamage, source);
    }

    private static double getPowerBudget(ItemStack weapon) {
        if (ItemElementDataEntry.hasEntry(weapon.getItem())) {
            return ItemElementDataEntry.getEntry(weapon.getItem()).powerBudget();
        }
        return 1.0; // Default power budget
    }

    /**
     * Apply defender's resistances to each elemental damage type
     */
    private static float applyElementalResistances(LivingEntity defender, ElementalBreakdown breakdown) {
        debugLog("=== APPLYING ELEMENTAL RESISTANCES ===");

        // Debug armor resistances
        debugLog("=== ARMOR RESISTANCE DEBUG ===");
        Map<ElementType, Float> armorResistances = calculateArmorResistances(defender);
        armorResistances.forEach((element, resistance) ->
                debugLog("Armor %s resistance: %.3f", element.name, resistance));
        debugLog("=== END ARMOR DEBUG ===");

        float totalFinalDamage = 0.0f;

        for (Map.Entry<ElementType, Float> entry : breakdown.elementalDamage.entrySet()) {
            ElementType element = entry.getKey();
            float elementDamage = entry.getValue();

            // Get combined resistance (player stats + armor)
            float playerResistance = getPlayerElementalResistance(defender, element);
            float armorResistance = armorResistances.getOrDefault(element, 0.0f);
            float totalResistance = Math.min(0.95f, playerResistance + armorResistance); // Cap at 95%

            float finalElementDamage = elementDamage * (1.0f - totalResistance);
            finalElementDamage = Math.max(0.05f, finalElementDamage);

            totalFinalDamage += finalElementDamage;

            debugLog("%s: %.2f × (1 - %.3f player - %.3f armor = %.3f total) = %.2f",
                    element.name, elementDamage, playerResistance, armorResistance, totalResistance, finalElementDamage);
        }
        sendDamageNumbers(defender, breakdown);

        return Math.max(0.1f, totalFinalDamage);
    }
    private static Map<ElementType, Float> calculateArmorResistances(LivingEntity entity) {
        Map<ElementType, Float> totalResistances = new HashMap<>();

        for (ItemStack armorPiece : entity.getArmorItems()) {
            if (armorPiece.isEmpty()) continue;

            debugLog("Checking armor: %s", armorPiece.getItem());

            if (ItemElementDataEntry.hasEntry(armorPiece.getItem())) {
                ItemElementDataEntry entry = ItemElementDataEntry.getEntry(armorPiece.getItem());
                debugLog("  Has elemental data: %s", entry.damageRatios());
                debugLog("  Power budget: %.2f", entry.powerBudget());

                // Convert armor ratios to resistance values
                for (Map.Entry<RegistryEntry<EntityAttribute>, Double> ratioEntry : entry.damageRatios().entrySet()) {
                    ElementType elementType = attributeToElementType(ratioEntry.getKey());
                    if (elementType != null) {
                        double ratio = ratioEntry.getValue();
                        double powerBudget = entry.powerBudget();

                        // Calculate resistance: ratio × powerBudget
                        float resistance = (float) (ratio * powerBudget);

                        totalResistances.merge(elementType, resistance, Float::sum);

                        debugLog("    %s: %.3f ratio × %.2f budget = %.3f resistance",
                                elementType.name, ratio, powerBudget, resistance);
                    }
                }
            } else {
                debugLog("  No elemental data found");
            }
        }

        return totalResistances;
    }

    private static float getPlayerElementalResistance(LivingEntity entity, ElementType element) {
        if (element == ElementType.PHYSICAL) {
            return (float) entity.getAttributeValue(ModEntityAttributes.MELEE_RESISTANCE);
        }
        if (element.resistAttribute == null) return 0.0f;
        return (float) entity.getAttributeValue(element.resistAttribute);
    }

    private static float getElementalResistance(LivingEntity entity, ElementType element) {
        return getPlayerElementalResistance(entity, element);
    }
    /**
     * Check if damage is weapon-based (should use weapon ratios)
     */
    private static boolean isWeaponBasedDamage(DamageSource source) {
        return source.isIn(ModTags.DamageTags.MELEE_DAMAGE) ||
                source.isIn(ModTags.DamageTags.PROJECTILE_DAMAGE);
    }

    /**
     * Map damage source to ElementType using damage tags
     */
    private static ElementType getElementTypeFromDamageSource(DamageSource source) {
        if (source.isIn(ModTags.DamageTags.FIRE_DAMAGE)) return ElementType.HEAT;
        if (source.isIn(ModTags.DamageTags.WATER_DAMAGE)) return ElementType.WATER;
        if (source.isIn(ModTags.DamageTags.ELECTRIC_DAMAGE)) return ElementType.ELECTRIC;
        if (source.isIn(ModTags.DamageTags.WIND_DAMAGE)) return ElementType.WIND;
        if (source.isIn(ModTags.DamageTags.HOLY_DAMAGE)) return ElementType.HOLY;
        if (source.isIn(ModTags.DamageTags.MELEE_DAMAGE)) return ElementType.PHYSICAL;
        return ElementType.PHYSICAL; // default
    }

    // Helper methods for getting attribute values
    private static float getElementalBonus(LivingEntity entity, ElementType element) {
        if (element == ElementType.PHYSICAL) {
            return (float) entity.getAttributeValue(ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT);
        }
        if (element.baseAttribute == null) return 0.0f;
        return (float) entity.getAttributeValue(element.baseAttribute);
    }

    private static float getElementalAffinity(LivingEntity entity, ElementType element) {
        if (element == ElementType.PHYSICAL) {
            return (float) entity.getAttributeValue(ModEntityAttributes.MELEE_ATTACK_DAMAGE_MULT);
        }
        if (element.affinityAttribute == null) return 0.0f;
        return (float) entity.getAttributeValue(element.affinityAttribute);
    }

    // Update the existing method to convert from attributes to ElementType
    private static Map<ElementType, Double> getElementalRatios(ItemStack weapon, DamageSource source) {
        Map<ElementType, Double> ratios = new HashMap<>();

        // First check if damage source itself has elemental type
        ElementType sourceElement = getElementTypeFromDamageSource(source);
        if (sourceElement != ElementType.PHYSICAL && !isWeaponBasedDamage(source)) {
            ratios.put(sourceElement, 1.0);
            return ratios;
        }

        // Convert attribute-based ratios to ElementType ratios
        if (ItemElementDataEntry.hasEntry(weapon.getItem())) {
            ItemElementDataEntry itemData = ItemElementDataEntry.getEntry(weapon.getItem());

            for (Map.Entry<RegistryEntry<EntityAttribute>, Double> entry : itemData.damageRatios().entrySet()) {
                RegistryEntry<EntityAttribute> attribute = entry.getKey();
                Double ratio = entry.getValue();

                ElementType elementType = attributeToElementType(attribute);
                if (elementType != null) {
                    ratios.put(elementType, ratio);
                }
            }
        }

        // Default to 100% physical if no ratios found
        if (ratios.isEmpty()) {
            ratios.put(ElementType.PHYSICAL, 0.0);
        }

        return ratios;
    }

    private static ElementType attributeToElementType(RegistryEntry<EntityAttribute> attribute) {
        // DAMAGE attributes (for weapons)
        if (attribute.equals(ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT)) return ElementType.PHYSICAL;
        if (attribute.equals(ModEntityAttributes.FIRE_ATTACK_DAMAGE_FLAT)) return ElementType.HEAT;
        if (attribute.equals(ModEntityAttributes.ELECTRIC_ATTACK_DAMAGE_FLAT)) return ElementType.ELECTRIC;
        if (attribute.equals(ModEntityAttributes.WATER_ATTACK_DAMAGE_FLAT)) return ElementType.WATER;
        if (attribute.equals(ModEntityAttributes.WIND_ATTACK_DAMAGE_FLAT)) return ElementType.WIND;
        if (attribute.equals(ModEntityAttributes.HOLY_ATTACK_DAMAGE_FLAT)) return ElementType.HOLY;

        // RESISTANCE attributes (for armor)
        if (attribute.equals(ModEntityAttributes.MELEE_RESISTANCE)) return ElementType.PHYSICAL;
        if (attribute.equals(ModEntityAttributes.FIRE_RESISTANCE)) return ElementType.HEAT;
        if (attribute.equals(ModEntityAttributes.ELECTRIC_RESISTANCE)) return ElementType.ELECTRIC;
        if (attribute.equals(ModEntityAttributes.WATER_RESISTANCE)) return ElementType.WATER;
        if (attribute.equals(ModEntityAttributes.WIND_RESISTANCE)) return ElementType.WIND;
        if (attribute.equals(ModEntityAttributes.HOLY_RESISTANCE)) return ElementType.HOLY;

        return null;
    }
    public static void sendDamageNumbers(LivingEntity target, ElementalBreakdown breakdown) {
        if (target.getWorld().isClient()) return;

        // Send to all nearby players
        PlayerLookup.tracking(target).forEach(player ->
                ElementalDamagePayloadS2C.send(
                        player,
                        target.getId(),
                        breakdown.elementalDamage,
                        breakdown.elementalDamage.size() > 1 // Show breakdown if multiple elements
                )
        );
    }
}