package com.sypztep.mamy.common.event.living;

import com.sypztep.mamy.common.api.entity.DominatusLivingEntityEvents;
import com.sypztep.mamy.common.api.entity.DominatusPlayerEntityEvents;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.util.DamageUtil;
import com.sypztep.mamy.common.util.LivingEntityUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.PersistentProjectileEntity;

public final class ModifyLivingDamageEvent implements DominatusLivingEntityEvents.PostArmorDamage {
    public static void register() {
        DominatusLivingEntityEvents.POST_ARMOR_DAMAGE.register(new ModifyLivingDamageEvent());
    }
    @Override
    public float postModifyDamage(LivingEntity entity, DamageSource source, float amount, boolean isCrit) {
        if (entity.getWorld().isClient()) return amount;
        if (source.getSource() instanceof PersistentProjectileEntity projectile && isCrit)
            LivingEntityUtil.playCriticalSound(projectile);
        if (ModEntityComponents.HEADSHOT.get(entity).isHeadShot() && source.getSource() instanceof PersistentProjectileEntity) {
            amount = amount * 5;
            ModEntityComponents.HEADSHOT.get(entity).setHeadShot(false);
        }
        float finalDmg = DamageUtil.damageModifier(entity, amount, source, isCrit);
        //Store value
        DominatusPlayerEntityEvents.DAMAGE_DEALT.invoker().onDamageDealt(entity, source, finalDmg);
        return finalDmg;
    }
}
