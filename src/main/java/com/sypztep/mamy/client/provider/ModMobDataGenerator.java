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
                createMobExpEntry(writer, EntityType.ZOMBIE, 50, 150,5, 5, 4, 8, 2, 3, 15),          // AGI:4 LUK:15 → EVA:7 → 71.5% player hit
                createMobExpEntry(writer, EntityType.SKELETON, 60,200, 6, 6, 5, 10, 3, 4, 20),       // AGI:5 LUK:20 → EVA:9 → 71% player hit
                createMobExpEntry(writer, EntityType.SPIDER, 40,250, 4, 4, 8, 12, 2, 5, 25),         // AGI:8 LUK:25 → EVA:13 → 70% player hit
                createMobExpEntry(writer, EntityType.CREEPER, 80, 300,8, 8, 6, 10, 4, 4, 20),        // AGI:6 LUK:20 → EVA:10 → 70.75% player hit


                createMobExpEntry(writer, EntityType.SLIME, 40,10, 6, 8, 4, 12, 4, 4, 6), // Tank-like

                // === PASSIVE MOBS (Level 1-3) ===
                createMobExpFile(writer, EntityType.COW, MobExpEntry.withLevelStats(5, 10,1)),
                createMobExpFile(writer, EntityType.PIG, MobExpEntry.withLevelStats(5,10,1)),
                createMobExpFile(writer, EntityType.SHEEP, MobExpEntry.withLevelStats(5, 10,1)),
                createMobExpFile(writer, EntityType.CHICKEN, MobExpEntry.withLevelStats(5, 10,1)),
                createMobExpFile(writer, EntityType.RABBIT, MobExpEntry.archer(5, 10,1)), // Fast
                createMobExpFile(writer, EntityType.HORSE, MobExpEntry.withLevelStats(5, 10,2)),
                createMobExpFile(writer, EntityType.DONKEY, MobExpEntry.withLevelStats(5, 10,2)),
                createMobExpFile(writer, EntityType.MULE, MobExpEntry.withLevelStats(5, 10,2)),
                createMobExpFile(writer, EntityType.LLAMA, MobExpEntry.withLevelStats(5, 10,2)),
                createMobExpFile(writer, EntityType.TRADER_LLAMA, MobExpEntry.withLevelStats(5, 10,2)),
                createMobExpFile(writer, EntityType.VILLAGER, MobExpEntry.withLevelStats(5, 10,2)),
                createMobExpFile(writer, EntityType.WANDERING_TRADER, MobExpEntry.withLevelStats(5, 10,2)),
        };

        return CompletableFuture.allOf(futures);
    }

    private CompletableFuture<?> createMobExpFile(DataWriter writer, EntityType<?> entityType, MobExpEntry mobEntry) {
        return createMobExpFile(writer, entityType, mobEntry.expReward(),mobEntry.classReward(), mobEntry.baseLevel(),
                mobEntry.getStat(StatTypes.STRENGTH), mobEntry.getStat(StatTypes.AGILITY),
                mobEntry.getStat(StatTypes.VITALITY), mobEntry.getStat(StatTypes.INTELLIGENCE),
                mobEntry.getStat(StatTypes.DEXTERITY), mobEntry.getStat(StatTypes.LUCK));
    }

    private CompletableFuture<?> createMobExpEntry(DataWriter writer, EntityType<?> entityType,
                                                   int expReward,int classReward, int baseLevel, int str, int agi, int vit, int intel, int dex, int luck) {
        return createMobExpFile(writer, entityType, expReward, classReward, baseLevel, str, agi, vit, intel, dex, luck);
    }

    private CompletableFuture<?> createMobExpFile(DataWriter writer, EntityType<?> entityType,
                                                  int expReward,int classReward, int baseLevel, int strength, int agility, int vitality,
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

        JsonObject jsonObject = createJsonObject(expReward,classReward, baseLevel, strength, agility, vitality, intelligence, dexterity, luck);

        return DataProvider.writeToPath(writer, jsonObject, filePath);
    }

    private JsonObject createJsonObject(int expReward,int classReward, int baseLevel, int strength, int agility,
                                        int vitality, int intelligence, int dexterity, int luck) {
        JsonObject root = new JsonObject();
        root.addProperty("expReward", expReward);
        root.addProperty("classReward", classReward);
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