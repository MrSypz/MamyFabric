package com.sypztep.mamy.common.compat.provider;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.util.LivingEntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum EntityComponentProvider implements IEntityComponentProvider {
    INSTANCE;
    @Override
    public void appendTooltip(ITooltip iTooltip, EntityAccessor entityAccessor, IPluginConfig iPluginConfig) {
        Entity target = entityAccessor.getEntity();
        PlayerEntity attacker = entityAccessor.getPlayer();

        if (target instanceof LivingEntity defender && attacker != null)
            iTooltip.add(Text.translatable("mamy.hitchance", String.format("%.2f", LivingEntityUtil.hitRate(attacker, defender))));
    }

    @Override
    public Identifier getUid() {
        return Mamy.id("stats_config");
    }
}
