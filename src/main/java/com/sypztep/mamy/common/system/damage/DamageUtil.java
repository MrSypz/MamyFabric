package com.sypztep.mamy.common.system.damage;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.client.util.ParticleHandler;
import com.sypztep.mamy.common.api.entity.DominatusPlayerEntityEvents;
import com.sypztep.mamy.common.init.*;
import com.sypztep.mamy.common.system.difficulty.ProgressiveDifficultySystem;
import com.sypztep.mamy.common.system.skill.SkillRegistry;
import com.sypztep.mamy.common.system.classkill.acolyte.passive.DemonBanePassiveSkill;
import com.sypztep.mamy.common.system.classkill.acolyte.passive.DivineProtectionPassiveSkill;
import com.sypztep.mamy.common.util.LivingEntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;

public final class DamageUtil {
    private static final boolean DEBUG = false;

    private static void debugLog(String message, Object... args) {
        if (DEBUG) Mamy.LOGGER.info("[DamageUtil] {}", String.format(message, args));
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

        // ==========================================
        // SPECIAL SKILL BONUSES
        // ==========================================

        DEMON_BANE(ModifierOperationType.ADD, (attacker, target, source, isCrit) -> {
            if (isDemonBaneApplicable(source, target)) {
                PlayerEntity player = (PlayerEntity) attacker;
                int skillLevel = ModEntityComponents.PLAYERCLASS.get(player).getSkillLevel(SkillRegistry.DEMON_BANE);
                return DemonBanePassiveSkill.calculateDamageBonus(player, skillLevel);
            }
            return 0.0f;
        });

        // NOTE: Combat type damage bonuses (MELEE/PROJECTILE/MAGIC) are now handled
        // by ElementalDamageSystem to avoid double calculation!

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

    public static float calculateDamage(LivingEntity target, LivingEntity attacker, DamageSource source, float amount, boolean isCrit) {
        if (target.getWorld().isClient()) return amount;

        debugLog("=== DAMAGE CALCULATION START ===");
        debugLog("Target: %s, Attacker: %s, Base Damage: %.1f, Critical: %s",
                target.getClass().getSimpleName(), attacker.getClass().getSimpleName(), amount, isCrit);

        // ===== 1. HANDLE PROJECTILE CRITICAL SOUND =====
        if (source.getSource() instanceof PersistentProjectileEntity projectile && isCrit) {
            LivingEntityUtil.playCriticalSound(projectile);
        }

        // ===== 2. ENVIRONMENTAL & DIFFICULTY MODIFIERS =====
        boolean attackerIsPlayer = LivingEntityUtil.isPlayer(attacker);
        boolean targetIsPlayer = LivingEntityUtil.isPlayer(target);

        if (attackerIsPlayer) {
            PlayerEntity playerAttacker = (PlayerEntity) attacker;
            // Rain wet player damage reduction
            if (LivingEntityUtil.isPlayerWetInRain(playerAttacker)) {
                amount = Math.max(0.1f, amount - 2.0f);
                debugLog("Rain wet player damage reduction: %.1f → %.1f (-2.0)", amount + 2.0f, amount);
            }
        } else {
            // Monster attacking something
            if (targetIsPlayer) {
                // Monster attacking player - apply progressive difficulty
                amount = ProgressiveDifficultySystem.applyProgressiveDifficulty(attacker, amount);
                debugLog("Progressive difficulty applied (monster → player): %.1f", amount);
            } else {
                // Monster attacking another monster - apply difficulty based on nearest player
                PlayerEntity nearestPlayer = ProgressiveDifficultySystem.findNearestPlayer(attacker, 64.0);
                amount = ProgressiveDifficultySystem.calculateAmplifiedDamage(attacker, amount, nearestPlayer);
                debugLog("Progressive difficulty applied (monster → monster): %.1f", amount);
            }

            // Night damage multiplier for monsters
            if (attacker.getWorld().isNight()) {
                amount *= 2.0f;
                debugLog("Night monster damage: %.1f → %.1f (×2.0)", amount / 2.0f, amount);
            }
        }

        // ===== 3. APPLY UNIVERSAL COMBAT MODIFIERS =====
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

        debugLog("Combat modifiers: base %.1f × %.2fx + %.1f → final %.1f",
                amount, multiplicativeMultiplier, additiveBonus, finalDamage);

        DominatusPlayerEntityEvents.DAMAGE_DEALT.invoker().onDamageDealt(target, source, finalDamage);

        debugLog("=== DAMAGE CALCULATION END: %.1f ===", finalDamage);
        return finalDamage;
    }

    public static float damageResistanceModifier(LivingEntity defender, float amount, DamageSource source) {
        debugLog("====RESISTANCE MODIFIER START - INPUT: %.2f====", amount);

        // ElementalDamageSystem now handles ALL damage type calculations (elemental + combat type)
        ElementalDamageSystem.ElementalBreakdown originalBreakdown = ElementalDamageSystem.calculateElementalModifierWithBreakdown(defender, amount, source);
        float elementalDamage = originalBreakdown.totalDamage();

        // Apply special skill resistances
        float flatReduction = 0.0f;

        if (isDivineProtectionApplicable(source, defender)) {
            PlayerEntity player = (PlayerEntity) defender;
            int skillLevel = ModEntityComponents.PLAYERCLASS.get(player).getSkillLevel(SkillRegistry.DIVINE_PROTECTION);
            if (skillLevel > 0) {
                float divineReduction = DivineProtectionPassiveSkill.calculateDamageReduction(player, skillLevel);
                flatReduction += divineReduction;
                debugLog("Divine Protection flat reduction: %.2f", divineReduction);
            }
        }

        // Apply flat reduction
        float finalDamage = Math.max(0.0f, elementalDamage - flatReduction);

        // Send damage numbers with proportionally reduced breakdown
        if (flatReduction > 0 && finalDamage > 0 && elementalDamage > 0) {
            float reductionRatio = finalDamage / elementalDamage;
            Map<ElementType, Float> reducedBreakdown = new HashMap<>();

            for (var entry : originalBreakdown.elementalDamage().entrySet()) {
                float reducedAmount = entry.getValue() * reductionRatio;
                if (reducedAmount > 0) {
                    reducedBreakdown.put(entry.getKey(), reducedAmount);
                }
            }

            ElementalDamageSystem.sendDamageNumbers(defender,
                    new ElementalDamageSystem.ElementalBreakdown(reducedBreakdown, originalBreakdown.originalSource()));
        } else if (flatReduction == 0) {
            ElementalDamageSystem.sendDamageNumbers(defender, originalBreakdown);
        }
        // If finalDamage = 0, don't send damage numbers

        debugLog("Elemental damage: %.2f, Special flat reduction: %.2f, Final: %.2f",
                elementalDamage, flatReduction, finalDamage);
        debugLog("====RESISTANCE MODIFIER END====");

        return finalDamage;
    }

    private static float calculateDamageAfterArmor(LivingEntity self, float originalDamage, float flatArmor) {
        debugLog("====ARMOR CALCULATION START====");
        float armorReduction = getArmorDamageReduction(flatArmor);
        float damageAfterArmor = originalDamage * (1.0f - armorReduction);
        debugLog("Armor: %.1f → %.1f%% reduction → %.1f damage",
                flatArmor, armorReduction * 100, damageAfterArmor);

        float percentageReduction = (float) self.getAttributeValue(ModEntityAttributes.DAMAGE_REDUCTION);
        debugLog("Raw attribute value: %.3f", percentageReduction);

        float finalDamage = damageAfterArmor * (1.0f - percentageReduction);
        debugLog("Calculation: %.3f × (1 - %.3f) = %.3f", damageAfterArmor, percentageReduction, finalDamage);

        debugLog("====ARMOR CALCULATION END====");
        return Math.max(0.0f, finalDamage);
    }

    public static float getDamageAfterArmor(LivingEntity self, DamageSource source, float amount) {
        if (!source.isIn(DamageTypeTags.BYPASSES_ARMOR)) {
            self.damageArmor(source, amount);
            return calculateDamageAfterArmor(self, amount, self.getArmor());
        }
        return amount;
    }

    public static float getArmorDamageReduction(float armor) {
        return armor / (armor + 20.0f);
    }

    public static boolean isDivineProtectionApplicable(DamageSource source, LivingEntity defender) {
        LivingEntity attacker = getActualAttacker(source);

        if (attacker == null) return false;
        if (attacker instanceof PlayerEntity) return false;
        if (!(defender instanceof PlayerEntity)) return false;

        return attacker.getType().isIn(EntityTypeTags.UNDEAD);
    }

    public static boolean isDemonBaneApplicable(DamageSource source, LivingEntity target) {
        LivingEntity attacker = getActualAttacker(source);

        if (!(attacker instanceof PlayerEntity)) return false;
        if (target instanceof PlayerEntity) return false;

        return target.getType().isIn(EntityTypeTags.UNDEAD);
    }

    private static LivingEntity getActualAttacker(DamageSource source) {
        Entity directSource = source.getSource();
        Entity attacker = source.getAttacker();

        if (directSource instanceof LivingEntity) return (LivingEntity) directSource;
        if (attacker instanceof LivingEntity) return (LivingEntity) attacker;
        if (directSource instanceof ProjectileEntity projectile) {
            Entity owner = projectile.getOwner();
            if (owner instanceof LivingEntity) return (LivingEntity) owner;
        }

        return null;
    }
}