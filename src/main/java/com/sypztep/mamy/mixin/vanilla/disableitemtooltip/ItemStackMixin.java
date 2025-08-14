package com.sypztep.mamy.mixin.vanilla.disableitemtooltip;

import com.sypztep.mamy.client.event.tooltip.CustomArmorTooltip;
import com.sypztep.mamy.client.event.tooltip.CustomSwordTooltip;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow public abstract Item getItem();

    @Inject(method = "appendAttributeModifiersTooltip", at = @At(value = "HEAD"), cancellable = true)
    private void replaceAttributeTooltip(Consumer<Text> textConsumer, @Nullable PlayerEntity player, CallbackInfo ci) {
        ItemStack itemStack = (ItemStack) (Object) this;

        AttributeModifiersComponent attributeModifiersComponent = itemStack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
        if (!attributeModifiersComponent.showInTooltip()) return; // Let vanilla handle it

        ci.cancel();

        if (itemStack.getItem() instanceof ArmorItem) {
            CustomArmorTooltip.appendCustomArmorTooltip(itemStack, textConsumer, player);
        } else {
            CustomSwordTooltip.appendCustomAttributeTooltip(itemStack, textConsumer, player);
        }
    }
}