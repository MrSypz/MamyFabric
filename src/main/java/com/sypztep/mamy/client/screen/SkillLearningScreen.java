package com.sypztep.mamy.client.screen;

import com.sypztep.mamy.client.screen.widget.ActionWidgetButton;
import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.payload.SkillActionPayloadC2S;
import com.sypztep.mamy.common.system.skill.Skill;
import com.sypztep.mamy.common.system.skill.SkillRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import sypztep.tyrannus.client.util.DrawContextUtils;

import java.util.ArrayList;
import java.util.List;

public final class SkillLearningScreen extends Screen {
    // UI constants
    private static final int CONTENT_PADDING = 50;
    private static final int SUMMARY_HEIGHT = 35;
    private static final int SKILL_ICON_SIZE = 32;
    private static final int SKILL_SPACING = 20;  // 20px padding between skills
    private static final int DOT_SIZE = 4;
    private static final int DOT_SPACING = 3;
    private static final int DOTS_OFFSET_Y = 38;  // Dots under icon

    // Colors
    private static final int BACKGROUND_COLOR = 0xF0121212;
    private static final int PANEL_COLOR = 0xFF1E1E1E;
    private static final int LEARNED_COLOR = 0xFF4CAF50;
    private static final int CAN_LEARN_COLOR = 0xFFFFD700;  // Gold for learnable
    private static final int DOT_LEARNED_COLOR = 0xFF4CAF50;
    private static final int DOT_UNLEARNED_COLOR = 0xFF666666;

    // Component data
    private final LivingLevelComponent playerStats;
    private final PlayerClassComponent classComponent;
    private List<Skill> availableSkills;
    private final List<SkillActionButton> skillButtons = new ArrayList<>();

    public SkillLearningScreen(MinecraftClient client) {
        super(Text.literal("Skill Learning"));
        this.playerStats = ModEntityComponents.LIVINGLEVEL.get(client.player);
        this.classComponent = ModEntityComponents.PLAYERCLASS.get(client.player);
        loadAvailableSkills();
    }

    private void loadAvailableSkills() {
        this.availableSkills = SkillRegistry.getSkillsForClass(classComponent.getClassManager().getCurrentClass());
    }

    @Override
    protected void init() {
        super.init();

        skillButtons.clear();

        int gridStartY = CONTENT_PADDING + SUMMARY_HEIGHT + 30;
        int gridWidth = width - (CONTENT_PADDING * 2);
        int skillsPerRow = gridWidth / (SKILL_ICON_SIZE + SKILL_SPACING);

        for (int i = 0; i < availableSkills.size(); i++) {
            Skill skill = availableSkills.get(i);

            int col = i % skillsPerRow;
            int row = i / skillsPerRow;
            int skillX = CONTENT_PADDING + col * (SKILL_ICON_SIZE + SKILL_SPACING);
            int skillY = gridStartY + row * (SKILL_ICON_SIZE + DOTS_OFFSET_Y);

            SkillActionButton button = new SkillActionButton(skillX, skillY, skill, playerStats, client);
            skillButtons.add(button);
            addDrawableChild(button);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        DrawContextUtils.fillScreen(context, BACKGROUND_COLOR);
        renderTitle(context);
        renderClassPointsSummary(context);

        super.render(context, mouseX, mouseY, delta); // This renders the buttons
    }

    private void renderTitle(DrawContext context) {
        Text titleText = Text.literal("Skill Learning & Upgrade").formatted(Formatting.GOLD, Formatting.BOLD);
        int titleY = CONTENT_PADDING - 25;

        context.drawCenteredTextWithShadow(textRenderer, titleText, width / 2, titleY, 0xFFD700);

        // Decorative line
        int lineY = titleY + textRenderer.fontHeight + 3;
        DrawContextUtils.renderHorizontalLineWithCenterGradient(context, CONTENT_PADDING, lineY,
                width - (CONTENT_PADDING * 2), 1, 1, 0xFFFFFFFF, 0x00FFFFFF);
    }

    private void renderClassPointsSummary(DrawContext context) {
        int availablePoints = classComponent.getClassManager().getClassStatPoints();
        var className = classComponent.getClassManager().getCurrentClass();

        int summaryX = CONTENT_PADDING;
        int summaryY = CONTENT_PADDING;
        int summaryWidth = width - (CONTENT_PADDING * 2);

        // Background
        context.fill(summaryX, summaryY, summaryX + summaryWidth, summaryY + SUMMARY_HEIGHT, PANEL_COLOR);
        context.fill(summaryX, summaryY, summaryX + summaryWidth, summaryY + 1, LEARNED_COLOR);
        context.fill(summaryX, summaryY + SUMMARY_HEIGHT - 1, summaryX + summaryWidth, summaryY + SUMMARY_HEIGHT, LEARNED_COLOR);

        context.drawTextWithShadow(textRenderer,
                Text.literal("Class: ").formatted(Formatting.WHITE)
                        .append(Text.literal(className.getDisplayName()).formatted(className.getColor())),
                summaryX + 10, summaryY + 8, 0xFFFFFF);

        // Available points
        String pointsText = String.format("Available Points: %d", availablePoints);
        Formatting pointsColor = availablePoints > 0 ? Formatting.GOLD : Formatting.GRAY;
        context.drawTextWithShadow(textRenderer, Text.literal(pointsText).formatted(pointsColor),
                summaryX + 10, summaryY + 20, pointsColor.getColorValue() != null ? pointsColor.getColorValue() : 0xFFFFFF);

        // Learned skills count
        int learnedCount = classComponent.getLearnedSkills(true).size();
        int totalCount = availableSkills.size();
        String skillsText = String.format("Skills: %d/%d", learnedCount, totalCount);
        int skillsTextWidth = textRenderer.getWidth(skillsText);
        context.drawTextWithShadow(textRenderer, Text.literal(skillsText).formatted(Formatting.AQUA),
                summaryX + summaryWidth - skillsTextWidth - 10, summaryY + 14, 0xFF00FFFF);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    // ============================================================================
    // SKILL BUTTON CLASS
    // ============================================================================

    private class SkillActionButton extends ActionWidgetButton {
        private final Skill skill;

        public SkillActionButton(int x, int y, Skill skill, LivingLevelComponent stats, MinecraftClient client) {
            super(x, y, SKILL_ICON_SIZE, SKILL_ICON_SIZE, Text.empty(), stats, client);
            this.skill = skill;
        }

        @Override
        protected void renderAdditionalOverlays(DrawContext context, int mouseX, int mouseY, float delta,
                                                boolean isHovered, boolean isPressed) {
            boolean isLearned = classComponent.hasLearnedSkill(skill.getId());
            int skillLevel = isLearned ? classComponent.getSkillLevel(skill.getId()) : 0;

            int availablePoints = classComponent.getClassManager().getClassStatPoints();
            boolean canLearn = !isLearned && availablePoints >= skill.getBaseClassPointCost();
            boolean canUpgrade = isLearned && skillLevel < skill.getMaxSkillLevel() &&
                    availablePoints >= skill.getUpgradeClassPointCost();

            int x = getX();
            int y = getY();

            // Calculate pulse once and reuse for all effects
            float pulse = (float) (Math.sin(System.currentTimeMillis() * 0.006) * 0.5 + 0.5);
            int pulseAlpha = (int) (pulse * 200) + 55; // avoid fully invisible range
            int pulseColor = (pulseAlpha << 24) | (CAN_LEARN_COLOR & 0xFFFFFF);

            if (canLearn || canUpgrade) {
                int border = 2;
                // Top
                context.fill(x - border, y - border, x + SKILL_ICON_SIZE + border - 1, y - border + 1, pulseColor);
                // Bottom
                context.fill(x - border, y + SKILL_ICON_SIZE , x + SKILL_ICON_SIZE + border - 1, y + SKILL_ICON_SIZE + border - 1, pulseColor);
                // Left
                context.fill(x - border, y - border + 1, x - border + 1, y + SKILL_ICON_SIZE, pulseColor);
                // Right
                context.fill(x + SKILL_ICON_SIZE , y - border + 1, x + SKILL_ICON_SIZE + border - 1, y + SKILL_ICON_SIZE, pulseColor);
            }


            // Skill icon or abbreviation
            if (skill.getIcon() != null) {
                context.drawGuiTexture(skill.getIcon(), x + 2, y + 2, SKILL_ICON_SIZE - 4, SKILL_ICON_SIZE - 4);
            } else {
                String abbreviation = skill.getName().length() >= 2
                        ? skill.getName().substring(0, 2).toUpperCase()
                        : skill.getName().toUpperCase();
                int textWidth = textRenderer.getWidth(abbreviation);
                int textX = x + (SKILL_ICON_SIZE - textWidth) / 2;
                int textY = y + (SKILL_ICON_SIZE - textRenderer.fontHeight) / 2;
                context.drawText(textRenderer, Text.literal(abbreviation), textX, textY, 0xFFFFFFFF, false);
            }

            // Dim overlay for unlearned or upgradeable-but-not-maxed
            if (!isLearned || skillLevel < skill.getMaxSkillLevel()) {
                context.fill(x + 1, y + 1, x + SKILL_ICON_SIZE - 2, y + SKILL_ICON_SIZE - 2, 0xBB000000);
            }

            // Draw pulsing "+" icon for available actions
            if (canLearn || canUpgrade) {
                String plus = "+";
                int textWidth = textRenderer.getWidth(plus);
                int textX = x + (SKILL_ICON_SIZE - textWidth) / 2;
                int textY = y + (SKILL_ICON_SIZE - textRenderer.fontHeight) / 2;

                context.getMatrices().push();
                context.getMatrices().translate(textX + textWidth / 2f, textY + textRenderer.fontHeight / 2f, 0);
                float scale = 1.5f + pulse * 0.5f; // subtle pulsing scale
                context.getMatrices().scale(scale, scale, 1f);
                context.getMatrices().translate(-textWidth / 2f, -textRenderer.fontHeight / 2f, 0);
                context.drawText(textRenderer, Text.literal(plus), 0, 0, pulseColor, true);
                context.getMatrices().pop();
            }

            // Level dots
            renderSkillLevelDots(context, x, y + DOTS_OFFSET_Y, skillLevel);

            // Tooltip
            if (isHovered) {
                renderTooltip(context, mouseX, mouseY);
            }
        }


        private void renderSkillLevelDots(DrawContext context, int x, int y, int currentLevel) {
            int maxLevel = skill.getMaxSkillLevel();

            int dotsPerRow = 5; // max dots before wrapping
            for (int i = 0; i < maxLevel; i++) {
                int row = i / dotsPerRow;
                int col = i % dotsPerRow;

                int dotX = x + col * (DOT_SIZE + DOT_SPACING);
                int dotY = y + row * (DOT_SIZE + DOT_SPACING);

                int dotColor = i < currentLevel ? DOT_LEARNED_COLOR : DOT_UNLEARNED_COLOR;

                // Draw dot
                context.fill(dotX, dotY, dotX + DOT_SIZE, dotY + DOT_SIZE, dotColor);

                // Draw dot border
                if (i < currentLevel) {
                    context.drawBorder(dotX, dotY, DOT_SIZE, DOT_SIZE, 0xFF2E7D32);
                } else {
                    context.drawBorder(dotX, dotY, DOT_SIZE, DOT_SIZE, 0xFF424242);
                }
            }
        }


        public void renderTooltip(DrawContext context, int mouseX, int mouseY) {
            boolean isLearned = classComponent.hasLearnedSkill(skill.getId());
            int skillLevel = isLearned ? classComponent.getSkillLevel(skill.getId()) : 1;

            List<Text> tooltip = skill.generateTooltip(
                    client.player,
                    skillLevel,
                    isLearned,
                    Skill.TooltipContext.LEARNING_SCREEN
            );

            context.drawTooltip(client.textRenderer, tooltip, mouseX, mouseY);
        }

        @Override
        protected int getBackgroundColor() {
            boolean isLearned = classComponent.hasLearnedSkill(skill.getId());
            if (isLearned) return 0;
            return PANEL_COLOR;
        }

        @Override
        protected int getHoverBackgroundColor() {
            boolean isLearned = classComponent.hasLearnedSkill(skill.getId());
            if (isLearned) return 0; // Brighter green
            return 0xFF3A3A3A; // Lighter gray
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!isMouseOver(mouseX, mouseY)) return false;

            boolean isLearned = classComponent.hasLearnedSkill(skill.getId());
            int skillLevel = isLearned ? classComponent.getSkillLevel(skill.getId()) : 0;

            int availablePoints = classComponent.getClassManager().getClassStatPoints();
            boolean canLearn = !isLearned && availablePoints >= skill.getBaseClassPointCost();
            boolean canUpgrade = isLearned && skillLevel < skill.getMaxSkillLevel() &&
                    availablePoints >= skill.getUpgradeClassPointCost();

            if (button == 0) { // Left click - Learn/Upgrade
                if (canLearn) {
                    SkillActionPayloadC2S.sendLearn(skill.getId());
                } else if (canUpgrade) {
                    SkillActionPayloadC2S.sendUpgrade(skill.getId());
                }
                return true;
            } else if (button == 1) { // Right click - Unlearn
                if (isLearned) {
                    SkillActionPayloadC2S.sendUnlearn(skill.getId());
                }
                return true;
            }

            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}