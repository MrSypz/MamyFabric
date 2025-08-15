package com.sypztep.mamy.common.util;

import com.sypztep.mamy.common.init.ModEntityAttributes;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.entry.RegistryEntry;

/**
 * Single source of truth for all element types
 */
public enum ElementType {
    PHYSICAL("physical", 0x9C9393, "\u0003",
            ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT,
            ModEntityAttributes.MELEE_ATTACK_DAMAGE_MULT,
            ModEntityAttributes.MELEE_RESISTANCE,
            ModEntityAttributes.FLAT_MELEE_REDUCTION),

    FIRE("fire", 0xFF4500, "\u0004",
            ModEntityAttributes.FIRE_ATTACK_DAMAGE_FLAT,
            ModEntityAttributes.FIRE_ATTACK_DAMAGE_MULT,
            ModEntityAttributes.FIRE_RESISTANCE,
            ModEntityAttributes.FLAT_FIRE_REDUCTION),

    COLD("cold", 0x70C1E3, "\u0005",
            ModEntityAttributes.COLD_ATTACK_DAMAGE_FLAT,
            ModEntityAttributes.COLD_ATTACK_DAMAGE_MULT,
            ModEntityAttributes.COLD_RESISTANCE,
            ModEntityAttributes.FLAT_COLD_REDUCTION),

    ELECTRIC("electric", 0xFFD700, "\u0006",
            ModEntityAttributes.ELECTRIC_ATTACK_DAMAGE_FLAT,
            ModEntityAttributes.ELECTRIC_ATTACK_DAMAGE_MULT,
            ModEntityAttributes.ELECTRIC_RESISTANCE,
            ModEntityAttributes.FLAT_ELECTRIC_REDUCTION),

    WATER("water", 0x4169E1, "\u0007",
            ModEntityAttributes.WATER_ATTACK_DAMAGE_FLAT,
            ModEntityAttributes.WATER_ATTACK_DAMAGE_MULT,
            ModEntityAttributes.WATER_RESISTANCE,
            ModEntityAttributes.FLAT_WATER_REDUCTION),

    WIND("wind", 0x98FB98, "\u0008",
            ModEntityAttributes.WIND_ATTACK_DAMAGE_FLAT,
            ModEntityAttributes.WIND_ATTACK_DAMAGE_MULT,
            ModEntityAttributes.WIND_RESISTANCE,
            ModEntityAttributes.FLAT_WIND_REDUCTION),

    HOLY("holy", 0xDDA0DD, "\u0009",
            ModEntityAttributes.HOLY_ATTACK_DAMAGE_FLAT,
            ModEntityAttributes.HOLY_ATTACK_DAMAGE_MULT,
            ModEntityAttributes.HOLY_RESISTANCE,
            ModEntityAttributes.FLAT_HOLY_REDUCTION);

    public final String name;
    public final int color;
    public final String icon;
    public final RegistryEntry<EntityAttribute> damageFlat;
    public final RegistryEntry<EntityAttribute> damageMult;
    public final RegistryEntry<EntityAttribute> resistance;
    public final RegistryEntry<EntityAttribute> flatReduction;

    ElementType(String name, int color, String icon,
                RegistryEntry<EntityAttribute> damageFlat,
                RegistryEntry<EntityAttribute> damageMult,
                RegistryEntry<EntityAttribute> resistance,
                RegistryEntry<EntityAttribute> flatReduction) {
        this.name = name;
        this.color = color;
        this.icon = icon;
        this.damageFlat = damageFlat;
        this.damageMult = damageMult;
        this.resistance = resistance;
        this.flatReduction = flatReduction;
    }

    // Pattern matching for attributes (Java 21)
    public static ElementType fromDamageAttribute(RegistryEntry<EntityAttribute> attribute) {
        for (ElementType type : values())
            if (type.damageFlat.equals(attribute)) return type;
        return PHYSICAL; // default
    }

    public static ElementType fromResistanceAttribute(RegistryEntry<EntityAttribute> attribute) {
        for (ElementType type : values())
            if (type.resistance.equals(attribute)) return type;
        return PHYSICAL; // default
    }
}