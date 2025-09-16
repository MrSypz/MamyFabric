package com.sypztep.mamy.mixin.vanilla.itemcraftsignature;

import com.sypztep.mamy.common.component.item.PotionQualityComponents;
import com.sypztep.mamy.common.init.ModDataComponents;
import com.sypztep.mamy.common.init.ModTags;
import com.sypztep.mamy.common.item.ResourcePotionItem;
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
        if (!stack.isEmpty() && !player.getWorld().isClient()) {
            if (stack.getItem() instanceof ResourcePotionItem && !stack.contains(ModDataComponents.POTION_QUALITY)) {
                PotionQualityComponents quality = PotionQualityComponents.random(world);
                stack.set(ModDataComponents.POTION_QUALITY, quality);
            }
        }
    }
}