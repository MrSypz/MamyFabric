package com.sypztep.mamy.client.sound;

import com.sypztep.mamy.common.entity.skill.ThunderSphereEntity;
import com.sypztep.mamy.common.init.ModSoundEvents;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.sound.SoundCategory;

public class ThunderSphereSoundInstance extends MovingSoundInstance {
    private final ThunderSphereEntity entity;
    private float fadeTimer = 0;
    private boolean isFadingOut = false;

    public ThunderSphereSoundInstance(ThunderSphereEntity entity) {
        super(ModSoundEvents.ENTITY_ELECTRIC_SPARK, SoundCategory.PLAYERS, entity.getRandom());
        this.entity = entity;
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = 1f;
        this.pitch = 1.0f + (entity.getRandom().nextFloat() - 0.5f) * 0.2f; // Slight pitch variation
        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();
    }
    @Override
    public void tick() {
        if (entity.isRemoved()) {
            if (!isFadingOut) {
                isFadingOut = true;
                fadeTimer = 0;
            }
        }

        if (isFadingOut) {
            fadeTimer += 1;
            // Fade out over 10 ticks (0.5 seconds)
            float fadeProgress = fadeTimer / 10.0f;
            this.volume = Math.max(0, 1 * (1.0f - fadeProgress));

            if (fadeProgress >= 1.0f) {
                this.setDone(); // Stop the sound
            }
        } else {
            this.x = entity.getX();
            this.y = entity.getY();
            this.z = entity.getZ();

            // Vary volume and pitch based on entity state
            float basePitch = 1.0f;

            // Increase volume and pitch when moving faster
            double velocity = entity.getVelocity().length();
            float velocityMultiplier = (float) Math.min(1.5f, 1.0f + velocity * 0.5f);

            this.volume *= velocityMultiplier;
            this.pitch = basePitch + (velocityMultiplier - 1.0f) * 0.3f;

            // Add slight random variation for electric crackling effect
            this.pitch += (entity.getRandom().nextFloat() - 0.5f) * 0.1f;
            this.volume += (entity.getRandom().nextFloat() - 0.5f) * 0.05f;

            // Clamp values
            this.volume = Math.max(0.1f, Math.min(0.6f, this.volume));
            this.pitch = Math.max(0.8f, Math.min(1.5f, this.pitch));
        }
    }

    @Override
    public boolean shouldAlwaysPlay() {
        return true; // Always play this sound even if there are many sounds
    }

    @Override
    public boolean canPlay() {
        return !entity.isRemoved();
    }
}