package com.sypztep.mamy.common.system.skill;

import com.sypztep.mamy.common.api.entity.MovementLock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public interface CastableSkill extends MovementLock {
    int getBaseVCT(int skillLevel);

    int getBaseFCT(int skillLevel);

    @Override
    default boolean shouldLockMovement() {
        return false;
    }
    default boolean canBeInterupt() {
        return false;
    }

    default Identifier getCastAnimation() {
        return null;
    }

    default boolean hasCastAnimation() {
        return getCastAnimation() != null;
    }

    default Identifier getCastedAnimation() {
        return null;
    }

    default boolean hasCastedAnimation() {
        return getCastedAnimation() != null;
    }

    record SoundContainer(SoundEvent sound, SoundCategory category, float volume, float pitch) {
        public static SoundContainer of(SoundEvent sound, float volume, float pitch) {
            return new SoundContainer(SoundEvent.of(sound.getId()), SoundCategory.PLAYERS, volume, pitch);
        }
    }

    default SoundContainer getCastStartSound() {
        return new SoundContainer(SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 0.3f, 1.2f);
    }
    default SoundContainer getCastCompleteSound() {
        return new SoundContainer(SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.PLAYERS, 0.5f, 1.5f);
    }

    default SoundContainer getCastInterruptSound() {
        return new SoundContainer(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), SoundCategory.PLAYERS, 0.3f, 0.8f);
    }

    default SoundContainer getCastCancelSound() {
        return new SoundContainer(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 0.5f, 1.0f);
    }

    default void playSound(PlayerEntity player, SoundContainer sound) {
        if (player != null && sound != null) {
            player.getWorld().playSound(player, player.getBlockPos(), sound.sound(), sound.category(), sound.volume(), sound.pitch());
        }
    }
}