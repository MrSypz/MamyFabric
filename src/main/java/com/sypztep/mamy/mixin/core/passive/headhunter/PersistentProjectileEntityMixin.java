package com.sypztep.mamy.mixin.core.passive.headhunter;

import com.sypztep.mamy.common.init.*;
import com.sypztep.mamy.common.system.passive.PassiveAbilityManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin extends ProjectileEntity {

    public PersistentProjectileEntityMixin(EntityType<? extends ProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = {"onEntityHit"}, at = {@At("HEAD")})
    protected void onEntityHit(EntityHitResult entityHitResult, CallbackInfo ci) {
        Entity hit = entityHitResult.getEntity();
        Entity owner = this.getOwner();

        if (!(hit instanceof LivingEntity livingTarget) || !(owner instanceof PlayerEntity shooter)) return;
        if (!PassiveAbilityManager.isActive(shooter, ModPassiveAbilities.HEADHUNTER)) return;
        if (!livingTarget.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) return;
        if (hit.getType().isIn(ModTags.EntityTypes.CANNOT_HEADSHOT)) return;

        float radius = 0.45F;
        if (hit instanceof GhastEntity) radius = 4.5f;
        else if (hit instanceof SquidEntity) radius = 1.5f;
        double y = this.getPos().getY();
        double eyeY = hit.getEyeY();

        if (y >= eyeY - radius && y <= eyeY + radius) {
            this.playSound(ModSoundEvents.ENTITY_GENERIC_HEADSHOT, 10.0F, 1.0F / (random.nextFloat() * 0.4F + 0.8F));
            ModEntityComponents.HEADSHOT.maybeGet(livingTarget).ifPresent(component -> component.setHeadShot(true));
        }
    }
}
