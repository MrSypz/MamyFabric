package com.sypztep.mamy.mixin.core.altfeature;

import com.sypztep.mamy.client.event.OverlayMouseHandler;
import net.minecraft.client.Keyboard;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    public void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (OverlayMouseHandler.isOverlayMode()) {
            KeyBinding.unpressAll();
            ci.cancel();
        }
    }
}
