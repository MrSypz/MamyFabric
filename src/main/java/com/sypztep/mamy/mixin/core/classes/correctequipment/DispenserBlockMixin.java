package com.sypztep.mamy.mixin.core.classes.correctequipment;

import com.sypztep.mamy.common.util.ClassEquipmentUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(DispenserBlock.class)
public class DispenserBlockMixin {

    /**
     * Check before dispenser activates
     */
    @Inject(method = "dispense", at = @At("HEAD"), cancellable = true)
    private void checkArmorDispenseRestrictions(ServerWorld world, BlockState state, BlockPos pos, CallbackInfo ci) {
        DispenserBlockEntity dispenser = (DispenserBlockEntity) world.getBlockEntity(pos);
        if (dispenser == null) return;

        // Get the item that would be dispensed
        ItemStack stack = dispenser.getStack(dispenser.chooseNonEmptySlot(world.random));
        if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem)) {
            return; // Not armor, let normal dispensing continue
        }

        BlockPos frontPos = pos.offset(state.get(DispenserBlock.FACING));
        Box searchBox = new Box(frontPos).expand(0.5);
        List<PlayerEntity> players = world.getEntitiesByClass(PlayerEntity.class, searchBox, player -> true);

        for (PlayerEntity player : players) {
            if (ClassEquipmentUtil.handleRestriction(player, stack, "be equipped by dispenser on")) {
                ci.cancel();
                return;
            }
        }
    }
}