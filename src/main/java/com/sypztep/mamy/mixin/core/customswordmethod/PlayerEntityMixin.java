package com.sypztep.mamy.mixin.core.customswordmethod;

import com.sypztep.mamy.common.api.entity.CustomHitParticleItem;
import com.sypztep.mamy.common.api.entity.CustomHitSoundItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity{

    @Shadow public abstract ItemStack getEquippedStack(EquipmentSlot slot);

    @Shadow public abstract float getAttackCooldownProgress(float baseTime);

    @Shadow public abstract boolean isPlayer();

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }
    @Inject(method = {"attack"},at = {@At(value = "INVOKE",target = "Lnet/minecraft/entity/player/PlayerEntity;getAttackCooldownProgress(F)F")})
    private void spawnCustomHitParticlesAndPlayCustomHitSound(Entity target, CallbackInfo ci) {
        if (this.getAttackCooldownProgress(0.5F) > 0.9F) {
            Item item = this.getMainHandStack().getItem();
            PlayerEntity player = PlayerEntity.class.cast(this);
            if (item instanceof CustomHitParticleItem customHitParticleItem) customHitParticleItem.spawnHitParticles(player);
            item = this.getMainHandStack().getItem();
            if (item instanceof CustomHitSoundItem customHitSoundItem) customHitSoundItem.playHitSound(player);
        }
    }

    @Inject(method = "spawnSweepAttackParticles",at = @At(value = "INVOKE",target = "Lnet/minecraft/server/world/ServerWorld;spawnParticles(Lnet/minecraft/particle/ParticleEffect;DDDIDDDD)I"), cancellable = true)
    private void disableSweepingattackParticle(CallbackInfo ci) {
        Item item = this.getMainHandStack().getItem();
        if (item instanceof CustomHitParticleItem) ci.cancel();
    }
}
