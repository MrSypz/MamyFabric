package com.sypztep.mamy.client.event.animation;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.common.system.crowdcontrol.CrowdControlManager.CrowdControlType;
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

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT) // TODO: fix runtimeException this thing are run on server.
public class CrowdControlAnimationManager {
    private static final String CC_LAYER_KEY = "crowd_control";
    private static ModifierLayer<IAnimation> ccLayer;

    private static final Map<CrowdControlType, Identifier> CC_ANIMATIONS = new HashMap<>();

    static {
        CC_ANIMATIONS.put(CrowdControlType.KNOCKDOWN, Mamy.id("knockdown"));
        CC_ANIMATIONS.put(CrowdControlType.BOUND, Mamy.id("bound"));
        CC_ANIMATIONS.put(CrowdControlType.STUN, Mamy.id("stun"));
        CC_ANIMATIONS.put(CrowdControlType.STIFFNESS, Mamy.id("stiffness"));
        CC_ANIMATIONS.put(CrowdControlType.FREEZING, Mamy.id("freezing"));
        CC_ANIMATIONS.put(CrowdControlType.KNOCKBACK, Mamy.id("knockback"));
        CC_ANIMATIONS.put(CrowdControlType.FLOATING, Mamy.id("floating"));
    }

    public static void initialize() {
        PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.register((player, animationStack) -> {
            if (player instanceof ClientPlayerEntity) {
                ModifierLayer<IAnimation> layer = new ModifierLayer<>();
                animationStack.addAnimLayer(200, layer);
                PlayerAnimationAccess.getPlayerAssociatedData(player).set(Mamy.id(CC_LAYER_KEY), layer);
            }
        });
    }

    public static boolean startCrowdControlAnimation(CrowdControlType ccType) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null || ccType == null) return false;

        Identifier animationId = CC_ANIMATIONS.get(ccType);
        if (animationId == null) return false;

        try {
            ccLayer = (ModifierLayer<IAnimation>) PlayerAnimationAccess
                    .getPlayerAssociatedData(player).get(Mamy.id(CC_LAYER_KEY));

            if (ccLayer == null) return false;

            if (ccLayer.getAnimation() != null) {
                ccLayer.replaceAnimationWithFade(
                        AbstractFadeModifier.standardFadeIn(5, Ease.OUTCUBIC), null);
            }

            var animation = PlayerAnimationRegistry.getAnimation(animationId);
            if (animation != null) {
                ccLayer.replaceAnimationWithFade(
                        AbstractFadeModifier.standardFadeIn(5, Ease.INOUTSINE),
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
            Mamy.LOGGER.error("Failed to start CC animation: {}", animationId, e);
        }

        return false;
    }

    public static void stopCrowdControlAnimation() {
        if (ccLayer != null && ccLayer.getAnimation() != null) {
            ccLayer.replaceAnimationWithFade(
                    AbstractFadeModifier.standardFadeIn(8, Ease.INOUTSINE), null);
        }
    }

    public static boolean isCrowdControlAnimationPlaying() {
        return ccLayer != null && ccLayer.getAnimation() != null;
    }
}