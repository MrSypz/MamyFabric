package com.sypztep.mamy.common.system.skill.swordman;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.entity.BloodLustEntity;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.Skill;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public class BloodlustSkill extends Skill {

    public BloodlustSkill() {
        super(Mamy.id("bloodlust"), "Bloodlust", "Launch a blood projectile",
                30f, 1, 3, ModClasses.SWORDMAN, 2,Mamy.id("skill/scythecooldown"));
    }

    @Override
    public boolean canUse(LivingEntity caster) {
        return caster instanceof PlayerEntity && caster.getHealth() > 3.0f;
    }

    @Override
    public void use(LivingEntity caster, int level) {
        if (!(caster instanceof PlayerEntity player)) return;

        if (!player.getWorld().isClient) {
            BloodLustEntity bloodLust = new BloodLustEntity(player.getWorld(), player);
            bloodLust.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, 0.7F, 0.0F);
            player.getWorld().spawnEntity(bloodLust);
        }
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.SWORDMAN;
    }
}