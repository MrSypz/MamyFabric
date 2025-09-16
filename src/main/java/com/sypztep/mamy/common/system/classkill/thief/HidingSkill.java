package com.sypztep.mamy.common.system.classkill.thief;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.component.living.LivingHidingComponent;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.init.ModStatusEffects;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.Skill;
import com.sypztep.mamy.common.util.LivingEntityUtil;
import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.system.classes.ResourceType;
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
        SkillTooltipData data = getSkillTooltipData(player, skillLevel);
        
        // Configure tooltip data
        if (skillLevel > 0) {
            int duration = skillLevel * 30;
            int drainInterval = 5 + skillLevel;
            
            data.effects.add("Duration: " + duration + " seconds");
            data.effects.add("Resource Drain: 1 SP every " + drainInterval + " seconds");
            data.effects.add("Toggles invisibility on/off");
            data.effects.add("Cannot move, attack, or use skills");
            data.effects.add("Invisible to players and monsters");
            data.effects.add("Stops targeted attacks");
        }
        data.tip = "Perfect for escaping dangerous situations";

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

            float cooldown = getCooldown(skillLevel);
            tooltip.add(Text.literal("Cooldown: ").formatted(Formatting.GRAY)
                    .append(Text.literal(String.format("%.1f", cooldown)).formatted(Formatting.YELLOW))
                    .append(Text.literal(" sec").formatted(Formatting.GRAY)));
        }

        // Context-specific info
        addContextInfo(tooltip, player, skillLevel, isLearned, context);

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