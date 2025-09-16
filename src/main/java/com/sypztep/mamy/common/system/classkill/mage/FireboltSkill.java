package com.sypztep.mamy.common.system.classkill.mage;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.entity.skill.FireboltEntity;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.CastableSkill;
import com.sypztep.mamy.common.system.skill.Skill;
import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.classes.ResourceType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class FireboltSkill extends Skill implements CastableSkill {

    public FireboltSkill(Identifier id) {
        super(id, "Firebolt", "Fires a fast burning projectile that deals fire damage",
                10, 0.3f, 10, Mamy.id("skill/firebolt"));
    }

    @Override
    public int getBaseVCT(int skillLevel) {
        return switch (skillLevel) {
            case 1 -> (int) (0.64f * 20); // 12 ticks
            case 2 -> (int) (0.96f * 20); // 19 ticks
            case 3 -> (int) (1.28f * 20); // 25 ticks
            case 4 -> (int) (1.6f * 20);  // 32 ticks
            case 5 -> (int) (1.92f * 20); // 38 ticks
            case 6 -> (int) (2.1f * 20);  // 42 ticks
            case 7 -> (int) (1.56f * 20); // 31 ticks
            case 8 -> (int) (2.88f * 20); // 57 ticks
            case 9, 10 -> (int) (3.2f * 20);  // 64 ticks
            default -> (int) (0.64f * 20);
        };
    }

    @Override
    public int getBaseFCT(int skillLevel) {
        return switch (skillLevel) {
            case 1 -> (int) (0.16f * 20); // 3 ticks
            case 2 -> (int) (0.24f * 20); // 4 ticks
            case 3 -> (int) (0.32f * 20); // 6 ticks
            case 4 -> (int) (0.4f * 20);  // 8 ticks
            case 5 -> (int) (0.48f * 20); // 9 ticks
            case 6 -> (int) (0.7f * 20);  // 14 ticks
            case 7 -> (int) (0.64f * 20); // 12 ticks
            case 8 -> (int) (0.72f * 20); // 14 ticks
            case 9 -> (int) (0.8f * 20);  // 16 ticks
            case 10 -> (int) (1.2f * 20); // 24 ticks
            default -> (int) (0.16f * 20);
        };
    }

    @Override
    public boolean canBeInterupt() {
        return true;
    }

    @Override
    public boolean shouldLockMovement() {
        return true;
    }

    @Override
    public float getResourceCost(int skillLevel) {
        return 10 + (2 * skillLevel);
    }

    @Override
    public SoundContainer getCastCompleteSound() {
        return SoundContainer.of(SoundEvents.ITEM_FIRECHARGE_USE, 0.8f,1.2f);
    }

    @Override
    protected SkillTooltipData getSkillTooltipData(PlayerEntity player, int skillLevel) {
        SkillTooltipData data = new SkillTooltipData();

        data.baseDamage = (float) player.getAttributeValue(ModEntityAttributes.MAGIC_ATTACK_DAMAGE_FLAT) + 1 + skillLevel;
        data.damageType = DamageTypeRef.ELEMENT;
        data.rangeInfo = "Projectile";
        data.tip = "Fast burning projectile that travels in a straight line";

        return data;
    }

    @Override
    public List<Text> generateTooltip(PlayerEntity player, int skillLevel, boolean isLearned, TooltipContext context) {
        SkillTooltipData data = getSkillTooltipData(player, skillLevel);
        
        List<Text> tooltip = SkillTooltipRenderer.render(player, skillLevel, isLearned, context, name, description, maxSkillLevel, data);
        
        // Add resource and cooldown info manually
        if (skillLevel > 0 || context == TooltipContext.LEARNING_SCREEN) {
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
    public boolean canUse(LivingEntity caster, int skillLevel) {
        return caster instanceof PlayerEntity && caster.isAlive();
    }

    @Override
    public boolean use(LivingEntity caster, int skillLevel) {
        if (!(caster instanceof PlayerEntity player)) return false;
        if (!(caster.getWorld() instanceof ServerWorld world)) return false;

        float damage = (float) player.getAttributeValue(ModEntityAttributes.MAGIC_ATTACK_DAMAGE_FLAT) + 1 + skillLevel;

        // Create firebolt
        FireboltEntity firebolt = new FireboltEntity(world, player, damage, skillLevel);

        // Set starting position (slightly in front of player)
        Vec3d startPos = player.getEyePos().add(player.getRotationVec(1.0f).multiply(0.5));
        firebolt.setPosition(startPos.x, startPos.y, startPos.z);

        world.spawnEntity(firebolt);

        return true;
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.MAGE;
    }
}