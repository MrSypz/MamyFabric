package com.sypztep.mamy.client.provider;

import com.sypztep.mamy.client.util.MamyCodecDataProvider;
import com.sypztep.mamy.common.component.item.MobComponent;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class ModMobDataProvider extends MamyCodecDataProvider<MobComponent> {

    public ModMobDataProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(dataOutput, registriesFuture, MobComponent.RESOURCE_LOCATION, MobComponent.CODEC);
    }

    @Override
    protected void configure(BiConsumer<Identifier, MobComponent> provider, RegistryWrapper.WrapperLookup lookup) {
        generateMobData(provider);
    }
    // PROGRESSION GATES:
    // Level 1-25: Learn basics, 50-70% hit rates
    // Level 25-50: Stat planning required, 35-55% hit rates
    // Level 50-75: Specialized builds needed, 20-40% hit rates
    // Level 75-99: Perfect optimization required, 10-30% hit rates
    // End Game: Group content only, 5-20% hit rates

    private void generateMobData(BiConsumer<Identifier, MobComponent> writer) {
        // ===== PASSIVE MOBS (Level 1-5) - Tutorial Tier =====
        // These should be harmless and give minimal EXP to encourage fighting real mobs
        createMobEntry(writer, EntityType.ALLAY, 8, 6, 1, 1, 5, 3, 1, 2, 1);
        createMobEntry(writer, EntityType.ARMADILLO, 12, 8, 2, 2, 3, 8, 1, 1, 1);
        createMobEntry(writer, EntityType.AXOLOTL, 15, 10, 2, 2, 8, 5, 2, 3, 2);
        createMobEntry(writer, EntityType.BAT, 5, 3, 1, 1, 12, 2, 1, 5, 3);
        createMobEntry(writer, EntityType.CAMEL, 25, 15, 3, 3, 2, 15, 1, 2, 1);
        createMobEntry(writer, EntityType.CAT, 12, 8, 2, 2, 15, 5, 2, 8, 3);
        createMobEntry(writer, EntityType.CHICKEN, 8, 5, 1, 1, 18, 3, 1, 5, 2);
        createMobEntry(writer, EntityType.COD, 10, 6, 1, 1, 12, 4, 1, 8, 3);
        createMobEntry(writer, EntityType.COW, 18, 12, 2, 2, 5, 10, 1, 3, 2);
        createMobEntry(writer, EntityType.DONKEY, 35, 25, 3, 3, 3, 18, 2, 4, 2);
        createMobEntry(writer, EntityType.FROG, 12, 8, 2, 2, 12, 6, 3, 10, 4);
        createMobEntry(writer, EntityType.GLOW_SQUID, 20, 15, 2, 2, 8, 8, 8, 6, 5);
        createMobEntry(writer, EntityType.SQUID, 15, 12, 2, 2, 6, 6, 4, 6, 3);
        createMobEntry(writer, EntityType.HORSE, 45, 35, 4, 4, 5, 20, 3, 6, 3);
        createMobEntry(writer, EntityType.MOOSHROOM, 30, 20, 3, 3, 3, 15, 4, 3, 3);
        createMobEntry(writer, EntityType.MULE, 40, 30, 3, 3, 4, 18, 2, 4, 2);
        createMobEntry(writer, EntityType.OCELOT, 25, 18, 3, 3, 18, 8, 4, 12, 6);
        createMobEntry(writer, EntityType.PARROT, 12, 8, 2, 2, 20, 5, 6, 10, 8);
        createMobEntry(writer, EntityType.PIG, 20, 15, 2, 2, 5, 10, 2, 4, 2);
        createMobEntry(writer, EntityType.PUFFERFISH, 25, 18, 3, 4, 8, 6, 4, 10, 5);
        createMobEntry(writer, EntityType.RABBIT, 15, 10, 2, 2, 20, 5, 3, 15, 6);
        createMobEntry(writer, EntityType.SALMON, 18, 12, 2, 2, 15, 6, 2, 10, 4);
        createMobEntry(writer, EntityType.SHEEP, 18, 15, 2, 2, 3, 12, 1, 3, 2);
        createMobEntry(writer, EntityType.SKELETON_HORSE, 280, 200, 15, 8, 15, 25, 8, 20, 12);
        createMobEntry(writer, EntityType.SNIFFER, 80, 60, 5, 5, 2, 30, 4, 5, 5);
        createMobEntry(writer, EntityType.SNOW_GOLEM, 30, 20, 3, 4, 6, 15, 8, 8, 4);
        createMobEntry(writer, EntityType.STRIDER, 35, 25, 4, 3, 5, 18, 6, 6, 4);
        createMobEntry(writer, EntityType.TADPOLE, 5, 3, 1, 1, 5, 2, 1, 3, 2);
        createMobEntry(writer, EntityType.TROPICAL_FISH, 12, 8, 2, 1, 15, 4, 4, 8, 5);
        createMobEntry(writer, EntityType.TURTLE, 45, 35, 4, 3, 2, 25, 4, 4, 4);
        createMobEntry(writer, EntityType.VILLAGER, 18, 15, 2, 2, 3, 8, 8, 5, 6);
        createMobEntry(writer, EntityType.WANDERING_TRADER, 30, 22, 3, 3, 5, 12, 12, 8, 10);
        createMobEntry(writer, EntityType.ZOMBIE_HORSE, 150, 120, 8, 10, 8, 20, 3, 8, 5);

        // ===== NEUTRAL MOBS (Level 5-15) - Early Game Practice =====
        // Slightly tougher but still manageable for new players
        createMobEntry(writer, EntityType.BEE, 35, 25, 5, 4, 25, 8, 8, 18, 10);
        createMobEntry(writer, EntityType.DOLPHIN, 65, 50, 8, 6, 30, 15, 12, 25, 15);
        createMobEntry(writer, EntityType.FOX, 55, 40, 7, 5, 35, 12, 8, 28, 18);
        createMobEntry(writer, EntityType.GOAT, 85, 65, 10, 10, 20, 25, 6, 15, 8);
        createMobEntry(writer, EntityType.IRON_GOLEM, 1200, 900, 25, 20, 8, 60, 12, 12, 15); // Village defender - tough but fair
        createMobEntry(writer, EntityType.LLAMA, 70, 55, 8, 8, 8, 22, 10, 10, 6);
        createMobEntry(writer, EntityType.PANDA, 95, 75, 12, 12, 15, 30, 8, 12, 10);
        createMobEntry(writer, EntityType.POLAR_BEAR, 140, 110, 15, 18, 18, 35, 5, 15, 10);
        createMobEntry(writer, EntityType.TRADER_LLAMA, 75, 60, 9, 8, 10, 24, 12, 12, 8);
        createMobEntry(writer, EntityType.WOLF, 85, 65, 10, 10, 28, 18, 6, 22, 12);
        createMobEntry(writer, EntityType.ZOMBIFIED_PIGLIN, 180, 140, 15, 15, 12, 25, 8, 18, 15);

        // ===== EARLY GAME HOSTILES (Level 10-25) - Learning Curve =====
        // These should teach players about stat requirements and hit rates
        // Target: 50-65% hit rate for new players (0-25 DEX)
        createMobEntry(writer, EntityType.ZOMBIE, 120, 90, 12, 15, 8, 35, 2, 5, 3);           // 65% hit rate vs 0 DEX
        createMobEntry(writer, EntityType.ZOMBIE_VILLAGER, 130, 100, 12, 15, 8, 35, 4, 5, 5);  // Same stats, slightly more INT/LUK
        createMobEntry(writer, EntityType.SKELETON, 140, 110, 15, 8, 25, 25, 8, 35, 45);       // HIGH CRIT - Korean style!
        createMobEntry(writer, EntityType.SPIDER, 160, 120, 18, 12, 30, 30, 5, 28, 8);         // Fast, evasive
        createMobEntry(writer, EntityType.CREEPER, 180, 140, 20, 5, 15, 20, 25, 12, 12);       // Tanky, magical
        createMobEntry(writer, EntityType.SLIME, 100, 75, 10, 8, 12, 45, 3, 8, 5);            // Tanky but weak
        createMobEntry(writer, EntityType.SILVERFISH, 80, 60, 8, 6, 25, 15, 4, 18, 8);        // Fast, annoying
        createMobEntry(writer, EntityType.ENDERMITE, 220, 170, 22, 10, 35, 25, 15, 25, 15);     // Teleporter - evasive

        // ===== MID GAME HOSTILES (Level 25-45) - Stat Check Begins =====
        // Target: 40-55% hit rate for balanced players (25-50 DEX)
        // Mobs get 15-35 evasion to challenge accuracy builds
        createMobEntry(writer, EntityType.CAVE_SPIDER, 240, 180, 25, 18, 40, 35, 12, 45, 15);   // Fast + poisonous
        createMobEntry(writer, EntityType.DROWNED, 200, 150, 22, 15, 20, 40, 15, 25, 10);       // Balanced but tanky
        createMobEntry(writer, EntityType.HUSK, 260, 200, 28, 20, 12, 50, 8, 15, 8);          // Slow tank
        createMobEntry(writer, EntityType.STRAY, 280, 210, 30, 12, 35, 35, 12, 40, 35);         // Archer with crit
        createMobEntry(writer, EntityType.PHANTOM, 320, 240, 35, 20, 50, 30, 18, 35, 20);       // Flying menace
        createMobEntry(writer, EntityType.WITCH, 420, 320, 40, 8, 18, 35, 45, 20, 25);         // Magical threat

        // ===== HIGH-TIER OVERWORLD (Level 45-65) - Serious Business =====
        // Target: 30-45% hit rate for specialized players (50-75 DEX)
        // Mobs get 35-65 evasion, serious stat requirements
        createMobEntry(writer, EntityType.PILLAGER, 580, 450, 50, 15, 22, 45, 12, 55, 15);      // Accurate ranged
        createMobEntry(writer, EntityType.GUARDIAN, 750, 580, 55, 18, 15, 60, 40, 30, 20);      // Tanky magical
        createMobEntry(writer, EntityType.RAVAGER, 1100, 850, 65, 45, 8, 85, 5, 12, 8);        // Pure physical tank
        createMobEntry(writer, EntityType.VEX, 920, 700, 60, 25, 45, 30, 25, 40, 25);           // Evasive magical
        createMobEntry(writer, EntityType.VINDICATOR, 1200, 900, 70, 35, 15, 55, 8, 20, 12);    // Strong melee

        // ===== ELITE OVERWORLD (Level 65-85) - Korean MMO Difficulty =====
        // Target: 20-35% hit rate for high-level players (75-99 DEX)
        // Mobs get 65-100 evasion, require serious builds
        createMobEntry(writer, EntityType.EVOKER, 1800, 1400, 80, 12, 25, 50, 55, 35, 35);      // Magical boss-tier
        createMobEntry(writer, EntityType.ILLUSIONER, 1650, 1300, 78, 15, 30, 45, 50, 40, 40);  // Illusion master
        createMobEntry(writer, EntityType.BOGGED, 1050, 800, 68, 18, 35, 50, 22, 50, 30);       // Swamp sniper
        createMobEntry(writer, EntityType.BREEZE, 1450, 1100, 75, 20, 45, 40, 45, 50, 35);      // Wind elemental
        createMobEntry(writer, EntityType.ELDER_GUARDIAN, 5500, 4200, 90, 30, 20, 95, 65, 40, 30); // Mini-boss tier
        createMobEntry(writer, EntityType.ENDERMAN, 3200, 2500, 85, 25, 55, 60, 35, 75, 40);    // End mob in overworld - very dangerous

        // ===== NETHER MOBS (Level 70-95) - Hell Incarnate =====
        // Target: 15-30% hit rate even for max DEX players
        // Mobs get 80-120 evasion, Korean MMO hellish difficulty
        createMobEntry(writer, EntityType.PIGLIN, 1500, 1150, 75, 20, 18, 50, 8, 25, 15);       // Basic nether mob but tough
        createMobEntry(writer, EntityType.HOGLIN, 1900, 1450, 80, 35, 15, 70, 5, 12, 8);        // Charging tank
        createMobEntry(writer, EntityType.MAGMA_CUBE, 1100, 850, 70, 15, 22, 55, 18, 15, 12);   // Fire elemental
        createMobEntry(writer, EntityType.ZOGLIN, 2100, 1600, 82, 40, 18, 75, 6, 15, 10);       // Undead hoglin - stronger
        createMobEntry(writer, EntityType.PIGLIN_BRUTE, 2600, 2000, 88, 45, 12, 85, 8, 18, 15); // Elite guard
        createMobEntry(writer, EntityType.BLAZE, 2200, 1700, 85, 15, 25, 45, 45, 35, 25);       // Flying fire demon
        createMobEntry(writer, EntityType.WITHER_SKELETON, 2800, 2200, 90, 35, 18, 60, 15, 28, 18); // Undead warrior
        createMobEntry(writer, EntityType.GHAST, 3200, 2500, 95, 12, 30, 50, 50, 40, 30);       // Flying artillery
        createMobEntry(writer, EntityType.WITHER, 25000, 20000, 110, 35, 25, 95, 65, 40, 45);   // Boss - requires raid group

        // ===== END MOBS (Level 85-135) - Endgame Hell =====
        // Target: 10-25% hit rate, these should feel impossible
        // Massive evasion values, Korean MMO endgame difficulty
        createMobEntry(writer, EntityType.SHULKER, 4500, 3500, 100, 18, 12, 100, 40, 25, 25);    // Teleporting fortress
        createMobEntry(writer, EntityType.ENDER_DRAGON, 80000, 65000, 135, 45, 35, 150, 55, 50, 50); // Raid boss - impossible solo

        // ===== DEEP DARK (Level 110+) - Korean MMO Ultimate Challenge =====
        // Target: 5-15% hit rate, absolute endgame
        createMobEntry(writer, EntityType.WARDEN, 65000, 52000, 125, 70, 25, 200, 50, 35, 40); // THE ultimate challenge
    }

    private void createMobEntry(BiConsumer<Identifier, MobComponent> provider, EntityType<?> entityType,
                                int expReward, int classReward, int baseLevel, int vitality, int dexterity,
                                int strength, int intelligence, int agility, int luck) {
        Identifier entityId = Registries.ENTITY_TYPE.getId(entityType);

        MobComponent.MobStats stats = new MobComponent.MobStats(
                strength, agility, vitality, intelligence, dexterity, luck
        );

        MobComponent mobData = new MobComponent(expReward, classReward, baseLevel, stats);
        provider.accept(entityId, mobData);
    }

    @Override
    public String getName() {
        return "Mob Exp and Stats Data";
    }
}