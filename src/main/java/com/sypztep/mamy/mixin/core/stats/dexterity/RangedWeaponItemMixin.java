package com.sypztep.mamy.mixin.core.stats.dexterity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.sypztep.mamy.ModConfig;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RangedWeaponItem.class)
public class RangedWeaponItemMixin {
    @WrapOperation(
            method = "shootAll",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/RangedWeaponItem;shoot(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/projectile/ProjectileEntity;IFFFLnet/minecraft/entity/LivingEntity;)V"))
    private void additionArrowDamage(
            RangedWeaponItem instance, LivingEntity shooter, ProjectileEntity projectile, int index, float speed, float divergence, float yaw, @Nullable LivingEntity target,
            Operation<Void> original) {
        float dexterity = 0f;
        float factor = 0;
        if (shooter instanceof PlayerEntity player) dexterity = MathHelper.clamp(ModEntityComponents.LIVINGLEVEL.get(player).getStatValue(StatTypes.DEXTERITY), 0, ModConfig.maxStatValue);

        float newDivergence = 10f * (1f - ((dexterity + factor) / ModConfig.maxStatValue));
        newDivergence = MathHelper.clamp(newDivergence, 0f, 10f);
        float newSpeed = (float) (speed + shooter.getAttributeValue(ModEntityAttributes.ARROW_SPEED));

        original.call(instance, shooter, projectile, index, newSpeed, newDivergence, yaw, target);

        if (projectile instanceof PersistentProjectileEntity projectileEntity) projectileEntity.setCritical(false); // Disable particle
    }
}
