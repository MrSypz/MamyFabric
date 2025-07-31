package com.sypztep.mamy.common.system.skill;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.entity.damage.DamageType;

public class SkillConfig {
    public final float damage;
    public final RegistryKey<DamageType> damageType;
    public final double hitRange;
    public final int maxHitCount;
    public final int iframeTime; // in ticks

    public SkillConfig(float damage, RegistryKey<DamageType> damageType, double hitRange, int maxHitCount, int iframeTime) {
        this.damage = damage;
        this.damageType = damageType;
        this.hitRange = hitRange;
        this.maxHitCount = maxHitCount;
        this.iframeTime = iframeTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private float damage = 0.0f;
        private RegistryKey<DamageType> damageType;
        private double hitRange = 1.0;
        private int maxHitCount = 1;
        private int iframeTime = 10; // default 0.5 seconds

        public Builder damage(float damage) {
            this.damage = damage;
            return this;
        }

        public Builder damageType(RegistryKey<DamageType> damageType) {
            this.damageType = damageType;
            return this;
        }

        public Builder hitRange(double hitRange) {
            this.hitRange = hitRange;
            return this;
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
            return new SkillConfig(damage, damageType, hitRange, maxHitCount, iframeTime);
        }
    }
}