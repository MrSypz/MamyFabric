package com.sypztep.mamy.common.system.skill;

import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.minecraft.entity.player.PlayerEntity;

public class CastingCalculator {
    private static final double STAT_DIVISOR = 530.0;

    /**
     * Calculate actual Variable Cast Time with all reductions
     */
    public static int calculateVCT(PlayerEntity player, int baseVCT) {
        if (baseVCT <= 0) return 0;
        LivingLevelComponent comp = ModEntityComponents.LIVINGLEVEL.get(player);

        // Get stats
        double dex = comp.getStatValue(StatTypes.DEXTERITY); // Replace with your actual DEX attribute
        double intel = comp.getStatValue(StatTypes.INTELLIGENCE); // Replace with your actual INT attribute

        // Get reductions
        double flatVCTReduc = player.getAttributeValue(ModEntityAttributes.VCT_REDUCTION_FLAT);
        double gearVCTReduc = player.getAttributeValue(ModEntityAttributes.VCT_REDUCTION_PERCENT);
        double skillVCTReduc = player.getAttributeValue(ModEntityAttributes.SKILL_VCT_REDUCTION);

        // Apply flat reduction first
        double vctAfterFlat = Math.max(0, baseVCT - flatVCTReduc);

        // Calculate stat reduction: SQRT[(DEX × 2 + INT) ÷ 530]
        double statReduction = Math.sqrt((dex * 2 + intel) / STAT_DIVISOR);
        statReduction = Math.min(statReduction, 0.8); // Cap at 80%

        // Apply all multipliers
        double finalVCT = vctAfterFlat * (1.0 - statReduction) *
                (1.0 - gearVCTReduc / 100.0) *
                (1.0 - skillVCTReduc / 100.0);

        return Math.max(0, (int) Math.round(finalVCT));
    }

    /**
     * Calculate actual Fixed Cast Time with reductions
     */
    public static int calculateFCT(PlayerEntity player, int baseFCT) {
        if (baseFCT <= 0) return 0;

        // Get reductions
        double flatFCTReduc = player.getAttributeValue(ModEntityAttributes.FCT_REDUCTION_FLAT);
        double maxFCTReduc = player.getAttributeValue(ModEntityAttributes.FCT_REDUCTION_PERCENT);

        // Apply flat reduction first
        double fctAfterFlat = Math.max(0, baseFCT - flatFCTReduc);

        // Apply percentage reduction
        double finalFCT = fctAfterFlat * (1.0 - maxFCTReduc / 100.0);

        return Math.max(0, (int) Math.round(finalFCT));
    }

    /**
     * Calculate total cast time (VCT + FCT)
     */
    public static int calculateTotalCastTime(PlayerEntity player, CastableSkill skill, int skillLevel) {
        int vct = calculateVCT(player, skill.getBaseVCT(skillLevel));
        int fct = calculateFCT(player, skill.getBaseFCT(skillLevel));
        return vct + fct;
    }

    /**
     * Get casting breakdown for UI/debugging
     */
    public static CastingBreakdown getCastingBreakdown(PlayerEntity player, CastableSkill skill, int skillLevel) {
        int baseVCT = skill.getBaseVCT(skillLevel);
        int baseFCT = skill.getBaseFCT(skillLevel);
        int finalVCT = calculateVCT(player, baseVCT);
        int finalFCT = calculateFCT(player, baseFCT);

        return new CastingBreakdown(baseVCT, baseFCT, finalVCT, finalFCT);
    }

    /**
     * Data class for casting breakdown
     */
    public static class CastingBreakdown {
        public final int baseVCT;
        public final int baseFCT;
        public final int finalVCT;
        public final int finalFCT;
        public final int totalCastTime;

        public CastingBreakdown(int baseVCT, int baseFCT, int finalVCT, int finalFCT) {
            this.baseVCT = baseVCT;
            this.baseFCT = baseFCT;
            this.finalVCT = finalVCT;
            this.finalFCT = finalFCT;
            this.totalCastTime = finalVCT + finalFCT;
        }
    }
}