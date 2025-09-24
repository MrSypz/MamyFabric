package com.sypztep.mamy.common.system.skill;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class SkillCastDelayManager {
    private static SkillCastDelayManager instance;

    private boolean inCastDelay = false;
    private int castDelayTicks = 0;
    private int maxCastDelayTicks = 0;

    public static SkillCastDelayManager getInstance() {
        if (instance == null) instance = new SkillCastDelayManager();
        return instance;
    }

    public void startCastDelay(float delaySeconds) {
        if (delaySeconds <= 0) return;

        this.inCastDelay = true;
        this.castDelayTicks = 0;
        this.maxCastDelayTicks = (int) (delaySeconds * 20); // Convert seconds to ticks
    }

    public void tick() {
        if (!inCastDelay) return;

        if (++castDelayTicks >= maxCastDelayTicks) {
            inCastDelay = false;
            castDelayTicks = 0;
            maxCastDelayTicks = 0;
        }
    }

    public boolean isInCastDelay() {
        return inCastDelay;
    }

    public float getRemainingDelay() {
        if (!inCastDelay) return 0f;
        return (maxCastDelayTicks - castDelayTicks) / 20f;
    }

    public void reset() {
        inCastDelay = false;
        castDelayTicks = 0;
        maxCastDelayTicks = 0;
    }
}