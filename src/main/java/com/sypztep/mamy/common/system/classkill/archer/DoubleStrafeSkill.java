package com.sypztep.mamy.common.system.classkill.archer;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.entity.skill.DoubleStrafeEntity;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.CastableSkill;
import com.sypztep.mamy.common.system.skill.Skill;
import com.sypztep.mamy.common.system.skill.SkillRegistry;
import com.sypztep.mamy.common.util.SkillUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DoubleStrafeSkill extends Skill implements CastableSkill {
    private static final float BASE_DAMAGE_MULTIPLIER = 1.0f; // 100% at level 1
    private static final float DAMAGE_MULTIPLIER_PER_LEVEL = 0.10f; // +10% per level
    private static final float BASE_DAMAGE = 6;

    public DoubleStrafeSkill(Identifier id) {
        super(id, "Double Strafe", "Fire two arrows in quick succession at a target",
                12f, 0.3f,
                10,
                Mamy.id("skill/double_strafe"));
    }

    @Override
    public int getBaseVCT(int skillLevel) {
        return 0;
    }

    @Override
    public int getBaseFCT(int skillLevel) {
        return 2;
    }

    @Override
    public boolean shouldLockMovement() {
        return true;
    }

    @Override
    protected SkillTooltipData getSkillTooltipData(PlayerEntity player, int skillLevel) {
        SkillTooltipData data = new SkillTooltipData();

        float damageMultiplier = calculateDamageMultiplier(skillLevel);
        data.baseDamage = damageMultiplier;
        data.damageType = DamageTypeRef.PHYSICAL;
        data.maxHits = 1;
        data.secondaryDamages = Collections.singletonList(
                new SecondaryDamage(DamageTypeRef.PHYSICAL, damageMultiplier, 1, 1)
        );

        return data;
    }

    @Override
    public boolean canUse(LivingEntity caster, int skillLevel) {
        if (!(caster instanceof PlayerEntity player)) return false;
        if (!caster.isAlive()) return false;

        // Check if player has bow in main hand
        ItemStack mainHand = player.getStackInHand(Hand.MAIN_HAND);
        if (!(mainHand.getItem() instanceof BowItem)) {
            return false;
        }

        // Creative mode doesn't need arrows (like vanilla Minecraft)
        if (player.isCreative()) {
            return true;
        }

        // Survival mode needs arrows - check offhand first, then inventory
        ItemStack offHand = player.getStackInHand(Hand.OFF_HAND);
        if (offHand.getItem() instanceof ArrowItem) {
            return true;
        }

        // Check inventory for any arrows
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() instanceof ArrowItem) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean use(LivingEntity caster, int skillLevel) {
        if (!(caster instanceof PlayerEntity player)) return false;
        if (!(caster.getWorld() instanceof ServerWorld world)) return false;

        // Verify requirements again
        ItemStack mainHand = player.getStackInHand(Hand.MAIN_HAND);
        if (!(mainHand.getItem() instanceof BowItem)) return false;

        // Find target
        LivingEntity target = SkillUtil.findTargetEntity(player,10 + ModEntityComponents.PLAYERCLASS.get(player).getSkillLevel(SkillRegistry.VULTURES_EYE));
        if (target == null) return false;

        // Get arrow with effects (consume 2 arrows for the skill)
        ArrowInfo firstArrow = getArrowWithEffects(player);

        // Combine effects from both arrows
        List<StatusEffectInstance> combinedEffects = new ArrayList<>();
        if (firstArrow != null) combinedEffects.addAll(firstArrow.effects);
        if (firstArrow != null) combinedEffects.addAll(firstArrow.effects);

        // Calculate damage multiplier
        float damageMultiplier = calculateDamageMultiplier(skillLevel);

        // Create and spawn ArrowStrafeEntity
        DoubleStrafeEntity arrowStrafe = new DoubleStrafeEntity(world, target, BASE_DAMAGE, damageMultiplier, combinedEffects);

        // Set proper starting position
        Vec3d startPos = new Vec3d(player.getX(), player.getEyeY(), player.getZ());
        arrowStrafe.setPosition(startPos.x, startPos.y, startPos.z);
        arrowStrafe.setOwner(player);

        double speed = 3 + player.getAttributeValue(ModEntityAttributes.ARROW_SPEED); // Arrow speed
        arrowStrafe.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, (float)speed, 1.0F);

        world.spawnEntity(arrowStrafe);

        return true;
    }

    private float calculateDamageMultiplier(int skillLevel) {
        return BASE_DAMAGE * (BASE_DAMAGE_MULTIPLIER + (DAMAGE_MULTIPLIER_PER_LEVEL * (skillLevel - 1)));
    }

    private ArrowInfo getArrowWithEffects(PlayerEntity player) {
        ItemStack arrowStack;

        // Priority 1: Offhand arrow (always used if present)
        ItemStack offHand = player.getStackInHand(Hand.OFF_HAND);
        if (offHand.getItem() instanceof ArrowItem) {
            arrowStack = offHand.copy();
            consumeArrow(player, offHand);
            return new ArrowInfo(arrowStack, extractArrowEffects(arrowStack));
        }

        // Priority 2: Search inventory (tipped → spectral → normal)
        ItemStack selectedArrow = findArrowInInventory(player, ArrowType.TIPPED);
        if (selectedArrow == null) {
            selectedArrow = findArrowInInventory(player, ArrowType.SPECTRAL);
        }
        if (selectedArrow == null) {
            selectedArrow = findArrowInInventory(player, ArrowType.NORMAL);
        }

        // If none found
        if (selectedArrow == null) {
            if (player.isCreative()) {
                return new ArrowInfo(Items.ARROW.getDefaultStack(), new ArrayList<>());
            }
            return null;
        }

        arrowStack = selectedArrow.copy();
        consumeArrow(player, selectedArrow);
        return new ArrowInfo(arrowStack, extractArrowEffects(arrowStack));
    }

    private void consumeArrow(PlayerEntity player, ItemStack stack) {
        if (!player.isCreative()) stack.decrement(1);
    }

    private enum ArrowType { TIPPED, SPECTRAL, NORMAL }

    private ItemStack findArrowInInventory(PlayerEntity player, ArrowType type) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!(stack.getItem() instanceof ArrowItem)) continue;

            if (Objects.requireNonNull(type) == ArrowType.TIPPED) {
                if (stack.contains(DataComponentTypes.POTION_CONTENTS)) return stack;
            } else if (type == ArrowType.SPECTRAL) {
                if (stack.isOf(Items.SPECTRAL_ARROW)) return stack;
            } else if (type == ArrowType.NORMAL) {
                if (stack.isOf(Items.ARROW)) return stack;
            }
        }
        return null;
    }

    private List<StatusEffectInstance> extractArrowEffects(ItemStack arrowStack) {
        if (!arrowStack.contains(DataComponentTypes.POTION_CONTENTS)) {
            return new ArrayList<>();
        }

        PotionContentsComponent potionContents = arrowStack.get(DataComponentTypes.POTION_CONTENTS);
        if (potionContents == null || potionContents.equals(PotionContentsComponent.DEFAULT)) {
            return new ArrayList<>();
        }

        List<StatusEffectInstance> effects = new ArrayList<>();

        if (potionContents.potion().isPresent()) {
            for (StatusEffectInstance effect : ((Potion)((RegistryEntry<?>)potionContents.potion().get()).value()).getEffects()) {
                StatusEffectInstance arrowEffect = new StatusEffectInstance(
                        effect.getEffectType(),
                        Math.max(effect.mapDuration(duration -> duration / 8), 1),
                        effect.getAmplifier(),
                        effect.isAmbient(),
                        effect.shouldShowParticles()
                );
                effects.add(arrowEffect);
            }
        }

        effects.addAll(potionContents.customEffects());

        return effects;
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.ARCHER;
    }

    private record ArrowInfo(ItemStack arrow, List<StatusEffectInstance> effects) {}
}