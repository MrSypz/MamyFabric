package com.sypztep.mamy.mixin.core.classes.theif.hiding;

import com.sypztep.mamy.common.component.living.LivingHidingComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.init.ModStatusEffects;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBlock.class)
public class AbstractBlockMixin {
    @Inject(method = "onStateReplaced", at = @At("HEAD"))
    private void unhidingwhenblockbreak(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved, CallbackInfo ci) {
        if (newState.isAir()) {
            world.getPlayers().forEach(player -> {
                LivingHidingComponent hiding = ModEntityComponents.HIDING.get(player);
                if (hiding.getHiddingPos() != null && hiding.getHiddingPos().equals(pos)) {
                    hiding.unHidden();
                    player.removeStatusEffect(ModStatusEffects.HIDING);
                    player.sendMessage(Text.literal("The ground crumbles beneath you!").formatted(Formatting.RED), true);
                }
            });
        }
    }
}
