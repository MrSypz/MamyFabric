package com.sypztep.mamy.common.system.damage;

import java.util.List;

/**
 * Interface for damage sources that can define their own elemental and combat type scaling
 * This allows skills, projectiles, and other damage sources to specify hybrid damage components
 */
public interface HybridDamageSource {

    /**
     * Get the damage components for this source
     * @return List of damage components with their elemental and combat scaling
     */
    List<DamageComponent> getDamageComponents();

    /**
     * Check if this source has custom damage components defined
     */
    default boolean hasCustomDamageComponents() {
        return !getDamageComponents().isEmpty();
    }

    /**
     * Get the total elemental weight (should usually sum to 1.0)
     */
    default float getTotalElementalWeight() {
        return (float) getDamageComponents().stream()
                .mapToDouble(c -> c.elementalWeight)
                .sum();
    }

    /**
     * Get the maximum combat weight across all components
     */
    default float getMaxCombatWeight() {
        return (float) getDamageComponents().stream()
                .mapToDouble(c -> c.combatWeight)
                .max()
                .orElse(0.0);
    }

    /**
     * Check if this source has any elemental components
     */
    default boolean hasElementalComponents() {
        return getDamageComponents().stream()
                .anyMatch(c -> c.elementType != ElementType.PHYSICAL || c.elementalWeight > 0);
    }

    /**
     * Check if this source has any combat type scaling
     */
    default boolean hasCombatComponents() {
        return getDamageComponents().stream()
                .anyMatch(c -> c.combatType != CombatType.PURE && c.combatWeight > 0);
    }

    /**
     * Get primary element type (the one with highest weight)
     */
    default ElementType getPrimaryElementType() {
        return getDamageComponents().stream()
                .max((a, b) -> Float.compare(a.elementalWeight, b.elementalWeight))
                .map(c -> c.elementType)
                .orElse(ElementType.PHYSICAL);
    }

    /**
     * Get primary combat type (the one with highest weight)
     */
    default CombatType getPrimaryCombatType() {
        return getDamageComponents().stream()
                .max((a, b) -> Float.compare(a.combatWeight, b.combatWeight))
                .map(c -> c.combatType)
                .orElse(CombatType.PURE);
    }
}

