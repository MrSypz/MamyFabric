package com.sypztep.mamy.common.init;

import com.sypztep.mamy.Mamy;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.biome.Biome;

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
        public static final TagKey<EntityType<?>> BOSSES = TagKey.of(RegistryKeys.ENTITY_TYPE, Mamy.id("bosses"));
    }
    // ==========================================
    // ITEM TAGS FOR CLASS RESTRICTION SYSTEM
    // ==========================================
    public static class Items {

        // === WEAPON CATEGORIES (For organization/reference) ===
        public static final TagKey<Item> ONE_HAND_SWORDS = TagKey.of(RegistryKeys.ITEM, Mamy.id("one_hand_swords"));
        public static final TagKey<Item> TWO_HAND_SWORDS = TagKey.of(RegistryKeys.ITEM, Mamy.id("two_hand_swords"));
        public static final TagKey<Item> SPEARS = TagKey.of(RegistryKeys.ITEM, Mamy.id("spears"));
        public static final TagKey<Item> DAGGERS = TagKey.of(RegistryKeys.ITEM, Mamy.id("daggers"));
        public static final TagKey<Item> STAFFS = TagKey.of(RegistryKeys.ITEM, Mamy.id("staffs"));
        public static final TagKey<Item> MACES = TagKey.of(RegistryKeys.ITEM, Mamy.id("maces"));
        public static final TagKey<Item> KNUCKLES = TagKey.of(RegistryKeys.ITEM, Mamy.id("knuckles"));
        public static final TagKey<Item> BOWS = TagKey.of(RegistryKeys.ITEM, Mamy.id("bows"));
        public static final TagKey<Item> SHORT_BOWS = TagKey.of(RegistryKeys.ITEM, Mamy.id("short_bows"));
        public static final TagKey<Item> SHIELDS = TagKey.of(RegistryKeys.ITEM, Mamy.id("shields"));

        // === ARMOR CATEGORIES ===
        public static final TagKey<Item> LIGHT_ARMOR = TagKey.of(RegistryKeys.ITEM, Mamy.id("light_armor"));
        public static final TagKey<Item> MEDIUM_ARMOR = TagKey.of(RegistryKeys.ITEM, Mamy.id("medium_armor"));
        public static final TagKey<Item> HEAVY_ARMOR = TagKey.of(RegistryKeys.ITEM, Mamy.id("heavy_armor"));

        // === SPECIAL ITEMS ===
        public static final TagKey<Item> HOLY_ITEMS = TagKey.of(RegistryKeys.ITEM, Mamy.id("holy_items"));
        public static final TagKey<Item> MAGIC_ITEMS = TagKey.of(RegistryKeys.ITEM, Mamy.id("magic_items"));
    }

    // ==================== BIOME TAG KEYS ====================
    public static class BiomeTags {
        public static final TagKey<Biome> PEACEFUL_BIOMES = TagKey.of(RegistryKeys.BIOME, Mamy.id("peaceful"));
        public static final TagKey<Biome> MODERATE_BIOMES = TagKey.of(RegistryKeys.BIOME, Mamy.id("moderate"));
        public static final TagKey<Biome> HARSH_BIOMES = TagKey.of(RegistryKeys.BIOME, Mamy.id("harsh"));
        public static final TagKey<Biome> EXTREME_BIOMES = TagKey.of(RegistryKeys.BIOME, Mamy.id("extreme"));
        public static final TagKey<Biome> HELLISH_BIOMES = TagKey.of(RegistryKeys.BIOME, Mamy.id("hellish"));
    }
}