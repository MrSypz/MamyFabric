package com.sypztep.mamy.common.util;

import com.sypztep.mamy.common.init.ModEntityAttributes;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Centralized registry for all elemental attributes
 * Add new elements here and they automatically work everywhere!
 */
public class ElementalAttributeRegistry {

    /**
     * @param jsonNames Alternative names in JSON
     */
    public record ElementInfo(RegistryEntry<EntityAttribute> flatAttribute,
                              RegistryEntry<EntityAttribute> multAttribute,
                              RegistryEntry<EntityAttribute> resistAttribute, String... jsonNames) {
    }

    // SINGLE PLACE TO DEFINE ALL ELEMENTS!
    private static final Map<String, ElementInfo> ELEMENTS = new HashMap<>();

    static {
        // Physical
        register("physical",
                ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT,
                ModEntityAttributes.MELEE_ATTACK_DAMAGE_MULT,
                ModEntityAttributes.MELEE_RESISTANCE,
                "melee", "normal");

        // Fire/Heat
        register("fire",
                ModEntityAttributes.FIRE_ATTACK_DAMAGE_FLAT,
                ModEntityAttributes.FIRE_ATTACK_DAMAGE_MULT,
                ModEntityAttributes.FIRE_RESISTANCE,
                "heat", "flame");

        // Cold/Ice
        register("cold",
                ModEntityAttributes.COLD_DAMAGE_FLAT,
                ModEntityAttributes.COLD_DAMAGE_MULT,
                ModEntityAttributes.COLD_RESISTANCE,
                "ice", "frost");

        // Electric/Lightning
        register("electric",
                ModEntityAttributes.ELECTRIC_ATTACK_DAMAGE_FLAT,
                ModEntityAttributes.ELECTRIC_ATTACK_DAMAGE_MULT,
                ModEntityAttributes.ELECTRIC_RESISTANCE,
                "lightning", "shock");

        // Water
        register("water",
                ModEntityAttributes.WATER_ATTACK_DAMAGE_FLAT,
                ModEntityAttributes.WATER_ATTACK_DAMAGE_MULT,
                ModEntityAttributes.WATER_RESISTANCE,
                "aqua", "hydro");

        // Wind/Air
        register("wind",
                ModEntityAttributes.WIND_ATTACK_DAMAGE_FLAT,
                ModEntityAttributes.WIND_ATTACK_DAMAGE_MULT,
                ModEntityAttributes.WIND_RESISTANCE,
                "air", "storm");

        // Holy/Light
        register("holy",
                ModEntityAttributes.HOLY_ATTACK_DAMAGE_FLAT,
                ModEntityAttributes.HOLY_ATTACK_DAMAGE_MULT,
                ModEntityAttributes.HOLY_RESISTANCE,
                "light", "divine");
    }

    private static void register(String primaryName,
                                 RegistryEntry<EntityAttribute> flatAttr,
                                 RegistryEntry<EntityAttribute> multAttr,
                                 RegistryEntry<EntityAttribute> resistAttr,
                                 String... aliases) {
        ElementInfo info = new ElementInfo(flatAttr, multAttr, resistAttr, aliases);

        // Register primary name
        ELEMENTS.put(primaryName, info);

        // Register all aliases
        for (String alias : aliases) {
            ELEMENTS.put(alias, info);
        }
    }

    // === PUBLIC API ===

    public static ElementInfo getElementInfo(String name) {
        return ELEMENTS.get(name.toLowerCase());
    }

    public static Set<String> getAllElementNames() {
        return ELEMENTS.keySet();
    }

    public static Map<RegistryEntry<EntityAttribute>, String> getAllFlatAttributes() {
        Map<RegistryEntry<EntityAttribute>, String> result = new HashMap<>();
        ELEMENTS.forEach((name, info) -> result.put(info.flatAttribute, name));
        return result;
    }

    // Auto-generate mapping for reload listener
    public static Map<String, RegistryEntry<EntityAttribute>> getJsonToAttributeMap() {
        Map<String, RegistryEntry<EntityAttribute>> map = new HashMap<>();
        ELEMENTS.forEach((name, info) -> map.put(name, info.flatAttribute));
        return map;
    }
}