package com.sypztep.mamy.mixin.vanilla.passive.windwalker;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.sypztep.mamy.common.component.living.AirHikeComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @ModifyArg(method = "applyMovementInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;updateVelocity(FLnet/minecraft/util/math/Vec3d;)V"))
    private float phantomWalker(float value) {
        if (!isOnGround()) {
            AirHikeComponent airhike = ModEntityComponents.AIRHIKE.getNullable(this);
            if ((LivingEntity) (Object) this instanceof PlayerEntity player && player.isCreative()) return value;
            if (airhike != null && airhike.isAirBorn()) return value * airhike.getAirControlMultiplier();
        }
        return value;
    }
    @WrapOperation(method = "computeFallDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getAttributeValue(Lnet/minecraft/registry/entry/RegistryEntry;)D", ordinal = 0))
    private double safeFall(LivingEntity instance, RegistryEntry<EntityAttribute> attribute, Operation<Double> original) {
        AirHikeComponent phantomWalker = ModEntityComponents.AIRHIKE.getNullable(this);
        double value = original.call(instance, attribute);

        if (phantomWalker != null) return value + (phantomWalker.getMaxJumps() - phantomWalker.getJumpsLeft()) + 16;

        return value;
    }
}
