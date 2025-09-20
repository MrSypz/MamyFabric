package com.sypztep.mamy.client.event.animation;

import com.sypztep.mamy.Mamy;
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
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class NetworkAnimationManager {
    private static final String NETWORK_LAYER_KEY = "network_animation";
    private static final Map<Integer, ModifierLayer<IAnimation>> playerLayers = new HashMap<>();

    public static void initialize() {
        PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.register((player, animationStack) -> {
            // Don't initialize for the local player - SkillAnimationManager handles that
            if (player != MinecraftClient.getInstance().player) {
                ModifierLayer<IAnimation> layer = new ModifierLayer<>();
                animationStack.addAnimLayer(99, layer); // Lower priority than casting (100)
                PlayerAnimationAccess.getPlayerAssociatedData(player).set(Mamy.id(NETWORK_LAYER_KEY), layer);
                playerLayers.put(player.getId(), layer);
            }
        });
    }

    /**
     * Start playing an animation for another player
     */
    public static boolean startPlayerAnimation(PlayerEntity player, Identifier animationId) {
        if (player == null || animationId == null) return false;

        // Don't animate the local player - that's handled by SkillAnimationManager
        if (player == MinecraftClient.getInstance().player) return false;

        try {
            ModifierLayer<IAnimation> layer = playerLayers.get(player.getUuid());

            if (layer == null) {
                // Try to get from player data if not in cache
                layer = (ModifierLayer<IAnimation>) PlayerAnimationAccess
                        .getPlayerAssociatedData((AbstractClientPlayerEntity) player).get(Mamy.id(NETWORK_LAYER_KEY));

                if (layer != null) {
                    playerLayers.put(player.getId(), layer);
                }
            }

            if (layer == null) return false;

            // Stop any current animation with fade
            if (layer.getAnimation() != null) {
                layer.replaceAnimationWithFade(
                        AbstractFadeModifier.standardFadeIn(5, Ease.INOUTCUBIC), null);
            }

            // Start the new animation
            IPlayable animation = PlayerAnimationRegistry.getAnimation(animationId);
            if (animation != null) {
                layer.replaceAnimationWithFade(
                        AbstractFadeModifier.standardFadeIn(10, Ease.INOUTSINE),
                        animation.playAnimation()
                                .setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL)
                                .setFirstPersonConfiguration(new FirstPersonConfiguration()
                                        .setShowRightArm(true)
                                        .setShowLeftArm(true)
                                        .setShowLeftItem(true)
                                        .setShowRightItem(true))
                );
                return true;
            }
        } catch (Exception e) {
            Mamy.LOGGER.error("Failed to start network animation for player {}: {}", player.getName().getString(), animationId, e);
        }

        return false;
    }

    /**
     * Stop the current animation for another player
     */
    public static void stopPlayerAnimation(PlayerEntity player) {
        if (player == null || player == MinecraftClient.getInstance().player) return;

        ModifierLayer<IAnimation> layer = playerLayers.get(player.getId());

        if (layer == null) {
            // Try to get from player data if not in cache
            layer = (ModifierLayer<IAnimation>) PlayerAnimationAccess
                    .getPlayerAssociatedData((AbstractClientPlayerEntity) player).get(Mamy.id(NETWORK_LAYER_KEY));
        }

        if (layer != null && layer.getAnimation() != null) {
            layer.replaceAnimationWithFade(
                    AbstractFadeModifier.standardFadeIn(10, Ease.INOUTSINE), null);
        }
    }

    /**
     * Check if a player has an animation currently playing
     */
    public static boolean isPlayerAnimationPlaying(PlayerEntity player) {
        if (player == null) return false;

        ModifierLayer<IAnimation> layer = playerLayers.get(player.getId());

        if (layer == null) {
            layer = (ModifierLayer<IAnimation>) PlayerAnimationAccess
                    .getPlayerAssociatedData((AbstractClientPlayerEntity) player).get(Mamy.id(NETWORK_LAYER_KEY));
        }

        return layer != null && layer.getAnimation() != null;
    }

    /**
     * Clean up player data when they disconnect
     */
    public static void cleanupPlayer(int playerId) {
        playerLayers.remove(playerId);
    }
}