package com.sypztep.mamy.client.provider;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ModLanguageProvider extends FabricLanguageProvider {
    public ModLanguageProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, "en_us", registryLookup);
    }

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translate) {
        // Existing particle text translations
        translate.add("mamy.text.missing", "Missing");
        translate.add("mamy.text.critical", "Critical");
        translate.add("mamy.text.back", "Back Attack");
        translate.add("mamy.text.air", "Air Attack");

        translate.add("mamy.hitchance", "Hit Chance: %f%%");
        translate.add("config.jade.plugin_mamy.stats_config","Stats Config");

        playerInfo(translate);
        generateConfigTranslations(translate);
        generateAttributeTranslations(translate);
        generateElementalTranslations(translate);
        generateItemTranslations(translate);
    }

    private void generateItemTranslations(TranslationBuilder translate) {
        translate.add("item.mamy.chilling_light_water.effect.water", "Chilling Light Water");
        translate.add("item.mamy.chilling_light_water.effect.empty", "Chilling Light Water");
        translate.add("item.mamy.thermal_essence.effect.water", "Thermal Essence");
        translate.add("item.mamy.thermal_essence.effect.empty", "Thermal Essence");
        translate.add("item.mamy.holy_water.effect.water", "Holy Water");
        translate.add("item.mamy.holy_water.effect.empty", "Holy Water");

        translate.add("item.mamy.pocket_resource_water.effect.empty", "Pocket Resource Water");
        translate.add("item.mamy.lesser_resource_water.effect.empty", "Lesser Resource Water");
        translate.add("item.mamy.resource_water.effect.empty", "Resource Water");
        translate.add("item.mamy.greater_resource_water.effect.empty", "Greater Resource Water");
        translate.add("item.mamy.superior_resource_water.effect.empty", "Superior Yin Yang Water");
        translate.add("item.mamy.ultimate_resource_water.effect.empty", "Supreme Yin Yang Convergence");
    }

    private void generateElementalTranslations(TranslationBuilder translate) {
        // Weapon tooltips
        translate.add("tooltip.mamy.power_budget", "Power: %s");
        translate.add("tooltip.mamy.base_weapon", "Base Weapon");
        translate.add("tooltip.mamy.melee_bonus", "Melee Bonus");
        translate.add("tooltip.mamy.other_attributes", "Other Attributes");

        translate.add("tooltip.mamy.combat_bonuses", "Combat Bonuses");
        translate.add("tooltip.mamy.combat_resistances", "Combat Resistances");

        translate.add("combat_type.melee", "Melee");
        translate.add("combat_type.ranged", "Ranged");
        translate.add("combat_type.magic", "Magic");
        translate.add("combat_type.hybrid", "Hybrid");


        translate.add("combat_resistance.melee", "Melee Resistance");
        translate.add("combat_resistance.ranged", "Ranged Resistance");
        translate.add("combat_resistance.magic", "Magic Resistance");

        // Armor tooltips
        translate.add("tooltip.mamy.elemental_resistances", "Elemental Resistances");
        translate.add("tooltip.mamy.resistance_budget", "Resistance: %s");

        // Damage element types
        translate.add("damage_element.physical", "Physical");
        translate.add("damage_element.fire", "Fire");
        translate.add("damage_element.cold", "Cold");
        translate.add("damage_element.electric", "Electric");
        translate.add("damage_element.water", "Water");
        translate.add("damage_element.wind", "Wind");
        translate.add("damage_element.holy", "Holy");

        // Resistance element types
        translate.add("resistance_element.physical", "Physical Resistance");
        translate.add("resistance_element.fire", "Fire Resistance");
        translate.add("resistance_element.cold", "Cold Resistance");
        translate.add("resistance_element.electric", "Electric Resistance");
        translate.add("resistance_element.water", "Water Resistance");
        translate.add("resistance_element.wind", "Wind Resistance");
        translate.add("resistance_element.holy", "Holy Resistance");
    }
    private void playerInfo(TranslationBuilder translator) {
        String key = "mamy.info.";
        translator.add("mamy.gui.player_info.title", "Player Information");
        translator.add("mamy.gui.player_info.header", "Player Stats");
        translator.add("mamy.gui.player_info.header_level", "Stats");

        // ==========================================
        // SECTION HEADERS
        // ==========================================
        translator.add(key + "header_combat", "COMBAT POWER");
        translator.add(key + "header_precision", "PRECISION & CRITICAL");
        translator.add(key + "header_defense", "DEFENSIVE STATS");
        translator.add(key + "header_recovery", "REGENERATION");
        translator.add(key + "header_elemental_damage", "ELEMENTAL DAMAGE");
        translator.add(key + "header_elemental_resist", "ELEMENTAL RESISTANCES");
        translator.add(key + "header_casting", "CASTING SYSTEM");
        translator.add(key + "header_utility", "UTILITY");
        translator.add(key + "header_attributes", "STATS");
        translator.add(key + "header_classbonus", "BONUS STATS");

        // ==========================================
        // COMBAT POWER STATS
        // ==========================================
        translator.add(key + "physical", "Base Attack Power: $phyd");
        translator.add(key + "melee_damage", "Melee Damage Bonus: $meleed");
        translator.add(key + "melee_mult", "Melee Damage Multiplier: $meleem%");
        translator.add(key + "projectile_damage", "Projectile Damage Bonus: $projd");
        translator.add(key + "projectile_mult", "Projectile Damage Multiplier: $projm%");
        translator.add(key + "magic_damage", "Magic Attack Power: $mdmg");
        translator.add(key + "magic_mult", "Magic Damage Multiplier: $mdmgm%");
        translator.add(key + "attack_speed", "Attack Speed: $asp");

        // ==========================================
        // PRECISION & CRITICAL STATS
        // ==========================================
        translator.add(key + "accuracy", "Accuracy Rating: $acc");
        translator.add(key + "critical_chance", "Critical Hit Chance: $ccn%");
        translator.add(key + "critical_damage", "Critical Hit Damage: $cdmg%");
        translator.add(key + "backattack_damage", "Back Attack Damage: $bkdmg%");
        translator.add(key + "special_damage", "Special Damage: $spedmg%");
        translator.add(key + "double_attack", "Double Attack Chance: $dblatt%");

        // ==========================================
        // DEFENSIVE STATS
        // ==========================================
        translator.add(key + "health", "Current Health: $hp");
        translator.add(key + "max_health", "Maximum Health: $maxhp");
        translator.add(key + "defense", "Armor Rating: $dp");
        translator.add(key + "damage_reduction", "Damage Reduction: $drec%");
        translator.add(key + "evasion", "Evasion Rating: $eva");
        translator.add(key + "magic_resistance", "Magic Resistance: $mresis%");
        translator.add(key + "projectile_resistance", "Projectile Resistance: $presis%");
        translator.add(key + "melee_resistance", "Melee Resistance: $meleeresis%");
        translator.add(key + "flat_magic_reduction", "Flat Magic Reduction: $flatmag");
        translator.add(key + "flat_projectile_reduction", "Flat Projectile Reduction: $flatproj");
        translator.add(key + "flat_melee_reduction", "Flat Melee Reduction: $flatmelee");

        // ==========================================
        // ELEMENTAL DAMAGE SYSTEM
        // ==========================================
        translator.add(key + "fire_damage", "Fire Damage: $fired");
        translator.add(key + "fire_mult", "Fire Damage Multiplier: $firem%");
        translator.add(key + "cold_damage", "Cold Damage: $coldd");
        translator.add(key + "cold_mult", "Cold Damage Multiplier: $coldm%");
        translator.add(key + "electric_damage", "Electric Damage: $elecd");
        translator.add(key + "electric_mult", "Electric Damage Multiplier: $elecm%");
        translator.add(key + "water_damage", "Water Damage: $waterd");
        translator.add(key + "water_mult", "Water Damage Multiplier: $waterm%");
        translator.add(key + "wind_damage", "Wind Damage: $windd");
        translator.add(key + "wind_mult", "Wind Damage Multiplier: $windm%");
        translator.add(key + "holy_damage", "Holy Damage: $holyd");
        translator.add(key + "holy_mult", "Holy Damage Multiplier: $holym%");

        // ==========================================
        // ELEMENTAL RESISTANCES SYSTEM
        // ==========================================
        translator.add(key + "fire_resistance", "Fire Resistance: $fireres%");
        translator.add(key + "cold_resistance", "Cold Resistance: $coldres%");
        translator.add(key + "electric_resistance", "Electric Resistance: $elecres%");
        translator.add(key + "water_resistance", "Water Resistance: $waterres%");
        translator.add(key + "wind_resistance", "Wind Resistance: $windres%");
        translator.add(key + "holy_resistance", "Holy Resistance: $holyres%");
        translator.add(key + "flat_fire_reduction", "Flat Fire Reduction: $flatfire");
        translator.add(key + "flat_cold_reduction", "Flat Cold Reduction: $flatcold");
        translator.add(key + "flat_electric_reduction", "Flat Electric Reduction: $flatelec");
        translator.add(key + "flat_water_reduction", "Flat Water Reduction: $flatwater");
        translator.add(key + "flat_wind_reduction", "Flat Wind Reduction: $flatwind");
        translator.add(key + "flat_holy_reduction", "Flat Holy Reduction: $flatholy");

        // ==========================================
        // REGENERATION & RECOVERY
        // ==========================================
        translator.add(key + "nature_health_regen", "Health Regeneration: $nhrg/sec");
        translator.add(key + "heal_effective", "Healing Effectiveness: $hef%");
        translator.add(key + "max_resource", "Max Resource: $resource");
        translator.add(key + "resource_regen", "Resource Regen: $resregen");
        translator.add(key + "resource_regen_rate", "Resource Regen Rate: $resrate sec");

        // ==========================================
        // CASTING SYSTEM
        // ==========================================
        translator.add(key + "vct_flat_reduction", "VCT Flat Reduction: $vctflat ticks");
        translator.add(key + "vct_percent_reduction", "VCT Percent Reduction: $vctpct%");
        translator.add(key + "fct_flat_reduction", "FCT Flat Reduction: $fctflat ticks");
        translator.add(key + "fct_percent_reduction", "FCT Percent Reduction: $fctpct%");
        translator.add(key + "skill_vct_reduction", "Skill VCT Reduction: $skillvct%");

        // ==========================================
        // UTILITY
        // ==========================================
        translator.add(key + "max_weight", "Max Weight: $weight");

        // ==========================================
        // BASE ATTRIBUTES
        // ==========================================
        translator.add(key + "strength", "Strength: $str");
        translator.add(key + "agility", "Agility: $agi");
        translator.add(key + "vitality", "Vitality: $vit");
        translator.add(key + "intelligence", "Intelligence: $int");
        translator.add(key + "dexterity", "Dexterity: $dex");
        translator.add(key + "luck", "Luck: $luk");

        translator.add(key + "classbonus." + "strength", "Strength: $cstr");
        translator.add(key + "classbonus." + "agility", "Agility: $cagi");
        translator.add(key + "classbonus." + "vitality", "Vitality: $cvit");
        translator.add(key + "classbonus." + "intelligence", "Intelligence: $cint");
        translator.add(key + "classbonus." + "dexterity", "Dexterity: $cdex");
        translator.add(key + "classbonus." + "luck", "Luck: $cluk");
    }
    private void generateConfigTranslations(TranslationBuilder translator) {
        String base = "text.autoconfig.mamy";

        // Main config title
        translator.add(base + ".title", "mamy Configuration");

        // =====================================
        // CATEGORIES
        // =====================================

        translator.add(base + ".category.client_features", "Client Features");
        translator.add(base + ".category.notifications", "Notifications");
        translator.add(base + ".category.progress_bars", "Progress Bars");
        translator.add(base + ".category.gameplay", "Gameplay Settings");
        translator.add(base + ".category.death_penalty", "Death Penalty");

        // =====================================
        // CLIENT FEATURES
        // =====================================

        translator.add(base + ".option.damageCritIndicator", "Critical Hit Indicator");

        translator.add(base + ".option.missingIndicator", "Miss Indicator");

        translator.add(base + ".option.critDamageColor", "Critical Hit Color");

        translator.add(base + ".option.tooltipinfo", "Show Tooltip Info");

        // =====================================
        // NOTIFICATIONS
        // =====================================

        translator.add(base + ".option.enableToastNotifications", "Toast Notifications");

        translator.add(base + ".option.toastPositionLeft", "Toast Position Left");

        translator.add(base + ".option.toastYOffset", "Toast Y Offset");

        translator.add(base + ".option.toastMargin", "Toast Margin");

        translator.add(base + ".option.toastScale", "Toast Scale");


        // =====================================
        // PROGRESS BARS
        // =====================================

        translator.add(base + ".option.barColor", "Progress Bar Color");

        translator.add(base + ".option.barBGColor", "Progress Bar Background");

        translator.add(base + ".option.renderStyle", "Render Style");

        // Render style enum values
        translator.add(base + ".option.renderStyle.BAR", "Bar Style");
        translator.add(base + ".option.renderStyle.SLATE", "Slate Style");

        // =====================================
        // GAMEPLAY SETTINGS
        // =====================================

        translator.add(base + ".option.maxLevel", "Maximum Level");

        translator.add(base + ".option.startStatpoints", "Starting Benefit Points");

        translator.add(base + ".option.EXP_MAP", "Experience Table");

        // =====================================
        // DEATH PENALTY
        // =====================================

        translator.add(base + ".option.enableDeathPenalty", "Enable Death Penalty");

        translator.add(base + ".option.deathPenaltyPercentage", "Death Penalty Percentage");
    }

    private void generateAttributeTranslations(TranslationBuilder translator) {
        translator.add("attribute.name.health_regen", "Health Regeneration");
        translator.add("attribute.name.resource", "Resource");
        translator.add("attribute.name.resource_regen", "Resource Regeneration");
        translator.add("attribute.name.resource_regen_rate", "Resource Regeneration Rate");
        translator.add("attribute.name.accuracy", "Accuracy");
        translator.add("attribute.name.evasion", "Evasion");
        translator.add("attribute.name.crit_damage", "Critical Damage");
        translator.add("attribute.name.crit_chance", "Critical Chance");
        translator.add("attribute.name.back_attack", "Back Attack Damage");
        translator.add("attribute.name.special_attack", "Special Attack Damage");
        translator.add("attribute.name.melee_attack_damage", "Melee Attack Damage");
        translator.add("attribute.name.magic_attack_damage", "Magic Attack Damage");
        translator.add("attribute.name.projectile_attack_damage", "Projectile Attack Damage");
        translator.add("attribute.name.magic_resistance", "Magic Resistance");
        translator.add("attribute.name.damage_reduction", "Damage Reduction");
        translator.add("attribute.name.heal_effective", "Heal Effectiveness");
        translator.add("attribute.name.physical_resistance", "Physical Resistance");
    }
}