package com.sypztep.mamy.mixin.core.skillcast.client;

import com.sypztep.mamy.common.system.skill.SkillCastingManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "setHealth", at = @At("HEAD"))
    private void onHealthSet(float health, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (!(entity instanceof ClientPlayerEntity)) return;

        float currentHealth = entity.getHealth();

        if (health < currentHealth) {
            float damage = currentHealth - health;

            if (damage >= 0.5f) {
                SkillCastingManager castingManager = SkillCastingManager.getInstance();
                if (castingManager.canBeInterupt() && castingManager.isCasting()) {
                    castingManager.interruptCast();
                }
            }
        }
    }
}