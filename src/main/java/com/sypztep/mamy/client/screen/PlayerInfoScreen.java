package com.sypztep.mamy.client.screen;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.client.screen.widget.*;
import com.sypztep.mamy.client.toast.ToastRenderer;
import com.sypztep.mamy.client.util.AnimationUtils;
import com.sypztep.mamy.client.util.CyclingTextIcon;
import com.sypztep.mamy.client.util.DrawContextUtils;
import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.init.ModPassiveAbilities;
import com.sypztep.mamy.common.system.passive.PassiveAbility;
import com.sypztep.mamy.common.system.passive.PassiveAbilityManager;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.*;

@Environment(EnvType.CLIENT)
public final class PlayerInfoScreen extends Screen {
    // Tab system
    public enum Tab {
        STATS("Stats", Identifier.ofVanilla("icon/accessibility")),
        PASSIVES("Passive Abilities", Mamy.id("hud/container/icon_1"));

        public final String name;
        public final Identifier icon;

        Tab(String name, Identifier icon) {
            this.name = name;
            this.icon = icon;
        }
    }

    private Tab currentTab = Tab.STATS;
    private final Map<Tab, Animation> tabAnimations = new HashMap<>();

    // Existing fields
    private static final float ANIMATION_DURATION = 12.0f;
    private static final float FINAL_Y_OFFSET = 50.0f;

    private Animation verticalAnimation;
    private Animation fadeAnimation;
    private final LivingLevelComponent playerStats;
    private List<IncreasePointButton> increaseButtons;
    private List<Text> texts;
    private final CyclingTextIcon cyclingTextIcon;
    private SmoothProgressBar progressBar;
    private final ScrollableTextList playerInfo;

    // Tab dimensions
    private static final int TAB_WIDTH = 80;
    private static final int TAB_HEIGHT = 30;
    private static final int TAB_SPACING = 5;

    // Passive abilities fields
    private List<PassiveAbility> displayedAbilities;
    private PassiveAbility selectedAbility;
    private int passiveScrollOffset = 0;

    private final int buttonHeight = 16;
    private final int statLabelWidth = 30;
    private final int statRowHeight = 25;

    public PlayerInfoScreen(MinecraftClient client) {
        super(Text.literal(""));
        assert client.player != null;
        this.playerStats = ModEntityComponents.LIVINGLEVEL.get(client.player);
        Map<String, Object> infoKeys = createPlayerInfoKey(client);
        List<ListElement> listInfo = createListItems();

        this.playerInfo = new ScrollableTextList(listInfo, infoKeys);
        this.cyclingTextIcon = new CyclingTextIcon(100);

        // Initialize tab animations
        for (Tab tab : Tab.values()) {
            tabAnimations.put(tab, new Animation(0.3f));
        }

        // Initialize passive abilities
        this.displayedAbilities = ModPassiveAbilities.getAbilitiesOrderedByLevel();
    }

    // Existing methods (createPlayerInfoKey, createListItems, updateValues) remain the same...
    public void updateValues(MinecraftClient client) {
        Map<String, Object> values = createPlayerInfoKey(client);
        this.playerInfo.updateValues(values);
    }

    private Map<String, Object> createPlayerInfoKey(MinecraftClient client) {
        Map<String, Object> values = new HashMap<>();

        assert client.player != null;
        values.put("phyd", client.player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE));
        values.put("meleed", client.player.getAttributeValue(ModEntityAttributes.MELEE_ATTACK_DAMAGE));
        values.put("projd", client.player.getAttributeValue(ModEntityAttributes.PROJECTILE_ATTACK_DAMAGE));
        values.put("asp", client.player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED));
        values.put("bkdmg", client.player.getAttributeValue(ModEntityAttributes.BACK_ATTACK) * 100f);
        values.put("cdmg", client.player.getAttributeValue(ModEntityAttributes.CRIT_DAMAGE) * 100f);
        values.put("ccn", client.player.getAttributeValue(ModEntityAttributes.CRIT_CHANCE) * 100f);
        values.put("acc", client.player.getAttributeValue(ModEntityAttributes.ACCURACY));
        values.put("hp", client.player.getHealth());
        values.put("maxhp", client.player.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH));
        values.put("dp", client.player.getAttributeValue(EntityAttributes.GENERIC_ARMOR));
        values.put("nhrg", client.player.getAttributeValue(ModEntityAttributes.HEALTH_REGEN));
        values.put("hef", client.player.getAttributeValue(ModEntityAttributes.HEAL_EFFECTIVE));
        values.put("eva", client.player.getAttributeValue(ModEntityAttributes.EVASION));

        values.put("str", playerStats.getStatValue(StatTypes.STRENGTH));
        values.put("agi", playerStats.getStatValue(StatTypes.AGILITY));
        values.put("vit", playerStats.getStatValue(StatTypes.VITALITY));
        values.put("int", playerStats.getStatValue(StatTypes.INTELLIGENCE));
        values.put("dex", playerStats.getStatValue(StatTypes.DEXTERITY));
        values.put("luk", playerStats.getStatValue(StatTypes.LUCK));

        values.put("mdmg", client.player.getAttributeValue(ModEntityAttributes.MAGIC_ATTACK_DAMAGE));
        values.put("mresis", client.player.getAttributeValue(ModEntityAttributes.MAGIC_RESISTANCE) * 100f);
        return values;
    }

    private List<ListElement> createListItems() {
        List<ListElement> listElements = new ArrayList<>();

        listElements.add(new ListElement(Text.translatable("mamy.info.header_1"), Mamy.id("hud/container/icon_1")));
        listElements.add(new ListElement(Text.translatable("mamy.info.physical")));
        listElements.add(new ListElement(Text.translatable("mamy.info.melee_damage")));
        listElements.add(new ListElement(Text.translatable("mamy.info.projectile_damage")));
        listElements.add(new ListElement(Text.translatable("mamy.info.attack_speed")));
        listElements.add(new ListElement(Text.translatable("mamy.info.accuracy")));
        listElements.add(new ListElement(Text.translatable("mamy.info.backattack_damage")));
        listElements.add(new ListElement(Text.translatable("mamy.info.critical_damage")));
        listElements.add(new ListElement(Text.translatable("mamy.info.critical_chance")));
        listElements.add(new ListElement(Text.translatable("mamy.info.header_2"), Mamy.id("hud/container/icon_0")));
        listElements.add(new ListElement(Text.translatable("mamy.info.magic_damage")));
        listElements.add(new ListElement(Text.translatable("mamy.info.header_3"), Identifier.ofVanilla("hud/heart/full")));
        listElements.add(new ListElement(Text.translatable("mamy.info.health")));
        listElements.add(new ListElement(Text.translatable("mamy.info.max_health")));
        listElements.add(new ListElement(Text.translatable("mamy.info.defense")));
        listElements.add(new ListElement(Text.translatable("mamy.info.nature_health_regen")));
        listElements.add(new ListElement(Text.translatable("mamy.info.heal_effective")));
        listElements.add(new ListElement(Text.translatable("mamy.info.evasion")));
        listElements.add(new ListElement(Text.translatable("mamy.info.header_4"), Identifier.ofVanilla("icon/accessibility")));
        listElements.add(new ListElement(Text.translatable("mamy.info.strength")));
        listElements.add(new ListElement(Text.translatable("mamy.info.agility")));
        listElements.add(new ListElement(Text.translatable("mamy.info.vitality")));
        listElements.add(new ListElement(Text.translatable("mamy.info.intelligence")));
        listElements.add(new ListElement(Text.translatable("mamy.info.dexterity")));
        listElements.add(new ListElement(Text.translatable("mamy.info.luck")));
        listElements.add(new ListElement(Text.translatable("mamy.info.header_5"), Mamy.id("hud/container/icon_2")));
        listElements.add(new ListElement(Text.translatable("mamy.info.magic_resistance")));

        return listElements;
    }

    @Override
    protected void init() {
        super.init();
        this.verticalAnimation = new Animation(ANIMATION_DURATION);
        this.fadeAnimation = new Animation(ANIMATION_DURATION);
        this.progressBar = new SmoothProgressBar(ANIMATION_DURATION * 1.4f, false, 400, 2);
        increaseButtons = new ArrayList<>();

        // Only initialize stat buttons for STATS tab
        if (currentTab == Tab.STATS) {
            initializeStatButtons();
        }

        texts = Arrays.asList(
                Text.of("Lvl Progress: " + playerStats.getExperience() + "/" + playerStats.getExperienceToNextLevel()),
                Text.of(playerStats.getExperienceToNextLevel() + " XP to Level " + (playerStats.getLevel() + 1)),
                Text.of("Level: " + playerStats.getLevel() + " " + String.format("%.1f%%", playerStats.getExperiencePercentage()))
        );
    }

    private void initializeStatButtons() {
        increaseButtons.clear();
        int y = 50;
        for (StatTypes statType : StatTypes.values()) {
            int statValueWidth = 30;
            int startX = 50;
            int buttonX = startX + statLabelWidth + statValueWidth + 10;
            int buttonY = y;
            int buttonWidth = 16;
            IncreasePointButton increaseButton = new IncreasePointButton(buttonX, buttonY, buttonWidth, buttonHeight, Text.of("+"), playerStats, statType, 1, client);
            this.addDrawableChild(increaseButton);
            increaseButtons.add(increaseButton);

            y += statRowHeight;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        assert client != null;
        updateValues(client);

        // Update animations
        verticalAnimation.update(delta);
        fadeAnimation.update(delta);
        progressBar.update(delta);

        // Update tab animations
        for (Tab tab : Tab.values()) {
            Animation anim = tabAnimations.get(tab);
            float target = (tab == currentTab) ? 1.0f : 0.0f;
            float current = anim.getProgress();
            if (current != target) {
                float speed = 8.0f; // Fast tab switching
                if (target > current) {
                    anim.elapsedTime = Math.min(anim.elapsedTime + delta * speed, anim.getDuration());
                } else {
                    anim.elapsedTime = Math.max(anim.elapsedTime - delta * speed, 0);
                }
            }
        }

        int screenWidth = this.width;
        int screenHeight = this.height;

        // Set background color
        DrawContextUtils.fillScreen(context, 0xF0121212);

        // Render tabs
        renderTabs(context, screenWidth, mouseX, mouseY, delta);

        // Render content based on current tab
        int yOffset = (int) AnimationUtils.getPositionOffset(verticalAnimation.getProgress(), FINAL_Y_OFFSET, screenHeight);

        switch (currentTab) {
            case STATS:
                renderStatsTab(context, screenWidth, screenHeight, yOffset, mouseX, mouseY, delta);
                break;
            case PASSIVES:
                renderPassivesTab(context, screenWidth, screenHeight, yOffset, mouseX, mouseY, delta);
                break;
        }

        renderToastsOverScreen(context, delta);
    }

    private void renderTabs(DrawContext context, int screenWidth, int mouseX, int mouseY, float delta) {
        int tabStartX = screenWidth / 2 - (Tab.values().length * (TAB_WIDTH + TAB_SPACING) - TAB_SPACING) / 2;
        int tabY = 10;

        for (int i = 0; i < Tab.values().length; i++) {
            Tab tab = Tab.values()[i];
            int tabX = tabStartX + i * (TAB_WIDTH + TAB_SPACING);

            boolean isSelected = tab == currentTab;
            boolean isHovered = mouseX >= tabX && mouseX <= tabX + TAB_WIDTH &&
                    mouseY >= tabY && mouseY <= tabY + TAB_HEIGHT;

            renderTab(context, tab, tabX, tabY, TAB_WIDTH, TAB_HEIGHT, isSelected, isHovered, delta);
        }
    }

    private void renderTab(DrawContext context, Tab tab, int x, int y, int width, int height,
                           boolean isSelected, boolean isHovered, float delta) {

        Animation anim = tabAnimations.get(tab);
        float progress = anim.getProgress();

        // Background colors
        int bgColor;
        int borderColor;
        if (isSelected) {
            bgColor = 0xFF2A2A2A;
            borderColor = 0xFF4CAF50;
        } else if (isHovered) {
            bgColor = 0xFF1F1F1F;
            borderColor = 0xFF666666;
        } else {
            bgColor = 0xFF1A1A1A;
            borderColor = 0xFF444444;
        }

        // Apply selection animation
        if (isSelected) {
            int selectedAlpha = (int) (100 * progress);
            bgColor = (bgColor & 0x00FFFFFF) | ((0x2A + selectedAlpha) << 24);
        }

        // Render tab background
        context.fill(x, y, x + width, y + height, bgColor);

        // Render tab border (top and sides only, bottom connects to content)
        context.fill(x, y, x + width, y + 1, borderColor); // Top
        context.fill(x, y, x + 1, y + height, borderColor); // Left
        context.fill(x + width - 1, y, x + width, y + height, borderColor); // Right

        // Don't render bottom border if selected (connects to content)
        if (!isSelected) {
            context.fill(x, y + height - 1, x + width, y + height, borderColor); // Bottom
        }

        // Render icon and text
        int iconSize = 16;
        int iconX = x + 8;
        int iconY = y + (height - iconSize) / 2;

        // Icon
        context.drawGuiTexture(tab.icon, iconX, iconY, iconSize, iconSize);

        // Text
        int textX = iconX + iconSize + 4;
        int textY = y + (height - textRenderer.fontHeight) / 2;
        int textColor = isSelected ? 0x4CAF50 : (isHovered ? 0xFFFFFF : 0xAAAAAA);

        context.drawText(textRenderer, tab.name, textX, textY, textColor, false);

        // Selection indicator (bottom line)
        if (isSelected && progress > 0) {
            int indicatorHeight = 2;
            int indicatorY = y + height - indicatorHeight;
            context.fill(x, indicatorY, x + width, y + height, 0xFF4CAF50);
        }
    }

    private void renderStatsTab(DrawContext context, int screenWidth, int screenHeight, int yOffset,
                                int mouseX, int mouseY, float delta) {

        float contentSectionWidthRatio = 0.25f;
        float contentSectionHeightRatio = 0.75f;

        int contentWidth = (int) (screenWidth * contentSectionWidthRatio);
        int contentHeight = (int) (screenHeight * contentSectionHeightRatio);

        int xOffset = (int) (screenWidth * 0.67f);

        drawStatsSection(context, xOffset, yOffset, contentWidth, contentHeight, delta);
        renderStatsAndButtons(context, screenWidth, yOffset, mouseX, mouseY, delta);
        renderBottomLeftSection(context, screenWidth, screenHeight, delta);
        renderMiddleSection(context, screenWidth, screenHeight, fadeAnimation.getProgress());

        // Draw headers
        drawHeaderSection(context, xOffset + 100, yOffset, fadeAnimation.getProgress(), "mamy.gui.player_info.header");
        drawHeaderSection(context, (int) (screenWidth * 0.025f) + 80, yOffset, fadeAnimation.getProgress(), "mamy.gui.player_info.header_level");
    }

    private void renderPassivesTab(DrawContext context, int screenWidth, int screenHeight, int yOffset,
                                   int mouseX, int mouseY, float delta) {

        // Main content area
        int contentX = 50;
        int contentY = yOffset + 50;
        int contentWidth = screenWidth - 100;
        int contentHeight = screenHeight - contentY - 20;

        // Split into two sections: ability list (left) and details (right)
        int listWidth = contentWidth * 2 / 4;
        int detailsWidth = contentWidth / 2 - 2 ;
        int detailsX = contentX + listWidth + 5;

        // Render ability list
        renderPassiveAbilityList(context, contentX, contentY + 10, listWidth, contentHeight, mouseX, mouseY, delta);

        // Render details panel
        if (selectedAbility != null) {
            renderPassiveAbilityDetails(context, detailsX, contentY + 10, detailsWidth, contentHeight, delta);
        }

        // Render summary info at top
        renderPassiveSummary(context, contentX, contentY - 20, contentWidth, delta);
    }

    private void renderPassiveSummary(DrawContext context, int x, int y, int width, float delta) {
        PassiveAbilityManager manager = playerStats.getPassiveAbilityManager();
        if (manager == null) return;

        int unlockedCount = manager.getUnlockedAbilities().size();
        int activeCount = manager.getActiveAbilities().size();
        int totalCount = displayedAbilities.size();

        // Background
        context.fill(x, y, x + width, y + 25, 0xFF1E1E1E);
        context.fill(x, y, x + width, y + 1, 0xFF4CAF50);

        // Summary text
        String summaryText = String.format("Passive Abilities - Unlocked: %d/%d | Active: %d",
                unlockedCount, totalCount, activeCount);

        AnimationUtils.drawFadeText(context, textRenderer,
                Text.literal(summaryText).formatted(Formatting.GOLD),
                x + 10, y + 8, 0xFFD700, AnimationUtils.getAlpha(fadeAnimation.getProgress()));

        // Progress bar
        int barX = x + width - 150;
        int barY = y + 10;
        int barWidth = 120;
        int barHeight = 6;
        float progress = totalCount > 0 ? (float) unlockedCount / totalCount : 0;

        context.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF333333);
        if (progress > 0) {
            int progressWidth = (int) (barWidth * progress);
            context.fill(barX, barY, barX + progressWidth, barY + barHeight, 0xFF4CAF50);
        }
    }

    private void renderPassiveAbilityList(DrawContext context, int x, int y, int width, int height,
                                          int mouseX, int mouseY, float delta) {

        // Background
        context.fill(x, y, x + width, y + height, 0xFF1E1E1E);
        context.fill(x, y, x + width, y + 1, 0xFF444444);
        context.fill(x, y + height - 1, x + width, y + height, 0xFF444444);
        context.fill(x, y, x + 1, y + height, 0xFF444444);
        context.fill(x + width - 1, y, x + width, y + height, 0xFF444444);

        PassiveAbilityManager manager = playerStats.getPassiveAbilityManager();
        if (manager == null) return;

        int itemHeight = 35;
        int visibleCount = (height - 10) / itemHeight;
        int maxScroll = Math.max(0, displayedAbilities.size() - visibleCount);
        passiveScrollOffset = Math.max(0, Math.min(passiveScrollOffset, maxScroll));

        int currentY = y + 10;

        for (int i = passiveScrollOffset; i < displayedAbilities.size() && i < passiveScrollOffset + visibleCount; i++) {
            PassiveAbility ability = displayedAbilities.get(i);

            boolean unlocked = manager.isUnlocked(ability);
            boolean active = manager.isActive(ability);
            boolean canUnlock = ability.meetsRequirements(client.player);
            boolean isHovered = mouseX >= x + 5 && mouseX <= x + width - 5 &&
                    mouseY >= currentY && mouseY <= currentY + itemHeight - 2;
            boolean isSelected = ability == selectedAbility;

            // Background for item
            int itemBg = 0xFF1A1A1A;
            if (isSelected) {
                itemBg = 0xFF2A4A2A;
            } else if (isHovered) {
                itemBg = 0xFF2A2A2A;
            }

            context.fill(x + 5, currentY, x + width - 5, currentY + itemHeight - 2, itemBg);

            // Status indicator
            String statusIcon;
            Formatting color;
            if (active) {
                statusIcon = "✓";
                color = Formatting.GREEN;
            } else if (unlocked) {
                statusIcon = "⚠";
                color = Formatting.YELLOW;
            } else if (canUnlock) {
                statusIcon = "!";
                color = Formatting.AQUA;
            } else {
                statusIcon = "✗";
                color = Formatting.GRAY;
            }

            // Render ability info
            int textX = x + 15;

            // Name
            Text abilityName = Text.literal(statusIcon + " " + ability.getDisplayName().getString()).formatted(color);
            AnimationUtils.drawFadeText(context, textRenderer, abilityName, textX, currentY + 5,
                    color.getColorValue() != null ? color.getColorValue() : 0xFFFFFF,
                    AnimationUtils.getAlpha(fadeAnimation.getProgress()));

            // Requirements summary
            String reqSummary = getRequirementSummary(ability);
            AnimationUtils.drawFadeText(context, textRenderer,
                    Text.literal(reqSummary).formatted(Formatting.DARK_GRAY),
                    textX, currentY + 20, 0x888888, AnimationUtils.getAlpha(fadeAnimation.getProgress()));

            currentY += itemHeight;
        }

        // Scrollbar if needed
        if (displayedAbilities.size() > visibleCount) {
            renderScrollbar(context, x + width - 8, y + 10, 6, height - 20, passiveScrollOffset, maxScroll);
        }
    }

    private void renderScrollbar(DrawContext context, int x, int y, int width, int height, int offset, int maxOffset) {
        // Background
        context.fill(x, y, x + width, y + height, 0xFF333333);

        if (maxOffset > 0) {
            // Thumb
            int thumbHeight = Math.max(20, height * height / (height + maxOffset * 35));
            int thumbY = y + (height - thumbHeight) * offset / maxOffset;
            context.fill(x, thumbY, x + width, thumbY + thumbHeight, 0xFF666666);
        }
    }

    private void renderPassiveAbilityDetails(DrawContext context, int x, int y, int width, int height, float delta) {
        // Background
        context.fill(x, y, x + width, y + height, 0xFF1E1E1E);
        context.fill(x, y, x + width, y + 1, 0xFF444444);
        context.fill(x, y + height - 1, x + width, y + height, 0xFF444444);
        context.fill(x, y, x + 1, y + height, 0xFF444444);
        context.fill(x + width - 1, y, x + width, y + height, 0xFF444444);

        if (selectedAbility == null) {
            AnimationUtils.drawFadeCenteredText(context, textRenderer,
                    Text.literal("Select an ability").formatted(Formatting.GRAY),
                    x + width / 2, y + height / 2, 0x888888, AnimationUtils.getAlpha(fadeAnimation.getProgress()));
            return;
        }

        int currentY = y + 10;
        int textX = x + 10;
        int maxWidth = width - 20;

        PassiveAbilityManager manager = playerStats.getPassiveAbilityManager();
        boolean unlocked = manager != null && manager.isUnlocked(selectedAbility);
        boolean active = manager != null && manager.isActive(selectedAbility);
        boolean canUnlock = selectedAbility.meetsRequirements(client.player);

        // Title
        String statusSuffix = active ? " ✓" : (unlocked ? " ⚠" : (canUnlock ? " !" : " ✗"));
        Formatting titleColor = active ? Formatting.GREEN : (unlocked ? Formatting.YELLOW : (canUnlock ? Formatting.AQUA : Formatting.GRAY));

        Text titleText = selectedAbility.getDisplayName().copy().formatted(titleColor, Formatting.BOLD)
                .append(Text.literal(statusSuffix).formatted(Formatting.WHITE));

        AnimationUtils.drawFadeText(context, textRenderer, titleText, textX, currentY,
                titleColor.getColorValue() != null ? titleColor.getColorValue() : 0xFFFFFF,
                AnimationUtils.getAlpha(fadeAnimation.getProgress()));
        currentY += textRenderer.fontHeight + 8;

        // Status
        String statusText = active ? "§a● ACTIVE" : (unlocked ? "§e● UNLOCKED" : (canUnlock ? "§b● CAN UNLOCK!" : "§7● LOCKED"));
        AnimationUtils.drawFadeText(context, textRenderer, Text.literal(statusText), textX, currentY, 0xFFFFFF,
                AnimationUtils.getAlpha(fadeAnimation.getProgress()));
        currentY += textRenderer.fontHeight + 10;

        // Description
        List<String> descLines = wrapText(selectedAbility.getDescription().getString(), maxWidth);
        for (String line : descLines) {
            AnimationUtils.drawFadeText(context, textRenderer, Text.literal(line).formatted(Formatting.GRAY),
                    textX, currentY, 0xAAAAAA, AnimationUtils.getAlpha(fadeAnimation.getProgress()));
            currentY += textRenderer.fontHeight + 2;
        }
        currentY += 10;

        // Requirements
        AnimationUtils.drawFadeText(context, textRenderer, Text.literal("Requirements:").formatted(Formatting.GOLD),
                textX, currentY, 0xFFD700, AnimationUtils.getAlpha(fadeAnimation.getProgress()));
        currentY += textRenderer.fontHeight + 5;

        for (var req : selectedAbility.getRequirements().entrySet()) {
            var statType = req.getKey();
            int required = req.getValue();
            int current = playerStats.getStatValue(statType);

            boolean meets = current >= required;
            Formatting reqColor = meets ? Formatting.GREEN : Formatting.RED;
            String reqText = String.format("  %s: %d/%d", statType.getAka(), current, required);

            AnimationUtils.drawFadeText(context, textRenderer, Text.literal(reqText).formatted(reqColor),
                    textX, currentY, reqColor.getColorValue() != null ? reqColor.getColorValue() : 0xFFFFFF,
                    AnimationUtils.getAlpha(fadeAnimation.getProgress()));
            currentY += textRenderer.fontHeight + 2;
        }
    }

    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
            if (textRenderer.getWidth(testLine) <= maxWidth) {
                currentLine = new StringBuilder(testLine);
            } else {
                if (!currentLine.isEmpty()) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    lines.add(word);
                }
            }
        }

        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    private String getRequirementSummary(PassiveAbility ability) {
        StringBuilder summary = new StringBuilder();
        var requirements = ability.getRequirements();

        for (var entry : requirements.entrySet()) {
            if (!summary.isEmpty()) summary.append(", ");
            summary.append(entry.getKey().getAka()).append(" ").append(entry.getValue());
        }

        return summary.toString();
    }

    // Keep all existing methods for stats rendering...
    private void drawStatsSection(DrawContext context, int xOffset, float yOffset, int contentWidth, int contentHeight, float deltatick) {
        this.playerInfo.render(context, this.textRenderer, xOffset + 25, (int) (yOffset + 55), contentWidth, contentHeight, 0.5f, 1f, AnimationUtils.getAlpha(fadeAnimation.getProgress()), deltatick);
    }

    private void renderStyledText(DrawContext context, int x, int y, int statValue, float scale) {
        Text perPointText = Text.of(String.valueOf(statValue)).copy().setStyle(Style.EMPTY.withColor((0xF17633)));
        Text pointText = Text.of(" Point").copy().setStyle(Style.EMPTY.withColor(Formatting.WHITE));

        Text text = Text.empty()
                .append(perPointText)
                .append(pointText);

        context.drawTextWithShadow(this.textRenderer, text, (int) (x / scale), (int) (y / scale), 0xFFFFFF);
    }

    private void renderStatsAndButtons(DrawContext context, int screenWidth, int yOffset, int mouseX, int mouseY, float delta) {
        int rectX = (int) (screenWidth * 0.025f);
        int rectY = yOffset + 20;
        int y;
        int labelX = rectX + 10;

        int buttonIndex = 0;

        int maxWidth = 0;
        int totalHeight = 0;

        MatrixStack matrixStack = context.getMatrices();

        for (StatTypes statType : StatTypes.values()) {
            String label = statType.getAka() + ":";
            int labelWidth = this.textRenderer.getWidth(Text.of(label));

            int pointCost = playerStats.getStatCost(statType);
            int valueWidth = this.textRenderer.getWidth(Text.of(pointCost + " Point"));

            maxWidth = Math.max(maxWidth, labelWidth + valueWidth + 50);
            totalHeight += statRowHeight + 2;
        }

        DrawContextUtils.drawRect(context, rectX, rectY, maxWidth, totalHeight, 0xFF1E1E1E);

        y = rectY + 10;

        for (StatTypes statType : StatTypes.values()) {
            String displayName = statType.getAka();

            int labelY = y + (buttonHeight - this.textRenderer.fontHeight) / 2;
            float scale = 0.9f;

            matrixStack.push();
            matrixStack.scale(scale, scale, 0);
            context.drawTextWithShadow(this.textRenderer, Text.of(displayName + ":"),
                    (int) (labelX / scale), (int) (labelY / scale), 0xFFFFFF);
            matrixStack.pop();

            DrawContextUtils.renderHorizontalLineWithCenterGradient(context, rectX, y + 20, maxWidth, 1, 1, 0xFFFFFFFF, 0x00FFFFFF);

            int valueX = labelX + statLabelWidth;
            int pointCost = playerStats.getStatCost(statType);

            matrixStack.push();
            matrixStack.scale(scale, scale, 0);
            renderStyledText(context, valueX, labelY, pointCost, scale);
            matrixStack.pop();

            IncreasePointButton button = increaseButtons.get(buttonIndex);
            int buttonX = valueX + statLabelWidth + 12;
            button.setX(buttonX);
            button.setY(y);
            button.render(context, mouseX, mouseY, delta);

            y += statRowHeight;
            buttonIndex++;
        }
    }

    private void renderMiddleSection(DrawContext context, int screenWidth, int screenHeight, float fadeProgress) {
        int remainingPoints = playerStats.getAvailableStatPoints();
        float scaleFactor = 2.5f;
        int adjustedX = (int) (screenWidth * 0.5f / scaleFactor);
        int adjustedY = (int) (screenHeight * 0.5f / scaleFactor);

        String pointText = remainingPoints > 1 ? "Benefit Points" : "Benefit Point";

        context.getMatrices().push();
        context.getMatrices().scale(scaleFactor, scaleFactor, 0.0f);

        AnimationUtils.drawFadeCenteredText(context, textRenderer, Text.of("" + remainingPoints), adjustedX, adjustedY, 0xF17633, AnimationUtils.getAlpha(fadeProgress));

        context.getMatrices().pop();
        AnimationUtils.drawFadeCenteredText(context, textRenderer, Text.of(pointText), (int) (screenWidth * 0.5f), (int) (screenHeight * 0.5f) + 25, 0xFFFFFF, AnimationUtils.getAlpha(fadeProgress));
    }

    private void renderBottomLeftSection(DrawContext context, int screenWidth, int screenHeight, float delta) {
        int labelX = (int) (screenWidth * 0.025f);
        int labelY = screenHeight - 45;
        context.getMatrices().push();
        context.getMatrices().scale(0.8f, 0.8f, 1.0f);
        int scaledLabelX = (int) (labelX / 0.8f);
        int scaledLabelY = (int) (labelY / 0.8f);

        cyclingTextIcon.render(context, textRenderer, delta, scaledLabelX, scaledLabelY + 20, 0xFFFFFF);

        progressBar.setProgress(playerStats.getExperience(), playerStats.getExperienceToNextLevel());
        progressBar.render(context, scaledLabelX, scaledLabelY + 40);
        context.getMatrices().pop();
    }

    private void drawHeaderSection(DrawContext context, int x, float verticalOffset, float fadeProgress, String text) {
        int textWidth = this.textRenderer.getWidth(Text.translatable(text));
        int centeredX = x - (textWidth / 2);
        AnimationUtils.drawFadeText(context, this.textRenderer, Text.translatable(text), centeredX, (int) verticalOffset, AnimationUtils.getAlpha(fadeProgress));
        int lineY1 = (int) (verticalOffset - 4);
        int lineY2 = (int) (verticalOffset + 10);
        DrawContextUtils.renderHorizontalLineWithCenterGradient(context, centeredX - 16, lineY1, textWidth + 32, 1, 400, 0xFFFFFFFF, 0, fadeProgress);
        DrawContextUtils.renderHorizontalLineWithCenterGradient(context, centeredX - 16, lineY2, textWidth + 32, 1, 400, 0xFFFFFFFF, 0, fadeProgress);
    }

    private void renderToastsOverScreen(DrawContext context, float delta) {
        float deltaTime = delta / 20.0f;
        ToastRenderer.renderToasts(context, this.width, deltaTime);
    }

    @Override
    public void tick() {
        super.tick();
        if (!texts.isEmpty())
            cyclingTextIcon.updateTexts(texts);
    }


    private boolean handleTabClick(double mouseX, double mouseY, int button) {
        if (button != 0) return false; // Left click only

        int tabStartX = width / 2 - (Tab.values().length * (TAB_WIDTH + TAB_SPACING) - TAB_SPACING) / 2;
        int tabY = 10;

        for (int i = 0; i < Tab.values().length; i++) {
            Tab tab = Tab.values()[i];
            int tabX = tabStartX + i * (TAB_WIDTH + TAB_SPACING);

            if (mouseX >= tabX && mouseX <= tabX + TAB_WIDTH &&
                    mouseY >= tabY && mouseY <= tabY + TAB_HEIGHT) {

                if (tab != currentTab) {
                    switchToTab(tab);
                }
                return true;
            }
        }

        return false;
    }

    private boolean handlePassiveAbilityClick(double mouseX, double mouseY) {
        int contentX = 50;
        int yOffset = (int) AnimationUtils.getPositionOffset(verticalAnimation.getProgress(), FINAL_Y_OFFSET, height);
        int contentY = yOffset + 50;
        int listWidth = (width - 100) * 2 / 3;

        if (mouseX >= contentX + 5 && mouseX <= contentX + listWidth - 5) {
            int itemHeight = 35;
            int relativeY = (int) (mouseY - contentY - 10);
            int itemIndex = relativeY / itemHeight + passiveScrollOffset;

            if (itemIndex >= 0 && itemIndex < displayedAbilities.size()) {
                selectedAbility = displayedAbilities.get(itemIndex);
                return true;
            }
        }

        return false;
    }

    private void switchToTab(Tab newTab) {
        if (newTab == currentTab) return;

        // Clear current tab's widgets
        this.clearChildren();

        currentTab = newTab;

        // Re-initialize for new tab
        if (currentTab == Tab.STATS) {
            initializeStatButtons();
        }

        // Reset animations for smooth transition
        tabAnimations.get(newTab).elapsedTime = 0;
    }

    // Add these methods to your PlayerInfoScreen class

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Handle tab clicking first
        if (handleTabClick(mouseX, mouseY, button)) {
            return true;
        }

        // Handle passive ability selection in PASSIVES tab
        if (currentTab == Tab.PASSIVES && button == 0) {
            if (handlePassiveAbilityClick(mouseX, mouseY)) {
                return true;
            }
        }

        // Handle scrollable text list interactions in STATS tab
        if (currentTab == Tab.STATS && button == 0) {
            if (playerInfo.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        // Handle scrollable text list dragging in STATS tab
        if (currentTab == Tab.STATS) {
            if (playerInfo.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
                return true;
            }
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // Handle scrollable text list mouse release in STATS tab
        if (currentTab == Tab.STATS) {
            playerInfo.mouseReleased(mouseX, mouseY, button);
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (currentTab == Tab.STATS) {
            // Handle scrolling in stats info panel with enhanced smoothness
            boolean isAnyScrolled = false;
            int scrollAmount = (int)(verticalAmount * 25);
            if (playerInfo.isMouseOver(mouseX, mouseY, playerInfo.getX(), playerInfo.getY() - 30,
                    playerInfo.getWidth(), playerInfo.getHeight())) {
                playerInfo.scroll(scrollAmount, mouseX, mouseY + 30);
                isAnyScrolled = true;
            }
            return isAnyScrolled || super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        } else if (currentTab == Tab.PASSIVES) {
            // Handle scrolling in passive abilities list (existing code)
            int contentX = 50;
            int yOffset = (int) AnimationUtils.getPositionOffset(verticalAnimation.getProgress(), FINAL_Y_OFFSET, height);
            int contentY = yOffset + 50;
            int listWidth = (width - 100) * 2 / 3;
            int contentHeight = height - contentY - 50;

            if (mouseX >= contentX && mouseX <= contentX + listWidth &&
                    mouseY >= contentY && mouseY <= contentY + contentHeight) {

                int itemHeight = 35;
                int visibleCount = (contentHeight - 20) / itemHeight;
                int maxScroll = Math.max(0, displayedAbilities.size() - visibleCount);

                passiveScrollOffset = Math.max(0, Math.min(maxScroll, passiveScrollOffset - (int) verticalAmount));
                return true;
            }
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Tab switching with number keys
        if (keyCode >= 49 && keyCode <= 57) { // Keys 1-9
            int tabIndex = keyCode - 49; // Convert to 0-based index
            if (tabIndex < Tab.values().length) {
                switchToTab(Tab.values()[tabIndex]);
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}