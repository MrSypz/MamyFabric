package com.sypztep.mamy.common.component.living;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.ladysnake.cca.api.v3.component.tick.CommonTickingComponent;

public final class EvasionTimerComponent implements CommonTickingComponent {
    private int resetTicks = 0, missedCount = 0;
    private final Identifier EVASION_REDUCE = Mamy.id("toomuch_damageincome");

    private final LivingEntity living;
    public EvasionTimerComponent(LivingEntity living) {
        this.living = living;
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        resetTicks = tag.getInt("ResetTicks");
        missedCount = tag.getInt("MissedCount");
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putInt("ResetTicks", resetTicks);
        tag.putInt("MissedCount", missedCount);
    }

    @Override
    public void tick() {
        if (resetTicks > 0) {
            resetTicks--;
            if (resetTicks == 0) {
                missedCount = 0;
                cleanupAttribute();
            }
        }
    }

    public void markMissed() {
        missedCount++;
        resetTicks = 20;
        EntityAttributeInstance att = living.getAttributeInstance(ModEntityAttributes.EVASION);
        if (att != null) {
            var mod = new EntityAttributeModifier(this.EVASION_REDUCE, Math.clamp(-4 * this.missedCount,-160D,0), EntityAttributeModifier.Operation.ADD_VALUE);
            att.overwritePersistentModifier(mod);
        }
    }
    private void cleanupAttribute() {
        EntityAttributeInstance att = living.getAttributeInstance(ModEntityAttributes.EVASION);
        if (att != null) att.removeModifier(this.EVASION_REDUCE);
    }
}
