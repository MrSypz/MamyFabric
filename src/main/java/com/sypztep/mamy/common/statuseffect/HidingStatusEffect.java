package com.sypztep.mamy.common.statuseffect;

import com.sypztep.mamy.common.component.living.LivingHidingComponent;
import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class HidingStatusEffect extends StatusEffect implements EffectRemoval{

    public HidingStatusEffect(StatusEffectCategory category) {
        super(category, 0);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        int skillLevel = amplifier + 1;
        int drainInterval = (5 + skillLevel) * 20;

        return duration % drainInterval == 0;
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (!(entity instanceof PlayerEntity player)) return false;
        if (!(entity.getWorld() instanceof ServerWorld)) return false;

        PlayerClassComponent resourceComponent = ModEntityComponents.PLAYERCLASS.get(player);
        if (resourceComponent.getClassManager().getCurrentResource() >= 1) resourceComponent.useResource(1);

        return true;
    }

    @Override
    public void onRemoved(LivingEntity entity) {
        unBury(entity);
    }

    private static void unBury(LivingEntity entity) {
        if (entity instanceof PlayerEntity player) {
            LivingHidingComponent buryComponent = ModEntityComponents.HIDING.get(player);
            buryComponent.unbury();
            entity.setPosition(entity.getX(), entity.getY() + 1, entity.getZ());
            player.sendMessage(Text.literal("You emerge from hiding").formatted(Formatting.GRAY), true);
        }
    }
}