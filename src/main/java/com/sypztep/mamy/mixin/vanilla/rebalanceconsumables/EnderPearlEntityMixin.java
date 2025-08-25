package com.sypztep.mamy.mixin.vanilla.rebalanceconsumables;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnderPearlEntity.class)
public class EnderPearlEntityMixin {
	@WrapWithCondition(method = "onCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
	private boolean noDamagePearl(Entity instance, DamageSource source, float amount) {
		return false;
	}
}
