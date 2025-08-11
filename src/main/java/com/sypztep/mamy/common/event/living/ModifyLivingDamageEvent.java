package com.sypztep.mamy.common.event.living;

import com.sypztep.mamy.client.util.ParticleHandler;
import com.sypztep.mamy.common.api.entity.DominatusLivingEntityEvents;
import com.sypztep.mamy.common.api.entity.DominatusPlayerEntityEvents;
import com.sypztep.mamy.common.init.ModCustomParticles;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.util.DamageUtil;
import com.sypztep.mamy.common.util.LivingEntityUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class ModifyLivingDamageEvent implements DominatusLivingEntityEvents.PostArmorDamage {
    public static void register() {
        DominatusLivingEntityEvents.POST_ARMOR_DAMAGE.register(new ModifyLivingDamageEvent());
    }
    @Override
    public float postModifyDamage(LivingEntity entity, DamageSource source, float amount, boolean isCrit) {
//        if (entity.getWorld().isClient()) return amount;
//        if (source.getSource() instanceof PersistentProjectileEntity projectile && isCrit)
//            LivingEntityUtil.playCriticalSound(projectile);
//        if (ModEntityComponents.HEADSHOT.get(entity).isHeadShot() && source.getSource() instanceof PersistentProjectileEntity) {
//            amount = amount * 5;
//            ModEntityComponents.HEADSHOT.get(entity).setHeadShot(false);
//        }
//        float finalDmg = DamageUtil.damageModifier(entity, amount, source, isCrit);
//        //Store value
//        DominatusPlayerEntityEvents.DAMAGE_DEALT.invoker().onDamageDealt(entity, source, finalDmg);
//        return finalDmg;
        if (source.getAttacker() instanceof LivingEntity attacker) {
            float totalMultiplier = 1.0f;

            // Crit check for NON-PLAYER attackers only
            if (!(LivingEntityUtil.isPlayer(attacker))) {
                if (LivingEntityUtil.isCrit(attacker)) {
                    ParticleHandler.sendToAll(entity, attacker, ModCustomParticles.CRITICAL);
                    ParticleHandler.sendToAll(entity, attacker, ParticleTypes.CRIT);
                    LivingEntityUtil.playCriticalSound(entity);
                    totalMultiplier += (float) attacker.getAttributeValue(ModEntityAttributes.CRIT_DAMAGE);
                }
            }

            Vec3d entityPos = entity.getPos();
            Vec3d attackerPos = attacker.getPos();
            Vec3d damageVector = attackerPos.subtract(entityPos).normalize();

            float damageDirection = (float) Math.toDegrees(Math.atan2(-damageVector.x, damageVector.z));
            float angleDifference = Math.abs(MathHelper.subtractAngles(entity.getHeadYaw(), damageDirection));

            if (angleDifference >= 75) {
                ParticleHandler.sendToAll(entity, attacker, ModCustomParticles.BACKATTACK);
                totalMultiplier += (float) attacker.getAttributeValue(ModEntityAttributes.BACK_ATTACK);
            }

            // === DAMAGE TYPE BONUSES (Using utility method) ===
//            totalMultiplier += DamageTypeUtil.calculateDamageBonus(attacker, source);

            float finalDamage = amount * totalMultiplier;

            // === RESISTANCE CALCULATIONS (Using utility method) ===
//            float totalResistance = DamageTypeUtil.calculateResistance(entity, source);
//            float resistanceReduction = Math.max(1.0f - totalResistance, 0.25f); // Min 25% damage
//            finalDamage *= resistanceReduction;

            DominatusPlayerEntityEvents.DAMAGE_DEALT.invoker().onDamageDealt(entity, source, finalDamage);

            return finalDamage;
        }
        return amount;
    }
}
