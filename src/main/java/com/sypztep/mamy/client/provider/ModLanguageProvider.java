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

        playerInfo(translate);
        generateConfigTranslations(translate);
        generateAttributeTranslations(translate);
    }

    private void playerInfo(TranslationBuilder translator) {
        String key = "mamy.info.";
        translator.add("mamy.gui.player_info.header", "Player Stats");
        translator.add("mamy.gui.player_info.header_level", "Stats");

        // ==========================================
        // SECTION HEADERS
        // ==========================================
        translator.add(key + "header_combat", "COMBAT POWER");
        translator.add(key + "header_precision", "PRECISION & CRITICAL");
        translator.add(key + "header_defense", "DEFENSIVE STATS");
        translator.add(key + "header_recovery", "REGENERATION");
        translator.add(key + "header_attributes", "STATS");

        // ==========================================
        // COMBAT POWER STATS
        // ==========================================
        translator.add(key + "physical", "Base Attack Power: $phyd");
        translator.add(key + "melee_damage", "Melee Damage Bonus: $meleed");
        translator.add(key + "projectile_damage", "Projectile Damage Bonus: $projd");
        translator.add(key + "magic_damage", "Magic Attack Power: $mdmg");
        translator.add(key + "attack_speed", "Attack Speed: $asp");

        // ==========================================
        // PRECISION & CRITICAL STATS
        // ==========================================
        translator.add(key + "accuracy", "Accuracy Rating: $acc");
        translator.add(key + "critical_chance", "Critical Hit Chance: $ccn%");
        translator.add(key + "critical_damage", "Critical Hit Damage: $cdmg%");
        translator.add(key + "backattack_damage", "Back Attack Damage: $bkdmg%");
        translator.add(key + "special_damage", "Special Damage: $spedmg%");

        // ==========================================
        // DEFENSIVE STATS
        // ==========================================
        translator.add(key + "health", "Current Health: $hp");
        translator.add(key + "max_health", "Maximum Health: $maxhp");
        translator.add(key + "defense", "Armor Rating: $dp");
        translator.add(key + "damage_reduction", "Damage Reduction: $drec%");
        translator.add(key + "evasion", "Evasion Rating: $eva");
        translator.add(key + "magic_resistance", "Magic Resistance: $mresis%");

        // ==========================================
        // REGENERATION & RECOVERY
        // ==========================================
        translator.add(key + "nature_health_regen", "Health Regeneration: $nhrg/sec");
        translator.add(key + "heal_effective", "Healing Effectiveness: $hef%");

        // ==========================================
        // BASE ATTRIBUTES
        // ==========================================
        translator.add(key + "strength", "Strength: $str");
        translator.add(key + "agility", "Agility: $agi");
        translator.add(key + "vitality", "Vitality: $vit");
        translator.add(key + "intelligence", "Intelligence: $int");
        translator.add(key + "dexterity", "Dexterity: $dex");
        translator.add(key + "luck", "Luck: $luk");
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
        translator.add("attribute.name.accuracy", "Accuracy");
        translator.add("attribute.name.evasion", "Evasion");
        translator.add("attribute.name.crit_damage", "Critical Damage");
        translator.add("attribute.name.crit_chance", "Critical Chance");
        translator.add("attribute.name.back_attack", "Back Attack Damage");
        translator.add("attribute.name.melee_attack_damage", "Melee Attack Damage");
        translator.add("attribute.name.magic_attack_damage", "Magic Attack Damage");
        translator.add("attribute.name.projectile_attack_damage", "Projectile Attack Damage");
        translator.add("attribute.name.magic_resistance", "Magic Resistance");
        translator.add("attribute.name.physical_resistance", "Physical Resistance");
    }
}