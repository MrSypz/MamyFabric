package com.sypztep.mamy.common.system.skill;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class ClientSkillCooldowns {
    private static final Map<Identifier, Long> cooldowns = new HashMap<>();

    public static void setCooldown(Identifier skillId, float seconds) {
        if (seconds <= 0) cooldowns.remove(skillId);
         else cooldowns.put(skillId, System.currentTimeMillis() + (long)(seconds * 1000));
    }

    public static float getRemaining(Identifier skillId) {
        Long endTime = cooldowns.get(skillId);
        if (endTime == null) return 0.0f;

        long remaining = endTime - System.currentTimeMillis();
        if (remaining <= 0) {
            cooldowns.remove(skillId);
            return 0.0f;
        }
        return remaining * 0.0001f;
    }

    public static void clear() {
        cooldowns.clear();
    }
}