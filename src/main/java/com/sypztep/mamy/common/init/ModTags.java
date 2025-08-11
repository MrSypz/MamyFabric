package com.sypztep.mamy.common.init;

import com.sypztep.mamy.Mamy;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public final class ModTags {
    public static class DamageTags {
        public static final TagKey<DamageType> MELEE_DAMAGE = TagKey.of(RegistryKeys.DAMAGE_TYPE, Mamy.id("melee_damage"));
        public static final TagKey<DamageType> MAGIC_DAMAGE = TagKey.of(RegistryKeys.DAMAGE_TYPE, Mamy.id("magic_damage"));
        public static final TagKey<DamageType> FIRE_DAMAGE = TagKey.of(RegistryKeys.DAMAGE_TYPE, Mamy.id("fire_damage"));
        public static final TagKey<DamageType> PROJECTILE_DAMAGE = TagKey.of(RegistryKeys.DAMAGE_TYPE, Mamy.id("projectile_damage"));

        public static final TagKey<DamageType> DOUBLE_ATTACK = TagKey.of(RegistryKeys.DAMAGE_TYPE, Mamy.id("double_attack"));
    }
    public static class EntityTypes {
        public static final TagKey<EntityType<?>> CANNOT_HEADSHOT = TagKey.of(RegistryKeys.ENTITY_TYPE, Mamy.id("cannot_headshot"));
    }
}
