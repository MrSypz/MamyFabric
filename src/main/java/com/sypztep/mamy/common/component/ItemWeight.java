package com.sypztep.mamy.common.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sypztep.mamy.common.util.NumberUtil;

public record ItemWeight(float value, NumberUtil.WeightUnit unit) {

    public static final Codec<ItemWeight> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("value").forGetter(ItemWeight::value),
            NumberUtil.WeightUnit.CODEC.fieldOf("unit").forGetter(ItemWeight::unit)
    ).apply(instance, ItemWeight::new));

    public float toGrams() {
        return unit.toGrams(value);
    }
}
