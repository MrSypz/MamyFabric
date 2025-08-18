package com.sypztep.mamy.mixin.core.classes.correctequipment;

import com.sypztep.mamy.common.util.ClassEquipmentUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.BiConsumer;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Redirect(
            method = "getEquipmentChanges",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;applyAttributeModifiers(Lnet/minecraft/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V"
            )
    )
    private void redirectApplyAttributes(ItemStack instance, EquipmentSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity instanceof PlayerEntity player) {
            ClassEquipmentUtil.RestrictionReason reason =
                    ClassEquipmentUtil.getRestrictionReason(player, instance);

            if (reason == ClassEquipmentUtil.RestrictionReason.BROKEN ||
                    reason == ClassEquipmentUtil.RestrictionReason.CLASS_RESTRICTED) {
                return;
            }
        }
        instance.applyAttributeModifiers(slot, attributeModifierConsumer);
    }


}
