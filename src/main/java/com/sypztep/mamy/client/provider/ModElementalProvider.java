package com.sypztep.mamy.client.provider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sypztep.mamy.Mamy;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricCodecDataProvider;
import net.minecraft.data.DataOutput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class ModElementalProvider extends FabricCodecDataProvider<ModElementalProvider.ElementalData> {

    public ModElementalProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(dataOutput, registriesFuture, DataOutput.OutputType.DATA_PACK, "elementator", ElementalData.CODEC);
    }

    @Override
    protected void configure(BiConsumer<Identifier, ElementalData> provider, RegistryWrapper.WrapperLookup lookup) {
        // ============================================================================
        // WEAPONS - Various elemental and combat combinations
        // ============================================================================

        // Basic Weapons - Pure combat types
        provider.accept(Identifier.of("minecraft", "wooden_sword"),
                ElementalData.weapon()
                        .elemental("physical", 1.0)
                        .combat("melee", 1.0)
                        .build());

        provider.accept(Identifier.of("minecraft", "stone_sword"),
                ElementalData.weapon()
                        .elemental("physical", 1.0)
                        .combat("melee", 1.0)
                        .powerBudget(1.1)
                        .build());

        provider.accept(Identifier.of("minecraft", "iron_sword"),
                ElementalData.weapon()
                        .elemental("physical", 1.0)
                        .combat("melee", 1.0)
                        .powerBudget(1.2)
                        .build());

        // Diamond Sword - Multi-elemental
        provider.accept(Identifier.of("minecraft", "diamond_sword"),
                ElementalData.weapon()
                        .elemental("physical", 0.6)
                        .elemental("holy", 0.2)
                        .elemental("cold", 0.1)
                        .elemental("fire", 0.1)
                        .combat("melee", 1.0)
                        .powerBudget(1.3)
                        .build());

        // Netherite Sword - Fire-based
        provider.accept(Identifier.of("minecraft", "netherite_sword"),
                ElementalData.weapon()
                        .elemental("physical", 0.5)
                        .elemental("fire", 0.5)
                        .combat("melee", 1.0)
                        .powerBudget(1.4)
                        .build());

        // Ranged Weapons
        provider.accept(Identifier.of("minecraft", "bow"),
                ElementalData.weapon()
                        .elemental("physical", 0.8)
                        .elemental("wind", 0.2)
                        .combat("ranged", 1.0)
                        .powerBudget(1.0)
                        .combatWeight(0.9)
                        .build());

        provider.accept(Identifier.of("minecraft", "crossbow"),
                ElementalData.weapon()
                        .elemental("physical", 1.0)
                        .combat("ranged", 1.0)
                        .powerBudget(1.1)
                        .build());

        // Magic Weapons (if you have them)
        provider.accept(Identifier.of("minecraft", "trident"),
                ElementalData.weapon()
                        .elemental("physical", 0.6)
                        .elemental("water", 0.3)
                        .elemental("electric", 0.1)
                        .combat("melee", 0.7)
                        .combat("ranged", 0.3)
                        .powerBudget(1.2)
                        .combatWeight(0.8)
                        .build());

        // ============================================================================
        // ARMOR SETS - Defensive configurations
        // ============================================================================

        // Leather Armor - Basic protection
        provider.accept(Identifier.of("minecraft", "leather_helmet"),
                ElementalData.armor()
                        .elemental("physical", 0.05)
                        .elemental("cold", 0.10)
                        .combat("melee", 0.05)
                        .powerBudget(0.8)
                        .build());

        provider.accept(Identifier.of("minecraft", "leather_chestplate"),
                ElementalData.armor()
                        .elemental("physical", 0.08)
                        .elemental("cold", 0.15)
                        .combat("melee", 0.08)
                        .powerBudget(0.8)
                        .build());

        provider.accept(Identifier.of("minecraft", "leather_leggings"),
                ElementalData.armor()
                        .elemental("physical", 0.06)
                        .elemental("cold", 0.12)
                        .combat("melee", 0.06)
                        .powerBudget(0.8)
                        .build());

        provider.accept(Identifier.of("minecraft", "leather_boots"),
                ElementalData.armor()
                        .elemental("physical", 0.04)
                        .elemental("cold", 0.08)
                        .elemental("water", 0.05)
                        .combat("melee", 0.04)
                        .powerBudget(0.8)
                        .build());

        // Iron Armor - Balanced protection
        provider.accept(Identifier.of("minecraft", "iron_helmet"),
                ElementalData.armor()
                        .elemental("physical", 0.10)
                        .elemental("fire", 0.08)
                        .elemental("electric", 0.05)
                        .combat("melee", 0.12)
                        .combat("ranged", 0.05)
                        .powerBudget(1.0)
                        .build());

        provider.accept(Identifier.of("minecraft", "iron_chestplate"),
                ElementalData.armor()
                        .elemental("physical", 0.18)
                        .elemental("fire", 0.15)
                        .elemental("electric", 0.08)
                        .combat("melee", 0.20)
                        .combat("ranged", 0.08)
                        .powerBudget(1.0)
                        .build());

        provider.accept(Identifier.of("minecraft", "iron_leggings"),
                ElementalData.armor()
                        .elemental("physical", 0.15)
                        .elemental("fire", 0.12)
                        .elemental("electric", 0.06)
                        .combat("melee", 0.18)
                        .combat("ranged", 0.06)
                        .powerBudget(1.0)
                        .build());

        provider.accept(Identifier.of("minecraft", "iron_boots"),
                ElementalData.armor()
                        .elemental("physical", 0.08)
                        .elemental("fire", 0.06)
                        .elemental("water", 0.10)
                        .combat("melee", 0.10)
                        .powerBudget(1.0)
                        .build());

        // Diamond Armor - High-tier protection
        provider.accept(Identifier.of("minecraft", "diamond_helmet"),
                ElementalData.armor()
                        .elemental("physical", 0.15)
                        .elemental("fire", 0.20)
                        .elemental("cold", 0.10)
                        .elemental("electric", 0.05)
                        .combat("melee", 0.20)
                        .combat("magic", 0.10)
                        .powerBudget(1.2)
                        .combatWeight(0.8)
                        .build());

        provider.accept(Identifier.of("minecraft", "diamond_chestplate"),
                ElementalData.armor()
                        .elemental("physical", 0.25)
                        .elemental("fire", 0.30)
                        .elemental("cold", 0.15)
                        .elemental("electric", 0.10)
                        .elemental("holy", 0.05)
                        .combat("melee", 0.30)
                        .combat("ranged", 0.15)
                        .combat("magic", 0.20)
                        .powerBudget(1.2)
                        .combatWeight(1.0)
                        .build());

        provider.accept(Identifier.of("minecraft", "diamond_leggings"),
                ElementalData.armor()
                        .elemental("physical", 0.20)
                        .elemental("fire", 0.25)
                        .elemental("cold", 0.10)
                        .elemental("wind", 0.10)
                        .combat("melee", 0.25)
                        .combat("ranged", 0.10)
                        .powerBudget(1.2)
                        .combatWeight(0.9)
                        .build());

        provider.accept(Identifier.of("minecraft", "diamond_boots"),
                ElementalData.armor()
                        .elemental("physical", 0.10)
                        .elemental("fire", 0.15)
                        .elemental("cold", 0.20)
                        .elemental("water", 0.15)
                        .combat("melee", 0.15)
                        .combat("magic", 0.05)
                        .powerBudget(1.2)
                        .combatWeight(0.7)
                        .build());

        // Netherite Armor - Ultimate protection with fire immunity
        provider.accept(Identifier.of("minecraft", "netherite_helmet"),
                ElementalData.armor()
                        .elemental("physical", 0.18)
                        .elemental("fire", 0.35)
                        .elemental("electric", 0.08)
                        .elemental("holy", 0.05)
                        .combat("melee", 0.25)
                        .combat("magic", 0.15)
                        .powerBudget(1.4)
                        .combatWeight(0.9)
                        .build());

        provider.accept(Identifier.of("minecraft", "netherite_chestplate"),
                ElementalData.armor()
                        .elemental("physical", 0.30)
                        .elemental("fire", 0.50)
                        .elemental("cold", 0.10)
                        .elemental("electric", 0.15)
                        .elemental("holy", 0.10)
                        .combat("melee", 0.35)
                        .combat("ranged", 0.20)
                        .combat("magic", 0.25)
                        .powerBudget(1.4)
                        .combatWeight(1.1)
                        .build());

        provider.accept(Identifier.of("minecraft", "netherite_leggings"),
                ElementalData.armor()
                        .elemental("physical", 0.25)
                        .elemental("fire", 0.40)
                        .elemental("electric", 0.12)
                        .elemental("wind", 0.08)
                        .combat("melee", 0.30)
                        .combat("ranged", 0.15)
                        .powerBudget(1.4)
                        .combatWeight(1.0)
                        .build());

        provider.accept(Identifier.of("minecraft", "netherite_boots"),
                ElementalData.armor()
                        .elemental("physical", 0.15)
                        .elemental("fire", 0.30)
                        .elemental("cold", 0.10)
                        .elemental("water", 0.10)
                        .elemental("electric", 0.08)
                        .combat("melee", 0.20)
                        .combat("magic", 0.10)
                        .powerBudget(1.4)
                        .combatWeight(0.8)
                        .build());

        // ============================================================================
        // SPECIAL/UNIQUE ITEMS - Creative examples
        // ============================================================================

        // Example: Pure Fire Staff (Magic weapon)
        provider.accept(Mamy.id("fire_staff"),
                ElementalData.weapon()
                        .elemental("fire", 1.0)
                        .combat("magic", 1.0)
                        .powerBudget(1.3)
                        .combatWeight(0.9)
                        .build());

        // Example: Frost Shield (Armor accessory)
        provider.accept(Mamy.id("frost_shield"),
                ElementalData.armor()
                        .elemental("cold", 0.50)
                        .elemental("water", 0.30)
                        .elemental("physical", 0.20)
                        .combat("melee", 0.40)
                        .combat("ranged", 0.60)
                        .powerBudget(1.1)
                        .combatWeight(0.8)
                        .build());

        // Example: Lightning Bow (Hybrid ranged weapon)
        provider.accept(Mamy.id("lightning_bow"),
                ElementalData.weapon()
                        .elemental("physical", 0.4)
                        .elemental("electric", 0.6)
                        .combat("ranged", 0.8)
                        .combat("magic", 0.2)
                        .powerBudget(1.25)
                        .combatWeight(0.85)
                        .build());
    }

    @Override
    public String getName() {
        return "Elemental Data";
    }

    // ============================================================================
    // DATA STRUCTURE AND CODEC
    // ============================================================================

    public record ElementalData(
            Map<String, Double> elementalRatios,
            Map<String, Double> combatRatios,
            double powerBudget,
            double combatWeight
    ) {
        public static final Codec<ElementalData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).fieldOf("elementalRatios").forGetter(ElementalData::elementalRatios),
                Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).optionalFieldOf("combatRatios", Map.of()).forGetter(ElementalData::combatRatios),
                Codec.DOUBLE.optionalFieldOf("powerBudget", 1.0).forGetter(ElementalData::powerBudget),
                Codec.DOUBLE.optionalFieldOf("combatWeight", 1.0).forGetter(ElementalData::combatWeight)
        ).apply(instance, ElementalData::new));

        public static Builder weapon() {
            return new Builder(false);
        }

        public static Builder armor() {
            return new Builder(true);
        }
    }

    public static class Builder {
        private final Map<String, Double> elementalRatios = new java.util.HashMap<>();
        private final Map<String, Double> combatRatios = new java.util.HashMap<>();
        private double powerBudget = 1.0;
        private double combatWeight = 1.0;
        private final boolean isArmor;

        private Builder(boolean isArmor) {
            this.isArmor = isArmor;
        }

        public Builder elemental(String element, double value) {
            if (isArmor || value > 0) {
                elementalRatios.put(element, value);
            }
            return this;
        }

        public Builder combat(String combat, double value) {
            if (isArmor || value > 0) {
                combatRatios.put(combat, value);
            }
            return this;
        }

        public Builder powerBudget(double budget) {
            this.powerBudget = budget;
            return this;
        }

        public Builder combatWeight(double weight) {
            this.combatWeight = weight;
            return this;
        }

        public ElementalData build() {
            return new ElementalData(
                    Map.copyOf(elementalRatios),
                    Map.copyOf(combatRatios),
                    powerBudget,
                    combatWeight
            );
        }
    }
}