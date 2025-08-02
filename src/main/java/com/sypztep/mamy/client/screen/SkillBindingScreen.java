package com.sypztep.mamy.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sypztep.mamy.client.screen.widget.ActionWidgetButton;
import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.payload.BindSkillPayloadC2S;
import com.sypztep.mamy.common.system.skill.Skill;
import com.sypztep.mamy.common.system.skill.SkillRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import sypztep.tyrannus.client.screen.widget.ScrollBehavior;
import sypztep.tyrannus.client.util.DrawContextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SkillBindingScreen extends Screen {
    // Base dimensions - will be adjusted for screen size
    private static final int BASE_BACKGROUND_WIDTH = 360;
    private static final int BASE_BACKGROUND_HEIGHT = 280;
    private static final int SKILL_SLOT_SIZE = 28;
    private static final int SKILL_BUTTON_SIZE = 24;

    // Custom styling similar to PlayerInfoScreen
    private static final int BACKGROUND_COLOR = 0xF0121212;
    private static final int PANEL_COLOR = 0xFF1E1E1E;
    private static final int SLOT_COLOR = 0xFF2A2A2A;
    private static final int SELECTED_COLOR = 0xFF4CAF50;
    private static final int BOUND_SLOT_COLOR = 0xFF3F51B5;
    private static final int HOVER_COLOR = 0xFF616161;

    private final MinecraftClient client;
    private int backgroundX, backgroundY;
    private int backgroundWidth, backgroundHeight; // Dynamic sizing

    // Skill data - FIXED: Changed from List to ArrayList to handle Set conversion
    private List<Identifier> learnedSkills = new ArrayList<>();
    private Identifier[] boundSkills = new Identifier[8];
    private Identifier selectedSkill = null;

    // Mouse tracking
    private int hoveredSlot = -1;
    private int hoveredSkill = -1;

    // UI Components
    private ScrollBehavior learnedSkillsScroll;
    private List<SkillSlotButton> skillSlotButtons = new ArrayList<>();
    private ActionWidgetButton closeButton;
    private ActionWidgetButton clearAllButton;

    // Slot positions
    private static final String[] SLOT_KEYS = {"Z", "X", "C", "V", "⇧Z", "⇧X", "⇧C", "⇧V"};

    public SkillBindingScreen(MinecraftClient client) {
        super(Text.literal("Skill Binding"));
        this.client = client;
        loadSkillData();

        // Initialize scroll behavior
        learnedSkillsScroll = new ScrollBehavior();
    }

    private void loadSkillData() {
        if (client.player == null) return;

        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(client.player);
        // FIXED: Convert Set to List
        learnedSkills = new ArrayList<>(classComponent.getLearnedSkills());
        boundSkills = classComponent.getClassManager().getAllBoundSkills();
    }

    @Override
    protected void init() {
        super.init();

        // Calculate responsive dimensions
        backgroundWidth = Math.min(BASE_BACKGROUND_WIDTH, (int)(width * 0.5f)); // Max 90% of screen width
        backgroundHeight = Math.min(BASE_BACKGROUND_HEIGHT, (int)(height * 0.3f)); // Max 90% of screen height

        // Ensure minimum usable size
        backgroundWidth = Math.max(backgroundWidth, 280);
        backgroundHeight = Math.max(backgroundHeight, 220);

        backgroundX = (width - backgroundWidth) / 2;
        backgroundY = (height - backgroundHeight) / 2;

        // Initialize UI components
        initializeButtons();
        setupScrollBehavior();
    }

    private void initializeButtons() {
        skillSlotButtons.clear();

        // Get stats component for buttons (needed for ActionWidgetButton)
        LivingLevelComponent statsComponent = ModEntityComponents.LIVINGLEVEL.get(client.player);

        // Create skill slot buttons (8 slots in 2 rows)
        for (int i = 0; i < 8; i++) {
            int col = i % 4;
            int row = i / 4;
            int slotX = backgroundX + 20 + col * (SKILL_SLOT_SIZE + 8);
            int slotY = backgroundY + 55 + row * (SKILL_SLOT_SIZE + 16);

            SkillSlotButton slotButton = new SkillSlotButton(
                    slotX, slotY, SKILL_SLOT_SIZE, SKILL_SLOT_SIZE,
                    Text.empty(), i, statsComponent, client
            );

            skillSlotButtons.add(slotButton);
            addDrawableChild(slotButton);
        }

        closeButton = new ControlButton(
                backgroundX + backgroundWidth - 60, backgroundY + backgroundHeight - 30,
                50, 20, Text.literal("Close"), statsComponent, client
        ) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                close();
            }
        };
        addDrawableChild(closeButton);

        clearAllButton = new ControlButton(
                backgroundX + 10, backgroundY + backgroundHeight - 30,
                60, 20, Text.literal("Clear All"), statsComponent, client
        ) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                clearAllSkills();
            }
        };
        addDrawableChild(clearAllButton);
    }

    private void setupScrollBehavior() {
        // Set up scroll area for learned skills - adjust for dynamic height
        int scrollX = backgroundX + 10;
        int scrollY = backgroundY + 165;
        int scrollWidth = backgroundWidth - 20;
        int scrollHeight = Math.max(40, backgroundHeight - 220); // Adaptive height

        learnedSkillsScroll.setBounds(scrollX, scrollY, scrollWidth, scrollHeight);

        // Calculate content height based on number of skills
        int cols = scrollWidth / (SKILL_BUTTON_SIZE + 4);
        int rows = (learnedSkills.size() + cols - 1) / cols; // Ceiling division
        int contentHeight = rows * (SKILL_BUTTON_SIZE + 4);
        learnedSkillsScroll.setContentHeight(contentHeight);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        DrawContextUtils.fillScreen(context, BACKGROUND_COLOR);
        DrawContextUtils.drawRect(context, backgroundX - 1, backgroundY - 1, backgroundWidth + 2, backgroundHeight + 2, PANEL_COLOR);
        drawBorder(context, backgroundX, backgroundY, backgroundWidth, backgroundHeight,0, 0xFF444444);
//        drawBorder(context, backgroundX, backgroundY, backgroundWidth, backgroundHeight, 0xFF444444);

    }
    public void drawBorder(DrawContext context, int x, int y, int width, int height, int z, int color) {
        Matrix4f matrix4f = context.getMatrices().peek().getPositionMatrix();
        VertexConsumer vertexConsumer = context.getVertexConsumers().getBuffer(RenderLayer.getGui());

        // Top border
        vertexConsumer.vertex(matrix4f, (float)x, (float)y, (float)z).color(color);
        vertexConsumer.vertex(matrix4f, (float)x, (float)(y + 1), (float)z).color(color);
        vertexConsumer.vertex(matrix4f, (float)(x + width), (float)(y + 1), (float)z).color(color);
        vertexConsumer.vertex(matrix4f, (float)(x + width), (float)y, (float)z).color(color);

        // Bottom border
        vertexConsumer.vertex(matrix4f, (float)x, (float)(y + height - 1), (float)z).color(color);
        vertexConsumer.vertex(matrix4f, (float)x, (float)(y + height), (float)z).color(color);
        vertexConsumer.vertex(matrix4f, (float)(x + width), (float)(y + height), (float)z).color(color);
        vertexConsumer.vertex(matrix4f, (float)(x + width), (float)(y + height - 1), (float)z).color(color);

        // Left border
        vertexConsumer.vertex(matrix4f, (float)x, (float)y, (float)z).color(color);
        vertexConsumer.vertex(matrix4f, (float)x, (float)(y + height), (float)z).color(color);
        vertexConsumer.vertex(matrix4f, (float)(x + 1), (float)(y + height), (float)z).color(color);
        vertexConsumer.vertex(matrix4f, (float)(x + 1), (float)y, (float)z).color(color);

        // Right border
        vertexConsumer.vertex(matrix4f, (float)(x + width - 1), (float)y, (float)z).color(color);
        vertexConsumer.vertex(matrix4f, (float)(x + width - 1), (float)(y + height), (float)z).color(color);
        vertexConsumer.vertex(matrix4f, (float)(x + width), (float)(y + height), (float)z).color(color);
        vertexConsumer.vertex(matrix4f, (float)(x + width), (float)y, (float)z).color(color);
        // tryDraw method
        RenderSystem.disableDepthTest();
        context.getVertexConsumers().draw();
        RenderSystem.enableDepthTest();
    }
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        // Header
        drawHeader(context);

        // Skill slots section
        drawSkillSlotsSection(context);

        // Learned skills section with scroll
        drawLearnedSkillsSection(context, mouseX, mouseY, delta);

        // Instructions
        drawInstructions(context);

        // Tooltips (render last, on top of everything)
        renderTooltips(context, mouseX, mouseY);
    }

    private void drawHeader(DrawContext context) {
        int headerY = backgroundY + 8;
        context.drawCenteredTextWithShadow(textRenderer, title, backgroundX + backgroundWidth / 2, headerY, 0xFFFFFF);

        // Decorative lines
        int lineY = headerY + 12;
        int lineStartX = backgroundX + 20;
        int lineEndX = backgroundX + backgroundWidth - 20;
        DrawContextUtils.renderHorizontalLineWithCenterGradient(context, lineStartX, lineY,
                lineEndX - lineStartX, 1, 1, 0xFFFFFFFF, 0x00FFFFFF);
    }

    private void drawSkillSlotsSection(DrawContext context) {
        int sectionY = backgroundY + 35;
        context.drawTextWithShadow(textRenderer, Text.literal("Skill Slots"), backgroundX + 10, sectionY, 0xFFFFFF);

        // Update skill slot button states
        for (int i = 0; i < skillSlotButtons.size(); i++) {
            SkillSlotButton button = skillSlotButtons.get(i);
            button.updateSkillState(boundSkills[i], selectedSkill);
        }
    }

    private void drawLearnedSkillsSection(DrawContext context, int mouseX, int mouseY, float delta) {
        int sectionY = backgroundY + 145;
        context.drawTextWithShadow(textRenderer, Text.literal("Learned Skills"), backgroundX + 10, sectionY, 0xFFFFFF);

        // Set up scrollable area - use dynamic dimensions
        int scrollX = backgroundX + 10;
        int scrollY = sectionY + 20;
        int scrollWidth = backgroundWidth - 20;
        int scrollHeight = Math.max(40, backgroundHeight - 220);

        // Update scroll behavior
        learnedSkillsScroll.update(context, mouseX, mouseY, delta);

        // Enable scissor for clipping
        context.enableScissor(scrollX, scrollY, scrollX + scrollWidth, scrollY + scrollHeight);

        // Render learned skills with scroll offset
        renderLearnedSkills(context, scrollX, scrollY, scrollWidth, mouseX, mouseY);

        // Disable scissor
        context.disableScissor();
    }

    private void renderLearnedSkills(DrawContext context, int startX, int startY, int width, int mouseX, int mouseY) {
        hoveredSkill = -1;

        int cols = width / (SKILL_BUTTON_SIZE + 4);
        int scrollOffset = (int) learnedSkillsScroll.getScrollAmount();

        for (int i = 0; i < learnedSkills.size(); i++) {
            int col = i % cols;
            int row = i / cols;
            int skillX = startX + col * (SKILL_BUTTON_SIZE + 4);
            int skillY = startY + row * (SKILL_BUTTON_SIZE + 4) - scrollOffset;

            int visibleHeight = Math.max(40, backgroundHeight - 220);
            if (skillY + SKILL_BUTTON_SIZE < startY || skillY > startY + visibleHeight) continue;

            boolean isHovered = mouseX >= skillX && mouseX < skillX + SKILL_BUTTON_SIZE &&
                    mouseY >= skillY && mouseY < skillY + SKILL_BUTTON_SIZE;

            if (isHovered) hoveredSkill = i;

            drawLearnedSkill(context, skillX, skillY, learnedSkills.get(i), isHovered);
        }
    }

    private void drawLearnedSkill(DrawContext context, int x, int y, Identifier skillId, boolean isHovered) {
        boolean isSelected = skillId.equals(selectedSkill);
        boolean isAlreadyBound = Arrays.asList(boundSkills).contains(skillId);

        // Skill background color
        int skillColor = SLOT_COLOR;
        if (isSelected) {
            skillColor = SELECTED_COLOR;
        } else if (isAlreadyBound) {
            skillColor = 0xFF8E24AA; // Purple for already bound
        } else if (isHovered) {
            skillColor = HOVER_COLOR;
        }

        // Draw skill background
        DrawContextUtils.drawRect(context, x, y, SKILL_BUTTON_SIZE, SKILL_BUTTON_SIZE, skillColor);
        context.drawBorder(x, y, SKILL_BUTTON_SIZE, SKILL_BUTTON_SIZE, 0xFF444444);

        // Draw skill content with level
        drawSkillInSlot(context, x + 1, y + 1, skillId);

        // Draw skill level
        if (client.player != null) {
            PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(client.player);
            int skillLevel = classComponent.getSkillLevel(skillId);
            if (skillLevel > 0) {
                String levelText = String.valueOf(skillLevel);
                context.drawText(textRenderer, Text.literal(levelText),
                        x + SKILL_BUTTON_SIZE - textRenderer.getWidth(levelText) - 2,
                        y + 2, 0xFFFFFF00, true);
            }
        }

        // Draw "bound" indicator if already bound
        if (isAlreadyBound) {
            context.drawText(textRenderer, Text.literal("✔"), x + 2, y + SKILL_BUTTON_SIZE - 10, 0xFF00FF00, false);
        }
    }

    private void drawSkillInSlot(DrawContext context, int x, int y, Identifier skillId) {
        drawSkillInSlot(context,x,y,6,skillId);
    }

    private void drawSkillInSlot(DrawContext context, int x, int y,int size, Identifier skillId) {
        if (skillId == null) return;

        Skill skill = SkillRegistry.getSkill(skillId);
        if (skill == null) return;

        int iconSize = SKILL_SLOT_SIZE - size;

        if (skill.getIcon() != null) {
            context.drawGuiTexture(skill.getIcon(), x, y, iconSize, iconSize);
        } else {
            drawSkillText(context, x, y, skill, iconSize);
        }
    }

    private void drawSkillText(DrawContext context, int x, int y, Skill skill, int iconSize) {
        String skillName = skill.getName();
        String abbreviation = skillName.length() >= 2 ? skillName.substring(0, 2).toUpperCase() : skillName.toUpperCase();

        int textWidth = textRenderer.getWidth(abbreviation);
        int textX = x + (iconSize - textWidth) / 2;
        int textY = y + (iconSize - textRenderer.fontHeight) / 2;

        // Text background for readability
        context.fill(textX - 1, textY - 1, textX + textWidth + 1, textY + textRenderer.fontHeight + 1, 0x88000000);
        context.drawText(textRenderer, Text.literal(abbreviation), textX, textY, 0xFFFFFFFF, false);
    }

    private void drawInstructions(DrawContext context) {
        String instruction;
        if (selectedSkill == null) {
            instruction = "Select a skill, then click a slot to bind";
        } else {
            instruction = "Click a slot to bind selected skill • Right-click to unbind";
        }

        context.drawCenteredTextWithShadow(textRenderer, Text.literal(instruction),
                backgroundX + backgroundWidth / 2, backgroundY + backgroundHeight - 20, 0xFFAAAAAA);
    }

    private void renderTooltips(DrawContext context, int mouseX, int mouseY) {
        // Skill slot tooltips - handled by SkillSlotButton
        for (SkillSlotButton button : skillSlotButtons) {
            if (button.isMouseOver(mouseX, mouseY)) {
                button.renderTooltip(context, mouseX, mouseY);
                return;
            }
        }

        // Learned skill tooltips
        if (hoveredSkill >= 0 && hoveredSkill < learnedSkills.size()) {
            Identifier skillId = learnedSkills.get(hoveredSkill);
            Skill skill = SkillRegistry.getSkill(skillId);
            if (skill != null && client.player != null) {
                PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(client.player);
                int skillLevel = classComponent.getSkillLevel(skillId);

                List<Text> tooltip = new ArrayList<>();
                tooltip.add(Text.literal(skill.getName() + " (Level " + skillLevel + "/" + skill.getMaxSkillLevel() + ")")
                        .formatted(Formatting.YELLOW));
                tooltip.add(Text.literal(skill.getDescription(skillLevel)).formatted(Formatting.WHITE));

                // FIXED: Use skill level for cost and cooldown calculations
                tooltip.add(Text.literal("Cost: " + String.format("%.1f", skill.getResourceCost(skillLevel)))
                        .formatted(Formatting.BLUE));
                tooltip.add(Text.literal("Cooldown: " + String.format("%.1f", skill.getCooldown(skillLevel)) + "s")
                        .formatted(Formatting.AQUA));

                // Show upgrade info if not max level
                if (skillLevel < skill.getMaxSkillLevel()) {
                    tooltip.add(Text.literal("Upgrade Cost: " + skill.getUpgradeClassPointCost() + " points")
                            .formatted(Formatting.GOLD));
                    tooltip.add(Text.literal("Next Level: " + String.format("%.1f", skill.getResourceCost(skillLevel + 1)) + " cost, " +
                                    String.format("%.1f", skill.getCooldown(skillLevel + 1)) + "s cooldown")
                            .formatted(Formatting.GREEN));
                }

                if (Arrays.asList(boundSkills).contains(skillId)) {
                    tooltip.add(Text.literal("Already bound").formatted(Formatting.GOLD));
                }

                context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Handle learned skill clicks in scrollable area - use dynamic dimensions
        int scrollX = backgroundX + 10;
        int scrollY = backgroundY + 165;
        int scrollWidth = backgroundWidth - 20;
        int scrollHeight = Math.max(40, backgroundHeight - 220);

        if (mouseX >= scrollX && mouseX < scrollX + scrollWidth &&
                mouseY >= scrollY && mouseY < scrollY + scrollHeight) {

            int cols = scrollWidth / (SKILL_BUTTON_SIZE + 4);
            int scrollOffset = (int) learnedSkillsScroll.getScrollAmount();

            for (int i = 0; i < learnedSkills.size(); i++) {
                int col = i % cols;
                int row = i / cols;
                int skillX = scrollX + col * (SKILL_BUTTON_SIZE + 4);
                int skillY = scrollY + row * (SKILL_BUTTON_SIZE + 4) - scrollOffset;

                if (skillY + SKILL_BUTTON_SIZE < scrollY || skillY > scrollY + scrollHeight) continue;

                if (mouseX >= skillX && mouseX < skillX + SKILL_BUTTON_SIZE &&
                        mouseY >= skillY && mouseY < skillY + SKILL_BUTTON_SIZE) {

                    Identifier skillId = learnedSkills.get(i);
                    selectedSkill = skillId.equals(selectedSkill) ? null : skillId;
                    return true;
                }
            }
        }

        if (learnedSkillsScroll.handleMouseClick(mouseX, mouseY, button)) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (learnedSkillsScroll.handleScroll(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private void bindSkill(int slot, Identifier skillId) {
        if (skillId == null) return;

        // Check if skill is already bound to another slot
        for (int i = 0; i < boundSkills.length; i++) {
            if (i != slot && skillId.equals(boundSkills[i])) {
                // Unbind from previous slot first
                boundSkills[i] = null;
                BindSkillPayloadC2S.unbind(i);
                break;
            }
        }

        // Bind to new slot
        boundSkills[slot] = skillId;
        BindSkillPayloadC2S.send(slot, skillId);
        selectedSkill = null; // Clear selection after binding
    }

    private void unbindSkill(int slot) {
        if (boundSkills[slot] != null) {
            boundSkills[slot] = null;
            BindSkillPayloadC2S.unbind(slot);
        }
    }

    private void clearAllSkills() {
        boolean hadBoundSkills = false;
        for (int i = 0; i < 8; i++) {
            if (boundSkills[i] != null) {
                boundSkills[i] = null;
                BindSkillPayloadC2S.unbind(i);
                hadBoundSkills = true;
            }
        }

        // Only show message if there were actually skills to clear
        if (hadBoundSkills && client.player != null) {
            client.player.sendMessage(Text.literal("Cleared all skill bindings")
                    .formatted(Formatting.YELLOW), false);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    // ============================================================================
    // CUSTOM BUTTON CLASSES
    // ============================================================================

    private class SkillSlotButton extends ActionWidgetButton {
        private final int slotIndex;
        private Identifier boundSkill;
        private Identifier previewSkill;
        private boolean canBind;

        public SkillSlotButton(int x, int y, int width, int height, Text message, int slotIndex,
                               LivingLevelComponent stats, MinecraftClient client) {
            super(x, y, width, height, message, stats, client);
            this.slotIndex = slotIndex;
        }

        public void updateSkillState(Identifier boundSkill, Identifier selectedSkill) {
            this.boundSkill = boundSkill;
            this.previewSkill = selectedSkill;
            this.canBind = selectedSkill != null && boundSkill == null;
        }

        @Override
        protected void renderAdditionalOverlays(DrawContext context, int mouseX, int mouseY, float delta,
                                                boolean isHovered, boolean isPressed) {
            // Draw keybinding label
            String keyText = SLOT_KEYS[slotIndex];
            int keyColor = boundSkill != null ? 0xFFFFFFFF : 0xFFAAAAAA;
            context.drawText(client.textRenderer, Text.literal(keyText),
                    getX() + 2, getY() - 12, keyColor, false);

            // Draw slot number
            String slotNum = String.valueOf(slotIndex + 1);

            // Draw skill content
            if (boundSkill != null) {
                drawSkillInSlot(context, getX() + 1, getY() + 1, 3, boundSkill);

                // Draw skill level
                if (client.player != null) {
                    PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(client.player);
                    int skillLevel = classComponent.getSkillLevel(boundSkill);
                    if (skillLevel > 0) {
                        String levelText = String.valueOf(skillLevel);
                        context.drawText(client.textRenderer, Text.literal(levelText),
                                getX() + getWidth() - client.textRenderer.getWidth(levelText) - 2,
                                getY() + getHeight() - 10, 0xFFFFFF00, true);
                    }
                }
            } else if (canBind && isHovered && previewSkill != null) {
                drawSkillInSlot(context, getX() + 1, getY() + 1, 3, previewSkill);
                context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x4400FF00);
            }

            context.drawText(client.textRenderer, Text.literal(slotNum),
                    getX() + getWidth() - client.textRenderer.getWidth(slotNum) - 2,
                    getY() + 2, 0xFF888888, true);
        }

        @Override
        protected int getBackgroundColor() {
            if (canBind && isHovered()) return SELECTED_COLOR;
            if (boundSkill != null) return BOUND_SLOT_COLOR;
            return SLOT_COLOR;
        }

        @Override
        protected int getHoverBackgroundColor() {
            if (canBind) return SELECTED_COLOR;
            if (boundSkill != null) return BOUND_SLOT_COLOR;
            return HOVER_COLOR;
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!isMouseOver(mouseX, mouseY)) return false;

            if (button == 1) { // Right click - unbind
                if (boundSkill != null) {
                    unbindSkill(slotIndex);
                }
                return true;
            } else if (button == 0) { // Left click - bind
                if (selectedSkill != null) {
                    bindSkill(slotIndex, selectedSkill);
                }
                return true;
            }

            return super.mouseClicked(mouseX, mouseY, button);
        }

        public void renderTooltip(DrawContext context, int mouseX, int mouseY) {
            List<Text> tooltip = new ArrayList<>();
            tooltip.add(Text.literal("Slot " + (slotIndex + 1) + " (" + SLOT_KEYS[slotIndex] + ")")
                    .formatted(Formatting.YELLOW));

            if (boundSkill != null) {
                Skill skill = SkillRegistry.getSkill(boundSkill);
                if (skill != null && client.player != null) {
                    PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(client.player);
                    int skillLevel = classComponent.getSkillLevel(boundSkill);

                    tooltip.add(Text.literal(skill.getName() + " (Level " + skillLevel + ")")
                            .formatted(Formatting.WHITE));
                    tooltip.add(Text.literal(skill.getDescription(skillLevel)).formatted(Formatting.GRAY));
                    tooltip.add(Text.literal("Cost: " + String.format("%.1f", skill.getResourceCost(skillLevel)))
                            .formatted(Formatting.BLUE));
                    tooltip.add(Text.literal("Cooldown: " + String.format("%.1f", skill.getCooldown(skillLevel)) + "s")
                            .formatted(Formatting.AQUA));
                    tooltip.add(Text.literal("Right-click to unbind").formatted(Formatting.RED));
                }
            } else {
                tooltip.add(Text.literal("Empty slot").formatted(Formatting.GRAY));
                if (selectedSkill != null) {
                    tooltip.add(Text.literal("Click to bind selected skill").formatted(Formatting.GREEN));
                }
            }

            context.drawTooltip(client.textRenderer, tooltip, mouseX, mouseY);
        }
    }

    private static class ControlButton extends ActionWidgetButton {
        public ControlButton(int x, int y, int width, int height, Text message,
                             LivingLevelComponent stats, MinecraftClient client) {
            super(x, y, width, height, message, stats, client);
        }

        @Override
        protected int getBackgroundColor() {
            return SLOT_COLOR;
        }

        @Override
        protected int getHoverBackgroundColor() {
            return HOVER_COLOR;
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
        }
    }
}