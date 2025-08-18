package com.sypztep.mamy.mixin.vanilla.freerepair;

import com.sypztep.mamy.common.util.ClassEquipmentUtil;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.screen.*;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {

    @Shadow @Final private Property levelCost;
    @Shadow private int repairItemUsage;

    public AnvilScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(type, syncId, playerInventory, context);
    }

    /**
     * Allow taking output for broken items that can be repaired
     */
    @Inject(method = "canTakeOutput", at = @At("HEAD"), cancellable = true)
    private void canTakeOutputForBrokenItems(PlayerEntity player, boolean present, CallbackInfoReturnable<Boolean> cir) {
        ItemStack brokenItem = input.getStack(0);
        if (ClassEquipmentUtil.isBroken(brokenItem)) {
            int currentDamage = brokenItem.getDamage();

            // Allow taking if item has damage > 0 and there's repair material
            if (currentDamage > 0 && !input.getStack(1).isEmpty()) {
                cir.setReturnValue(true);
            } else {
                cir.setReturnValue(false);
            }
        }
    }

    /**
     * Set repair cost to 0 for broken items (free repairs like Dominatus)
     */
    @Inject(method = "updateResult", at = @At("TAIL"))
    private void zeroRepairCostForBrokenItems(CallbackInfo ci) {
        if (ClassEquipmentUtil.isBroken(input.getStack(0))) {
            levelCost.set(0);
        }
    }

    /**
     * Handle broken item repair calculation (similar to Dominatus refinement system)
     */
    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    private void calculateBrokenItemRepair(CallbackInfo ci) {
        ItemStack itemToRepair = this.input.getStack(0);
        ItemStack repairMaterial = this.input.getStack(1);

        if (!itemToRepair.isEmpty() && ClassEquipmentUtil.isBroken(itemToRepair)) {
            if (!repairMaterial.isEmpty() && itemToRepair.copy().getItem().canRepair(itemToRepair, repairMaterial)) {
                int damage = itemToRepair.getDamage();

                // If no damage, clear output
                if (damage <= 0) {
                    this.output.setStack(0, ItemStack.EMPTY);
                    this.levelCost.set(0);
                    ci.cancel();
                    return;
                }

                // Each material repairs 25% of max durability
                int repairPerItem = itemToRepair.getMaxDamage() / 4;

                int materialsNeeded = (int) Math.ceil((double) damage / repairPerItem);
                materialsNeeded = Math.min(materialsNeeded, repairMaterial.getCount());

                int repairAmount = Math.min(damage, repairPerItem * materialsNeeded);

                if (repairAmount > 0) {
                    ItemStack result = itemToRepair.copy();
                    result.setDamage(Math.max(0, damage - repairAmount));

                    // If fully repaired (damage = 0), remove broken status
//                    if (result.getDamage() == 0) {
                        ClassEquipmentUtil.repair(result); // if repair mean repair..
//                    }

                    this.repairItemUsage = materialsNeeded;
                    this.output.setStack(0, result);
                    this.levelCost.set(0); // Keep repair free for broken items
                } else {
                    this.output.setStack(0, ItemStack.EMPTY);
                    this.levelCost.set(0);
                }

                ci.cancel();
            }
        }
    }

    /**
     * Handle taking the output for broken items (similar to Dominatus)
     */
    @Inject(method = "onTakeOutput", at = @At("HEAD"), cancellable = true)
    private void handleBrokenItemRepairTake(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        if (!stack.isEmpty() && ClassEquipmentUtil.isBroken(input.getStack(0))) {
            ItemStack repairMaterial = this.input.getStack(1);

            // Consume repair materials
            if (this.repairItemUsage > 0) {
                if (!repairMaterial.isEmpty()) {
                    if (repairMaterial.getCount() > this.repairItemUsage) {
                        repairMaterial.decrement(this.repairItemUsage);
                    } else {
                        this.input.setStack(1, ItemStack.EMPTY);
                    }
                }
            }

            // Clear input slot
            this.input.setStack(0, ItemStack.EMPTY);
            this.levelCost.set(0);

            // Anvil damage chance (same as Dominatus)
            this.context.run((world, pos) -> {
                BlockState blockState = world.getBlockState(pos);
                if (!player.isInCreativeMode() && blockState.isIn(BlockTags.ANVIL) && player.getRandom().nextFloat() < 0.12F) {
                    BlockState damagedState = AnvilBlock.getLandingState(blockState);
                    if (damagedState == null) {
                        world.removeBlock(pos, false);
                        world.syncWorldEvent(1029, pos, 0);
                    } else {
                        world.setBlockState(pos, damagedState, 2);
                        world.syncWorldEvent(1030, pos, 0);
                    }
                } else {
                    world.syncWorldEvent(1030, pos, 0);
                }
            });

            ci.cancel();
        }
    }
}