package com.sypztep.mamy.common.component.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record WeightComponent(float weight) {
    public static final Codec<WeightComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("weight").forGetter(WeightComponent::weight)
            ).apply(instance, WeightComponent::new));
    public static final String RESOURCE_LOCATION = "itemweight";
}