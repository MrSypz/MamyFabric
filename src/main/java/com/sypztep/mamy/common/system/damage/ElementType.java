package com.sypztep.mamy.common.system.damage;

import com.sypztep.mamy.client.util.IconAtlas;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.entry.RegistryEntry;

/**
 * Single source of truth for all element types
 * Separated from combat methods (CombatType)
 */
public enum ElementType {
    PHYSICAL("physical", 0x9C9393, IconAtlas.PHYSIC_ICON,
            ModEntityAttributes.PHYSICAL_ATTACK_DAMAGE_FLAT,
            ModEntityAttributes.PHYSICAL_ATTACK_DAMAGE_MULT,
            ModEntityAttributes.PHYSICAL_RESISTANCE,
            ModEntityAttributes.FLAT_PHYSICAL_REDUCTION),

    FIRE("fire", 0xFF4500, IconAtlas.FIRE_ICON,
            ModEntityAttributes.FIRE_ATTACK_DAMAGE_FLAT,
            ModEntityAttributes.FIRE_ATTACK_DAMAGE_MULT,
            ModEntityAttributes.FIRE_RESISTANCE,
            ModEntityAttributes.FLAT_FIRE_REDUCTION),

    COLD("cold", 0x70C1E3, IconAtlas.COLD_ICON,
            ModEntityAttributes.COLD_ATTACK_DAMAGE_FLAT,
            ModEntityAttributes.COLD_ATTACK_DAMAGE_MULT,
            ModEntityAttributes.COLD_RESISTANCE,
            ModEntityAttributes.FLAT_COLD_REDUCTION),

    ELECTRIC("electric", 0xFFD700,  IconAtlas.ELECTRIC_ICON,
            ModEntityAttributes.ELECTRIC_ATTACK_DAMAGE_FLAT,
            ModEntityAttributes.ELECTRIC_ATTACK_DAMAGE_MULT,
            ModEntityAttributes.ELECTRIC_RESISTANCE,
            ModEntityAttributes.FLAT_ELECTRIC_REDUCTION),

    WATER("water", 0x4169E1,  IconAtlas.WATER_ICON,
            ModEntityAttributes.WATER_ATTACK_DAMAGE_FLAT,
            ModEntityAttributes.WATER_ATTACK_DAMAGE_MULT,
            ModEntityAttributes.WATER_RESISTANCE,
            ModEntityAttributes.FLAT_WATER_REDUCTION),

    WIND("wind", 0x98FB98,  IconAtlas.WIND_ICON,
            ModEntityAttributes.WIND_ATTACK_DAMAGE_FLAT,
            ModEntityAttributes.WIND_ATTACK_DAMAGE_MULT,
            ModEntityAttributes.WIND_RESISTANCE,
            ModEntityAttributes.FLAT_WIND_REDUCTION),

    HOLY("holy", 0xDDA0DD,  IconAtlas.HOLY_ICON,
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