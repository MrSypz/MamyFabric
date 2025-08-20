package com.sypztep.mamy.client.event.animation;

import com.sypztep.mamy.Mamy;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class SkillAnimationManager {
    private static final String CASTING_LAYER_KEY = "skill_casting";
    private static ModifierLayer<IAnimation> castingLayer;

    public static void initialize() {
        // Register the casting animation layer
        PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.register((player, animationStack) -> {
            if (player instanceof ClientPlayerEntity) {
                ModifierLayer<IAnimation> layer = new ModifierLayer<>();
                animationStack.addAnimLayer(100, layer); // High priority for casting
                PlayerAnimationAccess.getPlayerAssociatedData(player).set(Mamy.id(CASTING_LAYER_KEY), layer);
            }
        });
    }

    /**
     * Start playing a casting animation
     */
    public static boolean startCastAnimation(Identifier animationId) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null || animationId == null) return false;

        try {
            castingLayer = (ModifierLayer<IAnimation>) PlayerAnimationAccess
                    .getPlayerAssociatedData(player).get(Mamy.id(CASTING_LAYER_KEY));

            if (castingLayer == null) return false;

            // Stop any current animation with fade
            if (castingLayer.getAnimation() != null) {
                castingLayer.replaceAnimationWithFade(
                        AbstractFadeModifier.standardFadeIn(5, Ease.LINEAR), null);
            }

            // Start the new casting animation
            var animation = PlayerAnimationRegistry.getAnimation(animationId);
            if (animation != null) {
                castingLayer.replaceAnimationWithFade(
                        AbstractFadeModifier.standardFadeIn(10, Ease.INOUTSINE),
                        animation.playAnimation()
                                .setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL)
                                .setFirstPersonConfiguration(new FirstPersonConfiguration()
                                        .setShowRightArm(true)
                                        .setShowLeftArm(true)
                                        .setShowLeftItem(false)
                                        .setShowRightItem(false))
                );
                return true;
            }
        } catch (Exception e) {
            Mamy.LOGGER.error("Failed to start cast animation: {}", animationId, e);
        }

        return false;
    }

    /**
     * Stop the current casting animation
     */
    public static void stopCastAnimation() {
        if (castingLayer != null && castingLayer.getAnimation() != null) {
            castingLayer.replaceAnimationWithFade(
                    AbstractFadeModifier.standardFadeIn(10, Ease.INOUTSINE), null);
        }
    }

    /**
     * Check if a casting animation is currently playing
     */
    public static boolean isCastAnimationPlaying() {
        return castingLayer != null && castingLayer.getAnimation() != null;
    }
}