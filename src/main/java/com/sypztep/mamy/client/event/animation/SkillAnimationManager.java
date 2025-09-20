package com.sypztep.mamy.client.event.animation;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.network.server.PlayerAnimationSyncPayloadC2S;
import dev.kosmx.playerAnim.api.IPlayable;
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
    private static final Identifier CASTING_LAYER_KEY = Mamy.id("skill_casting");
    private static ModifierLayer<IAnimation> castingLayer;

    public static void initialize() {
        PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.register((player, animationStack) -> {
            if (player instanceof ClientPlayerEntity) {
                ModifierLayer<IAnimation> layer = new ModifierLayer<>();
                animationStack.addAnimLayer(100, layer); // High priority for casting
                PlayerAnimationAccess.getPlayerAssociatedData(player).set(CASTING_LAYER_KEY, layer);
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
                    .getPlayerAssociatedData(player).get(CASTING_LAYER_KEY);

            if (castingLayer == null) return false;

            // Stop any current animation with fade
            if (castingLayer.getAnimation() != null) {
                castingLayer.replaceAnimationWithFade(
                        AbstractFadeModifier.standardFadeIn(5, Ease.INQUAD), null);
            }

            // Start the new casting animation
            IPlayable animation = PlayerAnimationRegistry.getAnimation(animationId);
            if (animation != null) {
                castingLayer.replaceAnimationWithFade(
                        AbstractFadeModifier.standardFadeIn(10, Ease.INOUTSINE),
                        animation.playAnimation()
                                .setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL)
                                .setFirstPersonConfiguration(new FirstPersonConfiguration()
                                        .setShowRightArm(true)
                                        .setShowLeftArm(true)
                                        .setShowLeftItem(true)
                                        .setShowRightItem(true))
                );

                // Send network packet to sync with other clients
                PlayerAnimationSyncPayloadC2S.sendToServer(animationId, true);

                return true;
            }
        } catch (Exception e) {
            Mamy.LOGGER.error("Failed to start cast animation: {}", animationId, e);
        }

        return false;
    }

    /**
     * Start playing a post-cast (skill casted) animation
     */
    public static boolean startSkillCastedAnimation(Identifier animationId) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null || animationId == null) return false;

        try {
            castingLayer = (ModifierLayer<IAnimation>) PlayerAnimationAccess
                    .getPlayerAssociatedData(player).get(CASTING_LAYER_KEY);

            if (castingLayer == null) return false;

            // Start the casted animation (no fade out from previous - snap transition for continuity)
            IPlayable animation = PlayerAnimationRegistry.getAnimation(animationId);
            if (animation != null) {
                // Use minimal fade for smooth snap transition
                castingLayer.replaceAnimationWithFade(
                        AbstractFadeModifier.standardFadeIn(10, Ease.INOUTSINE),
                        animation.playAnimation()
                                .setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL)
                                .setFirstPersonConfiguration(new FirstPersonConfiguration()
                                        .setShowRightArm(true)
                                        .setShowLeftArm(true)
                                        .setShowLeftItem(true)
                                        .setShowRightItem(true))
                );

                PlayerAnimationSyncPayloadC2S.sendToServer(animationId, true);

                return true;
            }
        } catch (Exception e) {
            Mamy.LOGGER.error("Failed to start skill casted animation: {}", animationId, e);
        }

        return false;
    }

    /**
     * Stop the current casting animation
     */
    public static void stopCastAnimation() {
        if (castingLayer != null && castingLayer.getAnimation() != null) {
            castingLayer.replaceAnimationWithFade(
                    AbstractFadeModifier.standardFadeIn(5, Ease.OUTCUBIC), null);

            PlayerAnimationSyncPayloadC2S.sendToServer(Mamy.id("stop"), false);
        }
    }

    /**
     * Check if a casting animation is currently playing
     */
    public static boolean isCastAnimationPlaying() {
        return castingLayer != null && castingLayer.getAnimation() != null;
    }
}