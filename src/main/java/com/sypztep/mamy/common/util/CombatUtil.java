package com.sypztep.mamy.common.util;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.client.util.ParticleHandler;
import com.sypztep.mamy.common.init.ModCustomParticles;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class CombatUtil {
    private static final boolean DEBUG = false;

    private static void debugLog(String message, Object... args) {
        if (DEBUG) {
            Mamy.LOGGER.info("[CombatUtil]{} {}%n", message, args);
        }
    }

    @FunctionalInterface
    private interface DamageModifier {
        float getMultiplierBonus(LivingEntity attacker, LivingEntity target, boolean isCrit);
    }

    private enum CombatModifierType {
        CRITICAL((attacker, target, isCrit) -> {
            if (isCrit) {
                ParticleHandler.sendToAll(target, attacker, ModCustomParticles.CRITICAL);
                ParticleHandler.sendToAll(target, attacker, ParticleTypes.CRIT);
                LivingEntityUtil.playCriticalSound(target);
                return (float) attacker.getAttributeValue(ModEntityAttributes.CRIT_DAMAGE);
            }
            return 0.0f;
        }),

        BACK_ATTACK((attacker, target, isCrit) -> {
            Vec3d entityPos = target.getPos();
            Vec3d attackerPos = attacker.getPos();
            Vec3d damageVector = attackerPos.subtract(entityPos).normalize();

            float damageDirection = (float) Math.toDegrees(Math.atan2(-damageVector.x, damageVector.z));
            float angleDifference = Math.abs(MathHelper.subtractAngles(target.getHeadYaw(), damageDirection));
            if (angleDifference >= 75) {
                ParticleHandler.sendToAll(target, attacker, ModCustomParticles.BACKATTACK);
                return (float) attacker.getAttributeValue(ModEntityAttributes.BACK_ATTACK);
            }
            return 0.0f;
        });

        private final DamageModifier modifier;

        CombatModifierType(DamageModifier modifier) {
            this.modifier = modifier;
        }

        float getMultiplierBonus(LivingEntity attacker, LivingEntity target, boolean isCrit) {
            return modifier.getMultiplierBonus(attacker, target, isCrit);
        }
    }

    public static float damageModifier(LivingEntity target, float amount, DamageSource source, boolean isCrit) {
        if (!(source.getAttacker() instanceof LivingEntity attacker)) {
            return amount;
        }

        float totalMultiplier = 1.0f;

        for (CombatModifierType modifier : CombatModifierType.values()) {
            totalMultiplier += modifier.getMultiplierBonus(attacker, target, isCrit);
        }

        return amount * totalMultiplier;
    }
}