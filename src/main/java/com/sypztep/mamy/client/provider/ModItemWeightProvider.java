package com.sypztep.mamy.client.provider;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ModItemWeightProvider implements DataProvider {
    private final FabricDataOutput output;
    private final Map<Item, Float> customWeights = new HashMap<>();

    public ModItemWeightProvider(FabricDataOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        generateRealisticWeights();

        CompletableFuture<?>[] futures = customWeights.entrySet().stream()
                .map(entry -> {
                    Item item = entry.getKey();
                    float weight = entry.getValue();

                    Identifier itemId = Registries.ITEM.getId(item);
                    Path path = output.getPath().resolve("data")
                            .resolve(itemId.getNamespace())
                            .resolve("itemweight")
                            .resolve(itemId.getPath() + ".json");

                    JsonObject json = new JsonObject();
                    json.addProperty("weight", weight);

                    return DataProvider.writeToPath(writer, json, path);
                })
                .toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(futures);
    }

    @Override
    public String getName() {
        return "Item Weight Data";
    }

    public ModItemWeightProvider addWeight(Item item, float weight) {
        customWeights.put(item, weight);
        return this;
    }

    public ModItemWeightProvider addWeight(float weight, Item... items) {
        for (Item item : items) {
            customWeights.put(item, weight);
        }
        return this;
    }

    public ModItemWeightProvider addWeightFromTag(TagKey<Item> tag, float weight) {
        Registries.ITEM.getEntryList(tag).ifPresent(entries -> {
            entries.forEach(entry -> {
                customWeights.put(entry.value(), weight);
            });
        });
        return this;
    }

    /**
     * REALISTIC WEIGHT SYSTEM
     *
     * Player Progression:
     * - Start: 400 max weight
     * - Per VIT point: +30 weight
     * - Equipment budget: 15-25% of total weight
     *
     * Starting Equipment Budget: 60-100 weight
     * Mid-game (50 VIT = 1900): 285-475 weight
     * Endgame (99 VIT = 3370): 505-843 weight
     */
    private void generateRealisticWeights() {

        // ==========================================
        // NETHERITE ARMOR - EXTREMELY HEAVY (Ancient Debris + Gold)
        // Full Set: 125 weight (requires ~17+ VIT investment)
        // ==========================================
        addWeight(45.0f, Items.NETHERITE_CHESTPLATE);    // Plate armor equivalent
        addWeight(35.0f, Items.NETHERITE_LEGGINGS);      // Heavy leg protection
        addWeight(25.0f, Items.NETHERITE_HELMET);        // Reinforced helm
        addWeight(20.0f, Items.NETHERITE_BOOTS);         // Armored boots

        // ==========================================
        // DIAMOND ARMOR - HEAVY (Crystal Structure)
        // Full Set: 80 weight (good for mid-game)
        // ==========================================
        addWeight(30.0f, Items.DIAMOND_CHESTPLATE);
        addWeight(24.0f, Items.DIAMOND_LEGGINGS);
        addWeight(16.0f, Items.DIAMOND_HELMET);
        addWeight(10.0f, Items.DIAMOND_BOOTS);

        // ==========================================
        // IRON ARMOR - MEDIUM WEIGHT (Realistic Medieval)
        // Full Set: 44 weight (perfect starter set - 11% of 400)
        // ==========================================
        addWeight(18.0f, Items.IRON_CHESTPLATE);         // Chainmail/plate combo
        addWeight(12.0f, Items.IRON_LEGGINGS);           // Leg armor
        addWeight(8.0f, Items.IRON_HELMET);              // Iron helm
        addWeight(6.0f, Items.IRON_BOOTS);               // Iron sabatons

        // ==========================================
        // GOLD ARMOR - VERY HEAVY (Real Gold Density!)
        // Full Set: 95 weight (heavier than diamond - realistic!)
        // ==========================================
        addWeight(38.0f, Items.GOLDEN_CHESTPLATE);       // Gold is DENSE
        addWeight(28.0f, Items.GOLDEN_LEGGINGS);
        addWeight(18.0f, Items.GOLDEN_HELMET);
        addWeight(11.0f, Items.GOLDEN_BOOTS);

        // ==========================================
        // CHAINMAIL ARMOR - LIGHT-MEDIUM
        // Full Set: 32 weight (budget option)
        // ==========================================
        addWeight(14.0f, Items.CHAINMAIL_CHESTPLATE);
        addWeight(10.0f, Items.CHAINMAIL_LEGGINGS);
        addWeight(5.0f, Items.CHAINMAIL_HELMET);
        addWeight(3.0f, Items.CHAINMAIL_BOOTS);

        // ==========================================
        // LEATHER ARMOR - LIGHT WEIGHT
        // Full Set: 8 weight (2% of starting weight - negligible)
        // ==========================================
        addWeight(3.0f, Items.LEATHER_CHESTPLATE);
        addWeight(2.5f, Items.LEATHER_LEGGINGS);
        addWeight(1.5f, Items.LEATHER_HELMET);
        addWeight(1.0f, Items.LEATHER_BOOTS);

        // ==========================================
        // SWORDS - REALISTIC MEDIEVAL WEIGHTS
        // ==========================================
        addWeight(30.0f, Items.NETHERITE_SWORD);         // Massive two-hander equivalent
        addWeight(18.0f, Items.DIAMOND_SWORD);           // Crystal blade - heavy but sharp
        addWeight(10.0f, Items.IRON_SWORD);              // Standard longsword (1-1.5kg real)
        addWeight(8.0f, Items.GOLDEN_SWORD);             // Heavy but soft metal
        addWeight(5.0f, Items.STONE_SWORD);              // Primitive heavy club
        addWeight(2.0f, Items.WOODEN_SWORD);             // Training sword

        // ==========================================
        // TOOLS - BASED ON REAL TOOL WEIGHTS
        // ==========================================

        // PICKAXES (Mining tools - heavy duty)
        addWeight(25.0f, Items.NETHERITE_PICKAXE);       // Industrial mining equipment
        addWeight(15.0f, Items.DIAMOND_PICKAXE);
        addWeight(8.0f, Items.IRON_PICKAXE);             // Standard pickaxe ~3-4kg
        addWeight(6.0f, Items.GOLDEN_PICKAXE);
        addWeight(4.0f, Items.STONE_PICKAXE);
        addWeight(1.5f, Items.WOODEN_PICKAXE);

        // AXES (Combat/Utility tools)
        addWeight(28.0f, Items.NETHERITE_AXE);           // Battle axe / felling axe
        addWeight(16.0f, Items.DIAMOND_AXE);
        addWeight(9.0f, Items.IRON_AXE);                 // Standard axe ~2-3kg
        addWeight(7.0f, Items.GOLDEN_AXE);
        addWeight(5.0f, Items.STONE_AXE);
        addWeight(2.0f, Items.WOODEN_AXE);

        // SHOVELS (Lighter than picks/axes)
        addWeight(20.0f, Items.NETHERITE_SHOVEL);
        addWeight(12.0f, Items.DIAMOND_SHOVEL);
        addWeight(6.0f, Items.IRON_SHOVEL);              // Standard spade ~1-2kg
        addWeight(5.0f, Items.GOLDEN_SHOVEL);
        addWeight(3.0f, Items.STONE_SHOVEL);
        addWeight(1.0f, Items.WOODEN_SHOVEL);

        // HOES (Farming tools - lightest)
        addWeight(15.0f, Items.NETHERITE_HOE);
        addWeight(8.0f, Items.DIAMOND_HOE);
        addWeight(4.0f, Items.IRON_HOE);                 // Garden hoe ~1kg
        addWeight(3.0f, Items.GOLDEN_HOE);
        addWeight(2.0f, Items.STONE_HOE);
        addWeight(0.5f, Items.WOODEN_HOE);

        // ==========================================
        // RANGED WEAPONS - REAL WORLD BASED
        // ==========================================
        addWeight(15.0f, Items.CROSSBOW);                // Heavy crossbow ~6-15kg (as you mentioned)
        addWeight(8.0f, Items.BOW);                      // Compound/longbow ~2-4kg
        addWeight(12.0f, Items.TRIDENT);                 // Heavy polearm weapon

        // ==========================================
        // SHIELDS & DEFENSIVE GEAR
        // ==========================================
        addWeight(6.0f, Items.SHIELD);                   // Medieval shield ~2-3kg

        // ==========================================
        // SPECIAL HEAVY ITEMS
        // ==========================================
        addWeight(50.0f, Items.ANVIL);                   // Blacksmith anvil (25-150kg real)
        addWeight(35.0f, Items.NETHERITE_BLOCK);         // Concentrated ancient debris
        addWeight(20.0f, Items.GOLD_BLOCK);              // Pure gold block (very dense)
        addWeight(15.0f, Items.IRON_BLOCK);              // Iron ingot block
        addWeight(12.0f, Items.DIAMOND_BLOCK);           // Compressed crystal

        // Items not listed use default 0.01f weight
        // This includes: food, crafting materials, redstone, etc.
    }
}