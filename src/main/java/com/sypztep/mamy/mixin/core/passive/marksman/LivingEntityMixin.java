package com.sypztep.mamy.mixin.core.passive.marksman;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.sypztep.mamy.common.init.ModPassiveAbilities;
import com.sypztep.mamy.common.system.passive.PassiveAbilityManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @ModifyExpressionValue(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/damage/DamageSource;isIn(Lnet/minecraft/registry/tag/TagKey;)Z", ordinal = 4))
    private boolean projectileNoIframe(boolean value, DamageSource source) {
        if (source.getSource() instanceof ProjectileEntity projectile)
            if (projectile.getOwner() instanceof PlayerEntity player && PassiveAbilityManager.isActive(player, ModPassiveAbilities.MARKS_MAN))
                return true;
        return value;
    }
}