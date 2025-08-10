package com.sypztep.mamy.common.system.stat;

import com.sypztep.mamy.common.system.level.LevelSystem;
import com.sypztep.mamy.common.util.AttributeModification;
import com.sypztep.mamy.common.util.LivingEntityUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.function.ToDoubleFunction;

public abstract class Stat {
    protected short baseValue; // Base value = 0 it write value in LivingStats when reset it going to 0
    protected short currentValue  ;
    protected short increasePerPoint;
    protected short totalPointsUsed; // Track total points used
    protected short classBonus;

    public Stat(short baseValue) {
        this.baseValue = baseValue;
        this.currentValue = baseValue;
        this.increasePerPoint = 1;
        this.totalPointsUsed = 0;
        this.classBonus = 0;
    }

    public void readFromNbt(NbtCompound tag,LivingEntity living) {
        this.currentValue = tag.getShort("CurrentValue");
        if (LivingEntityUtil.isPlayer(living)) {
            this.totalPointsUsed = tag.getShort("TotalPointsUsed"); // Read total points used from NBT
            this.classBonus = tag.getShort("ClassBonus"); //Class bonus
        }
    }

    public void writeToNbt(NbtCompound tag,LivingEntity living) {
        tag.putShort("CurrentValue", this.currentValue);
        if (LivingEntityUtil.isPlayer(living)) {
            tag.putShort("TotalPointsUsed", this.totalPointsUsed); // Write total points used to NBT
            tag.putShort("ClassBonus", this.classBonus);
        }
    }

    public short getBaseValue() {
        return baseValue;
    }

    public short getValue() {
        return currentValue;
    }

    public short getClassBonus() {
        return classBonus;
    }

    public short getEffective() {
        return (short) (currentValue + classBonus); // Total effective stat (Current + Class Bonus)
    }

    public short getIncreasePerPoint() {
        return increasePerPoint = (short) (1 + (currentValue / 10));
    }

    public void increase(short points) {
        this.currentValue += points;
        this.totalPointsUsed += (short) (points * increasePerPoint); // Track แต้มที่ใช้
    }
    public short getTotalPointsUsed() {
        return totalPointsUsed;
    }
    //for monster
    public void setPoints(short points) {
        this.currentValue = points;
    }
    public void add(short points) {
        this.currentValue += points;
    }

    public void setClassBonus(short classBonus) {
        this.classBonus = classBonus;
    }

    public abstract void applyPrimaryEffect(LivingEntity player);
    public abstract void applySecondaryEffect(LivingEntity player);
    protected void applyEffect(LivingEntity living, RegistryEntry<EntityAttribute> attribute, Identifier modifierId, ToDoubleFunction<Double> effectFunction) {
        applyEffect(living,attribute,modifierId,EntityAttributeModifier.Operation.ADD_VALUE,effectFunction);
    }
    protected void applyEffect(LivingEntity living, RegistryEntry<EntityAttribute> attribute, Identifier modifierId, EntityAttributeModifier.Operation operation, ToDoubleFunction<Double> effectFunction) {
        EntityAttributeInstance attributeInstance = living.getAttributeInstance(attribute);
        if (attributeInstance != null) {
            double baseValue = living.getAttributeBaseValue(attribute);
            double effectValue = effectFunction.applyAsDouble(baseValue);

            if (modifierId == null) throw new IllegalArgumentException("modifierId cannot be null report this on github");

            // Remove existing modifier
            EntityAttributeModifier existingModifier = attributeInstance.getModifier(modifierId);
            if (existingModifier != null) attributeInstance.removeModifier(existingModifier);

            // Apply new modifier with the specified operation
            EntityAttributeModifier mod = new EntityAttributeModifier(modifierId, effectValue, operation);
            attributeInstance.addPersistentModifier(mod);
        }
    }
    protected void applyEffects(LivingEntity living, List<AttributeModification> modifications) {
        for (AttributeModification modification : modifications) {
            EntityAttributeInstance attributeInstance = living.getAttributeInstance(modification.attribute());
            if (attributeInstance != null) {
                double baseValue = living.getAttributeBaseValue(modification.attribute());
                double effectValue = modification.effectFunction().applyAsDouble(baseValue);

                if (modification.modifierId() == null) throw new IllegalArgumentException("modifierId cannot be null report this on github");

                EntityAttributeModifier existingModifier = attributeInstance.getModifier(modification.modifierId());
                if (existingModifier != null) attributeInstance.removeModifier(existingModifier);

                EntityAttributeModifier mod = new EntityAttributeModifier(modification.modifierId(), effectValue, modification.operation());
                attributeInstance.addPersistentModifier(mod);
            }
        }
    }
    public void reset(PlayerEntity player, LevelSystem levelSystem, boolean shouldReturnPoint) {
        if (shouldReturnPoint) levelSystem.addStatPoints(this.totalPointsUsed); // return player point for each apply

        this.currentValue = baseValue;
        this.totalPointsUsed = 0;

        applyPrimaryEffect(player);
        applySecondaryEffect(player);
    }

    public abstract List<Text> getEffectDescription(int additionalPoints);
    protected Identifier getPrimaryId() {
        return null;
    }
    protected Identifier getSecondaryId(){
        return null;
    }
}

