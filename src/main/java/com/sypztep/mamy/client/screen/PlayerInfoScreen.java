package com.sypztep.mamy.client.screen;

import com.sypztep.mamy.Mamy;
import com.sypztep.mamy.client.screen.widget.ListElement;
import com.sypztep.mamy.client.screen.widget.ScrollableTextList;
import com.sypztep.mamy.client.screen.widget.ScrollableStat;
import com.sypztep.mamy.client.screen.widget.ScrollablePlayerInfo;
import com.sypztep.mamy.client.toast.ToastRenderer;
import com.sypztep.mamy.client.util.DrawContextUtils;
import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.component.living.PlayerClassComponent;
import com.sypztep.mamy.common.init.ModEntityAttributes;
import com.sypztep.mamy.common.init.ModEntityComponents;
import com.sypztep.mamy.common.system.stat.Stat;
import com.sypztep.mamy.common.system.stat.StatTypes;
import com.sypztep.mamy.common.system.damage.DamageUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.*;

@Environment(EnvType.CLIENT)
public final class PlayerInfoScreen extends Screen {
    private final LivingLevelComponent playerStats;
    private final PlayerClassComponent playerClassComponent;
    private final ScrollableTextList playerInfo;
    private final ScrollableStat scrollableStat;
    private final ScrollablePlayerInfo scrollablePlayerInfo;

    public PlayerInfoScreen(MinecraftClient client) {
        super(Text.literal(""));
        assert client.player != null;
        this.playerStats = ModEntityComponents.LIVINGLEVEL.get(client.player);
        this.playerClassComponent = ModEntityComponents.PLAYERCLASS.get(client.player);
        Map<String, Object> infoKeys = createPlayerInfoKey(client);
        List<ListElement> listInfo = createListItems();

        this.playerInfo = new ScrollableTextList(listInfo, infoKeys);
        this.scrollableStat = new ScrollableStat(playerStats, client);
        this.scrollablePlayerInfo = new ScrollablePlayerInfo(client, playerStats, playerClassComponent);
    }

    public void updateValues(MinecraftClient client) {
        Map<String, Object> values = createPlayerInfoKey(client);
        this.playerInfo.updateValues(values);
    }

    private Map<String, Object> createPlayerInfoKey(MinecraftClient client) {
        Map<String, Object> values = new HashMap<>();
        assert client.player != null;

        // Combat stats
        values.put("phyd", client.player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE));
        values.put("meleed", client.player.getAttributeValue(ModEntityAttributes.MELEE_ATTACK_DAMAGE_FLAT));
        values.put("meleem", client.player.getAttributeValue(ModEntityAttributes.MELEE_ATTACK_DAMAGE_MULT) * 100f);
        values.put("projd", client.player.getAttributeValue(ModEntityAttributes.PROJECTILE_ATTACK_DAMAGE_FLAT));
        values.put("projm", client.player.getAttributeValue(ModEntityAttributes.PROJECTILE_ATTACK_DAMAGE_MULT) * 100f);
        values.put("mdmg", client.player.getAttributeValue(ModEntityAttributes.MAGIC_ATTACK_DAMAGE_FLAT));
        values.put("mdmgm", client.player.getAttributeValue(ModEntityAttributes.MAGIC_ATTACK_DAMAGE_MULT) * 100f);
        values.put("asp", client.player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED));

        // Precision stats
        values.put("acc", client.player.getAttributeValue(ModEntityAttributes.ACCURACY));
        values.put("ccn", client.player.getAttributeValue(ModEntityAttributes.CRIT_CHANCE) * 100f);
        values.put("cdmg", client.player.getAttributeValue(ModEntityAttributes.CRIT_DAMAGE) * 100f);
        values.put("bkdmg", client.player.getAttributeValue(ModEntityAttributes.BACK_ATTACK) * 100f);
        values.put("spedmg", client.player.getAttributeValue(ModEntityAttributes.SPECIAL_ATTACK) * 100f);
        values.put("dblatt", client.player.getAttributeValue(ModEntityAttributes.DOUBLE_ATTACK_CHANCE) * 100f);

        // Defensive stats
        values.put("hp", client.player.getHealth());
        values.put("maxhp", client.player.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH));
        values.put("dp", client.player.getAttributeValue(EntityAttributes.GENERIC_ARMOR));
        values.put("drec", (client.player.getAttributeValue(ModEntityAttributes.DAMAGE_REDUCTION) * 100f) +
                (DamageUtil.getArmorDamageReduction(client.player.getArmor()) * 100f));
        values.put("eva", client.player.getAttributeValue(ModEntityAttributes.EVASION));

        // Combat resistances
        values.put("mresis", client.player.getAttributeValue(ModEntityAttributes.MAGIC_RESISTANCE) * 100f);
        values.put("presis", client.player.getAttributeValue(ModEntityAttributes.PROJECTILE_RESISTANCE) * 100f);
        values.put("meleeresis", client.player.getAttributeValue(ModEntityAttributes.MELEE_RESISTANCE) * 100f);

        // Flat damage reductions
        values.put("flatmag", client.player.getAttributeValue(ModEntityAttributes.FLAT_MAGIC_REDUCTION));
        values.put("flatproj", client.player.getAttributeValue(ModEntityAttributes.FLAT_PROJECTILE_REDUCTION));
        values.put("flatmelee", client.player.getAttributeValue(ModEntityAttributes.FLAT_MELEE_REDUCTION));

        // Elemental damage stats
        values.put("fired", client.player.getAttributeValue(ModEntityAttributes.FIRE_ATTACK_DAMAGE_FLAT));
        values.put("firem", client.player.getAttributeValue(ModEntityAttributes.FIRE_ATTACK_DAMAGE_MULT) * 100f);
        values.put("coldd", client.player.getAttributeValue(ModEntityAttributes.COLD_ATTACK_DAMAGE_FLAT));
        values.put("coldm", client.player.getAttributeValue(ModEntityAttributes.COLD_ATTACK_DAMAGE_MULT) * 100f);
        values.put("elecd", client.player.getAttributeValue(ModEntityAttributes.ELECTRIC_ATTACK_DAMAGE_FLAT));
        values.put("elecm", client.player.getAttributeValue(ModEntityAttributes.ELECTRIC_ATTACK_DAMAGE_MULT) * 100f);
        values.put("waterd", client.player.getAttributeValue(ModEntityAttributes.WATER_ATTACK_DAMAGE_FLAT));
        values.put("waterm", client.player.getAttributeValue(ModEntityAttributes.WATER_ATTACK_DAMAGE_MULT) * 100f);
        values.put("windd", client.player.getAttributeValue(ModEntityAttributes.WIND_ATTACK_DAMAGE_FLAT));
        values.put("windm", client.player.getAttributeValue(ModEntityAttributes.WIND_ATTACK_DAMAGE_MULT) * 100f);
        values.put("holyd", client.player.getAttributeValue(ModEntityAttributes.HOLY_ATTACK_DAMAGE_FLAT));
        values.put("holym", client.player.getAttributeValue(ModEntityAttributes.HOLY_ATTACK_DAMAGE_MULT) * 100f);

        // Elemental resistances
        values.put("fireres", client.player.getAttributeValue(ModEntityAttributes.FIRE_RESISTANCE) * 100f);
        values.put("coldres", client.player.getAttributeValue(ModEntityAttributes.COLD_RESISTANCE) * 100f);
        values.put("elecres", client.player.getAttributeValue(ModEntityAttributes.ELECTRIC_RESISTANCE) * 100f);
        values.put("waterres", client.player.getAttributeValue(ModEntityAttributes.WATER_RESISTANCE) * 100f);
        values.put("windres", client.player.getAttributeValue(ModEntityAttributes.WIND_RESISTANCE) * 100f);
        values.put("holyres", client.player.getAttributeValue(ModEntityAttributes.HOLY_RESISTANCE) * 100f);

        // Flat elemental reductions
        values.put("flatfire", client.player.getAttributeValue(ModEntityAttributes.FLAT_FIRE_REDUCTION));
        values.put("flatcold", client.player.getAttributeValue(ModEntityAttributes.FLAT_COLD_REDUCTION));
        values.put("flatelec", client.player.getAttributeValue(ModEntityAttributes.FLAT_ELECTRIC_REDUCTION));
        values.put("flatwater", client.player.getAttributeValue(ModEntityAttributes.FLAT_WATER_REDUCTION));
        values.put("flatwind", client.player.getAttributeValue(ModEntityAttributes.FLAT_WIND_REDUCTION));
        values.put("flatholy", client.player.getAttributeValue(ModEntityAttributes.FLAT_HOLY_REDUCTION));

        // Resource & regeneration system
        values.put("nhrg", client.player.getAttributeValue(ModEntityAttributes.HEALTH_REGEN));
        values.put("hef", client.player.getAttributeValue(ModEntityAttributes.HEAL_EFFECTIVE) * 100f);
        values.put("resource", client.player.getAttributeValue(ModEntityAttributes.RESOURCE));
        values.put("resregen", client.player.getAttributeValue(ModEntityAttributes.RESOURCE_REGEN));
        values.put("resrate", client.player.getAttributeValue(ModEntityAttributes.RESOURCE_REGEN_RATE));

        // Casting system attributes
        values.put("vctflat", client.player.getAttributeValue(ModEntityAttributes.VCT_REDUCTION_FLAT));
        values.put("vctpct", client.player.getAttributeValue(ModEntityAttributes.VCT_REDUCTION_PERCENT));
        values.put("fctflat", client.player.getAttributeValue(ModEntityAttributes.FCT_REDUCTION_FLAT));
        values.put("fctpct", client.player.getAttributeValue(ModEntityAttributes.FCT_REDUCTION_PERCENT));
        values.put("skillvct", client.player.getAttributeValue(ModEntityAttributes.SKILL_VCT_REDUCTION));

        values.put("str", playerStats.getStatValue(StatTypes.STRENGTH));
        values.put("agi", playerStats.getStatValue(StatTypes.AGILITY));
        values.put("vit", playerStats.getStatValue(StatTypes.VITALITY));
        values.put("int", playerStats.getStatValue(StatTypes.INTELLIGENCE));
        values.put("dex", playerStats.getStatValue(StatTypes.DEXTERITY));
        values.put("luk", playerStats.getStatValue(StatTypes.LUCK));

        // Utility stats
        values.put("weight", client.player.getAttributeValue(ModEntityAttributes.MAX_WEIGHT));

        // Unified stats - showing effective values with breakdowns
        for (StatTypes statType : StatTypes.values()) {
            Stat stat = playerStats.getStatByType(statType);
            values.put(statType.name().toLowerCase(), formatUnifiedStat(stat));
        }

        return values;
    }

    private String formatUnifiedStat(Stat stat) {
        short effective = stat.getEffective();
        StringBuilder result = new StringBuilder(String.valueOf(effective));

        if (stat.getClassBonus() > 0 || stat.getTotalTemporaryModifiers() != 0) {
            result.append(" (");

            boolean needsPlus = false;
            if (stat.getCurrentValue() > 0) {
                result.append(stat.getCurrentValue());
                needsPlus = true;
            }

            if (stat.getClassBonus() > 0) {
                if (needsPlus) result.append("+");
                result.append(stat.getClassBonus()).append("c");
                needsPlus = true;
            }

            if (stat.getTotalTemporaryModifiers() != 0) {
                short temp = stat.getTotalTemporaryModifiers();
                if (needsPlus && temp > 0) result.append("+");
                result.append(temp).append("t");
            }

            result.append(")");
        }

        return result.toString();
    }

    private List<ListElement> createListItems() {
        List<ListElement> listElements = new ArrayList<>();

        // Unified Attributes Section
        listElements.add(new ListElement(Text.translatable("mamy.info.header_attributes"),
                Identifier.ofVanilla("icon/accessibility")));
        listElements.add(new ListElement(Text.translatable("mamy.info.strength")));
        listElements.add(new ListElement(Text.translatable("mamy.info.agility")));
        listElements.add(new ListElement(Text.translatable("mamy.info.vitality")));
        listElements.add(new ListElement(Text.translatable("mamy.info.intelligence")));
        listElements.add(new ListElement(Text.translatable("mamy.info.dexterity")));
        listElements.add(new ListElement(Text.translatable("mamy.info.luck")));

        // Combat Power Section
        listElements.add(new ListElement(Text.translatable("mamy.info.header_combat"),
                Mamy.id("hud/icon_sword")));
        listElements.add(new ListElement(Text.translatable("mamy.info.physical")));
        listElements.add(new ListElement(Text.translatable("mamy.info.melee_damage")));
        listElements.add(new ListElement(Text.translatable("mamy.info.melee_mult")));
        listElements.add(new ListElement(Text.translatable("mamy.info.projectile_damage")));
        listElements.add(new ListElement(Text.translatable("mamy.info.projectile_mult")));
        listElements.add(new ListElement(Text.translatable("mamy.info.magic_damage")));
        listElements.add(new ListElement(Text.translatable("mamy.info.magic_mult")));
        listElements.add(new ListElement(Text.translatable("mamy.info.attack_speed")));

        // Precision & Critical Section
        listElements.add(new ListElement(Text.translatable("mamy.info.header_precision"),
                Mamy.id("hud/icon_crosshair")));
        listElements.add(new ListElement(Text.translatable("mamy.info.accuracy")));
        listElements.add(new ListElement(Text.translatable("mamy.info.critical_chance")));
        listElements.add(new ListElement(Text.translatable("mamy.info.critical_damage")));
        listElements.add(new ListElement(Text.translatable("mamy.info.backattack_damage")));
        listElements.add(new ListElement(Text.translatable("mamy.info.special_damage")));
        listElements.add(new ListElement(Text.translatable("mamy.info.double_attack")));

        // Defensive Section
        listElements.add(new ListElement(Text.translatable("mamy.info.header_defense"),
                Identifier.ofVanilla("hud/heart/full")));
        listElements.add(new ListElement(Text.translatable("mamy.info.health")));
        listElements.add(new ListElement(Text.translatable("mamy.info.max_health")));
        listElements.add(new ListElement(Text.translatable("mamy.info.defense")));
        listElements.add(new ListElement(Text.translatable("mamy.info.damage_reduction")));
        listElements.add(new ListElement(Text.translatable("mamy.info.evasion")));
        listElements.add(new ListElement(Text.translatable("mamy.info.magic_resistance")));
        listElements.add(new ListElement(Text.translatable("mamy.info.projectile_resistance")));
        listElements.add(new ListElement(Text.translatable("mamy.info.melee_resistance")));
        listElements.add(new ListElement(Text.translatable("mamy.info.flat_magic_reduction")));
        listElements.add(new ListElement(Text.translatable("mamy.info.flat_projectile_reduction")));
        listElements.add(new ListElement(Text.translatable("mamy.info.flat_melee_reduction")));

        // Elemental Damage Section
        listElements.add(new ListElement(Text.translatable("mamy.info.header_elemental_damage"),
                Mamy.id("hud/icon_fire")));
        listElements.add(new ListElement(Text.translatable("mamy.info.fire_damage")));
        listElements.add(new ListElement(Text.translatable("mamy.info.fire_mult")));
        listElements.add(new ListElement(Text.translatable("mamy.info.cold_damage")));
        listElements.add(new ListElement(Text.translatable("mamy.info.cold_mult")));
        listElements.add(new ListElement(Text.translatable("mamy.info.electric_damage")));
        listElements.add(new ListElement(Text.translatable("mamy.info.electric_mult")));
        listElements.add(new ListElement(Text.translatable("mamy.info.water_damage")));
        listElements.add(new ListElement(Text.translatable("mamy.info.water_mult")));
        listElements.add(new ListElement(Text.translatable("mamy.info.wind_damage")));
        listElements.add(new ListElement(Text.translatable("mamy.info.wind_mult")));
        listElements.add(new ListElement(Text.translatable("mamy.info.holy_damage")));
        listElements.add(new ListElement(Text.translatable("mamy.info.holy_mult")));

        // Elemental Resistances Section
        listElements.add(new ListElement(Text.translatable("mamy.info.header_elemental_resist"),
                Mamy.id("hud/icon_shield")));
        listElements.add(new ListElement(Text.translatable("mamy.info.fire_resistance")));
        listElements.add(new ListElement(Text.translatable("mamy.info.cold_resistance")));
        listElements.add(new ListElement(Text.translatable("mamy.info.electric_resistance")));
        listElements.add(new ListElement(Text.translatable("mamy.info.water_resistance")));
        listElements.add(new ListElement(Text.translatable("mamy.info.wind_resistance")));
        listElements.add(new ListElement(Text.translatable("mamy.info.holy_resistance")));
        listElements.add(new ListElement(Text.translatable("mamy.info.flat_fire_reduction")));
        listElements.add(new ListElement(Text.translatable("mamy.info.flat_cold_reduction")));
        listElements.add(new ListElement(Text.translatable("mamy.info.flat_electric_reduction")));
        listElements.add(new ListElement(Text.translatable("mamy.info.flat_water_reduction")));
        listElements.add(new ListElement(Text.translatable("mamy.info.flat_wind_reduction")));
        listElements.add(new ListElement(Text.translatable("mamy.info.flat_holy_reduction")));

        // Resource & Regeneration Section
        listElements.add(new ListElement(Text.translatable("mamy.info.header_recovery"),
                Mamy.id("hud/recovery")));
        listElements.add(new ListElement(Text.translatable("mamy.info.nature_health_regen")));
        listElements.add(new ListElement(Text.translatable("mamy.info.heal_effective")));
        listElements.add(new ListElement(Text.translatable("mamy.info.max_resource")));
        listElements.add(new ListElement(Text.translatable("mamy.info.resource_regen")));
        listElements.add(new ListElement(Text.translatable("mamy.info.resource_regen_rate")));

        // Casting System Section
        listElements.add(new ListElement(Text.translatable("mamy.info.header_casting"),
                Mamy.id("hud/icon_cast")));
        listElements.add(new ListElement(Text.translatable("mamy.info.vct_flat_reduction")));
        listElements.add(new ListElement(Text.translatable("mamy.info.vct_percent_reduction")));
        listElements.add(new ListElement(Text.translatable("mamy.info.fct_flat_reduction")));
        listElements.add(new ListElement(Text.translatable("mamy.info.fct_percent_reduction")));
        listElements.add(new ListElement(Text.translatable("mamy.info.skill_vct_reduction")));

        // Utility Section
        listElements.add(new ListElement(Text.translatable("mamy.info.header_utility"),
                Mamy.id("hud/icon_utility")));
        listElements.add(new ListElement(Text.translatable("mamy.info.max_weight")));

        return listElements;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        assert client != null;
        updateValues(client);
        DrawContextUtils.fillScreen(context,0xEC141414);

        // Header
        renderHeader(context, 20);

        // Layout sections
        int contentY = 50;
        int contentHeight = this.height - contentY - 20;

        // Calculate section dimensions - only 2 sections now
        int leftWidth = (this.width * 2) / 5;  // 40% for left side
        int rightWidth = (this.width * 3) / 5; // 60% for right side
        int section1Height = contentHeight / 2;
        int section2Height = contentHeight / 2;

        // Section 1 (top-left) - ScrollableStat
        int section1X = 20;
        int section1Y = contentY;
        renderScrollableStatSection(context, section1X, section1Y, leftWidth - 30, section1Height , mouseX, mouseY, delta);

        // Section 2 (bottom-left) - ScrollablePlayerInfo
        int section2X = 20;
        int section2Y = contentY + section1Height + 10;
        renderScrollablePlayerInfoSection(context, section2X, section2Y, leftWidth - 30, section2Height - 10, mouseX, mouseY, delta);

        // Section 3 (right side - ScrollableTextList with search)
        int section3X = leftWidth + 20;
        int section3Y = contentY;

        this.playerInfo.render(context, this.textRenderer, section3X , section3Y, rightWidth - 40, contentHeight , 0.8f, delta, mouseX, mouseY);

        // Add separator lines
        renderSeparatorLines(context, leftWidth, contentY, contentHeight, section1Height);

        renderToastsOverScreen(context, delta);
    }

    private void renderScrollableStatSection(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, float delta) {
        this.scrollableStat.render(context, this.textRenderer, x , y, width, height, delta, mouseX, mouseY);
    }

    private void renderScrollablePlayerInfoSection(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, float delta) {
        this.scrollablePlayerInfo.render(context, this.textRenderer, x, y, width, height, delta, mouseX, mouseY);
    }

    private void renderSeparatorLines(DrawContext context, int leftWidth, int contentY, int contentHeight, int section1Height) {
        // Vertical line separating left and right sections
        int verticalLineX = leftWidth + 10;
        context.drawVerticalLine(verticalLineX, contentY, contentY + contentHeight, 0xFF404040);

        // Horizontal line separating top-left and bottom-left sections
        int horizontalLineY = contentY + section1Height + 5;
        context.drawHorizontalLine(20, verticalLineX, horizontalLineY , 0xFF404040);
    }

    private void renderHeader(DrawContext context, int headerHeight) {
        DrawContextUtils.drawRect(context, 0, 5, width, headerHeight, 0xE0000000);

        // Title
        Text title = Text.translatable("mamy.gui.player_info.title");
        int titleY = headerHeight / 2;
        context.drawTextWithShadow(textRenderer, title, 20, titleY, 0xFFDAA520);

        // Available points (right)
        int points = playerStats.getAvailableStatPoints();
        String pointText = points == 1 ? "Stat Point" : "Stat Points";
        Text pointsDisplay = Text.literal(points + " " + pointText);
        int pointsX = width - textRenderer.getWidth(pointsDisplay) - 20;
        context.drawTextWithShadow(textRenderer, pointsDisplay, pointsX, titleY, points > 0 ? 0xFFDAA520 : 0xFF888888);
    }

    private void renderToastsOverScreen(DrawContext context, float delta) {
        float deltaTime = delta / 16.0f;
        ToastRenderer.renderToasts(context, this.width, deltaTime);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int contentY = 50;
        int contentHeight = this.height - contentY - 20;

        int leftWidth = (this.width * 2) / 5;  // 40% for left side
        int rightWidth = (this.width * 3) / 5; // 60% for right side
        int section1Height = contentHeight / 2;
        int section2Height = contentHeight / 2;

        // Section 1 (top-left) - ScrollableStat
        int section1X = 20;
        int section1Y = contentY;
        int section1Width = leftWidth - 30;

        if (scrollableStat.isMouseOver(mouseX, mouseY, section1X, section1Y, section1Width, section1Height)) {
            if (scrollableStat.handleMouseClick(mouseX, mouseY, button)) return true;
        }

        // Section 2 (bottom-left) - ScrollablePlayerInfo
        int section2X = 20;
        int section2Y = contentY + section1Height + 10;
        int section2Width = leftWidth - 30;

        if (scrollablePlayerInfo.isMouseOver(mouseX, mouseY, section2X, section2Y, section2Width, section2Height - 10)) {
            if (scrollablePlayerInfo.handleMouseClick(mouseX, mouseY, button)) return true;
        }

        // Section 3 (right side) - ScrollableTextList with search
        int section3X = leftWidth + 20;
        int section3Y = contentY;

        if (playerInfo.isMouseOver(mouseX, mouseY, section3X, section3Y, rightWidth - 40, contentHeight)) {
            if (playerInfo.handleMouseClick(mouseX, mouseY, button)) return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int contentY = 50;
        int contentHeight = this.height - contentY - 20;

        int leftWidth = (this.width * 2) / 5;  // 40% for left side
        int rightWidth = (this.width * 3) / 5; // 60% for right side
        int section1Height = contentHeight / 2;
        int section2Height = contentHeight / 2;

        // Section 1 (top-left) - ScrollableStat
        int section1X = 20;
        int section1Y = contentY;
        int section1Width = leftWidth - 30;

        if (scrollableStat.isMouseOver(mouseX, mouseY, section1X, section1Y, section1Width, section1Height)) {
            if (scrollableStat.handleMouseScroll(mouseX, mouseY, horizontalAmount, verticalAmount)) return true;
        }

        // Section 2 (bottom-left) - ScrollablePlayerInfo
        int section2X = 20;
        int section2Y = contentY + section1Height + 10;
        int section2Width = leftWidth - 30;

        if (scrollablePlayerInfo.isMouseOver(mouseX, mouseY, section2X, section2Y, section2Width, section2Height - 10)) {
            if (scrollablePlayerInfo.handleMouseScroll(mouseX, mouseY, horizontalAmount, verticalAmount)) return true;
        }

        // Section 3 (right side) - ScrollableTextList with search
        int section3X = leftWidth + 20;
        int section3Y = contentY;

        if (playerInfo.isMouseOver(mouseX, mouseY, section3X, section3Y, rightWidth - 40, contentHeight)) {
            if (playerInfo.handleMouseScroll(mouseX, mouseY, horizontalAmount, verticalAmount)) return true;
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (scrollableStat.handleMouseDrag(mouseX, mouseY, button, deltaX, deltaY)) return true;
        if (scrollablePlayerInfo.handleMouseDrag(mouseX, mouseY, button, deltaX, deltaY)) return true;
        if (playerInfo.handleMouseDrag(mouseX, mouseY, button, deltaX, deltaY)) return true;

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        scrollableStat.handleMouseRelease(mouseX, mouseY, button);
        scrollablePlayerInfo.handleMouseRelease(mouseX, mouseY, button);
        playerInfo.handleMouseRelease(mouseX, mouseY, button);

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (playerInfo.handleKeyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (playerInfo.handleCharTyped(chr, modifiers)) {
            return true;
        }

        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}