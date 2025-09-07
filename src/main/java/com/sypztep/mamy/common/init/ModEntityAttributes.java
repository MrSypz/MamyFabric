package com.sypztep.mamy.common.init;

import com.sypztep.mamy.Mamy;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.ArrayList;

public interface ModEntityAttributes {
    ArrayList<RegistryEntry<EntityAttribute>> COMMON = new ArrayList<>();
    ArrayList<RegistryEntry<EntityAttribute>> PLAYER_EXCLUSIVE = new ArrayList<>();

    RegistryEntry<EntityAttribute> HEALTH_REGEN = register("health_regen", 0, 0.0, 2048);

    RegistryEntry<EntityAttribute> RESOURCE = register("resource", 0, 0.0, 10000000,true); // NOTE : fallback are not working sadly
    RegistryEntry<EntityAttribute> RESOURCE_REGEN = register("resource_regen", 1, 0.0, 10000000,true); // NOTE : fallback are not working sadly
    RegistryEntry<EntityAttribute> RESOURCE_REGEN_RATE = register("resource_regen_rate", 8, -1, 10000000,true);

    RegistryEntry<EntityAttribute> ACCURACY = register("accuracy", 0, 0.0, 2048.0D);
    RegistryEntry<EntityAttribute> EVASION = register("evasion", 0, 0.0, 2048.0D);

    RegistryEntry<EntityAttribute> CRIT_DAMAGE = register("crit_damage", 1, 0.0, 10.24D);
    RegistryEntry<EntityAttribute> CRIT_CHANCE = register("crit_chance", 0.0, 0.0, 2.0D);
    RegistryEntry<EntityAttribute> BACK_ATTACK = register("back_attack", 0.2, 0.0, 10.24D);
    RegistryEntry<EntityAttribute> SPECIAL_ATTACK = register("special_attack", 0.0, 0.0, 10.24D);
    RegistryEntry<EntityAttribute> ARROW_SPEED = register("arrow_speed",0,-2,15, true);

    // Dual attack damage attributes (flat and percentage)
    RegistryEntry<EntityAttribute> MELEE_ATTACK_DAMAGE_FLAT = register("melee_attack_damage_flat", 0, 0.0, 4096);
    RegistryEntry<EntityAttribute> MELEE_ATTACK_DAMAGE_MULT = register("melee_attack_damage_mult", 0, 0.0, 10.24D);

    RegistryEntry<EntityAttribute> MAGIC_ATTACK_DAMAGE_FLAT = register("magic_attack_damage_flat", 0, 0.0, 4096);
    RegistryEntry<EntityAttribute> MAGIC_ATTACK_DAMAGE_MULT = register("magic_attack_damage_mult", 0, 0.0, 10.24D);

    RegistryEntry<EntityAttribute> PROJECTILE_ATTACK_DAMAGE_FLAT = register("projectile_attack_damage_flat", 0, 0.0, 4096);
    RegistryEntry<EntityAttribute> PROJECTILE_ATTACK_DAMAGE_MULT = register("projectile_attack_damage_mult", 0, 0.0, 10.24D);

    // Flat damage reduction
    RegistryEntry<EntityAttribute> FLAT_PROJECTILE_REDUCTION = register("flat_projectile_reduction", 0, 0.0D, 20.0D);
    RegistryEntry<EntityAttribute> FLAT_MELEE_REDUCTION = register("flat_melee_reduction", 0, 0.0D, 20.0D);
    RegistryEntry<EntityAttribute> FLAT_MAGIC_REDUCTION = register("flat_magic_reduction", 0, 0.0D, 20.0D);

    // ADD MISSING FLAT ELEMENTAL REDUCTIONS
    RegistryEntry<EntityAttribute> FLAT_FIRE_REDUCTION = register("flat_fire_reduction", 0, 0.0D, 20.0D);
    RegistryEntry<EntityAttribute> FLAT_ELECTRIC_REDUCTION = register("flat_electric_reduction", 0, 0.0D, 20.0D);
    RegistryEntry<EntityAttribute> FLAT_WATER_REDUCTION = register("flat_water_reduction", 0, 0.0D, 20.0D);
    RegistryEntry<EntityAttribute> FLAT_WIND_REDUCTION = register("flat_wind_reduction", 0, 0.0D, 20.0D);
    RegistryEntry<EntityAttribute> FLAT_HOLY_REDUCTION = register("flat_holy_reduction", 0, 0.0D, 20.0D);
    RegistryEntry<EntityAttribute> FLAT_COLD_REDUCTION = register("flat_cold_reduction", 0, 0.0D, 20.0D);

    RegistryEntry<EntityAttribute> PROJECTILE_RESISTANCE = register("projectile_resistance", 0, -10.0D, 0.75D);
    RegistryEntry<EntityAttribute> MELEE_RESISTANCE = register("melee_resistance", 0, -10.0D, 0.75D);
    RegistryEntry<EntityAttribute> MAGIC_RESISTANCE = register("magic_resistance", 0, -10.0D, 0.75D);

    // Elemental damage attributes (flat and percentage)
    RegistryEntry<EntityAttribute> DAMAGE_REDUCTION = register("damage_reduction", 0.05, 0.0D, 0.8D);

    // ========== ELEMENTAL DAMAGE SYSTEM ATTRIBUTES ==========

    // Base elemental damage (flat bonuses)
    RegistryEntry<EntityAttribute> FIRE_ATTACK_DAMAGE_FLAT = register("fire_attack_damage_flat", 0, 0.0, 4096);      // ELEMENTAL
    RegistryEntry<EntityAttribute> COLD_ATTACK_DAMAGE_FLAT = register("cold_attack_damage_flat", 0, 0.0, 4096);      // ELEMENTAL
    RegistryEntry<EntityAttribute> ELECTRIC_ATTACK_DAMAGE_FLAT = register("electric_attack_damage_flat", 0, 0.0, 4096); // ELEMENTAL
    RegistryEntry<EntityAttribute> WATER_ATTACK_DAMAGE_FLAT = register("water_attack_damage_flat", 0, 0.0, 4096);    // ELEMENTAL
    RegistryEntry<EntityAttribute> WIND_ATTACK_DAMAGE_FLAT = register("wind_attack_damage_flat", 0, 0.0, 4096);      // ELEMENTAL
    RegistryEntry<EntityAttribute> HOLY_ATTACK_DAMAGE_FLAT = register("holy_attack_damage_flat", 0, 0.0, 4096);      // ELEMENTAL

    // Elemental damage multipliers (affinity)
    RegistryEntry<EntityAttribute> FIRE_ATTACK_DAMAGE_MULT = register("fire_attack_damage_mult", 0, 0.0, 10.24D);    // ELEMENTAL
    RegistryEntry<EntityAttribute> COLD_ATTACK_DAMAGE_MULT = register("cold_attack_damage_mult", 0, 0.0, 10.24D);                  // ELEMENTAL
    RegistryEntry<EntityAttribute> ELECTRIC_ATTACK_DAMAGE_MULT = register("electric_attack_damage_mult", 0, 0.0, 10.24D); // ELEMENTAL
    RegistryEntry<EntityAttribute> WATER_ATTACK_DAMAGE_MULT = register("water_attack_damage_mult", 0, 0.0, 10.24D);  // ELEMENTAL
    RegistryEntry<EntityAttribute> WIND_ATTACK_DAMAGE_MULT = register("wind_attack_damage_mult", 0, 0.0, 10.24D);    // ELEMENTAL
    RegistryEntry<EntityAttribute> HOLY_ATTACK_DAMAGE_MULT = register("holy_attack_damage_mult", 0, 0.0, 10.24D);    // ELEMENTAL

    // Elemental resistances (damage reduction)
    RegistryEntry<EntityAttribute> FIRE_RESISTANCE = register("fire_resistance", 0, -10.0D, 0.75D);                 // ELEMENTAL
    RegistryEntry<EntityAttribute> ELECTRIC_RESISTANCE = register("electric_resistance", 0, -10.0D, 0.75D);         // ELEMENTAL
    RegistryEntry<EntityAttribute> WATER_RESISTANCE = register("water_resistance", 0, -10.0D, 0.75D);               // ELEMENTAL
    RegistryEntry<EntityAttribute> WIND_RESISTANCE = register("wind_resistance", 0, -10.0D, 0.75D);                 // ELEMENTAL
    RegistryEntry<EntityAttribute> HOLY_RESISTANCE = register("holy_resistance", 0, -10.0D, 0.75D);                 // ELEMENTAL
    RegistryEntry<EntityAttribute> COLD_RESISTANCE = register("cold_resistance", 0, -10.0D, 0.75D);                 // ELEMENTAL

    RegistryEntry<EntityAttribute> MAX_WEIGHT = register("max_weight", 400, 0D, 1000000,true);

    RegistryEntry<EntityAttribute> HEAL_EFFECTIVE = register("heal_effective", 0, -10.0D, 10.24D);
    RegistryEntry<EntityAttribute> DOUBLE_ATTACK_CHANCE = register("double_attack_chance", 0, 0, 1,true);

    // ========== CASTING SYSTEM ATTRIBUTES ==========

    // VCT (Variable Cast Time) reductions
    RegistryEntry<EntityAttribute> VCT_REDUCTION_FLAT = register("vct_reduction_flat", 0, 0.0, 1000.0,true);
    RegistryEntry<EntityAttribute> VCT_REDUCTION_PERCENT = register("vct_reduction_percent", 0, 0.0, 100.0,true);

    // FCT (Fixed Cast Time) reductions
    RegistryEntry<EntityAttribute> FCT_REDUCTION_FLAT = register("fct_reduction_flat", 0, 0.0, 1000.0,true);
    RegistryEntry<EntityAttribute> FCT_REDUCTION_PERCENT = register("fct_reduction_percent", 0, 0.0, 100.0,true);

    // Skill-specific VCT reduction (from passive skills)
    RegistryEntry<EntityAttribute> SKILL_VCT_REDUCTION = register("skill_vct_reduction", 0, 0.0, 100.0,true);

    private static RegistryEntry<EntityAttribute> register(String id, double fallback, double min, double max, boolean playerExclusive) {
        RegistryEntry<EntityAttribute> entry = Registry.registerReference(Registries.ATTRIBUTE, Mamy.id(id), new ClampedEntityAttribute("attribute.name." + id, fallback, min, max).setTracked(true));
        if (playerExclusive) PLAYER_EXCLUSIVE.add(entry); else COMMON.add(entry);
        return entry;
    }
    private static RegistryEntry<EntityAttribute> register(String id, double fallback, double min, double max) {
        return register(id,fallback,min,max, false);
    }
}