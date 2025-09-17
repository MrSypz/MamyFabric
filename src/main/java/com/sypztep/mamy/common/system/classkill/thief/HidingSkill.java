package com.sypztep.mamy.common.system.classkill.thief;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.component.living.LivingHidingComponent;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.init.ModStatusEffects;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.Skill;
import com.sypztep.mamy.common.util.LivingEntityUtil;
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

public class HidingSkill extends Skill {

    public HidingSkill(Identifier identifier, List<SkillRequirement> skillRequirements) {
        super(identifier, "Hiding", "Toggles the hide effect on the character on/off. Hidden characters cannot move, attack or use any skill.",
                10f, 1.5f,
                10,
                Mamy.id("skill/hiding"), skillRequirements);
    }

    @Override
    public List<Text> generateTooltip(PlayerEntity player, int skillLevel, boolean isLearned, TooltipContext context) {
        List<Text> tooltip = super.generateTooltip(player, skillLevel, isLearned, context);

        if (skillLevel > 0 || context == TooltipContext.LEARNING_SCREEN) {
            tooltip.add(Text.literal(""));

            // Duration info
            int duration = skillLevel * 30;
            tooltip.add(Text.literal("Duration: ").formatted(Formatting.GRAY).append(Text.literal(duration + " seconds").formatted(Formatting.YELLOW)));

            // Resource drain
            int drainInterval = 5 + skillLevel;
            tooltip.add(Text.literal("Resource Drain: ").formatted(Formatting.GRAY).append(Text.literal("1 SP every " + drainInterval + " seconds").formatted(Formatting.RED)));

            tooltip.add(Text.literal(""));
            tooltip.add(Text.literal("• Toggles invisibility on/off").formatted(Formatting.DARK_GREEN));
            tooltip.add(Text.literal("• Cannot move, attack, or use skills").formatted(Formatting.DARK_RED));
            tooltip.add(Text.literal("• Invisible to players and monsters").formatted(Formatting.DARK_GREEN));
            tooltip.add(Text.literal("• Stops targeted attacks").formatted(Formatting.DARK_GREEN));

            if (context == TooltipContext.LEARNING_SCREEN) {
                tooltip.add(Text.literal(""));
                tooltip.add(Text.literal("💡 Tip: ").formatted(Formatting.YELLOW).append(Text.literal("Perfect for escaping dangerous situations").formatted(Formatting.GRAY)));
            }
        }

        return tooltip;
    }

    @Override
    protected SkillTooltipData getSkillTooltipData(PlayerEntity player, int skillLevel) {
        SkillTooltipData data = new SkillTooltipData();

        data.baseDamage = 0;
        data.damageType = DamageTypeRef.MAGIC;
        data.maxHits = 1;

        return data;
    }

    @Override
    public boolean canUse(LivingEntity caster, int skillLevel) {
        if (!(caster instanceof PlayerEntity player)) return false;
        if (!player.isAlive()) return false;

        LivingHidingComponent buryComponent = ModEntityComponents.HIDING.get(player);
        if (buryComponent.getHiddingPos() != null) return true;

        return LivingEntityUtil.isValidGround(caster);
    }

    @Override
    public boolean use(LivingEntity caster, int skillLevel) {
        if (!(caster instanceof PlayerEntity player)) return false;
        LivingHidingComponent buryComponent = ModEntityComponents.HIDING.get(player);
        if (!player.getWorld().isClient() && player.getWorld() instanceof ServerWorld serverWorld) {
            if (buryComponent.getHiddingPos() != null) {
                player.removeStatusEffect(ModStatusEffects.HIDING);

                serverWorld.spawnParticles(ParticleTypes.POOF, player.getX(), player.getY() + 1, player.getZ(), 8, 0.3, 0.3, 0.3, 0.1);

                serverWorld.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 0.5f, 1.2f);

                player.sendMessage(Text.literal("You emerge from hiding").formatted(Formatting.GRAY), true);

            } else {
                buryComponent.setHiddingPos(player.getBlockPos().down(1));
                buryComponent.sync();

                int duration = (skillLevel * 30) * 20;

                StatusEffectInstance hidingEffect = new StatusEffectInstance(ModStatusEffects.HIDING, duration, skillLevel - 1, false, false, false);
                player.addStatusEffect(hidingEffect);

                serverWorld.spawnParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 1, player.getZ(), 10, 0.3, 0.3, 0.3, 0.1);

                serverWorld.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 0.5f, 0.8f);

                player.sendMessage(Text.literal("You hide in the shadows").formatted(Formatting.DARK_GRAY), true);
            }
        }
        return true;
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.THIEF;
    }
}