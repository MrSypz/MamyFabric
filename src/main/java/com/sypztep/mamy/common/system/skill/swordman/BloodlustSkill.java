package com.sypztep.mamy.common.system.skill.swordman;

import com.sypztep.mamy.common.entity.BloodLustEntity;
import com.sypztep.mamy.common.system.classes.ClassRegistry;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.Skill;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;

import java.util.ArrayList;

public class BloodlustSkill extends Skill {

    public BloodlustSkill() {
        super("bloodlust", "Bloodlust", "Launch a blood projectile that transfers your status effects",
                30f, 60, 3, ClassRegistry.SWORDMAN, 10);
    }

    @Override
    public boolean canUse(LivingEntity caster) {
        return caster instanceof PlayerEntity && caster.getHealth() > 3.0f;
    }

    @Override
    public void use(LivingEntity caster, int level) {
        if (!(caster instanceof PlayerEntity player)) return;

        float f = 1.0F;
        if (!player.getWorld().isClient) {
            BloodLustEntity bloodLust = new BloodLustEntity(player.getWorld(), player);
            bloodLust.setOwner(player);
            bloodLust.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, f * 3.0F, 0.0F);
            bloodLust.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;

            // Transfer halved status effects - same as original
            ArrayList<StatusEffectInstance> statusEffectsHalved = new ArrayList<>();
            float absorption = player.getAbsorptionAmount();

            for (StatusEffectInstance statusEffectInstance : player.getStatusEffects()) {
                StatusEffectInstance statusHalved = new StatusEffectInstance(
                        statusEffectInstance.getEffectType(),
                        statusEffectInstance.getDuration() / 2,
                        statusEffectInstance.getAmplifier(),
                        statusEffectInstance.isAmbient(),
                        statusEffectInstance.shouldShowParticles(),
                        statusEffectInstance.shouldShowIcon()
                );
                bloodLust.addEffect(statusHalved); // Use addEffect method
                statusEffectsHalved.add(statusHalved);
            }

            player.clearStatusEffects();
            for (StatusEffectInstance statusEffectInstance : statusEffectsHalved) {
                player.addStatusEffect(statusEffectInstance);
            }

            player.setAbsorptionAmount(absorption);
            player.getWorld().spawnEntity(bloodLust);
        }

//        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
//                ModSoundEvents.ITEM_SPEWING, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ClassRegistry.SWORDMAN ||
                playerClass == ClassRegistry.KNIGHT ||
                playerClass == ClassRegistry.CRUSADER ||
                playerClass == ClassRegistry.LORD_KNIGHT ||
                playerClass == ClassRegistry.PALADIN;
    }
}