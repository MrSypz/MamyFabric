package com.sypztep.mamy.common.init;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.component.ElementalDamageComponent;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface ModDataComponents {
    ComponentType<ElementalDamageComponent> DAMAGE_PROPERTY = new ComponentType.Builder<ElementalDamageComponent>().codec(ElementalDamageComponent.CODEC).build();

    static void init() {
        Registry.register(Registries.DATA_COMPONENT_TYPE, Mamy.id("damage_property"), DAMAGE_PROPERTY);
    }
}