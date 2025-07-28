package com.sypztep.mamy.common.init;

import com.sypztep.mamy.ModConfig;
import com.sypztep.mamy.client.util.TextParticleProvider;
import net.minecraft.text.Text;

import java.awt.*;

public final class ModCustomParticles {
    public ModCustomParticles() {
    }

    public static final TextParticleProvider CRITICAL;
    public static final TextParticleProvider MISSING;
    public static final TextParticleProvider MISSING_MONSTER;
    public static final TextParticleProvider BACKATTACK;

    static {
        CRITICAL = TextParticleProvider.register(Text.translatable("mamy.text.critical"), new Color(ModConfig.critDamageColor), -0.055f, -0.045F, () -> ModConfig.damageCritIndicator);
        MISSING = TextParticleProvider.register(Text.translatable("mamy.text.missing"), new Color(1f, 1f, 1f), -0.045f, -0.085F, () -> ModConfig.missingIndicator);
        MISSING_MONSTER = TextParticleProvider.register(Text.translatable("mamy.text.missing"), new Color(255,  28, 28),-0.045F,-0.085F, () -> ModConfig.missingIndicator);
        BACKATTACK = TextParticleProvider.register(Text.translatable("mamy.text.back"), new Color(1f,1f,1f),-0.035f,0.3f, () -> ModConfig.damageCritIndicator);
    }
}
