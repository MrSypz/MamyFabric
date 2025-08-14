package com.sypztep.mamy.common.init;

import com.sypztep.mamy.ModConfig;
import com.sypztep.mamy.client.util.TextParticleProvider;
import com.sypztep.mamy.client.util.TextParticleRegistry;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public interface ModCustomParticles {
    Map<TextParticleProvider, Integer> PARTICLES = new LinkedHashMap<>();

    TextParticleProvider CRITICAL = createParticle(Text.translatable("mamy.text.critical"), new Color(ModConfig.critDamageColor), -0.055f, -0.045F, () -> ModConfig.damageCritIndicator);
    TextParticleProvider MISSING = createParticle(Text.translatable("mamy.text.missing"), new Color(1f, 1f, 1f), -0.045f, -0.085F, () -> ModConfig.missingIndicator);
    TextParticleProvider MISSING_MONSTER = createParticle(Text.translatable("mamy.text.missing"), new Color(255, 28, 28), -0.045F, -0.085F, () -> ModConfig.missingIndicator);
    TextParticleProvider BACKATTACK = createParticle(Text.translatable("mamy.text.back"), new Color(1f, 1f, 1f), -0.035f, 0.3f, () -> ModConfig.damageCritIndicator);
    TextParticleProvider HEADSHOT = createParticle(Text.translatable("mamy.text.headshot"), new Color(ModConfig.critDamageColor), -0.045f, 0.8f, () -> ModConfig.damageCritIndicator);

    static void init() {
        System.out.println("ModCustomParticles initialized - registered " + PARTICLES.size() + " particles");
    }

    private static TextParticleProvider createParticle(Text text, Color color, float maxSize, float yPos, Supplier<Boolean> configSupplier) {
        TextParticleProvider provider = TextParticleProvider.create(text, color, maxSize, yPos, configSupplier);
        int id = TextParticleRegistry.register(provider);
        PARTICLES.put(provider, id);
        return provider;
    }
}