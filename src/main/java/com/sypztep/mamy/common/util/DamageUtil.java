package com.sypztep.mamy.common.util;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.client.util.ParticleHandler;
import com.sypztep.mamy.common.init.ModCustomParticles;
import com.sypztep.mamy.common.init.ModTags;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class DamageUtil {
    private static final boolean DEBUG = false;

    private static void debugLog(String message, Object... args) {
        if (DEBUG) Mamy.LOGGER.info("[CombatUtil] {}", String.format(message, args));
    }

    @FunctionalInterface
    private interface DamageModifier {
        float get(LivingEntity attacker, LivingEntity target, DamageSource source, boolean isCrit);
    }

    private static float specialAttack(LivingEntity attacker) {
        return (float) attacker.getAttributeValue(ModEntityAttributes.SPECIAL_ATTACK);
    }

    private enum ModifierOperationType {
        MULTIPLY, ADD
    }

    private enum CombatModifierType {
        CRITICAL(ModifierOperationType.MULTIPLY, (attacker, target, source, isCrit) -> {
            if (isCrit) {
                ParticleHandler.sendToAll(target, attacker, ModCustomParticles.CRITICAL);
                ParticleHandler.sendToAll(target, attacker, ParticleTypes.CRIT);
                LivingEntityUtil.playCriticalSound(target);

                float critBonus = (float) attacker.getAttributeValue(ModEntityAttributes.CRIT_DAMAGE);
                float multiplier = critBonus + specialAttack(attacker);

                debugLog("Critical Hit: +%.1f from crit, +%.1f from special", critBonus, specialAttack(attacker));
                return multiplier;
            }
            return 0.0f; // 0 = no change when additive/multiply with 1.0
        }),

        BACK_ATTACK(ModifierOperationType.MULTIPLY, (attacker, target, source, isCrit) -> {
            Vec3d entityPos = target.getPos();
            Vec3d attackerPos = attacker.getPos();
            Vec3d damageVector = attackerPos.subtract(entityPos).normalize();

            float damageDirection = (float) Math.toDegrees(Math.atan2(-damageVector.x, damageVector.z));
            float angleDifference = Math.abs(MathHelper.subtractAngles(target.getHeadYaw(), damageDirection));

            if (angleDifference >= 75) {
                ParticleHandler.sendToAll(target, attacker, ModCustomParticles.BACKATTACK);

                float backBonus = (float) attacker.getAttributeValue(ModEntityAttributes.BACK_ATTACK);
                float multiplier = backBonus + specialAttack(attacker);

                debugLog("Back Attack: +%.1f from back, +%.1f from special", backBonus, specialAttack(attacker));
                return multiplier;
            }
            return 0.0f;
        }),

        PROJECTILE_DAMAGE(ModifierOperationType.ADD, (attacker, target, source, isCrit) -> {
            if (source.isIn(ModTags.DamageTags.PROJECTILE_DAMAGE)) {
                float projectileBonus = (float) attacker.getAttributeValue(ModEntityAttributes.PROJECTILE_ATTACK_DAMAGE);
                float extra = projectileBonus + specialAttack(attacker);
                debugLog("Projectile Damage: +%.1f (%.1f projectile, %.1f special)", extra, projectileBonus, specialAttack(attacker));
                return extra;
            }
            return 0.0f;
        }),

        MELEE_DAMAGE(ModifierOperationType.ADD, (attacker, target, source, isCrit) -> {
            if (source.isIn(ModTags.DamageTags.MELEE_DAMAGE)) {
                float meleeBonus = (float) attacker.getAttributeValue(ModEntityAttributes.MELEE_ATTACK_DAMAGE);
                float extra = meleeBonus + specialAttack(attacker);
                debugLog("Melee Damage: +%.1f (%.1f melee, %.1f special)", extra, meleeBonus, specialAttack(attacker));
                return extra;
            }
            return 0.0f;
        }),

        MAGIC_DAMAGE(ModifierOperationType.ADD, (attacker, target, source, isCrit) -> {
            if (source.isIn(ModTags.DamageTags.MAGIC_DAMAGE)) {
                float meleeBonus = (float) attacker.getAttributeValue(ModEntityAttributes.MAGIC_ATTACK_DAMAGE);
                float extra = meleeBonus + specialAttack(attacker);
                debugLog("Melee Damage: +%.1f (%.1f melee, %.1f special)", extra, meleeBonus, specialAttack(attacker));
                return extra;
            }
            return 0.0f;
        });



        private final ModifierOperationType opType;
        private final DamageModifier modifier;

        CombatModifierType(ModifierOperationType opType, DamageModifier modifier) {
            this.opType = opType;
            this.modifier = modifier;
        }

        public ModifierOperationType getOperationType() {
            return opType;
        }

        public float getModifierValue(LivingEntity attacker, LivingEntity target, DamageSource source, boolean isCrit) {
            return modifier.get(attacker, target, source, isCrit);
        }
    }

    public static float damageModifier(LivingEntity target, float amount, DamageSource source, boolean isCrit) {
        if (!(source.getAttacker() instanceof LivingEntity attacker)) return amount;

        float additiveBonus = 0.0f;
        float multiplicativeMultiplier = 1.0f;

        for (CombatModifierType modifierType : CombatModifierType.values()) {
            float value = modifierType.getModifierValue(attacker, target, source, isCrit);

            if (modifierType.getOperationType() == ModifierOperationType.ADD) {
                additiveBonus += value;
            } else if (modifierType.getOperationType() == ModifierOperationType.MULTIPLY) {
                multiplicativeMultiplier *= (1.0f + value); // value = 0.3 → ×1.3
            }
        }

        float finalDamage = (amount * multiplicativeMultiplier) + additiveBonus;

        debugLog("Damage calculation: base %.1f × %.2fx + %.1f → final %.1f", amount, multiplicativeMultiplier, additiveBonus, finalDamage);

        return finalDamage;
    }

    public static float calculateFinalDamage(LivingEntity self, float originalDamage,
                                             DamageSource source, float flatArmor) {
    debugLog("====START====");
        float armorPenetration = 0.0f;
        ItemStack itemStack = source.getWeaponStack();

        if (itemStack != null && self.getWorld() instanceof ServerWorld serverWorld) {
            armorPenetration = 1.0f - EnchantmentHelper.getArmorEffectiveness(
                    serverWorld, itemStack, self, source, 1.0f);
            debugLog("Armor Penetration: %.1f%%", armorPenetration * 100);
        }

        float effectiveArmor = flatArmor * (1.0f - armorPenetration);
        debugLog("Base Armor: %.1f → Effective: %.1f", flatArmor, effectiveArmor);

        float damageReduction = effectiveArmor / (effectiveArmor + 20.0f);
        float damageAfterArmor = originalDamage * (1.0f - damageReduction);

        debugLog("Damage After Armor: %.1f (%.1f%% reduction)", damageAfterArmor, damageReduction * 100);

        float percentageReduction = (float) self.getAttributeValue(ModEntityAttributes.DAMAGE_REDUCTION);
        float finalDamage = damageAfterArmor * (1.0f - percentageReduction);

        debugLog("Final Damage: %.1f", finalDamage);
        debugLog("====END====");
        return Math.max(0.1f, finalDamage);
    }
}
