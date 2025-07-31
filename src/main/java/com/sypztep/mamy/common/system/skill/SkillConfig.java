package com.sypztep.mamy.common.system.skill;

import net.minecraft.registry.RegistryKey;
import net.minecraft.entity.damage.DamageType;

public class SkillConfig {
    public final float damage;
    public final RegistryKey<DamageType> damageType;
    public final double hitRange;
    public final int maxHitCount;
    public final boolean bypassIframe;
    public final boolean endOnMaxHits;
    public final long iframeTime; // New iframe setting

    private SkillConfig(Builder builder) {
        this.damage = builder.damage;
        this.damageType = builder.damageType;
        this.hitRange = builder.hitRange;
        this.maxHitCount = builder.maxHitCount;
        this.bypassIframe = builder.bypassIframe;
        this.endOnMaxHits = builder.endOnMaxHits;
        this.iframeTime = builder.iframeTime;
    }

    public static class Builder {
        private float damage = 0.0f;
        private RegistryKey<DamageType> damageType;
        private double hitRange = 3.0;
        private int maxHitCount = 1;
        private boolean bypassIframe = false;
        private boolean endOnMaxHits = true;
        private long iframeTime = 10; // 0.5 seconds default

        public Builder damage(float damage, RegistryKey<DamageType> damageType) {
            this.damage = damage;
            this.damageType = damageType;
            return this;
        }

        public Builder hitRange(double range) {
            this.hitRange = range;
            return this;
        }

        public Builder maxHits(int maxHits) {
            this.maxHitCount = maxHits;
            return this;
        }

        public Builder bypassIframe(boolean bypass) {
            this.bypassIframe = bypass;
            return this;
        }

        public Builder iframeTime(long ticks) {
            this.iframeTime = ticks;
            return this;
        }

        public Builder endOnMaxHits(boolean end) {
            this.endOnMaxHits = end;
            return this;
        }

        public SkillConfig build() {
            return new SkillConfig(this);
        }
    }
}