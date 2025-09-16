package com.sypztep.mamy.common.system.classkill.acolyte;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.init.ModStatusEffects;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.Skill;
import com.sypztep.mamy.common.util.SkillUtil;
import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.classes.ResourceType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public class BlessingSkill extends Skill {

    public BlessingSkill(Identifier identifier, List<SkillRequirement> skillRequirements) {
        super(identifier, "Blessing",
                "Places a temporary buff on target that increases STR, DEX, INT, and HIT. Purges curse and stone effects. Against undead/demons, reduces their DEX and INT instead.",
                24f, 0f,
                10,
                Mamy.id("skill/blessing"),skillRequirements);
    }

    @Override
    public float getResourceCost(int skillLevel) {
        return super.getResourceCost(skillLevel) + (skillLevel * 4f);
    }

    @Override
    protected SkillTooltipData getSkillTooltipData(PlayerEntity player, int skillLevel) {
        SkillTooltipData data = new SkillTooltipData();

        // This is a buff skill
        data.baseDamage = 0;
        data.damageType = DamageTypeRef.HEAL; // Use heal type for beneficial effects
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

        // Find target using the new SkillUtil helper
        LivingEntity target = SkillUtil.findTargetEntity(player, 9.0);
        if (target == null) target = player; // Self-cast if no target found

        // Purge curse and stone effects (2nd stage only means skill level >= 2)
        if (skillLevel >= 2) {
            purgeCurseAndStone(target);
        }

        // Calculate duration: 40 + (skillLevel * 20) seconds, converted to ticks
        int duration = (40 + (skillLevel * 20)) * 20;

        // Amplifier = (skillLevel - 1) so level 1 = amplifier 0
        StatusEffectInstance blessing = new StatusEffectInstance(ModStatusEffects.BLESSING, duration, skillLevel - 1, false, false, false);

        boolean success = target.addStatusEffect(blessing);

        if (success) {
            boolean isUndeadOrDemon = target.getType().isIn(EntityTypeTags.UNDEAD) || isDemon(target);

            if (isUndeadOrDemon) {
                serverWorld.spawnParticles(ParticleTypes.SMOKE, target.getX(), target.getY() + target.getHeight() / 2, target.getZ(), 15, 0.5, 0.5, 0.5, 0.1);
                serverWorld.spawnParticles(ParticleTypes.SOUL, target.getX(), target.getY() + target.getHeight() / 2, target.getZ(), 8, 0.3, 0.3, 0.3, 0.05);
                serverWorld.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ENTITY_WITCH_HURT, SoundCategory.PLAYERS, 0.8f, 1.2f);
            } else {
                serverWorld.spawnParticles(ParticleTypes.END_ROD, target.getX(), target.getY() + target.getHeight() / 2, target.getZ(), 15, 0.5, 0.5, 0.5, 0.1);
                serverWorld.spawnParticles(ParticleTypes.ENCHANT, target.getX(), target.getY() + target.getHeight() / 2, target.getZ(), 12, 0.4, 0.4, 0.4, 0.1);
                serverWorld.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 0.8f, 1.5f);
            }
        }

        return success;
    }

    private void purgeCurseAndStone(LivingEntity target) {
        target.removeStatusEffect(StatusEffects.SLOWNESS);
        target.removeStatusEffect(StatusEffects.WEAKNESS);
        target.removeStatusEffect(StatusEffects.MINING_FATIGUE);
    }

    private boolean isDemon(LivingEntity entity) {
        String entityName = entity.getType().toString().toLowerCase();
        return entityName.contains("wither") || entityName.contains("blaze") || entityName.contains("ghast") || entityName.contains("demon") || entityName.contains("devil"); // Add more demon types as needed
    }

    @Override
    public List<Text> generateTooltip(PlayerEntity player, int skillLevel, boolean isLearned, TooltipContext context) {
        SkillTooltipData data = getSkillTooltipData(player, skillLevel);
        
        // Configure tooltip data
        if (skillLevel > 0) {
            int hitIncrease = skillLevel * 2;
            int duration = 40 + (skillLevel * 20);
            
            data.effects.add("STR: +" + skillLevel);
            data.effects.add("DEX: +" + skillLevel);
            data.effects.add("INT: +" + skillLevel);
            data.effects.add("HIT: +" + hitIncrease);
            data.effects.add("Duration: " + duration + "s");
            data.effects.add("vs Undead/Demons: Reduces DEX & INT");
        }
        data.rangeInfo = "9 blocks";
        data.tip = "Powerful buff for allies, debuff for undead and demons";

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
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.ACOLYTE;
    }
}