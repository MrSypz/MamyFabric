package com.sypztep.mamy.client.util;

import java.awt.*;
import java.util.*;
import java.util.List;

import com.sypztep.mamy.client.particle.ElementalDamageParticle;
import com.sypztep.mamy.common.util.ElementalDamageSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import net.minecraft.util.math.random.Random;

@Environment(EnvType.CLIENT)
public class ElementalDamageDisplay {

    /**
     * Show single elemental damage number with outward burst effect
     */
    public static void showElementalDamage(Entity target, ElementalDamageSystem.ElementType element, float damage) {
        if (damage <= 0) return;

        Color color = ElementalColors.getElementColor(element);
        String damageText = String.format("%.1f", damage);

        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;
        if (world == null || !target.getWorld().isClient()) return;

        Vec3d spawnPos = target.getPos().add(0.0, target.getHeight() + 0.5, 0.0);
        Random random = world.getRandom();

        // Create random position for outward direction
        Vec3d randomPos = new Vec3d(
                random.nextGaussian() * 0.3,
                0,
                random.nextGaussian() * 0.3
        );

        ElementalDamageParticle particle = ElementalDamageParticle.createWithOutwardSpeed(
                world, spawnPos.x, spawnPos.y, spawnPos.z, randomPos, random, color
        );
        particle.setText(damageText);
        client.particleManager.addParticle(particle);
    }

    /**
     * Show multiple elemental damage numbers with outward burst spread effect
     */
    public static void showElementalDamageBreakdown(Entity target, Map<ElementalDamageSystem.ElementType, Float> elementalDamage) {
        if (elementalDamage.isEmpty()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;
        if (world == null || !target.getWorld().isClient()) return;

        // Filter and sort by damage amount (highest first)
        List<Map.Entry<ElementalDamageSystem.ElementType, Float>> sortedDamage = elementalDamage.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .sorted((a, b) -> Float.compare(b.getValue(), a.getValue()))
                .toList();

        Vec3d spawnPos = target.getPos().add(0.0, target.getHeight() + 0.5, 0.0);
        Random random = world.getRandom();

        // Display individual damage numbers with staggered outward burst
        for (int i = 0; i < sortedDamage.size(); i++) {
            Map.Entry<ElementalDamageSystem.ElementType, Float> entry = sortedDamage.get(i);
            ElementalDamageSystem.ElementType element = entry.getKey();
            float damage = entry.getValue();

            Color color = ElementalColors.getElementColor(element);
            String damageText = String.format("%.1f", damage);

            // Create outward direction for each particle
            Vec3d randomPos = new Vec3d(
                    random.nextGaussian() * 0.5,
                    0,
                    random.nextGaussian() * 0.5
            );

            // Add slight delay for visual effect (staggered burst)
            scheduleOutwardParticleSpawn(target, damageText, randomPos, color, i * 2, random);
        }

        // Show total damage with special effect (delayed, more prominent)
        float totalDamage = (float) elementalDamage.values().stream().mapToDouble(Float::doubleValue).sum();
        String totalText = "Σ " + String.format("%.1f", totalDamage);
        Vec3d totalPos = new Vec3d(0, 0.5, 0); // Straight up

        scheduleOutwardParticleSpawn(target, totalText, totalPos, Color.WHITE, sortedDamage.size() * 2 + 5, random);
    }

    /**
     * Compact burst - all elements burst out simultaneously
     */
    public static void showElementalDamageCompact(Entity target, Map<ElementalDamageSystem.ElementType, Float> elementalDamage) {
        if (elementalDamage.isEmpty()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;
        if (world == null || !target.getWorld().isClient()) return;

        List<Map.Entry<ElementalDamageSystem.ElementType, Float>> sortedDamage = elementalDamage.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .sorted((a, b) -> Float.compare(b.getValue(), a.getValue()))
                .toList();

        Vec3d spawnPos = target.getPos().add(0.0, target.getHeight() + 0.5, 0.0);
        Random random = world.getRandom();

        // All particles spawn at once with outward burst
        for (int i = 0; i < sortedDamage.size(); i++) {
            Map.Entry<ElementalDamageSystem.ElementType, Float> entry = sortedDamage.get(i);
            ElementalDamageSystem.ElementType element = entry.getKey();
            float damage = entry.getValue();

            Color color = ElementalColors.getElementColor(element);
            String damageText = String.format("%.1f", damage);

            Vec3d randomPos = new Vec3d(
                    random.nextGaussian() * 0.4,
                    0,
                    random.nextGaussian() * 0.4
            );

            ElementalDamageParticle particle = ElementalDamageParticle.createWithOutwardSpeed(
                    world, spawnPos.x, spawnPos.y, spawnPos.z, randomPos, random, color
            );
            particle.setText(damageText);
            client.particleManager.addParticle(particle);
        }
    }

    /**
     * Radial burst - particles explode outward in organized directions
     */
    public static void showElementalDamageRadial(Entity target, Map<ElementalDamageSystem.ElementType, Float> elementalDamage) {
        if (elementalDamage.isEmpty()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;
        if (world == null || !target.getWorld().isClient()) return;

        List<Map.Entry<ElementalDamageSystem.ElementType, Float>> sortedDamage = elementalDamage.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .sorted((a, b) -> Float.compare(b.getValue(), a.getValue()))
                .toList();

        Vec3d spawnPos = target.getPos().add(0.0, target.getHeight() + 0.5, 0.0);
        Random random = world.getRandom();
        int count = sortedDamage.size();

        for (int i = 0; i < count; i++) {
            Map.Entry<ElementalDamageSystem.ElementType, Float> entry = sortedDamage.get(i);
            ElementalDamageSystem.ElementType element = entry.getKey();
            float damage = entry.getValue();

            Color color = ElementalColors.getElementColor(element);
            String damageText = String.format("%.1f", damage);

            // Create radial pattern but use outward speed for natural variation
            double angle = (2.0 * Math.PI * i) / count;
            Vec3d radialPos = new Vec3d(
                    Math.cos(angle) * 0.5,
                    0,
                    Math.sin(angle) * 0.5
            );

            ElementalDamageParticle particle = ElementalDamageParticle.createWithOutwardSpeed(
                    world, spawnPos.x, spawnPos.y, spawnPos.z, radialPos, random, color
            );
            particle.setText(damageText);
            client.particleManager.addParticle(particle);
        }
    }

    /**
     * Multiple burst effect - spawns several particles per damage type
     */
    public static void showElementalDamageMultiBurst(Entity target, Map<ElementalDamageSystem.ElementType, Float> elementalDamage) {
        if (elementalDamage.isEmpty()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;
        if (world == null || !target.getWorld().isClient()) return;

        Vec3d spawnPos = target.getPos().add(0.0, target.getHeight() + 0.5, 0.0);
        Random random = world.getRandom();

        // Spawn multiple particles for each damage type
        for (Map.Entry<ElementalDamageSystem.ElementType, Float> entry : elementalDamage.entrySet()) {
            if (entry.getValue() <= 0) continue;

            ElementalDamageSystem.ElementType element = entry.getKey();
            float damage = entry.getValue();
            Color color = ElementalColors.getElementColor(element);
            String damageText = String.format("%.1f", damage);

            // Spawn 2-4 particles per damage type based on damage amount
            int particleCount = Math.min(4, Math.max(2, (int)(damage / 10f) + 1));

            ElementalDamageParticle.spawnOutwardBurst(world, spawnPos.x, spawnPos.y, spawnPos.z,
                    damageText, color, particleCount, random);
        }
    }

    /**
     * Schedule outward particle spawn with delay
     */
    private static void scheduleOutwardParticleSpawn(Entity target, String text, Vec3d direction, Color color, int delayTicks, Random random) {
        if (delayTicks <= 0) {
            spawnOutwardParticle(target, text, direction, color, random);
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null) {
            ScheduledOutwardSpawn spawn = new ScheduledOutwardSpawn(target, text, direction, color, client.world.getTime() + delayTicks, random);
            scheduledOutwardSpawns.add(spawn);
        }
    }

    /**
     * Spawn single outward particle immediately
     */
    private static void spawnOutwardParticle(Entity target, String text, Vec3d direction, Color color, Random random) {
        if (!target.getWorld().isClient()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;
        if (world == null) return;

        Vec3d spawnPos = target.getPos().add(0.0, target.getHeight() + 0.5, 0.0);
        ElementalDamageParticle particle = ElementalDamageParticle.createWithOutwardSpeed(
                world, spawnPos.x, spawnPos.y, spawnPos.z, direction, random, color
        );
        particle.setText(text);
        client.particleManager.addParticle(particle);
    }

    // Scheduled spawn system for outward particles
    private static final List<ScheduledOutwardSpawn> scheduledOutwardSpawns = new ArrayList<>();

    /**
     * Call this from your client tick event
     */
    public static void tickScheduledSpawns(MinecraftClient client) {
        if (client.world == null) return;

        long currentTime = client.world.getTime();
        Iterator<ScheduledOutwardSpawn> iterator = scheduledOutwardSpawns.iterator();

        while (iterator.hasNext()) {
            ScheduledOutwardSpawn spawn = iterator.next();
            if (currentTime >= spawn.spawnTime) {
                // Check if entity still exists
                if (spawn.target != null && !spawn.target.isRemoved()) {
                    spawnOutwardParticle(spawn.target, spawn.text, spawn.direction, spawn.color, spawn.random);
                }
                iterator.remove();
            }
        }
    }

    private record ScheduledOutwardSpawn(Entity target, String text, Vec3d direction, Color color, long spawnTime, Random random) {
    }
}