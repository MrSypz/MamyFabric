package com.sypztep.mamy.common.system.damage;

/**
 * Default implementations for common damage source patterns
 */
public class DefaultDamageSources {

    // Pure elemental sources (no combat bonuses)
    public static final HybridDamageSource PURE_FIRE = SimpleDamageSource.pureElemental(ElementType.FIRE);
    public static final HybridDamageSource PURE_COLD = SimpleDamageSource.pureElemental(ElementType.COLD);
    public static final HybridDamageSource PURE_ELECTRIC = SimpleDamageSource.pureElemental(ElementType.ELECTRIC);
    public static final HybridDamageSource PURE_WATER = SimpleDamageSource.pureElemental(ElementType.WATER);
    public static final HybridDamageSource PURE_WIND = SimpleDamageSource.pureElemental(ElementType.WIND);
    public static final HybridDamageSource PURE_HOLY = SimpleDamageSource.pureElemental(ElementType.HOLY);

    // Pure combat sources (Physical + combat bonuses)
    public static final HybridDamageSource PURE_MELEE = SimpleDamageSource.pureCombat(CombatType.MELEE);
    public static final HybridDamageSource PURE_RANGED = SimpleDamageSource.pureCombat(CombatType.RANGED);
    public static final HybridDamageSource PURE_MAGIC = SimpleDamageSource.pureCombat(CombatType.MAGIC);

    // Common hybrid combinations
    public static final HybridDamageSource FIRE_MELEE = SimpleDamageSource.hybrid(ElementType.FIRE, 0.8f, CombatType.MELEE, 0.3f);
    public static final HybridDamageSource FIRE_MAGIC = SimpleDamageSource.hybrid(ElementType.FIRE, 0.9f, CombatType.MAGIC, 0.2f);
    public static final HybridDamageSource COLD_RANGED = SimpleDamageSource.hybrid(ElementType.COLD, 0.8f, CombatType.RANGED, 0.3f);
}
