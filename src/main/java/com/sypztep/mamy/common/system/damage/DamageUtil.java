package com.sypztep.mamy.common.system.damage;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.client.util.ParticleHandler;
import com.sypztep.mamy.common.api.entity.DominatusPlayerEntityEvents;
import com.sypztep.mamy.common.init.*;
import com.sypztep.mamy.common.system.passive.PassiveAbilityManager;
import com.sypztep.mamy.common.util.LivingEntityUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
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

    /**
     * Monster attack player with environmental modifiers
     */
    public static float damageMonsterModifier(LivingEntity target, float amount, DamageSource source, boolean isCrit) {
        if (target.getWorld().isClient()) return amount;
        if (!(source.getAttacker() instanceof LivingEntity attacker)) return amount;

        // Handle projectile critical sound
        if (source.getSource() instanceof PersistentProjectileEntity projectile && isCrit) {
            LivingEntityUtil.playCriticalSound(projectile);
        }

        // ===== ENVIRONMENTAL DAMAGE CONDITIONS FOR MONSTERS =====
        // Night damage multiplier for monsters
        if (attacker.getWorld().isNight() && !LivingEntityUtil.isPlayer(attacker)) {
            amount *= 2.0f;
            debugLog("Night monster damage: %.1f → %.1f (×2.0)", amount / 2.0f, amount);
        }

        // Apply combat modifiers (existing logic)
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

        debugLog("Monster damage calculation: base %.1f × %.2fx + %.1f → final %.1f",
                amount, multiplicativeMultiplier, additiveBonus, finalDamage);

        DominatusPlayerEntityEvents.DAMAGE_DEALT.invoker().onDamageDealt(target, source, finalDamage);
        return finalDamage;
    }

    /**
     * Player attack monster with environmental modifiers
     */
    public static float damageModifier(LivingEntity target, float amount, DamageSource source, boolean isCrit) {
        if (target.getWorld().isClient()) return amount;
        if (!(source.getAttacker() instanceof LivingEntity attacker)) return amount;

        if (source.getSource() instanceof PersistentProjectileEntity projectile && isCrit) {
            LivingEntityUtil.playCriticalSound(projectile);
        }

        if (LivingEntityUtil.isPlayer(attacker) && LivingEntityUtil.isPlayerWetInRain((PlayerEntity) attacker)) {
            amount = Math.max(0.1f, amount - 2.0f);
            debugLog("Rain wet player damage reduction: %.1f → %.1f (-2.0)", amount + 2.0f, amount);
        }

        // Handle headshot damage
        if (ModEntityComponents.HEADSHOT.get(target).isHeadShot() &&
                source.getSource() instanceof PersistentProjectileEntity &&
                source.getAttacker() instanceof LivingEntity headshotAttacker) {

            amount *= (float) headshotAttacker.getAttributeValue(ModEntityAttributes.HEADSHOT_DAMAGE);
            ModEntityComponents.HEADSHOT.get(target).setHeadShot(false);
            ParticleHandler.sendToAll(target, source.getSource(), ModCustomParticles.HEADSHOT);
        }

        // Handle Back Breaker passive (50% more damage to low health enemies)
        if (LivingEntityUtil.isHealthBelow(target, 0.25f) &&
                source.getAttacker() instanceof PlayerEntity backBreakerAttacker &&
                PassiveAbilityManager.isActive(backBreakerAttacker, ModPassiveAbilities.BACK_BREAKER)) {

            amount *= 1.5f; // 50% bonus
        }

        // Handle Berserker Rage passive
        if (source.getAttacker() instanceof PlayerEntity berserkerAttacker &&
                PassiveAbilityManager.isActive(berserkerAttacker, ModPassiveAbilities.BERSERKER_RAGE)) {

            amount = LivingEntityUtil.getBerserkerDamageBonus(berserkerAttacker);
        }

        // Apply combat modifiers (existing logic)
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

        debugLog("Player damage calculation: base %.1f × %.2fx + %.1f → final %.1f",
                amount, multiplicativeMultiplier, additiveBonus, finalDamage);

        DominatusPlayerEntityEvents.DAMAGE_DEALT.invoker().onDamageDealt(target, source, finalDamage);
        return finalDamage;
    }

    /**
     * UPDATED: Handle elemental damage system + ALL non-elemental flat reductions
     */
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

        float afterPercentageReduction = elementalDamage * (1.0f - Math.min(0.95f, percentageReduction));

        // Apply flat reduction
        float finalDamage = Math.max(0.1f, afterPercentageReduction - flatReduction);

        debugLog("Elemental damage: %.2f, Percentage reduction: %.2f, After percentage: %.2f, Flat reduction: %.2f, Final: %.2f",
                elementalDamage, percentageReduction, afterPercentageReduction, flatReduction, finalDamage);
        debugLog("====RESISTANCE MODIFIER END====");

        return finalDamage;
    }

    private static float calculateDamageAfterArmor(LivingEntity self, float originalDamage,
                                                   float flatArmor) {
        debugLog("====START====");
        float armorReduction = getArmorDamageReduction(flatArmor);
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
            return calculateDamageAfterArmor(self, amount, self.getArmor());
        }
        return amount;
    }

    public static float getArmorDamageReduction(float armor) {
        return armor / (armor + 20.0f);
    }
}