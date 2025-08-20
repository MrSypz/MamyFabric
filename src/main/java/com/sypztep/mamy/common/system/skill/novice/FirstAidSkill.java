package com.sypztep.mamy.common.system.skill.novice;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.CastableSkill;
import com.sypztep.mamy.common.system.skill.Skill;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public class FirstAidSkill extends Skill implements CastableSkill {

    public FirstAidSkill(Identifier identifier) {
        super(identifier, "First Aid", "Heal yourself and nearby allies",
                10f, 3f, ModClasses.NOVICE, 0, 1, 10, true, Mamy.id("skill/first_aid"));
    }

    @Override
    public int getBaseVCT(int skillLevel) {
        return 30; // Minimum 1 second VCT
    }

    @Override
    public int getBaseFCT(int skillLevel) {
        return 10;
    }

    @Override
    public boolean shouldLockMovement() {
        return true;
    }

    @Override
    public Identifier getCastAnimation() {
        return Mamy.id("pray");
    }

    @Override
    protected SkillTooltipData getSkillTooltipData(PlayerEntity player, int skillLevel) {
        SkillTooltipData data = new SkillTooltipData();
        data.damagePercentage = 0.3f + (skillLevel * 0.1f);
        data.baseDamage = 2.0f + (skillLevel * 1.5f);
        data.damageType = DamageType.HEAL;
        data.maxHits = 1;

        return data;
    }

    @Override
    public boolean canUse(LivingEntity caster, int skillLevel) {
        return caster instanceof PlayerEntity && caster.isAlive();
    }

    @Override
    public boolean use(LivingEntity caster, int skillLevel) {
        if (!(caster instanceof PlayerEntity player)) return false;

        double magicAttackDamage = player.getAttributeValue(ModEntityAttributes.MAGIC_ATTACK_DAMAGE_FLAT);
        double healingPercentage = (30 + (skillLevel * 10)) * 0.01;
        float baseHealing = 2.0f + (skillLevel * 1.5f);
        float totalHealing = (float)(magicAttackDamage * healingPercentage) + baseHealing;
        double range = 2.0 + (skillLevel * 0.5);

        player.heal(totalHealing);

        var nearbyAllies = player.getWorld().getEntitiesByClass(PlayerEntity.class,
                player.getBoundingBox().expand(range),
                entity -> entity != player && entity.isAlive() && entity.isTeammate(player));

        for (PlayerEntity ally : nearbyAllies) {
            ally.heal(totalHealing);
        }

        if (player.getWorld() instanceof ServerWorld serverWorld) {
            int particleCount = 5 + (skillLevel * 2);
            serverWorld.spawnParticles(ParticleTypes.HEART,
                    player.getX(), player.getY() + 1, player.getZ(),
                    particleCount, 0.5, 0.5, 0.5, 0.1);
        }

        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS,
                0.3f + (skillLevel * 0.1f), 1.2f + (skillLevel * 0.1f));
        return true;
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return true;
    }
}