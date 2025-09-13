package com.sypztep.mamy.client.provider;

import com.sypztep.mamy.client.util.MamyCodecDataProvider;
import com.sypztep.mamy.common.component.item.WeightComponent;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class ModItemWeightProvider extends MamyCodecDataProvider<WeightComponent> {

    public ModItemWeightProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(dataOutput, registriesFuture, WeightComponent.RESOURCE_LOCATION, WeightComponent.CODEC);
    }

    @Override
    protected void configure(BiConsumer<Identifier, WeightComponent> provider, RegistryWrapper.WrapperLookup lookup) {
        generateRealisticWeights(provider);
    }

    @Override
    public String getName() {
        return "Item Weight Data";
    }

    private void addWeight(BiConsumer<Identifier, WeightComponent> provider, float weight, Item... items) {
        for (Item item : items) {
            Identifier itemId = Registries.ITEM.getId(item);
            provider.accept(itemId, new WeightComponent(weight));
        }
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
    private void generateRealisticWeights(BiConsumer<Identifier, WeightComponent> provider) {

        // ==========================================
        // NETHERITE ARMOR - EXTREMELY HEAVY (Ancient Debris + Gold)
        // Full Set: 125 weight (requires ~17+ VIT investment)
        // ==========================================
        addWeight(provider,45.0f, Items.NETHERITE_CHESTPLATE);    // Plate armor equivalent
        addWeight(provider,35.0f, Items.NETHERITE_LEGGINGS);      // Heavy leg protection
        addWeight(provider,25.0f, Items.NETHERITE_HELMET);        // Reinforced helm
        addWeight(provider,20.0f, Items.NETHERITE_BOOTS);         // Armored boots

        // ==========================================
        // DIAMOND ARMOR - HEAVY (Crystal Structure)
        // Full Set: 80 weight (good for mid-game)
        // ==========================================
        addWeight(provider,30.0f, Items.DIAMOND_CHESTPLATE);
        addWeight(provider,24.0f, Items.DIAMOND_LEGGINGS);
        addWeight(provider,16.0f, Items.DIAMOND_HELMET);
        addWeight(provider,10.0f, Items.DIAMOND_BOOTS);

        // ==========================================
        // IRON ARMOR - MEDIUM WEIGHT (Realistic Medieval)
        // Full Set: 44 weight (perfect starter set - 11% of 400)
        // ==========================================
        addWeight(provider,18.0f, Items.IRON_CHESTPLATE);
        addWeight(provider,12.0f, Items.IRON_LEGGINGS);
        addWeight(provider,8.0f, Items.IRON_HELMET);
        addWeight(provider,6.0f, Items.IRON_BOOTS);

        // ==========================================
        // GOLD ARMOR - VERY HEAVY (Real Gold Density!)
        // Full Set: 95 weight (heavier than diamond - realistic!)
        // ==========================================
        addWeight(provider,38.0f, Items.GOLDEN_CHESTPLATE);       // Gold is DENSE
        addWeight(provider,28.0f, Items.GOLDEN_LEGGINGS);
        addWeight(provider,18.0f, Items.GOLDEN_HELMET);
        addWeight(provider,11.0f, Items.GOLDEN_BOOTS);

        // ==========================================
        // CHAINMAIL ARMOR - LIGHT-MEDIUM
        // Full Set: 32 weight (budget option)
        // ==========================================
        addWeight(provider,14.0f, Items.CHAINMAIL_CHESTPLATE);
        addWeight(provider,10.0f, Items.CHAINMAIL_LEGGINGS);
        addWeight(provider,5.0f, Items.CHAINMAIL_HELMET);
        addWeight(provider,3.0f, Items.CHAINMAIL_BOOTS);

        // ==========================================
        // LEATHER ARMOR - LIGHT WEIGHT
        // Full Set: 8 weight (2% of starting weight - negligible)
        // ==========================================
        addWeight(provider,3.0f, Items.LEATHER_CHESTPLATE);
        addWeight(provider,2.5f, Items.LEATHER_LEGGINGS);
        addWeight(provider,1.5f, Items.LEATHER_HELMET);
        addWeight(provider,1.0f, Items.LEATHER_BOOTS);

        // ==========================================
        // SWORDS - REALISTIC MEDIEVAL WEIGHTS
        // ==========================================
        addWeight(provider,30.0f, Items.NETHERITE_SWORD);
        addWeight(provider,18.0f, Items.DIAMOND_SWORD);
        addWeight(provider,10.0f, Items.IRON_SWORD);
        addWeight(provider,8.0f, Items.GOLDEN_SWORD);
        addWeight(provider,5.0f, Items.STONE_SWORD);
        addWeight(provider,2.0f, Items.WOODEN_SWORD);

        // ==========================================
        // TOOLS - BASED ON REAL TOOL WEIGHTS
        // ==========================================

        // PICKAXES (Mining tools - heavy duty)
        addWeight(provider,25.0f, Items.NETHERITE_PICKAXE);
        addWeight(provider,15.0f, Items.DIAMOND_PICKAXE);
        addWeight(provider,8.0f, Items.IRON_PICKAXE);
        addWeight(provider,6.0f, Items.GOLDEN_PICKAXE);
        addWeight(provider,4.0f, Items.STONE_PICKAXE);
        addWeight(provider,1.5f, Items.WOODEN_PICKAXE);

        // AXES (Combat/Utility tools)
        addWeight(provider,28.0f, Items.NETHERITE_AXE);
        addWeight(provider,16.0f, Items.DIAMOND_AXE);
        addWeight(provider,9.0f, Items.IRON_AXE);
        addWeight(provider,7.0f, Items.GOLDEN_AXE);
        addWeight(provider,5.0f, Items.STONE_AXE);
        addWeight(provider,2.0f, Items.WOODEN_AXE);

        // SHOVELS (Lighter than picks/axes)
        addWeight(provider,20.0f, Items.NETHERITE_SHOVEL);
        addWeight(provider,12.0f, Items.DIAMOND_SHOVEL);
        addWeight(provider,6.0f, Items.IRON_SHOVEL);
        addWeight(provider,5.0f, Items.GOLDEN_SHOVEL);
        addWeight(provider,3.0f, Items.STONE_SHOVEL);
        addWeight(provider,1.0f, Items.WOODEN_SHOVEL);

        // HOES (Farming tools - lightest)
        addWeight(provider,15.0f, Items.NETHERITE_HOE);
        addWeight(provider,8.0f, Items.DIAMOND_HOE);
        addWeight(provider,4.0f, Items.IRON_HOE);
        addWeight(provider,3.0f, Items.GOLDEN_HOE);
        addWeight(provider,2.0f, Items.STONE_HOE);
        addWeight(provider,0.5f, Items.WOODEN_HOE);

        // ==========================================
        // RANGED WEAPONS - REAL WORLD BASED
        // ==========================================
        addWeight(provider,15.0f, Items.CROSSBOW);
        addWeight(provider,4.0f, Items.BOW);
        addWeight(provider,12.0f, Items.TRIDENT);
        // ==========================================
        // SHIELDS & DEFENSIVE GEAR
        // ==========================================
        addWeight(provider,6.0f, Items.SHIELD);

        // ==========================================
        // SPECIAL HEAVY ITEMS
        // ==========================================
        addWeight(provider,50.0f, Items.ANVIL);
        addWeight(provider,5.0f, Items.TOTEM_OF_UNDYING);

        // ==========================================
        // METAL INGOTS & RAW MATERIALS
        // Calculated to match block weights (block = 9 ingots)
        // ==========================================

        // ==========================================
        // METAL BLOCKS (Base weights)
        // ==========================================
        addWeight(provider,15.0f, Items.IRON_BLOCK);
        addWeight(provider,20.0f, Items.GOLD_BLOCK);
        addWeight(provider,12.0f, Items.DIAMOND_BLOCK);
        addWeight(provider,10.0f, Items.COPPER_BLOCK);       // Lighter than iron
        addWeight(provider,30.0f, Items.NETHERITE_BLOCK);    // Reduced for balance

        // ==========================================
        // METAL INGOTS (Block ÷ 9)
        // ==========================================
        addWeight(provider,1.7f, Items.IRON_INGOT);          // 15.0f ÷ 9 = 1.67f
        addWeight(provider,2.2f, Items.GOLD_INGOT);          // 20.0f ÷ 9 = 2.22f
        addWeight(provider,1.3f, Items.DIAMOND);             // 12.0f ÷ 9 = 1.33f
        addWeight(provider,1.1f, Items.COPPER_INGOT);        // 10.0f ÷ 9 = 1.11f
        addWeight(provider,3.3f, Items.NETHERITE_INGOT);     // 30.0f ÷ 9 = 3.33f

        // ==========================================
        // METAL NUGGETS (Ingot ÷ 9)
        // ==========================================
        addWeight(provider,0.2f, Items.IRON_NUGGET);         // 1.7f ÷ 9 = 0.19f
        addWeight(provider,0.2f, Items.GOLD_NUGGET);         // 2.2f ÷ 9 = 0.24f

        // ==========================================
        // RAW MATERIALS (Same as refined ingots)
        // ==========================================
        addWeight(provider,1.7f, Items.RAW_IRON);
        addWeight(provider,2.2f, Items.RAW_GOLD);
        addWeight(provider,1.1f, Items.RAW_COPPER);

        // ==========================================
        // NETHERITE MATERIALS (Crafting Recipe Based)
        // 4 Scrap + 4 Gold Ingot = 1 Netherite Ingot
        // 4×0.8f + 4×2.2f = 3.2f + 8.8f = 12.0f → 3.3f (magical fusion)
        // ==========================================
        addWeight(provider,4.0f, Items.ANCIENT_DEBRIS);     // Heavy raw nether ore
        addWeight(provider,0.8f, Items.NETHERITE_SCRAP);    // Refined, lighter than debris

        // ==========================================
        // ORES (Raw material + stone weight)
        // ==========================================
        addWeight(provider,2.0f, Items.IRON_ORE);
        addWeight(provider,2.0f, Items.DEEPSLATE_IRON_ORE);
        addWeight(provider,2.5f, Items.GOLD_ORE);
        addWeight(provider,2.5f, Items.DEEPSLATE_GOLD_ORE);
        addWeight(provider,2.8f, Items.NETHER_GOLD_ORE);
        addWeight(provider,2.0f, Items.COPPER_ORE);
        addWeight(provider,2.0f, Items.DEEPSLATE_COPPER_ORE);
        addWeight(provider,1.8f, Items.DIAMOND_ORE);
        addWeight(provider,1.8f, Items.DEEPSLATE_DIAMOND_ORE);

        // ==========================================
        // COAL & CARBON (Very light)
        // ==========================================
        addWeight(provider,0.1f, Items.COAL);
        addWeight(provider,0.9f, Items.COAL_BLOCK);          // 0.1f × 9 = 0.9f
        addWeight(provider,0.5f, Items.COAL_ORE);
        addWeight(provider,0.5f, Items.DEEPSLATE_COAL_ORE);
        addWeight(provider,0.1f, Items.CHARCOAL);

        // ==========================================
        // OTHER GEMS & MATERIALS
        // ==========================================
        addWeight(provider,0.8f, Items.EMERALD);
        addWeight(provider,7.2f, Items.EMERALD_BLOCK);       // 0.8f × 9 = 7.2f
        addWeight(provider,1.2f, Items.EMERALD_ORE);
        addWeight(provider,1.2f, Items.DEEPSLATE_EMERALD_ORE);

        addWeight(provider,0.3f, Items.LAPIS_LAZULI);
        addWeight(provider,2.7f, Items.LAPIS_BLOCK);         // 0.3f × 9 = 2.7f
        addWeight(provider,0.8f, Items.LAPIS_ORE);
        addWeight(provider,0.8f, Items.DEEPSLATE_LAPIS_ORE);

        addWeight(provider,0.2f, Items.REDSTONE);
        addWeight(provider,1.8f, Items.REDSTONE_BLOCK);      // 0.2f × 9 = 1.8f
        addWeight(provider,0.7f, Items.REDSTONE_ORE);
        addWeight(provider,0.7f, Items.DEEPSLATE_REDSTONE_ORE);

        addWeight(provider,0.4f, Items.QUARTZ);
        addWeight(provider,3.6f, Items.QUARTZ_BLOCK);        // 0.4f × 9 = 3.6f
        addWeight(provider,1.0f, Items.NETHER_QUARTZ_ORE);

        // ==========================================
        // SMELTED MATERIALS
        // ==========================================
        addWeight(provider,0.8f, Items.BRICK);
        addWeight(provider,7.2f, Items.BRICKS);              // 0.8f × 9 = 7.2f
        addWeight(provider,1.2f, Items.NETHER_BRICK);
        addWeight(provider,10.8f, Items.NETHER_BRICKS);      // 1.2f × 9 = 10.8f
        addWeight(provider,2.5f, Items.GLASS);
        addWeight(provider,1.8f, Items.TERRACOTTA);

        // GLAZED TERRACOTTA (all same weight - decorative)
        addWeight(provider,1.8f, Items.WHITE_GLAZED_TERRACOTTA);
        addWeight(provider,1.8f, Items.ORANGE_GLAZED_TERRACOTTA);
        addWeight(provider,1.8f, Items.MAGENTA_GLAZED_TERRACOTTA);
        addWeight(provider,1.8f, Items.LIGHT_BLUE_GLAZED_TERRACOTTA);
        addWeight(provider,1.8f, Items.YELLOW_GLAZED_TERRACOTTA);
        addWeight(provider,1.8f, Items.LIME_GLAZED_TERRACOTTA);
        addWeight(provider,1.8f, Items.PINK_GLAZED_TERRACOTTA);
        addWeight(provider,1.8f, Items.GRAY_GLAZED_TERRACOTTA);
        addWeight(provider,1.8f, Items.LIGHT_GRAY_GLAZED_TERRACOTTA);
        addWeight(provider,1.8f, Items.CYAN_GLAZED_TERRACOTTA);
        addWeight(provider,1.8f, Items.PURPLE_GLAZED_TERRACOTTA);
        addWeight(provider,1.8f, Items.BLUE_GLAZED_TERRACOTTA);
        addWeight(provider,1.8f, Items.BROWN_GLAZED_TERRACOTTA);
        addWeight(provider,1.8f, Items.GREEN_GLAZED_TERRACOTTA);
        addWeight(provider,1.8f, Items.RED_GLAZED_TERRACOTTA);
        addWeight(provider,1.8f, Items.BLACK_GLAZED_TERRACOTTA);
    }
}