package com.sypztep.mamy.mixin.core.classes.theif.hiding;

import com.sypztep.mamy.common.init.ModEntityComponents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity {
    protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "tryAttack", at = @At("HEAD"), cancellable = true)
    public void prventMobFromAttack(Entity target, CallbackInfoReturnable<Boolean> cir) {
        if (target instanceof PlayerEntity)
            if (ModEntityComponents.HIDING.get(target).getHiddingPos() != null) cir.setReturnValue(false);
    }

    @ModifyVariable(method = "setTarget", at = @At("HEAD"), argsOnly = true)
    public LivingEntity setTarget(LivingEntity target) {
        if (target instanceof PlayerEntity)
            if (ModEntityComponents.HIDING.get(target).getHiddingPos() != null) return null;
        return target;
    }
}