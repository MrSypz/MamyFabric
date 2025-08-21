package com.sypztep.mamy.common.system.gearscore;

import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.init.ModPassiveAbilities;
import com.sypztep.mamy.common.system.classes.GrowthFactor;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.passive.PassiveAbilityManager;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.*;

public class PlayerGearscore {
    private static final Map<UUID, CachedGearscore> gearscoreCache = new HashMap<>();

    public static int calculateGearscore(PlayerEntity player) {
        UUID playerId = player.getUuid();
        CachedGearscore cached = gearscoreCache.get(playerId);

        // Check if cache is still valid
        if (cached != null && cached.isValid(player)) {
            return cached.gearscore;
        }

        // Calculate new gearscore
        int gearscore = calculateFreshGearscore(player);

        // Cache the result
        gearscoreCache.put(playerId, new CachedGearscore(gearscore, player));

        return gearscore;
    }

    private static int calculateFreshGearscore(PlayerEntity player) {
        LivingLevelComponent levelComp = ModEntityComponents.LIVINGLEVEL.get(player);
        PlayerClassComponent classComp = ModEntityComponents.PLAYERCLASS.get(player);

        int baseStatScore = calculateBaseStatScore(levelComp);
        int passiveScore = calculatePassiveScore(player);
        int classScore = calculateClassScore(classComp);

        return baseStatScore + passiveScore + classScore;
    }
    private static int calculateBaseStatScore(LivingLevelComponent levelComp) {
        int totalScore = 0;

        // Each stat point contributes differently based on combat effectiveness
        for (StatTypes statType : StatTypes.values()) {
            int statValue = levelComp.getStatValue(statType);
            int statScore = statValue * getStatMultiplier(statType);
            totalScore += statScore;
        }

        return totalScore;
    }

    private static int getStatMultiplier(StatTypes statType) {
        return switch (statType) {
            case STRENGTH -> 12;     // High damage impact
            case DEXTERITY -> 10;    // Accuracy + projectile damage
            case VITALITY -> 8;      // Survivability (important but not damage)
            case INTELLIGENCE -> 11; // Magic damage + resources
            case AGILITY -> 9;       // Evasion + attack speed
            case LUCK -> 7;          // Crit chance + hybrid bonuses
        };
    }
    private static int calculatePassiveScore(PlayerEntity player) {
        int totalPassiveScore = 0;

        for (PassiveAbility passive : ModPassiveAbilities.getAllAbilities())
            if (PassiveAbilityManager.isActive(player, passive))
                totalPassiveScore += getPassiveScore(passive);

        return totalPassiveScore;
    }

    private static int getPassiveScore(PassiveAbility passive) {
        // AGI PASSIVES (Evasion Focus) - Total possible: 1400
        if (passive == ModPassiveAbilities.SWIFT_FEET) return 80;           // AGI 10: +15 evasion
        if (passive == ModPassiveAbilities.QUICK_REFLEX) return 120;        // AGI 20: +25 evasion
        if (passive == ModPassiveAbilities.WIND_WALKER) return 180;         // AGI 30: +35 evasion + mobility
        if (passive == ModPassiveAbilities.SHADOW_STEP) return 280;         // AGI 50: +55 evasion + stealth
        if (passive == ModPassiveAbilities.PHANTOM_WALKER) return 320;      // AGI 75: +60 evasion + jump
        if (passive == ModPassiveAbilities.VOID_DANCER) return 420;         // AGI 99: +55 evasion + 15% atk speed

        // DEX PASSIVES (Accuracy Focus) - Total possible: 1420
        if (passive == ModPassiveAbilities.PRECISION_STRIKES) return 90;    // DEX 10: +15 acc + 2% crit
        if (passive == ModPassiveAbilities.HEADHUNTER) return 140;          // DEX 20: +25 acc + headshot dmg
        if (passive == ModPassiveAbilities.STEADY_AIM) return 200;          // DEX 30: +35 acc + proj dmg + stability
        if (passive == ModPassiveAbilities.MARKS_MAN) return 300;           // DEX 50: +55 acc + proj dmg + iframe bypass
        if (passive == ModPassiveAbilities.LETHAL_PRECISION) return 350;    // DEX 75: +60 acc + 25% crit dmg
        if (passive == ModPassiveAbilities.RICOCHET_MASTER) return 340;     // DEX 99: +55 acc + proj dmg + ricochet

        // STR PASSIVES (Melee Focus) - Total possible: 1540
        if (passive == ModPassiveAbilities.BRUTAL_STRIKES) return 100;      // STR 10: +3 melee + knockback + block break
        if (passive == ModPassiveAbilities.DEVASTATING_BLOWS) return 130;   // STR 20: +6 melee + 5% crit + mining speed
        if (passive == ModPassiveAbilities.BACK_BREAKER) return 190;        // STR 30: +25% crit dmg + 15% back atk + low HP bonus
        if (passive == ModPassiveAbilities.BERSERKER_RAGE) return 280;      // STR 50: +12 melee + 15% double atk + missing HP scaling
        if (passive == ModPassiveAbilities.WEAPON_MASTER) return 380;       // STR 75: +18 melee + 50% crit dmg + 25% armor pen
        if (passive == ModPassiveAbilities.TITAN_STRENGTH) return 460;      // STR 99: +25 melee + 25% double atk + 3x3 area

        // VIT PASSIVES (Tank Focus) - Total possible: 1400
        if (passive == ModPassiveAbilities.HARDY_CONSTITUTION) return 100;  // VIT 10: +50 HP + 1 regen + combat regen
        if (passive == ModPassiveAbilities.RAPID_REGENERATION) return 130;  // VIT 20: +3% DR + 15% heal eff + immunities
        if (passive == ModPassiveAbilities.IRON_SKIN) return 170;           // VIT 30: +3 regen + 5% DR + dmg to healing
        if (passive == ModPassiveAbilities.GUARDIAN_SPIRIT) return 260;     // VIT 50: +100 HP + 7% DR + death immunity
        if (passive == ModPassiveAbilities.UNDYING_WILL) return 320;        // VIT 75: +150 HP + 5 regen + fatal dmg survival
        if (passive == ModPassiveAbilities.IMMORTAL_FORTRESS) return 420;   // VIT 99: +200 HP + 8 regen + 10% DR + dmg sharing

        // INT PASSIVES (Magic Focus) - Total possible: 1390
        if (passive == ModPassiveAbilities.MANA_EFFICIENCY) return 80;      // INT 10: +20% resource + 3 regen + health bars
        if (passive == ModPassiveAbilities.ELEMENTAL_AFFINITY) return 110;  // INT 20: +3 magic dmg + ore detection
        if (passive == ModPassiveAbilities.ARCANE_INTELLECT) return 160;    // INT 30: +6 magic dmg + 30% resource + spell duration
        if (passive == ModPassiveAbilities.SPELL_PENETRATION) return 250;   // INT 50: +10 magic dmg + 15% magic resist + mana immunity
        if (passive == ModPassiveAbilities.ELEMENTAL_MASTERY) return 320;   // INT 75: +15 magic dmg + status effects
        if (passive == ModPassiveAbilities.ARCHMAGE_POWER) return 470;      // INT 99: +25 magic dmg + 50% resource + remote abilities

        // LUK PASSIVES (Balance Focus) - Total possible: 1180
        if (passive == ModPassiveAbilities.LUCKY_STRIKES) return 90;        // LUK 10: +5% crit + 12 acc + resource restore
        if (passive == ModPassiveAbilities.FORTUNE_FINDER) return 120;      // LUK 20: +15 evasion + 10% crit dmg + double drops
        if (passive == ModPassiveAbilities.CRITICAL_MASTERY) return 170;    // LUK 30: +20 acc + 10% crit + crits heal
        if (passive == ModPassiveAbilities.DESTINED_STRIKES) return 240;    // LUK 50: +25 acc + 20% crit dmg + 15% double atk
        if (passive == ModPassiveAbilities.PROBABILITY_MASTER) return 280;  // LUK 75: +20 acc + 15% crit + crit immunity
        if (passive == ModPassiveAbilities.FATE_WEAVER) return 280;         // LUK 99: +10 acc + 25% crit + 30% crit dmg + RNG control

        // Default fallback for unknown passives
        return 50;
    }

    private static int calculateClassScore(PlayerClassComponent classComp) {
        if (classComp == null) return 0;

        PlayerClass playerClass = classComp.getClassManager().getCurrentClass();
        if (playerClass == null) return 0;

        int classLevel = classComp.getClassManager().getClassLevel();

        // Base score by class tier and level
        int tierBonus = getClassTierBonus(playerClass);
        int levelBonus = classLevel * 15; // Each class level = 15 gearscore

        // Growth factor bonus (classes with better scaling worth more)
        int growthBonus = calculateGrowthFactorBonus(playerClass, classLevel);

        // Job stat bonus (from jobBonuses in class definition)
        int jobStatBonus = calculateJobStatBonus(playerClass);

        return tierBonus + levelBonus + growthBonus + jobStatBonus;
    }

    private static int getClassTierBonus(PlayerClass playerClass) {
        return switch (playerClass.getTier()) {
            case 0 -> 0;     // Novice
            case 1 -> 200;   // First job classes
            case 2 -> 500;   // Second job classes
            case 3 -> 800;   // Transcendent classes
            default -> 100;
        };
    }

    private static int calculateGrowthFactorBonus(PlayerClass playerClass, int classLevel) {
        // Classes with better growth factors provide more gearscore
        // This rewards picking classes with strong scaling

        Map<RegistryEntry<EntityAttribute>, GrowthFactor> growthFactors = playerClass.getGrowthFactors();
        int totalGrowthBonus = 0;

        for (var entry : growthFactors.entrySet()) {
            GrowthFactor growth = entry.getValue();
            RegistryEntry<EntityAttribute> attribute = entry.getKey();

            // Calculate growth bonus based on attribute type and growth rate
            if (attribute.equals(EntityAttributes.GENERIC_MAX_HEALTH)) {
                totalGrowthBonus += (int)(growth.calculateGrowth(1.0, classLevel - 1) * 2);
            } else if (attribute.equals(ModEntityAttributes.RESOURCE)) {
                totalGrowthBonus += (int)(growth.calculateGrowth(1.0, classLevel - 1) * 1);
            }
            // Add more attribute types as needed
        }

        return totalGrowthBonus;
    }

    private static int calculateJobStatBonus(PlayerClass playerClass) {
        // Job stat bonuses from class definition provide flat gearscore
        // These are the (short)str, (short)agi, etc. values in class creation

        // This would need access to the job bonuses from the class
        // For now, estimate based on class archetype
        return switch (playerClass.getId()) {
            case "swordman" -> 180;  // (7,2,4,0,3,2) = 18 total × 10
            case "mage" -> 180;      // (0,4,0,8,3,3) = 18 total × 10
            case "archer" -> 180;    // (3,3,1,2,7,2) = 18 total × 10
            case "acolyte" -> 180;   // (3,2,3,3,3,4) = 18 total × 10
            case "thief" -> 180;     // (4,4,2,1,4,3) = 18 total × 10
            default -> 100;
        };
    }
    private static class CachedGearscore {
        final int gearscore;
        final int playerLevel;
        final int totalStats;
        final Set<String> activePassives;
        final String className;
        final int classLevel;
        final long timestamp;

        public CachedGearscore(int gearscore, PlayerEntity player) {
            this.gearscore = gearscore;
            this.timestamp = System.currentTimeMillis();

            // Store validation data
            LivingLevelComponent levelComp = ModEntityComponents.LIVINGLEVEL.get(player);
            PlayerClassComponent classComp = ModEntityComponents.PLAYERCLASS.get(player);

            this.playerLevel = levelComp.getLevel();
            this.totalStats = calculateTotalStats(levelComp);
            this.activePassives = getActivePassiveIds(player);
            this.className = classComp.getClassManager().getCurrentClass().getId();
            this.classLevel = classComp.getClassManager().getClassLevel();
        }

        public boolean isValid(PlayerEntity player) {
            // Cache expires after 30 seconds or if any relevant data changed
            if (System.currentTimeMillis() - timestamp > 30000) return false;

            LivingLevelComponent levelComp = ModEntityComponents.LIVINGLEVEL.get(player);
            PlayerClassComponent classComp = ModEntityComponents.PLAYERCLASS.get(player);

            if (levelComp.getLevel() != playerLevel) return false;
            if (calculateTotalStats(levelComp) != totalStats) return false;
            if (!getActivePassiveIds(player).equals(activePassives)) return false;

            String currentClassName = classComp.getClassManager().getCurrentClass().getId();
            int currentClassLevel = classComp.getClassManager().getClassLevel();

            return currentClassName.equals(className) && currentClassLevel == classLevel;
        }

        private int calculateTotalStats(LivingLevelComponent levelComp) {
            int total = 0;
            for (StatTypes statType : StatTypes.values()) {
                total += levelComp.getStatValue(statType);
            }
            return total;
        }

        private Set<String> getActivePassiveIds(PlayerEntity player) {
            Set<String> activeIds = new HashSet<>();
            for (PassiveAbility passive : ModPassiveAbilities.getAllAbilities()) {
                if (PassiveAbilityManager.isActive(player, passive)) {
                    activeIds.add(passive.getId());
                }
            }
            return activeIds;
        }
    }

    // Utility method to clear cache when needed
    public static void clearGearscoreCache(PlayerEntity player) {
        gearscoreCache.remove(player.getUuid());
    }
}