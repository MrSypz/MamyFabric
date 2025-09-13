package com.sypztep.mamy.common.component.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

// Mob data class with codec
public record MobComponent(int expReward, int classReward, int baseLevel, MobStats stats) {

    public static final Codec<MobComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("expReward").forGetter(MobComponent::expReward),
                    Codec.INT.fieldOf("classReward").forGetter(MobComponent::classReward),
                    Codec.INT.fieldOf("baseLevel").forGetter(MobComponent::baseLevel),
                    MobStats.CODEC.fieldOf("stats").forGetter(MobComponent::stats)
            ).apply(instance, MobComponent::new));

    public static final String RESOURCE_LOCATION = "mobexp";

    // Helper record for stats
    public record MobStats(int strength, int agility, int vitality, int intelligence, int dexterity, int luck) {
        public static final Codec<MobStats> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.INT.fieldOf("strength").forGetter(MobStats::strength),
                        Codec.INT.fieldOf("agility").forGetter(MobStats::agility),
                        Codec.INT.fieldOf("vitality").forGetter(MobStats::vitality),
                        Codec.INT.fieldOf("intelligence").forGetter(MobStats::intelligence),
                        Codec.INT.fieldOf("dexterity").forGetter(MobStats::dexterity),
                        Codec.INT.fieldOf("luck").forGetter(MobStats::luck)
                ).apply(instance, MobStats::new));
    }
}