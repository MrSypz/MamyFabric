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
                // === PASSIVE MOBS (Level 1-8) - No penalty, easy targets ===
                createMobExpFile(writer, EntityType.ALLAY, MobExpEntry.withLevelStats(1, 1, 1)),
                createMobExpFile(writer, EntityType.ARMADILLO, MobExpEntry.withLevelStats(1, 1, 2)),
                createMobExpFile(writer, EntityType.AXOLOTL, MobExpEntry.withLevelStats(1, 1, 3)),
                createMobExpFile(writer, EntityType.BAT, MobExpEntry.withLevelStats(1, 1, 1)),
                createMobExpFile(writer, EntityType.CAMEL, MobExpEntry.withLevelStats(2, 1, 4)),
                createMobExpFile(writer, EntityType.CAT, MobExpEntry.withLevelStats(1, 1, 2)),
                createMobExpFile(writer, EntityType.CHICKEN, MobExpEntry.withLevelStats(1, 1, 1)),
                createMobExpFile(writer, EntityType.COD, MobExpEntry.withLevelStats(1, 1, 2)),
                createMobExpFile(writer, EntityType.COW, MobExpEntry.withLevelStats(1, 1, 3)),
                createMobExpFile(writer, EntityType.DONKEY, MobExpEntry.withLevelStats(2, 1, 5)),
                createMobExpFile(writer, EntityType.FROG, MobExpEntry.withLevelStats(1, 1, 2)),
                createMobExpFile(writer, EntityType.GLOW_SQUID, MobExpEntry.withLevelStats(1, 1, 4)),
                createMobExpFile(writer, EntityType.SQUID, MobExpEntry.withLevelStats(1, 1, 3)),
                createMobExpFile(writer, EntityType.HORSE, MobExpEntry.withLevelStats(2, 1, 5)),
                createMobExpFile(writer, EntityType.MOOSHROOM, MobExpEntry.withLevelStats(2, 1, 4)),
                createMobExpFile(writer, EntityType.MULE, MobExpEntry.withLevelStats(2, 1, 5)),
                createMobExpFile(writer, EntityType.OCELOT, MobExpEntry.withLevelStats(1, 1, 3)),
                createMobExpFile(writer, EntityType.PARROT, MobExpEntry.withLevelStats(1, 1, 2)),
                createMobExpFile(writer, EntityType.PIG, MobExpEntry.withLevelStats(1, 1, 3)),
                createMobExpFile(writer, EntityType.PUFFERFISH, MobExpEntry.withLevelStats(2, 1, 3)),
                createMobExpFile(writer, EntityType.RABBIT, MobExpEntry.withLevelStats(1, 1, 2)),
                createMobExpFile(writer, EntityType.SALMON, MobExpEntry.withLevelStats(1, 1, 2)),
                createMobExpFile(writer, EntityType.SHEEP, MobExpEntry.withLevelStats(1, 1, 3)),
                createMobExpEntry(writer, EntityType.SKELETON_HORSE, 1500, 1200, 25, 20, 50, 30, 15, 100, 10), // Rare spawn, higher level
                createMobExpFile(writer, EntityType.SNIFFER, MobExpEntry.withLevelStats(3, 2, 8)),
                createMobExpFile(writer, EntityType.SNOW_GOLEM, MobExpEntry.withLevelStats(1, 1, 2)),
                createMobExpFile(writer, EntityType.STRIDER, MobExpEntry.withLevelStats(2, 1, 4)),
                createMobExpFile(writer, EntityType.TADPOLE, MobExpEntry.withLevelStats(1, 1, 1)),
                createMobExpFile(writer, EntityType.TROPICAL_FISH, MobExpEntry.withLevelStats(1, 1, 2)),
                createMobExpFile(writer, EntityType.TURTLE, MobExpEntry.withLevelStats(2, 1, 5)),
                createMobExpFile(writer, EntityType.VILLAGER, MobExpEntry.withLevelStats(1, 1, 1)),
                createMobExpFile(writer, EntityType.WANDERING_TRADER, MobExpEntry.withLevelStats(2, 1, 4)),
                createMobExpFile(writer, EntityType.ZOMBIE_HORSE, MobExpEntry.withLevelStats(5, 3, 8)),

                // === NEUTRAL MOBS (Level 8-15) - Early game challenges ===
                createMobExpFile(writer, EntityType.BEE, MobExpEntry.withLevelStats(1, 1, 8)),
                createMobExpFile(writer, EntityType.DOLPHIN, MobExpEntry.withLevelStats(1, 1, 10)),
                createMobExpFile(writer, EntityType.FOX, MobExpEntry.withLevelStats(1, 1, 9)),
                createMobExpFile(writer, EntityType.GOAT, MobExpEntry.withLevelStats(1, 1, 12)),
                createMobExpFile(writer, EntityType.IRON_GOLEM, MobExpEntry.overworldTank(5000, 7500, 25)), // Village defender, tough
                createMobExpFile(writer, EntityType.LLAMA, MobExpEntry.withLevelStats(1, 1, 10)),
                createMobExpFile(writer, EntityType.PANDA, MobExpEntry.withLevelStats(1, 1, 14)),
                createMobExpFile(writer, EntityType.POLAR_BEAR, MobExpEntry.withLevelStats(1, 1, 15)),
                createMobExpFile(writer, EntityType.TRADER_LLAMA, MobExpEntry.withLevelStats(1, 1, 10)),
                createMobExpFile(writer, EntityType.WOLF, MobExpEntry.withLevelStats(1, 1, 12)),
                createMobExpFile(writer, EntityType.ZOMBIFIED_PIGLIN, MobExpEntry.withLevelStats(950, 850, 18)), // Nether mob in overworld

                // === OVERWORLD HOSTILE MOBS (Level 10-30) - Main progression ===
                createMobExpFile(writer, EntityType.ZOMBIE, MobExpEntry.overworldMob(850, 550,10)), // Level 10
                createMobExpFile(writer, EntityType.ZOMBIE_VILLAGER, MobExpEntry.overworldMob(850, 550,10)), // Level 10
                createMobExpFile(writer, EntityType.SKELETON, MobExpEntry.overworldArcher(1200, 980,10)), // Level 10
                createMobExpFile(writer, EntityType.SPIDER, MobExpEntry.overworldMob(1700, 1250,10)), // Level 10
                createMobExpFile(writer, EntityType.CREEPER, MobExpEntry.overworldMob(2100, 2300,10)), // Level 10
                createMobExpFile(writer, EntityType.SLIME, MobExpEntry.overworldMob(480, 330,10)), // Level 10
                createMobExpFile(writer, EntityType.SILVERFISH, MobExpEntry.overworldMob(550, 480,10)), // Level 10

                // Mid-tier overworld (Level 15-25)
                createMobExpFile(writer, EntityType.CAVE_SPIDER, MobExpEntry.overworldArcher(2850, 1800,15)), // Level 15 - confined spawning
                createMobExpFile(writer, EntityType.DROWNED, MobExpEntry.overworldMob(1500, 1250,15)), // Level 15
                createMobExpFile(writer, EntityType.HUSK, MobExpEntry.overworldMob(2100, 1500,18)), // Level 18 - desert only
                createMobExpFile(writer, EntityType.STRAY, MobExpEntry.overworldArcher(3000, 1800,20)), // Level 20 - cold biomes only
                createMobExpFile(writer, EntityType.PHANTOM, MobExpEntry.overworldMob(3500, 2000,22)), // Level 22 - insomnia spawn
                createMobExpFile(writer, EntityType.WITCH, MobExpEntry.overworldMage(4500, 8500,25)), // Level 25 - rare spawn, dangerous

                // High-tier overworld (Level 25-35)
                createMobExpFile(writer, EntityType.PILLAGER, MobExpEntry.overworldArcher(4000, 2500,25)), // Level 25
                createMobExpFile(writer, EntityType.GUARDIAN, MobExpEntry.overworldMage(7850, 12000,28)), // Level 28 - ocean monument
                createMobExpFile(writer, EntityType.RAVAGER, MobExpEntry.overworldTank(8000, 5000,30)), // Level 30 - raid only
                createMobExpFile(writer, EntityType.VEX, MobExpEntry.overworldMob(17500, 15250,32)), // Level 32 - evoker summon
                createMobExpFile(writer, EntityType.VINDICATOR, MobExpEntry.overworldWarrior(45850, 28785,35)), // Level 35 - woodland mansion/raid

                // Elite overworld (Level 35-45)
                createMobExpFile(writer, EntityType.EVOKER, MobExpEntry.overworldMage(60752, 40794,40)), // Level 40 - woodland mansion/raid
                createMobExpFile(writer, EntityType.ILLUSIONER, MobExpEntry.overworldMage(55750, 35218,38)), // Level 38 - unused mob, very rare
                createMobExpFile(writer, EntityType.BOGGED, MobExpEntry.overworldArcher(28795, 16785,35)), // Level 35 - trial chambers
                createMobExpFile(writer, EntityType.BREEZE, MobExpEntry.overworldMage(50175, 30148,38)), // Level 38 - trial chambers
                createMobExpFile(writer, EntityType.ELDER_GUARDIAN, MobExpEntry.overworldTank(185000, 85000, 45)), // Level 45 - boss, very rare

                // Special rare spawns (High level for rarity compensation)
                createMobExpFile(writer, EntityType.ENDERMITE, MobExpEntry.overworldMob(19000, 17000,35)), // Level 35 - ender pearl teleport spawn
                createMobExpFile(writer, EntityType.ENDERMAN, MobExpEntry.endMob(120000, 65850,50)), // Level 50 in overworld - requires 80+ DEX

                // === DEEP DARK/ANCIENT CITY MOBS (Level 95+) ===
                createMobExpFile(writer, EntityType.WARDEN, MobExpEntry.netherBoss(258850, 185575, 100)), // Level 100 - ultimate challenge

                // === NETHER MOBS (Level 66-80) - Mid-game progression ===
                createMobExpFile(writer, EntityType.PIGLIN, MobExpEntry.netherMob(45850, 28718,66)), // Level 66
                createMobExpFile(writer, EntityType.HOGLIN, MobExpEntry.netherMob(50715, 32258,68)), // Level 68
                createMobExpFile(writer, EntityType.MAGMA_CUBE, MobExpEntry.netherMob(8518, 6580,66)), // Level 66
                createMobExpFile(writer, EntityType.ZOGLIN, MobExpEntry.netherMob(55775, 35122,70)), // Level 70
                createMobExpFile(writer, EntityType.PIGLIN_BRUTE, MobExpEntry.netherWarrior(60347, 40785,72)), // Level 72
                createMobExpFile(writer, EntityType.BLAZE, MobExpEntry.netherMage(70985, 45578,75)), // Level 75
                createMobExpFile(writer, EntityType.WITHER_SKELETON, MobExpEntry.netherWarrior(75153, 48128,78)), // Level 78
                createMobExpFile(writer, EntityType.GHAST, MobExpEntry.netherMage(80175, 50152,80)), // Level 80
                createMobExpFile(writer, EntityType.WITHER, MobExpEntry.netherBoss(801200, 685400,95)), // Level 95 - boss

                // === END MOBS (Level 81-115) - End game ===
                createMobExpFile(writer, EntityType.SHULKER, MobExpEntry.endTank(185000, 120995,90)), // Level 90
                createMobExpFile(writer, EntityType.ENDER_DRAGON, MobExpEntry.endBoss(900000, 700000,115)) // Level 115 - final boss
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