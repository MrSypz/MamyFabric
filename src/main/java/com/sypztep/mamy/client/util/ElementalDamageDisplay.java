package com.sypztep.mamy.client.util;

import com.sypztep.mamy.client.particle.ElementalDamageParticle;
import com.sypztep.mamy.common.util.ElementType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class ElementalDamageDisplay {

    public static void showElementalDamageSmart(Entity target, Map<ElementType, Float> elementalDamage) {
        if (elementalDamage.isEmpty()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;
        if (world == null || client.player == null) return;

        List<Map.Entry<ElementType, Float>> sortedDamage = elementalDamage.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .sorted((a, b) -> Float.compare(b.getValue(), a.getValue()))
                .toList();

        if (sortedDamage.isEmpty()) return;

        Vec3d spawnPos = target.getPos().add(0.0, target.getHeight() + 0.3, 0.0);

        if (sortedDamage.size() > 1) {
            createHorizontalSpread(world, spawnPos, sortedDamage, client);
        } else {
            var entry = sortedDamage.getFirst();
            showElementalDamage(target, entry.getKey(), entry.getValue());
        }
    }

    public static void showElementalDamage(Entity target, ElementType element, float damage) {
        if (damage <= 0) return;

        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;
        if (world == null || client.player == null) return;

        String damageText = String.format("%.1f", damage);
        Vec3d spawnPos = target.getPos().add(0.0, target.getHeight() + 0.3, 0.0);

        ElementalDamageParticle particle = new ElementalDamageParticle(
                world,
                spawnPos.x, spawnPos.y, spawnPos.z,
                0, 0.3, 0,
                damageText, element, element.color
        );

        client.particleManager.addParticle(particle);
    }
    private static void createHorizontalSpread(ClientWorld world, Vec3d spawnPos,
                                               List<Map.Entry<ElementType, Float>> sortedDamage,
                                               MinecraftClient client) {
        if (client.player == null) return;

        Vector3f lookVector = client.gameRenderer.getCamera().getHorizontalPlane();
        float newZ = -1 * lookVector.x();
        float newX = lookVector.z();

        int count = sortedDamage.size();
        for (int i = 0; i < count; i++) {
            var entry = sortedDamage.get(i);
            String text = String.format("%.1f", entry.getValue());

            double horizontalOffset = (i - (count - 1) * 0.5) * 0.4;
            double verticalOffset = i * 0.1;

            double velocityX = (newX * horizontalOffset) * 0.25;

            // CREATE ARC: Center particles go higher, edge particles stay lower
            double centerDistance = Math.abs(horizontalOffset); // 0.0 at center, higher at edges
            double arcHeight = 1.0 - centerDistance; // 1.0 at center, lower at edges
            double velocityY = 0.1 + (arcHeight * 0.3); // Center: 0.4, Edges: ~0.1

            double velocityZ = (newZ * horizontalOffset) * 0.25;

            ElementalDamageParticle particle = new ElementalDamageParticle(
                    world,
                    spawnPos.x, spawnPos.y + verticalOffset, spawnPos.z,
                    velocityX, velocityY, velocityZ,
                    text, entry.getKey(), entry.getKey().color
            );

            client.particleManager.addParticle(particle);
        }
    }
}