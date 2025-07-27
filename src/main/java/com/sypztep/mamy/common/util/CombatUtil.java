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
        if (DEBUG) Mamy.LOGGER.info("[CombatUtil] {}", String.format(message, args));
    }

    @FunctionalInterface
    private interface DamageModifier {
        float getMultiplier(LivingEntity attacker, LivingEntity target, boolean isCrit);
    }
    private static float specialAttack(LivingEntity attacker) {
        return (float) attacker.getAttributeValue(ModEntityAttributes.SPECIAL_ATTACK);
    }

    private enum CombatModifierType {
        CRITICAL((attacker, target, isCrit) -> {
            if (isCrit) {
                ParticleHandler.sendToAll(target, attacker, ModCustomParticles.CRITICAL);
                ParticleHandler.sendToAll(target, attacker, ParticleTypes.CRIT);
                LivingEntityUtil.playCriticalSound(target);

                float critDamageBonus = (float) attacker.getAttributeValue(ModEntityAttributes.CRIT_DAMAGE);
                float multiplier = 1.0f + critDamageBonus + specialAttack(attacker);

                debugLog("Critical Hit: %.1f%% bonus → %.2fx multiplier", critDamageBonus * 100, multiplier);
                return multiplier;
            }
            return 1.0f; // No modification
        }),

        BACK_ATTACK((attacker, target, isCrit) -> {
            Vec3d entityPos = target.getPos();
            Vec3d attackerPos = attacker.getPos();
            Vec3d damageVector = attackerPos.subtract(entityPos).normalize();

            float damageDirection = (float) Math.toDegrees(Math.atan2(-damageVector.x, damageVector.z));
            float angleDifference = Math.abs(MathHelper.subtractAngles(target.getHeadYaw(), damageDirection));

            if (angleDifference >= 75) {
                ParticleHandler.sendToAll(target, attacker, ModCustomParticles.BACKATTACK);

                float backAttackBonus = (float) attacker.getAttributeValue(ModEntityAttributes.BACK_ATTACK);
                float multiplier = 1.0f + backAttackBonus + specialAttack(attacker);

                debugLog("Back Attack: %.1f%% bonus → %.2fx multiplier", backAttackBonus * 100, multiplier);
                return multiplier;
            }
            return 1.0f; // No modification
        });

        private final DamageModifier modifier;

        CombatModifierType(DamageModifier modifier) {
            this.modifier = modifier;
        }

        float getMultiplier(LivingEntity attacker, LivingEntity target, boolean isCrit) {
            return modifier.getMultiplier(attacker, target, isCrit);
        }
    }

    public static float damageModifier(LivingEntity target, float amount, DamageSource source, boolean isCrit) {
        if (!(source.getAttacker() instanceof LivingEntity attacker)) return amount;


        float totalMultiplier = 1.0f;

        for (CombatModifierType modifierType : CombatModifierType.values()) {
            float modifier = modifierType.getMultiplier(attacker, target, isCrit);
            totalMultiplier *= modifier;
        }

        float finalDamage = amount * totalMultiplier;

        debugLog("Damage calculation: %.1f base → %.2fx total multiplier → %.1f final",
                amount, totalMultiplier, finalDamage);

        return finalDamage;
    }
}