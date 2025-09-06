package com.sypztep.mamy.client.screen.widget;

import com.sypztep.mamy.ModConfig;
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
    private static final int SKILL_SIZE = 32;
    private static final int TIER_WIDTH = 160;
    private static final int SKILL_SPACING = 16;
    private static final int TREE_PADDING = 20;
    private static final float RENDER_SCALE = 0.85f;
    private static final int CONNECTION_THICKNESS = 2;

    private static final int DOT_SIZE = 3;
    private static final int DOT_SPACING = 2;
    private static final int DOTS_OFFSET_Y = 4;
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

    // Dot colors
    private static final int DOT_LEARNED = 0xFFFFC107;
    private static final int DOT_UNLEARNED = 0xFF404040;
    private static final int DOT_BORDER_LEARNED = 0xFFFFD54F;
    private static final int DOT_BORDER_UNLEARNED = 0xFF2A2A2A;

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
        this.scrollBehavior = new ScrollBehavior().setScrollbarWidth(6).setScrollbarPadding(2).setMinHandleSize(30).setScrollbarEnabled(true);

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
        tierMap.values().forEach(list -> list.sort(Comparator.comparingInt(Skill::getBaseClassPointCost)));

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
            Skill prereq = SkillRegistry.getSkill(req.skillId());
            if (prereq != null) {
                maxTier = Math.max(maxTier, calculateTier(prereq, new HashSet<>(visited)));
            }
        }
        return maxTier + 1;
    }

    /**
     * NEW METHOD: Check if a skill can be unlearned (client-side validation)
     * This mirrors the server-side logic in ClassSkillManager
     */
    private boolean canUnlearnSkill(Skill skill) {
        if (!ModConfig.unlearnskill) return false;

        // Can't unlearn basic skill
        if (skill.getId().equals(SkillRegistry.BASICSKILL)) return false;

        int currentLevel = classComponent.getSkillLevel(skill.getId());

        // Can't unlearn default skills completely
        if (skill.isDefaultSkill() && currentLevel <= 1) return false;

        // Check for dependent skills
        List<Identifier> dependentSkills = findDependentSkills(skill.getId(), currentLevel);
        return dependentSkills.isEmpty();
    }

    /**
     * NEW METHOD: Find skills that depend on the given skill (client-side version)
     */
    private List<Identifier> findDependentSkills(Identifier skillId, int currentLevel) {
        List<Identifier> dependentSkills = new ArrayList<>();

        List<Skill> allSkills = SkillRegistry.getSkillsForClass(classComponent.getClassManager().getCurrentClass());

        for (Skill skill : allSkills) {
            // Skip if this skill is not learned
            if (!classComponent.hasLearnedSkill(skill.getId())) continue;

            // Check if this skill has the target skill as a prerequisite
            for (Skill.SkillRequirement req : skill.getPrerequisites()) {
                if (req.skillId().equals(skillId)) {
                    // Check if unlearning/downgrading would break the requirement
                    int newLevel = currentLevel <= 1 ? 0 : currentLevel - 1;
                    if (newLevel < req.minLevel()) {
                        dependentSkills.add(skill.getId());
                    }
                    break;
                }
            }
        }

        return dependentSkills;
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

            if (mouseX >= scaledX && mouseX < scaledX + scaledSize && mouseY >= scaledY && mouseY < scaledY + scaledSize) {

                hoveredNodeIndex = i;

                // Play hover sound
                if (!wasNodeHovered) {
                    boolean prerequisitesMet = node.skill.arePrerequisitesMet(client.player);
                    if (prerequisitesMet) {
                        client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_HAT, 1.8F));
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
        List<Connection> connections = new ArrayList<>();
        for (SkillTreeNode node : treeNodes) {
            if (node.skill.getPrerequisites().isEmpty()) continue;

            for (Skill.SkillRequirement req : node.skill.getPrerequisites()) {
                SkillTreeNode prereq = nodeMap.get(SkillRegistry.getSkill(req.skillId()));
                if (prereq == null) continue;

                int color = getConnectionColor(node.skill, req);
                connections.add(new Connection(prereq, node, color));
            }
        }

        // Sort connections by priority (inactive first, then ready, then active last to draw on top)
        connections.sort(Comparator.comparingInt(connection -> getConnectionPriority(connection.color)));

        for (Connection connection : connections) {
            drawConnection(context, connection.from, connection.to, connection.color, scrollOffset);
        }
    }

    private int getConnectionPriority(int color) {
        if (color == CONNECTION_INACTIVE) return 0;
        if (color == CONNECTION_READY) return 1;
        if (color == CONNECTION_ACTIVE) return 2;
        return 0;
    }

    private void drawConnection(DrawContext context, SkillTreeNode from, SkillTreeNode to, int color, int scrollOffset) {
        int fromX = (int) ((x + from.x * RENDER_SCALE + SKILL_SIZE * RENDER_SCALE) / RENDER_SCALE);
        int fromY = (int) ((y + from.y * RENDER_SCALE + SKILL_SIZE * RENDER_SCALE / 2 - scrollBehavior.getScrollOffset()) / RENDER_SCALE);

        // End at left edge of to node, center Y
        int toX = (int) ((x + to.x * RENDER_SCALE) / RENDER_SCALE);
        int toY = (int) ((y + to.y * RENDER_SCALE + SKILL_SIZE * RENDER_SCALE / 2 - scrollBehavior.getScrollOffset()) / RENDER_SCALE);

        // Midpoint for L-shape, adjusted to be more centered in the space between tiers
        int midX = fromX + (toX - fromX) / 2;

        // Horizontal line from from to mid
        DrawContextUtils.renderHorizontalLine(context, fromX, fromY, midX - fromX, CONNECTION_THICKNESS, 0, color);

        // Vertical line if needed
        if (fromY != toY) {
            DrawContextUtils.renderVerticalLine(context, midX, Math.min(fromY, toY), Math.abs(toY - fromY), CONNECTION_THICKNESS, 0, color);
        }

        // Final horizontal to target
        DrawContextUtils.renderHorizontalLine(context, midX, toY, toX - midX, CONNECTION_THICKNESS, 0, color);
    }

    private void renderSkillNode(DrawContext context, TextRenderer textRenderer, SkillTreeNode node, int x, int y, boolean isHovered) {
        boolean isLearned = classComponent.hasLearnedSkill(node.skill.getId());
        int skillLevel = isLearned ? classComponent.getSkillLevel(node.skill.getId()) : 0;
        boolean prerequisitesMet = node.skill.arePrerequisitesMet(client.player);
        int availablePoints = classComponent.getClassManager().getClassStatPoints();
        boolean canLearn = !isLearned && prerequisitesMet && availablePoints >= node.skill.getBaseClassPointCost();
        boolean canUpgrade = isLearned && skillLevel < node.skill.getMaxSkillLevel() && availablePoints >= node.skill.getUpgradeClassPointCost();

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

        // Level dots indicator (replaces the old badge)
        if (node.skill.getMaxSkillLevel() > 0) {
            renderLevelDots(context, x, y, skillLevel, node.skill.getMaxSkillLevel());
        }

        // Action indicator
        if (canLearn || canUpgrade) {
            renderActionPulse(context, textRenderer, x, y);
        }

        renderSkillName(context, textRenderer, x, y, node.skill, isLearned, prerequisitesMet);
    }

    private void renderLevelDots(DrawContext context, int x, int y, int currentLevel, int maxLevel) {
        if (maxLevel <= 0) return;

        int totalDotsWidth = (maxLevel * DOT_SIZE) + ((maxLevel - 1) * DOT_SPACING);

        int startX = x + (SKILL_SIZE - totalDotsWidth) / 2;
        int dotsY = y + SKILL_SIZE + DOTS_OFFSET_Y;

        for (int i = 0; i < maxLevel; i++) {
            int dotX = startX + i * (DOT_SIZE + DOT_SPACING);
            boolean isLearned = i < currentLevel;

            int dotColor = isLearned ? DOT_LEARNED : DOT_UNLEARNED;
            int borderColor = isLearned ? DOT_BORDER_LEARNED : DOT_BORDER_UNLEARNED;

            DrawContextUtils.drawRect(context, dotX, dotsY, DOT_SIZE, DOT_SIZE, dotColor);
            context.drawBorder(dotX, dotsY, DOT_SIZE, DOT_SIZE, borderColor);
        }
    }

    private void renderSkillIcon(DrawContext context, TextRenderer textRenderer, int x, int y, Skill skill, boolean isLearned, boolean prerequisitesMet) {
        int iconX = x + 1;
        int iconY = y + 1;
        int iconSize = SKILL_SIZE - 2;

        if (skill.getIcon() != null) {
            context.drawGuiTexture(skill.getIcon(), iconX, iconY, iconSize, iconSize);
        } else {
            String abbrev = skill.getName().length() >= 2 ? skill.getName().substring(0, 2).toUpperCase() : skill.getName().substring(0, 1).toUpperCase();

            int textColor = isLearned ? 0xFFFFFFFF : prerequisitesMet ? 0xFFCCCCCC : 0xFF666666;
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(abbrev).formatted(Formatting.BOLD), x + SKILL_SIZE / 2, y + SKILL_SIZE / 2 - 4, textColor);
        }

        if (!prerequisitesMet && !isLearned) {
            DrawContextUtils.drawRect(context, iconX, iconY, iconSize, iconSize, 0x88000000);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("X"), x + SKILL_SIZE / 2, y + SKILL_SIZE / 2 - 4, NODE_ERROR);
        } else if (!isLearned) {
            DrawContextUtils.drawRect(context, iconX, iconY, iconSize, iconSize, 0x66000000);
        }
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
        context.drawText(textRenderer, Text.literal("+").formatted(Formatting.BOLD), -textRenderer.getWidth("+") / 2, -textRenderer.fontHeight / 2, pulseColor, true);
        context.getMatrices().pop();
    }

    private void renderSkillName(DrawContext context, TextRenderer textRenderer, int x, int y,
                                 Skill skill, boolean isLearned, boolean prerequisitesMet) {
        Formatting color = isLearned ? Formatting.GREEN :
                prerequisitesMet ? Formatting.WHITE : Formatting.DARK_GRAY;

        Text nameText = Text.literal(skill.getName()).formatted(color);

        float scale = 0.6f;
        int rawWidth = textRenderer.getWidth(nameText);
        int scaledWidth = (int)(rawWidth * scale);

        int nameX = x + (SKILL_SIZE - scaledWidth) / 2;
        int nameY = y - 6;

        context.getMatrices().push();
        context.getMatrices().translate(nameX, nameY, 0);
        context.getMatrices().scale(scale, scale, 1f);

        context.drawTextWithShadow(textRenderer, nameText, 0, 0,
                color.getColorValue() != null ? color.getColorValue() : 0xFFFFFFFF);
        context.getMatrices().pop();
    }

    private void renderSkillTooltip(DrawContext context, TextRenderer textRenderer, SkillTreeNode node, double mouseX, double mouseY) {
        boolean isLearned = classComponent.hasLearnedSkill(node.skill.getId());
        int skillLevel = isLearned ? classComponent.getSkillLevel(node.skill.getId()) : 1;

        // Use the skill's own tooltip generation method
        List<Text> tooltip = node.skill.generateTooltip(client.player, skillLevel, isLearned, Skill.TooltipContext.LEARNING_SCREEN);
        tooltip.add(Text.literal("")); // Empty line
        // Prerequisites check (only if not learned and has prerequisites)
        if (!node.skill.getPrerequisites().isEmpty() && !isLearned) {
            tooltip.add(Text.literal(""));
            tooltip.add(Text.literal("  Requirements").formatted(Formatting.YELLOW));

            for (Skill.SkillRequirement req : node.skill.getPrerequisites()) {
                Skill prereq = SkillRegistry.getSkill(req.skillId());
                if (prereq != null) {
                    boolean learned = classComponent.hasLearnedSkill(req.skillId());
                    int level = learned ? classComponent.getSkillLevel(req.skillId()) : 0;
                    boolean met = learned && level >= req.minLevel();

                    String symbol = met ? "✓" : "✗";
                    Formatting reqColor = met ? Formatting.GREEN : Formatting.RED;

                    tooltip.add(Text.literal(String.format("%s %s Lv.%d", symbol, prereq.getName(), req.minLevel())).formatted(reqColor));
                }
            }
        }

        // Learning/upgrade status and actions
        tooltip.add(Text.literal("")); // Empty line
        if (!isLearned) {
            boolean prerequisitesMet = node.skill.arePrerequisitesMet(client.player);
            int cost = node.skill.getBaseClassPointCost();
            int points = classComponent.getClassManager().getClassStatPoints();

            if (prerequisitesMet && points >= cost) {
                tooltip.add(Text.literal("Click to learn (" + cost + " points)").formatted(Formatting.GREEN));
            } else if (!prerequisitesMet) {
                tooltip.add(Text.literal("Requirements not met").formatted(Formatting.RED));
            } else {
                tooltip.add(Text.literal("Insufficient points (" + points + "/" + cost + ")").formatted(Formatting.RED));
            }
        } else {
            tooltip.add(Text.literal("Learned (Level " + skillLevel + ")").formatted(Formatting.GREEN));

            if (skillLevel < node.skill.getMaxSkillLevel()) {
                int upgradeCost = node.skill.getUpgradeClassPointCost();
                int points = classComponent.getClassManager().getClassStatPoints();
                if (points >= upgradeCost) {
                    tooltip.add(Text.literal("Click to upgrade (" + upgradeCost + " points)").formatted(Formatting.GOLD));
                } else {
                    tooltip.add(Text.literal("Insufficient points for upgrade (" + points + "/" + upgradeCost + ")").formatted(Formatting.RED));
                }
            } else {
                tooltip.add(Text.literal("Maximum level reached").formatted(Formatting.GRAY));
            }

            if (ModConfig.unlearnskill) {
                boolean canUnlearn = canUnlearnSkill(node.skill);
                if (canUnlearn) {
                    tooltip.add(Text.literal("Right-click to unlearn").formatted(Formatting.RED));
                } else {
                    List<Identifier> dependentSkills = findDependentSkills(node.skill.getId(), skillLevel);
                    if (!dependentSkills.isEmpty()) {
                        tooltip.add(Text.literal("Cannot unlearn: Required by other skills").formatted(Formatting.DARK_RED));

                        StringBuilder dependentNames = new StringBuilder();
                        for (int i = 0; i < Math.min(dependentSkills.size(), 3); i++) {
                            Skill dependentSkill = SkillRegistry.getSkill(dependentSkills.get(i));
                            if (dependentSkill != null) {
                                if (i > 0) dependentNames.append(", ");
                                dependentNames.append(dependentSkill.getName());
                            }
                        }
                        if (dependentSkills.size() > 3) {
                            dependentNames.append("...");
                        }
                        tooltip.add(Text.literal("Dependencies: " + dependentNames).formatted(Formatting.GRAY));
                    } else if (node.skill.getId().equals(SkillRegistry.BASICSKILL)) {
                        tooltip.add(Text.literal("Cannot unlearn: Basic skill").formatted(Formatting.DARK_RED));
                    } else if (node.skill.isDefaultSkill() && skillLevel <= 1) {
                        tooltip.add(Text.literal("Cannot unlearn: Default skill").formatted(Formatting.DARK_RED));
                    }
                }
            }
        }

        context.drawTooltip(textRenderer, tooltip, (int) mouseX, (int) mouseY);
    }

    private int getNodeColor(boolean isLearned, boolean prerequisitesMet, boolean canLearn, boolean canUpgrade) {
        if (isLearned) return 0xFF0D2818;
        if (canLearn || canUpgrade) return 0xFF2A1F0A;
        if (!prerequisitesMet) return 0xFF2A1010;
        return TREE_BG;
    }

    private int getNodeBorderColor(boolean isLearned, boolean prerequisitesMet, boolean canLearn, boolean canUpgrade, boolean isHovered) {
        if (isLearned) return isHovered ? GLOW_LEARNED : NODE_LEARNED;
        if (canLearn || canUpgrade) return isHovered ? GLOW_AVAILABLE : NODE_AVAILABLE;
        if (!prerequisitesMet) return NODE_ERROR;
        return NODE_LOCKED;
    }

    private int getConnectionColor(Skill skill, Skill.SkillRequirement req) {
        boolean skillLearned = classComponent.hasLearnedSkill(skill.getId());
        boolean prereqLearned = classComponent.hasLearnedSkill(req.skillId());
        int prereqLevel = prereqLearned ? classComponent.getSkillLevel(req.skillId()) : 0;
        boolean prereqMet = prereqLearned && prereqLevel >= req.minLevel();

        if (skillLearned && prereqMet) return CONNECTION_ACTIVE;
        if (prereqMet) return CONNECTION_READY;
        return CONNECTION_INACTIVE;
    }

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
        boolean canUpgrade = isLearned && skillLevel < node.skill.getMaxSkillLevel() && availablePoints >= node.skill.getUpgradeClassPointCost();

        if (button == 0) {
            if (canLearn) {
                SkillActionPayloadC2S.sendLearn(node.skill.getId());
                client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_LOOM_TAKE_RESULT, 1.2F));
                return true;
            } else if (canUpgrade) {
                SkillActionPayloadC2S.sendUpgrade(node.skill.getId());
                client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_LOOM_TAKE_RESULT, 1.0F));
                return true;
            } else {
                client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_BASS, 0.8F));
                return true;
            }
        } else if (button == 1 && isLearned && ModConfig.unlearnskill) {
            if (canUnlearnSkill(node.skill)) {
                SkillActionPayloadC2S.sendUnlearn(node.skill.getId());
                client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_LOOM_SELECT_PATTERN, 1.0F));
            } else {
                client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_BASS, 0.5F));
            }
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

    private record SkillTreeNode(Skill skill, int tier, int x, int y) {
    }

    private static class Connection {
        final SkillTreeNode from;
        final SkillTreeNode to;
        final int color;

        Connection(SkillTreeNode from, SkillTreeNode to, int color) {
            this.from = from;
            this.to = to;
            this.color = color;
        }
    }
}