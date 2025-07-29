package com.sypztep.mamy.mixin.feature.screenanimation;

import com.sypztep.mamy.client.screen.camera.SpiralCameraController;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraMixin {

    @Shadow private boolean thirdPerson;

    @Inject(method = "update", at = @At("TAIL"))
    private void onCameraUpdate(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        SpiralCameraController controller = SpiralCameraController.getInstance();

        controller.update(tickDelta / 20.0f); // Convert ticks to seconds

        if (controller.isAnimating()) {
            Camera camera = (Camera) (Object) this;
            Vec3d spiralPos = controller.getCurrentCameraPos();
            this.thirdPerson = true;
            camera.setPos(spiralPos);
            camera.setRotation(controller.getCurrentYaw(), controller.getCurrentPitch());
        }
    }
}
