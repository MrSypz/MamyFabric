package com.sypztep.mamy.common.system.damage;

import java.util.List;
import java.util.Map;

/**
 * Utility class for working with damage components
 */
class DamageComponentUtils {

    /**
     * Normalize component weights to ensure they sum to 1.0
     */
    public static List<DamageComponent> normalizeWeights(List<DamageComponent> components) {
        if (components.isEmpty()) {
            return List.of(DamageComponent.pureCombat(CombatType.MELEE, 1.0f));
        }

        float totalWeight = (float) components.stream()
                .mapToDouble(DamageComponent::elementalWeight)
                .sum();

        if (totalWeight <= 0.0f) {
            return List.of(DamageComponent.pureCombat(CombatType.MELEE, 1.0f));
        }

        return components.stream()
                .map(c -> new DamageComponent(
                        c.elementType(),
                        c.elementalWeight() / totalWeight,
                        c.combatType(),
                        c.combatWeight()))
                .toList();
    }

    /**
     * Create default damage components based on combat type
     */
    public static List<DamageComponent> createDefaultComponents(CombatType combatType) {
        return List.of(DamageComponent.pureCombat(combatType, 1.0f));
    }

    /**
     * Merge multiple damage components into elemental breakdown
     */
    public static Map<ElementType, Float> mergeToElementalBreakdown(List<DamageComponent> components) {
        Map<ElementType, Float> breakdown = new java.util.HashMap<>();

        for (DamageComponent component : components) {
            breakdown.merge(component.elementType(), component.elementalWeight(), Float::sum);
        }

        return breakdown;
    }
}
