package com.sypztep.mamy.common.system.damage;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.client.util.ParticleHandler;
import com.sypztep.mamy.common.api.entity.DominatusPlayerEntityEvents;
import com.sypztep.mamy.common.init.*;
import com.sypztep.mamy.common.system.difficulty.ProgressiveDifficultySystem;
import com.sypztep.mamy.common.system.passive.PassiveAbilityManager;
import com.sypztep.mamy.common.system.skill.SkillRegistry;
import com.sypztep.mamy.common.system.classkill.acolyte.DemonBanePassiveSkill;
import com.sypztep.mamy.common.system.classkill.acolyte.DivineProtectionPassiveSkill;
import com.sypztep.mamy.common.util.LivingEntityUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class DamageUtil {
    private static final boolean DEBUG = true;

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
        // NON-ELEMENTAL DAMAGE BONUSES (Pre-Element System)
        // ==========================================

        PROJECTILE_DAMAGE(ModifierOperationType.ADD, (attacker, target, source, isCrit) -> {
            if (source.isIn(ModTags.DamageTags.PROJECTILE_DAMAGE)) {
                float projectileFlat = (float) attacker.getAttributeValue(ModEntityAttributes.PROJECTILE_ATTACK_DAMAGE_FLAT);
                float extra = projectileFlat + specialAttack(attacker);
                debugLog("Projectile Damage: +%.1f (%.1f projectile, %.1f special)", extra, projectileFlat, specialAttack(attacker));
                return extra;
            }
            return 0.0f;
        }),

        PROJECTILE_MULTIPLIER(ModifierOperationType.MULTIPLY, (attacker, target, source, isCrit) -> {
            if (source.isIn(ModTags.DamageTags.PROJECTILE_DAMAGE)) {
                float projectileMult = (float) attacker.getAttributeValue(ModEntityAttributes.PROJECTILE_ATTACK_DAMAGE_MULT);
                debugLog("Projectile Multiplier: %.2fx", projectileMult);
                return projectileMult;
            }
            return 0.0f;
        }),

        MAGIC_DAMAGE(ModifierOperationType.ADD, (attacker, target, source, isCrit) -> {
            if (source.isIn(ModTags.DamageTags.MAGIC_DAMAGE)) {
                float magicFlat = (float) attacker.getAttributeValue(ModEntityAttributes.MAGIC_ATTACK_DAMAGE_FLAT);
                float extra = magicFlat + specialAttack(attacker);
                debugLog("Magic Damage: +%.1f (%.1f magic, %.1f special)", extra, magicFlat, specialAttack(attacker));
                return extra;
            }
            return 0.0f;
        }),

        MAGIC_MULTIPLIER(ModifierOperationType.MULTIPLY, (attacker, target, source, isCrit) -> {
            if (source.isIn(ModTags.DamageTags.MAGIC_DAMAGE)) {
                float magicMult = (float) attacker.getAttributeValue(ModEntityAttributes.MAGIC_ATTACK_DAMAGE_MULT);
                debugLog("Magic Multiplier: %.2fx", magicMult);
                return magicMult;
            }
            return 0.0f;
        }),

        DEMON_BANE(ModifierOperationType.ADD, (attacker, target, source, isCrit) -> {
            if (isDemonBaneApplicable(attacker, target)) {
                PlayerEntity player = (PlayerEntity) attacker;
                int skillLevel = ModEntityComponents.PLAYERCLASS.get(player).getSkillLevel(SkillRegistry.DEMON_BANE);
                return DemonBanePassiveSkill.calculateDamageBonus(player, skillLevel);
            }
            return 0.0f;
        });

        // NOTE: ELEMENTAL damage (fire, electric, etc.) is handled by ElementalDamageSystem
        // Don't add them here to avoid double-processing!

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

        // ===== 2. HANDLE HEADSHOT DAMAGE =====
        if (ModEntityComponents.HEADSHOT.get(target).isHeadShot() &&
                source.getSource() instanceof PersistentProjectileEntity) {

            float headshotMultiplier = (float) attacker.getAttributeValue(ModEntityAttributes.HEADSHOT_DAMAGE);
            amount *= headshotMultiplier;
            ModEntityComponents.HEADSHOT.get(target).setHeadShot(false);
            ParticleHandler.sendToAll(target, source.getSource(), ModCustomParticles.HEADSHOT);
            debugLog("Headshot damage applied: ×%.2f → %.1f", headshotMultiplier, amount);
        }

        // ===== 3. ENVIRONMENTAL & DIFFICULTY MODIFIERS =====
        boolean attackerIsPlayer = LivingEntityUtil.isPlayer(attacker);
        boolean targetIsPlayer = LivingEntityUtil.isPlayer(target);

        if (attackerIsPlayer) {
            PlayerEntity playerAttacker = (PlayerEntity) attacker;

            // Rain wet player damage reduction
            if (LivingEntityUtil.isPlayerWetInRain(playerAttacker)) {
                amount = Math.max(0.1f, amount - 2.0f);
                debugLog("Rain wet player damage reduction: %.1f → %.1f (-2.0)", amount + 2.0f, amount);
            }

            // Handle Back Breaker passive (50% more damage to low health enemies)
            if (LivingEntityUtil.isHealthBelow(target, 0.25f) &&
                    PassiveAbilityManager.isActive(playerAttacker, ModPassiveAbilities.BACK_BREAKER)) {
                amount *= 1.5f; // 50% bonus
                debugLog("Back Breaker passive applied: %.1f → %.1f (×1.5)", amount / 1.5f, amount);
            }

            // Handle Berserker Rage passive
            if (PassiveAbilityManager.isActive(playerAttacker, ModPassiveAbilities.BERSERKER_RAGE)) {
                amount = LivingEntityUtil.getBerserkerDamageBonus(playerAttacker);
                debugLog("Berserker Rage passive applied: final damage %.1f", amount);
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

        // ===== 4. APPLY UNIVERSAL COMBAT MODIFIERS =====
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
        debugLog("====RESISTANCE MODIFIER START====");

        float elementalDamage = ElementalDamageSystem.calculateElementalModifier(defender, amount, source);

        float flatReduction = 0.0f;

        // Combat type flat reductions
        if (source.isIn(ModTags.DamageTags.MAGIC_DAMAGE)) {
            flatReduction += (float) defender.getAttributeValue(ModEntityAttributes.FLAT_MAGIC_REDUCTION);
            debugLog("Magic flat reduction: %.2f", (float) defender.getAttributeValue(ModEntityAttributes.FLAT_MAGIC_REDUCTION));
        }
        if (source.isIn(ModTags.DamageTags.PROJECTILE_DAMAGE)) {
            flatReduction += (float) defender.getAttributeValue(ModEntityAttributes.FLAT_PROJECTILE_REDUCTION);
            debugLog("Projectile flat reduction: %.2f", (float) defender.getAttributeValue(ModEntityAttributes.FLAT_PROJECTILE_REDUCTION));
        }
        if (source.isIn(ModTags.DamageTags.MELEE_DAMAGE)) {
            flatReduction += (float) defender.getAttributeValue(ModEntityAttributes.FLAT_MELEE_REDUCTION);
            debugLog("Melee flat reduction: %.2f", (float) defender.getAttributeValue(ModEntityAttributes.FLAT_MELEE_REDUCTION));
        }

        // Step 3: Apply combat type percentage resistances
        float percentageReduction = 0.0f;

        if (source.isIn(ModTags.DamageTags.PROJECTILE_DAMAGE)) {
            percentageReduction += (float) defender.getAttributeValue(ModEntityAttributes.PROJECTILE_RESISTANCE);
            debugLog("Projectile resistance: %.2f", (float) defender.getAttributeValue(ModEntityAttributes.PROJECTILE_RESISTANCE));
        }
        if (source.isIn(ModTags.DamageTags.MELEE_DAMAGE)) {
            percentageReduction += (float) defender.getAttributeValue(ModEntityAttributes.MELEE_RESISTANCE);
            debugLog("Melee resistance: %.2f", (float) defender.getAttributeValue(ModEntityAttributes.MELEE_RESISTANCE));
        }
        if (source.isIn(ModTags.DamageTags.MAGIC_DAMAGE)) {
            percentageReduction += (float) defender.getAttributeValue(ModEntityAttributes.MAGIC_RESISTANCE);
            debugLog("Magic resistance: %.2f", (float) defender.getAttributeValue(ModEntityAttributes.MAGIC_RESISTANCE));
        }

        if (isDivineProtectionApplicable((LivingEntity) source.getAttacker(), defender)) {
            PlayerEntity player = (PlayerEntity) defender;
            int skillLevel = ModEntityComponents.PLAYERCLASS.get(player).getSkillLevel(SkillRegistry.DIVINE_PROTECTION);
            if (skillLevel > 0) {
                float divineReduction = DivineProtectionPassiveSkill.calculateDamageReduction(player, skillLevel);
                flatReduction += divineReduction;
                debugLog("Divine Protection flat reduction: %.2f", divineReduction);
            }
        }

        float afterPercentageReduction = elementalDamage * (1.0f - Math.min(0.95f, percentageReduction));

        // Apply flat reduction
        float finalDamage = Math.max(0.1f, afterPercentageReduction - flatReduction);

        debugLog("Elemental damage: %.2f, Percentage reduction: %.2f, After percentage: %.2f, Flat reduction: %.2f, Final: %.2f",
                elementalDamage, percentageReduction, afterPercentageReduction, flatReduction, finalDamage);
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
        return Math.max(0.1f, finalDamage);
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
    public static boolean isDemonBaneApplicable(LivingEntity attacker, LivingEntity target) {
        if (!(attacker instanceof PlayerEntity)) return false;
        if (target instanceof PlayerEntity) return false;

        return target.getType().isIn(EntityTypeTags.UNDEAD);
    }
    public static boolean isDivineProtectionApplicable(LivingEntity attacker, LivingEntity defender) {
        if (attacker instanceof PlayerEntity) return false;
        if (!(defender instanceof PlayerEntity)) return false;

        return attacker.getType().isIn(EntityTypeTags.UNDEAD);
    }
}