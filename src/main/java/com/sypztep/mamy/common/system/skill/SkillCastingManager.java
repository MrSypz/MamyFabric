package com.sypztep.mamy.common.system.skill;

import com.sypztep.mamy.client.event.animation.SkillAnimationManager;
import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.init.ModClassesSkill;
import com.sypztep.mamy.common.network.server.UseSkillPayloadC2S;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class SkillCastingManager {
    private static SkillCastingManager instance;

    private boolean isCasting = false;
    private Identifier currentSkillId;
    private int castTicks = 0;
    private int maxCastTicks = 0;
    private boolean hasAnimation = false;

    public static SkillCastingManager getInstance() {
        if (instance == null) instance = new SkillCastingManager();
        return instance;
    }

    public void startCasting(Identifier skillId, int skillLevel) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        // Comprehensive usability check using the new utility
        SkillUsabilityChecker.UsabilityCheck usabilityCheck = SkillUsabilityChecker.checkClientUsability(player, skillId, skillLevel);

        if (!usabilityCheck.isUsable()) {
            // Send feedback for failed attempts
            SkillUsabilityChecker.sendUsabilityFeedback(player, usabilityCheck);
            return;
        }

        Skill skill = ModClassesSkill.getSkill(skillId);
        if (skill == null) return; // Already checked in usability check, but keep for safety

        // If already casting this skill, cancel it
        if (isCasting && skillId.equals(currentSkillId)) {
            cancelCast();
            return;
        }

        // Handle instant vs castable skills
        if (!(skill instanceof CastableSkill castable)) {
            UseSkillPayloadC2S.send(skillId); // Instant skill
            return;
        }

        // Cancel any current cast before starting new one
        if (isCasting) cancelCast();

        // Initialize casting state
        this.isCasting = true;
        this.currentSkillId = skillId;
        this.castTicks = 0;
        this.maxCastTicks = CastingCalculator.calculateTotalCastTime(player, castable, skillLevel);
        this.hasAnimation = false;

        // Start casting animation if available
        if (castable.hasCastAnimation())
            this.hasAnimation = SkillAnimationManager.startCastAnimation(castable.getCastAnimation()) || SkillAnimationManager.startSkillCastedAnimation(castable.getCastAnimation()) ;


        // Play cast start sound
        castable.playSound(player, castable.getCastStartSound());
    }

    public void tick() {
        if (!isCasting) return;

        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null || !player.isAlive()) {
            cancelCast();
            return;
        }

        // Re-validate skill usability during casting (in case resources were drained by other means)
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(player);
        int skillLevel = classComponent.getSkillLevel(currentSkillId);

        SkillUsabilityChecker.UsabilityCheck check = SkillUsabilityChecker.checkClientUsability(player, currentSkillId, skillLevel);
        if (!check.isUsable()) {
            // If it's not usable anymore, interrupt the cast
            interruptCast();
            SkillUsabilityChecker.sendUsabilityFeedback(player, check);
            return;
        }

        if (++castTicks >= maxCastTicks) completeCast();
    }

    private void completeCast() {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        Skill skill = ModClassesSkill.getSkill(currentSkillId);
        boolean hasCastedAnimation = false;

        if (skill instanceof CastableSkill castable) {
            // เล่น animation หลัง cast เสร็จ
            if (castable.hasCastedAnimation())
                hasCastedAnimation = SkillAnimationManager.startSkillCastedAnimation(castable.getCastedAnimation());
            else if (hasAnimation) SkillAnimationManager.stopCastAnimation();


            castable.playSound(player, castable.getCastCompleteSound());
        } else if (hasAnimation) SkillAnimationManager.stopCastAnimation();

        UseSkillPayloadC2S.send(currentSkillId);

        // Reset casting state
        isCasting = false;
        hasAnimation = hasCastedAnimation;
    }

    public void interruptCast() {
        if (!isCasting) return;
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;
        Skill skill = ModClassesSkill.getSkill(currentSkillId);
        if (skill instanceof CastableSkill castable) castable.playSound(player, castable.getCastInterruptSound());

        // Stop animation on interrupt
        if (hasAnimation) SkillAnimationManager.stopCastAnimation();

        isCasting = false;
        hasAnimation = false;
    }

    public void cancelCast() {
        if (!isCasting) return;

        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            player.sendMessage(Text.literal("Cast cancelled").formatted(Formatting.YELLOW), true);

            Skill skill = ModClassesSkill.getSkill(currentSkillId);
            if (skill instanceof CastableSkill castable) castable.playSound(player, castable.getCastCancelSound());
        }

        if (hasAnimation) SkillAnimationManager.stopCastAnimation();

        isCasting = false;
        hasAnimation = false;
    }

    // Getters
    public boolean isCasting() { return isCasting; }

    public boolean shouldLockMovement() {
        if (!isCasting) return false;
        Skill skill = ModClassesSkill.getSkill(currentSkillId);
        return skill instanceof CastableSkill castable && castable.shouldLockMovement();
    }
    public boolean canBeInterupt() {
        if (!isCasting) return false;
        Skill skill = ModClassesSkill.getSkill(currentSkillId);
        return skill instanceof CastableSkill castable && castable.canBeInterupt();
    }

    public float getCastProgress() {
        return isCasting ? (float) castTicks / maxCastTicks : 0f;
    }

    public Identifier getCurrentCastingSkill() {
        return currentSkillId;
    }

    public int getRemainingTicks() {
        return isCasting ? maxCastTicks - castTicks : 0;
    }

    public boolean hasCastAnimation() {
        return hasAnimation;
    }
}