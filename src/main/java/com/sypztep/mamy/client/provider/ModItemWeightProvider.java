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
        addWeight(provider,18.0f, Items.IRON_CHESTPLATE);         // Chainmail/plate combo
        addWeight(provider,12.0f, Items.IRON_LEGGINGS);           // Leg armor
        addWeight(provider,8.0f, Items.IRON_HELMET);              // Iron helm
        addWeight(provider,6.0f, Items.IRON_BOOTS);               // Iron sabatons

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
        addWeight(provider,30.0f, Items.NETHERITE_SWORD);         // Massive two-hander equivalent
        addWeight(provider,18.0f, Items.DIAMOND_SWORD);           // Crystal blade - heavy but sharp
        addWeight(provider,10.0f, Items.IRON_SWORD);              // Standard longsword (1-1.5kg real)
        addWeight(provider,8.0f, Items.GOLDEN_SWORD);             // Heavy but soft metal
        addWeight(provider,5.0f, Items.STONE_SWORD);              // Primitive heavy club
        addWeight(provider,2.0f, Items.WOODEN_SWORD);             // Training sword

        // ==========================================
        // TOOLS - BASED ON REAL TOOL WEIGHTS
        // ==========================================

        // PICKAXES (Mining tools - heavy duty)
        addWeight(provider,25.0f, Items.NETHERITE_PICKAXE);       // Industrial mining equipment
        addWeight(provider,15.0f, Items.DIAMOND_PICKAXE);
        addWeight(provider,8.0f, Items.IRON_PICKAXE);             // Standard pickaxe ~3-4kg
        addWeight(provider,6.0f, Items.GOLDEN_PICKAXE);
        addWeight(provider,4.0f, Items.STONE_PICKAXE);
        addWeight(provider,1.5f, Items.WOODEN_PICKAXE);

        // AXES (Combat/Utility tools)
        addWeight(provider,28.0f, Items.NETHERITE_AXE);           // Battle axe / felling axe
        addWeight(provider,16.0f, Items.DIAMOND_AXE);
        addWeight(provider,9.0f, Items.IRON_AXE);                 // Standard axe ~2-3kg
        addWeight(provider,7.0f, Items.GOLDEN_AXE);
        addWeight(provider,5.0f, Items.STONE_AXE);
        addWeight(provider,2.0f, Items.WOODEN_AXE);

        // SHOVELS (Lighter than picks/axes)
        addWeight(provider,20.0f, Items.NETHERITE_SHOVEL);
        addWeight(provider,12.0f, Items.DIAMOND_SHOVEL);
        addWeight(provider,6.0f, Items.IRON_SHOVEL);              // Standard spade ~1-2kg
        addWeight(provider,5.0f, Items.GOLDEN_SHOVEL);
        addWeight(provider,3.0f, Items.STONE_SHOVEL);
        addWeight(provider,1.0f, Items.WOODEN_SHOVEL);

        // HOES (Farming tools - lightest)
        addWeight(provider,15.0f, Items.NETHERITE_HOE);
        addWeight(provider,8.0f, Items.DIAMOND_HOE);
        addWeight(provider,4.0f, Items.IRON_HOE);                 // Garden hoe ~1kg
        addWeight(provider,3.0f, Items.GOLDEN_HOE);
        addWeight(provider,2.0f, Items.STONE_HOE);
        addWeight(provider,0.5f, Items.WOODEN_HOE);

        // ==========================================
        // RANGED WEAPONS - REAL WORLD BASED
        // ==========================================
        addWeight(provider,15.0f, Items.CROSSBOW);                // Heavy crossbow ~6-15kg (as you mentioned)
        addWeight(provider,8.0f, Items.BOW);                      // Compound/longbow ~2-4kg
        addWeight(provider,12.0f, Items.TRIDENT);                 // Heavy polearm weapon

        // ==========================================
        // SHIELDS & DEFENSIVE GEAR
        // ==========================================
        addWeight(provider,6.0f, Items.SHIELD);                   // Medieval shield ~2-3kg

        // ==========================================
        // SPECIAL HEAVY ITEMS
        // ==========================================
        addWeight(provider,50.0f, Items.ANVIL);                   // Blacksmith anvil (25-150kg real)
        addWeight(provider,35.0f, Items.NETHERITE_BLOCK);         // Concentrated ancient debris
        addWeight(provider,20.0f, Items.GOLD_BLOCK);              // Pure gold block (very dense)
        addWeight(provider,15.0f, Items.IRON_BLOCK);              // Iron ingot block
        addWeight(provider,12.0f, Items.DIAMOND_BLOCK);           // Compressed crystal

        // Items not listed use default 0.01f weight
        // This includes: food, crafting materials, redstone, etc.
    }
}