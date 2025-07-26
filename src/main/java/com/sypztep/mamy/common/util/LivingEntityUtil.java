package com.sypztep.mamy.common.util;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

public final class LivingEntityUtil {

    public static boolean isHitable(LivingEntity target, DamageSource source) {
        return !target.isInvulnerable() && !target.isInvulnerableTo(source) && target.hurtTime == 0;
    }

    public static boolean isPlayer(LivingEntity entity) {
        return entity instanceof PlayerEntity;
    }

    public static boolean hitCheck(LivingEntity attacker, LivingEntity defender) {
        int aAccuracy = (int) attacker.getAttributeValue(ModEntityAttributes.ACCURACY);
        int dEvasion = (int) defender.getAttributeValue(ModEntityAttributes.EVASION);
        Mamy.LOGGER.info("Attacker Accuracy: {}",  aAccuracy);
        int hitRate = aAccuracy - dEvasion;
        Mamy.LOGGER.info("hitRate: {}", hitRate);
        float hitChane = hitRate * 0.01f;
        Mamy.LOGGER.info("hitChane: {}%", hitChane * 100);
        return roll(attacker) < hitChane;
    }

    public static void playCriticalSound(Entity target) {
        target.getWorld().playSound(null, target.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.HOSTILE, 1, 1);
    }

    public static boolean isCrit(LivingEntity attacker) {
        return roll(attacker) < attacker.getAttributeValue(ModEntityAttributes.CRIT_CHANCE);
    }

    public static float roll(LivingEntity attacker) {
        return attacker.getRandom().nextFloat();
    }
}
