package com.sypztep.mamy.mixin.vanilla.changeattributerange;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ArmorItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ArmorItem.class)
public class ArmorItemMixin {

    @ModifyReturnValue(method = "getAttributeModifiers", at = @At("RETURN"))
    private AttributeModifiersComponent removeArmorToughness(AttributeModifiersComponent original) {
        AttributeModifiersComponent.Builder builder = AttributeModifiersComponent.builder();

        for (AttributeModifiersComponent.Entry entry : original.modifiers()) {
            if (entry.attribute() != EntityAttributes.GENERIC_ARMOR_TOUGHNESS) {
                builder.add(entry.attribute(), entry.modifier(), entry.slot());
            }
        }

        return builder.build();
    }
}