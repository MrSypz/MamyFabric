package com.sypztep.mamy.client.event.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sypztep.mamy.client.ModKeyBindings;
import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.component.living.PlayerStanceComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.skill.ClientSkillCooldowns;
import com.sypztep.mamy.common.system.skill.Skill;
import com.sypztep.mamy.common.init.ModClassesSkill;
import com.sypztep.mamy.common.system.skill.SkillUsabilityChecker;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public final class SkillHudOverlayRenderer {
    // Skill hotbar constants
    private static final int SKILL_SLOT_SIZE = 22;
    private static final int SKILL_SLOT_SPACING = 22;
    private static final int HOTBAR_MARGIN_BOTTOM = 30;

    // Animation constants
    private static final float FADE_DURATION = 0.5f;

    // Minecraft hotbar textures
    private static final Identifier HOTBAR_SLOT_TEXTURE = Identifier.ofVanilla("textures/gui/sprites/hud/hotbar.png");
    private static final Identifier HOTBAR_SELECTION_TEXTURE = Identifier.ofVanilla("hud/hotbar_selection");

    // Animation state
    private static float fadeOffset = 1.0f;
    private static boolean shouldBeVisible = false;
    private static boolean lastCombatStance = false;

    public static void register() {
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
            shouldBeVisible = currentCombatStance;
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

            if (direction > 0 && fadeOffset > targetFadeOffset) fadeOffset = targetFadeOffset;
            else if (direction < 0 && fadeOffset < targetFadeOffset) fadeOffset = targetFadeOffset;
        }
    }

    private static void renderStanceIndicator(DrawContext context, PlayerStanceComponent stanceComponent) {
        int screenWidth = context.getScaledWindowWidth();
        Text stanceText = stanceComponent.isInCombatStance() ?
                Text.literal("⚔ COMBAT").formatted(Formatting.RED, Formatting.BOLD) :
                Text.literal("✋ NORMAL").formatted(Formatting.GRAY);
        int xPadding = 5;

        int stanceX = screenWidth - MinecraftClient.getInstance().textRenderer.getWidth(stanceText) - xPadding;
        int stanceY = 10;

        context.drawText(MinecraftClient.getInstance().textRenderer, stanceText, stanceX, stanceY, 0xFFFFFF, true);
    }

    private static void renderSkillHotbar(DrawContext context, PlayerClassComponent classComponent) {
        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();

        // Calculate hotbar position
        int totalHotbarHeight = 8 * SKILL_SLOT_SPACING;
        int availableHeight = screenHeight - HOTBAR_MARGIN_BOTTOM * 2;

        int hotbarStartY = Math.max(HOTBAR_MARGIN_BOTTOM,
                screenHeight - HOTBAR_MARGIN_BOTTOM - totalHotbarHeight);

        // Position hotbar with fade animation
        int baseHotbarX = screenWidth - SKILL_SLOT_SIZE + 1;
        int fadeDistance = 50;
        int animatedHotbarX = baseHotbarX + (int) (fadeOffset * fadeDistance);

        float alpha = 1.0f - fadeOffset;

        // Get bound skills
        Identifier[] boundSkills = classComponent.getClassManager().getAllBoundSkills();

        // Enable blending
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);

        // Render skill slots
        for (int i = 0; i < 8; i++) {
            int slotY = hotbarStartY + (int) (i * SKILL_SLOT_SPACING * (1.0f - fadeOffset * 0.1f));
            renderSkillSlot(context, animatedHotbarX, slotY, i, boundSkills[i], alpha, classComponent);
        }

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }

    private static void renderSkillSlot(DrawContext context, int x, int y, int slotIndex, Identifier skillId, float alpha, PlayerClassComponent classComponent) {
        MinecraftClient client = MinecraftClient.getInstance();

        // Check if this skill slot is visually "pressed"
        boolean isPressed = ModKeyBindings.isKeyVisuallyPressed(slotIndex);

        // Determine if skill should be dimmed
        boolean shouldDim = false;
        if (skillId != null && client.player != null) {
            int skillLevel = classComponent.getSkillLevel(skillId);
            shouldDim = SkillUsabilityChecker.shouldDimSkillInUI(client.player, skillId, skillLevel);
        }

        // Apply dimming to slot rendering
        float slotAlpha = shouldDim ? alpha * 0.4f : alpha;
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, slotAlpha);

        // Render hotbar slot
        context.drawTexture(HOTBAR_SLOT_TEXTURE, x, y, 0, 0, SKILL_SLOT_SIZE - 1, SKILL_SLOT_SIZE, 182, 22);

        // Render selection highlight if pressed (with dimming)
        if (isPressed) {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, slotAlpha);
            context.drawGuiTexture(HOTBAR_SELECTION_TEXTURE, x - 1, y - 1, SKILL_SLOT_SIZE + 2, SKILL_SLOT_SIZE + 2);
        }

        // Reset shader color for content rendering
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);

        // Render skill content
        if (skillId != null) {
            Skill skill = ModClassesSkill.getSkill(skillId);
            if (skill != null) {
                renderSkillIcon(context, x + 3, y + 3, skill, shouldDim);
                renderSkillOverlays(context, x + 3, y + 3, skill, classComponent, shouldDim);
            }
        }

        // Render keybinding text
        String keyText = getKeybindDisplayName(slotIndex);
        int baseKeyTextColor = isPressed ? 0xFFFFFF00 : 0xFFAAAAAA;

        // Apply dimming to text color
        if (shouldDim) {
            baseKeyTextColor = 0xFF666666; // Darker gray for dimmed state
        }

        int alphaValue = (int) (alpha * 255);
        int finalKeyTextColor = (baseKeyTextColor & 0x00FFFFFF) | (alphaValue << 24);

        int keyTextWidth = client.textRenderer.getWidth(keyText);
        int keyX = Math.max(4, x - keyTextWidth - 6);
        int keyY = y + (SKILL_SLOT_SIZE - client.textRenderer.fontHeight) / 2;

        context.drawText(client.textRenderer, Text.literal(keyText), keyX, keyY, finalKeyTextColor, true);

        // Render slot number
        String slotNum = String.valueOf(slotIndex + 1);
        int baseSlotNumColor = shouldDim ? 0xFF555555 : 0xFF888888;
        int finalSlotNumColor = (baseSlotNumColor & 0x00FFFFFF) | (alphaValue << 24);

        context.drawText(client.textRenderer, Text.literal(slotNum),
                x + SKILL_SLOT_SIZE - client.textRenderer.getWidth(slotNum) - 2,
                y + 2, finalSlotNumColor, false);
    }

    private static String getKeybindDisplayName(int slotIndex) {
        if (slotIndex < 4) {
            // Single keys (slots 1-4): Z, X, C, V
            KeyBinding keyBinding = ModKeyBindings.getSkillKeybinding(slotIndex);
            if (keyBinding != null) {
                return keyBinding.getBoundKeyLocalizedText().getString();
            }
            return "?";
        } else {
            // Shift combinations (slots 5-8): ⇧Z, ⇧X, ⇧C, ⇧V
            KeyBinding keyBinding = ModKeyBindings.getSkillKeybinding(slotIndex - 4);
            if (keyBinding != null) {
                return "⇧" + keyBinding.getBoundKeyLocalizedText().getString();
            }
            return "⇧?";
        }
    }

    private static void renderSkillIcon(DrawContext context, int x, int y, Skill skill, boolean shouldDim) {
        MinecraftClient client = MinecraftClient.getInstance();

        // Apply dimming to skill icon
        float iconAlpha = shouldDim ? 0.5f : 1.0f;
        RenderSystem.setShaderColor(iconAlpha, iconAlpha, iconAlpha, iconAlpha);

        if (skill.getIcon() != null) {
            context.drawGuiTexture(skill.getIcon(), x, y, SKILL_SLOT_SIZE - 6, SKILL_SLOT_SIZE - 6);
        } else {
            String skillName = skill.getName();
            String abbreviation = skillName.length() >= 2 ? skillName.substring(0, 2).toUpperCase() : skillName.toUpperCase();

            int availableSpace = SKILL_SLOT_SIZE - 6;
            int textWidth = client.textRenderer.getWidth(abbreviation);
            int textX = x + (availableSpace - textWidth) / 2;
            int textY = y + (availableSpace - client.textRenderer.fontHeight) / 2;

            // Background with dimming
            int bgColor = shouldDim ? 0x44000000 : 0x88000000;
            context.fill(textX - 1, textY - 1, textX + textWidth + 1, textY + client.textRenderer.fontHeight + 1, bgColor);

            // Text with dimming
            int textColor = shouldDim ? 0xFF888888 : 0xFFFFFFFF;
            context.drawText(client.textRenderer, Text.literal(abbreviation), textX, textY, textColor, false);
        }

        // Reset shader color
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private static void renderSkillOverlays(DrawContext context, int x, int y, Skill skill, PlayerClassComponent classComponent, boolean shouldDim) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        int availableSpace = SKILL_SLOT_SIZE - 6;
        int skillLevel = classComponent.getSkillLevel(skill.getId());

        // Check different overlay types
        float remainingCooldown = ClientSkillCooldowns.getRemaining(skill.getId());
        boolean hasInsufficientResource = false;

        // Check if skill has insufficient resource
        if (remainingCooldown <= 0) { // Only check resource if not on cooldown
            float resourceCost = skill.getResourceCost(skillLevel);
            float currentResource = classComponent.getClassManager().getCurrentResource();
            hasInsufficientResource = currentResource < resourceCost;
        }

        // Render cooldown overlay (priority over resource)
        if (remainingCooldown > 0) {
            float cooldownProgress = remainingCooldown / skill.getCooldown(skillLevel);
            int overlayHeight = (int) (availableSpace * cooldownProgress);

            context.fill(x, y + availableSpace - overlayHeight,
                    x + availableSpace, y + availableSpace,
                    0x88222222);

            if (remainingCooldown > 0.1f) {
                String cooldownText = String.format("%.1f", remainingCooldown);
                int textWidth = client.textRenderer.getWidth(cooldownText);
                context.drawText(client.textRenderer,
                        Text.literal(cooldownText),
                        x + (availableSpace - textWidth) / 2,
                        y + (availableSpace - 8) / 2,
                        0xFFFFFFFF, true);
            }
        }
        // Render resource overlay if insufficient resource and not on cooldown
        else if (hasInsufficientResource) {
            // Semi-transparent red overlay for insufficient resource
            context.fill(x, y, x + availableSpace, y + availableSpace, 0x55AA0000);

            // Show "!" indicator for insufficient resource
            String resourceIndicator = "!";
            int textWidth = client.textRenderer.getWidth(resourceIndicator);
            context.drawText(client.textRenderer,
                    Text.literal(resourceIndicator),
                    x + (availableSpace - textWidth) / 2,
                    y + (availableSpace - 8) / 2,
                    0xFFFFAAAA, true);
        }
        // Additional dimming overlay for other unusable states (like not learned, wrong class)
        else if (shouldDim && skillLevel == 0) {
            // Dark overlay for unlearned skills
            context.fill(x, y, x + availableSpace, y + availableSpace, 0x88000000);

            // Show "?" for unlearned skills
            String unlearned = "?";
            int textWidth = client.textRenderer.getWidth(unlearned);
            context.drawText(client.textRenderer,
                    Text.literal(unlearned),
                    x + (availableSpace - textWidth) / 2,
                    y + (availableSpace - 8) / 2,
                    0xFF666666, true);
        }
    }
}