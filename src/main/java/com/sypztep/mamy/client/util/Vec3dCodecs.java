package com.sypztep.mamy.client.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.Vec3d;
// due to 1.21.1 don't have build in yet so :)
public class Vec3dCodecs {
    public static final Codec<Vec3d> VEC3D_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("x").forGetter(Vec3d::getX),
            Codec.DOUBLE.fieldOf("y").forGetter(Vec3d::getY),
            Codec.DOUBLE.fieldOf("z").forGetter(Vec3d::getZ)
    ).apply(instance, Vec3d::new));

    public static final PacketCodec<RegistryByteBuf, Vec3d> VEC3D_PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.DOUBLE, Vec3d::getX,
            PacketCodecs.DOUBLE, Vec3d::getY,
            PacketCodecs.DOUBLE, Vec3d::getZ,
            Vec3d::new
    );

    public static final PacketCodec<RegistryByteBuf, Vec3d> VEC3D_PACKET_CODEC_COMPACT = PacketCodec.tuple(
            PacketCodecs.FLOAT, vec3d -> (float) vec3d.getX(),
            PacketCodecs.FLOAT, vec3d -> (float) vec3d.getY(),
            PacketCodecs.FLOAT, vec3d -> (float) vec3d.getZ(),
            Vec3d::new
    );
}
