package com.sypztep.mamy.common.system.skill.swordman;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.entity.entity.skill.BloodLustEntity;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.Skill;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import com.sypztep.mamy.common.init.ModDamageTypes;
import com.sypztep.mamy.common.system.skill.config.SkillConfig;
import net.minecraft.util.Identifier;
@SuppressWarnings("DemoSkill")
public class BloodlustSkill extends Skill {

    public BloodlustSkill(Identifier identifier) {
        super(identifier, "Bloodlust", "Launch a blood projectile",
                30f, 1, ModClasses.SWORDMAN, 0, 1, 5, false, Mamy.id("skill/bloodlust"));
    }

    @Override
    protected SkillTooltipData getSkillTooltipData(PlayerEntity player, int skillLevel) {
        SkillTooltipData data = new SkillTooltipData();

        // Base damage + scaling with skill level
        data.baseDamage = 4.0f + (skillLevel * 2.0f) + (float) player.getAttributeValue(ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT);
        data.damageType = DamageType.PHYSICAL;
        data.maxHits = 5;

        // Add life steal effect
        data.healthPerHit = 0.5f;

        return data;
    }

    @Override
    public boolean canUse(LivingEntity caster, int skillLevel) {
        return caster instanceof PlayerEntity && caster.isAlive();
    }

    @Override
    public boolean use(LivingEntity caster, int level) {
        if (!(caster instanceof PlayerEntity player)) return false;

        if (!player.getWorld().isClient) {
            SkillConfig config = createBloodlustConfig(level, caster);

            BloodLustEntity bloodLust = new BloodLustEntity(player.getWorld(), player, config);
            bloodLust.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, 0.7F, 0.0F);
            player.getWorld().spawnEntity(bloodLust);
        }
        return true;
    }

    private SkillConfig createBloodlustConfig(int skillLevel, LivingEntity caster) {
        float totalDamage = 4.0f + (skillLevel * 2.0f) + (float) caster.getAttributeValue(ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT);
        int maxHitCount = Math.min(5 + skillLevel, 10);

        return new SkillConfig.Builder()
                .damage(totalDamage)
                .damageType(ModDamageTypes.BLOODLUST)
                .slashHitBox(5, 0.2f)
                .maxHitCount(maxHitCount)
                .iframeTime(2)
                .build();
    }

    @Override
    public float getResourceCost(int skillLevel) {
        return this.baseResourceCost * (1.0f + 0.2f * (skillLevel - 1));
    }

    @Override
    public float getCooldown(int skillLevel) {
        return this.baseCooldown * (1.0f + 0.4f * (skillLevel - 1));
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.SWORDMAN;
    }
}