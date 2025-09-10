package com.sypztep.mamy.mixin.core.stats.vitality;

import com.sypztep.mamy.common.init.ModEntityAttributes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow public abstract double getAttributeValue(RegistryEntry<EntityAttribute> attribute);

    @Shadow public abstract void heal(float amount);

    @Inject(method = "tickMovement", at = @At("TAIL"))
    private void natureHealthRegen(CallbackInfo ci) {
        float natureHealthRegen = (float) this.getAttributeValue(ModEntityAttributes.HEALTH_REGEN);
        if (natureHealthRegen > 0.00f && this.age % 120 == 0)
            this.heal(natureHealthRegen);


        if ((LivingEntity) (Object) this instanceof PlayerEntity player) {
            float passiveRegen = (float) this.getAttributeValue(ModEntityAttributes.PASSIVE_HEALTH_REGEN);
            boolean isIdle = isPlayerIdle(player);
            if (passiveRegen > 0.00f && isIdle && this.age % 200 == 0)  // 200 ticks = 10 seconds
                this.heal(passiveRegen);
        }
    }

    @Unique
    private boolean isPlayerIdle(PlayerEntity player) {
        return player.getVelocity().lengthSquared() < 0.01; // Very small movement threshold
    }
}
