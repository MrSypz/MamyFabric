package com.sypztep.mamy.common.system.damage;

import com.sypztep.mamy.common.network.client.ElementalDamagePayloadS2C;
import com.sypztep.mamy.common.data.ItemElementDataEntry;
import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModTags;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.DamageTypeTags;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ElementalDamageSystem {
    private static final boolean DEBUG = true;

    private static void debugLog(String message, Object... args) {
        if (DEBUG) Mamy.LOGGER.info("[ElementalDamage] {}", String.format(message, args));
    }

    public record ElementalBreakdown(Map<ElementType, Float> elementalDamage, float totalDamage,
                                     DamageSource originalSource) {
        public ElementalBreakdown(Map<ElementType, Float> elementalDamage, DamageSource originalSource) {
            this(elementalDamage, elementalDamage.values().stream().reduce(0.0f, Float::sum), originalSource);
        }
    }

    public static ElementalBreakdown calculateElementalModifierWithBreakdown(LivingEntity defender, float incomingDamage, DamageSource source) {
        debugLog("====ELEMENTAL MODIFIER START====");
        debugLog("Original damage: %.2f, Source: %s", incomingDamage, source.getType());

        if (!(source.getAttacker() instanceof LivingEntity attacker)) {
            debugLog("Non-living attacker, applying damage source element check");
            return applyEnvironmentalDamageWithBreakdown(defender, incomingDamage, source);
        }

        ElementalBreakdown breakdown = splitDamageIntoElements(attacker, source, incomingDamage);
        ElementalBreakdown finalBreakdown = applyElementalResistancesWithBreakdown(defender, breakdown);

        debugLog("Final damage after elemental calculation: %.2f", finalBreakdown.totalDamage());
        debugLog("====ELEMENTAL MODIFIER END====");

        return finalBreakdown;
    }
    private static ElementalBreakdown applyEnvironmentalDamageWithBreakdown(LivingEntity defender, float damage, DamageSource source) {
        ElementType sourceElement = getElementTypeFromDamageSource(source);
        float resistance = (float) defender.getAttributeValue(sourceElement.resistance);
        float finalDamage = Math.max(0.0f, damage * (1.0f - resistance));
        debugLog("Environmental damage: %s, Resistance: %.2f, Final: %.2f", sourceElement.name(), resistance, finalDamage);

        return new ElementalBreakdown(Map.of(sourceElement, finalDamage), source);
    }
    private static ElementalBreakdown splitDamageIntoElements(LivingEntity attacker, DamageSource source, float totalDamage) {
        debugLog("=== SPLITTING DAMAGE INTO ELEMENTS ===");

        // Debug: Log all source tags for troubleshooting
        debugLog("Source tags check:");
        debugLog("  - IS_PLAYER_ATTACK: %b", source.isIn(DamageTypeTags.IS_PLAYER_ATTACK));
        debugLog("  - WIND_DAMAGE: %b", source.isIn(ModTags.DamageTags.WIND_DAMAGE));
        debugLog("  - FIRE_DAMAGE: %b", source.isIn(ModTags.DamageTags.FIRE_DAMAGE));
        debugLog("  - MAGIC_DAMAGE: %b", source.isIn(ModTags.DamageTags.MAGIC_DAMAGE));
        debugLog("  - MELEE_DAMAGE: %b", source.isIn(ModTags.DamageTags.MELEE_DAMAGE));

        // PRIORITY 1: Check if source implements HybridDamageSource (skills, projectiles)
        if (source instanceof HybridDamageSource hybridSource) {
            debugLog("ROUTE: Using hybrid damage source components");
            return createElementalBreakdownFromHybridSource(attacker, source, hybridSource, totalDamage);
        }

        // PRIORITY 2: Check damage source first for elemental types
        ElementType sourceElement = getElementTypeFromDamageSource(source);
        boolean hasElemental = hasElementalTag(source);
        boolean isBasicWeapon = isBasicWeaponAttack(source);

        debugLog("Element analysis:");
        debugLog("  - Source element: %s", sourceElement.name());
        debugLog("  - Has elemental tag: %b", hasElemental);
        debugLog("  - Is basic weapon: %b", isBasicWeapon);

        // If source has elemental type and is NOT a basic weapon attack, use source element
        if (sourceElement != ElementType.PHYSICAL && !isBasicWeapon) {
            debugLog("ROUTE: Using damage source element: %s", sourceElement.name());
            return createElementalBreakdownFromSource(attacker, source, sourceElement, totalDamage);
        }

        // PRIORITY 3: Use weapon elemental ratios for basic weapon attacks
        ItemStack weapon = attacker.getMainHandStack();
        if (isBasicWeapon && ItemElementDataEntry.hasEntry(weapon.getItem())) {
            debugLog("ROUTE: Using weapon elemental ratios for item: %s", weapon.getItem());
            return createElementalBreakdownFromWeapon(attacker, source, weapon, totalDamage);
        }

        // PRIORITY 4: Fallback to source element or physical
        debugLog("ROUTE: Fallback to source element: %s", sourceElement.name());
        return createElementalBreakdownFromSource(attacker, source, sourceElement, totalDamage);
    }

    /**
     * Create elemental breakdown from hybrid damage source (skills, projectiles)
     */
    private static ElementalBreakdown createElementalBreakdownFromHybridSource(LivingEntity attacker, DamageSource source, HybridDamageSource hybridSource, float totalDamage) {
        Map<ElementType, Float> elementalDamage = new HashMap<>();
        List<DamageComponent> components = DamageComponentUtils.normalizeWeights(hybridSource.getDamageComponents());

        debugLog("Processing %d damage components", components.size());

        for (DamageComponent component : components) {
            ElementType elementType = component.elementType;
            CombatType combatType = component.combatType;

            // Calculate base damage for this component
            float baseDamage = totalDamage * component.elementalWeight;

            // Apply elemental bonuses
            float elementalBonus = (float) attacker.getAttributeValue(elementType.damageFlat);
            float elementalAffinity = (float) attacker.getAttributeValue(elementType.damageMult);

            // Apply combat type bonuses (scaled by combatWeight)
            float combatBonus = 0.0f;
            float combatMultiplier = 1.0f;

            if (combatType.hasAttributes() && component.combatWeight > 0.0f) {
                combatBonus = (float) attacker.getAttributeValue(combatType.damageFlat) * component.combatWeight;
                combatMultiplier = 1.0f + ((float) attacker.getAttributeValue(combatType.damageMult) * component.combatWeight);
            }

            // Calculate final damage: (base + elemental_bonus + combat_bonus) * elemental_mult * combat_mult
            float finalComponentDamage = (baseDamage + elementalBonus + combatBonus) * (1.0f + elementalAffinity) * combatMultiplier;

            if (finalComponentDamage > 0) {
                elementalDamage.merge(elementType, finalComponentDamage, Float::sum);
                debugLog("%s/%s: %.2f base + %.2f ele_bonus + %.2f combat_bonus × %.2f ele_mult × %.2f combat_mult = %.2f",
                        elementType.name(), combatType.name(), baseDamage, elementalBonus, combatBonus,
                        1.0f + elementalAffinity, combatMultiplier, finalComponentDamage);
            }
        }

        if (elementalDamage.isEmpty()) {
            return createElementalBreakdownFromSource(attacker, source, ElementType.PHYSICAL, totalDamage);
        }

        return new ElementalBreakdown(elementalDamage, source);
    }

    /**
     * Create elemental breakdown based on damage source element
     */
    private static ElementalBreakdown createElementalBreakdownFromSource(LivingEntity attacker, DamageSource source, ElementType sourceElement, float totalDamage) {
        Map<ElementType, Float> elementalDamage = new HashMap<>();

        // Get elemental bonus and affinity from attacker
        float elementalBonus = (float) attacker.getAttributeValue(sourceElement.damageFlat);
        float affinity = (float) attacker.getAttributeValue(sourceElement.damageMult);

        // Calculate final damage with bonuses
        float finalElementDamage = (totalDamage + elementalBonus) * (1.0f + affinity);

        elementalDamage.put(sourceElement, finalElementDamage);

        debugLog("%s (from source): %.2f base + %.2f bonus × %.2f affinity = %.2f", sourceElement.name(), totalDamage, elementalBonus, 1.0f + affinity, finalElementDamage);

        return new ElementalBreakdown(elementalDamage, source);
    }

    /**
     * Create elemental breakdown based on weapon ratios
     */
    private static ElementalBreakdown createElementalBreakdownFromWeapon(LivingEntity attacker, DamageSource source, ItemStack weapon, float totalDamage) {
        Map<ElementType, Float> elementalDamage = new HashMap<>();
        ItemElementDataEntry itemData = ItemElementDataEntry.getEntry(weapon.getItem());
        double powerBudget = itemData.powerBudget();

        // Determine which combat type to use based on damage source
        CombatType activeCombatType = determineCombatTypeFromSource(source, itemData);

        // Apply combat scaling first
        float scaledDamage = totalDamage;
        if (activeCombatType != null && activeCombatType.hasAttributes()) {
            double combatRatio = itemData.combatRatios().getOrDefault(activeCombatType.damageFlat, 1.0);
            scaledDamage = totalDamage * (float) combatRatio;
            debugLog("Combat scaling: %.2f × %.2f (%s ratio) = %.2f", totalDamage, combatRatio, activeCombatType.name, scaledDamage);
        } else {
            debugLog("No combat scaling applied (no active combat type)");
        }

        // Now distribute the scaled damage across elemental ratios
        for (var entry : itemData.elementalRatios().entrySet()) {
            ElementType elementType = ElementType.fromDamageAttribute(entry.getKey());
            if (elementType != null && entry.getValue() > 0) {
                double ratio = entry.getValue();

                float baseDamage = (float) (scaledDamage * ratio * powerBudget);
                float elementalBonus = (float) attacker.getAttributeValue(elementType.damageFlat);
                float affinity = (float) attacker.getAttributeValue(elementType.damageMult);

                // Apply combat type bonuses if active combat type matches
                float combatBonus = 0.0f;
                float combatMultiplier = 1.0f;

                if (activeCombatType != null && activeCombatType.hasAttributes()) {
                    float combatWeight = (float) itemData.combatWeight();
                    combatBonus = (float) attacker.getAttributeValue(activeCombatType.damageFlat) * combatWeight;
                    combatMultiplier = 1.0f + ((float) attacker.getAttributeValue(activeCombatType.damageMult) * combatWeight);
                }

                float finalElementDamage = (baseDamage + elementalBonus + combatBonus) * (1.0f + affinity) * combatMultiplier;

                if (finalElementDamage > 0) {
                    elementalDamage.put(elementType, finalElementDamage);
                    debugLog("%s (from weapon): %.2f scaled_base + %.2f ele_bonus + %.2f %s_bonus × %.2f ele_mult × %.2f combat_mult = %.2f",
                            elementType.name(), baseDamage, elementalBonus, combatBonus,
                            activeCombatType != null ? activeCombatType.name : "none",
                            1.0f + affinity, combatMultiplier, finalElementDamage);
                }
            }
        }

        if (elementalDamage.isEmpty()) return createElementalBreakdownFromSource(attacker, source, ElementType.PHYSICAL, totalDamage);

        return new ElementalBreakdown(elementalDamage, source);
    }

    /**
     * Determine which combat type should be active based on the damage source
     */
    private static CombatType determineCombatTypeFromSource(DamageSource source, ItemElementDataEntry itemData) {
        // Check damage source tags to determine combat type
        if (source.isIn(ModTags.DamageTags.PROJECTILE_DAMAGE)) {
            // For projectile attacks, prefer RANGED if available
            if (itemData.combatRatios().containsKey(CombatType.RANGED.damageFlat)) {
                debugLog("Combat type: RANGED (projectile attack)");
                return CombatType.RANGED;
            }
        } else if (source.isIn(ModTags.DamageTags.MELEE_DAMAGE) || source.isIn(DamageTypeTags.IS_PLAYER_ATTACK)) {
            // For melee attacks, prefer MELEE if available
            if (itemData.combatRatios().containsKey(CombatType.MELEE.damageFlat)) {
                debugLog("Combat type: MELEE (melee attack)");
                return CombatType.MELEE;
            }
        } else if (source.isIn(ModTags.DamageTags.MAGIC_DAMAGE)) {
            // For magic attacks, prefer MAGIC if available
            if (itemData.combatRatios().containsKey(CombatType.MAGIC.damageFlat)) {
                debugLog("Combat type: MAGIC (magic attack)");
                return CombatType.MAGIC;
            }
        }

        // Fallback: use the combat type with highest ratio
        var highestCombat = itemData.combatRatios().entrySet().stream()
                .max((a, b) -> Double.compare(a.getValue(), b.getValue()))
                .orElse(null);

        if (highestCombat != null) {
            CombatType fallbackType = CombatType.fromDamageAttribute(highestCombat.getKey());
            debugLog("Combat type: %s (fallback - highest ratio)", fallbackType != null ? fallbackType.name : "unknown");
            return fallbackType;
        }

        debugLog("Combat type: none (no combat ratios)");
        return null;
    }

    private static float applyElementalResistances(LivingEntity defender, ElementalBreakdown breakdown) {
        return applyElementalResistancesWithBreakdown(defender, breakdown).totalDamage();
    }

    private static ElementalBreakdown applyElementalResistancesWithBreakdown(LivingEntity defender, ElementalBreakdown breakdown) {
        debugLog("=== APPLYING ELEMENTAL RESISTANCES ===");

        Map<ElementType, Float> armorResistances = calculateArmorResistances(defender);
        Map<ElementType, Float> finalDamageBreakdown = new HashMap<>();

        for (var entry : breakdown.elementalDamage.entrySet()) {
            ElementType element = entry.getKey();
            float elementDamage = entry.getValue();

            // Apply player resistance and armor resistance separately (multiplicative)
            float playerResistance = (float) defender.getAttributeValue(element.resistance);
            float armorResistance = armorResistances.getOrDefault(element, 0.0f);

            float afterPlayerRes = elementDamage * (1.0f - Math.min(0.95f, playerResistance));
            float afterArmorRes = afterPlayerRes * (1.0f - Math.min(0.95f, armorResistance));

            float flatReduction = (float) defender.getAttributeValue(element.flatReduction);
            float finalElementDamage = Math.max(0.0f, afterArmorRes - flatReduction);

            if (finalElementDamage > 0) {
                finalDamageBreakdown.put(element, finalElementDamage);
            }

            debugLog("%s: %.2f × (1 - %.3f player) × (1 - %.3f armor) = %.2f - %.2f flat = %.2f",
                    element.name(), elementDamage, playerResistance, armorResistance, afterArmorRes, flatReduction, finalElementDamage);
        }

        // Don't send damage numbers here - will be handled after special reductions
        return new ElementalBreakdown(finalDamageBreakdown, breakdown.originalSource);
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
        if (source.isIn(ModTags.DamageTags.FIRE_DAMAGE)) {
            return ElementType.FIRE;
        } else if (source.isIn(ModTags.DamageTags.COLD_DAMAGE)) {
            return ElementType.COLD;
        } else if (source.isIn(ModTags.DamageTags.ELECTRIC_DAMAGE)) {
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

    /**
     * Check if this is a basic weapon attack that should use weapon ratios
     */
    private static boolean isBasicWeaponAttack(DamageSource source) {
        if (hasElementalTag(source)) return false;

        return source.isIn(DamageTypeTags.IS_PLAYER_ATTACK) ||
                source.isIn(ModTags.DamageTags.MELEE_DAMAGE) ||
                source.isIn(ModTags.DamageTags.PROJECTILE_DAMAGE);
    }

    /**
     * Check if damage source has any elemental tag
     */
    private static boolean hasElementalTag(DamageSource source) {
        return source.isIn(ModTags.DamageTags.FIRE_DAMAGE) ||
                source.isIn(ModTags.DamageTags.COLD_DAMAGE) ||
                source.isIn(ModTags.DamageTags.ELECTRIC_DAMAGE) ||
                source.isIn(ModTags.DamageTags.WATER_DAMAGE) ||
                source.isIn(ModTags.DamageTags.WIND_DAMAGE) ||
                source.isIn(ModTags.DamageTags.HOLY_DAMAGE) ||
                source.isIn(ModTags.DamageTags.MAGIC_DAMAGE);
    }

    public static void sendDamageNumbers(LivingEntity target, ElementalBreakdown breakdown) {
        if (target.getWorld().isClient()) return;
        PlayerLookup.tracking(target).forEach(player -> ElementalDamagePayloadS2C.send(player, target.getId(), breakdown.elementalDamage, breakdown.elementalDamage.size() > 1));
    }
}