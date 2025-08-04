package com.sypztep.mamy.mixin.vanilla.passive;

import com.sypztep.mamy.common.component.living.HeadShotEntityComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.init.ModParticles;
import com.sypztep.mamy.common.init.ModSoundEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Shadow public abstract boolean addStatusEffect(StatusEffectInstance effect);

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Unique
    boolean onceDo = false;
    @Inject(
            method = {"tick"},
            at = {@At("TAIL")}
    )
    public void tick(CallbackInfo ci) {
        boolean isHeadExploding = ModEntityComponents.HEADSHOT.maybeGet(this)
                .map(HeadShotEntityComponent::isHeadShot)
                .orElse(false);
        if (isHeadExploding && !onceDo) {
            for (int i = 0; i < 500; ++i) {
                this.getWorld().addParticle(
                        ModParticles.BLOOD_BUBBLE_SPLATTER,
                        this.getX() ,
                        this.getEyeY() + this.random.nextGaussian() * 0.2,
                        this.getZ() ,
                        this.random.nextGaussian() * 0.3,     // motionX
                        this.random.nextFloat() * 0.6 + 0.1F, // motionY (upward splash)
                        this.random.nextGaussian() * 0.3      // motionZ
                );
            }
            this.playSound(ModSoundEvents.ENTITY_GENERIC_BLOODHIT, 5.0F, 1.0F);
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER,600,4,false,true,false));
            onceDo = true;
        }
    }
}
