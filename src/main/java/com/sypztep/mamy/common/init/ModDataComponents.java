package com.sypztep.mamy.common.init;

import com.mojang.serialization.Codec;
import com.sypztep.mamy.Mamy;
import net.minecraft.component.ComponentType;
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

    public static void register() {
        Registry.register(
                Registries.DATA_COMPONENT_TYPE,
                Mamy.id("broken_flag"),
                BROKEN_FLAG
        );
    }
}