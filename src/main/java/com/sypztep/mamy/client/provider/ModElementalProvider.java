package com.sypztep.mamy.client.provider;

import com.sypztep.mamy.client.util.MamyCodecDataProvider;
import com.sypztep.mamy.common.component.item.ElementalComponent;
import com.sypztep.mamy.common.system.damage.CombatType;
import com.sypztep.mamy.common.system.damage.ElementType;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class ModElementalProvider extends MamyCodecDataProvider<ElementalComponent> {

    public ModElementalProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(dataOutput, registriesFuture, ElementalComponent.RESOURCE_LOCATION, ElementalComponent.CODEC);
    }

    @Override
    protected void configure(BiConsumer<Identifier, ElementalComponent> provider, RegistryWrapper.WrapperLookup lookup) {
        // ============================================================================
        // WEAPONS - Various elemental and combat combinations
        // ============================================================================

        // Basic Weapons - Pure combat types
        provider.accept(Identifier.ofVanilla("wooden_sword"),
                weapon()
                        .elemental(ElementType.PHYSICAL, 1.0)
                        .combat(CombatType.MELEE, 1.0)
                        .build());

        provider.accept(Identifier.ofVanilla("stone_sword"),
                weapon()
                        .elemental(ElementType.PHYSICAL, 1.0)
                        .combat(CombatType.MELEE, 1.0)
                        .build());

        provider.accept(Identifier.ofVanilla("iron_sword"),
                weapon()
                        .elemental(ElementType.PHYSICAL, 1.0)
                        .combat(CombatType.MELEE, 1.0)
                        .build());

        provider.accept(Identifier.ofVanilla("golden_sword"),
                weapon()
                        .elemental(ElementType.PHYSICAL, 1.0)
                        .combat(CombatType.MELEE, 1.0)
                        .build());

        provider.accept(Identifier.ofVanilla("diamond_sword"),
                weapon()
                        .elemental(ElementType.PHYSICAL, 1.0)
                        .combat(CombatType.MELEE, 1.0)
                        .build());

        provider.accept(Identifier.ofVanilla("netherite_sword"),
                weapon()
                        .elemental(ElementType.PHYSICAL, 0.85f)
                        .elemental(ElementType.FIRE, 0.15)
                        .combat(CombatType.MELEE, 1.0)
                        .build());

        // Ranged Weapons
        provider.accept(Identifier.ofVanilla("bow"),
                weapon()
                        .elemental(ElementType.PHYSICAL, 1)
                        .combat(CombatType.RANGED, 1.0)
                        .build());

        provider.accept(Identifier.ofVanilla("crossbow"),
                weapon()
                        .elemental(ElementType.PHYSICAL, 1)
                        .combat(CombatType.RANGED, 1.0)
                        .powerBudget(1.2)
                        .build());

        provider.accept(Identifier.ofVanilla("trident"),
                weapon()
                        .elemental(ElementType.PHYSICAL, 0.6)
                        .elemental(ElementType.WATER, 0.3)
                        .elemental(ElementType.ELECTRIC, 0.1)
                        .combat(CombatType.MELEE, 0.7)
                        .combat(CombatType.RANGED, 0.3)
                        .build());

        // ============================================================================
        // ARMOR SETS - Defensive configurations
        // ============================================================================

        // Leather Armor - Basic protection
        provider.accept(Identifier.ofVanilla("leather_helmet"),
                armor()
                        .elemental("physical", 0.05)
                        .elemental("cold", 0.10)
                        .combat("melee", 0.05)
                        .powerBudget(0.8)
                        .build());

        provider.accept(Identifier.ofVanilla("leather_chestplate"),
                armor()
                        .elemental("physical", 0.08)
                        .elemental("cold", 0.15)
                        .combat("melee", 0.08)
                        .powerBudget(0.8)
                        .build());

        provider.accept(Identifier.ofVanilla("leather_leggings"),
                armor()
                        .elemental("physical", 0.06)
                        .elemental("cold", 0.12)
                        .combat("melee", 0.06)
                        .powerBudget(0.8)
                        .build());

        provider.accept(Identifier.ofVanilla("leather_boots"),
                armor()
                        .elemental("physical", 0.04)
                        .elemental("cold", 0.08)
                        .elemental("water", 0.05)
                        .combat("melee", 0.04)
                        .powerBudget(0.8)
                        .build());

        // Iron Armor - Balanced protection
        provider.accept(Identifier.ofVanilla("iron_helmet"),
                armor()
                        .elemental("physical", 0.10)
                        .elemental("fire", 0.08)
                        .elemental("electric", 0.05)
                        .combat("melee", 0.12)
                        .combat("ranged", 0.05)
                        .powerBudget(1.0)
                        .build());

        provider.accept(Identifier.ofVanilla("iron_chestplate"),
                armor()
                        .elemental("physical", 0.18)
                        .elemental("fire", 0.15)
                        .elemental("electric", 0.08)
                        .combat("melee", 0.20)
                        .combat("ranged", 0.08)
                        .powerBudget(1.0)
                        .build());

        provider.accept(Identifier.ofVanilla("iron_leggings"),
                armor()
                        .elemental("physical", 0.15)
                        .elemental("fire", 0.12)
                        .elemental("electric", 0.06)
                        .combat("melee", 0.18)
                        .combat("ranged", 0.06)
                        .powerBudget(1.0)
                        .build());

        provider.accept(Identifier.ofVanilla("iron_boots"),
                armor()
                        .elemental("physical", 0.08)
                        .elemental("fire", 0.06)
                        .elemental("water", 0.10)
                        .combat("melee", 0.10)
                        .powerBudget(1.0)
                        .build());

        // Diamond Armor - High-tier protection
        provider.accept(Identifier.ofVanilla("diamond_helmet"),
                armor()
                        .elemental("physical", 0.15)
                        .elemental("fire", 0.20)
                        .elemental("cold", 0.10)
                        .elemental("electric", 0.05)
                        .combat("melee", 0.20)
                        .combat("magic", 0.10)
                        .powerBudget(1.2)
                        .combatWeight(0.8)
                        .build());

        provider.accept(Identifier.ofVanilla("diamond_chestplate"),
                armor()
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

        provider.accept(Identifier.ofVanilla("diamond_leggings"),
                armor()
                        .elemental("physical", 0.20)
                        .elemental("fire", 0.25)
                        .elemental("cold", 0.10)
                        .elemental("wind", 0.10)
                        .combat("melee", 0.25)
                        .combat("ranged", 0.10)
                        .powerBudget(1.2)
                        .combatWeight(0.9)
                        .build());

        provider.accept(Identifier.ofVanilla("diamond_boots"),
                armor()
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
        provider.accept(Identifier.ofVanilla("netherite_helmet"),
                armor()
                        .elemental("physical", 0.18)
                        .elemental("fire", 0.35)
                        .elemental("electric", 0.08)
                        .elemental("holy", 0.05)
                        .combat("melee", 0.25)
                        .combat("magic", 0.15)
                        .powerBudget(1.4)
                        .combatWeight(0.9)
                        .build());

        provider.accept(Identifier.ofVanilla("netherite_chestplate"),
                armor()
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

        provider.accept(Identifier.ofVanilla("netherite_leggings"),
                armor()
                        .elemental("physical", 0.25)
                        .elemental("fire", 0.40)
                        .elemental("electric", 0.12)
                        .elemental("wind", 0.08)
                        .combat("melee", 0.30)
                        .combat("ranged", 0.15)
                        .powerBudget(1.4)
                        .combatWeight(1.0)
                        .build());

        provider.accept(Identifier.ofVanilla("netherite_boots"),
                armor()
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
//        provider.accept(Mamy.id("fire_staff"),
//                weapon()
//                        .elemental("fire", 1.0)
//                        .combat("magic", 1.0)
//                        .powerBudget(1.3)
//                        .combatWeight(0.9)
//                        .build());
//
//        // Example: Frost Shield (Armor accessory)
//        provider.accept(Mamy.id("frost_shield"),
//                armor()
//                        .elemental("cold", 0.50)
//                        .elemental("water", 0.30)
//                        .elemental("physical", 0.20)
//                        .combat("melee", 0.40)
//                        .combat("ranged", 0.60)
//                        .powerBudget(1.1)
//                        .combatWeight(0.8)
//                        .build());
//
//        // Example: Lightning Bow (Hybrid ranged weapon)
//        provider.accept(Mamy.id("lightning_bow"),
//                weapon()
//                        .elemental("physical", 0.4)
//                        .elemental("electric", 0.6)
//                        .combat("ranged", 0.8)
//                        .combat("magic", 0.2)
//                        .powerBudget(1.25)
//                        .combatWeight(0.85)
//                        .build());
    }

    @Override
    public String getName() {
        return "Elemental Data";
    }

    // ============================================================================
    // DATA STRUCTURE AND CODEC
    // ============================================================================
    public static ModElementalProvider.Builder weapon() {
        return new ModElementalProvider.Builder(false);
    }

    public static ModElementalProvider.Builder armor() {
        return new ModElementalProvider.Builder(true);
    }

    public static class Builder {
        private final Map<String, Double> elementalRatios = new HashMap<>();
        private final Map<String, Double> combatRatios = new HashMap<>();
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

        public Builder elemental(ElementType element, double value) {
            if (isArmor || value > 0) {
                elementalRatios.put(element.name, value);
            }
            return this;
        }

        public Builder combat(CombatType combat, double value) {
            if (isArmor || value > 0) {
                combatRatios.put(combat.name, value);
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

        public ElementalComponent build() {
            return new ElementalComponent(
                    Map.copyOf(elementalRatios),
                    Map.copyOf(combatRatios),
                    powerBudget,
                    combatWeight
            );
        }
    }
}