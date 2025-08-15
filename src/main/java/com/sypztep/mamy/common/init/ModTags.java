package com.sypztep.mamy.common.init;

import com.sypztep.mamy.Mamy;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public final class ModTags {
    public static class DamageTags {
        // Combat damage types
        public static final TagKey<DamageType> MELEE_DAMAGE = TagKey.of(RegistryKeys.DAMAGE_TYPE, Mamy.id("melee_damage"));
        public static final TagKey<DamageType> PROJECTILE_DAMAGE = TagKey.of(RegistryKeys.DAMAGE_TYPE, Mamy.id("projectile_damage"));
        public static final TagKey<DamageType> MAGIC_DAMAGE = TagKey.of(RegistryKeys.DAMAGE_TYPE, Mamy.id("magic_damage"));

        // Elemental damage types
        public static final TagKey<DamageType> FIRE_DAMAGE = TagKey.of(RegistryKeys.DAMAGE_TYPE, Mamy.id("fire_damage"));
        public static final TagKey<DamageType> ELECTRIC_DAMAGE = TagKey.of(RegistryKeys.DAMAGE_TYPE, Mamy.id("electric_damage"));
        public static final TagKey<DamageType> WATER_DAMAGE = TagKey.of(RegistryKeys.DAMAGE_TYPE, Mamy.id("water_damage"));
        public static final TagKey<DamageType> WIND_DAMAGE = TagKey.of(RegistryKeys.DAMAGE_TYPE, Mamy.id("wind_damage"));
        public static final TagKey<DamageType> HOLY_DAMAGE = TagKey.of(RegistryKeys.DAMAGE_TYPE, Mamy.id("holy_damage"));
        public static final TagKey<DamageType> COLD_DAMAGE = TagKey.of(RegistryKeys.DAMAGE_TYPE, Mamy.id("cold_damage"));
    }
    public static class EntityTypes {
        public static final TagKey<EntityType<?>> CANNOT_HEADSHOT = TagKey.of(RegistryKeys.ENTITY_TYPE, Mamy.id("cannot_headshot"));
    }
}
