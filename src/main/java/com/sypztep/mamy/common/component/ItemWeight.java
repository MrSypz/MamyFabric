package com.sypztep.mamy.common.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ItemWeight(float weight) {
    public static final Codec<ItemWeight> CODEC = RecordCodecBuilder.create(instance -> instance.group(Codec.FLOAT.optionalFieldOf("weight", 0.0f).forGetter(ItemWeight::weight)).apply(instance, ItemWeight::new));
}