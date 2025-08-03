package com.sypztep.mamy.common.system.skill.swordman;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.entity.BloodLustEntity;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.Skill;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import com.sypztep.mamy.common.init.ModDamageTypes;
import com.sypztep.mamy.common.system.skill.config.SkillConfig;
import net.minecraft.util.Identifier;

public class BloodlustSkill extends Skill {

    public BloodlustSkill(Identifier identifier) {
        super(identifier, "Bloodlust", "Launch a blood projectile",
                30f, 1, ModClasses.SWORDMAN, 0, 1, 5, false, Mamy.id("skill/bloodlust"));
    }

    @Override
    protected SkillTooltipData getSkillTooltipData(PlayerEntity player, int skillLevel) {
        SkillTooltipData data = new SkillTooltipData();

        // Base damage + scaling with skill level
        data.baseDamage = 4.0f + (skillLevel * 2.0f);
        data.damageType = DamageType.PHYSICAL;
        data.maxHits = Math.min(5 + skillLevel, 10);

        // Add life steal effect
        data.healthPerHit = 0.5f;

        return data;
    }

    @Override
    public boolean canUse(LivingEntity caster, int skillLevel) {
        return caster instanceof PlayerEntity && caster.isAlive();
    }

    @Override
    public void use(LivingEntity caster, int level) {
        if (!(caster instanceof PlayerEntity player)) return;

        if (!player.getWorld().isClient) {
            SkillConfig config = createBloodlustConfig(level);

            BloodLustEntity bloodLust = new BloodLustEntity(player.getWorld(), player, config);
            bloodLust.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, 0.7F, 0.0F);
            player.getWorld().spawnEntity(bloodLust);
        }
    }

    private SkillConfig createBloodlustConfig(int skillLevel) {
        float totalDamage = 4.0f + (skillLevel * 2.0f);
        int maxHitCount = Math.min(5 + skillLevel, 10);
        float slashRange = 5.0f + (skillLevel * 0.5f);

        return new SkillConfig.Builder()
                .damage(totalDamage)
                .damageType(ModDamageTypes.BLOODLUST)
                .slashHitBox(slashRange, 0.2f)
                .maxHitCount(maxHitCount)
                .iframeTime(2)
                .build();
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.SWORDMAN;
    }
}