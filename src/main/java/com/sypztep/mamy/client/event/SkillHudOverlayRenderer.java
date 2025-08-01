package com.sypztep.mamy.client.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sypztep.mamy.client.ModKeyBindings;
import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.component.living.PlayerStanceComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.skill.Skill;
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

    // Minecraft hotbar textures
    private static final Identifier HOTBAR_SLOT_TEXTURE = Identifier.ofVanilla("textures/gui/sprites/hud/hotbar.png");
    private static final Identifier HOTBAR_SELECTION_TEXTURE =  Identifier.ofVanilla("hud/hotbar_selection");

    // Keybinding references for display
    private static final KeyBinding[] SKILL_KEYBINDINGS = new KeyBinding[8];
    private static final String[] KEYBIND_DISPLAY_NAMES = {
            "Z", "X", "C", "V", "⇧Z", "⇧X", "⇧C", "⇧V"
    };

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
        if (stanceComponent.isInCombatStance())
            ResourceBarHudRenderer.resourceBarHud.forceShow();
         else
            ResourceBarHudRenderer.resourceBarHud.forceHide();

        renderStanceIndicator(context, stanceComponent);

        if (stanceComponent.isInCombatStance())
            renderSkillHotbar(context, classComponent, tickCounter.getTickDelta(false));
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

    private static void renderSkillHotbar(DrawContext context, PlayerClassComponent classComponent, float partialTicks) {
        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();

        // Calculate hotbar position - fit properly on screen
        int totalHotbarHeight = 8 * SKILL_SLOT_SPACING;
        int availableHeight = screenHeight - HOTBAR_MARGIN_BOTTOM * 2;

        // Ensure hotbar fits on screen
        int hotbarStartY = Math.max(HOTBAR_MARGIN_BOTTOM,
                screenHeight - HOTBAR_MARGIN_BOTTOM - totalHotbarHeight);

        // Position hotbar slots at the right edge of screen
        int hotbarX = screenWidth - SKILL_SLOT_SIZE + 1;

        // Get bound skills
        Identifier[] boundSkills = classComponent.getClassManager().getAllBoundSkills();

        // Enable blending for proper transparency
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Render each skill slot vertically (only as many as fit on screen)
        int maxSlotsToShow = Math.min(8, (availableHeight - 20) / SKILL_SLOT_SPACING);

        for (int i = 0; i < maxSlotsToShow; i++) {
            int slotY = hotbarStartY + (i * SKILL_SLOT_SPACING);
            // Make sure slot is visible on screen
            if (slotY + SKILL_SLOT_SIZE <= screenHeight - 10) {
                renderSkillSlot(context, hotbarX, slotY, i, boundSkills[i], partialTicks);
            }
        }

        RenderSystem.disableBlend();
    }

    private static void renderSkillSlot(DrawContext context, int x, int y, int slotIndex, Identifier skillId, float partialTicks) {
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
                renderSkillIcon(context, x + 3, y + 3, skill, partialTicks);

                // Show cooldown overlay if applicable
                renderCooldownOverlay(context, x + 3, y + 3, skill, partialTicks);
            }
        }

        // Render keybinding text (to the left of slot, but make sure it doesn't go off screen)
        String keyText = KEYBIND_DISPLAY_NAMES[slotIndex];
        int keyTextColor = isPressed ? 0xFFFFFF00 : 0xFFAAAAAA;
        int keyTextWidth = client.textRenderer.getWidth(keyText);

        // Position key text to the left of the slot, but keep it on screen
        int keyX = Math.max(4, x - keyTextWidth - 6); // Ensure minimum 4px from left edge
        int keyY = y + (SKILL_SLOT_SIZE - client.textRenderer.fontHeight) / 2;

        context.drawText(client.textRenderer, Text.literal(keyText), keyX, keyY, keyTextColor, true);

        // Render slot number (small, in corner)
        String slotNum = String.valueOf(slotIndex + 1);
        context.drawText(client.textRenderer, Text.literal(slotNum),
                x + SKILL_SLOT_SIZE - client.textRenderer.getWidth(slotNum) - 2,
                y + 2, 0xFF888888, false);
    }

    private static void renderSkillIcon(DrawContext context, int x, int y, Skill skill, float partialTicks) {
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

    private static void renderCooldownOverlay(DrawContext context, int x, int y, Skill skill, float partialTicks) {
        // TODO: Implement cooldown visualization when cooldown system is available
        // This would require tracking skill cooldowns in PlayerClassComponent

        /*
        Example implementation:

        long remainingCooldown = skill.getRemainingCooldown();
        if (remainingCooldown > 0) {
            float cooldownProgress = remainingCooldown / (float) skill.getCooldown();
            int availableSpace = SKILL_SLOT_SIZE - 6;
            int overlayHeight = (int) (availableSpace * cooldownProgress);

            // Draw semi-transparent cooldown overlay
            context.fill(x, y + availableSpace - overlayHeight,
                        x + availableSpace, y + availableSpace,
                        0x88222222);

            // Draw cooldown text (seconds remaining)
            if (remainingCooldown > 20) { // Only show if > 1 second
                String cooldownText = String.valueOf((remainingCooldown + 19) / 20);
                int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(cooldownText);
                context.drawText(MinecraftClient.getInstance().textRenderer,
                               Text.literal(cooldownText),
                               x + (availableSpace - textWidth) / 2,
                               y + (availableSpace - 8) / 2,
                               0xFFFFFFFF, true);
            }
        }
        */
    }
}