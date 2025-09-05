package com.sypztep.mamy.common.init;

import com.mojang.serialization.Codec;
import com.sypztep.mamy.Mamy;
import net.minecraft.component.ComponentType;
import net.minecraft.component.type.ToolComponent;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface ModDataComponents {
    ComponentType<Boolean> BROKEN_FLAG = ComponentType.<Boolean>builder().codec(Codec.BOOL).build();
    ComponentType<ToolComponent> ORIGINAL_TOOL = ComponentType.<ToolComponent>builder().codec(ToolComponent.CODEC).build();
    ComponentType<String> CRAFT_BY = ComponentType.<String>builder().codec(Codec.STRING).build();

    static void init() {
        Registry.register(Registries.DATA_COMPONENT_TYPE, Mamy.id("broken_flag"), BROKEN_FLAG);
        Registry.register(Registries.DATA_COMPONENT_TYPE, Mamy.id("original_tool"), ORIGINAL_TOOL);
        Registry.register(Registries.DATA_COMPONENT_TYPE, Mamy.id("craft_by"), CRAFT_BY);
    }
}