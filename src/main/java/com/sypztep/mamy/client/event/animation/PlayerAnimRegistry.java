package com.sypztep.mamy.client.event.animation;

import com.sypztep.mamy.Mamy;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.MirrorModifier;
import dev.kosmx.playerAnim.api.layered.modifier.SpeedModifier;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import net.minecraft.client.network.ClientPlayerEntity;

public class PlayerAnimRegistry {
    public static void register() {
        // Initialize the skill animation manager
        SkillAnimationManager.initialize();

        // Register test animation (keep your existing code)
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(Mamy.id("animation"), 42, (player) -> {
            if (player instanceof ClientPlayerEntity) {
                ModifierLayer<IAnimation> testAnimation = new ModifierLayer<>();
                testAnimation.addModifierBefore(new SpeedModifier(0.5f));
                testAnimation.addModifierBefore(new MirrorModifier(true));
                return testAnimation;
            }
            return null;
        });

        PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.register((player, animationStack) -> {
            ModifierLayer<IAnimation> layer = new ModifierLayer<>();
            animationStack.addAnimLayer(69, layer);
            PlayerAnimationAccess.getPlayerAssociatedData(player).set(Mamy.id("test"), layer);
        });

        // Register skill animations here
        registerSkillAnimations();
    }

    private static void registerSkillAnimations() {
    }
}