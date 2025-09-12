package com.sypztep.mamy.common.system.damage;

import java.util.List;

/**
 * Simple implementation for sources with single damage type
 */
class SimpleDamageSource implements HybridDamageSource {
    private final List<DamageComponent> components;

    public SimpleDamageSource(DamageComponent component) {
        this.components = List.of(component);
    }

    public SimpleDamageSource(List<DamageComponent> components) {
        this.components = DamageComponentUtils.normalizeWeights(components);
    }

    @Override
    public List<DamageComponent> getDamageComponents() {
        return components;
    }

    // Static factory methods for common cases
    public static SimpleDamageSource pureElemental(ElementType elementType) {
        return new SimpleDamageSource(DamageComponent.pureElemental(elementType, 1.0f));
    }

    public static SimpleDamageSource pureCombat(CombatType combatType) {
        return new SimpleDamageSource(DamageComponent.pureCombat(combatType, 1.0f));
    }

    public static SimpleDamageSource hybrid(ElementType elementType, float elementalWeight,
                                            CombatType combatType, float combatWeight) {
        return new SimpleDamageSource(DamageComponent.hybrid(elementType, elementalWeight, combatType, combatWeight));
    }
}
