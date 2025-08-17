package com.sypztep.mamy.mixin.core.passive.intelligence.xray.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.sypztep.mamy.common.init.ModPassiveAbilities;
import com.sypztep.mamy.common.system.passive.PassiveAbilityManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
	@Shadow
	@Nullable
	public ClientPlayerEntity player;

	@ModifyReturnValue(method = "hasOutline", at = @At("RETURN"))
	private boolean elementAffinityPassive(boolean original, Entity entity) {
		if (!original && player != null && entity instanceof LivingEntity living && !living.isSneaking() && !living.isInvisible()) {
			float distance = 16;
			if (entity.distanceTo(player) < distance && PassiveAbilityManager.isActive(player, ModPassiveAbilities.ELEMENTAL_AFFINITY) && !living.canSee(player))
				return true;
		}
		return original;
	}
}
