package com.sypztep.mamy.common.init;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.component.item.ResourceComponents;
import com.sypztep.mamy.common.item.DaggerItem;
import com.sypztep.mamy.common.item.MaceItem;
import com.sypztep.mamy.common.item.ResourcePotionItem;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.*;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Rarity;

import java.util.Set;

public class ModItems {
    public static final Set<Item> CUSTOM_RENDER = new ReferenceOpenHashSet<>();

    public static Item CHILLING_LIGHT_WATER;
    public static Item THERMAL_ESSENCE;
    public static Item HOLY_WATER;

    public static Item POCKET_RESOURCE_WATER;
    public static Item LESSER_RESOURCE_WATER;
    public static Item RESOURCE_WATER;
    public static Item GREATER_RESOURCE_WATER;
    public static Item SUPERIOR_RESOURCE_WATER;
    public static Item ULTIMATE_RESOURCE_WATER;

    public static Item NOVICE_DAGGER;
    public static Item DARKSTEEL_MACE;

    public static void init() {
        CHILLING_LIGHT_WATER = registeritem("chilling_light_water", new PotionItem(new Item.Settings().maxCount(64)));
        THERMAL_ESSENCE = registeritem("thermal_essence", new PotionItem(new Item.Settings().maxCount(64)));
        HOLY_WATER = registeritem("holy_water", new PotionItem(new Item.Settings().maxCount(64)));

        POCKET_RESOURCE_WATER = registeritem("pocket_resource_water",
                new ResourcePotionItem(new Item.Settings()
                        .maxCount(16)
                        .component(ModDataComponents.RESOURCE_RESTORE, ResourceComponents.POCKET_RESOURCE)));

        LESSER_RESOURCE_WATER = registeritem("lesser_resource_water",
                new ResourcePotionItem(new Item.Settings()
                        .maxCount(16)
                        .component(ModDataComponents.RESOURCE_RESTORE, ResourceComponents.LESSER_RESOURCE)));

        RESOURCE_WATER = registeritem("resource_water",
                new ResourcePotionItem(new Item.Settings()
                        .maxCount(16)
                        .component(ModDataComponents.RESOURCE_RESTORE, ResourceComponents.STANDARD_RESOURCE)));

        GREATER_RESOURCE_WATER = registeritem("greater_resource_water",
                new ResourcePotionItem(new Item.Settings()
                        .maxCount(16)
                        .component(ModDataComponents.RESOURCE_RESTORE, ResourceComponents.GREATER_RESOURCE)));

        SUPERIOR_RESOURCE_WATER = registeritem("superior_resource_water",
                new ResourcePotionItem(new Item.Settings()
                        .maxCount(16)
                        .component(ModDataComponents.RESOURCE_RESTORE, ResourceComponents.SUPERIOR_RESOURCE)));

        ULTIMATE_RESOURCE_WATER = registeritem("ultimate_resource_water",
                new ResourcePotionItem(new Item.Settings()
                        .maxCount(16)
                        .component(ModDataComponents.RESOURCE_RESTORE, ResourceComponents.ULTIMATE_RESOURCE)));

        NOVICE_DAGGER = registerCustomRenderItem("novice_dagger", new DaggerItem(ToolMaterials.IRON, new Item.Settings().attributeModifiers(DaggerItem.createAttributeModifiers(ToolMaterials.IRON, -1.0F, -1.0F))));
        DARKSTEEL_MACE = registerCustomRenderItem("darksteel_mace",
                new MaceItem(
                        new Item.Settings()
                                .rarity(Rarity.UNCOMMON)
                                .maxDamage(450)
                                .component(DataComponentTypes.TOOL, MaceItem.createToolComponent())
                                .attributeModifiers(MaceItem.createAttributeModifiers())
                )
        );
    }
    public static <T extends Item> T registeritem(String name, T item) {
        Registry.register(Registries.ITEM, Mamy.id(name), item);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK)
                .register(entries -> {
                    entries.add(CHILLING_LIGHT_WATER);
                    entries.add(THERMAL_ESSENCE);
                    entries.add(POCKET_RESOURCE_WATER);
                    entries.add(LESSER_RESOURCE_WATER);
                    entries.add(RESOURCE_WATER);
                    entries.add(GREATER_RESOURCE_WATER);
                    entries.add(SUPERIOR_RESOURCE_WATER);
                    entries.add(ULTIMATE_RESOURCE_WATER);
                    entries.add(HOLY_WATER);
                });
        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> {
            builder.registerItemRecipe(
                    Items.POTION,
                    Ingredient.ofItems(Items.SNOWBALL),
                    ModItems.CHILLING_LIGHT_WATER);
            builder.registerItemRecipe(
                    Items.POTION,
                    Ingredient.ofItems(Items.FIRE_CHARGE),
                    ModItems.THERMAL_ESSENCE);
        });
        return item;
    }
    public static <T extends Item> T registerCustomRenderItem(String name, T item) {
        Registry.register(Registries.ITEM, Mamy.id(name), item);
        CUSTOM_RENDER.add(item);
        return item;
    }
}
