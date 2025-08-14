package com.sypztep.mamy.client.event.tooltip;

import java.util.List;

import com.sypztep.mamy.common.util.NumberUtil;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import com.sypztep.mamy.common.data.ItemWeightEntry;
import com.sypztep.mamy.Mamy;

public class ItemWeightTooltip implements ItemTooltipCallback {
    public static void register() {
        ItemTooltipCallback.EVENT.register(new ItemWeightTooltip());
    }

    private static final Style ICONS = Style.EMPTY.withFont(Mamy.id("icons"));
    private static final String WEIGHT_ICON = "\u0020";

    @Override
    public void getTooltip(ItemStack stack, Item.TooltipContext tooltipContext, TooltipType tooltipType, List<Text> lines) {
        if (ItemWeightEntry.hasWeight(stack)) {
            float rawWeight = ItemWeightEntry.getWeight(stack);

            // Convert to best SI unit
            NumberUtil.WeightUnit unit = NumberUtil.WeightUnit.bestFit(rawWeight);
            double displayValue = unit.convertFromKg(rawWeight); // assuming weight is stored in kg

            MutableText line = Text.empty();
            line.append(Text.literal(WEIGHT_ICON).setStyle(ICONS));
            line.append(Text.literal(" Weight: ").formatted(Formatting.GRAY));
            line.append(Text.literal(String.format("%.2f", displayValue)).formatted(Formatting.YELLOW));
            line.append(Text.literal(" " + unit.getSymbol()).formatted(Formatting.DARK_GRAY));

            lines.add(line);
        }
    }
}
