package com.sypztep.mamy.common.util;

import com.sypztep.mamy.common.init.ModEntityAttributes;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Centralized registry for all elemental attributes with CLEAR naming
 */
public class ElementalAttributeRegistry {

    public record ElementInfo(RegistryEntry<EntityAttribute> flatAttribute,
                              RegistryEntry<EntityAttribute> multAttribute,
                              RegistryEntry<EntityAttribute> resistAttribute,
                              String weaponJsonName,
                              String armorJsonName) {
    }

    // SINGLE PLACE TO DEFINE ALL ELEMENTS WITH CLEAR NAMES!
    private static final Map<String, ElementInfo> ELEMENTS = new HashMap<>();

    static {
        // Physical/Melee
        register("physical",
                ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT,
                ModEntityAttributes.MELEE_ATTACK_DAMAGE_MULT,
                ModEntityAttributes.MELEE_RESISTANCE,
                "physical", "physical_resistance");

        // Fire/Heat
        register("fire",
                ModEntityAttributes.FIRE_ATTACK_DAMAGE_FLAT,
                ModEntityAttributes.FIRE_ATTACK_DAMAGE_MULT,
                ModEntityAttributes.FIRE_RESISTANCE,
                "fire", "fire_resistance");

        // Cold/Ice
        register("cold",
                ModEntityAttributes.COLD_DAMAGE_FLAT,
                ModEntityAttributes.COLD_DAMAGE_MULT,
                ModEntityAttributes.COLD_RESISTANCE,
                "cold", "cold_resistance");

        // Electric/Lightning
        register("electric",
                ModEntityAttributes.ELECTRIC_ATTACK_DAMAGE_FLAT,
                ModEntityAttributes.ELECTRIC_ATTACK_DAMAGE_MULT,
                ModEntityAttributes.ELECTRIC_RESISTANCE,
                "electric", "electric_resistance");

        // Water
        register("water",
                ModEntityAttributes.WATER_ATTACK_DAMAGE_FLAT,
                ModEntityAttributes.WATER_ATTACK_DAMAGE_MULT,
                ModEntityAttributes.WATER_RESISTANCE,
                "water", "water_resistance");

        // Wind/Air
        register("wind",
                ModEntityAttributes.WIND_ATTACK_DAMAGE_FLAT,
                ModEntityAttributes.WIND_ATTACK_DAMAGE_MULT,
                ModEntityAttributes.WIND_RESISTANCE,
                "wind", "wind_resistance");

        // Holy/Light
        register("holy",
                ModEntityAttributes.HOLY_ATTACK_DAMAGE_FLAT,
                ModEntityAttributes.HOLY_ATTACK_DAMAGE_MULT,
                ModEntityAttributes.HOLY_RESISTANCE,
                "holy", "holy_resistance");
    }

    private static void register(String primaryName,
                                 RegistryEntry<EntityAttribute> flatAttr,
                                 RegistryEntry<EntityAttribute> multAttr,
                                 RegistryEntry<EntityAttribute> resistAttr,
                                 String weaponJsonName,
                                 String armorJsonName) {
        ElementInfo info = new ElementInfo(flatAttr, multAttr, resistAttr, weaponJsonName, armorJsonName);
        ELEMENTS.put(primaryName, info);
    }

    // === PUBLIC API ===

    public static ElementInfo getElementInfo(String name) {
        return ELEMENTS.get(name.toLowerCase());
    }

    public static Set<String> getAllElementNames() {
        return ELEMENTS.keySet();
    }

    /**
     * For WEAPONS - maps JSON names to DAMAGE attributes
     */
    public static Map<String, RegistryEntry<EntityAttribute>> getWeaponAttributeMap() {
        Map<String, RegistryEntry<EntityAttribute>> map = new HashMap<>();
        ELEMENTS.forEach((name, info) -> {
            map.put(info.weaponJsonName, info.flatAttribute);
        });
        return map;
    }

    /**
     * For ARMOR - maps JSON names to RESISTANCE attributes
     */
    public static Map<String, RegistryEntry<EntityAttribute>> getArmorAttributeMap() {
        Map<String, RegistryEntry<EntityAttribute>> map = new HashMap<>();
        ELEMENTS.forEach((name, info) -> {
            map.put(info.armorJsonName, info.resistAttribute);
        });
        return map;
    }

    /**
     * Get element name from resistance attribute (for armor processing)
     */
    public static String getElementNameFromResistanceAttribute(RegistryEntry<EntityAttribute> attribute) {
        for (Map.Entry<String, ElementInfo> entry : ELEMENTS.entrySet()) {
            if (entry.getValue().resistAttribute.equals(attribute)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Get element name from damage attribute (for weapon processing)
     */
    public static String getElementNameFromDamageAttribute(RegistryEntry<EntityAttribute> attribute) {
        for (Map.Entry<String, ElementInfo> entry : ELEMENTS.entrySet()) {
            if (entry.getValue().flatAttribute.equals(attribute)) {
                return entry.getKey();
            }
        }
        return null;
    }
}