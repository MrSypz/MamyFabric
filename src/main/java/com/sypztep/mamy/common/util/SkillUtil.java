package com.sypztep.mamy.common.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public class SkillUtil {
    public static LivingEntity findTargetEntity(PlayerEntity player,double range) {
        Vec3d start = player.getCameraPosVec(1.0f);
        Vec3d direction = player.getRotationVec(1.0f);
        Vec3d end = start.add(direction.multiply(range));

        Box rayBox = new Box(start, end);

        LivingEntity closestEntity = null;
        double closestDistance = Double.MAX_VALUE;

        for (LivingEntity entity : player.getWorld().getEntitiesByClass(LivingEntity.class, rayBox, e -> e != player && e.isAlive())) {
            Optional<Vec3d> intersection = entity.getBoundingBox().raycast(start, end);
            if (intersection.isPresent()) {
                double distance = start.distanceTo(intersection.get());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestEntity = entity;
                }
            }
        }

        return closestEntity;
    }
}
