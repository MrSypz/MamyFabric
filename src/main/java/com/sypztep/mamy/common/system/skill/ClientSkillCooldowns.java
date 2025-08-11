package com.sypztep.mamy.common.system.skill;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class ClientSkillCooldowns {
    private static final Map<Identifier, Long> cooldowns = new HashMap<>();

    // Store the server cooldown end time (epoch millis)
    public static void setCooldown(Identifier skillId, long cooldownEndTime) {
        if (cooldownEndTime <= System.currentTimeMillis()) {
            cooldowns.remove(skillId);
        } else {
            cooldowns.put(skillId, cooldownEndTime);
        }
    }

    // Returns remaining seconds of cooldown or 0 if expired
    public static float getRemaining(Identifier skillId) {
        Long endTime = cooldowns.get(skillId);
        if (endTime == null) return 0f;
        return Math.max(0f, (endTime - System.currentTimeMillis()) / 1000f);
    }
    public static void clear() {
        cooldowns.clear();
    }
}
