package com.sypztep.mamy.client.screen.widget;

import com.sypztep.mamy.ModConfig;
import com.sypztep.mamy.common.component.living.LivingLevelComponent;
import com.sypztep.mamy.common.payload.IncreaseStatsPayloadC2S;
import com.sypztep.mamy.common.system.stat.Stat;
import com.sypztep.mamy.common.system.stat.StatTypes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public final class IncreasePointButton extends ActionWidgetButton {
    private final StatTypes statType; // Use StatTypes enum instead of String
    private final int pointsToIncrease;
    private int requiredStatPoints;

    public IncreasePointButton(int x, int y, int width, int height, Text message,
                               LivingLevelComponent stats, StatTypes statType, int pointsToIncrease, MinecraftClient client) {
        super(x, y, width, height, message, stats, client);
        this.statType = statType;
        this.pointsToIncrease = pointsToIncrease;
        this.requiredStatPoints = 1;
        initializeTooltip();
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (stats == null) {
            return;
        }

        Stat stat = stats.getStatByType(statType);
        if (stat == null) {
            return;
        }

        if (stat.getValue() >= ModConfig.maxStatValue) {
            return;
        }

        int requiredPoints = stat.getIncreasePerPoint() * pointsToIncrease;
        int availablePoints = stats.getAvailableStatPoints();

        if (availablePoints >= requiredPoints) {
            performAction();
            playClickSound();
        }
    }

    private void performAction() {
        IncreaseStatsPayloadC2S.send(statType);
    }

    private void initializeTooltip() {
        if (stats == null) return;

        Stat stat = stats.getStatByType(statType);
        if (stat != null) {
            this.requiredStatPoints = stat.getIncreasePerPoint() * pointsToIncrease;
        }
    }

    @Override
    protected void updateAnimations(float delta) {
        super.updateAnimations(delta);

        initializeTooltip();

        boolean canAfford = stats != null && stats.getAvailableStatPoints() >= requiredStatPoints;
        boolean isMaxed = false;

        if (stats != null) {
            Stat stat = stats.getStatByType(statType);
            if (stat != null) isMaxed = stat.getValue() >= ModConfig.maxStatValue;
        }

        setEnabled(canAfford && !isMaxed);
    }

    @Override
    protected void renderAdditionalOverlays(DrawContext context, int mouseX, int mouseY, float delta, boolean isHovered, boolean isPressed) {
        if (isHovered && ModConfig.tooltipinfo) renderStatTooltip(context, mouseX, mouseY);
    }

    private void renderStatTooltip(DrawContext context, int mouseX, int mouseY) {
        if (stats == null) return;

        Stat stat = stats.getStatByType(statType);
        if (stat == null) return;

        List<Text> tooltipLines = buildCompleteTooltip(stat);
        context.drawTooltip(client.textRenderer, tooltipLines, mouseX, mouseY);
    }

    private List<Text> buildCompleteTooltip(Stat stat) {
        List<Text> tooltip = new ArrayList<>();

        boolean isMaxed = stat.getValue() >= ModConfig.maxStatValue;

        if (isMaxed) {
            tooltip.add(Text.of("§6§l" + statType.getAka() + " §c§l(MAX)"));
            tooltip.add(Text.literal("♦  ").formatted(Formatting.GOLD)
                    .append(Text.literal("Current: ").formatted(Formatting.GRAY))
                    .append(Text.literal(String.valueOf(ModConfig.maxStatValue)).formatted(Formatting.GOLD)));

            tooltip.add(Text.literal("♠  ").formatted(Formatting.RED)
                    .append(Text.literal("Points spent: ").formatted(Formatting.GRAY))
                    .append(Text.literal(String.valueOf(stat.getTotalPointsUsed())).formatted(Formatting.WHITE)));

            tooltip.add(Text.literal(""));
            tooltip.add(Text.literal("§7This stat is at maximum level!"));

            List<Text> descriptions = stat.getEffectDescription(0);
            tooltip.addAll(descriptions);

        } else {
            tooltip.add(Text.of("§6§l" + statType.getAka()));
            tooltip.add(Text.literal("♣  ").formatted(Formatting.DARK_GREEN)
                    .append(Text.literal("Current: ").formatted(Formatting.GRAY))
                    .append(Text.literal(String.valueOf(stat.getValue())).formatted(Formatting.WHITE))
                    .append(Text.literal("/").formatted(Formatting.DARK_GRAY))
                    .append(Text.literal(String.valueOf(ModConfig.maxStatValue)).formatted(Formatting.GRAY)));

            tooltip.add(Text.literal("♦  ").formatted(Formatting.RED)
                    .append(Text.literal("Cost per point: ").formatted(Formatting.GRAY))
                    .append(Text.literal(String.valueOf(stat.getIncreasePerPoint())).formatted(Formatting.WHITE)));

            // Show effect descriptions
            List<Text> descriptions = stat.getEffectDescription(pointsToIncrease);
            tooltip.addAll(descriptions);
        }
        return tooltip;
    }

    @Override
    protected int getBackgroundColor() {
        return 0xFF1A472A; // Dark green
    }

    @Override
    protected int getHoverBackgroundColor() {
        return 0xFF2D5A3D; // Lighter green
    }

    @Override
    protected int getPressedBackgroundColor() {
        return 0xFF0F2C18; // Darker green
    }

    @Override
    protected int getBorderColor() {
        return 0xFF4CAF50; // Green border
    }

    @Override
    protected int getHoverBorderColor() {
        return 0xFF66BB6A; // Lighter green border
    }

    @Override
    protected int getHoverTextColor() {
        return 0xFF81C784; // Light green text on hover
    }

    @Override
    protected boolean hasTextShadow() {
        return true; // Enable text shadow for better readability
    }
}