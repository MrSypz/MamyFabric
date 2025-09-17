package com.sypztep.mamy.common.util;

import com.sypztep.mamy.client.render.DebugBoxRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public class SkillUtil {
    public static LivingEntity findTargetEntity(PlayerEntity player, double range) {
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

    public static Box makeBox(Vec3d center, double width, double height, double depth) {
        double halfW = width * 0.5f;
        double halfD = depth * 0.5f;
        Box box = new Box(center.x - halfW, center.y, center.z - halfD, center.x + halfW, center.y + height, center.z + halfD);
        DebugBoxRenderer.addBox(box, 0f, 1f, 0f, 1f, 40);
        return box;
    }

    public static Box makeBox(Entity entity, double width, double height, double depth) {
        double halfW = width * 0.5f;
        double halfD = depth * 0.5f;
        Vec3d center = entity.getPos();
        Box box = new Box(center.x - halfW, center.y, center.z - halfD, center.x + halfW, center.y + height, center.z + halfD);
        DebugBoxRenderer.addBox(box, 0f, 1f, 0f, 1f, 40);
        return box;
    }

    public static Box makePathBox(Vec3d start, Vec3d end, double width, double height) {
        double halfW = width * 0.5;
        Box box = new Box(Math.min(start.x, end.x) - halfW, Math.min(start.y, end.y), Math.min(start.z, end.z) - halfW, Math.max(start.x, end.x) + halfW, Math.max(start.y, end.y) + height, Math.max(start.z, end.z) + halfW);
        DebugBoxRenderer.addBox(box, 0f, 1f, 0f, 1f, 40);
        return box;
    }
    public static Box makePathBox(Entity entity,double range, double width, double height) {
        double halfW = width * 0.5;
        Vec3d direction = entity.getRotationVector().normalize();
        Vec3d start = entity.getPos();
        Vec3d end = start.add(direction.multiply(range)); // 5 block range
        Box box = new Box(Math.min(start.x, end.x) - halfW, Math.min(start.y, end.y), Math.min(start.z, end.z) - halfW, Math.max(start.x, end.x) + halfW, Math.max(start.y, end.y) + height, Math.max(start.z, end.z) + halfW);
        DebugBoxRenderer.addBox(box, 0f, 1f, 0f, 1f, 40);
        return box;
    }
}
