package com.sypztep.mamy.mixin.vanilla.strength;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @ModifyExpressionValue(
            method = "appendAttributeModifierTooltip",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;getAttributeBaseValue(Lnet/minecraft/registry/entry/RegistryEntry;)D",
                    ordinal = 0)
    )
    private double addMeleeDamageToTooltip(double original,
                                           @Local(argsOnly = true) PlayerEntity player,
                                           @Local(argsOnly = true) RegistryEntry<EntityAttribute> attribute,
                                           @Local(argsOnly = true) EntityAttributeModifier modifier) {

        // If this is attack damage and the modifier is the base attack damage modifier
        if (attribute.matches(EntityAttributes.GENERIC_ATTACK_DAMAGE) &&
                modifier.idMatches(Item.BASE_ATTACK_DAMAGE_MODIFIER_ID)) {

            double meleeDamage = player.getAttributeValue(ModEntityAttributes.MELEE_ATTACK_DAMAGE);
            return original + meleeDamage;
        }

        return original;
    }
    @Inject(method = "appendAttributeModifierTooltip",
            at = @At(value = "INVOKE",
                    target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V",
                    shift = At.Shift.AFTER)
    )
    private void addMeleeDamageAfterMainTooltip(Consumer<Text> textConsumer, PlayerEntity player,
                                                RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier modifier,
                                                CallbackInfo ci, @Local(ordinal = 0) double d, @Local boolean bl) {
        // Only add when bl is true (base modifiers) and it's attack damage and shift is held
        if (bl && player != null && Screen.hasShiftDown() &&
                attribute.matches(EntityAttributes.GENERIC_ATTACK_DAMAGE) &&
                modifier.idMatches(Item.BASE_ATTACK_DAMAGE_MODIFIER_ID)) {

            double meleeDamage = player.getAttributeValue(ModEntityAttributes.MELEE_ATTACK_DAMAGE);
            if (meleeDamage > 0) {
                textConsumer.accept(
                        ScreenTexts.space()
                                .append(Text.literal("  └ Base: " + AttributeModifiersComponent.DECIMAL_FORMAT.format(d - meleeDamage)))
                                .formatted(Formatting.GRAY)
                );
                textConsumer.accept(
                        ScreenTexts.space()
                                .append(Text.literal("  └ Melee: +" + AttributeModifiersComponent.DECIMAL_FORMAT.format(meleeDamage)))
                                .formatted(Formatting.GREEN)
                );
            }
        }
    }
}