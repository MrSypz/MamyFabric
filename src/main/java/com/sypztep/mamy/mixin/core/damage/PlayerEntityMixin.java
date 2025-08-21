package com.sypztep.mamy.mixin.core.damage;

import com.sypztep.mamy.common.system.damage.DamageUtil;
import com.sypztep.mamy.common.util.LivingEntityUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = PlayerEntity.class, priority = 1500)
public class PlayerEntityMixin {

    @ModifyVariable(method = "applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V", at = @At("HEAD"), argsOnly = true)
    private float applyPreArmorDamageModification(float amount, DamageSource source) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        if (!(source.getAttacker() instanceof LivingEntity attacker)) return amount;

        boolean isCrit = LivingEntityUtil.isCrit(attacker);
        return DamageUtil.damageMonsterModifier(player, amount, source, isCrit); // player this one is target is player
    }
}