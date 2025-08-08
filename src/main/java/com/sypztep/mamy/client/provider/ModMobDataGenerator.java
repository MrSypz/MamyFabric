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
                createMobExpEntry(writer, EntityType.SKELETON_HORSE, 1500, 1200, 25, 20, 50, 30, 15, 100, 10),
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
                createMobExpEntry(writer, EntityType.IRON_GOLEM, 5000, 7500, 25, 33, 15, 45, 25, 120, 28), // Village defender
                createMobExpFile(writer, EntityType.LLAMA, MobExpEntry.withLevelStats(1, 1, 10)),
                createMobExpFile(writer, EntityType.PANDA, MobExpEntry.withLevelStats(1, 1, 14)),
                createMobExpFile(writer, EntityType.POLAR_BEAR, MobExpEntry.withLevelStats(1, 1, 15)),
                createMobExpFile(writer, EntityType.TRADER_LLAMA, MobExpEntry.withLevelStats(1, 1, 10)),
                createMobExpFile(writer, EntityType.WOLF, MobExpEntry.withLevelStats(1, 1, 12)),
                createMobExpFile(writer, EntityType.ZOMBIFIED_PIGLIN, MobExpEntry.withLevelStats(950, 850, 18)),

                // === OVERWORLD HOSTILE MOBS (Level 10-65) - Main progression ===
                // Basic overworld (Level 10-15)
                createMobExpEntry(writer, EntityType.ZOMBIE, 850, 550, 10, 10, 32, 10, 10, 64, 10),
                createMobExpEntry(writer, EntityType.ZOMBIE_VILLAGER, 850, 550, 10, 10, 32, 10, 10, 64, 10),
                createMobExpEntry(writer, EntityType.SKELETON, 1200, 980, 12, 12, 40, 12, 12, 80, 27), // Archer
                createMobExpEntry(writer, EntityType.SPIDER, 1700, 1250, 11, 11, 35, 11, 11, 70, 11),
                createMobExpEntry(writer, EntityType.CREEPER, 2100, 2300, 13, 15, 30, 15, 13, 65, 13),
                createMobExpEntry(writer, EntityType.SLIME, 480, 330, 10, 8, 25, 8, 8, 60, 8),
                createMobExpEntry(writer, EntityType.SILVERFISH, 550, 480, 10, 8, 45, 8, 8, 55, 8),

                // Mid-tier overworld (Level 15-30)
                createMobExpEntry(writer, EntityType.CAVE_SPIDER, 2850, 1800, 18, 18, 50, 18, 18, 90, 33), // Archer spider
                createMobExpEntry(writer, EntityType.DROWNED, 1500, 1250, 16, 16, 35, 16, 16, 75, 16),
                createMobExpEntry(writer, EntityType.HUSK, 2100, 1500, 20, 20, 30, 22, 20, 70, 20),
                createMobExpEntry(writer, EntityType.STRAY, 3000, 1800, 22, 22, 45, 22, 22, 95, 37), // Cold archer
                createMobExpEntry(writer, EntityType.PHANTOM, 3500, 2000, 25, 25, 55, 25, 25, 85, 25),
                createMobExpEntry(writer, EntityType.WITCH, 4500, 8500, 28, 26, 45, 28, 40, 75, 40), // Mage

                // High-tier overworld (Level 30-45)
                createMobExpEntry(writer, EntityType.PILLAGER, 4000, 2500, 32, 32, 50, 32, 32, 100, 47), // Archer
                createMobExpEntry(writer, EntityType.GUARDIAN, 7850, 12000, 35, 35, 40, 35, 45, 85, 45), // Ocean mage
                createMobExpEntry(writer, EntityType.RAVAGER, 8000, 5000, 38, 46, 20, 58, 38, 120, 41), // Tank
                createMobExpEntry(writer, EntityType.VEX, 17500, 15250, 40, 40, 60, 40, 40, 95, 40),
                createMobExpEntry(writer, EntityType.VINDICATOR, 45850, 28785, 42, 47, 30, 50, 42, 105, 47), // Warrior

                // Elite overworld (Level 45-65)
                createMobExpEntry(writer, EntityType.EVOKER, 60752, 40794, 50, 48, 50, 50, 62, 90, 62), // Elite mage
                createMobExpEntry(writer, EntityType.ILLUSIONER, 55750, 35218, 48, 46, 48, 48, 60, 88, 60), // Rare mage
                createMobExpEntry(writer, EntityType.BOGGED, 28795, 16785, 45, 45, 55, 45, 45, 110, 60), // Trial archer
                createMobExpEntry(writer, EntityType.BREEZE, 50175, 30148, 52, 52, 65, 52, 72, 95, 72), // Trial mage
                createMobExpEntry(writer, EntityType.ELDER_GUARDIAN, 185000, 85000, 58, 68, 60, 78, 68, 175, 68), // Ocean boss

                // Special rare spawns (High level for rarity)
                createMobExpEntry(writer, EntityType.ENDERMITE, 19000, 17000, 45, 45, 70, 45, 45, 105, 45), // Rare spawn
                createMobExpEntry(writer, EntityType.ENDERMAN, 120000, 65850, 65, 75, 112, 115, 75, 176, 75), // Overworld enderman

                // === DEEP DARK/ANCIENT CITY MOBS (Level 95+) ===
                createMobExpEntry(writer, EntityType.WARDEN, 258850, 185575, 110, 135, 95, 210, 135, 250, 135), // Ultimate challenge

                // === NETHER MOBS (Level 66-95) - Mid-game progression ===
                createMobExpEntry(writer, EntityType.PIGLIN, 45850, 28718, 68, 73, 82, 73, 73, 120, 73),
                createMobExpEntry(writer, EntityType.HOGLIN, 50715, 32258, 70, 75, 75, 75, 70, 115, 70),
                createMobExpEntry(writer, EntityType.MAGMA_CUBE, 8518, 6580, 66, 66, 70, 71, 71, 110, 66),
                createMobExpEntry(writer, EntityType.ZOGLIN, 55775, 35122, 72, 77, 78, 77, 72, 125, 72),
                createMobExpEntry(writer, EntityType.PIGLIN_BRUTE, 60347, 40785, 75, 85, 75, 87, 75, 130, 85), // Warrior
                createMobExpEntry(writer, EntityType.BLAZE, 70985, 45578, 78, 78, 90, 81, 98, 100, 98), // Mage
                createMobExpEntry(writer, EntityType.WITHER_SKELETON, 75153, 48128, 82, 92, 75, 94, 82, 130, 92), // Warrior
                createMobExpEntry(writer, EntityType.GHAST, 80175, 50152, 85, 85, 90, 88, 105, 100, 105), // Flying mage
                createMobExpEntry(writer, EntityType.WITHER, 801200, 685400, 88, 113, 95, 203, 113, 150, 113), // Nether boss

                // === END MOBS (Level 95-135) - End game ===
                createMobExpEntry(writer, EntityType.SHULKER, 185000, 120995, 105, 120, 100, 135, 110, 200, 115), // End tank
                createMobExpEntry(writer, EntityType.ENDER_DRAGON, 900000, 700000, 135, 165, 120, 270, 165, 220, 160) // Final boss
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