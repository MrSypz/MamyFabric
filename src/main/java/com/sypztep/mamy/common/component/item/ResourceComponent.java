package com.sypztep.mamy.common.component.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.dynamic.Codecs;

import java.util.Optional;

public record ResourceComponent(
        float resourceAmount,
        int eatSeconds,
        Optional<ItemStack> usingConvertsTo
) {

    public static final Codec<ResourceComponent> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codecs.POSITIVE_FLOAT.fieldOf("resource_amount").forGetter(ResourceComponent::resourceAmount),
                    Codecs.POSITIVE_INT.optionalFieldOf("eat_seconds",16).forGetter(ResourceComponent::eatSeconds),
                    ItemStack.UNCOUNTED_CODEC.optionalFieldOf("using_converts_to").forGetter(ResourceComponent::usingConvertsTo)
            ).apply(instance, ResourceComponent::new)
    );

    public static final PacketCodec<RegistryByteBuf, ResourceComponent> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.FLOAT, ResourceComponent::resourceAmount,
            PacketCodecs.VAR_INT, ResourceComponent::eatSeconds,
            ItemStack.PACKET_CODEC.collect(PacketCodecs::optional), ResourceComponent::usingConvertsTo,
            ResourceComponent::new
    );

    public static class Builder {
        private float resourceAmount;
        private int eatTicks;
        private Optional<ItemStack> usingConvertsTo = Optional.empty();

        public ResourceComponent.Builder resourceAmount(float resourceAmount) {
            this.resourceAmount = resourceAmount;
            return this;
        }

        public ResourceComponent.Builder eatTicks(int eatSeconds) {
            this.eatTicks = eatSeconds;
            return this;
        }

        public ResourceComponent.Builder fastDrink() {
            this.eatTicks = 16;
            return this;
        }

        public ResourceComponent.Builder normalDrink() {
            this.eatTicks = 32;
            return this;
        }

        public ResourceComponent.Builder slowDrink() {
            this.eatTicks = 64;
            return this;
        }

        public ResourceComponent.Builder usingConvertsTo(ItemConvertible item) {
            this.usingConvertsTo = Optional.of(new ItemStack(item));
            return this;
        }

        public ResourceComponent build() {
            return new ResourceComponent(this.resourceAmount, this.eatTicks, this.usingConvertsTo);
        }
    }
}