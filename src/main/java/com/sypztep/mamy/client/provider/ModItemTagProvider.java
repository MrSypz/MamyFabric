package com.sypztep.mamy.client.provider;

import com.sypztep.mamy.common.init.ModItems;
import com.sypztep.mamy.common.init.ModTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends FabricTagProvider.ItemTagProvider {

    public ModItemTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {

        // ==========================================
        // WEAPON CATEGORIES (For organization/reference)
        // ==========================================

        // One-handed swords
        getOrCreateTagBuilder(ModTags.Items.ONE_HAND_SWORDS)
                .add(Items.WOODEN_SWORD, Items.STONE_SWORD, Items.IRON_SWORD)
                .add(Items.GOLDEN_SWORD, Items.DIAMOND_SWORD, Items.NETHERITE_SWORD);

        // Two-handed swords (placeholder for custom weapons)
        getOrCreateTagBuilder(ModTags.Items.TWO_HAND_SWORDS);

        // Spears
        getOrCreateTagBuilder(ModTags.Items.SPEARS)
                .add(Items.TRIDENT);

        // Daggers (placeholder for custom weapons)
        getOrCreateTagBuilder(ModTags.Items.DAGGERS)
                .add(ModItems.NOVICE_DAGGER);

        // Staffs (placeholder for custom weapons)
        getOrCreateTagBuilder(ModTags.Items.STAFFS);

        // Maces (placeholder for custom weapons)
        getOrCreateTagBuilder(ModTags.Items.MACES)
                .add(ModItems.DARKSTEEL_MACE);

        // Knuckles (placeholder for custom weapons)
        getOrCreateTagBuilder(ModTags.Items.KNUCKLES);

        // Bows
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
    }
}