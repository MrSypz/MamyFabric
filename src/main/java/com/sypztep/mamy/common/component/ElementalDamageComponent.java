package com.sypztep.mamy.common.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.entity.attribute.EntityAttribute;

import java.util.Map;

public record ElementalDamageComponent(Map<RegistryEntry<EntityAttribute>, Float> elementalDamage, float totalDamage) {
    public static final Codec<ElementalDamageComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.unboundedMap(EntityAttribute.CODEC, Codec.FLOAT).fieldOf("elemental_damage").forGetter(ElementalDamageComponent::elementalDamage),
                    Codec.FLOAT.fieldOf("total_damage").forGetter(ElementalDamageComponent::totalDamage)
            ).apply(instance, ElementalDamageComponent::new)
    );
}