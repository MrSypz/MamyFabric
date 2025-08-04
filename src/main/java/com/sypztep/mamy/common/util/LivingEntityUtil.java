package com.sypztep.mamy.common.util;

import com.sypztep.mamy.common.init.ModEntityAttributes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;

public final class LivingEntityUtil {
    private static final float BASE_HIT_RATE = 0.67f, POINT_EFICENT = 0.0025f;
    public static boolean isHitable(LivingEntity target, DamageSource source) {
        return !target.isInvulnerable() && !target.isInvulnerableTo(source) && target.hurtTime == 0;
    }

    public static boolean isPlayer(LivingEntity entity) {
        return entity instanceof PlayerEntity;
    }

    public static boolean hitCheck(LivingEntity attacker, LivingEntity defender) {
        int aAccuracy = (int) attacker.getAttributeValue(ModEntityAttributes.ACCURACY);
        int dEvasion = (int) defender.getAttributeValue(ModEntityAttributes.EVASION);

        float hitRate = BASE_HIT_RATE + ((aAccuracy - dEvasion) * POINT_EFICENT);

        hitRate = MathHelper.clamp(hitRate, 0,1);

        return roll(attacker) < hitRate;
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
