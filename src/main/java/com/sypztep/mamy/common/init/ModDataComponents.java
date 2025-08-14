package com.sypztep.mamy.common.init;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.component.ItemWeight;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface ModDataComponents {
    ComponentType<ItemWeight> ITEM_WEIGHT_COMPONENT_TYPE = new ComponentType.Builder<ItemWeight>().codec(ItemWeight.CODEC).build();

    static void init() {
        Registry.register(Registries.DATA_COMPONENT_TYPE, Mamy.id("item_weight"), ITEM_WEIGHT_COMPONENT_TYPE);
    }
}