package com.sypztep.mamy.common.init;

import com.sypztep.mamy.Mamy;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.item.Item;
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
    // ==========================================
    // ITEM TAGS FOR CLASS EQUIPMENT SYSTEM
    // ==========================================
    public static class Items {

        // === WEAPON CATEGORIES ===
        public static final TagKey<Item> SWORDS = TagKey.of(RegistryKeys.ITEM, Mamy.id("swords"));
        public static final TagKey<Item> MACES = TagKey.of(RegistryKeys.ITEM, Mamy.id("maces"));
        public static final TagKey<Item> STAFFS = TagKey.of(RegistryKeys.ITEM, Mamy.id("staffs"));
        public static final TagKey<Item> BOWS = TagKey.of(RegistryKeys.ITEM, Mamy.id("bows"));
        public static final TagKey<Item> DAGGERS = TagKey.of(RegistryKeys.ITEM, Mamy.id("daggers"));
        public static final TagKey<Item> AXES = TagKey.of(RegistryKeys.ITEM, Mamy.id("axes"));
        public static final TagKey<Item> SPEARS = TagKey.of(RegistryKeys.ITEM, Mamy.id("spears"));

        // === ARMOR CATEGORIES ===
        public static final TagKey<Item> HEAVY_ARMOR = TagKey.of(RegistryKeys.ITEM, Mamy.id("heavy_armor"));
        public static final TagKey<Item> MEDIUM_ARMOR = TagKey.of(RegistryKeys.ITEM, Mamy.id("medium_armor"));
        public static final TagKey<Item> LIGHT_ARMOR = TagKey.of(RegistryKeys.ITEM, Mamy.id("light_armor"));

        // === EQUIPMENT CATEGORIES ===
        public static final TagKey<Item> SHIELDS = TagKey.of(RegistryKeys.ITEM, Mamy.id("shields"));
        public static final TagKey<Item> ACCESSORIES = TagKey.of(RegistryKeys.ITEM, Mamy.id("accessories"));

        // === SPECIAL CATEGORIES ===
        public static final TagKey<Item> HOLY_ITEMS = TagKey.of(RegistryKeys.ITEM, Mamy.id("holy_items"));
        public static final TagKey<Item> MAGIC_ITEMS = TagKey.of(RegistryKeys.ITEM, Mamy.id("magic_items"));

        // === CLASS EQUIPMENT SETS ===
        public static final TagKey<Item> NOVICE_EQUIPMENT = TagKey.of(RegistryKeys.ITEM, Mamy.id("novice_equipment"));
        public static final TagKey<Item> SWORDMAN_EQUIPMENT = TagKey.of(RegistryKeys.ITEM, Mamy.id("swordman_equipment"));
        public static final TagKey<Item> MAGE_EQUIPMENT = TagKey.of(RegistryKeys.ITEM, Mamy.id("mage_equipment"));
        public static final TagKey<Item> ARCHER_EQUIPMENT = TagKey.of(RegistryKeys.ITEM, Mamy.id("archer_equipment"));
        public static final TagKey<Item> ACOLYTE_EQUIPMENT = TagKey.of(RegistryKeys.ITEM, Mamy.id("acolyte_equipment"));
        public static final TagKey<Item> THIEF_EQUIPMENT = TagKey.of(RegistryKeys.ITEM, Mamy.id("thief_equipment"));

        // === TIER 2 CLASS EQUIPMENT ===
//        public static final TagKey<Item> KNIGHT_EQUIPMENT = TagKey.of(RegistryKeys.ITEM, Mamy.id("knight_equipment"));
//        public static final TagKey<Item> WIZARD_EQUIPMENT = TagKey.of(RegistryKeys.ITEM, Mamy.id("wizard_equipment"));
//        public static final TagKey<Item> PRIEST_EQUIPMENT = TagKey.of(RegistryKeys.ITEM, Mamy.id("priest_equipment"));
//        public static final TagKey<Item> ASSASSIN_EQUIPMENT = TagKey.of(RegistryKeys.ITEM, Mamy.id("assassin_equipment"));
//        public static final TagKey<Item> HUNTER_EQUIPMENT = TagKey.of(RegistryKeys.ITEM, Mamy.id("hunter_equipment"));

        // === WEAPON TIER TAGS ===
        public static final TagKey<Item> BASIC_WEAPONS = TagKey.of(RegistryKeys.ITEM, Mamy.id("basic_weapons"));
        public static final TagKey<Item> ADVANCED_WEAPONS = TagKey.of(RegistryKeys.ITEM, Mamy.id("advanced_weapons"));
        public static final TagKey<Item> LEGENDARY_WEAPONS = TagKey.of(RegistryKeys.ITEM, Mamy.id("legendary_weapons"));

        // === SPECIAL ===
        public static final TagKey<Item> ALL_CLASSES = TagKey.of(RegistryKeys.ITEM, Mamy.id("all_classes"));
    }
}
