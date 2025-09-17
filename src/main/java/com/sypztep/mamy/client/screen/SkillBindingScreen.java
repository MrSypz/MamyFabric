package com.sypztep.mamy.client.screen;

import com.sypztep.mamy.client.screen.widget.ActionWidgetButton;
import com.sypztep.mamy.client.screen.widget.ScrollBehavior;
import com.sypztep.mamy.client.util.DrawContextUtils;
import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.network.server.BindSkillPayloadC2S;
import com.sypztep.mamy.common.system.skill.Skill;
import com.sypztep.mamy.common.init.ModClassesSkill;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SkillBindingScreen extends Screen {
    // UI constants - following PassiveAbilityScreen pattern
    private static final int CONTENT_PADDING = 50;
    private static final int SECTION_SPACING = 20;
    private static final int SKILL_BUTTON_SIZE = 24; // Match skill learning screen

    // Custom styling
    private static final int BACKGROUND_COLOR = 0xF0121212;
    private static final int PANEL_COLOR = 0xFF1E1E1E;
    private static final int SLOT_COLOR = 0xFF2A2A2A;
    private static final int SELECTED_COLOR = 0xFF4CAF50;
    private static final int BOUND_SLOT_COLOR = 0xFF3F51B5;
    private static final int HOVER_COLOR = 0xFF616161;

    private final MinecraftClient client;

    // Skill data
    private List<Identifier> learnedSkills = new ArrayList<>();
    private Identifier[] boundSkills = new Identifier[8];
    private Identifier selectedSkill = null;

    // Mouse tracking
    private int hoveredSkill = -1;

    // UI Components
    private final ScrollBehavior learnedSkillsScroll;
    private final List<SkillSlotButton> skillSlotButtons = new ArrayList<>();

    // Slot positions
    private static final String[] SLOT_KEYS = {"Z", "X", "C", "V", "⇧Z", "⇧X", "⇧C", "⇧V"};

    public SkillBindingScreen(MinecraftClient client) {
        super(Text.literal("Skill Binding"));
        this.client = client;
        loadSkillData();

        // Initialize scroll behavior like PassiveAbilityScreen
        learnedSkillsScroll = new ScrollBehavior()
                .setScrollbarWidth(8)
                .setScrollbarPadding(3)
                .setMinHandleSize(24);
    }

    private void loadSkillData() {
        if (client.player == null) return;

        PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(client.player);
        learnedSkills = new ArrayList<>(classComponent.getLearnedSkills(false));
        boundSkills = classComponent.getClassManager().getAllBoundSkills();
    }

    @Override
    protected void init() {
        super.init();
        initializeButtons();
        updateScrollBounds();
    }

    private void initializeButtons() {
        skillSlotButtons.clear();

        // Get stats component for buttons
        LivingLevelComponent statsComponent = ModEntityComponents.LIVINGLEVEL.get(client.player);

        // Match panel positioning - same calculations as renderSkillSlotsSection
        int sectionY = CONTENT_PADDING + 30;
        int slotsY = sectionY + 30; // Below title in panel

        for (int i = 0; i < 8; i++) {
            int col = i % 4;
            int row = i / 4;
            int slotX = CONTENT_PADDING + col * (SKILL_BUTTON_SIZE + 12);
            int slotY = slotsY + row * (SKILL_BUTTON_SIZE + 12);

            SkillSlotButton slotButton = new SkillSlotButton(
                    slotX, slotY, SKILL_BUTTON_SIZE, SKILL_BUTTON_SIZE,
                    Text.empty(), i, statsComponent, client
            );

            skillSlotButtons.add(slotButton);
            addDrawableChild(slotButton);
        }

        // Control buttons
        ActionWidgetButton closeButton = new ControlButton(
                width - CONTENT_PADDING - 60, height - CONTENT_PADDING,
                50, 20, Text.literal("Close"), statsComponent, client
        ) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                close();
            }
        };
        addDrawableChild(closeButton);

        ActionWidgetButton clearAllButton = new ControlButton(
                CONTENT_PADDING, height - CONTENT_PADDING,
                60, 20, Text.literal("Clear All"), statsComponent, client
        ) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                clearAllSkills();
            }
        };
        addDrawableChild(clearAllButton);
    }

    private void updateScrollBounds() {
        // Simple 50:50 split
        int contentWidth = width - (CONTENT_PADDING * 2);
        int rightColumnX = CONTENT_PADDING + (contentWidth / 2) + SECTION_SPACING;
        int rightColumnWidth = (contentWidth / 2) - SECTION_SPACING;

        int sectionY = CONTENT_PADDING + 30;
        int panelHeight = height - sectionY - CONTENT_PADDING + 30;
        int scrollY = sectionY + 35;
        int scrollHeight = panelHeight - 45;

        learnedSkillsScroll.setBounds(rightColumnX, scrollY, rightColumnWidth, scrollHeight);

        // Calculate content height
        int cols = Math.max(1, rightColumnWidth / (SKILL_BUTTON_SIZE + 6));
        int rows = (learnedSkills.size() + cols - 1) / cols;
        learnedSkillsScroll.setContentHeight(rows * (SKILL_BUTTON_SIZE + 6));
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Full screen background like PassiveAbilityScreen
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        DrawContextUtils.fillScreen(context, BACKGROUND_COLOR);

        // Update scroll bounds in case of window resize
        updateScrollBounds();

        // Render title
        renderTitle(context);

        // Skill slots section
        renderSkillSlotsSection(context);

        // Learned skills section with scroll
        renderLearnedSkillsSection(context, mouseX, mouseY, delta);

        // Instructions
        renderInstructions(context);

        // Tooltips (render last, on top of everything)
        renderTooltips(context, mouseX, mouseY);
        super.render(context, mouseX, mouseY, delta);
    }

    private void renderTitle(DrawContext context) {
        int titleY = CONTENT_PADDING - 20;
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, titleY, 0xFFFFFF);

        // Decorative line
        int lineY = titleY + 15;
        int lineStartX = CONTENT_PADDING + 50;
        int lineEndX = width - CONTENT_PADDING - 50;
        DrawContextUtils.renderHorizontalLineWithCenterGradient(context, lineStartX, lineY,
                lineEndX - lineStartX, 1, 1, 0xFFFFFFFF, 0x00FFFFFF);
    }

    private void renderSkillSlotsSection(DrawContext context) {
        // Simple 50:50 split
        int contentX = CONTENT_PADDING;
        int contentWidth = width - (CONTENT_PADDING * 2);
        int leftColumnWidth = (contentWidth / 2) - SECTION_SPACING;

        int sectionY = CONTENT_PADDING + 30;
        int panelHeight = height - sectionY - CONTENT_PADDING + 30;

        // Draw background panel
        DrawContextUtils.drawRect(context, contentX - 5, sectionY - 5,
                leftColumnWidth + 10, panelHeight, PANEL_COLOR);
        context.drawBorder(contentX - 5, sectionY - 5,
                leftColumnWidth + 10, panelHeight, 0xFF444444);

        // Draw title
        context.drawTextWithShadow(textRenderer, Text.literal("Skill Slots"),
                contentX + 5, sectionY + 5, 0xFFFFFF);

        // Update button positions to match panel (prevents overflow)
        int slotsY = sectionY + 30; // Below title
        for (int i = 0; i < skillSlotButtons.size(); i++) {
            int col = i % 4;
            int row = i / 4;
            int slotX = contentX + col * (SKILL_BUTTON_SIZE + 12);
            int slotY = slotsY + row * (SKILL_BUTTON_SIZE + 12);

            skillSlotButtons.get(i).setPosition(slotX, slotY);
            skillSlotButtons.get(i).updateSkillState(boundSkills[i], selectedSkill);
        }
    }

    private void renderLearnedSkillsSection(DrawContext context, int mouseX, int mouseY, float delta) {
        // Simple 50:50 split
        int contentWidth = width - (CONTENT_PADDING * 2);
        int rightColumnX = CONTENT_PADDING + (contentWidth / 2) + SECTION_SPACING;
        int rightColumnWidth = (contentWidth / 2) - SECTION_SPACING;

        int sectionY = CONTENT_PADDING + 30;
        int panelHeight = height - sectionY - CONTENT_PADDING + 30;

        // Draw background panel
        DrawContextUtils.drawRect(context, rightColumnX - 5, sectionY - 5,
                rightColumnWidth + 10, panelHeight, PANEL_COLOR);
        context.drawBorder(rightColumnX - 5, sectionY - 5,
                rightColumnWidth + 10, panelHeight, 0xFF444444);

        // Draw title
        context.drawTextWithShadow(textRenderer, Text.literal("Learned Skills"),
                rightColumnX + 5, sectionY + 5, 0xFFFFFF);

        learnedSkillsScroll.update(context, mouseX, mouseY, delta);

        // Scrollable area
        int scrollY = sectionY + 30;
        int scrollHeight = panelHeight - 40;

        context.enableScissor(rightColumnX, scrollY, rightColumnX + rightColumnWidth, scrollY + scrollHeight);
        renderLearnedSkills(context, rightColumnX, scrollY, rightColumnWidth, mouseX, mouseY);
        context.disableScissor();
    }

    private void renderLearnedSkills(DrawContext context, int startX, int startY, int width, int mouseX, int mouseY) {
        hoveredSkill = -1;

        int cols = Math.max(1, width / (SKILL_BUTTON_SIZE + 6));
        int scrollOffset = (int) learnedSkillsScroll.getScrollAmount();

        for (int i = 0; i < learnedSkills.size(); i++) {
            int col = i % cols;
            int row = i / cols;
            int skillX = startX + col * (SKILL_BUTTON_SIZE + 6);
            int skillY = startY + row * (SKILL_BUTTON_SIZE + 6) - scrollOffset;

            // Use correct height calculation
            int sectionY = CONTENT_PADDING + 30;
            int panelHeight = height - sectionY - CONTENT_PADDING + 30;
            int scrollHeight = panelHeight - 40;

            if (skillY + SKILL_BUTTON_SIZE < startY || skillY > startY + scrollHeight) continue;

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

        // Draw skill content
        drawSkillInSlot(context, x + 1, y + 1,2, skillId);

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

    private void drawSkillInSlot(DrawContext context, int x, int y,int size, Identifier skillId) {
        if (skillId == null) return;

        Skill skill = ModClassesSkill.getSkill(skillId);
        if (skill == null) return;

        int iconSize = SKILL_BUTTON_SIZE - size;

        if (skill.getIcon() != null) {
            context.drawGuiTexture(skill.getIcon(), x, y, iconSize, iconSize);
        } else {
            drawSkillText(context, x, y, skill, iconSize);
        }
    }
    private void drawSkillInSlot(DrawContext context, int x, int y, Identifier skillId) {
        drawSkillInSlot(context,x,y,3,skillId);
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

    private void renderInstructions(DrawContext context) {
        String instruction;
        if (selectedSkill == null) {
            instruction = "Select a skill, then click a slot to bind";
        } else {
            instruction = "Click a slot to bind selected skill • Right-click to unbind";
        }

        context.drawCenteredTextWithShadow(textRenderer, Text.literal(instruction),
                width / 2,  CONTENT_PADDING + 5, 0xFFAAAAAA);
    }

    private void renderTooltips(DrawContext context, int mouseX, int mouseY) {
        // Skill slot tooltips
        for (SkillSlotButton button : skillSlotButtons) {
            if (button.isMouseOver(mouseX, mouseY)) {
                button.renderTooltip(context, mouseX, mouseY);
                return;
            }
        }

        // Learned skill tooltips
        if (hoveredSkill >= 0 && hoveredSkill < learnedSkills.size()) {
            Identifier skillId = learnedSkills.get(hoveredSkill);
            Skill skill = ModClassesSkill.getSkill(skillId);
            if (skill != null && client.player != null) {
                PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(client.player);
                int skillLevel = classComponent.getSkillLevel(skillId);

                List<Text> tooltip = skill.generateTooltip(
                        client.player,
                        skillLevel,
                        true,
                        Skill.TooltipContext.BINDING_SCREEN
                );

                if (Arrays.asList(boundSkills).contains(skillId)) {
                    tooltip.add(Text.literal("Already bound").formatted(Formatting.GOLD));
                }

                context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (learnedSkillsScroll.handleMouseClick(mouseX, mouseY, button)) {
            return true;
        }

        // Simple 50:50 split
        int contentWidth = width - (CONTENT_PADDING * 2);
        int rightColumnX = CONTENT_PADDING + (contentWidth / 2) + SECTION_SPACING;
        int rightColumnWidth = (contentWidth / 2) - SECTION_SPACING;

        int sectionY = CONTENT_PADDING + 30;
        int scrollY = sectionY + 30;
        int panelHeight = height - sectionY - CONTENT_PADDING + 30;
        int scrollHeight = panelHeight - 40;

        if (mouseX >= rightColumnX && mouseX < rightColumnX + rightColumnWidth &&
                mouseY >= scrollY && mouseY < scrollY + scrollHeight) {

            int cols = Math.max(1, rightColumnWidth / (SKILL_BUTTON_SIZE + 6));
            int scrollOffset = (int) learnedSkillsScroll.getScrollAmount();

            for (int i = 0; i < learnedSkills.size(); i++) {
                int col = i % cols;
                int row = i / cols;
                int skillX = rightColumnX + col * (SKILL_BUTTON_SIZE + 6);
                int skillY = scrollY + row * (SKILL_BUTTON_SIZE + 6) - scrollOffset;

                if (skillY + SKILL_BUTTON_SIZE < scrollY || skillY > scrollY + scrollHeight) continue;

                if (mouseX >= skillX && mouseX < skillX + SKILL_BUTTON_SIZE &&
                        mouseY >= skillY && mouseY < skillY + SKILL_BUTTON_SIZE) {

                    Identifier skillId = learnedSkills.get(i);
                    selectedSkill = skillId.equals(selectedSkill) ? null : skillId;
                    return true;
                }
            }
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
                drawSkillInSlot(context, getX() + 1, getY() + 1, boundSkill);

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
                drawSkillInSlot(context, getX() + 1, getY() + 1, previewSkill);
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
                    playClickSound(); // Add sound due to the base class are only rightclick
                }
                return true;
            } else if (button == 0) { // Left click - bind
                if (selectedSkill != null) {
                    bindSkill(slotIndex, selectedSkill);
                    playClickSound(); // only if unbind
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
                Skill skill = ModClassesSkill.getSkill(boundSkill);
                if (skill != null && client.player != null) {
                    PlayerClassComponent classComponent = ModEntityComponents.PLAYERCLASS.get(client.player);
                    int skillLevel = classComponent.getSkillLevel(boundSkill);

                    // Add skill-generated tooltip
                    List<Text> skillTooltip = skill.generateTooltip(
                            client.player,
                            skillLevel,
                            true,
                            Skill.TooltipContext.BINDING_SLOT
                    );

                    // Skip the first line (skill name) since we have slot info
                    for (int i = 1; i < skillTooltip.size(); i++) {
                        tooltip.add(skillTooltip.get(i));
                    }
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