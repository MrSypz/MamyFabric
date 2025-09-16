package com.sypztep.mamy.common.system.classkill.swordman;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.init.ModStatusEffects;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.Skill;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.particle.ParticleTypes;

import java.util.List;

public class EndureSkill extends Skill {

    public EndureSkill(Identifier identifier, List<SkillRequirement> skillRequirements) {
        super(identifier, "Endure", "Temporarily increase your armor for enhanced defense",
                10f, 10f,
                10,
                 Mamy.id("skill/endure"), skillRequirements);
    }

    @Override
    protected SkillTooltipData getSkillTooltipData(PlayerEntity player, int skillLevel) {
        SkillTooltipData data = new SkillTooltipData();

        // This is a buff skill, not a damage skill
        data.baseDamage = 0;
        data.damageType = DamageTypeRef.PHYSICAL;
        data.maxHits = 0;

        return data;
    }

    @Override
    public boolean canUse(LivingEntity caster, int skillLevel) {
        if (!(caster instanceof PlayerEntity player)) return false;
        return player.isAlive() && !player.hasStatusEffect(ModStatusEffects.ENDURE);
    }

    @Override
    public boolean use(LivingEntity caster, int skillLevel) {
        if (!(caster instanceof PlayerEntity player)) return false;
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return false;

        int duration = (10 + (3 * skillLevel)) * 20; // Convert seconds to ticks

        // Amplifier = skillLevel - 1 (so level 1 = amplifier 0 = +1 armor)
        StatusEffectInstance endureEffect = new StatusEffectInstance(
                ModStatusEffects.ENDURE,
                duration,
                skillLevel - 1, // Amplifier for armor bonus
                false, // Not ambient
                false,  // Show particles
                false   // Show icon
        );

        player.addStatusEffect(endureEffect);

        // Create protective aura particles
        for (int i = 0; i < 34; i++) {
            double angle = (Math.PI * 2 * i) / 20;
            double x = player.getX() + Math.cos(angle) * 1.5;
            double z = player.getZ() + Math.sin(angle) * 1.5;
            serverWorld.spawnParticles(ParticleTypes.ENCHANT,
                    x, player.getY() + 1, z,
                    1, 0.1, 0.1, 0.1, 0.02);
        }

        serverWorld.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS,
                0.8f, 1.5f);

        return true;
    }

    @Override
    public List<Text> generateTooltip(PlayerEntity player, int skillLevel, boolean isLearned, TooltipContext context) {
        List<Text> tooltip = super.generateTooltip(player, skillLevel, isLearned, context);

        if (skillLevel > 0 || context == TooltipContext.LEARNING_SCREEN) {
            tooltip.add(Text.literal("Endure Effects:").formatted(Formatting.YELLOW, Formatting.BOLD));
            int durationSeconds = 10 + (3 * skillLevel);

            tooltip.add(Text.literal("• Armor: +" + skillLevel).formatted(Formatting.GOLD));
            tooltip.add(Text.literal("• Duration: " + durationSeconds + " seconds").formatted(Formatting.GRAY));
            tooltip.add(Text.literal("• Cooldown: 10 seconds").formatted(Formatting.DARK_GRAY));

            if (skillLevel < getMaxSkillLevel()) {
                tooltip.add(Text.literal(""));
                tooltip.add(Text.literal("Next Level:").formatted(Formatting.DARK_GRAY));
                int nextArmorBonus = skillLevel + 1;
                int nextDurationSeconds = 10 + (3 * (skillLevel + 1));
                tooltip.add(Text.literal("• Armor: +" + nextArmorBonus).formatted(Formatting.GOLD));
                tooltip.add(Text.literal("• Duration: " + nextDurationSeconds + " seconds").formatted(Formatting.GRAY));
            }
        }
        return tooltip;
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.SWORDMAN;
    }
}
