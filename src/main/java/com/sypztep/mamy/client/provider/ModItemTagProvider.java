package com.sypztep.mamy.client.provider;

import com.sypztep.mamy.common.init.ModTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends FabricTagProvider.ItemTagProvider {

    public ModItemTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {

        // ==========================================
        // UNIVERSAL TOOLS - ALL CLASSES CAN USE
        // ==========================================
        getOrCreateTagBuilder(ModTags.Items.ALL_CLASSES)
                .addOptionalTag(ItemTags.AXES)          // Axes for chopping/tools
                .addOptionalTag(ItemTags.PICKAXES)      // Mining tools
                .addOptionalTag(ItemTags.SHOVELS)       // Digging tools
                .addOptionalTag(ItemTags.HOES)          // Farming tools
                .add(Items.FISHING_ROD, Items.SHEARS)
                .add(Items.FLINT_AND_STEEL, Items.BUCKET, Items.WATER_BUCKET, Items.LAVA_BUCKET);

        // ==========================================
        // WEAPON CATEGORIES (Clear Organization)
        // ==========================================

        // One-handed swords (most swords)
        getOrCreateTagBuilder(ModTags.Items.ONE_HAND_SWORDS)
                .add(Items.WOODEN_SWORD, Items.STONE_SWORD, Items.IRON_SWORD)
                .add(Items.GOLDEN_SWORD, Items.DIAMOND_SWORD, Items.NETHERITE_SWORD);

        // Two-handed swords (placeholder for custom weapons)
        getOrCreateTagBuilder(ModTags.Items.TWO_HAND_SWORDS);

        // Spears (placeholder for custom weapons)
        getOrCreateTagBuilder(ModTags.Items.SPEARS)
                .add(Items.TRIDENT); // Trident can act as a spear

        // Daggers (placeholder for custom weapons)
        getOrCreateTagBuilder(ModTags.Items.DAGGERS);

        // Staffs (placeholder for custom weapons)
        getOrCreateTagBuilder(ModTags.Items.STAFFS);

        // Maces (placeholder for custom weapons)
        getOrCreateTagBuilder(ModTags.Items.MACES);

        // Knuckles (placeholder for custom weapons)
        getOrCreateTagBuilder(ModTags.Items.KNUCKLES);

        // Regular bows
        getOrCreateTagBuilder(ModTags.Items.BOWS)
                .add(Items.BOW, Items.CROSSBOW);

        // Short bows (placeholder for custom weapons)
        getOrCreateTagBuilder(ModTags.Items.SHORT_BOWS);

        // Shields
        getOrCreateTagBuilder(ModTags.Items.SHIELDS)
                .add(Items.SHIELD);

        // ==========================================
        // ARMOR CATEGORIES
        // ==========================================

        getOrCreateTagBuilder(ModTags.Items.LIGHT_ARMOR)
                .add(Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS);

        getOrCreateTagBuilder(ModTags.Items.MEDIUM_ARMOR)
                .add(Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS)
                .add(Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS);

        getOrCreateTagBuilder(ModTags.Items.HEAVY_ARMOR)
                .add(Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS)
                .add(Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS)
                .add(Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS);

        // ==========================================
        // SPECIAL ITEMS
        // ==========================================
        getOrCreateTagBuilder(ModTags.Items.HOLY_ITEMS);
        getOrCreateTagBuilder(ModTags.Items.MAGIC_ITEMS);

        // ==========================================
        // CLASS EQUIPMENT SETS
        // Based on: "Novice: One Hand Sword, Shield, Light Armor, Medium Armor"
        // ==========================================

        // NOVICE: One Hand Sword, Shield, Light Armor, Medium Armor + Tools
        getOrCreateTagBuilder(ModTags.Items.NOVICE_EQUIPMENT)
                .addTag(ModTags.Items.ONE_HAND_SWORDS)
                .addTag(ModTags.Items.SHIELDS)
                .addTag(ModTags.Items.LIGHT_ARMOR)
                .addTag(ModTags.Items.MEDIUM_ARMOR)
                .addTag(ModTags.Items.ALL_CLASSES);

        // SWORDMAN: Spear, Shield, Two Hand Sword, One Hand Sword, Medium Armor, Heavy Armor + Tools
        getOrCreateTagBuilder(ModTags.Items.SWORDMAN_EQUIPMENT)
                .addTag(ModTags.Items.SPEARS)
                .addTag(ModTags.Items.SHIELDS)
                .addTag(ModTags.Items.TWO_HAND_SWORDS)
                .addTag(ModTags.Items.ONE_HAND_SWORDS)
                .addTag(ModTags.Items.MEDIUM_ARMOR)
                .addTag(ModTags.Items.HEAVY_ARMOR)
                .addTag(ModTags.Items.ALL_CLASSES);

        // MAGE: Dagger, Staff, Light Armor + Tools
        getOrCreateTagBuilder(ModTags.Items.MAGE_EQUIPMENT)
                .addTag(ModTags.Items.DAGGERS)
                .addTag(ModTags.Items.STAFFS)
                .addTag(ModTags.Items.LIGHT_ARMOR)
                .addTag(ModTags.Items.MAGIC_ITEMS)
                .addTag(ModTags.Items.ALL_CLASSES);

        // ARCHER: Bow, Dagger, Light Armor + Tools
        getOrCreateTagBuilder(ModTags.Items.ARCHER_EQUIPMENT)
                .addTag(ModTags.Items.BOWS)
                .addTag(ModTags.Items.DAGGERS)
                .addTag(ModTags.Items.LIGHT_ARMOR)
                .addTag(ModTags.Items.ALL_CLASSES);

        // ACOLYTE: Mace, Shield, Knuckle, Light Armor, Medium Armor + Tools
        getOrCreateTagBuilder(ModTags.Items.ACOLYTE_EQUIPMENT)
                .addTag(ModTags.Items.MACES)
                .addTag(ModTags.Items.SHIELDS)
                .addTag(ModTags.Items.KNUCKLES)
                .addTag(ModTags.Items.LIGHT_ARMOR)
                .addTag(ModTags.Items.MEDIUM_ARMOR)
                .addTag(ModTags.Items.HOLY_ITEMS)
                .addTag(ModTags.Items.ALL_CLASSES);

        // THIEF: Dagger, Short Bow, Light Armor + Tools
        getOrCreateTagBuilder(ModTags.Items.THIEF_EQUIPMENT)
                .addTag(ModTags.Items.DAGGERS)
                .addTag(ModTags.Items.SHORT_BOWS)
                .addTag(ModTags.Items.LIGHT_ARMOR)
                .addTag(ModTags.Items.ALL_CLASSES);
    }
}