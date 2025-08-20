package com.sypztep.mamy.common.system.skill;

import com.sypztep.mamy.common.payload.UseSkillPayloadC2S;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import com.sypztep.mamy.client.animation.SkillAnimationManager;

@Environment(EnvType.CLIENT)
public class SkillCastingManager {
    private static SkillCastingManager instance;

    private boolean isCasting = false;
    private Identifier currentSkillId;
    private int castTicks = 0;
    private int maxCastTicks = 0;

    public static SkillCastingManager getInstance() {
        if (instance == null) instance = new SkillCastingManager();
        return instance;
    }

    public boolean startCasting(Identifier skillId, int skillLevel) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return false;

        // Check for cooldown FIRST
        float remainingCooldown = ClientSkillCooldowns.getRemaining(skillId);
        if (remainingCooldown > 0) {
            return false;
        }

        Skill skill = SkillRegistry.getSkill(skillId);
        if (skill == null) {
            player.sendMessage(Text.literal("Unknown skill!")
                    .formatted(Formatting.RED), true);
            return false;
        }

        // If already casting this skill, cancel it
        if (isCasting && skillId.equals(currentSkillId)) {
            cancelCast();
            return true;
        }

        if (!(skill instanceof CastableSkill castable)) {
            // Check cooldown for instant skills too
            if (remainingCooldown > 0) {
                player.sendMessage(Text.literal("Skill is on cooldown (" + String.format("%.1f", remainingCooldown) + "s)")
                        .formatted(Formatting.RED), true);
                return false;
            }
            UseSkillPayloadC2S.send(skillId); // Instant skill
            return true;
        }

        if (isCasting) cancelCast(); // Cancel current cast

        this.isCasting = true;
        this.currentSkillId = skillId;
        this.castTicks = 0;
        this.maxCastTicks = castable.getCastTime(skillLevel);

        // Start casting animation
        SkillAnimationManager.startCastingAnimation(skill);

        // Play cast start sound
        player.getWorld().playSound(player, player.getBlockPos(),
                SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS,
                0.3f, 1.2f);

        return true;
    }

    public void tick() {
        if (!isCasting) return;

        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null || !player.isAlive()) {
            cancelCast();
            return;
        }

        // Check if skill went on cooldown during casting (shouldn't happen but safety check)
        float remainingCooldown = ClientSkillCooldowns.getRemaining(currentSkillId);
        if (remainingCooldown > 0) {
            player.sendMessage(Text.literal("Cast interrupted - skill on cooldown!")
                    .formatted(Formatting.RED), true);
            cancelCast();
            return;
        }

        if (++castTicks >= maxCastTicks) {
            // Cast complete
            completeCast();
        }
    }

    private void completeCast() {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            player.getWorld().playSound(player, player.getBlockPos(),
                    SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.PLAYERS,
                    0.5f, 1.5f);
        }

        UseSkillPayloadC2S.send(currentSkillId);

        // Stop casting animation
        SkillAnimationManager.stopCastingAnimation();

        isCasting = false;
    }

    public void interruptCast() {
        if (!isCasting) return;

        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            player.sendMessage(Text.literal("Cast interrupted by damage!")
                    .formatted(Formatting.RED), true);

            player.getWorld().playSound(player, player.getBlockPos(),
                    SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), SoundCategory.PLAYERS,
                    0.3f, 0.8f);
        }

        // Stop casting animation
        SkillAnimationManager.stopCastingAnimation();

        isCasting = false;
    }

    public void cancelCast() {
        if (!isCasting) return;

        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            player.sendMessage(Text.literal("Cast cancelled")
                    .formatted(Formatting.YELLOW), true);

            player.getWorld().playSound(player, player.getBlockPos(),
                    SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS,
                    0.5f, 1.0f);
        }

        // Stop casting animation
        SkillAnimationManager.stopCastingAnimation();

        isCasting = false;
    }

    public boolean isCasting() { return isCasting; }
    public boolean shouldLockMovement() {
        if (!isCasting) return false;
        Skill skill = SkillRegistry.getSkill(currentSkillId);
        return skill instanceof CastableSkill castable && castable.shouldLockMovement();
    }
    public float getCastProgress() { return isCasting ? (float) castTicks / maxCastTicks : 0f; }
    public Identifier getCurrentCastingSkill() { return currentSkillId; }
    public int getRemainingTicks() { return isCasting ? maxCastTicks - castTicks : 0; }
}