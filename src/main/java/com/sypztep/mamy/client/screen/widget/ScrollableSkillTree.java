package com.sypztep.mamy.client.screen.widget;

import com.sypztep.mamy.client.util.DrawContextUtils;
import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.payload.SkillActionPayloadC2S;
import com.sypztep.mamy.common.system.skill.Skill;
import com.sypztep.mamy.common.system.skill.SkillRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.*;

public final class ScrollableSkillTree {
    // Layout constants
    private static final int SKILL_SIZE = 48;
    private static final int TIER_WIDTH = 160;
    private static final int SKILL_SPACING = 16;
    private static final int TREE_PADDING = 20;
    private static final float RENDER_SCALE = 0.85f;
    private static final int CONNECTION_THICKNESS = 2;

    // Modern colors
    private static final int BG_COLOR = 0xFF0D1117;
    private static final int TREE_BG = 0xFF161B22;
    private static final int NODE_LEARNED = 0xFF238636;
    private static final int NODE_AVAILABLE = 0xFFDA8B00;
    private static final int NODE_LOCKED = 0xFF656D76;
    private static final int NODE_ERROR = 0xFFFD7A5C;
    private static final int GLOW_LEARNED = 0xFF2EA043;
    private static final int GLOW_AVAILABLE = 0xFFF2CC60;
    private static final int CONNECTION_ACTIVE = 0xFF2EA043;
    private static final int CONNECTION_READY = 0xFFF2CC60;
    private static final int CONNECTION_INACTIVE = 0xFF484F58;

    // Components
    private final PlayerClassComponent classComponent;
    private final MinecraftClient client;
    private final ScrollBehavior scrollBehavior;

    // Tree data
    private final List<SkillTreeNode> treeNodes = new ArrayList<>();
    private final Map<Skill, SkillTreeNode> nodeMap = new HashMap<>();

    // UI state
    private int x, y, width, height;
    private int hoveredNodeIndex = -1;
    private boolean wasNodeHovered = false;
    private int contentWidth, contentHeight;

    public ScrollableSkillTree(PlayerClassComponent classComponent, MinecraftClient client) {
        this.classComponent = classComponent;
        this.client = client;
        this.scrollBehavior = new ScrollBehavior()
                .setScrollbarWidth(6)
                .setScrollbarPadding(2)
                .setMinHandleSize(30)
                .setScrollbarEnabled(true);

        buildSkillTree();
    }

    private void buildSkillTree() {
        List<Skill> skills = SkillRegistry.getSkillsForClass(classComponent.getClassManager().getCurrentClass());

        // Organize by tiers
        Map<Integer, List<Skill>> tierMap = new TreeMap<>();
        for (Skill skill : skills) {
            int tier = calculateTier(skill);
            tierMap.computeIfAbsent(tier, k -> new ArrayList<>()).add(skill);
        }

        // Sort within tiers by cost
        tierMap.values().forEach(list ->
                list.sort(Comparator.comparingInt(Skill::getBaseClassPointCost)));

        // Position nodes
        treeNodes.clear();
        nodeMap.clear();
        contentWidth = TREE_PADDING;
        contentHeight = TREE_PADDING;

        for (Map.Entry<Integer, List<Skill>> entry : tierMap.entrySet()) {
            int tier = entry.getKey();
            List<Skill> tierSkills = entry.getValue();

            int tierX = TREE_PADDING + tier * TIER_WIDTH;

            for (int i = 0; i < tierSkills.size(); i++) {
                Skill skill = tierSkills.get(i);
                int nodeY = TREE_PADDING + i * (SKILL_SIZE + SKILL_SPACING);

                SkillTreeNode node = new SkillTreeNode(skill, tier, tierX, nodeY);
                treeNodes.add(node);
                nodeMap.put(skill, node);

                contentWidth = Math.max(contentWidth, tierX + SKILL_SIZE + TREE_PADDING);
                contentHeight = Math.max(contentHeight, nodeY + SKILL_SIZE + TREE_PADDING);
            }
        }
    }

    private int calculateTier(Skill skill) {
        return calculateTier(skill, new HashSet<>());
    }

    private int calculateTier(Skill skill, Set<Identifier> visited) {
        if (visited.contains(skill.getId())) return 0;
        visited.add(skill.getId());

        if (skill.getPrerequisites().isEmpty()) return 0;

        int maxTier = 0;
        for (Skill.SkillRequirement req : skill.getPrerequisites()) {
            Skill prereq = SkillRegistry.getSkill(req.getSkillId());
            if (prereq != null) {
                maxTier = Math.max(maxTier, calculateTier(prereq, new HashSet<>(visited)));
            }
        }
        return maxTier + 1;
    }

    public void render(DrawContext context, TextRenderer textRenderer, int x, int y, int width, int height, float deltaTime, double mouseX, double mouseY) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        if (width <= 0 || height <= 0) return;

        updateHoverState(mouseX, mouseY);
        updateScrollBounds();

        // Container background
        DrawContextUtils.drawRect(context, x, y, width, height, BG_COLOR);
        context.drawBorder(x, y, width, height, 0xFF30363D);

        // Update scroll behavior
        scrollBehavior.update(context, (int) mouseX, (int) mouseY, deltaTime);

        // Enable scissor clipping
        scrollBehavior.enableScissor(context);

        // Render scaled content
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.scale(RENDER_SCALE, RENDER_SCALE, 1.0f);

        renderTreeContent(context, textRenderer);

        matrices.pop();
        scrollBehavior.disableScissor(context);

        // Render tooltips outside scissor
        if (hoveredNodeIndex >= 0 && hoveredNodeIndex < treeNodes.size()) {
            renderSkillTooltip(context, textRenderer, treeNodes.get(hoveredNodeIndex), mouseX, mouseY);
        }
    }

    private void updateScrollBounds() {
        scrollBehavior.setBounds(x, y, width, height);
        scrollBehavior.setContentHeight((int) (contentHeight * RENDER_SCALE));
    }

    private void updateHoverState(double mouseX, double mouseY) {
        hoveredNodeIndex = -1;

        if (mouseX < x || mouseX > x + width || mouseY < y || mouseY > y + height) {
            if (wasNodeHovered) {
                wasNodeHovered = false;
            }
            return;
        }

        int scrollOffset = scrollBehavior.getScrollOffset();

        for (int i = 0; i < treeNodes.size(); i++) {
            SkillTreeNode node = treeNodes.get(i);

            // Calculate scaled and scrolled position
            int scaledX = (int) ((x + node.x * RENDER_SCALE));
            int scaledY = (int) ((y + node.y * RENDER_SCALE) - scrollOffset);
            int scaledSize = (int) (SKILL_SIZE * RENDER_SCALE);

            if (mouseX >= scaledX && mouseX < scaledX + scaledSize &&
                    mouseY >= scaledY && mouseY < scaledY + scaledSize) {

                hoveredNodeIndex = i;

                // Play hover sound
                if (!wasNodeHovered) {
                    boolean prerequisitesMet = node.skill.arePrerequisitesMet(client.player);
                    if (prerequisitesMet) {
                        client.getSoundManager().play(
                                PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_HAT, 1.8F));
                    }
                    wasNodeHovered = true;
                }
                return;
            }
        }

        if (wasNodeHovered) {
            wasNodeHovered = false;
        }
    }

    private void renderTreeContent(DrawContext context, TextRenderer textRenderer) {
        int scrollOffset = (int) (scrollBehavior.getScrollOffset() / RENDER_SCALE);

        // Render connections first
        renderConnections(context, scrollOffset);

        // Render nodes
        for (int i = 0; i < treeNodes.size(); i++) {
            SkillTreeNode node = treeNodes.get(i);

            int nodeX = (int) ((x + node.x * RENDER_SCALE) / RENDER_SCALE);
            int nodeY = (int) ((y + node.y * RENDER_SCALE - scrollBehavior.getScrollOffset()) / RENDER_SCALE);

            // Skip if not visible
            if (nodeY + SKILL_SIZE < y / RENDER_SCALE || nodeY > (y + height) / RENDER_SCALE) {
                continue;
            }

            boolean isHovered = (i == hoveredNodeIndex);
            renderSkillNode(context, textRenderer, node, nodeX, nodeY, isHovered);
        }
    }

    private void renderConnections(DrawContext context, int scrollOffset) {
        for (SkillTreeNode node : treeNodes) {
            if (node.skill.getPrerequisites().isEmpty()) continue;

            for (Skill.SkillRequirement req : node.skill.getPrerequisites()) {
                SkillTreeNode prereq = nodeMap.get(SkillRegistry.getSkill(req.getSkillId()));
                if (prereq == null) continue;

                int color = getConnectionColor(node.skill, req);
                drawConnection(context, prereq, node, color, scrollOffset);
            }
        }
    }

    private void drawConnection(DrawContext context, SkillTreeNode from, SkillTreeNode to, int color, int scrollOffset) {
        int fromX = (int) ((x + from.x * RENDER_SCALE + SKILL_SIZE * RENDER_SCALE / 2) / RENDER_SCALE);
        int fromY = (int) ((y + from.y * RENDER_SCALE + SKILL_SIZE * RENDER_SCALE / 2 - scrollBehavior.getScrollOffset()) / RENDER_SCALE);
        int toX = (int) ((x + to.x * RENDER_SCALE + SKILL_SIZE * RENDER_SCALE / 2) / RENDER_SCALE);
        int toY = (int) ((y + to.y * RENDER_SCALE + SKILL_SIZE * RENDER_SCALE / 2 - scrollBehavior.getScrollOffset()) / RENDER_SCALE);

        // L-shaped connection
        int midX = fromX + (toX - fromX) * 2 / 3;

        // Horizontal line
        DrawContextUtils.renderHorizontalLine(context, fromX, fromY, midX - fromX, CONNECTION_THICKNESS, 0, color);

        // Vertical line
        if (fromY != toY) {
            DrawContextUtils.renderVerticalLine(context, midX, Math.min(fromY, toY),
                    Math.abs(toY - fromY), CONNECTION_THICKNESS, 0, color);
        }

        // Final horizontal to target
        DrawContextUtils.renderHorizontalLine(context, midX, toY, toX - midX, CONNECTION_THICKNESS, 0, color);
    }

    private void renderSkillNode(DrawContext context, TextRenderer textRenderer, SkillTreeNode node,
                                 int x, int y, boolean isHovered) {
        boolean isLearned = classComponent.hasLearnedSkill(node.skill.getId());
        int skillLevel = isLearned ? classComponent.getSkillLevel(node.skill.getId()) : 0;
        boolean prerequisitesMet = node.skill.arePrerequisitesMet(client.player);
        int availablePoints = classComponent.getClassManager().getClassStatPoints();
        boolean canLearn = !isLearned && prerequisitesMet && availablePoints >= node.skill.getBaseClassPointCost();
        boolean canUpgrade = isLearned && skillLevel < node.skill.getMaxSkillLevel() &&
                availablePoints >= node.skill.getUpgradeClassPointCost();

        // Determine visual state
        int nodeColor = getNodeColor(isLearned, prerequisitesMet, canLearn, canUpgrade);
        int borderColor = getNodeBorderColor(isLearned, prerequisitesMet, canLearn, canUpgrade, isHovered);

        // Node background
        DrawContextUtils.drawRect(context, x, y, SKILL_SIZE, SKILL_SIZE, nodeColor);

        // Glow effect for interactive nodes
        if (canLearn || canUpgrade || isLearned) {
            float pulse = (float) (Math.sin(System.currentTimeMillis() * 0.004) * 0.2 + 0.8);
            int glowAlpha = isLearned ? 40 : (int) (pulse * 60);
            int glowColor = (glowAlpha << 24) | (borderColor & 0xFFFFFF);

            for (int i = 1; i <= 3; i++) {
                context.drawBorder(x - i, y - i, SKILL_SIZE + 2 * i, SKILL_SIZE + 2 * i, glowColor);
            }
        }

        // Main border
        context.drawBorder(x, y, SKILL_SIZE, SKILL_SIZE, borderColor);

        // Skill icon
        renderSkillIcon(context, textRenderer, x, y, node.skill, isLearned, prerequisitesMet);

        // Level indicator
        if (isLearned && skillLevel > 0) {
            renderLevelBadge(context, textRenderer, x, y, skillLevel);
        }

        // Action indicator
        if (canLearn || canUpgrade) {
            renderActionPulse(context, textRenderer, x, y);
        }

        // Skill name below node
        renderSkillName(context, textRenderer, x, y, node.skill, isLearned, prerequisitesMet);
    }

    private void renderSkillIcon(DrawContext context, TextRenderer textRenderer, int x, int y,
                                 Skill skill, boolean isLearned, boolean prerequisitesMet) {
        int iconX = x + 6;
        int iconY = y + 6;
        int iconSize = SKILL_SIZE - 12;

        if (skill.getIcon() != null) {
            context.drawGuiTexture(skill.getIcon(), iconX, iconY, iconSize, iconSize);
        } else {
            // Text fallback
            String abbrev = skill.getName().length() >= 2
                    ? skill.getName().substring(0, 2).toUpperCase()
                    : skill.getName().substring(0, 1).toUpperCase();

            int textColor = isLearned ? 0xFFFFFFFF : prerequisitesMet ? 0xFFCCCCCC : 0xFF666666;
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(abbrev).formatted(Formatting.BOLD),
                    x + SKILL_SIZE / 2, y + SKILL_SIZE / 2 - 4, textColor);
        }

        // Lock overlay
        if (!prerequisitesMet && !isLearned) {
            DrawContextUtils.drawRect(context, iconX, iconY, iconSize, iconSize, 0x88000000);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("X"),
                    x + SKILL_SIZE / 2, y + SKILL_SIZE / 2 - 4, NODE_ERROR);
        } else if (!isLearned) {
            DrawContextUtils.drawRect(context, iconX, iconY, iconSize, iconSize, 0x66000000);
        }
    }

    private void renderLevelBadge(DrawContext context, TextRenderer textRenderer, int x, int y, int level) {
        int badgeSize = 14;
        int badgeX = x + SKILL_SIZE - badgeSize;
        int badgeY = y - 2;

        DrawContextUtils.drawRect(context, badgeX, badgeY, badgeSize, badgeSize, NODE_LEARNED);
        context.drawBorder(badgeX, badgeY, badgeSize, badgeSize, GLOW_LEARNED);

        String levelText = String.valueOf(level);
        int textX = badgeX + (badgeSize - textRenderer.getWidth(levelText)) / 2;
        context.drawTextWithShadow(textRenderer, Text.literal(levelText), textX, badgeY + 2, 0xFFFFFFFF);
    }

    private void renderActionPulse(DrawContext context, TextRenderer textRenderer, int x, int y) {
        float pulse = (float) (Math.sin(System.currentTimeMillis() * 0.008) * 0.3 + 0.7);
        int pulseX = x + SKILL_SIZE + 8;
        int pulseY = y + SKILL_SIZE / 2;

        context.getMatrices().push();
        context.getMatrices().translate(pulseX, pulseY, 0);
        context.getMatrices().scale(pulse * 1.2f, pulse * 1.2f, 1f);

        int alpha = (int) (pulse * 255);
        int pulseColor = (alpha << 24) | (GLOW_AVAILABLE & 0xFFFFFF);
        context.drawText(textRenderer, Text.literal("+").formatted(Formatting.BOLD),
                -textRenderer.getWidth("+") / 2, -textRenderer.fontHeight / 2, pulseColor, true);
        context.getMatrices().pop();
    }

    private void renderSkillName(DrawContext context, TextRenderer textRenderer, int x, int y,
                                 Skill skill, boolean isLearned, boolean prerequisitesMet) {
        Formatting color = isLearned ? Formatting.GREEN :
                prerequisitesMet ? Formatting.WHITE : Formatting.DARK_GRAY;

        Text nameText = Text.literal(skill.getName()).formatted(color);
        int nameX = x + (SKILL_SIZE - textRenderer.getWidth(nameText)) / 2;
        int nameY = y + SKILL_SIZE + 4;

        context.drawTextWithShadow(textRenderer, nameText, nameX, nameY,
                color.getColorValue() != null ? color.getColorValue() : 0xFFFFFFFF);
    }

    private void renderSkillTooltip(DrawContext context, TextRenderer textRenderer, SkillTreeNode node,
                                    double mouseX, double mouseY) {
        boolean isLearned = classComponent.hasLearnedSkill(node.skill.getId());
        int skillLevel = isLearned ? classComponent.getSkillLevel(node.skill.getId()) : 1;

        List<Text> tooltip = new ArrayList<>();

        // Header
        tooltip.add(Text.literal(node.skill.getName()).formatted(Formatting.AQUA, Formatting.BOLD));
        tooltip.add(Text.literal("Tier " + (node.tier + 1)).formatted(Formatting.DARK_GRAY));
        tooltip.add(Text.literal(""));

        // Description
        tooltip.add(Text.literal(node.skill.getDescription()).formatted(Formatting.WHITE));

        // Prerequisites
        if (!node.skill.getPrerequisites().isEmpty()) {
            tooltip.add(Text.literal(""));
            tooltip.add(Text.literal("Requirements:").formatted(Formatting.YELLOW));

            for (Skill.SkillRequirement req : node.skill.getPrerequisites()) {
                Skill prereq = SkillRegistry.getSkill(req.getSkillId());
                if (prereq != null) {
                    boolean learned = classComponent.hasLearnedSkill(req.getSkillId());
                    int level = learned ? classComponent.getSkillLevel(req.getSkillId()) : 0;
                    boolean met = learned && level >= req.getMinLevel();

                    String symbol = met ? "✓" : "✗";
                    Formatting reqColor = met ? Formatting.GREEN : Formatting.RED;

                    tooltip.add(Text.literal(String.format("%s %s Lv.%d", symbol, prereq.getName(), req.getMinLevel()))
                            .formatted(reqColor));
                }
            }
        }

        // Status and cost
        tooltip.add(Text.literal(""));
        if (!isLearned) {
            boolean prerequisitesMet = node.skill.arePrerequisitesMet(client.player);
            int cost = node.skill.getBaseClassPointCost();
            int points = classComponent.getClassManager().getClassStatPoints();

            if (prerequisitesMet && points >= cost) {
                tooltip.add(Text.literal("Click to learn (" + cost + " points)").formatted(Formatting.GREEN));
            } else if (!prerequisitesMet) {
                tooltip.add(Text.literal("Requirements not met").formatted(Formatting.RED));
            } else {
                tooltip.add(Text.literal("Insufficient points (" + points + "/" + cost + ")")
                        .formatted(Formatting.RED));
            }
        } else {
            tooltip.add(Text.literal("Learned (Level " + skillLevel + ")").formatted(Formatting.GREEN));
            if (skillLevel < node.skill.getMaxSkillLevel()) {
                int upgradeCost = node.skill.getUpgradeClassPointCost();
                int points = classComponent.getClassManager().getClassStatPoints();
                if (points >= upgradeCost) {
                    tooltip.add(Text.literal("Click to upgrade (" + upgradeCost + " points)")
                            .formatted(Formatting.GOLD));
                }
            }
            tooltip.add(Text.literal("Right-click to unlearn").formatted(Formatting.RED));
        }

        context.drawTooltip(textRenderer, tooltip, (int) mouseX, (int) mouseY);
    }

    private int getNodeColor(boolean isLearned, boolean prerequisitesMet, boolean canLearn, boolean canUpgrade) {
        if (isLearned) return 0xFF0D2818;
        if (canLearn || canUpgrade) return 0xFF2A1F0A;
        if (!prerequisitesMet) return 0xFF2A1010;
        return TREE_BG;
    }

    private int getNodeBorderColor(boolean isLearned, boolean prerequisitesMet, boolean canLearn,
                                   boolean canUpgrade, boolean isHovered) {
        if (isLearned) return isHovered ? GLOW_LEARNED : NODE_LEARNED;
        if (canLearn || canUpgrade) return isHovered ? GLOW_AVAILABLE : NODE_AVAILABLE;
        if (!prerequisitesMet) return NODE_ERROR;
        return NODE_LOCKED;
    }

    private int getConnectionColor(Skill skill, Skill.SkillRequirement req) {
        boolean skillLearned = classComponent.hasLearnedSkill(skill.getId());
        boolean prereqLearned = classComponent.hasLearnedSkill(req.getSkillId());
        int prereqLevel = prereqLearned ? classComponent.getSkillLevel(req.getSkillId()) : 0;
        boolean prereqMet = prereqLearned && prereqLevel >= req.getMinLevel();

        if (skillLearned && prereqMet) return CONNECTION_ACTIVE;
        if (prereqMet) return CONNECTION_READY;
        return CONNECTION_INACTIVE;
    }

    // Mouse interaction methods
    public boolean handleMouseClick(double mouseX, double mouseY, int button) {
        if (hoveredNodeIndex >= 0) {
            SkillTreeNode node = treeNodes.get(hoveredNodeIndex);
            return handleSkillClick(node, button);
        }
        return scrollBehavior.handleMouseClick(mouseX, mouseY, button);
    }

    private boolean handleSkillClick(SkillTreeNode node, int button) {
        boolean isLearned = classComponent.hasLearnedSkill(node.skill.getId());
        int skillLevel = isLearned ? classComponent.getSkillLevel(node.skill.getId()) : 0;
        boolean prerequisitesMet = node.skill.arePrerequisitesMet(client.player);
        int availablePoints = classComponent.getClassManager().getClassStatPoints();
        boolean canLearn = !isLearned && prerequisitesMet && availablePoints >= node.skill.getBaseClassPointCost();
        boolean canUpgrade = isLearned && skillLevel < node.skill.getMaxSkillLevel() &&
                availablePoints >= node.skill.getUpgradeClassPointCost();

        if (button == 0) { // Left click
            if (canLearn) {
                SkillActionPayloadC2S.sendLearn(node.skill.getId());
                client.getSoundManager().play(PositionedSoundInstance.master(
                        SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.2F));
                return true;
            } else if (canUpgrade) {
                SkillActionPayloadC2S.sendUpgrade(node.skill.getId());
                client.getSoundManager().play(PositionedSoundInstance.master(
                        SoundEvents.ENTITY_PLAYER_LEVELUP, 1.0F));
                return true;
            } else {
                client.getSoundManager().play(PositionedSoundInstance.master(
                        SoundEvents.BLOCK_NOTE_BLOCK_BASS, 0.8F));
                return true;
            }
        } else if (button == 1 && isLearned) { // Right click unlearn
            SkillActionPayloadC2S.sendUnlearn(node.skill.getId());
            client.getSoundManager().play(PositionedSoundInstance.master(
                    SoundEvents.UI_TOAST_OUT, 1.0F));
            return true;
        }

        return false;
    }

    public boolean handleMouseDrag(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return scrollBehavior.handleMouseDrag(mouseX, mouseY, button, dragX, dragY);
    }

    public void handleMouseRelease(double mouseX, double mouseY, int button) {
        scrollBehavior.handleMouseRelease(mouseX, mouseY, button);
    }

    public boolean handleMouseScroll(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return scrollBehavior.handleScroll(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    // Helper classes
        private record SkillTreeNode(Skill skill, int tier, int x, int y) {
    }
}