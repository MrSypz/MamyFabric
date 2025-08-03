package com.sypztep.mamy.mixin.core.altfeature;

import com.sypztep.mamy.client.screen.overlay.IconOverlayManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (IconOverlayManager.isOverlayMode() && action == 1) { // GLFW_PRESS
            if (button == 0) {
                if (IconOverlayManager.handleIconClick(this.client)) {
                    ci.cancel();
                    return;
                }
            }
            ci.cancel();
        }
    }
    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (IconOverlayManager.isOverlayMode())
            ci.cancel();

    }
}