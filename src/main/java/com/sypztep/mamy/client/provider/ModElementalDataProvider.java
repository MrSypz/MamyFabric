package com.sypztep.mamy.client.provider;

import com.google.gson.JsonObject;
import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ModElementalDataProvider implements DataProvider {

    private final FabricDataOutput output;

    public ModElementalDataProvider(FabricDataOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        return generateElementalData(writer);
    }

    private CompletableFuture<?> generateElementalData(DataWriter writer) {
        CompletableFuture<?>[] futures = new CompletableFuture[]{
                // === VANILLA WEAPONS ===
                // Swords
                createWeaponEntry(writer, Items.WOODEN_SWORD, 0.8, physicalOnly()),
                createWeaponEntry(writer, Items.STONE_SWORD, 0.9, physicalOnly()),
                createWeaponEntry(writer, Items.IRON_SWORD, 1.0, physicalOnly()),
                createWeaponEntry(writer, Items.GOLDEN_SWORD, 1.2, Map.of(
                        ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT, 0.7,
                        ModEntityAttributes.HOLY_ATTACK_DAMAGE_FLAT, 0.3
                )),
                createWeaponEntry(writer, Items.DIAMOND_SWORD, 1.1, physicalOnly()),
                createWeaponEntry(writer, Items.NETHERITE_SWORD, 1.3, Map.of(
                        ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT, 0.8,
                        ModEntityAttributes.FIRE_ATTACK_DAMAGE_FLAT, 0.2
                )),

                // Axes
                createWeaponEntry(writer, Items.WOODEN_AXE, 0.7, physicalOnly()),
                createWeaponEntry(writer, Items.STONE_AXE, 0.8, physicalOnly()),
                createWeaponEntry(writer, Items.IRON_AXE, 0.9, physicalOnly()),
                createWeaponEntry(writer, Items.GOLDEN_AXE, 1.1, Map.of(
                        ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT, 0.7,
                        ModEntityAttributes.HOLY_ATTACK_DAMAGE_FLAT, 0.3
                )),
                createWeaponEntry(writer, Items.DIAMOND_AXE, 1.0, physicalOnly()),
                createWeaponEntry(writer, Items.NETHERITE_AXE, 1.2, Map.of(
                        ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT, 0.8,
                        ModEntityAttributes.FIRE_ATTACK_DAMAGE_FLAT, 0.2
                )),

                // Pickaxes
                createWeaponEntry(writer, Items.WOODEN_PICKAXE, 0.6, physicalOnly()),
                createWeaponEntry(writer, Items.STONE_PICKAXE, 0.7, physicalOnly()),
                createWeaponEntry(writer, Items.IRON_PICKAXE, 0.8, physicalOnly()),
                createWeaponEntry(writer, Items.GOLDEN_PICKAXE, 1.0, Map.of(
                        ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT, 0.7,
                        ModEntityAttributes.HOLY_ATTACK_DAMAGE_FLAT, 0.3
                )),
                createWeaponEntry(writer, Items.DIAMOND_PICKAXE, 0.9, physicalOnly()),
                createWeaponEntry(writer, Items.NETHERITE_PICKAXE, 1.1, Map.of(
                        ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT, 0.8,
                        ModEntityAttributes.FIRE_ATTACK_DAMAGE_FLAT, 0.2
                )),

                // Shovels
                createWeaponEntry(writer, Items.WOODEN_SHOVEL, 0.5, physicalOnly()),
                createWeaponEntry(writer, Items.STONE_SHOVEL, 0.6, physicalOnly()),
                createWeaponEntry(writer, Items.IRON_SHOVEL, 0.7, physicalOnly()),
                createWeaponEntry(writer, Items.GOLDEN_SHOVEL, 0.9, Map.of(
                        ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT, 0.7,
                        ModEntityAttributes.HOLY_ATTACK_DAMAGE_FLAT, 0.3
                )),
                createWeaponEntry(writer, Items.DIAMOND_SHOVEL, 0.8, physicalOnly()),
                createWeaponEntry(writer, Items.NETHERITE_SHOVEL, 1.0, Map.of(
                        ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT, 0.8,
                        ModEntityAttributes.FIRE_ATTACK_DAMAGE_FLAT, 0.2
                )),

                // Hoes
                createWeaponEntry(writer, Items.WOODEN_HOE, 0.4, physicalOnly()),
                createWeaponEntry(writer, Items.STONE_HOE, 0.5, physicalOnly()),
                createWeaponEntry(writer, Items.IRON_HOE, 0.6, physicalOnly()),
                createWeaponEntry(writer, Items.GOLDEN_HOE, 0.8, Map.of(
                        ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT, 0.7,
                        ModEntityAttributes.HOLY_ATTACK_DAMAGE_FLAT, 0.3
                )),
                createWeaponEntry(writer, Items.DIAMOND_HOE, 0.7, physicalOnly()),
                createWeaponEntry(writer, Items.NETHERITE_HOE, 0.9, Map.of(
                        ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT, 0.8,
                        ModEntityAttributes.FIRE_ATTACK_DAMAGE_FLAT, 0.2
                )),

                // Special weapons
                createWeaponEntry(writer, Items.TRIDENT, 1.2, Map.of(
                        ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT, 0.6,
                        ModEntityAttributes.WATER_ATTACK_DAMAGE_FLAT, 0.4
                )),
                createWeaponEntry(writer, Items.MACE, 1.5, Map.of(
                        ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT, 0.7,
                        ModEntityAttributes.WIND_ATTACK_DAMAGE_FLAT, 0.3
                )),

                // Ranged weapons
                createWeaponEntry(writer, Items.BOW, 1.0, physicalOnly()),
                createWeaponEntry(writer, Items.CROSSBOW, 1.1, physicalOnly()),

                // === VANILLA ARMOR ===
                // Leather armor
                createArmorEntry(writer, Items.LEATHER_HELMET, 0.8, physicalOnly()),
                createArmorEntry(writer, Items.LEATHER_CHESTPLATE, 0.8, physicalOnly()),
                createArmorEntry(writer, Items.LEATHER_LEGGINGS, 0.8, physicalOnly()),
                createArmorEntry(writer, Items.LEATHER_BOOTS, 0.8, physicalOnly()),

                // Chainmail armor
                createArmorEntry(writer, Items.CHAINMAIL_HELMET, 0.9, physicalOnly()),
                createArmorEntry(writer, Items.CHAINMAIL_CHESTPLATE, 0.9, physicalOnly()),
                createArmorEntry(writer, Items.CHAINMAIL_LEGGINGS, 0.9, physicalOnly()),
                createArmorEntry(writer, Items.CHAINMAIL_BOOTS, 0.9, physicalOnly()),

                // Iron armor
                createArmorEntry(writer, Items.IRON_HELMET, 1.0, physicalOnly()),
                createArmorEntry(writer, Items.IRON_CHESTPLATE, 1.0, physicalOnly()),
                createArmorEntry(writer, Items.IRON_LEGGINGS, 1.0, physicalOnly()),
                createArmorEntry(writer, Items.IRON_BOOTS, 1.0, physicalOnly()),

                // Golden armor (holy resistance)
                createArmorEntry(writer, Items.GOLDEN_HELMET, 1.1, Map.of(
                        ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT, 0.7,
                        ModEntityAttributes.HOLY_ATTACK_DAMAGE_FLAT, 0.3
                )),
                createArmorEntry(writer, Items.GOLDEN_CHESTPLATE, 1.1, Map.of(
                        ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT, 0.7,
                        ModEntityAttributes.HOLY_ATTACK_DAMAGE_FLAT, 0.3
                )),
                createArmorEntry(writer, Items.GOLDEN_LEGGINGS, 1.1, Map.of(
                        ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT, 0.7,
                        ModEntityAttributes.HOLY_ATTACK_DAMAGE_FLAT, 0.3
                )),
                createArmorEntry(writer, Items.GOLDEN_BOOTS, 1.1, Map.of(
                        ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT, 0.7,
                        ModEntityAttributes.HOLY_ATTACK_DAMAGE_FLAT, 0.3
                )),

                // Diamond armor
                createArmorEntry(writer, Items.DIAMOND_HELMET, 1.2, physicalOnly()),
                createArmorEntry(writer, Items.DIAMOND_CHESTPLATE, 1.2, physicalOnly()),
                createArmorEntry(writer, Items.DIAMOND_LEGGINGS, 1.2, physicalOnly()),
                createArmorEntry(writer, Items.DIAMOND_BOOTS, 1.2, physicalOnly()),

                // Netherite armor (fire resistance)
                createArmorEntry(writer, Items.NETHERITE_HELMET, 1.5, Map.of(
                        ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT, 0.8,
                        ModEntityAttributes.FIRE_ATTACK_DAMAGE_FLAT, 0.2
                )),
                createArmorEntry(writer, Items.NETHERITE_CHESTPLATE, 1.5, Map.of(
                        ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT, 0.8,
                        ModEntityAttributes.FIRE_ATTACK_DAMAGE_FLAT, 0.2
                )),
                createArmorEntry(writer, Items.NETHERITE_LEGGINGS, 1.5, Map.of(
                        ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT, 0.8,
                        ModEntityAttributes.FIRE_ATTACK_DAMAGE_FLAT, 0.2
                )),
                createArmorEntry(writer, Items.NETHERITE_BOOTS, 1.5, Map.of(
                        ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT, 0.8,
                        ModEntityAttributes.FIRE_ATTACK_DAMAGE_FLAT, 0.2
                )),

                // Special armor
                createArmorEntry(writer, Items.TURTLE_HELMET, 1.0, Map.of(
                        ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT, 0.6,
                        ModEntityAttributes.WATER_ATTACK_DAMAGE_FLAT, 0.4
                )),

                // === EXAMPLE ELEMENTAL WEAPONS ===
                // Fire themed
                createWeaponEntry(writer, Items.BLAZE_ROD, 1.3, Map.of(
                        ModEntityAttributes.FIRE_ATTACK_DAMAGE_FLAT, 0.8,
                        ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT, 0.2
                )),

                // Ice themed (using prismarine items as example)
                createWeaponEntry(writer, Items.PRISMARINE_SHARD, 1.1, Map.of(
                        ModEntityAttributes.COLD_DAMAGE_FLAT, 0.6,
                        ModEntityAttributes.WATER_ATTACK_DAMAGE_FLAT, 0.4
                )),

                // Electric themed
                createWeaponEntry(writer, Items.END_ROD, 1.2, Map.of(
                        ModEntityAttributes.ELECTRIC_ATTACK_DAMAGE_FLAT, 0.7,
                        ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT, 0.3
                )),

                // Wind themed
                createWeaponEntry(writer, Items.FEATHER, 0.9, Map.of(
                        ModEntityAttributes.WIND_ATTACK_DAMAGE_FLAT, 0.8,
                        ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT, 0.2
                )),

                // Holy themed
                createWeaponEntry(writer, Items.NETHER_STAR, 2.0, Map.of(
                        ModEntityAttributes.HOLY_ATTACK_DAMAGE_FLAT, 0.6,
                        ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT, 0.4
                ))
        };

        return CompletableFuture.allOf(futures);
    }

    // Helper method for pure physical damage/resistance
    private Map<?, Double> physicalOnly() {
        return Map.of(ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT, 1.0);
    }

    // Weapon entry creator
    private CompletableFuture<?> createWeaponEntry(DataWriter writer, Item item, double powerBudget, Map<?, Double> damageRatios) {
        return createElementalFile(writer, item, powerBudget, damageRatios);
    }

    // Armor entry creator
    private CompletableFuture<?> createArmorEntry(DataWriter writer, Item item, double resistanceBudget, Map<?, Double> resistanceRatios) {
        return createElementalFile(writer, item, resistanceBudget, resistanceRatios);
    }

    // Generic elemental entry creator
    private CompletableFuture<?> createElementalEntry(DataWriter writer, Item item, double budget, Map<?, Double> ratios) {
        return createElementalFile(writer, item, budget, ratios);
    }

    private CompletableFuture<?> createElementalFile(DataWriter writer, Item item, double powerBudget, Map<?, Double> damageRatios) {
        Identifier itemId = Registries.ITEM.getId(item);
        String namespace = itemId.getNamespace();
        String path = itemId.getPath();

        // Create the file path: data/mamy/elementator/namespace/path.json
        Path filePath = this.output.getPath()
                .resolve("data")
                .resolve(Mamy.MODID)
                .resolve("elementator")
                .resolve(namespace)
                .resolve(path + ".json");

        JsonObject jsonObject = createJsonObject(powerBudget, damageRatios);

        return DataProvider.writeToPath(writer, jsonObject, filePath);
    }

    private JsonObject createJsonObject(double powerBudget, Map<?, Double> damageRatios) {
        JsonObject root = new JsonObject();
        root.addProperty("powerBudget", powerBudget);

        JsonObject ratios = new JsonObject();
        damageRatios.forEach((attribute, ratio) -> {
            String attributeName = getAttributeName(attribute);
            ratios.addProperty(attributeName, ratio);
        });

        root.add("damageRatios", ratios);
        return root;
    }

    private String getAttributeName(Object attribute) {
        // Convert attribute to string name for JSON
        if (attribute == ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT) {
            return "mamy:melee_attack_damage_flat";
        } else if (attribute == ModEntityAttributes.FIRE_ATTACK_DAMAGE_FLAT) {
            return "mamy:fire_attack_damage_flat";
        } else if (attribute == ModEntityAttributes.COLD_DAMAGE_FLAT) {
            return "mamy:cold_damage_flat";
        } else if (attribute == ModEntityAttributes.ELECTRIC_ATTACK_DAMAGE_FLAT) {
            return "mamy:electric_attack_damage_flat";
        } else if (attribute == ModEntityAttributes.WATER_ATTACK_DAMAGE_FLAT) {
            return "mamy:water_attack_damage_flat";
        } else if (attribute == ModEntityAttributes.WIND_ATTACK_DAMAGE_FLAT) {
            return "mamy:wind_attack_damage_flat";
        } else if (attribute == ModEntityAttributes.HOLY_ATTACK_DAMAGE_FLAT) {
            return "mamy:holy_attack_damage_flat";
        }
        return "unknown";
    }

    @Override
    public String getName() {
        return "Elemental Item Data";
    }
}