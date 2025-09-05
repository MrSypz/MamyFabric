package com.sypztep.mamy.common.item;

import com.sypztep.mamy.common.component.item.PotionQualityComponents;
import com.sypztep.mamy.common.component.item.ResourceComponent;
import com.sypztep.mamy.common.init.ModDataComponents;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.classes.ResourceType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.PotionItem;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.List;

public class ResourcePotionItem extends PotionItem {

    public ResourcePotionItem(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient && user instanceof ServerPlayerEntity player) {
            ResourceComponent resourceComponent = stack.get(ModDataComponents.RESOURCE_RESTORE);
            PotionQualityComponents quality = stack.get(ModDataComponents.POTION_QUALITY);

            if (resourceComponent != null) {
                float baseAmount = resourceComponent.resourceAmount();
                float actualAmount = quality != null ? quality.getActualResourceAmount(baseAmount) : baseAmount;

                ModEntityComponents.PLAYERCLASS.get(player).addResource(actualAmount);

                player.getItemCooldownManager().set(this, 100);

                world.playSound(null, user.getX(), user.getY(), user.getZ(),
                        SoundEvents.ENTITY_GENERIC_DRINK, SoundCategory.PLAYERS, 1.0F,
                        world.random.nextFloat() * 0.1F + 0.9F);

                if (!player.isCreative() && resourceComponent.usingConvertsTo().isPresent()) {
                    ItemStack convertedItem = resourceComponent.usingConvertsTo().get().copy();
                    if (!player.getInventory().insertStack(convertedItem)) {
                        player.dropItem(convertedItem, false);
                    }
                }
            }
        }

        if (user instanceof PlayerEntity player && player.isCreative()) {
            return stack;
        }

        return stack.copyWithCount(stack.getCount() - 1);
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        ResourceComponent resourceComponent = stack.get(ModDataComponents.RESOURCE_RESTORE);
        return resourceComponent != null ? resourceComponent.eatSeconds() : 32;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        return ActionResult.PASS;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (user.getItemCooldownManager().isCoolingDown(this)) return TypedActionResult.fail(stack);

        user.setCurrentHand(hand);
        return TypedActionResult.consume(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        ResourceComponent resourceComponent = stack.get(ModDataComponents.RESOURCE_RESTORE);
        PotionQualityComponents quality = stack.get(ModDataComponents.POTION_QUALITY);

        if (MinecraftClient.getInstance().player != null) {
            ResourceType resourceType = ModEntityComponents.PLAYERCLASS.get(MinecraftClient.getInstance().player)
                    .getClassManager().getCurrentClass().getPrimaryResource();

            if (resourceComponent != null) {
                float baseAmount = resourceComponent.resourceAmount();
                float actualAmount = quality != null ? quality.getActualResourceAmount(baseAmount) : baseAmount;

                tooltip.add(Text.literal("Restores ").formatted(Formatting.GRAY)
                        .append(Text.literal((int) actualAmount + " " + resourceType.getDisplayName())
                                .withColor(resourceType.getColor())));
            }

            if (quality != null) {
                tooltip.add(Text.literal("Quality: ").formatted(Formatting.GRAY)
                        .append(quality.getDisplayText()));
                tooltip.add(Text.literal("Effectiveness: ").formatted(Formatting.GRAY)
                        .append(Text.literal((int)(quality.effectiveness() * 100) + "%")
                                .formatted(Formatting.YELLOW)));
            }
        }
    }
}
