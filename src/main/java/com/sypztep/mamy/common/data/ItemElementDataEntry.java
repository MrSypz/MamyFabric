package com.sypztep.mamy.common.data;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.Item;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public record ItemElementDataEntry(
        Map<RegistryEntry<EntityAttribute>, Double> elementalRatios,    // Fire, Cold, Physical, etc.
        Map<RegistryEntry<EntityAttribute>, Double> combatRatios,       // Melee, Ranged, Magic
        double powerBudget,
        double combatWeight
) {

    public static final Map<Item, ItemElementDataEntry> ITEM_DATA_MAP = new ConcurrentHashMap<>();

    // Legacy constructor for backward compatibility
    @Deprecated()
    public ItemElementDataEntry(Map<RegistryEntry<EntityAttribute>, Double> damageRatios, double powerBudget) {
        this(damageRatios, Map.of(), powerBudget, 1.0);
    }

    public static ItemElementDataEntry getEntry(Item item) {
        return ITEM_DATA_MAP.get(item);
    }

    public static boolean hasEntry(Item item) {
        return ITEM_DATA_MAP.containsKey(item);
    }

    // Legacy method for backward compatibility
    public Map<RegistryEntry<EntityAttribute>, Double> damageRatios() {
        return elementalRatios;
    }

    @Override
    public String toString() {
        return String.format("ItemDataEntry{elemental=%s, combat=%s, powerBudget=%.2f, combatWeight=%.2f}",
                elementalRatios, combatRatios, powerBudget, combatWeight);
    }
}