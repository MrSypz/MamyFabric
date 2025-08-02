package com.sypztep.mamy.client.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sypztep.mamy.client.ModKeyBindings;
import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.component.living.PlayerStanceComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.skill.Skill;
import com.sypztep.mamy.common.system.skill.SkillManager;
import com.sypztep.mamy.common.system.skill.SkillRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class SkillHudOverlayRenderer {
    // Skill hotbar constants
    private static final int SKILL_SLOT_SIZE = 22;
    private static final int SKILL_SLOT_SPACING = 22;
    private static final int HOTBAR_MARGIN_BOTTOM = 30;

    // Animation constants
    private static final float FADE_DURATION = 0.5f; // 0.5 second fade in/out
    private static final float AUTO_HIDE_DELAY = 8.0f; // Hide after 8 seconds of non-combat

    // Minecraft hotbar textures
    private static final Identifier HOTBAR_SLOT_TEXTURE = Identifier.ofVanilla("textures/gui/sprites/hud/hotbar.png");
    private static final Identifier HOTBAR_SELECTION_TEXTURE = Identifier.ofVanilla("hud/hotbar_selection");

    // Keybinding references for display
    private static final KeyBinding[] SKILL_KEYBINDINGS = new KeyBinding[8];
    private static final String[] KEYBIND_DISPLAY_NAMES = {
            "Z", "X", "C", "V", "⇧Z", "⇧X", "⇧C", "⇧V"
    };

    // Animation state
    private static float fadeOffset = 1.0f; // 0 = fully visible, 1 = fully hidden
    private static float hideTimer = 0.0f;
    private static boolean shouldBeVisible = false;
    private static boolean lastCombatStance = false;

    public static void register() {
        // Initialize keybinding references
        SKILL_KEYBINDINGS[0] = ModKeyBindings.SKILL_SLOT_1;
        SKILL_KEYBINDINGS[1] = ModKeyBindings.SKILL_SLOT_2;
        SKILL_KEYBINDINGS[2] = ModKeyBindings.SKILL_SLOT_3;
        SKILL_KEYBINDINGS[3] = ModKeyBindings.SKILL_SLOT_4;
        SKILL_KEYBINDINGS[4] = ModKeyBindings.SKILL_SLOT_5;
        SKILL_KEYBINDINGS[5] = ModKeyBindings.SKILL_SLOT_6;
        SKILL_KEYBINDINGS[6] = ModKeyBindings.SKILL_SLOT_7;
        SKILL_KEYBINDINGS[7] = ModKeyBindings.SKILL_SLOT_8;

        HudRenderCallback.EVENT.register(SkillHudOverlayRenderer::render);
    }

    private static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) return;

        PlayerStanceComponent stanceComponent = ModEntityComponents.PLAYERSTANCE.get(client.player);
        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(client.player);

        // Check for stance changes
        boolean currentCombatStance = stanceComponent.isInCombatStance();
        if (currentCombatStance != lastCombatStance) {
            if (currentCombatStance) {
                // Entering combat stance - show skill hotbar immediately
                shouldBeVisible = true;
                hideTimer = 0.0f; // Reset hide timer
            } else {
                // Exiting combat stance - hide immediately (no delay)
                shouldBeVisible = false;
                hideTimer = 0.0f;
            }
            lastCombatStance = currentCombatStance;
        }

        // Force resource bar visibility based on combat stance
        if (currentCombatStance)
            ResourceBarHudRenderer.resourceBarHud.forceShow();
        else
            ResourceBarHudRenderer.resourceBarHud.forceHide();

        // Update animations
        float deltaTime = tickCounter.getTickDelta(false) / 20.0f;
        updateAnimations(deltaTime);

        // Render stance indicator
        renderStanceIndicator(context, stanceComponent);

        // Only render skill hotbar if it's at least partially visible
        if (fadeOffset < 1.0f) {
            renderSkillHotbar(context, classComponent);
        }
    }

    private static void updateAnimations(float deltaTime) {
        float targetFadeOffset = shouldBeVisible ? 0.0f : 1.0f;

        float fadeSpeed = 1.0f / FADE_DURATION;
        if (fadeOffset != targetFadeOffset) {
            float direction = targetFadeOffset > fadeOffset ? 1.0f : -1.0f;
            fadeOffset += direction * fadeSpeed * deltaTime;

            // Clamp to target
            if (direction > 0 && fadeOffset > targetFadeOffset) fadeOffset = targetFadeOffset;
            else if (direction < 0 && fadeOffset < targetFadeOffset) fadeOffset = targetFadeOffset;
        }
    }

    private static void renderStanceIndicator(DrawContext context, PlayerStanceComponent stanceComponent) {
        int screenWidth = context.getScaledWindowWidth();
        Text stanceText = stanceComponent.isInCombatStance() ?
                Text.literal("⚔ COMBAT").formatted(Formatting.RED, Formatting.BOLD) :
                Text.literal("✋ NORMAL").formatted(Formatting.GRAY);
        int XPaddle = 5;
        // Stance indicator (top right)
        int stanceX = screenWidth - MinecraftClient.getInstance().textRenderer.getWidth(stanceText) - XPaddle;
        int stanceY = 10;

        context.drawText(MinecraftClient.getInstance().textRenderer, stanceText, stanceX, stanceY, 0xFFFFFF, true);
    }

    private static void renderSkillHotbar(DrawContext context, PlayerClassComponent classComponent) {
        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();

        // Calculate hotbar position - fit properly on screen
        int totalHotbarHeight = 8 * SKILL_SLOT_SPACING;
        int availableHeight = screenHeight - HOTBAR_MARGIN_BOTTOM * 2;

        // Ensure hotbar fits on screen
        int hotbarStartY = Math.max(HOTBAR_MARGIN_BOTTOM,
                screenHeight - HOTBAR_MARGIN_BOTTOM - totalHotbarHeight);

        // Position hotbar slots at the right edge of screen with fade animation
        int baseHotbarX = screenWidth - SKILL_SLOT_SIZE + 1;
        int fadeDistance = 50; // Distance to slide in from
        int animatedHotbarX = baseHotbarX + (int) (fadeOffset * fadeDistance);

        // Calculate overall alpha for fade effect
        float alpha = 1.0f - fadeOffset;

        // Get bound skills
        Identifier[] boundSkills = classComponent.getClassManager().getAllBoundSkills();

        // Enable blending for proper transparency
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Apply global alpha for fade effect
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);

        // Render each skill slot vertically (only as many as fit on screen)
        int maxSlotsToShow = Math.min(8, (availableHeight - 20) / SKILL_SLOT_SPACING);

        for (int i = 0; i < maxSlotsToShow; i++) {
            int slotY = hotbarStartY + (int) (i * SKILL_SLOT_SPACING * (1.0f - fadeOffset * 0.1f)); // Slight spacing animation
            // Make sure slot is visible on screen
            if (slotY + SKILL_SLOT_SIZE <= screenHeight - 10) {
                renderSkillSlot(context, animatedHotbarX, slotY, i, boundSkills[i], alpha, classComponent);
            }
        }

        // Reset shader color
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }

    private static void renderSkillSlot(DrawContext context, int x, int y, int slotIndex, Identifier skillId, float alpha, PlayerClassComponent classComponent) {
        MinecraftClient client = MinecraftClient.getInstance();

        // Check if this keybinding is currently pressed
        boolean isPressed = SKILL_KEYBINDINGS[slotIndex] != null && SKILL_KEYBINDINGS[slotIndex].isPressed();

        // Render hotbar slot using Minecraft texture
        context.drawTexture(HOTBAR_SLOT_TEXTURE, x, y, 0, 0, SKILL_SLOT_SIZE - 1, SKILL_SLOT_SIZE, 182, 22);

        // Render selection highlight if pressed
        if (isPressed) {
            context.drawGuiTexture(HOTBAR_SELECTION_TEXTURE, x - 1, y - 1, SKILL_SLOT_SIZE + 2, SKILL_SLOT_SIZE + 2);
        }

        // Render skill content
        if (skillId != null) {
            Skill skill = SkillRegistry.getSkill(skillId);
            if (skill != null) {
                renderSkillIcon(context, x + 3, y + 3, skill);

                // Show cooldown overlay if applicable
                renderCooldownOverlay(context, x + 3, y + 3, skill, classComponent);
            }
        }

        // Render keybinding text (to the left of slot, but make sure it doesn't go off screen)
        String keyText = KEYBIND_DISPLAY_NAMES[slotIndex];
        int keyTextColor = isPressed ? 0xFFFFFF00 : 0xFFAAAAAA;

        // Apply alpha to keybinding text
        int alphaValue = (int) (alpha * 255);
        int finalKeyTextColor = (keyTextColor & 0x00FFFFFF) | (alphaValue << 24);

        int keyTextWidth = client.textRenderer.getWidth(keyText);

        // Position key text to the left of the slot, but keep it on screen
        int keyX = Math.max(4, x - keyTextWidth - 6); // Ensure minimum 4px from left edge
        int keyY = y + (SKILL_SLOT_SIZE - client.textRenderer.fontHeight) / 2;

        context.drawText(client.textRenderer, Text.literal(keyText), keyX, keyY, finalKeyTextColor, true);

        // Render slot number (small, in corner)
        String slotNum = String.valueOf(slotIndex + 1);
        int slotNumColor = 0xFF888888;
        int finalSlotNumColor = (slotNumColor & 0x00FFFFFF) | (alphaValue << 24);

        context.drawText(client.textRenderer, Text.literal(slotNum),
                x + SKILL_SLOT_SIZE - client.textRenderer.getWidth(slotNum) - 2,
                y + 2, finalSlotNumColor, false);
    }

    private static void renderSkillIcon(DrawContext context, int x, int y, Skill skill) {
        MinecraftClient client = MinecraftClient.getInstance();

        // Try to render skill icon first
        if (skill.getIcon() != null) {
            context.drawGuiTexture(skill.getIcon(), x, y, SKILL_SLOT_SIZE - 6, SKILL_SLOT_SIZE - 6);
        } else {
            String skillName = skill.getName();
            String abbreviation = skillName.length() >= 2 ? skillName.substring(0, 2).toUpperCase() : skillName.toUpperCase();

            // Center the text in the available space (accounting for slot padding)
            int availableSpace = SKILL_SLOT_SIZE - 6; // 3px padding on each side
            int textWidth = client.textRenderer.getWidth(abbreviation);
            int textX = x + (availableSpace - textWidth) / 2;
            int textY = y + (availableSpace - client.textRenderer.fontHeight) / 2;

            // Background for text readability
            context.fill(textX - 1, textY - 1, textX + textWidth + 1, textY + client.textRenderer.fontHeight + 1, 0x88000000);
            context.drawText(client.textRenderer, Text.literal(abbreviation), textX, textY, 0xFFFFFFFF, false);
        }
    }

    private static void renderCooldownOverlay(DrawContext context, int x, int y, Skill skill, PlayerClassComponent classComponent) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null) return;

        // Pass currentTick to the cooldown method
        float remainingCooldown = SkillManager.getRemainingCooldownSeconds(client.player, skill.getId());

        int availableSpace = SKILL_SLOT_SIZE - 6;
        int skillLevel = classComponent.getSkillLevel(skill.getId());
        if (remainingCooldown > 0) {
            float cooldownProgress = remainingCooldown / skill.getCooldown(skillLevel);
            int overlayHeight = (int) (availableSpace * cooldownProgress);

            // Draw semi-transparent cooldown overlay
            context.fill(x, y + availableSpace - overlayHeight,
                    x + availableSpace, y + availableSpace,
                    0x88222222);

            // Draw cooldown text with decimal precision
            if (remainingCooldown > 0.1f) {
                String cooldownText;
                cooldownText = String.format("%.1f", remainingCooldown);

                int textWidth = client.textRenderer.getWidth(cooldownText);
                context.drawText(client.textRenderer,
                        Text.literal(cooldownText),
                        x + (availableSpace - textWidth) / 2,
                        y + (availableSpace - 8) / 2,
                        0xFFFFFFFF, true);
            }
        }
    }
}