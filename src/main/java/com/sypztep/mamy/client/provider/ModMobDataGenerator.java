package com.sypztep.mamy.client.provider;

import com.google.gson.JsonObject;
import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.data.MobExpEntry;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class ModMobDataGenerator implements DataProvider {

    private final FabricDataOutput output;

    public ModMobDataGenerator(FabricDataOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        return generateMobExp(writer);
    }

    private CompletableFuture<?> generateMobExp(DataWriter writer) {
        CompletableFuture<?>[] futures = new CompletableFuture[]{

                // === EARLY GAME (vs Level 10 players with ~25 accuracy) ===
                // Target: 70-75% player hit rate → monsters need 8-13 evasion
                createMobExpEntry(writer, EntityType.ZOMBIE, 50, 5, 5, 4, 8, 2, 3, 15),          // AGI:4 LUK:15 → EVA:7 → 71.5% player hit
                createMobExpEntry(writer, EntityType.SKELETON, 60, 6, 6, 5, 10, 3, 4, 20),       // AGI:5 LUK:20 → EVA:9 → 71% player hit
                createMobExpEntry(writer, EntityType.SPIDER, 40, 4, 4, 8, 12, 2, 5, 25),         // AGI:8 LUK:25 → EVA:13 → 70% player hit
                createMobExpEntry(writer, EntityType.CREEPER, 80, 8, 8, 6, 10, 4, 4, 20),        // AGI:6 LUK:20 → EVA:10 → 70.75% player hit

                // === MID GAME (vs players with ~50 accuracy) ===
                // Target: 65-70% player hit rate → monsters need 50-70 evasion

                // Lower tier mid-game (easier)
                createMobExpEntry(writer, EntityType.ZOMBIFIED_PIGLIN, 120, 15, 17, 45, 20, 5, 8, 150), // AGI:45 LUK:150 → EVA:75 → 61.25% player hit
                createMobExpEntry(writer, EntityType.VINDICATOR, 150, 15, 17, 40, 18, 8, 10, 120),      // AGI:40 LUK:120 → EVA:64 → 63.5% player hit

                // Higher tier mid-game (harder)
                createMobExpEntry(writer, EntityType.STRAY, 180, 18, 18, 50, 18, 15, 12, 180),          // AGI:50 LUK:180 → EVA:86 → 57% player hit
                createMobExpEntry(writer, EntityType.PILLAGER, 140, 12, 12, 45, 12, 10, 8, 150),        // AGI:45 LUK:150 → EVA:75 → 61.25% player hit

                // Specialized mid-game
                createMobExpEntry(writer, EntityType.BLAZE, 180, 18, 18, 35, 18, 25, 15, 120),          // AGI:35 LUK:120 → EVA:59 → 64.75% player hit
                createMobExpEntry(writer, EntityType.WITCH, 350, 30, 10, 40, 35, 45, 8, 160),           // AGI:40 LUK:160 → EVA:72 → 62.5% player hit

                // === LATE GAME (vs players with ~90 accuracy) ===
                // Target: 60-65% player hit rate → monsters need 110-130 evasion

                createMobExpEntry(writer, EntityType.ENDERMAN, 400, 25, 25, 110, 20, 30, 15, 275),      // AGI:110 LUK:275 → EVA:165 → 48.25% player hit
                createMobExpEntry(writer, EntityType.WITHER_SKELETON, 220, 20, 22, 95, 22, 18, 20, 250), // AGI:95 LUK:250 → EVA:145 → 53.75% player hit
                createMobExpEntry(writer, EntityType.VEX, 80, 8, 8, 100, 8, 12, 10, 200),               // AGI:100 LUK:200 → EVA:140 → 55% player hit

                // High-tier late game
                createMobExpEntry(writer, EntityType.EVOKER, 400, 25, 15, 90, 30, 40, 18, 225),         // AGI:90 LUK:225 → EVA:135 → 56.25% player hit
                createMobExpEntry(writer, EntityType.GUARDIAN, 280, 18, 18, 85, 25, 15, 20, 200),       // AGI:85 LUK:200 → EVA:125 → 58.75% player hit

                // === BOSSES (vs players with ~90 accuracy) ===
                // Target: 55-60% player hit rate → monsters need 130-160 evasion

                createMobExpEntry(writer, EntityType.WITHER, 2000, 60, 62, 120, 70, 55, 40, 300),       // AGI:120 LUK:300 → EVA:180 → 44.5% player hit
                createMobExpEntry(writer, EntityType.ENDER_DRAGON, 5000, 80, 85, 140, 90, 70, 50, 350), // AGI:140 LUK:350 → EVA:210 → 37% player hit
                createMobExpEntry(writer, EntityType.WARDEN, 3000, 70, 75, 110, 85, 40, 35, 275),       // AGI:110 LUK:275 → EVA:165 → 48.25% player hit
                createMobExpEntry(writer, EntityType.ELDER_GUARDIAN, 1500, 35, 37, 100, 45, 30, 30, 250), // AGI:100 LUK:250 → EVA:150 → 52.5% player hit

                // === TANK MOBS (Low evasion, easy to hit) ===
                createMobExpEntry(writer, EntityType.RAVAGER, 300, 30, 32, 20, 35, 10, 8, 40),          // AGI:20 LUK:40 → EVA:28 → 82.5% player hit (easy to hit)
                createMobExpEntry(writer, EntityType.PIGLIN_BRUTE, 200, 20, 22, 15, 25, 8, 6, 30),      // AGI:15 LUK:30 → EVA:21 → 84.25% player hit

                // === PASSIVE MOBS (Very low evasion) ===
                createMobExpEntry(writer, EntityType.COW, 1, 2, 2, 2, 4, 1, 2, 5),                      // AGI:2 LUK:5 → EVA:3 → 72.25% player hit
                createMobExpEntry(writer, EntityType.PIG, 1, 2, 2, 2, 3, 1, 2, 5),                      // AGI:2 LUK:5 → EVA:3 → 72.25% player hit
                createMobExpEntry(writer, EntityType.SHEEP, 1, 2, 2, 3, 3, 1, 2, 5),                    // AGI:3 LUK:5 → EVA:4 → 71.75% player hit
        };

        return CompletableFuture.allOf(futures);
    }

    private CompletableFuture<?> createMobExpFile(DataWriter writer, EntityType<?> entityType, MobExpEntry mobEntry) {
        return createMobExpFile(writer, entityType, mobEntry.expReward(), mobEntry.baseLevel(),
                mobEntry.getStat(StatTypes.STRENGTH), mobEntry.getStat(StatTypes.AGILITY),
                mobEntry.getStat(StatTypes.VITALITY), mobEntry.getStat(StatTypes.INTELLIGENCE),
                mobEntry.getStat(StatTypes.DEXTERITY), mobEntry.getStat(StatTypes.LUCK));
    }

    private CompletableFuture<?> createMobExpEntry(DataWriter writer, EntityType<?> entityType,
                                                   int expReward, int baseLevel, int str, int agi, int vit, int intel, int dex, int luck) {
        return createMobExpFile(writer, entityType, expReward, baseLevel, str, agi, vit, intel, dex, luck);
    }

    private CompletableFuture<?> createMobExpFile(DataWriter writer, EntityType<?> entityType,
                                                  int expReward, int baseLevel, int strength, int agility, int vitality,
                                                  int intelligence, int dexterity, int luck) {

        Identifier entityId = Registries.ENTITY_TYPE.getId(entityType);
        String namespace = entityId.getNamespace();
        String path = entityId.getPath();

        // Create the file path: data/mamy/mobexp/namespace/path.json
        Path filePath = this.output.getPath()
                .resolve("data")
                .resolve(Mamy.MODID)
                .resolve("mobexp")
                .resolve(namespace)
                .resolve(path + ".json");

        JsonObject jsonObject = createJsonObject(expReward, baseLevel, strength, agility, vitality, intelligence, dexterity, luck);

        return DataProvider.writeToPath(writer, jsonObject, filePath);
    }

    private JsonObject createJsonObject(int expReward, int baseLevel, int strength, int agility,
                                        int vitality, int intelligence, int dexterity, int luck) {
        JsonObject root = new JsonObject();
        root.addProperty("expReward", expReward);
        root.addProperty("baseLevel", baseLevel);

        JsonObject stats = new JsonObject();
        stats.addProperty("strength", strength);
        stats.addProperty("agility", agility);
        stats.addProperty("vitality", vitality);
        stats.addProperty("intelligence", intelligence);
        stats.addProperty("dexterity", dexterity);
        stats.addProperty("luck", luck);

        root.add("stats", stats);
        return root;
    }

    @Override
    public String getName() {
        return "Mob Exp and Stats Data";
    }
}