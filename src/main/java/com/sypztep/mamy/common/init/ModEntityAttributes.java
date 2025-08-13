package com.sypztep.mamy.common.init;

import com.sypztep.mamy.Mamy;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.ArrayList;

public interface ModEntityAttributes {
    ArrayList<RegistryEntry<EntityAttribute>> ENTRIES = new ArrayList<>();

    RegistryEntry<EntityAttribute> HEALTH_REGEN = register("health_regen", 0, 0.0, 2048);

    RegistryEntry<EntityAttribute> RESOURCE = register("resource", 200, 0.0, 1000000);
    RegistryEntry<EntityAttribute> RESOURCE_REGEN = register("resource_regen", 10, 0.0, 10000000);
    RegistryEntry<EntityAttribute> RESOURCE_REGEN_RATE = register("resource_regen_rate", 30, 0.0, 10000000);

    RegistryEntry<EntityAttribute> ACCURACY = register("accuracy", 0, 0.0, 2048.0D);
    RegistryEntry<EntityAttribute> EVASION = register("evasion", 0, 0.0, 2048.0D);

    RegistryEntry<EntityAttribute> CRIT_DAMAGE = register("crit_damage",1, 0.0, 10.24D);
    RegistryEntry<EntityAttribute> CRIT_CHANCE = register("crit_chance",0.0, 0.0, 2.0D);
    RegistryEntry<EntityAttribute> BACK_ATTACK = register("back_attack", 0.2, 0.0, 10.24D);
    RegistryEntry<EntityAttribute> SPECIAL_ATTACK = register("special_attack", 0.0, 0.0, 10.24D);
    RegistryEntry<EntityAttribute> HEADSHOT_DAMAGE = register("headshot_damage", 0.0, 0.0, 1024D);


    RegistryEntry<EntityAttribute> MELEE_ATTACK_DAMAGE = register("melee_attack_damage", 0, 0.0, 4096);
    RegistryEntry<EntityAttribute> MAGIC_ATTACK_DAMAGE = register("magic_attack_damage", 0, 0.0, 4096);
    RegistryEntry<EntityAttribute> PROJECTILE_ATTACK_DAMAGE = register("projectile_attack_damage",0, 0.0, 4096);

    RegistryEntry<EntityAttribute> MAGIC_RESISTANCE = register("magic_resistance", 0, -10.0D, 0.75D);
    RegistryEntry<EntityAttribute> DAMAGE_REDUCTION = register("damage_reduction", 0.05, 0.0D, 0.8D);

    RegistryEntry<EntityAttribute> HEAL_EFFECTIVE = register("heal_effective", 0, -10.0D, 10.24D);
    RegistryEntry<EntityAttribute> DOUBLE_ATTACK_CHANCE = register("double_attack_chance", 0, 0, 1);

    private static RegistryEntry<EntityAttribute> register(String id, double fallback,double min, double max) {
        RegistryEntry<EntityAttribute> entry = Registry.registerReference(Registries.ATTRIBUTE, Mamy.id(id), new ClampedEntityAttribute("attribute.name." + id, fallback, min, max).setTracked(true));
        ENTRIES.add(entry);
        return entry;
    }
}
