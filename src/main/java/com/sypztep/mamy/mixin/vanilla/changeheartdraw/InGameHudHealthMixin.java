package com.sypztep.mamy.mixin.vanilla.changeheartdraw;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sypztep.mamy.ModConfig;
import com.sypztep.mamy.client.event.hud.HealthBarRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public abstract class InGameHudHealthMixin {
    @Shadow
    public abstract TextRenderer getTextRenderer();

    @Inject(method = "renderHealthBar", at = @At("HEAD"), cancellable = true)
    private void rendernewHealthbar(DrawContext context, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, CallbackInfo ci) {
        if (!ModConfig.fixHeartScreen) return;
        HealthBarRenderer.render(context, player, x, y, this.getTextRenderer());
        boolean hardcore = player.getWorld().getLevelProperties().isHardcore();
        InGameHud.HeartType heartType = InGameHud.HeartType.fromPlayerState(player);//
        this.drawHeart(context, InGameHud.HeartType.CONTAINER, x - 6, y - 1, hardcore, blinking);
        this.drawHeart(context, heartType, x - 6 , y - 1 , hardcore, false);
        ci.cancel();
    }
    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    private static void renderNewArmor(DrawContext context, PlayerEntity player, int i, int j, int k, int x, CallbackInfo ci) {
        if (!ModConfig.fixHeartScreen) return;
        int y = i - 10;

        HealthBarRenderer.Armor.render(context, player, x, y, MinecraftClient.getInstance().textRenderer);
        ci.cancel();
    }
    @Unique
    private void drawHeart(DrawContext context, InGameHud.HeartType type, int x, int y, boolean hardcore, boolean blinking) {
        RenderSystem.enableBlend();
        context.drawGuiTexture(type.getTexture(hardcore, false, blinking), x, y, 9, 9);
        RenderSystem.disableBlend();
    }
}