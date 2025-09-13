package com.sypztep.mamy.common.system.classkill.mage;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.entity.skill.MeteorFloorEntity;
import com.sypztep.mamy.common.init.ModClasses;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.skill.CastableSkill;
import com.sypztep.mamy.common.system.skill.Skill;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.List;

public class MeteorShowerSkill extends Skill implements CastableSkill {

    public MeteorShowerSkill(Identifier id, List<SkillRequirement> skillRequirements) {
        super(id, "Meteor Shower", "Summons a devastating meteor shower that creates ground flames followed by a massive meteor impact",
                80,
                30,
                10,
                Mamy.id("skill/meteor_shower"),
                skillRequirements);
    }

    @Override
    public int getBaseVCT(int skillLevel) {
        // Variable Cast Time: 150 + 40 * level ticks
        return 100 + (40 * skillLevel);
    }

    @Override
    public int getBaseFCT(int skillLevel) {
        // Fixed Cast Time: 20 + 5 * level ticks
        return 20 + (5 * skillLevel);
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
        // Resource cost: 80 + 10 * level
        return 80f + (10f * skillLevel);
    }

    @Override
    protected SkillTooltipData getSkillTooltipData(PlayerEntity player, int skillLevel) {
        SkillTooltipData data = new SkillTooltipData();

        data.baseDamage = 20 + (15 * skillLevel);
        data.damageType = DamageTypeRef.ELEMENT;
        data.maxHits = 4; // Ground effect 8x8, meteor explosion 15x15
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

        Vec3d targetPos = getTargetPosition(player);
        if (targetPos == null) return false;

        float damage = 20 + (15 * skillLevel);

        MeteorFloorEntity meteorFloor = new MeteorFloorEntity(world, player, damage);
        meteorFloor.setPosition(targetPos.x, targetPos.y, targetPos.z);
        world.spawnEntity(meteorFloor);

        return true;
    }

    private Vec3d getTargetPosition(PlayerEntity player) {
        Vec3d start = player.getEyePos();
        Vec3d direction = player.getRotationVec(1.0f);
        Vec3d end = start.add(direction.multiply(32)); // Max range 15 blocks

        BlockHitResult result = player.getWorld().raycast(new RaycastContext(
                start, end,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                player
        ));

        if (result.getType() == HitResult.Type.BLOCK) {
            return result.getPos().add(0, 1, 0);
        }

        return new Vec3d(end.x, player.getY(), end.z);
    }

    @Override
    public boolean isAvailableForClass(PlayerClass playerClass) {
        return playerClass == ModClasses.MAGE;
    }
}