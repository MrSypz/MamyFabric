package com.sypztep.mamy.client.screen.widget;

import com.sypztep.mamy.client.event.tooltip.ElementalTooltipHelper;
import com.sypztep.mamy.client.event.tooltip.ItemWeightTooltip;
import com.sypztep.mamy.common.component.living.PlayerWeightComponent;
import com.sypztep.mamy.common.data.ItemWeightEntry;
import com.sypztep.mamy.common.init.ModEntityComponents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class WeightBarWidget {
    // === CONSTANTS ===
    private static final int BG_COLOR = 0xFF2A2A2A;
    private static final int BORDER_COLOR = 0xFF000000;
    private static final int EQUIPMENT_COLOR = 0xFFFFD700;  // Yellow
    private static final int INVENTORY_COLOR = 0xFFE53E3E;  // Red
    private static final int TEXT_COLOR = 0xFFFFFFFF;       // White
    private static final int OVERWEIGHT_INDICATOR_COLOR = 0xFFFFFFFF; // White

    private static final int BAR_WIDTH = 176;
    private static final int BAR_HEIGHT = 2;
    private static final int ICON_WIDTH = 12;
    private static final float Z_LEVEL = 0.0f;

    public static void render(MinecraftClient client, DrawContext context, int mouseX, int mouseY) {
        if (client.player == null) return;
        if (!(client.currentScreen instanceof InventoryScreen screen)) return;

        PlayerWeightComponent component = ModEntityComponents.PLAYERWEIGHT.get(client.player);
        PlayerEntity player = client.player;

        int screenX = (screen.width - 176) / 2;
        int screenY = (screen.height - 166) / 2;

        // Relative to inventory
        // barX is screenX
        int barY = screenY + 179; // Relative to inventory

        double equipmentWeight = calculateEquipmentWeight(player);
        double inventoryWeight = calculateInventoryWeight(player);
        double totalWeight = equipmentWeight + inventoryWeight;
        double maxWeight = component.getMaxWeight();

        boolean isOverweight = totalWeight > maxWeight;

        // === HEADER ===
        int headerY = barY - 10;
        int headerX = screenX;

        // Draw weight icon
        context.drawText(client.textRenderer, ElementalTooltipHelper.createIconText(ItemWeightTooltip.WEIGHT_ICON),
                headerX, headerY, TEXT_COLOR, false);

        // Check hover
        boolean isHoveringIcon = mouseX >= headerX && mouseX <= headerX + ICON_WIDTH &&
                mouseY >= headerY && mouseY <= headerY + client.textRenderer.fontHeight;

        headerX += ICON_WIDTH;

        // Draw "Weight" label
        context.drawText(client.textRenderer, "Weight", headerX, headerY, TEXT_COLOR, false);

        // Draw weight values (right aligned)
        String weightText = String.format("%d/%d LT", (int) totalWeight, (int) maxWeight);
        int textWidth = client.textRenderer.getWidth(weightText);
        int weightTextX = screenX + BAR_WIDTH - textWidth;
        context.drawText(client.textRenderer, weightText, weightTextX, headerY, TEXT_COLOR, false);

        // === LOW-LEVEL OPTIMIZED BAR RENDERING ===
        renderWeightBarOptimized(context, screenX, barY, equipmentWeight, totalWeight, maxWeight, isOverweight);

        // === TOOLTIP ===
        if (isHoveringIcon) {
            renderWeightTooltip(context, client, mouseX, mouseY, equipmentWeight, inventoryWeight, totalWeight, maxWeight);
        }
    }

    private static void renderWeightBarOptimized(DrawContext context, int barX, int barY,
                                                 double equipmentWeight,
                                                 double totalWeight, double maxWeight, boolean isOverweight) {

        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        VertexConsumer buffer = context.getVertexConsumers().getBuffer(RenderLayer.getGui());

        double totalPercent = Math.min(totalWeight / maxWeight, 1.0);
        int totalFillWidth = (int) (BAR_WIDTH * totalPercent);

        int equipmentDisplayWidth = 0;
        int inventoryDisplayWidth = 0;

        if (totalWeight > 0 && totalFillWidth > 0) {
            double equipmentRatio = equipmentWeight / totalWeight;
            equipmentDisplayWidth = (int) (totalFillWidth * equipmentRatio);
            inventoryDisplayWidth = totalFillWidth - equipmentDisplayWidth;
        }

        // Border (outer rectangle)
        drawRect(buffer, matrix, barX - 1, barY - 1, barX + BAR_WIDTH + 1, barY + BAR_HEIGHT + 1, BORDER_COLOR);

        // Background
        drawRect(buffer, matrix, barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, BG_COLOR);

        // Equipment fill (yellow)
        if (equipmentDisplayWidth > 0)
            drawRect(buffer, matrix, barX, barY, barX + equipmentDisplayWidth, barY + BAR_HEIGHT, EQUIPMENT_COLOR);

        // Inventory fill (red)
        if (inventoryDisplayWidth > 0)
            drawRect(buffer, matrix, barX + equipmentDisplayWidth, barY, barX + totalFillWidth, barY + BAR_HEIGHT, INVENTORY_COLOR);

        // Overweight indicator line
        if (isOverweight && totalWeight > 0) {
            int maxLine = (int) (BAR_WIDTH * (maxWeight / totalWeight));
            if (maxLine < BAR_WIDTH) drawRect(buffer, matrix, barX + maxLine - 1, barY - 2, barX + maxLine + 1, barY + BAR_HEIGHT + 2, OVERWEIGHT_INDICATOR_COLOR);
        }
//        context.draw(); minecraft draw
        context.getVertexConsumers().drawCurrentLayer();
    }

    private static void drawRect(VertexConsumer buffer, Matrix4f matrix, int x1, int y1, int x2, int y2, int color) {
        if (x1 > x2) { int temp = x1; x1 = x2; x2 = temp; }
        if (y1 > y2) { int temp = y1; y1 = y2; y2 = temp; }

        buffer.vertex(matrix, x1, y1, Z_LEVEL).color(color);
        buffer.vertex(matrix, x1, y2, Z_LEVEL).color(color);
        buffer.vertex(matrix, x2, y2, Z_LEVEL).color(color);
        buffer.vertex(matrix, x2, y1, Z_LEVEL).color(color);
    }

    private static void renderWeightTooltip(DrawContext context, MinecraftClient client, int mouseX, int mouseY,
                                            double equipmentWeight, double inventoryWeight, double totalWeight, double maxWeight) {
        List<Text> tooltipLines = new ArrayList<>();

        tooltipLines.add(Text.literal("Weight Breakdown").formatted(Formatting.WHITE, Formatting.BOLD));
        tooltipLines.add(Text.empty());

        tooltipLines.add(Text.literal(" Equipment: ").formatted(Formatting.YELLOW)
                .append(Text.literal(String.format("%,.1f LT", equipmentWeight)).formatted(Formatting.WHITE)));

        tooltipLines.add(Text.literal(" Inventory: ").formatted(Formatting.RED)
                .append(Text.literal(String.format("%,.1f LT", inventoryWeight)).formatted(Formatting.WHITE)));

        tooltipLines.add(Text.empty());

        if (totalWeight > maxWeight) {
            double overweightPercent = ((totalWeight / maxWeight) - 1.0) * 100;
            tooltipLines.add(Text.empty());
            tooltipLines.add(Text.literal("⚠ Overweight: ").formatted(Formatting.RED)
                    .append(Text.literal(String.format("+%,.1f%%", overweightPercent)).formatted(Formatting.DARK_RED)));
        }

        context.drawTooltip(client.textRenderer, tooltipLines, mouseX, mouseY);
    }

    private static double calculateEquipmentWeight(PlayerEntity player) {
        double weight = 0.0;
        for (ItemStack armorStack : player.getArmorItems())
            if (!armorStack.isEmpty()) weight += ItemWeightEntry.getTotalWeight(armorStack);

        ItemStack offhandStack = player.getOffHandStack();
        if (!offhandStack.isEmpty()) weight += ItemWeightEntry.getTotalWeight(offhandStack);

        return weight;
    }

    private static double calculateInventoryWeight(PlayerEntity player) {
        double weight = 0.0;
        PlayerInventory inventory = player.getInventory();
        for (int i = 9; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty()) weight += ItemWeightEntry.getTotalWeight(stack);
        }
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty()) weight += ItemWeightEntry.getTotalWeight(stack);
        }
        return weight;
    }
}