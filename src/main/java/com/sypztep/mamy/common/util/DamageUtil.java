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

    @FunctionalInterface
    private interface DamageResistanceModifier {
        float get(LivingEntity defender, DamageSource source, float amount);
    }

    private static float specialAttack(LivingEntity attacker) {
        return (float) attacker.getAttributeValue(ModEntityAttributes.SPECIAL_ATTACK);
    }

    private static float specialDefense(LivingEntity defender) {
        return (float) defender.getAttributeValue(ModEntityAttributes.SPECIAL_DEFENSE);
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

        PROJECTILE_DAMAGE(ModifierOperationType.ADD, (attacker, target, source, isCrit) -> {
            if (source.isIn(ModTags.DamageTags.PROJECTILE_DAMAGE)) {
                float projectileBonus = (float) attacker.getAttributeValue(ModEntityAttributes.PROJECTILE_ATTACK_DAMAGE_FLAT);
                float extra = projectileBonus + specialAttack(attacker);
                debugLog("Projectile Damage: +%.1f (%.1f projectile, %.1f special)", extra, projectileBonus, specialAttack(attacker));
                return extra;
            }
            return 0.0f;
        }),

        MELEE_DAMAGE(ModifierOperationType.ADD, (attacker, target, source, isCrit) -> {
            if (source.isIn(ModTags.DamageTags.MELEE_DAMAGE)) {
                float meleeBonus = (float) attacker.getAttributeValue(ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT);
                float extra = meleeBonus + specialAttack(attacker);
                debugLog("Melee Damage: +%.1f (%.1f melee, %.1f special)", extra, meleeBonus, specialAttack(attacker));
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
        }),

        // ADD ELEMENTAL DAMAGE MODIFIERS
        FIRE_DAMAGE(ModifierOperationType.ADD, (attacker, target, source, isCrit) -> {
            // You can add custom fire damage tags or use vanilla fire tags
            if (source.isIn(DamageTypeTags.IS_FIRE)) {
                float fireBonus = (float) attacker.getAttributeValue(ModEntityAttributes.FIRE_ATTACK_DAMAGE_FLAT);
                float extra = fireBonus + specialAttack(attacker);
                debugLog("Fire Damage: +%.1f (%.1f fire, %.1f special)", extra, fireBonus, specialAttack(attacker));
                return extra;
            }
            return 0.0f;
        }),

        ELECTRIC_DAMAGE(ModifierOperationType.ADD, (attacker, target, source, isCrit) -> {
            // You'll need to create custom electric damage tags
            if (source.isIn(ModTags.DamageTags.ELECTRIC_DAMAGE)) { // Add this to your ModTags
                float electricBonus = (float) attacker.getAttributeValue(ModEntityAttributes.ELECTRIC_ATTACK_DAMAGE_FLAT);
                float extra = electricBonus + specialAttack(attacker);
                debugLog("Electric Damage: +%.1f (%.1f electric, %.1f special)", extra, electricBonus, specialAttack(attacker));
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

    private enum ResistanceModifierType {
        PROJECTILE_RESISTANCE(ModifierOperationType.MULTIPLY, (defender, source, amount) -> {
            if (source.isIn(ModTags.DamageTags.PROJECTILE_DAMAGE)) {
                float projectileResistance = (float) defender.getAttributeValue(ModEntityAttributes.PROJECTILE_RESISTANCE);
                float resistance = projectileResistance + specialDefense(defender);
                debugLog("Projectile Resistance: %.1f%% (%.1f projectile, %.1f special)", resistance * 100, projectileResistance * 100, specialDefense(defender) * 100);
                return resistance;
            }
            return 0.0f;
        }),

        MELEE_RESISTANCE(ModifierOperationType.MULTIPLY, (defender, source, amount) -> {
            if (source.isIn(ModTags.DamageTags.MELEE_DAMAGE)) {
                float meleeResistance = (float) defender.getAttributeValue(ModEntityAttributes.MELEE_RESISTANCE);
                float resistance = meleeResistance + specialDefense(defender);
                debugLog("Melee Resistance: %.1f%% (%.1f melee, %.1f special)", resistance * 100, meleeResistance * 100, specialDefense(defender) * 100);
                return resistance;
            }
            return 0.0f;
        }),

        MAGIC_RESISTANCE(ModifierOperationType.MULTIPLY, (defender, source, amount) -> {
            if (source.isIn(ModTags.DamageTags.MAGIC_DAMAGE)) {
                float magicResistance = (float) defender.getAttributeValue(ModEntityAttributes.MAGIC_RESISTANCE);
                float resistance = magicResistance + specialDefense(defender);
                debugLog("Magic Resistance: %.1f%% (%.1f magic, %.1f special)", resistance * 100, magicResistance * 100, specialDefense(defender) * 100);
                return resistance;
            }
            return 0.0f;
        }),

        FIRE_RESISTANCE(ModifierOperationType.MULTIPLY, (defender, source, amount) -> {
            if (source.isIn(DamageTypeTags.IS_FIRE)) {
                float fireResistance = (float) defender.getAttributeValue(ModEntityAttributes.FIRE_RESISTANCE);
                float resistance = fireResistance + specialDefense(defender);
                debugLog("Fire Resistance: %.1f%% (%.1f fire, %.1f special)", resistance * 100, fireResistance * 100, specialDefense(defender) * 100);
                return resistance;
            }
            return 0.0f;
        }),

        ELECTRIC_RESISTANCE(ModifierOperationType.MULTIPLY, (defender, source, amount) -> {
            if (source.isIn(ModTags.DamageTags.ELECTRIC_DAMAGE)) {
                float electricResistance = (float) defender.getAttributeValue(ModEntityAttributes.ELECTRIC_RESISTANCE);
                float resistance = electricResistance + specialDefense(defender);
                debugLog("Electric Resistance: %.1f%% (%.1f electric, %.1f special)", resistance * 100, electricResistance * 100, specialDefense(defender) * 100);
                return resistance;
            }
            return 0.0f;
        }),

        // Add flat damage reduction for specific damage types
        FLAT_DAMAGE_REDUCTION(ModifierOperationType.ADD, (defender, source, amount) -> {
            float flatReduction = 0.0f;

            if (source.isIn(ModTags.DamageTags.PROJECTILE_DAMAGE)) {
                flatReduction += (float) defender.getAttributeValue(ModEntityAttributes.FLAT_PROJECTILE_REDUCTION);
            }
            if (source.isIn(ModTags.DamageTags.MELEE_DAMAGE)) {
                flatReduction += (float) defender.getAttributeValue(ModEntityAttributes.FLAT_MELEE_REDUCTION);
            }
            if (source.isIn(ModTags.DamageTags.MAGIC_DAMAGE)) {
                flatReduction += (float) defender.getAttributeValue(ModEntityAttributes.FLAT_MAGIC_REDUCTION);
            }
            if (source.isIn(DamageTypeTags.IS_FIRE)) {
                flatReduction += (float) defender.getAttributeValue(ModEntityAttributes.FLAT_FIRE_REDUCTION);
            }
            if (source.isIn(ModTags.DamageTags.ELECTRIC_DAMAGE)) {
                flatReduction += (float) defender.getAttributeValue(ModEntityAttributes.FLAT_ELECTRIC_REDUCTION);
            }

            if (flatReduction > 0) {
                flatReduction += specialDefense(defender);
                debugLog("Flat Damage Reduction: -%.1f damage", flatReduction);
            }

            return flatReduction;
        });

        private final ModifierOperationType opType;
        private final DamageResistanceModifier modifier;

        ResistanceModifierType(ModifierOperationType opType, DamageResistanceModifier modifier) {
            this.opType = opType;
            this.modifier = modifier;
        }

        public ModifierOperationType getOperationType() {
            return opType;
        }

        public float getModifierValue(LivingEntity defender, DamageSource source, float amount) {
            return modifier.get(defender, source, amount);
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
                multiplicativeMultiplier *= (1.0f + value); // value = 0.3 → ×1.3
            }
        }

        float finalDamage = (amount * multiplicativeMultiplier) + additiveBonus;

        debugLog("Damage calculation: base %.1f × %.2fx + %.1f → final %.1f", amount, multiplicativeMultiplier, additiveBonus, finalDamage);

        return finalDamage;
    }

    /**
     * Handles damage reductions (defender resistances)
     */
    public static float damageResistanceModifier(LivingEntity defender, float amount, DamageSource source) {
        debugLog("====RESISTANCE START====");

        float flatReduction = 0.0f;
        float percentageReduction = 1.0f;

        for (ResistanceModifierType resistanceType : ResistanceModifierType.values()) {
            float value = resistanceType.getModifierValue(defender, source, amount);

            if (resistanceType.getOperationType() == ModifierOperationType.ADD) {
                flatReduction += value;
            } else if (resistanceType.getOperationType() == ModifierOperationType.MULTIPLY) {
                percentageReduction *= (1.0f - value); // value = 0.3 (30%) → ×0.7 (reduce by 30%)
            }
        }

        // Apply percentage reduction first, then subtract flat reduction
        float finalDamage = (amount * percentageReduction) - flatReduction;

        // Ensure minimum damage
        finalDamage = Math.max(0.1f, finalDamage);

        debugLog("Resistance calculation: base %.1f × %.2fx - %.1f → final %.1f",
                amount, percentageReduction, flatReduction, finalDamage);
        debugLog("====RESISTANCE END====");

        return finalDamage;
    }

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

    //Armour
    public static float getDamageAfterArmor(LivingEntity self, DamageSource source, float amount) {
        if (!source.isIn(DamageTypeTags.BYPASSES_ARMOR)) {
            self.damageArmor(source, amount);
            return calculateDamageAfterArmor(self,amount,source,self.getArmor());
        }
        return amount;
    }

    public static float getArmorDamageReduction(float armor) {
        return (armor / (armor + 20.0f)) * 100f;
    }
}