package com.sypztep.mamy.mixin.core.classes.theif.hiding;

import com.sypztep.mamy.common.component.living.LivingHidingComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.init.ModStatusEffects;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBlock.class)
public class AbstractBlockMixin {
	@Inject(method = "onStateReplaced", at = @At("HEAD"))
	private void enchancement$buryEntity(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved, CallbackInfo ci) {
		if (state.equals(newState)) return;
		world.getEntitiesByClass(LivingEntity.class, new Box(pos), entity -> !entity.isDead()).forEach(foundEntity -> {
			LivingHidingComponent buryComponent = ModEntityComponents.HIDING.get(foundEntity);
			if (buryComponent.getHiddingPos() != null && buryComponent.getHiddingPos().equals(pos)) {
				if (newState.isAir() || !newState.isOpaque() || newState.getBlock().getBlastResistance() < 1.0f) {
					buryComponent.unbury();
					if (foundEntity instanceof PlayerEntity player) {
						player.removeStatusEffect(ModStatusEffects.HIDING);
						player.sendMessage(Text.literal("The ground crumbles beneath you!").formatted(Formatting.RED), true);
					}
				}
			}
		});
	}
}
