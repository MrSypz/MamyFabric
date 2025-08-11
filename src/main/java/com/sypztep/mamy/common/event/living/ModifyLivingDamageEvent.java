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

public final class ModifyLivingDamageEvent implements DominatusLivingEntityEvents.PreArmorDamage {
    public static void register() {
        DominatusLivingEntityEvents.PRE_ARMOR_DAMAGE.register(new ModifyLivingDamageEvent());
    }
    @Override
    public float preModifyDamage(LivingEntity entity, DamageSource source, float amount, boolean isCrit) {
        if (entity.getWorld().isClient()) return amount;
        if (source.getSource() instanceof PersistentProjectileEntity projectile && isCrit)
            LivingEntityUtil.playCriticalSound(projectile);
        if (ModEntityComponents.HEADSHOT.get(entity).isHeadShot() && source.getSource() instanceof PersistentProjectileEntity && source.getAttacker() instanceof LivingEntity attacker) {
            amount *= (float) attacker.getAttributeValue(ModEntityAttributes.HEADSHOT_DAMAGE);
            ModEntityComponents.HEADSHOT.get(entity).setHeadShot(false);
            ParticleHandler.sendToAll(entity, source.getSource(), ModCustomParticles.HEADSHOT);
        }
        float finalDmg = DamageUtil.damageModifier(entity, amount, source, isCrit);
        //Store value
        DominatusPlayerEntityEvents.DAMAGE_DEALT.invoker().onDamageDealt(entity, source, finalDmg);
        return finalDmg;
    }
}
