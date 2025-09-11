package com.sypztep.mamy.common.system.classkill.archer;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.entity.skill.ArrowRainEntity;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.CastableSkill;
import com.sypztep.mamy.common.system.skill.Skill;
import com.sypztep.mamy.common.system.skill.SkillRegistry;
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
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ArrowRainSkill extends Skill implements CastableSkill {
    private static final float BASE_DAMAGE = 6.0f;
    private static final float ARROW_DAMAGE_MULTIPLIER = 1.6f;
    private static final float SKILL_LEVEL_BONUS = 0.10f;

    public ArrowRainSkill(Identifier id, List<SkillRequirement> skillRequirements) {
        super(id, "Arrow Rain", "Rain arrows from the sky in target area",
                15f,0.3f,
                10,
                Mamy.id("skill/arrow_rain"),
                skillRequirements);
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
    public boolean canBeInterupt() {
        return true;
    }

    @Override
    protected SkillTooltipData getSkillTooltipData(PlayerEntity player, int skillLevel) {
        SkillTooltipData data = new SkillTooltipData();

        data.baseDamage = calculateDamage(skillLevel);
        data.damageType = DamageType.PHYSICAL;
        data.maxHits = 5;
        data.secondaryDamages = Collections.singletonList(new SecondaryDamage(DamageType.PHYSICAL, calculateDamage(skillLevel), 2, 1));

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

        // Get arrow item and effects (following Minecraft's behavior)
        ArrowInfo arrowInfo = getArrowWithEffects(player);

        // Calculate damage
        float damage = calculateDamage(skillLevel);

        // Find target location on the ground
        Vec3d targetLocation = getGroundTargetLocation(player);
        if (targetLocation == null) return false;

        // Create arrow effects list
        List<StatusEffectInstance> effects = arrowInfo != null ? arrowInfo.effects : new ArrayList<>();

        ArrowRainEntity arrowRain = new ArrowRainEntity(world, damage, skillLevel, skillLevel >= 6 ? 3 : 5, effects);
        arrowRain.setPosition(targetLocation.x, targetLocation.y, targetLocation.z);
        arrowRain.setOwner(player);
        world.spawnEntity(arrowRain);

        // Play cast sound
        world.playSound(null, player.getBlockPos(),
                SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS,
                1.0f, 0.8f);

        return true;
    }

    private float calculateDamage(int skillLevel) {
        float skillLevelBonus = SKILL_LEVEL_BONUS * (skillLevel - 1);

        return BASE_DAMAGE + ARROW_DAMAGE_MULTIPLIER + skillLevelBonus;
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

        // Add effects from registered potion (like Minecraft does)
        if (potionContents.potion().isPresent())
            for (StatusEffectInstance effect : ((Potion)((RegistryEntry<?>)potionContents.potion().get()).value()).getEffects()) {
                StatusEffectInstance arrowEffect = new StatusEffectInstance(
                        effect.getEffectType(),
                        Math.max(effect.mapDuration(duration -> duration / 8),1),
                        effect.getAmplifier(),
                        effect.isAmbient(),
                        effect.shouldShowParticles()
                );
                effects.add(arrowEffect);
            }

        // Add custom effects
        effects.addAll(potionContents.customEffects());

        return effects;
    }

    private Vec3d getGroundTargetLocation(PlayerEntity player) {
        double maxDistance = 9.0 + ModEntityComponents.PLAYERCLASS.get(player).getSkillLevel(SkillRegistry.VULTURES_EYE);
        Vec3d start = player.getCameraPosVec(1.0f);
        Vec3d direction = player.getRotationVec(1.0f);
        Vec3d end = start.add(direction.multiply(maxDistance));

        HitResult hitResult = player.getWorld().raycast(new RaycastContext(
                start, end, RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE, player
        ));

        Vec3d targetPos = hitResult.getPos();

        if (hitResult.getType() == HitResult.Type.BLOCK && hitResult instanceof BlockHitResult) {
            if (Math.abs(targetPos.y - player.getY()) <= 3.0) {
                return targetPos;
            }
        }

        // Otherwise, find the ground below the target point
        Vec3d groundStart = new Vec3d(targetPos.x, targetPos.y + 5, targetPos.z);
        Vec3d groundEnd = new Vec3d(targetPos.x, player.getY() - 20, targetPos.z);

        HitResult groundHit = player.getWorld().raycast(new RaycastContext(
                groundStart, groundEnd, RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE, player
        ));

        if (groundHit.getType() == HitResult.Type.BLOCK) return groundHit.getPos();

        return new Vec3d(targetPos.x, player.getY(), targetPos.z);
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.ARCHER;
    }

    private record ArrowInfo(ItemStack arrow, List<StatusEffectInstance> effects) {}
}