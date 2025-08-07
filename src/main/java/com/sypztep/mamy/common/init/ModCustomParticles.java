package com.sypztep.mamy.common.init;

import com.sypztep.mamy.ModConfig;
import net.minecraft.text.Text;
import sypztep.tyrannus.client.util.TextParticleProvider;

import java.awt.*;

public interface ModCustomParticles {
    TextParticleProvider CRITICAL = TextParticleProvider.register(Text.translatable("mamy.text.critical"), new Color(ModConfig.critDamageColor), -0.055f, -0.045F, () -> ModConfig.damageCritIndicator);
    TextParticleProvider MISSING = TextParticleProvider.register(Text.translatable("mamy.text.missing"), new Color(1f, 1f, 1f), -0.045f, -0.085F, () -> ModConfig.missingIndicator);
    TextParticleProvider MISSING_MONSTER = TextParticleProvider.register(Text.translatable("mamy.text.missing"), new Color(255,  28, 28),-0.045F,-0.085F, () -> ModConfig.missingIndicator);
    TextParticleProvider BACKATTACK = TextParticleProvider.register(Text.translatable("mamy.text.back"), new Color(1f,1f,1f),-0.035f,0.3f, () -> ModConfig.damageCritIndicator);
}
