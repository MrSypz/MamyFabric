package com.sypztep.mamy.common.util;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.client.util.ParticleHandler;
import com.sypztep.mamy.common.init.ModCustomParticles;
import com.sypztep.mamy.common.init.ModTags;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class DamageUtil {
    private static final boolean DEBUG = true;

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
            return 0.0f;
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

        // Keep only NON-ELEMENTAL damage modifiers
        PROJECTILE_DAMAGE(ModifierOperationType.ADD, (attacker, target, source, isCrit) -> {
            if (source.isIn(ModTags.DamageTags.PROJECTILE_DAMAGE)) {
                float projectileBonus = (float) attacker.getAttributeValue(ModEntityAttributes.PROJECTILE_ATTACK_DAMAGE_FLAT);
                float extra = projectileBonus + specialAttack(attacker);
                debugLog("Projectile Damage: +%.1f (%.1f projectile, %.1f special)", extra, projectileBonus, specialAttack(attacker));
                return extra;
            }
            return 0.0f;
        }),

        MAGIC_DAMAGE(ModifierOperationType.ADD, (attacker, target, source, isCrit) -> {
            if (source.isIn(ModTags.DamageTags.MAGIC_DAMAGE)) {
                float magicBonus = (float) attacker.getAttributeValue(ModEntityAttributes.MAGIC_ATTACK_DAMAGE_FLAT);
                float extra = magicBonus + specialAttack(attacker);
                debugLog("Magic Damage: +%.1f (%.1f magic, %.1f special)", extra, magicBonus, specialAttack(attacker));
                return extra;
            }
            return 0.0f;
        });

        // REMOVED: MELEE_DAMAGE, FIRE_DAMAGE, ELECTRIC_DAMAGE - let ElementalDamageSystem handle these

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

    /**
     * Handles damage increases (attacker bonuses)
     */
    public static float damageModifier(LivingEntity target, float amount, DamageSource source, boolean isCrit) {
        if (!(source.getAttacker() instanceof LivingEntity attacker)) return amount;

        float additiveBonus = 0.0f;
        float multiplicativeMultiplier = 1.0f;

        for (CombatModifierType modifierType : CombatModifierType.values()) {
            float value = modifierType.getModifierValue(attacker, target, source, isCrit);

            if (modifierType.getOperationType() == ModifierOperationType.ADD) {
                additiveBonus += value;
            } else if (modifierType.getOperationType() == ModifierOperationType.MULTIPLY) {
                multiplicativeMultiplier *= (1.0f + value);
            }
        }

        float finalDamage = (amount * multiplicativeMultiplier) + additiveBonus;

        debugLog("Damage calculation: base %.1f × %.2fx + %.1f → final %.1f", amount, multiplicativeMultiplier, additiveBonus, finalDamage);

        return finalDamage;
    }

    /**
     * SIMPLIFIED: Only handle elemental damage system + non-elemental flat reductions
     */
    public static float damageResistanceModifier(LivingEntity defender, float amount, DamageSource source) {
        debugLog("====RESISTANCE MODIFIER START====");

        float elementalDamage = ElementalDamageSystem.calculateElementalModifier(defender, amount, source);

        float flatReduction = 0.0f;

        // Keep only non-elemental flat reductions
        if (source.isIn(ModTags.DamageTags.MAGIC_DAMAGE)) {
            flatReduction += (float) defender.getAttributeValue(ModEntityAttributes.FLAT_MAGIC_REDUCTION);
        }
        // You could add other non-elemental flat reductions here

        float finalDamage = Math.max(0.1f, elementalDamage - flatReduction);

        debugLog("Elemental damage: %.2f, Flat reduction: %.2f, Final: %.2f",
                elementalDamage, flatReduction, finalDamage);
        debugLog("====RESISTANCE MODIFIER END====");

        return finalDamage;
    }

    // Keep armor and other utility methods unchanged
    public static float calculateDamageAfterArmor(LivingEntity self, float originalDamage,
                                                  DamageSource source, float flatArmor) {
        debugLog("====START====");
        float armorReduction = flatArmor / (flatArmor + 20.0f);
        float damageAfterArmor = originalDamage * (1.0f - armorReduction);
        debugLog("Armor: %.1f → %.1f%% reduction → %.1f damage",
                flatArmor, armorReduction * 100, damageAfterArmor);
        float percentageReduction = (float) self.getAttributeValue(ModEntityAttributes.DAMAGE_REDUCTION);
        debugLog("Raw attribute value: %.3f", percentageReduction);
        float finalDamage = damageAfterArmor * (1.0f - percentageReduction);
        debugLog("Calculation: %.3f × (1 - %.3f) = %.3f", damageAfterArmor, percentageReduction, finalDamage);

        debugLog("====END====");
        return Math.max(0.1f, finalDamage);
    }

    public static float getDamageAfterArmor(LivingEntity self, DamageSource source, float amount) {
        if (!source.isIn(DamageTypeTags.BYPASSES_ARMOR)) {
            self.damageArmor(source, amount);
            return calculateDamageAfterArmor(self, amount, source, self.getArmor());
        }
        return amount;
    }

    public static float getArmorDamageReduction(float armor) {
        return (armor / (armor + 20.0f)) * 100f;
    }
}