package com.sypztep.mamy.client.screen;

import com.sypztep.mamy.client.screen.widget.ScrollableSkillTree;
import com.sypztep.mamy.client.toast.ToastRenderer;
import com.sypztep.mamy.client.util.DrawContextUtils;
import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.classes.PlayerClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class SkillLearningScreen extends Screen {
    // Clean design constants
    private static final int HEADER_HEIGHT = 25;
    private static final int CONTENT_PADDING = 20;

    // Modern colors
    private static final int BACKGROUND_COLOR = 0xF00A0A0A;
    private static final int HEADER_COLOR = 0xE0151515;
    private static final int TEXT_PRIMARY = 0xFFE6EDF3;
    private static final int ACCENT_GOLD = 0xFFE3B341;

    private final PlayerClassComponent classComponent;
    private ScrollableSkillTree skillTree;

    public SkillLearningScreen(MinecraftClient client) {
        super(Text.literal("Skill Learning"));
        this.classComponent = ModEntityComponents.PLAYERCLASS.get(client.player);
    }

    @Override
    protected void init() {
        super.init();

        this.skillTree = new ScrollableSkillTree(classComponent, client);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Background
        DrawContextUtils.fillScreen(context, BACKGROUND_COLOR);

        // Header
        renderHeader(context);

        int treeX = CONTENT_PADDING;
        int treeY = HEADER_HEIGHT + CONTENT_PADDING;
        int treeWidth = width - (CONTENT_PADDING * 2);
        int treeHeight = height - HEADER_HEIGHT - (CONTENT_PADDING * 2);

        skillTree.render(context, textRenderer, treeX, treeY, treeWidth, treeHeight, delta, mouseX, mouseY);

        // Toasts
        renderToastsOverScreen(context, delta);
    }

    private void renderHeader(DrawContext context) {
        DrawContextUtils.drawRect(context, 0, 0, width, HEADER_HEIGHT, HEADER_COLOR);
        DrawContextUtils.renderHorizontalLine(context, 0, HEADER_HEIGHT - 1, width, 1, 0, 0xFF30363D);

        PlayerClass currentClass = classComponent.getClassManager().getCurrentClass();

        // Class emblem (left)
        int emblemSize = 8;
        int emblemY = ((HEADER_HEIGHT - emblemSize) / 2) - 1;

        // Title
        Text titleText = Text.literal(currentClass.getDisplayName() + " Skill Tree")
                .formatted(Formatting.WHITE, Formatting.BOLD);
        context.drawTextWithShadow(textRenderer, titleText, CONTENT_PADDING + emblemSize, emblemY, TEXT_PRIMARY);

        // Stats (right side)
        int availablePoints = classComponent.getClassManager().getClassStatPoints();
        int learnedSkills = classComponent.getLearnedSkills(true).size();

        Text statsText = Text.literal(String.format("Points: %d | Learned: %d", availablePoints, learnedSkills))
                .formatted(availablePoints > 0 ? Formatting.GOLD : Formatting.GRAY);

        int statsX = width - textRenderer.getWidth(statsText) - CONTENT_PADDING;
        context.drawTextWithShadow(textRenderer, statsText, statsX, emblemY,
                availablePoints > 0 ? ACCENT_GOLD : 0xFF8B949E);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return skillTree.handleMouseScroll(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return skillTree.handleMouseClick(mouseX, mouseY, button) ||
                super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return skillTree.handleMouseDrag(mouseX, mouseY, button, dragX, dragY) ||
                super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        skillTree.handleMouseRelease(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // No default background
    }

    private void renderToastsOverScreen(DrawContext context, float delta) {
        ToastRenderer.renderToasts(context, this.width, delta / 20.0f);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}