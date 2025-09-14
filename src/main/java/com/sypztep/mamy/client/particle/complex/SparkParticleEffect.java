package com.sypztep.mamy.client.particle.complex;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sypztep.mamy.client.util.Vec3dCodecs;
import com.sypztep.mamy.common.init.ModParticles;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.math.Vec3d;

public record SparkParticleEffect(Vec3d destination) implements ParticleEffect {

    public static final MapCodec<SparkParticleEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Vec3dCodecs.VEC3D_CODEC.fieldOf("destination").forGetter(SparkParticleEffect::destination)
    ).apply(instance, SparkParticleEffect::new));

    public static final PacketCodec<RegistryByteBuf, SparkParticleEffect> PACKET_CODEC = PacketCodec.tuple(
            Vec3dCodecs.VEC3D_PACKET_CODEC, SparkParticleEffect::destination,
            SparkParticleEffect::new
    );

    @Override
    public ParticleType<?> getType() {
        return ModParticles.SPARK;
    }
}