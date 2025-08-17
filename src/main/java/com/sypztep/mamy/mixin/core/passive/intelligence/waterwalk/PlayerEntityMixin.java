package com.sypztep.mamy.mixin.core.passive.intelligence.waterwalk;

import com.sypztep.mamy.common.init.ModPassiveAbilities;
import com.sypztep.mamy.common.system.passive.PassiveAbilityManager;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntityMixin {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected boolean canWalkonFluidThing(boolean original) {
        return original || (!this.isSneaking() && this.isTouchingWater() && !this.isSubmergedInWater() && // Keep this - only walk on SURFACE
                PassiveAbilityManager.isActive((PlayerEntity) (Object) this, ModPassiveAbilities.ARCHMAGE_POWER));
    }

    @ModifyArg(method = "updatePose", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setPose(Lnet/minecraft/entity/EntityPose;)V"))
    private EntityPose forceStandPos(EntityPose value) {
        if (value == EntityPose.SWIMMING && isTouchingWater() && !this.isSubmergedInWater() && // Add surface check here too
                PassiveAbilityManager.isActive((PlayerEntity) (Object) this, ModPassiveAbilities.ARCHMAGE_POWER)) {

            return EntityPose.STANDING;
        }
        return value;
    }
}
