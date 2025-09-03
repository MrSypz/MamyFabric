package com.sypztep.mamy.common.system.classkill.acolyte;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.init.ModStatusEffects;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.CastableSkill;
import com.sypztep.mamy.common.system.skill.Skill;
import com.sypztep.mamy.common.util.SkillUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public class IncreaseAgilitySkill extends Skill implements CastableSkill {

    public IncreaseAgilitySkill(Identifier identifier, List<SkillRequirement> skillRequirements) {
        super(identifier, "Increase Agility", "Temporarily increases AGI, Attack Speed and Movement Speed of target.",
                18f, 1f,
                ModClasses.ACOLYTE, 1,
                1, 10,
                false, Mamy.id("skill/increase_agility"), skillRequirements);
    }

    @Override
    public float getResourceCost(int skillLevel) {
        return 15f + (skillLevel * 3f);
    }

    @Override
    public int getBaseVCT(int skillLevel) {
        return 16; // 0.8 seconds (16 ticks)
    }

    @Override
    public int getBaseFCT(int skillLevel) {
        return 4; // 0.2 seconds (4 ticks)
    }

    @Override
    public float getCooldown(int skillLevel) {
        return 1.0f; // 1 second cast delay
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

        // This is a buff skill, show the benefits
        data.baseDamage = 0;
        data.damageType = DamageType.HEAL; // Use heal type for beneficial effects
        data.maxHits = 1;

        return data;
    }

    @Override
    public boolean canUse(LivingEntity caster, int skillLevel) {
        if (!(caster instanceof PlayerEntity player)) return false;

        return player.isAlive();
    }

    @Override
    public boolean use(LivingEntity caster, int skillLevel) {
        if (!(caster instanceof PlayerEntity player)) return false;
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return false;

        // Find target using raycast (same as heal skill)
        LivingEntity target = SkillUtil.findTargetEntity(player,9);
        if (target == null) target = player;

        int duration = (40 + (skillLevel * 20)) * 20;

        // Amplifier = (skillLevel - 1) so level 1 = amplifier 0 = base effects
        StatusEffectInstance agilityBuff = new StatusEffectInstance(ModStatusEffects.INCREASE_AGILITY, duration, skillLevel - 1, false, false, false);
        target.addStatusEffect(agilityBuff);

        serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER, target.getX(), target.getY() + target.getHeight() / 2, target.getZ(), 12, 0.4, 0.4, 0.4, 0.1);
        serverWorld.spawnParticles(ParticleTypes.COMPOSTER, target.getX(), target.getY() + target.getHeight() / 2, target.getZ(), 8, 0.3, 0.3, 0.3, 0.05);
        serverWorld.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.6f, 1.5f);

        return true;
    }


    @Override
    public List<Text> generateTooltip(PlayerEntity player, int skillLevel, boolean isLearned, TooltipContext context) {
        List<Text> tooltip = super.generateTooltip(player, skillLevel, isLearned, context);

        // Add buff details to tooltip
        if (skillLevel > 0) {
            int agiBonus = 3 + (skillLevel - 1);
            int duration = 40 + (skillLevel * 20);

            tooltip.add(Text.literal(""));
            tooltip.add(Text.literal("Buff Effects:").formatted(Formatting.GOLD));
            tooltip.add(Text.literal("• AGI: ").formatted(Formatting.GRAY).append(Text.literal("+" + agiBonus).formatted(Formatting.GREEN)));
            tooltip.add(Text.literal("• Attack Speed: ").formatted(Formatting.GRAY).append(Text.literal("+" + skillLevel + "%").formatted(Formatting.GREEN)));
            tooltip.add(Text.literal("• Movement Speed: ").formatted(Formatting.GRAY).append(Text.literal("+5% per level").formatted(Formatting.GREEN)));
            tooltip.add(Text.literal("• Duration: ").formatted(Formatting.GRAY).append(Text.literal(duration + "s").formatted(Formatting.YELLOW)));

            tooltip.add(Text.literal(""));
            tooltip.add(Text.literal("HP Cost: ").formatted(Formatting.RED).append(Text.literal("15 HP").formatted(Formatting.DARK_RED)));
        }

        return tooltip;
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.ACOLYTE;
    }
}