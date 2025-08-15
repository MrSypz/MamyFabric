package com.sypztep.mamy.common.data;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public record ItemWeightEntry(float weight) {
    public static final Map<Item, ItemWeightEntry> ITEM_DATA_MAP = new ConcurrentHashMap<>();

    private static final float DEFAULT_WEIGHT = 0.1f;

    public static float getWeight(ItemStack item) {
        ItemWeightEntry entry = ITEM_DATA_MAP.get(item.getItem());
        return entry != null ? entry.weight() : DEFAULT_WEIGHT;
    }

    public static float getTotalWeight(ItemStack item) {
        return getWeight(item) * item.getCount();
    }

    public static boolean hasCustomWeight(ItemStack item) {
        return ITEM_DATA_MAP.containsKey(item.getItem());
    }
}