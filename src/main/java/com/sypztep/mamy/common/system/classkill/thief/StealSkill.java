package com.sypztep.mamy.common.system.classkill.thief;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.init.ModTags;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.Skill;
import com.sypztep.mamy.common.system.stat.StatTypes;
import com.sypztep.mamy.common.util.SkillUtil;
import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.system.classes.ResourceType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class StealSkill extends Skill {

    public StealSkill(Identifier identifier) {
        super(identifier, "Steal", "Attempts to pinch an item from a targeted monster.",
                10f, 0f,
                10,
                Mamy.id("skill/steal"));
    }

    @Override
    public List<Text> generateTooltip(PlayerEntity player, int skillLevel, boolean isLearned, TooltipContext context) {
        SkillTooltipData data = getSkillTooltipData(player, skillLevel);
        
        // Configure tooltip data
        int baseSuccessRate = 10 + ((skillLevel - 1) * 6);
        
        data.effects.add("Base Success Rate: " + baseSuccessRate + "%");
        data.effects.add("Final Rate: Base + (Your DEX - Target DEX)/2");
        data.effects.add("Uses target's loot table");
        data.effects.add("Cannot steal from same target twice");
        data.effects.add("Cannot steal from boss monsters");
        data.effects.add("Doesn't affect death drops");
        data.rangeInfo = "Interaction range";
        data.tip = "Higher DEX improves success rate against fast enemies";

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
        data.damageType = DamageTypeRef.ELEMENT;
        data.maxHits = 1;

        return data;
    }

    @Override
    public boolean canUse(LivingEntity caster, int skillLevel) {
        if (!(caster instanceof PlayerEntity player)) return false;
        if (!player.isAlive()) return false;

        // Check if there's a valid target in range
        LivingEntity target = SkillUtil.findTargetEntity(player, player.getEntityInteractionRange());
        if (target == null) return false;

        // Cannot steal from players
        if (target instanceof PlayerEntity) return false;

        // Cannot steal from boss monsters
        if (target.getType().isIn(ModTags.EntityTypes.BOSSES)) return false;

        // Check if already stolen from this target
        return !ModEntityComponents.LIVINGSTEAL.get(target).hasBeenStolenFrom();
    }

    @Override
    public boolean use(LivingEntity caster, int skillLevel) {
        if (!(caster instanceof PlayerEntity player)) return false;
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return false;

        // Find target using player interaction range
        LivingEntity target = SkillUtil.findTargetEntity(player, player.getEntityInteractionRange());
        if (target == null) return false;

        // Validate target (same checks as canUse)
        if (target instanceof PlayerEntity) return false;
        if (target.getType().isIn(ModTags.EntityTypes.BOSSES)) return false;

        var stealComponent = ModEntityComponents.LIVINGSTEAL.get(target);
        if (stealComponent.hasBeenStolenFrom()) {
            // Already stolen from this target
            showFailureEffect(serverWorld, target, "already_stolen");
            return true;
        }

        // Calculate success rate
        int baseSuccessRate = 10 + ((skillLevel - 1) * 6); // 10% + 6% per level

        // Get DEX stats
        int userDex = ModEntityComponents.LIVINGLEVEL.get(player).getStatValue(StatTypes.DEXTERITY);
        int targetDex = 0;

        // Try to get target DEX if it has the component
        var targetLevelComponent = ModEntityComponents.LIVINGLEVEL.getNullable(target);
        if (targetLevelComponent != null) {
            targetDex = targetLevelComponent.getStatValue(StatTypes.DEXTERITY);
        }

        // Calculate final success rate: Base + (User_DEX - Target_DEX)/2
        int finalSuccessRate = baseSuccessRate + ((userDex - targetDex) / 2);
        finalSuccessRate = Math.max(1, Math.min(95, finalSuccessRate)); // Clamp between 1-95%

        boolean success = player.getRandom().nextFloat() < (finalSuccessRate * 0.01f);

        // Mark as stolen from regardless of success/failure
        stealComponent.markAsStolen();

        if (success) {
            // Generate loot from target's loot table
            RegistryKey<LootTable> lootTableId = target.getLootTable();
            LootTable lootTable = serverWorld.getServer().getReloadableRegistries().getLootTable(lootTableId);

            if (lootTable != null) {
                // Create loot context+
                LootContextParameterSet lootContextParameterSet = new LootContextParameterSet.Builder(serverWorld).
                        add(LootContextParameters.ORIGIN, target.getPos()).
                        add(LootContextParameters.DAMAGE_SOURCE, player.getDamageSources().playerAttack(player))
                        .add(LootContextParameters.THIS_ENTITY, target)
                        .addOptional(LootContextParameters.ATTACKING_ENTITY, player)
                        .addOptional(LootContextParameters.DIRECT_ATTACKING_ENTITY, player)
                        .build(LootContextTypes.ENTITY);

                // Generate items
                List<ItemStack> loot = lootTable.generateLoot(lootContextParameterSet);
                if (!loot.isEmpty()) {
                    ItemStack stolenItem = loot.get(player.getRandom().nextInt(loot.size()));

                    if (!stolenItem.isEmpty()) {
                        player.giveItemStack(stolenItem);

                        showSuccessEffect(serverWorld, target);
                        player.sendMessage(Text.literal("Stole Success! ").formatted(Formatting.GREEN), true);

                        return true;
                    }
                }
            }

            // Success but no items generated
            showSuccessEffect(serverWorld, target);
            player.sendMessage(Text.literal("Nothing to steal!").formatted(Formatting.YELLOW), true);
        } else {
            showFailureEffect(serverWorld, target, "failed");
        }

        return true;
    }

    private void showSuccessEffect(ServerWorld world, LivingEntity target) {
        Vec3d pos = target.getPos().add(0, target.getHeight() / 2, 0);

        world.spawnParticles(ParticleTypes.HAPPY_VILLAGER, pos.x, pos.y, pos.z, 8, 0.3, 0.3, 0.3, 0.1);

        world.spawnParticles(ParticleTypes.ENCHANT, pos.x, pos.y, pos.z, 5, 0.2, 0.2, 0.2, 0.05);

        world.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1.0f, 1.5f);
    }

    private void showFailureEffect(ServerWorld world, LivingEntity target, String reason) {
        Vec3d pos = target.getPos().add(0, target.getHeight() / 2, 0);

        world.spawnParticles(ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 5, 0.2, 0.2, 0.2, 0.05);

        world.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 0.5f, 0.8f);
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.THIEF;
    }
}