package com.sypztep.mamy.mixin.core.broken.client;

import com.sypztep.mamy.common.init.ModDataComponents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    @Final
    private MatrixStack matrices;

    @Inject(at = @At("RETURN"), method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V")
    public void drawBrokenOverlay(TextRenderer textRenderer, ItemStack itemStack, int x, int y, String countOverride, CallbackInfo ci) {
        drawtextInSlot(textRenderer, itemStack, x, y, 1F);
    }

    @Unique
    public void drawtextInSlot(TextRenderer renderer, ItemStack stack, int i, int j, float scale) {
        final ClientWorld world = this.client.world;
        if (world == null || stack.isEmpty()) return;
        if (!stack.contains(ModDataComponents.BROKEN_FLAG)) return;

        DrawContext context = ((DrawContext) (Object) this);

        // Draw red "X" to indicate broken
        String brokenText = "✗"; // Or use "X"
        int color = 0xFF0000; // Red color

        this.matrices.push();
        this.matrices.scale(scale, scale, scale);
        this.matrices.translate(0.0F, 0.0F, 200.0F); // Render on top

        // Position in top-left corner
        int textX = (int) (i / scale);
        int textY = (int) (j / scale);

        // Draw the text with shadow for better visibility
        context.drawText(renderer, brokenText, textX, textY, color, true);

        this.matrices.pop();
    }
}
