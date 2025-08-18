package com.sypztep.mamy.common.init;

import com.mojang.serialization.Codec;
import com.sypztep.mamy.Mamy;
import net.minecraft.component.ComponentType;
import net.minecraft.component.type.ToolComponent;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

/**
 * Data components for storing custom data on items
 */
public class ModDataComponents {

    /**
     * Boolean flag to mark items as "broken" - they cannot be used but won't disappear
     */
    public static final ComponentType<Boolean> BROKEN_FLAG = ComponentType.<Boolean>builder()
            .codec(Codec.BOOL)
            .build();

    /**
     * Store the original tool component so we can restore it when repaired
     */
    public static final ComponentType<ToolComponent> ORIGINAL_TOOL = ComponentType.<ToolComponent>builder()
            .codec(ToolComponent.CODEC)
            .build();

    public static final ComponentType<String> CRAFT_BY = ComponentType.<String>builder()
            .codec(Codec.STRING)
            .build();

    public static void register() {
        Registry.register(
                Registries.DATA_COMPONENT_TYPE,
                Mamy.id("broken_flag"),
                BROKEN_FLAG
        );

        Registry.register(
                Registries.DATA_COMPONENT_TYPE,
                Mamy.id("original_tool"),
                ORIGINAL_TOOL
        );
        Registry.register(
                Registries.DATA_COMPONENT_TYPE,
                Mamy.id("craft_by"),
                CRAFT_BY
        );
    }
}