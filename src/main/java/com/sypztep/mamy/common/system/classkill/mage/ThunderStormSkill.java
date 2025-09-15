package com.sypztep.mamy.common.system.classkill.mage;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.entity.skill.ThunderStormEntity;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.init.ModSoundEvents;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.CastableSkill;
import com.sypztep.mamy.common.system.skill.Skill;
import com.sypztep.mamy.common.system.skill.SkillRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class ThunderStormSkill extends Skill implements CastableSkill {

    public ThunderStormSkill(Identifier id) {
        super(id, "Thunder Storm", "Summon a devastating thunderstorm that strikes multiple lightning bolts in target area",
                25f, 0.8f, 10, Mamy.id("skill/thunder_storm"));
    }

    @Override
    public int getBaseVCT(int skillLevel) {
        return 0;
    }

    @Override
    public int getBaseFCT(int skillLevel) {
        return 5;
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
    public Identifier getCastedAnimation() {
        return Mamy.id("thunder_storm_cast");
    }

    @Override
    public SoundContainer getCastCompleteSound() {
        return new SoundContainer(ModSoundEvents.ENTITY_ELECTRIC_SHOOT, SoundCategory.PLAYERS, 1.2f, 1.1f);
    }

    @Override
    public float getResourceCost(int skillLevel) {
        return 1;
    }

    @Override
    protected SkillTooltipData getSkillTooltipData(PlayerEntity player, int skillLevel) {
        SkillTooltipData data = new SkillTooltipData();

        float baseDamage = (float) player.getAttributeValue(ModEntityAttributes.MAGIC_ATTACK_DAMAGE_FLAT);
        data.baseDamage = baseDamage + (12 + (skillLevel * 4)); // 12 + 4*level base damage
        data.damageType = DamageTypeRef.ELEMENT;
        data.maxHits = 5; // 5 lightning strikes max

        return data;
    }

    @Override
    public boolean canUse(LivingEntity caster, int skillLevel) {
        return caster instanceof PlayerEntity && caster.isAlive();
    }

    @Override
    public boolean use(LivingEntity caster, int skillLevel) {
        if (!(caster instanceof PlayerEntity player)) return false;
        if (!(caster.getWorld() instanceof ServerWorld world)) return false;

        Vec3d targetLocation = getGroundTargetLocation(player);
        if (targetLocation == null) return false;

        float baseDamage = (float) player.getAttributeValue(ModEntityAttributes.MAGIC_ATTACK_DAMAGE_FLAT);
        float damage = baseDamage + (12 + (skillLevel * 4));

        // Create thunderstorm entity at target location with skill level
        ThunderStormEntity thunderstorm = new ThunderStormEntity(world, player, damage, skillLevel);
        thunderstorm.setPosition(targetLocation.x, targetLocation.y, targetLocation.z);
        world.spawnEntity(thunderstorm);
        return true;
    }

    private Vec3d getGroundTargetLocation(PlayerEntity player) {
        double maxDistance = 12.0 + ModEntityComponents.PLAYERCLASS.get(player).getSkillLevel(SkillRegistry.VULTURES_EYE);
        Vec3d start = player.getCameraPosVec(1.0f);
        Vec3d direction = player.getRotationVec(1.0f);
        Vec3d end = start.add(direction.multiply(maxDistance));

        HitResult hitResult = player.getWorld().raycast(new RaycastContext(
                start, end, RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE, player
        ));

        Vec3d targetPos = hitResult.getPos();

        if (hitResult.getType() == HitResult.Type.BLOCK && hitResult instanceof BlockHitResult) {
            if (Math.abs(targetPos.y - player.getY()) <= 4.0) { // Allow slightly higher elevation
                return targetPos;
            }
        }

        // Otherwise, find the ground below the target point
        Vec3d groundStart = new Vec3d(targetPos.x, targetPos.y + 8, targetPos.z);
        Vec3d groundEnd = new Vec3d(targetPos.x, player.getY() - 25, targetPos.z);

        HitResult groundHit = player.getWorld().raycast(new RaycastContext(
                groundStart, groundEnd, RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE, player
        ));

        if (groundHit.getType() == HitResult.Type.BLOCK) {
            return groundHit.getPos();
        }

        return new Vec3d(targetPos.x, player.getY(), targetPos.z);
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.MAGE;
    }
}