package com.sypztep.mamy.common.data;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public record ItemWeightEntry(float weight) {
    public static final Map<Item, ItemWeightEntry> ITEM_DATA_MAP = new ConcurrentHashMap<>();
    public static float getWeight(ItemStack item) {
        return ItemWeightEntry.ITEM_DATA_MAP.get(item.getItem()).weight();
    }
    public static boolean hasWeight(ItemStack item) {
        return ItemWeightEntry.ITEM_DATA_MAP.containsKey(item.getItem());
    }
}
