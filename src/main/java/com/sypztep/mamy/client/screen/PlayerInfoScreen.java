package com.sypztep.mamy.client.screen;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.client.screen.widget.*;
import com.sypztep.mamy.client.toast.ToastRenderer;
import com.sypztep.mamy.client.util.DrawContextUtils;
import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.init.ModEntityComponents;
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
    // Player Stats
    private final LivingLevelComponent playerStats;

    // UI Components
    private List<IncreasePointButton> increaseButtons;
    private final ScrollableTextList playerInfo;

    private final int buttonHeight = 16;
    private final int statLabelWidth = 30;
    private final int statRowHeight = 25;

    public PlayerInfoScreen(MinecraftClient client) {
        super(Text.literal("")); // Keep original title
        assert client.player != null;
        this.playerStats = ModEntityComponents.LIVINGLEVEL.get(client.player);
        Map<String, Object> infoKeys = createPlayerInfoKey(client);
        List<ListElement> listInfo = createListItems();

        this.playerInfo = new ScrollableTextList(listInfo, infoKeys);
    }

    public void updateValues(MinecraftClient client) {
        Map<String, Object> values = createPlayerInfoKey(client);
        this.playerInfo.updateValues(values);
    }

    private Map<String, Object> createPlayerInfoKey(MinecraftClient client) {
        Map<String, Object> values = new HashMap<>();
        assert client.player != null;

        // ==========================================
        // COMBAT STATS - Offensive Power
        // ==========================================
        values.put("phyd", client.player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE));
        values.put("meleed", client.player.getAttributeValue(ModEntityAttributes.MELEE_ATTACK_DAMAGE));
        values.put("projd", client.player.getAttributeValue(ModEntityAttributes.PROJECTILE_ATTACK_DAMAGE));
        values.put("mdmg", client.player.getAttributeValue(ModEntityAttributes.MAGIC_ATTACK_DAMAGE));
        values.put("asp", client.player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED));

        // ==========================================
        // PRECISION & CRITICAL STATS
        // ==========================================
        values.put("acc", client.player.getAttributeValue(ModEntityAttributes.ACCURACY));
        values.put("ccn", client.player.getAttributeValue(ModEntityAttributes.CRIT_CHANCE) * 100f);
        values.put("cdmg", client.player.getAttributeValue(ModEntityAttributes.CRIT_DAMAGE) * 100f);
        values.put("bkdmg", client.player.getAttributeValue(ModEntityAttributes.BACK_ATTACK) * 100f);
        values.put("spedmg", client.player.getAttributeValue(ModEntityAttributes.SPECIAL_ATTACK) * 100f);

        // ==========================================
        // DEFENSIVE STATS
        // ==========================================
        values.put("hp", client.player.getHealth());
        values.put("maxhp", client.player.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH));
        values.put("dp", client.player.getAttributeValue(EntityAttributes.GENERIC_ARMOR));
        values.put("drec", client.player.getAttributeValue(ModEntityAttributes.DAMAGE_REDUCTION) * 100f);
        values.put("eva", client.player.getAttributeValue(ModEntityAttributes.EVASION));
        values.put("mresis", client.player.getAttributeValue(ModEntityAttributes.MAGIC_RESISTANCE) * 100f);

        // ==========================================
        // REGENERATION & RECOVERY
        // ==========================================
        values.put("nhrg", client.player.getAttributeValue(ModEntityAttributes.HEALTH_REGEN));
        values.put("hef", client.player.getAttributeValue(ModEntityAttributes.HEAL_EFFECTIVE));

        // ==========================================
        // BASE ATTRIBUTES
        // ==========================================
        values.put("str", playerStats.getStatValue(StatTypes.STRENGTH));
        values.put("agi", playerStats.getStatValue(StatTypes.AGILITY));
        values.put("vit", playerStats.getStatValue(StatTypes.VITALITY));
        values.put("int", playerStats.getStatValue(StatTypes.INTELLIGENCE));
        values.put("dex", playerStats.getStatValue(StatTypes.DEXTERITY));
        values.put("luk", playerStats.getStatValue(StatTypes.LUCK));

        values.put("cstr", playerStats.getStatByType(StatTypes.STRENGTH).getClassBonus());
        values.put("cagi", playerStats.getStatByType(StatTypes.AGILITY).getClassBonus());
        values.put("cvit", playerStats.getStatByType(StatTypes.VITALITY).getClassBonus());
        values.put("cint", playerStats.getStatByType(StatTypes.INTELLIGENCE).getClassBonus());
        values.put("cdex", playerStats.getStatByType(StatTypes.DEXTERITY).getClassBonus());
        values.put("cluk", playerStats.getStatByType(StatTypes.LUCK).getClassBonus());

        return values;
    }
    private List<ListElement> createListItems() {
        List<ListElement> listElements = new ArrayList<>();

        // ==========================================
        // COMBAT POWER SECTION
        // ==========================================
        listElements.add(new ListElement(Text.translatable("mamy.info.header_combat"),
                Mamy.id("hud/container/icon_sword")));
        listElements.add(new ListElement(Text.translatable("mamy.info.physical")));
        listElements.add(new ListElement(Text.translatable("mamy.info.melee_damage")));
        listElements.add(new ListElement(Text.translatable("mamy.info.projectile_damage")));
        listElements.add(new ListElement(Text.translatable("mamy.info.magic_damage")));
        listElements.add(new ListElement(Text.translatable("mamy.info.attack_speed")));

        // ==========================================
        // PRECISION & CRITICAL SECTION
        // ==========================================
        listElements.add(new ListElement(Text.translatable("mamy.info.header_precision"),
                Mamy.id("hud/container/icon_crosshair")));
        listElements.add(new ListElement(Text.translatable("mamy.info.accuracy")));
        listElements.add(new ListElement(Text.translatable("mamy.info.critical_chance")));
        listElements.add(new ListElement(Text.translatable("mamy.info.critical_damage")));
        listElements.add(new ListElement(Text.translatable("mamy.info.backattack_damage")));
        listElements.add(new ListElement(Text.translatable("mamy.info.special_damage")));

        // ==========================================
        // DEFENSIVE SECTION
        // ==========================================
        listElements.add(new ListElement(Text.translatable("mamy.info.header_defense"),
                Identifier.ofVanilla("hud/heart/full")));
        listElements.add(new ListElement(Text.translatable("mamy.info.health")));
        listElements.add(new ListElement(Text.translatable("mamy.info.max_health")));
        listElements.add(new ListElement(Text.translatable("mamy.info.defense")));
        listElements.add(new ListElement(Text.translatable("mamy.info.damage_reduction")));
        listElements.add(new ListElement(Text.translatable("mamy.info.evasion")));
        listElements.add(new ListElement(Text.translatable("mamy.info.magic_resistance")));

        // ==========================================
        // REGENERATION & RECOVERY SECTION
        // ==========================================
        listElements.add(new ListElement(Text.translatable("mamy.info.header_recovery"),
                Mamy.id("hud/container/icon_heal")));
        listElements.add(new ListElement(Text.translatable("mamy.info.nature_health_regen")));
        listElements.add(new ListElement(Text.translatable("mamy.info.heal_effective")));

        // ==========================================
        // BASE ATTRIBUTES SECTION
        // ==========================================
        listElements.add(new ListElement(Text.translatable("mamy.info.header_attributes"),
                Identifier.ofVanilla("icon/accessibility")));
        listElements.add(new ListElement(Text.translatable("mamy.info.strength")));
        listElements.add(new ListElement(Text.translatable("mamy.info.agility")));
        listElements.add(new ListElement(Text.translatable("mamy.info.vitality")));
        listElements.add(new ListElement(Text.translatable("mamy.info.intelligence")));
        listElements.add(new ListElement(Text.translatable("mamy.info.dexterity")));
        listElements.add(new ListElement(Text.translatable("mamy.info.luck")));

        listElements.add(new ListElement(Text.translatable("mamy.info.header_classbonus"),
                Identifier.ofVanilla("icon/accessibility")));
        listElements.add(new ListElement(Text.translatable("mamy.info.classbonus.strength")));
        listElements.add(new ListElement(Text.translatable("mamy.info.classbonus.agility")));
        listElements.add(new ListElement(Text.translatable("mamy.info.classbonus.vitality")));
        listElements.add(new ListElement(Text.translatable("mamy.info.classbonus.intelligence")));
        listElements.add(new ListElement(Text.translatable("mamy.info.classbonus.dexterity")));
        listElements.add(new ListElement(Text.translatable("mamy.info.classbonus.luck")));
        return listElements;
    }

    @Override
    protected void init() {
        super.init();
        increaseButtons = new ArrayList<>(); // Initialize the list to hold buttons

        int y = 50;
        for (StatTypes statType : StatTypes.values()) {
            int statValueWidth = 30;
            int startX = 50;
            int buttonX = startX + statLabelWidth + statValueWidth + 10; // Some spacing
            int buttonY = y;
            int buttonWidth = 16;
            IncreasePointButton increaseButton = new IncreasePointButton(buttonX, buttonY, buttonWidth, buttonHeight, Text.of("+"), playerStats, statType, 1, client); // Updated constructor
            this.addDrawableChild(increaseButton);
            increaseButtons.add(increaseButton);

            y += statRowHeight; // Move to the next row
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        assert client != null;
        updateValues(client);
        DrawContextUtils.fillScreenVerticalRatio(context,0xFC141414,0,0xFC141414);

        int screenWidth = this.width;
        int screenHeight = this.height;

        float contentSectionWidthRatio = 0.25f; // 25% of screen width
        float contentSectionHeightRatio = 0.75f; // 50% of screen height (keep original comment)

        int contentWidth = (int) (screenWidth * contentSectionWidthRatio);
        int contentHeight = (int) (screenHeight * contentSectionHeightRatio);

        int xOffset = (int) (screenWidth * 0.67f); // 2/3 of the screen width
        int yOffset = 50; // Static offset, removed animation
        drawStatsSection(context, xOffset, yOffset, contentWidth, contentHeight, delta, mouseX, mouseY);

        renderStatsAndButtons(context, screenWidth, yOffset, mouseX, mouseY, delta);

        renderBenefitPoint(context, screenWidth, screenHeight);

        // Draw header section - updated translation keys
        drawHeaderSection(context, xOffset + 100, yOffset, "mamy.gui.player_info.header");
        drawHeaderSection(context, (int) (screenWidth * 0.025f) + 80, yOffset, "mamy.gui.player_info.header_level");

        renderToastsOverScreen(context, delta);
    }

    private void renderToastsOverScreen(DrawContext context, float delta) {
        float deltaTime = delta / 16.0f;
        ToastRenderer.renderToasts(context, this.width, deltaTime);
    }

    private void drawStatsSection(DrawContext context, int xOffset, float yOffset, int contentWidth, int contentHeight, float deltatick, int mouseX, int mouseY) {
        this.playerInfo.render(context, this.textRenderer, xOffset + 25, (int) (yOffset + 18), contentWidth, contentHeight, 0.5f, 1f, 1, deltatick, mouseX, mouseY);
    }

    private void renderStyledText(DrawContext context, int x, int y, int statValue, float scale) { // Updated parameter name
        Text perPointText = Text.of(String.valueOf(statValue)).copy().setStyle(Style.EMPTY.withColor((0xF17633))); // Updated variable name
        Text pointText = Text.of(" Point").copy().setStyle(Style.EMPTY.withColor(Formatting.WHITE));

        Text text = Text.empty()
                .append(perPointText)
                .append(pointText);

        // Render the styled text
        context.drawTextWithShadow(this.textRenderer, text, (int) (x / scale), (int) (y / scale), 0xFFFFFF);
    }

    private void renderStatsAndButtons(DrawContext context, int screenWidth, int yOffset, int mouseX, int mouseY, float delta) {
        int rectX = (int) (screenWidth * 0.025f);
        int rectY = yOffset + 20;
        int y;
        int labelX = rectX + 10;

        int buttonIndex = 0;

        // Calculate maximum width and height needed
        int maxWidth = 0;
        int totalHeight = 0;

        MatrixStack matrixStack = context.getMatrices();

        for (StatTypes statType : StatTypes.values()) {
            String label = statType.getAka() + ":";
            int labelWidth = this.textRenderer.getWidth(Text.of(label));

            int pointCost =playerStats.getStatCost(statType);
            int valueWidth = this.textRenderer.getWidth(Text.of(pointCost + " Point"));

            // Add padding to width calculation
            maxWidth = Math.max(maxWidth, labelWidth + valueWidth + 50);
            totalHeight += statRowHeight + 2;
        }

        // Draw the rectangle with the calculated size
        DrawContextUtils.drawRect(context, rectX, rectY, maxWidth, totalHeight, 0xFF1E1E1E);

        // Render the content inside the rectangle
        y = rectY + 10;

        // Use StatTypes enum instead of arrays
        for (StatTypes statType : StatTypes.values()) {
            String displayName = statType.getAka(); // Use enum method instead of array

            int labelY = y + (buttonHeight - this.textRenderer.fontHeight) / 2;
            float scale = 0.9f;

            matrixStack.push();
            matrixStack.scale(scale, scale, 0);
            context.drawTextWithShadow(this.textRenderer, Text.of(displayName + ":"),
                    (int) (labelX / scale), (int) (labelY / scale), 0xFFFFFF);
            matrixStack.pop();

            DrawContextUtils.renderHorizontalLineWithCenterGradient(context, rectX, y + 20, maxWidth, 1, 1, 0xFFFFFFFF, 0x00FFFFFF);

            int valueX = labelX + statLabelWidth;

            int pointCost; // Default value
            pointCost = playerStats.getStatCost(statType); // Direct access to cost

            matrixStack.push();
            matrixStack.scale(scale, scale, 0);
            renderStyledText(context, valueX, labelY, pointCost, scale);
            matrixStack.pop();

            // Position and render the corresponding button
            IncreasePointButton button = increaseButtons.get(buttonIndex);
            int buttonX = valueX + statLabelWidth + 12;
            button.setX(buttonX);
            button.setY(y);
            button.render(context, mouseX, mouseY, delta);

            y += statRowHeight;
            buttonIndex++;
        }
    }

    private void renderBenefitPoint(DrawContext context, int screenWidth, int screenHeight) {
        int remainingPoints = playerStats.getAvailableStatPoints();
        float scaleFactor = 2.5f;
        int posX = (int) (screenWidth * 0.50f);
        int posY = (int) (screenHeight * 0.75f);
        int adjustedX = (int) (posX / scaleFactor);
        int adjustedY = (int) (posY / scaleFactor);

        String fuckyougramma = remainingPoints > 1 ? "Benefit Points" : "Benefit Point"; // Updated text

        context.getMatrices().push();
        context.getMatrices().scale(scaleFactor, scaleFactor, 0.0f);

        context.drawCenteredTextWithShadow(textRenderer, Text.of("" + remainingPoints), adjustedX, adjustedY + 5 , 0xF17633);
        context.getMatrices().pop();
        context.drawCenteredTextWithShadow(textRenderer, Text.of(fuckyougramma),  posX, posY + 35, 0xFFFFFF);
    }

    private void drawHeaderSection(DrawContext context, int x, float verticalOffset, String text) {
        int textWidth = this.textRenderer.getWidth(Text.translatable(text));
        int centeredX = x - (textWidth / 2);
        context.drawText(this.textRenderer, Text.translatable(text), centeredX, (int) verticalOffset, 0xFFFFFF, false);
        int lineY1 = (int) (verticalOffset - 4);
        int lineY2 = (int) (verticalOffset + 10);
        DrawContextUtils.renderHorizontalLineWithCenterGradient(context, centeredX - 16, lineY1, textWidth + 32, 1, 400, 0xFFFFFFFF, 0, 1.0f);
        DrawContextUtils.renderHorizontalLineWithCenterGradient(context, centeredX - 16, lineY2, textWidth + 32, 1, 400, 0xFFFFFFFF, 0, 1.0f);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (playerInfo.isMouseOver(mouseX, mouseY, playerInfo.getX(), playerInfo.getY() - 30,
                playerInfo.getWidth(), playerInfo.getHeight() + 30)) {
            if (playerInfo.handleMouseScroll(mouseX, mouseY, horizontalAmount, verticalAmount)) return true;
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (playerInfo.isMouseOver(mouseX, mouseY, playerInfo.getX(), playerInfo.getY(),
                playerInfo.getWidth(), playerInfo.getHeight())) {
            if (playerInfo.handleMouseClick(mouseX, mouseY, button)) return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (playerInfo.handleMouseDrag(mouseX, mouseY, button, deltaX, deltaY)) return true;

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        playerInfo.handleMouseRelease(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }
    @Override
    public boolean shouldPause() {
        return false;
    }
}