package com.sypztep.mamy.mixin.core.passive.intelligence.waterwalk;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.init.ModPassiveAbilities;
import com.sypztep.mamy.common.system.passive.PassiveAbilityManager;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntityMixin {
	protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Override
	protected boolean canWalkonFluidThing(boolean original) {
		return original || (!this.isSneaking() &&
				this.isTouchingWater() &&
				!this.isSubmergedInWater() && // Keep this - only walk on SURFACE
				PassiveAbilityManager.isActive((PlayerEntity)(Object)this, ModPassiveAbilities.ARCHMAGE_POWER));
	}

	@ModifyArg(method = "updatePose", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setPose(Lnet/minecraft/entity/EntityPose;)V"))
	private EntityPose forceStandPos(EntityPose value) {
		if (value == EntityPose.SWIMMING &&
				isTouchingWater() &&
				!this.isSubmergedInWater() && // Add surface check here too
				PassiveAbilityManager.isActive((PlayerEntity)(Object)this, ModPassiveAbilities.ARCHMAGE_POWER)) {

			return EntityPose.STANDING;
		}
		return value;
	}
	/**
	 * Override noclip to include wall phasing
	 */
	@ModifyExpressionValue(
			method = "tick",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isSpectator()Z")
	)
	private boolean includeWallPhasingInNoClip(boolean isSpectator) {
		PlayerEntity player = (PlayerEntity)(Object)this;
		var phasingComponent = ModEntityComponents.WALLPHASING.get(player);

		return isSpectator || phasingComponent.isPhasing();
	}

	/**
	 * Override player velocity during wall phasing
	 */
	@Inject(method = "tickMovement", at = @At("HEAD"))
	private void overrideMovementDuringPhasing(CallbackInfo ci) {
		PlayerEntity player = (PlayerEntity)(Object)this;
		var phasingComponent = ModEntityComponents.WALLPHASING.get(player);

		if (phasingComponent.isPhasing()) {
			// Calculate movement towards target
			Vec3d targetPos = phasingComponent.getPhasingTargetPos();
			if (targetPos != null) {
				Vec3d currentPos = player.getPos();
				Vec3d direction = targetPos.subtract(currentPos).normalize();
				double speed = 0.3; // Movement speed during phasing

				// Set velocity towards target
				Vec3d phasingVelocity = direction.multiply(speed);
				player.setVelocity(phasingVelocity);

				// Check if reached target
				if (currentPos.distanceTo(targetPos) < 0.2) {
					phasingComponent.completePhasingMovement();
				}
			}
		}
	}
}
