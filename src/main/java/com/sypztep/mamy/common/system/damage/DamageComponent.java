package com.sypztep.mamy.common.system.damage;

/**
 * Represents a single damage component with both elemental and combat type scaling
 * Used for hybrid damage calculations (e.g., Fire Melee Attack)
 *
 * @param elementalWeight 0.0 - 1.0, how much this element scales
 * @param combatWeight    0.0 - 1.0, how much combat type bonuses apply
 */
public record DamageComponent(ElementType elementType, float elementalWeight, CombatType combatType,
                              float combatWeight) {
    public DamageComponent(ElementType elementType, float elementalWeight,
                           CombatType combatType, float combatWeight) {
        this.elementType = elementType;
        this.elementalWeight = Math.max(0.0f, Math.min(1.0f, elementalWeight));
        this.combatType = combatType;
        this.combatWeight = Math.max(0.0f, Math.min(1.0f, combatWeight));
    }

    /**
     * Create a pure elemental component (no combat type bonuses)
     */
    public static DamageComponent pureElemental(ElementType elementType, float weight) {
        return new DamageComponent(elementType, weight, CombatType.PURE, 0.0f);
    }

    /**
     * Create a pure combat component (Physical element with combat bonuses)
     */
    public static DamageComponent pureCombat(CombatType combatType, float weight) {
        return new DamageComponent(ElementType.PHYSICAL, weight, combatType, 1.0f);
    }

    /**
     * Create a hybrid component (both elemental and combat scaling)
     */
    public static DamageComponent hybrid(ElementType elementType, float elementalWeight,
                                         CombatType combatType, float combatWeight) {
        return new DamageComponent(elementType, elementalWeight, combatType, combatWeight);
    }

    @Override
    public String toString() {
        if (combatType == CombatType.PURE) {
            return String.format("%s(%.1f%%)", elementType.name, elementalWeight * 100);
        } else if (elementType == ElementType.PHYSICAL && combatWeight >= 0.9f) {
            return String.format("%s(%.1f%%)", combatType.name, elementalWeight * 100);
        } else {
            return String.format("%s/%s(%.1f%%/%.1f%%)",
                    elementType.name, combatType.name,
                    elementalWeight * 100, combatWeight * 100);
        }
    }
}
