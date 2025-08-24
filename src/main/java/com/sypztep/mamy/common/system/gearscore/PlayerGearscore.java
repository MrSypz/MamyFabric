package com.sypztep.mamy.common.system.gearscore;

import com.sypztep.mamy.common.init.ModEntityAttributes;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;

public class PlayerGearscore {
    public static int calculateGearscore(PlayerEntity player) {
        float baseAttack = (float) player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        float meleeFlat = (float) player.getAttributeValue(ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT);
        float meleeMult = (float) player.getAttributeValue(ModEntityAttributes.MELEE_ATTACK_DAMAGE_MULT);
        float armor = (float) player.getAttributeValue(EntityAttributes.GENERIC_ARMOR);

        // Attack score: base + (melee flat * melee mult)
        int attackScore = Math.round((baseAttack + meleeFlat) * (1 + meleeMult));
        int armorScore = Math.round(armor);

        return attackScore + armorScore;
    }
}