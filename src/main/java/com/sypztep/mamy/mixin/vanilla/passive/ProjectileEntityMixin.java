package com.sypztep.mamy.mixin.vanilla.passive;

import com.sypztep.mamy.common.init.ModDamageTypes;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.init.ModPassiveAbilities;
import com.sypztep.mamy.common.system.passive.PassiveAbilityManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ProjectileEntity.class)
public abstract class ProjectileEntityMixin extends Entity {
    @Shadow
    @Nullable
    public abstract Entity getOwner();

    public ProjectileEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(
            method = {"onEntityHit"},
            at = {@At("HEAD")}
    )
    protected void onEntityHit(EntityHitResult entityHitResult, CallbackInfo ci) {
        Entity hit = entityHitResult.getEntity();
        Entity owner = this.getOwner();

        if (!(hit instanceof LivingEntity livingTarget) || !(owner instanceof PlayerEntity shooter))return;
        if (!PassiveAbilityManager.isActive(shooter, ModPassiveAbilities.HEADHUNTER)) return;
        if (hit.getType().toString().contains("ender_dragon_part")) return;
        if (hit instanceof SlimeEntity) return;

        float radius = 0.5F;
        if (hit instanceof GhastEntity)
            radius = 7;
        else if (hit instanceof SquidEntity)
            radius = 1.5f;
        double y = this.getPos().getY();
        double eyeY = hit.getEyeY();

        if (y >= eyeY - radius && y <= eyeY + radius) {
            ModEntityComponents.HEADSHOT.maybeGet(livingTarget).ifPresent(component -> {
                component.setHeadShot(true);
                livingTarget.damage(
                        this.getDamageSources().create(ModDamageTypes.HEADSHOT),
                        livingTarget.getMaxHealth() * 5
                );
            });
        }
    }
}
