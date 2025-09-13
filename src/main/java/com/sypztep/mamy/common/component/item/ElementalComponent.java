package com.sypztep.mamy.common.component.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Map;

public record ElementalComponent(
        Map<String, Double> elementalRatios,
        Map<String, Double> combatRatios,
        double powerBudget,
        double combatWeight
) {
    public static final Codec<ElementalComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).fieldOf("elementalRatios").forGetter(ElementalComponent::elementalRatios),
            Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).optionalFieldOf("combatRatios", Map.of()).forGetter(ElementalComponent::combatRatios),
            Codec.DOUBLE.optionalFieldOf("powerBudget", 1.0).forGetter(ElementalComponent::powerBudget),
            Codec.DOUBLE.optionalFieldOf("combatWeight", 1.0).forGetter(ElementalComponent::combatWeight)
    ).apply(instance, ElementalComponent::new));
    public static final String RESOURCE_LOCATION = "elementator";
}