package com.sypztep.mamy.common.system.skill.config;

import net.minecraft.registry.RegistryKey;
import net.minecraft.entity.damage.DamageType;

public record SkillConfig(
        float damage,
        RegistryKey<DamageType> damageType,
        double hitRange,     // Simple expansion (if using simple mode)
        double hitWidth,     // Custom X expansion
        double hitHeight,    // Custom Y expansion
        double hitDepth,     // Custom Z expansion
        boolean useCustomBox, // Use custom dimensions instead of simple range
        int maxHitCount,
        int iframeTime
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private float damage = 0.0f;
        private RegistryKey<DamageType> damageType;
        private double hitRange = 1.0;      // Simple mode
        private double hitWidth = 1.0;      // Custom mode
        private double hitHeight = 1.0;
        private double hitDepth = 1.0;
        private boolean useCustomBox = false;
        private int maxHitCount = 1;
        private int iframeTime = 10;

        public Builder damage(float damage) {
            this.damage = damage;
            return this;
        }

        public Builder damageType(RegistryKey<DamageType> damageType) {
            this.damageType = damageType;
            return this;
        }

        // Simple mode - expand equally in all directions
        public Builder hitRange(double hitRange) {
            this.hitRange = hitRange;
            this.useCustomBox = false;
            return this;
        }

        // Custom mode - specify exact dimensions
        public Builder customHitBox(double width, double height, double depth) {
            this.hitWidth = width;
            this.hitHeight = height;
            this.hitDepth = depth;
            this.useCustomBox = true;
            return this;
        }

        // Convenience methods for common shapes
        public Builder slashHitBox(double width, double height) {
            return customHitBox(width, height, width); // Thin depth for slash
        }

        public Builder maxHitCount(int maxHitCount) {
            this.maxHitCount = maxHitCount;
            return this;
        }

        public Builder iframeTime(int iframeTime) {
            this.iframeTime = iframeTime;
            return this;
        }

        public SkillConfig build() {
            return new SkillConfig(damage, damageType, hitRange, hitWidth, hitHeight, hitDepth, useCustomBox, maxHitCount, iframeTime);
        }
    }
}