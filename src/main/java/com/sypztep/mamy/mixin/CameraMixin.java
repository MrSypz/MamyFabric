package com.sypztep.mamy.mixin;

import com.sypztep.mamy.client.screen.CameraShakeManager;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Inject(method = "update", at = @At("TAIL"))
    private void applyCameraShake(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (!thirdPerson && focusedEntity instanceof PlayerEntity player) {
            CameraShakeManager shakeManager = CameraShakeManager.getInstance();
            shakeManager.tick(tickDelta);

            if (shakeManager.isShaking()) {
                Camera camera = (Camera) (Object) this;

                Vec3d playerPos = player.getPos();

                float shakeX = shakeManager.getShakeOffsetWithDistance(true, playerPos.x, playerPos.y, playerPos.z);
                float shakeY = shakeManager.getShakeOffsetWithDistance(false, playerPos.x, playerPos.y, playerPos.z);

                this.setRotation(camera.getYaw() + (shakeX), camera.getPitch() + (shakeY));
            }
        }
    }
}