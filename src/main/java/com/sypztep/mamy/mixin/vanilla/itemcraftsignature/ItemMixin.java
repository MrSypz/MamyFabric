package com.sypztep.mamy.mixin.vanilla.itemcraftsignature;

import com.sypztep.mamy.common.init.ModDataComponents;
import net.minecraft.component.ComponentHolder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Item.class)
public abstract class ItemMixin implements ComponentHolder {
    @Inject(method = "onCraftByPlayer", at = @At("HEAD"))
    public void onCraft(ItemStack stack, World world, PlayerEntity player, CallbackInfo ci) {
        if (!stack.isEmpty() && !player.getWorld().isClient() && !stack.contains(ModDataComponents.CRAFT_BY)) {
            stack.set(ModDataComponents.CRAFT_BY, player.getName().getString());
        }
    }
}


