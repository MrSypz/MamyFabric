package com.sypztep.mamy.common.system.classkill.acolyte;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.CastableSkill;
import com.sypztep.mamy.common.system.skill.Skill;
import com.sypztep.mamy.common.util.SkillUtil;
import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.classes.ResourceType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public class CureSkill extends Skill implements CastableSkill {

    public CureSkill(Identifier identifier, List<SkillRequirement> skillRequirements) {
        super(identifier, "Cure", "Removes poison, blindness, confusion and other negative status effects from target.",
                15f, 6f,
                1, // Max level 1
                Mamy.id("skill/cure"), skillRequirements);
    }

    @Override
    public int getBaseVCT(int skillLevel) {
        return 20; // 1 second (20 ticks)
    }

    @Override
    public int getBaseFCT(int skillLevel) {
        return 0; // No fixed cast time
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
    public boolean canUse(LivingEntity caster, int skillLevel) {
        return caster instanceof PlayerEntity && caster.isAlive();
    }

    @Override
    public boolean use(LivingEntity caster, int skillLevel) {
        if (!(caster instanceof PlayerEntity player)) return false;
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return false;

        // Find target using raycast (same targeting as heal)
        LivingEntity target = SkillUtil.findTargetEntity(player, 9);
        if (target == null) target = player;

        // List of negative status effects to remove
        List<RegistryEntry<StatusEffect>> negativeEffects = List.of(
                StatusEffects.POISON,
                StatusEffects.WITHER,
                StatusEffects.BLINDNESS,
                StatusEffects.NAUSEA
        );

        boolean removedAny = false;

        // Remove all negative status effects
        for (RegistryEntry<StatusEffect> effect : negativeEffects) {
            if (target.hasStatusEffect(effect)) {
                target.removeStatusEffect(effect);
                removedAny = true;
            }
        }

        if (removedAny) {
            serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER,
                    target.getX(), target.getY() + target.getHeight() / 2, target.getZ(),
                    12, 0.5, 0.5, 0.5, 0.1);
            serverWorld.spawnParticles(ParticleTypes.END_ROD,
                    target.getX(), target.getY() + target.getHeight() / 2, target.getZ(),
                    6, 0.3, 0.3, 0.3, 0.05);

            serverWorld.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 0.5f, 1.5f);
        } else {
            serverWorld.spawnParticles(ParticleTypes.POOF,
                    target.getX(), target.getY() + target.getHeight() / 2, target.getZ(),
                    5, 0.2, 0.2, 0.2, 0.05);

            // Neutral sound
            serverWorld.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.PLAYERS, 0.3f, 1.2f);
        }

        return true;
    }

    @Override
    public List<Text> generateTooltip(PlayerEntity player, int skillLevel, boolean isLearned, TooltipContext context) {
        SkillTooltipData data = getSkillTooltipData(player, skillLevel);
        
        // Configure tooltip data
        data.effects.add("Removes Poison & Wither");
        data.effects.add("Removes Blindness");
        data.effects.add("Removes Nausea");
        data.rangeInfo = "9 blocks";
        data.cooldownOverride = "6 seconds";
        data.tip = "Essential support skill for removing debuffs";

        List<Text> tooltip = SkillTooltipRenderer.render(player, skillLevel, isLearned, context, name, description, maxSkillLevel, data);
        
        // Add resource info manually
        if (!data.hideResourceCost && (skillLevel > 0 || context == TooltipContext.LEARNING_SCREEN)) {
            PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);
            ResourceType resourceType = classComponent.getClassManager().getResourceType();

            float cost = getResourceCost(skillLevel);
            tooltip.add(Text.literal(""));
            tooltip.add(Text.literal("Require Resource: ").formatted(Formatting.GRAY)
                    .append(Text.literal(String.format("%.0f", cost)).formatted(Formatting.YELLOW))
                    .append(Text.literal(" " + resourceType.getDisplayName()).formatted(Formatting.GRAY)));
        }

        // Context-specific info
        addContextInfo(tooltip, player, skillLevel, isLearned, context);

        return tooltip;
    }
    @Override
    protected SkillTooltipData getSkillTooltipData(PlayerEntity player, int skillLevel) {
        SkillTooltipData data = new SkillTooltipData();

        data.baseDamage = 0;
        data.damageType = DamageTypeRef.HEAL;
        data.maxHits = 1;

        return data;
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.ACOLYTE;
    }
}