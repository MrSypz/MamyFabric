package com.sypztep.mamy.common.data;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.Item;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public record ItemDataEntry(
        Map<RegistryEntry<EntityAttribute>, Double> damageRatios,
        double powerBudget
) {

    public static final Map<Item, ItemDataEntry> ITEM_DATA_MAP = new ConcurrentHashMap<>();

    public static void addEntry(Item item, ItemDataEntry entry) {
        ITEM_DATA_MAP.put(item, entry);
    }

    public static ItemDataEntry getEntry(Item item) {
        return ITEM_DATA_MAP.get(item);
    }

    public static boolean hasEntry(Item item) {
        return ITEM_DATA_MAP.containsKey(item);
    }

    public static void clearAll() {
        ITEM_DATA_MAP.clear();
    }

    @Override
    public String toString() {
        return String.format("ItemDataEntry{ratios=%s, powerBudget=%.2f}", damageRatios, powerBudget);
    }
}