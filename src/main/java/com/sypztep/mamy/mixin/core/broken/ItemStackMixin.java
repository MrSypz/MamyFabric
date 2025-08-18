package com.sypztep.mamy.mixin.core.broken;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.sypztep.mamy.common.util.ClassEquipmentUtil;
import net.minecraft.component.ComponentHolder;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ComponentHolder {

    @Shadow
    public abstract int getDamage();

    @Shadow
    public abstract int getMaxDamage();

    @Unique
    private ItemStack stack = (ItemStack) (Object) this;

    /**
     * Prevent attribute modifiers from being applied if the item is broken or the player can't use it
     */
    @Inject(method = "applyAttributeModifiers", at = @At("HEAD"), cancellable = true)
    public void preventAttributeModifiersIfBroken(EquipmentSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer, CallbackInfo ci) {
        if (ClassEquipmentUtil.isBroken(stack)) {
            ci.cancel();
        }
    }
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void preventUse(World world, PlayerEntity player, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (ClassEquipmentUtil.isBroken(stack)) {
            if (!world.isClient) {
                player.sendMessage(Text.literal("This item is broken and cannot be used!")
                        .formatted(Formatting.RED), true);
            }
            cir.setReturnValue(TypedActionResult.fail(stack));
            return;
        }

        if (!ClassEquipmentUtil.canPlayerUseItem(player, player.getMainHandStack())) {
            if (!player.getWorld().isClient) {
                String className = ClassEquipmentUtil.getPlayerClassName(player);
                player.sendMessage(Text.literal(className + " cannot used with this item!")
                        .formatted(Formatting.RED), true);
            }
            cir.setReturnValue(TypedActionResult.fail(stack));
        }
    }

    /**
     * Handle item damage and prevent breaking by marking as broken instead
     */
    @Inject(method = "damage(ILnet/minecraft/server/world/ServerWorld;Lnet/minecraft/server/network/ServerPlayerEntity;Ljava/util/function/Consumer;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;"),
            cancellable = true)
    public void preventBreaking(int amount, ServerWorld world, ServerPlayerEntity player, Consumer<Item> breakCallback, CallbackInfo ci) {
        if (ClassEquipmentUtil.shouldPreventDamage(stack, amount)) {
            ci.cancel(); // Prevent the normal damage/breaking process
        }
    }

    /**
     * Override damage setting to implement broken system
     */
    @WrapOperation(method = "damage(ILnet/minecraft/server/world/ServerWorld;Lnet/minecraft/server/network/ServerPlayerEntity;Ljava/util/function/Consumer;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;setDamage(I)V"))
    private void handleBrokenItems(ItemStack stack, int damage, Operation<Void> original) {
        if (damage >= this.getMaxDamage()) {
            ClassEquipmentUtil.setBroken(stack);
            original.call(stack, this.getMaxDamage() - 1);
        } else {
            original.call(stack, damage);
        }
    }
}