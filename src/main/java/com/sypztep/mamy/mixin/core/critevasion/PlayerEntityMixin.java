package com.sypztep.mamy.mixin.core.critevasion;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.sypztep.mamy.client.util.ParticleHandler;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.init.ModCustomParticles;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntityMixin {
    public PlayerEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }
    @Unique
    protected LivingEntity player = (PlayerEntity) (Object) this;

    @ModifyVariable(method = "attack", at = @At("STORE"), ordinal = 2)
    private boolean modifyAttackCondition(boolean bl3, Entity target) {
        if (!this.getWorld().isClient()) {
            if (this.isCrit) ParticleHandler.sendToAll(target, player, ModCustomParticles.CRITICAL);
            return isCrit;
        }
        return false;
    }

    @ModifyExpressionValue(method = "attack", at = @At(value = "CONSTANT", args = "floatValue=1.5"))
    private float modifyCritDamage(float f) {
        return 1 + (float) player.getAttributeValue(ModEntityAttributes.CRIT_DAMAGE);
    }
}
