package com.sypztep.mamy.common.system.damage;

import com.sypztep.mamy.common.init.ModEntityAttributes;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.entry.RegistryEntry;

/**
 * Defines combat methods/attack categories separate from elemental damage types
 */
public enum CombatType {
    MELEE("melee",
            ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT,
            ModEntityAttributes.MELEE_ATTACK_DAMAGE_MULT,
            ModEntityAttributes.MELEE_RESISTANCE,
            ModEntityAttributes.FLAT_MELEE_REDUCTION),

    RANGED("ranged",
            ModEntityAttributes.PROJECTILE_ATTACK_DAMAGE_FLAT,
            ModEntityAttributes.PROJECTILE_ATTACK_DAMAGE_MULT,
            ModEntityAttributes.PROJECTILE_RESISTANCE,
            ModEntityAttributes.FLAT_PROJECTILE_REDUCTION),

    MAGIC("magic",
            ModEntityAttributes.MAGIC_ATTACK_DAMAGE_FLAT,
            ModEntityAttributes.MAGIC_ATTACK_DAMAGE_MULT,
            ModEntityAttributes.MAGIC_RESISTANCE,
            ModEntityAttributes.FLAT_MAGIC_REDUCTION),

    HYBRID("hybrid",
            null, // No direct attributes - calculated from components
            null,
            null,
            null),

    PURE("pure",
            null, // Pure elemental - no combat type bonuses
            null,
            null,
            null);

    public final String name;
    public final RegistryEntry<EntityAttribute> damageFlat;
    public final RegistryEntry<EntityAttribute> damageMult;
    public final RegistryEntry<EntityAttribute> resistance;
    public final RegistryEntry<EntityAttribute> flatReduction;

    CombatType(String name,
               RegistryEntry<EntityAttribute> damageFlat,
               RegistryEntry<EntityAttribute> damageMult,
               RegistryEntry<EntityAttribute> resistance,
               RegistryEntry<EntityAttribute> flatReduction) {
        this.name = name;
        this.damageFlat = damageFlat;
        this.damageMult = damageMult;
        this.resistance = resistance;
        this.flatReduction = flatReduction;
    }

    /**
     * Check if this combat type has associated attributes
     */
    public boolean hasAttributes() {
        return damageFlat != null;
    }

    public static CombatType fromDamageAttribute(RegistryEntry<EntityAttribute> attribute) {
        for (CombatType type : values()) {
            if (type.hasAttributes() && type.damageFlat.equals(attribute)) {
                return type;
            }
        }
        return PURE; // default - no combat type bonuses
    }

    public static CombatType fromResistanceAttribute(RegistryEntry<EntityAttribute> attribute) {
        for (CombatType type : values()) {
            if (type.hasAttributes() && type.resistance.equals(attribute)) {
                return type;
            }
        }
        return PURE; // default
    }
}