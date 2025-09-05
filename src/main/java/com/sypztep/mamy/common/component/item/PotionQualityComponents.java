package com.sypztep.mamy.common.component.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public record PotionQualityComponents(
        int qualityLevel, // 0-9 (10 steps)
        float effectiveness // 0-500% (0.0f - 5.0f)
) {
    public static final Codec<PotionQualityComponents> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.intRange(0, 9).fieldOf("quality_level").forGetter(PotionQualityComponents::qualityLevel),
                    Codec.floatRange(0.0f, 5.0f).fieldOf("effectiveness").forGetter(PotionQualityComponents::effectiveness)
            ).apply(instance, PotionQualityComponents::new)
    );

    public static final PacketCodec<RegistryByteBuf, PotionQualityComponents> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, PotionQualityComponents::qualityLevel,
            PacketCodecs.FLOAT, PotionQualityComponents::effectiveness,
            PotionQualityComponents::new
    );

    public static PotionQualityComponents random(World world) {
        int level = world.getRandom().nextInt(10);

        float baseEffectiveness = 0.5f + (level * 0.5f);
        float variance = 0.2f;
        float effectiveness = baseEffectiveness + (world.getRandom().nextFloat() - 0.5f) * variance * 2;
        effectiveness = Math.max(0.0f, Math.min(5.0f, effectiveness));

        return new PotionQualityComponents(level, effectiveness);
    }
    public String getQualityName() {
        return switch (qualityLevel) {
            case 0 -> "Poor";
            case 1 -> "Common";
            case 2 -> "Uncommon";
            case 3 -> "Rare";
            case 4 -> "Epic";
            case 5 -> "Legendary";
            case 6 -> "Mythic";
            case 7 -> "Divine";
            case 8 -> "Celestial";
            case 9 -> "Godly";
            default -> "Unknown";
        };
    }

    public int getQualityColor() {
        return switch (qualityLevel) {
            case 0 -> 0x8B0000; // Dark red
            case 1 -> 0xFF0000; // Red
            case 2 -> 0x808080; // Gray
            case 3 -> 0x87CEEB; // Light Blue
            case 4 -> 0x4169E1; // Royal Blue
            case 5 -> 0x8888FF; // Light blue-purple
            case 6 -> 0x4444FF; // Blue
            case 7 -> 0x0000FF; // Pure blue
            case 8 -> 0x50C878; // Dark Turquoise (green cyan emerald)
            case 9 -> 0xFFD700; // Gold
            default -> 0xFFFFFF; // White
        };
    }

    public Text getDisplayText() {
        return Text.literal(getQualityName()).withColor(getQualityColor());
    }

    public float getActualResourceAmount(float baseAmount) {
        return baseAmount * effectiveness;
    }
}
