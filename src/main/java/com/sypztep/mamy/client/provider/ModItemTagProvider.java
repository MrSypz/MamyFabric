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
        // ALL CLASSES - TOOLS EVERYONE CAN USE
        // ==========================================
        getOrCreateTagBuilder(ModTags.Items.ALL_CLASSES)
                .addOptionalTag(ItemTags.AXES)          // Pickaxe, axe tools
                .addOptionalTag(ItemTags.PICKAXES)      // Mining tools
                .addOptionalTag(ItemTags.SHOVELS)       // Digging tools
                .addOptionalTag(ItemTags.HOES)          // Farming tools
                .add(Items.FISHING_ROD, Items.TRIDENT, Items.SHEARS)
                .add(Items.FLINT_AND_STEEL, Items.BUCKET, Items.WATER_BUCKET, Items.LAVA_BUCKET);

        // ==========================================
        // WEAPON CATEGORIES (CLASS EXCLUSIVE)
        // ==========================================

        getOrCreateTagBuilder(ModTags.Items.SWORDS)
                .addOptionalTag(ItemTags.SWORDS);       // All vanilla swords

        getOrCreateTagBuilder(ModTags.Items.BOWS)
                .add(Items.BOW)
                .add(Items.CROSSBOW);

        getOrCreateTagBuilder(ModTags.Items.SHIELDS)
                .add(Items.SHIELD);

        // ==========================================
        // ARMOR CATEGORIES (CLASS EXCLUSIVE)
        // ==========================================

        getOrCreateTagBuilder(ModTags.Items.HEAVY_ARMOR)
                .add(Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS)
                .add(Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS)
                .add(Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS);

        getOrCreateTagBuilder(ModTags.Items.MEDIUM_ARMOR)
                .add(Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS)
                .add(Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS);

        getOrCreateTagBuilder(ModTags.Items.LIGHT_ARMOR)
                .add(Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS);

        // ==========================================
        // BASIC WEAPONS (for novice)
        // ==========================================

        getOrCreateTagBuilder(ModTags.Items.BASIC_WEAPONS)
                .add(Items.WOODEN_SWORD)
                .add(Items.BOW);

        // ==========================================
        // CLASS EQUIPMENT SETS (WEAPONS + ARMOR ONLY)
        // ==========================================

        // NOVICE - Basic weapons + Light armor + ALL TOOLS
        getOrCreateTagBuilder(ModTags.Items.NOVICE_EQUIPMENT)
                .addTag(ModTags.Items.LIGHT_ARMOR)
                .addTag(ModTags.Items.BASIC_WEAPONS)
                .addTag(ModTags.Items.ALL_CLASSES);  // Can use all tools

        // SWORDMAN - Swords + Shields + Heavy/Medium armor + ALL TOOLS
        getOrCreateTagBuilder(ModTags.Items.SWORDMAN_EQUIPMENT)
                .addTag(ModTags.Items.SWORDS)
                .addTag(ModTags.Items.SHIELDS)
                .addTag(ModTags.Items.HEAVY_ARMOR)
                .addTag(ModTags.Items.MEDIUM_ARMOR)
                .addTag(ModTags.Items.ALL_CLASSES);  // Can use all tools

        // MAGE - Staffs + Light armor ONLY + ALL TOOLS
        getOrCreateTagBuilder(ModTags.Items.MAGE_EQUIPMENT)
                .addTag(ModTags.Items.STAFFS)
                .addTag(ModTags.Items.LIGHT_ARMOR)
                .addTag(ModTags.Items.MAGIC_ITEMS)
                .addTag(ModTags.Items.ALL_CLASSES);  // Can use all tools

        // ARCHER - Bows + Light/Medium armor + ALL TOOLS
        getOrCreateTagBuilder(ModTags.Items.ARCHER_EQUIPMENT)
                .addTag(ModTags.Items.BOWS)
                .addTag(ModTags.Items.DAGGERS)
                .addTag(ModTags.Items.LIGHT_ARMOR)
                .addTag(ModTags.Items.MEDIUM_ARMOR)
                .addTag(ModTags.Items.ALL_CLASSES);  // Can use all tools

        // ACOLYTE - Maces + Staffs + Medium/Light armor + ALL TOOLS
        getOrCreateTagBuilder(ModTags.Items.ACOLYTE_EQUIPMENT)
                .addTag(ModTags.Items.MACES)
                .addTag(ModTags.Items.STAFFS)
                .addTag(ModTags.Items.MEDIUM_ARMOR)
                .addTag(ModTags.Items.LIGHT_ARMOR)
                .addTag(ModTags.Items.HOLY_ITEMS)
                .addTag(ModTags.Items.ALL_CLASSES);  // Can use all tools

        // THIEF - Daggers + Light armor + ALL TOOLS
        getOrCreateTagBuilder(ModTags.Items.THIEF_EQUIPMENT)
                .addTag(ModTags.Items.DAGGERS)
                .addTag(ModTags.Items.BOWS)  // For stealth attacks
                .addTag(ModTags.Items.LIGHT_ARMOR)
                .addTag(ModTags.Items.ALL_CLASSES);  // Can use all tools

        // ==========================================
        // PLACEHOLDER TAGS (for future expansion)
        // ==========================================

        getOrCreateTagBuilder(ModTags.Items.MACES);
        getOrCreateTagBuilder(ModTags.Items.STAFFS);
        getOrCreateTagBuilder(ModTags.Items.DAGGERS);
        getOrCreateTagBuilder(ModTags.Items.SPEARS);
        getOrCreateTagBuilder(ModTags.Items.HOLY_ITEMS);
        getOrCreateTagBuilder(ModTags.Items.MAGIC_ITEMS);
    }
}
